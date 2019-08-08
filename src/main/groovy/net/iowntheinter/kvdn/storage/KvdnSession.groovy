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
import io.vertx.core.impl.VertxInternal
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.LocalMap
import io.vertx.core.shareddata.impl.SharedDataImpl
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.query.QueryProvider
import net.iowntheinter.kvdn.storage.counter.impl.CtrOp
import net.iowntheinter.kvdn.storage.kv.impl.KVIterator
import net.iowntheinter.kvdn.storage.kv.impl.KvOp
import net.iowntheinter.kvdn.storage.kv.key.impl.LocalKeyProvider
import net.iowntheinter.kvdn.storage.kv.key.KeyProvider
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.meta.impl.StoreMetaKVHook
import net.iowntheinter.kvdn.def.KvdnSessionInterface
import net.iowntheinter.kvdn.storage.queue.impl.QueOp

import java.lang.reflect.Array

//import net.iowntheinter.kvdn.storage.counter.impl.CtrOp
//import net.iowntheinter.kvdn.storage.lock.impl.LOperation

@TypeChecked
@CompileStatic
class KvdnSession implements KvdnSessionInterface {
    static final String ACCESS_CACHE_LOC = '__KVDN_ACCESS_CACHE'
    private String cname = this.getClass().getName()
    private Logger logger

    Map<String, QueryProvider> queryEngines = [:]
    enum SESSIONTYPE {
        NATIVE_SESSION, PROTOCOL_SERVER, PROXY_SERVER
    }

    enum TXFLAGS {
        READ_ONLY
    }

    enum DATATYPE {
        KV, CTR, LCK, QUE
    }
    boolean initalized = false
    boolean tracking = false
    Vertx vertx
    Set outstandingTX
    //roMode should not issue new sessions, new kvdnTX's on existing sessions will take the ROFLAG
    boolean roMode = false
    boolean transition = false
    Set txflags
    EventBus eb
    String sessionid
    KeyProvider keyprov
    final JsonObject config
    public KVData D, M

    ArrayList<TXNHook> preHooks = new ArrayList<>()
    ArrayList<TXNHook> postHooks = new ArrayList<>()
    LocalMap accessCache
    Map<String, JsonObject> featureMap = [:]

    void registerFeature(String featureKey, JsonObject featureData) {
        featureMap[featureKey] = featureData
    }

    void sessionPreOpHooks(KvdnOperation tx, Handler cb) {
        _hookCaller(tx as KvdnOperation, this.preHooks, 0, cb)
    }

    void sessionPostOpHooks(KvdnOperation tx, Handler cb) {
        _hookCaller(tx as KvdnOperation, this.postHooks, 0, cb)
    }

    void opCompletionHandler(KvdnOperation tx, Handler<AsyncResult> cb) {
        sessionPostOpHooks(tx, cb)
    }
/*
 * this is a recursive traversal of the hooks array
 * it jumps into the next hook, then the hook jumps either back into a new _hookCaller call, or straight to the next cb
 * it will probably explode if you have an unreasonable number of hooks to call
 *
 */

    private QueryProvider QueryProviderLoader(String it) {
        QueryProvider result = this.class.classLoader.loadClass(it)?.newInstance([vertx as Vertx, this].toArray()) as QueryProvider
        if (result == null)
            throw new Exception("could not load $it")
        return result
    }

    private KVData DataImplLoader(String it) {
        KVData result = this.class.classLoader.loadClass(it)?.newInstance([vertx as Vertx, this].toArray()) as KVData
        if (result == null)
            throw new Exception("could not load $it")
        return result
    }

    private TXNHook txHookLoader(String it) {
        TXNHook result = this.class.classLoader.loadClass(it)?.newInstance([vertx as Vertx, this].toArray()) as TXNHook
        if (result == null)
            throw new Exception("could not load $it")
        return result
    }

    interface AsyncIterOp {
        void call(MapEntry data, Handler<AsyncResult> cb)
    }


    KVIterator newIterator(String straddr, Handler<AsyncResult> fin) {
        return new KVIterator(straddr, this, fin)
    }

    void _hookCaller(KvdnOperation tx, ArrayList<TXNHook> hooks, int ptr, Handler cb) {
        logger.debug("inside hook caller ptr: $ptr hooks: $hooks")
        if (ptr != hooks.size()) {
            TXNHook nxt = hooks[ptr] as TXNHook
            logger.debug("calling hook ptr $ptr size ${hooks.size()} ${nxt.class}")
            ptr++
            //skip calling metadata hook on metadata map
            if ((nxt.type == TXNHook.HookType.META_HOOK) && (tx.strAddr == '__METADATA_MAP' || tx.strAddr == '__DATATYPE_MAP')) {
                logger.trace("Skipping meta hook to next hook ptr $ptr size ${hooks.size()} ${nxt.class}")
                _hookCaller(tx, hooks, ptr, cb) //trampoline
            } else {
                logger.trace("calling next hook ptr $ptr size ${hooks.size()} ${nxt.class}")
                nxt.call(tx, this, { AsyncResult res ->
                    logger.trace("called next hook ptr $ptr size ${hooks.size()} ${nxt.class}")
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
        postHooks.add(new StoreMetaKVHook())
        configuredPostHooks.each { name ->
            postHooks.add(txHookLoader(name as String))
        }
    }

    KvdnSession(Vertx vertx, stype = SESSIONTYPE.NATIVE_SESSION) {

        sessionid = UUID.randomUUID().toString()
        String loggerName = "${cname} :  ${sessionid.toString()}"
        logger = new LoggerFactory().getLogger(loggerName)
        logger.info("creating KvdnSession")

        //@fixme hack?
        // in memory shared data

        /*@todo we need a new way to check to make sure we are a singleton session per jvm
        if this is required by the cluster manager
        Maybe the data implementation itself should enforce the singleton pattern
        */
//        vertx.sharedData().getLocalMap("sessions", { AsyncResult<AsyncMap> ar ->
//            AsyncMap m = ar.result()
//            m.keys(new Handler<AsyncResult<Set>>() {
//                @Override
//                void call(AsyncResult<Set> event) {
//                    assert event.succeeded()
//                    assert event.result().size() == 0
//
//
//                }
//            })
////
////
////            c.incrementAndGet({ AsyncResult<Long> igr ->
////                LoggerFactory.getLogger(this.class.getName()).error("SESSION NUMBER:" + igr.result())
////            })
//        })
//anyway should not have async code in a constructor

        this.vertx = vertx
        JsonObject Vconfig = this.vertx.getOrCreateContext().config()
        logger.trace("VERTX CONFIG: ${Vconfig.encodePrettily()}")
        config = Vconfig.getJsonObject('kvdn') ?: new JsonObject()

        logger.trace("KVDN CONFIG: ${config.encodePrettily()}")

        txflags = []
        outstandingTX = new HashSet()
        eb = this.vertx.eventBus()
        accessCache = this.vertx.sharedData().getLocalMap(ACCESS_CACHE_LOC)

        String configured_data = config.getString('data_implementation') ?:
                'net.iowntheinter.kvdn.storage.kv.data.DefaultDataImpl'
        try {
            this.D = DataImplLoader(configured_data) as KVData
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

        logger.debug("CONFIGURED PROVIDER: ${this.keyprov}")

        logger.trace("starting new kvdn session with clustered = ${this.vertx.isClustered()} keyprovider = ${this.keyprov}")

    }

    void init(Handler<AsyncResult> cb, Handler<Throwable> error_cb) {

        logger.debug("Calling session INIT")
        //this should verify everyting is setup (outstanding admin ops have finished, req cluster members have joined
        if (vertx.isClustered()) {
            SharedDataImpl sdlocal = new SharedDataImpl(vertx as VertxInternal, null)
            //@todo this is where we check if we are the only Session for this vertx instance

        }
        checkInternalState(vertx, new Handler<AsyncResult<KvdnSession>>() {
            @Override
            void handle(AsyncResult<KvdnSession> event) {

                System.err.println("success handler in zerostate")
                KvdnSession s = event.result()
                s.initalized = true
                cb.handle(Future.succeededFuture(s))


            }
        })
        logger.debug("post zerostate call")
    }
    /*
     * This is the entrypoint to using the kv api, take your transactions here.
     *
     * @param strAddr storage address
     * @return KvdnOperation your transaction
     */

    KvOp newKvOp(String strAddr, datatype = DATATYPE.KV, String mimeType = null, String transactionID = null) {
        return newOp(strAddr, datatype, mimeType, transactionID) as KvOp
    }

    KvdnOperation newOp(String strAddr, datatype = DATATYPE.KV, String mimeType = null, String transactionID = null) {
        if (mimeType != null && DATATYPE != DATATYPE.KV)
            throw new Exception("CANNOT DECLARE A MIMETYPE ON ${datatype.toString()}")

        if (datatype == DATATYPE.KV && mimeType == null)
            mimeType = "text/plain"

        if (!initalized) {
            throw new Exception("kvdn session needs to be init(cb,ecb) before you use it")
        } else {
            UUID txid = UUID.randomUUID()
            outstandingTX.add(txid)
            switch (datatype) {
                case DATATYPE.KV:
                    return (new KvOp(strAddr, mimeType, txid, this, vertx))
                    break
                case DATATYPE.CTR:
                    return (new CtrOp(strAddr, txid, this, vertx))
                    break
                case DATATYPE.QUE:
                    return (new QueOp(strAddr, txid, this, vertx))
                    break
                case DATATYPE.LCK:
                    return null //(new LOperation(strAddr, txid, this, vertx))
                    break
                default:
                    throw new Exception("transaction data type ${datatype.toString()} not available")
            }
        }
    }
/*
 * you must call this as the last action
 */

    void finishOp(KvdnOperation op, Handler cb) {
        outstandingTX.remove(op.txid)
        opCompletionHandler(op, new Handler<AsyncResult>() {
            @Override
            void handle(AsyncResult event) {
                if (event.succeeded()) {
                    if (tracking)
                        accessCache.put(op.strAddr, op.txid)
                    else
                        accessCache.putIfAbsent(op.strAddr, true)
                    cb.handle(Future.succeededFuture())
                } else {
                    logger.error(event.cause())
                    cb.handle(event)
                }
            }

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

    QueryProvider getQueryEngine(String id) {
        if (!queryEngines.containsKey(id))
            throw new Exception("Requested query engine ${id} but engine is not registered")
    }

    private void checkInternalState(Vertx vertx, Handler<AsyncResult<KvdnSession>> cb) {
        cb.handle(Future.succeededFuture(this))
        //@todo unimplemented
    }
}