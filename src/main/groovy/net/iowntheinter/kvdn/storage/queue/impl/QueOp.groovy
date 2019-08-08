package net.iowntheinter.kvdn.storage.queue.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.counter.impl.CtrOp
import net.iowntheinter.kvdn.storage.kv.AsyncIterator
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.kv.impl.KvOp
import net.iowntheinter.kvdn.storage.queue.QOP

/*
the key of every submission is the next counter value
there is a counter for the current head
the tail is always the most recent submission
 */

class QueOp extends KvdnOperation implements QOP {
    public KVData D
    public KVData M

    QueOp(String sa, String mimeType = "text/plain", UUID txid, KvdnSession session, Vertx vertx) {
        this.vertx = vertx
        this.keyprov = session.keyprov
        this.session = session as KvdnSession
        this.txid = txid
        this.strAddr = sa
        //doing it this way avoids a wierd compile static error
        // Type 'java/lang/Object' (current frame, stack[6]) is not assignable to 'java/lang/String'
        String classname = this.getClass().getName().toString()
        String ID = "$classname:${strAddr}" //
        this.logger = new LoggerFactory().getLogger(ID)
        //end wierd hack
        this.sd = vertx.sharedData() as SharedData
        this.eb = vertx.eventBus() as EventBus
        this.D = session.D as KVData
        this.M = session.D as KVData
    }

    @Override
    void enqueue(String value, Handler<AsyncResult<Long>> cb) {
        //increment map counter, set map
        //if q is uninitialized, head is 0
        CtrOp ctr = session.newOp('_QUEUE_T:' + strAddr, KvdnSession.DATATYPE.CTR) as CtrOp
        KvOp kv = session.newOp('_QUEUE:' + strAddr) as KvOp
        ctr.addAndGet(1, { AsyncResult<Long> r ->
            if (r.succeeded()) {
                kv.put(r.result().toString(), value, { AsyncResult<String> kr ->
                    if (kr.succeeded())
                        cb.handle(Future.succeededFuture(r.result()))
                    else
                        abortOperation(kr, this, cb)
                })
            } else {
                abortOperation(r, this, cb)
            }
        })

    }

    @Override
    void dequeue(Handler<AsyncResult<String>> cb) {
        CtrOp ctr = session.newOp('_QUEUE_H:' + strAddr, KvdnSession.DATATYPE.CTR) as CtrOp
        KvOp kv = session.newOp('_QUEUE:' + strAddr) as KvOp
        ctr.getAndAdd(1, { AsyncResult<Long> r ->
            if (r.succeeded()) {
                kv.get(r.result().toString(), { AsyncResult<String> kr ->
                    if (kr.succeeded())
                        cb.handle(Future.succeededFuture(kr.result()))
                    else
                        abortOperation(kr, this, cb)
                })
            } else {
                abortOperation(r, this, cb)
            }
        })


    }

    @Override
    void peek(Handler<AsyncResult<String>> cb) {
        CtrOp ctr = session.newOp('_QUEUE_H:' + strAddr, KvdnSession.DATATYPE.CTR) as CtrOp
        ctr.get({ AsyncResult<Long> r ->
            if (r.succeeded()) {
                KvOp kv = session.newOp('_QUEUE:' + strAddr) as KvOp
                kv.get(r.result().toString(), { AsyncResult<String> kr ->
                    cb.handle(kr)
                })
            } else {

                abortOperation(r, this, cb)
            }
        })
    }

    @Override
    void arrayView(Handler<AsyncResult<JsonObject>> cb) {
        cb.handle(Future.succeededFuture(new JsonObject().put("straddr", '_QUEUE:' + strAddr)))
    }

    @Override
    void stat(Handler<AsyncResult<JsonObject>> cb) {
        CtrOp ctr = session.newOp('_QUEUE_H:' + strAddr, KvdnSession.DATATYPE.CTR) as CtrOp
        ctr.get({ AsyncResult<Long> r ->
            if (r.succeeded()) {
                JsonObject resp = new JsonObject()
                resp.put("head", r.result())
                resp.put("straddr" + '_QUEUE:' + strAddr)
                resp.put("time", new Date().toInstant().toString())
            } else {
                abortOperation(r, this, cb)
            }

        })

    }
}
