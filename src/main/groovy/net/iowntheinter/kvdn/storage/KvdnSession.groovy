package net.iowntheinter.kvdn.storage

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.Counter
import io.vertx.core.shareddata.LocalMap
import net.iowntheinter.kvdn.KvdnTX
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.kv.key.impl.LocalKeyProvider
import net.iowntheinter.kvdn.storage.kv.key.KeyProvider
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.meta.impl.storeMetaKVHook
import net.iowntheinter.kvdn.util.KvdnSessionInterface

//import net.iowntheinter.kvdn.storage.counter.impl.CtrTx
//import net.iowntheinter.kvdn.storage.lock.impl.LTx

@TypeChecked
@CompileStatic
class KvdnSession implements KvdnSessionInterface {
    static final String ACCESS_CACHE_LOC = '__KVDN_ACCESS_CACHE'
    private String cname = this.getClass().getName()
    private Logger logger

    enum SESSIONTYPE {
        NATIVE_SESSION, PROTOCOL_SERVER, PROXY_SERVER
    }

    enum TXFLAGS {
        READ_ONLY
    }

    enum DATATYPE {
        KV, CTR, LCK
    }
    boolean initalized = false
    boolean tracking = false
    private Vertx vertx
    Set outstandingTX
    //roMode should not issue new sessions, new kvdnTX's on existing sessions will get the ROFLAG
    boolean roMode = false
    boolean transition = false
    Set txflags
    EventBus eb
    String sessionid
    KeyProvider keyprov
    final JsonObject config
    public KVData D, M

    ArrayList<TXNHook> preHooks = []
    ArrayList<TXNHook> postHooks = []
    LocalMap accessCache

    void sessionPreTxHooks(KvdnTX tx, Handler cb) {
        _hookCaller(tx as KvdnTX, this.preHooks, 0, cb)
    }

    void sessionPostTxHooks(KvdnTX tx, Handler cb) {
        _hookCaller(tx as KvdnTX, this.postHooks, 0, cb)
    }
    Closure txEndHandler = { KvTx tx, Handler cb ->
        sessionPostTxHooks(tx, cb)
    }
/*
 * this is a recursive traversal of the hooks array
 * it jumps into the next hook, then the hook jumps either back into a new _hookCaller call, or straight to the next cb
 * it will probably explode if you have an unreasonable number of hooks to call
 *
 */

    private TXNHook txHookLoader(String it) {
        return this.class.classLoader.loadClass(it)?.newInstance(vertx as Vertx, this) as TXNHook
    }

    void _hookCaller(KvdnTX tx, ArrayList<TXNHook> hooks, int ptr, Handler cb) {
        logger.debug("inside hook caller ptr: $ptr hooks: $hooks")
        if (ptr != hooks.size()) {

            TXNHook nxt = hooks[ptr] as TXNHook
            logger.debug("calling hook ptr $ptr size ${hooks.size()} ${nxt.class}")

            ptr++
            //skip calling metadata hook on metadata map
            if ((nxt.type == TXNHook.HookType.META_HOOK) && tx.strAddr == '__METADATA_MAP') {
                logger.debug("Skipping meta hook to next hook ptr $ptr size ${hooks.size()} ${nxt.class}")
                _hookCaller(tx, hooks, ptr, cb) //trampoline
            } else {
                logger.debug("calling next hook ptr $ptr size ${hooks.size()} ${nxt.class}")
                nxt.call(tx, this, { AsyncResult res ->
                    logger.debug("called next hook ptr $ptr size ${hooks.size()} ${nxt.class}")
                    _hookCaller(tx, hooks, ptr, cb) //trampoline
                })
            }

        } else
            cb.handle(Future.succeededFuture())
    }

    void processConfiguredHooks() {
        JsonArray configuredPreHooks = config.getJsonArray('preHooks')
        JsonArray configuredPostHooks = config.getJsonArray('postHooks')
        configuredPreHooks.each { name ->
            preHooks.add(txHookLoader(name as String))
        }
        postHooks.add(new storeMetaKVHook())
        configuredPostHooks.each { name ->
            postHooks.add(txHookLoader(name as String))
        }
    }

    KvdnSession(Vertx vertx, stype = SESSIONTYPE.NATIVE_SESSION) {
        vertx.sharedData().getCounter("sessions", { AsyncResult<Counter> ar ->
            Counter c = ar.result()
            c.incrementAndGet({ AsyncResult<Long> igr ->
                LoggerFactory.getLogger(this.class.getName()).error("SESSION NUMBER:" + igr.result())
            })
        })

        sessionid = UUID.randomUUID().toString()
        String loggerName = cname + ":" + sessionid.toString()
        logger = new LoggerFactory().getLogger(loggerName)

        this.vertx = vertx
        JsonObject Vconfig = this.vertx.getOrCreateContext().config()
        logger.trace("VERTX CONFIG:" + Vconfig.encodePrettily())
        config = Vconfig.getJsonObject('kvdn') ?: new JsonObject()

        logger.trace("KVDN CONFIG:" + config.encodePrettily())

        txflags = []
        outstandingTX = new HashSet()
        eb = this.vertx.eventBus()
        accessCache = this.vertx.sharedData().getLocalMap(ACCESS_CACHE_LOC)

        String configured_data = config.getString('data_implementation') ?:
                'net.iowntheinter.kvdn.storage.kv.data.defaultDataImpl'
        try {
            this.D = txHookLoader(configured_data) as KVData
            this.preHooks.addAll((D as KVData).getPreHooks())
            this.postHooks.addAll((D as KVData).getPostHooks())
        } catch (e) {
            e.printStackTrace()
            logger.fatal("could not load data impl $configured_data: ${e.getMessage()}")
            throw e
        }

        processConfiguredHooks()
        String configured_provider
        try {
            configured_provider = config.getString('key_provider') ?:
                    'net.iowntheinter.kvdn.storage.kv.key.impl.LocalKeyProvider'
            //  'net.iowntheinter.kvdn.storage.kv.key.impl.CRDTKeyProvider' // not working right now
            try {
                this.keyprov = this.class.classLoader.loadClass(configured_provider)?.newInstance(this.vertx, this.D as KVData) as KeyProvider
            } catch (e) {
                e.printStackTrace()
                logger.fatal("could not load key provider $configured_provider : ${e.getMessage()}")
                throw e //erm this is pretty fatal
            }
            //load cluster key provider
        } catch (e) { // in memory mode
            logger.trace("Defaulting to LocalKeyProvider because of ${e.message}")
        }
        if (this.keyprov == null)
            this.keyprov = new LocalKeyProvider(this.vertx, D as KVData)

        logger.debug("CONFIGURED PROVIDER: " + this.keyprov)

        logger.trace("starting new kvdn session with clustered = ${this.vertx.isClustered()} keyprovider = ${this.keyprov}")

    }

    void init(Handler cb, Handler error_cb) {
        zeroState(vertx, { KvdnSession s ->
            s.initalized = true
            cb.handle(Future.succeededFuture())
        }, error_cb)
    }
    /*
     * This is the entrypoint to using the kv api, get your transactions here.
     *
     * @param strAddr storage address
     * @return KvdnTX your transaction
     */

    KvdnTX newTx(String strAddr, datatype = DATATYPE.KV, String mimeType = null) {
        if (mimeType != null && DATATYPE != DATATYPE.KV)
            throw new Exception("CANNOT DECLARE A MIMETYPE ON ${DATATYPE.toString()}")

        if (datatype == DATATYPE.KV && mimeType == null)
            mimeType = "text/plain"

        if (!initalized) {
            throw new Exception("kvdn session needs to be init(cb,ecb) before you use it")
        } else {
            UUID txid = UUID.randomUUID()
            outstandingTX.add(txid)
            switch (datatype) {
                case DATATYPE.KV:
                    return (new KvTx(strAddr, mimeType, txid, this, vertx))
                    break
                case DATATYPE.CTR:
                    return null //(new CtrTx(strAddr, txid, this, vertx))
                    break
                case DATATYPE.LCK:
                    return null //(new LTx(strAddr, txid, this, vertx))
                    break
                default:
                    return (null)
            }
        }
    }
/*
 * you must call this as the last action
 */

    void finishTx(KvdnTX tx, Handler cb) {
        outstandingTX.remove(tx.txid)
        txEndHandler(tx, {
            if (tracking)
                accessCache.put(tx.strAddr, tx.txid)
            else
                accessCache.putIfAbsent(tx.strAddr, true)
            cb.handle(Future.succeededFuture())
        })
    }


    KvdnSession onWrite_f(String strAddr, String key = null, Handler cb) {
        eb.consumer("_KVDN_+${strAddr}", { Message message -> //listen for updates on this key
            if ((key == null) || (message.body() == key))
                cb.handle(message.body())
        })
        return this
    }

    KvdnSession onDelete_f(String strAddr, String key = null, Handler cb) {
        eb.consumer("_KVDN_-${strAddr}", { Message message -> //listen for deletes on this keyset
            if ((key == null) || (message.body() == key))
                cb.handle(message.body())
        })
        return this
    }


    MessageConsumer onWrite(String strAddr, String key = null, Handler<JsonObject> cb) {
        return eb.consumer("_KVDN_+${strAddr}", { Message message -> //listen for updates on this key
            if ((key == null) || (message.body() == key))
                cb.handle(message.body() as JsonObject)
        })
    }


    MessageConsumer onDelete(String strAddr, String key = null, Handler<JsonObject> cb) {
        return eb.consumer("_KVDN_-${strAddr}", { Message message -> //listen for deletes on this key
            if ((key == null) || (message.body() == key))
                cb.handle(message.body() as JsonObject)
        })
    }

    void adminCommandListener() {
        eb.consumer("_KVDN_ADMIN_COMMANDS", { Message event ->
            JsonObject cmd = event.body() as JsonObject
            switch (cmd.getString("CMD")) {

            }
        })
    }

    private void zeroState(Vertx vertx, Handler cb, Handler error_cb) {
        boolean goodstate = false
        assert (!txflags.contains(TXFLAGS.READ_ONLY) && !roMode && !transition)
        //when initializing a session, there should be no outstanding admin operations
        vertx.sharedData().getCounter("_KVDN_ADMIN_OPERATIONS", { AsyncResult<Counter> ar ->
            try {
                assert ar.succeeded()
            } catch (gce) {
                error_cb.handle(gce)
            }
            Counter l = ar.result()
            l.get({ AsyncResult r ->
                try {
                    assert r.succeeded()
                } catch (gre) {
                    error_cb.handle(gre)
                }
                try {
                    assert r.result() == 0
                    goodstate = true
                } catch (ase) {
                    error_cb.handle(ase)
                }
                if (goodstate)
                    cb.handle(this)
            })
        })
    }
}
