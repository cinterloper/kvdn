package net.iowntheinter.kvdn.storage

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
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
    def sessionid, keyprov, config
    public D

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

    void _hookCaller(kvdnTX tx, ArrayList<txnHook> hooks, int ptr, cb) {
        logger.trace("inside hook caller ptr: $ptr hooks: $hooks")
        if (ptr != hooks.size()) {

            def nxt = hooks[ptr] as txnHook
            logger.trace("calling hook")

            ptr++
            nxt.call(tx, this, {
                _hookCaller(tx, hooks, ptr, cb)
            })
        } else
            cb()
    }

    kvdnSession(Vertx vx, stype = sessionType.NATIVE_SESSION) {
        vertx = vx
        txflags = []
        outstandingTX = new HashSet()
        config = vertx.getOrCreateContext().config().getJsonObject('kvdn') ?: new JsonObject()
        sessionid = UUID.randomUUID().toString()
        logger = new LoggerFactory().getLogger("Kvdnsession:${sessionid.toString()}")
        eb = vertx.eventBus()
        accessCache = vertx.sharedData().getLocalMap(ACCESS_CACHE_LOC)

        String configured_data = config.getString('data_implementation') ?: 'net.iowntheinter.kvdn.storage.kv.data.defaultDataImpl'
        try {
            this.D = this.class.classLoader.loadClass(configured_data)?.newInstance(vertx as Vertx) as kvdata
            this.preHooks.addAll((D as kvdata).getPreHooks())
            this.postHooks.addAll((D as kvdata).getPostHooks())
        } catch (e) {
            e.printStackTrace()
            logger.fatal("could not load data impl $configured_data: ${e.getMessage()}")
            throw e
        }

        try {
            String configured_provider = config.getString('key_provider') ?: null
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

        logger.trace("starting new kvdn session with clustered = ${vertx.isClustered()} keyprovider = ${this.keyprov}")

    }

    void init(cb, error_cb) {
        zeroState(vertx, { kvdnSession s ->
            s.initalized = true
            cb()
        }, error_cb)
    }

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


    MessageConsumer onWrite(String strAddr, String key = null, Closure cb) {
        return eb.consumer("_KVDN_+${strAddr}", { message -> //listen for updates on this key
            if ((key == null) || (message.body() == key))
                cb(message.body())
        })
    }


    MessageConsumer onDelete(String strAddr, String key = null, Closure cb) {
        return eb.consumer("_KVDN_-${strAddr}", { message -> //listen for deletes on this key
            if ((key == null) || (message.body() == key))
                cb(message.body())
        })
    }

    void adminCommandListener() {
        eb.consumer("_KVDN_ADMIN_COMMANDS", { event ->
            JsonObject cmd = event.body() as JsonObject
            switch (cmd.getString("CMD")) {

            }
        })
    }

    private void zeroState(Vertx v, cb, error_cb) {
        boolean goodstate = false
        assert (!txflags.contains(txFlags.READ_ONLY) && !roMode && !transition)
        //when initializing a session, there should be no outstanding admin operations
        v.sharedData().getCounter("_KVDN_ADMIN_OPERATIONS", { ar ->
            try {
                assert ar.succeeded()
            } catch (gce) {
                error_cb(gce)
            }
            Counter l = ar.result()
            l.get({ AsyncResult r ->
                try {
                    assert r.succeeded()
                } catch (gre) {
                    error_cb(gre)
                }
                try {
                    assert r.result() == 0
                    goodstate = true
                } catch (ase) {
                    error_cb(ase)
                }
                if (goodstate)
                    cb(this)
            })
        })
    }
}
