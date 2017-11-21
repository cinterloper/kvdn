package net.iowntheinter.kvdn.storage

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.Counter
import io.vertx.core.shareddata.LocalMap
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.counter.impl.CtrTx
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.kv.key.impl.LocalKeyProvider
import net.iowntheinter.kvdn.storage.kv.key.keyProvider
import net.iowntheinter.kvdn.storage.kv.kvdata
import net.iowntheinter.kvdn.storage.lock.impl.LTx


class kvdnSession {
    static final ACCESS_CACHE_LOC = '__KVDN_ACCESS_CACHE'
    enum sessionType {
        NATIVE_SESSION, PROTOCOL_SERVER, PROXY_SERVER
    }

    enum txFlags {
        READ_ONLY
    }

    enum dataType {
        KV, CTR, LCK
    }
    boolean initalized = false
    boolean tracking = false
    Vertx vertx
    Set outstandingTX
    //roMode should not issue new sessions, new tx's on existing sessions will get the ROFLAG
    boolean roMode = false
    boolean transition = false
    Set txflags
    EventBus eb
    Logger logger
    def sessionid, keyprov
    final JsonObject config
    def D, M

    ArrayList<txnHook> preHooks = []
    ArrayList<txnHook> postHooks = []
    LocalMap accessCache

    void sessionPreTxHooks(tx, cb) {
        _hookCaller(tx as kvdnTX, this.preHooks, 0, cb)
    }

    void sessionPostTxHooks(tx, cb) {
        _hookCaller(tx as kvdnTX, this.postHooks, 0, cb)
    }
    Closure txEndHandler = { KvTx tx, cb ->
        sessionPostTxHooks(tx, cb)
    }
/*
 * this is a recursive traversal of the hooks array
 * it jumps into the next hook, then the hook jumps either back into a new _hookCaller call, or straight to the next cb
 * it will probably explode if you have an unreasonable number of hooks to call
 *
 */
    def txHookLoader = { String it ->
        return this.class.classLoader.loadClass(it)?.newInstance(vertx as Vertx, this) as txnHook
    }

    void _hookCaller(kvdnTX tx, ArrayList<txnHook> hooks, int ptr, cb) {
        logger.trace("inside hook caller ptr: $ptr hooks: $hooks")
        if (ptr != hooks.size()) {

            def nxt = hooks[ptr] as txnHook
            logger.trace("calling hook")

            ptr++
            nxt.call(tx, this, { //trampoline
                _hookCaller(tx, hooks, ptr, cb)
            })
        } else
            cb()
    }

    void processConfiguredHooks() {
        JsonArray configuredPreHooks = config.getJsonArray('preHooks')
        JsonArray configuredPostHooks = config.getJsonArray('postHooks')
        configuredPreHooks.each { name ->
            preHooks.add(txHookLoader(name as String))
        }
        configuredPostHooks.each { name ->
            postHooks.add(txHookLoader(name as String))
        }

    }

    kvdnSession(Vertx vx, stype = sessionType.NATIVE_SESSION) {
        vertx = vx
        JsonObject Vconfig = vertx.getOrCreateContext().config()
        config = Vconfig.getJsonObject('kvdn') ?: new JsonObject()

        txflags = []
        outstandingTX = new HashSet()
        sessionid = UUID.randomUUID().toString()
        logger = new LoggerFactory().getLogger("Kvdnsession:${sessionid.toString()}")
        eb = vertx.eventBus()
        accessCache = vertx.sharedData().getLocalMap(ACCESS_CACHE_LOC)

        String configured_data = config.getString('data_implementation') ?:
                'net.iowntheinter.kvdn.storage.kv.data.defaultDataImpl'
        try {
            this.D = txHookLoader(configured_data) as kvdata
            this.preHooks.addAll((D as kvdata).getPreHooks())
            this.postHooks.addAll((D as kvdata).getPostHooks())
        } catch (e) {
            e.printStackTrace()
            logger.fatal("could not load data impl $configured_data: ${e.getMessage()}")
            throw e
        }

        processConfiguredHooks()
        String configured_provider
        try {
           configured_provider= config.getString('key_provider') ?:
                    'net.iowntheinter.kvdn.storage.kv.key.impl.LocalKeyProvider'
            //  'net.iowntheinter.kvdn.storage.kv.key.impl.CRDTKeyProvider' // not working right now
            try {
                this.keyprov = this.class.classLoader.loadClass(configured_provider)?.newInstance(vertx, this.D) as keyProvider
            } catch (e) {
                e.printStackTrace()
                logger.fatal("could not load key provider $configured_provider : ${e.getMessage()}")
                throw e //erm this is pretty fatal
            }
            //load cluster key provider
        } catch (e) { // in memory mode
            this.keyprov = new LocalKeyProvider(vertx)
        }
        logger.info("CONFIGURED PROVIDER: "+ this.keyprov)

        logger.trace("starting new kvdn session with clustered = ${vertx.isClustered()} keyprovider = ${this.keyprov}")

    }

    void init(Handler cb, Handler error_cb) {
        zeroState(vertx, { kvdnSession s ->
            s.initalized = true
            cb.handle(Future.succeededFuture())
        }, error_cb)
    }
    /*
     * This is the entrypoint to using the kv api, get your transactions here.
     *
     * @param strAddr storage address
     * @return kvdnTX your transaction
     */

    def newTx(String strAddr, datatype = dataType.KV) {
        if (!initalized) {
            throw new Exception("kvdn session needs to be init(cb,ecb) before you use it")
        } else {
            def txid = UUID.randomUUID()
            outstandingTX.add(txid)
            switch (datatype) {
                case dataType.KV:
                    return (new KvTx(strAddr, txid, this, vertx))
                    break
                case dataType.CTR:
                    return (new CtrTx(strAddr, txid, this, vertx))
                    break
                case dataType.LCK:
                    return (new LTx(strAddr, txid, this, vertx))
                    break
                default:
                    return (null)
            }
        }
    }
/*
 * you must call this as the last action
 */

    void finishTx(kvdnTX tx, cb) {
        outstandingTX.remove(tx.txid)
        txEndHandler(tx, {
            if (tracking)
                accessCache.put(tx.strAddr, tx.txid)
            else
                accessCache.putIfAbsent(tx.strAddr, true)
            cb()
        })
    }


    def onWrite_f(String strAddr, String key = null, Closure cb) {
        eb.consumer("_KVDN_+${strAddr}", { message -> //listen for updates on this key
            if ((key == null) || (message.body() == key))
                cb(message.body())
        })
        return this
    }

    def onDelete_f(String strAddr, String key = null, Closure cb) {
        eb.consumer("_KVDN_-${strAddr}", { message -> //listen for deletes on this keyset
            if ((key == null) || (message.body() == key))
                cb(message.body())
        })
        return this
    }


    MessageConsumer onWrite(String strAddr, String key = null, Handler<JsonObject> cb) {
        return eb.consumer("_KVDN_+${strAddr}", { message -> //listen for updates on this key
            if ((key == null) || (message.body() == key))
                cb.handle(message.body() as JsonObject)
        })
    }


    MessageConsumer onDelete(String strAddr, String key = null, Handler<JsonObject> cb) {
        return eb.consumer("_KVDN_-${strAddr}", { message -> //listen for deletes on this key
            if ((key == null) || (message.body() == key))
                cb.handle(message.body() as JsonObject)
        })
    }

    void adminCommandListener() {
        eb.consumer("_KVDN_ADMIN_COMMANDS", { event ->
            JsonObject cmd = event.body() as JsonObject
            switch (cmd.getString("CMD")) {

            }
        })
    }

    private void zeroState(Vertx v, Handler cb, Handler error_cb) {
        boolean goodstate = false
        assert (!txflags.contains(txFlags.READ_ONLY) && !roMode && !transition)
        //when initializing a session, there should be no outstanding admin operations
        v.sharedData().getCounter("_KVDN_ADMIN_OPERATIONS", { ar ->
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
