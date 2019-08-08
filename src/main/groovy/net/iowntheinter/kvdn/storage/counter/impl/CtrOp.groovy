package net.iowntheinter.kvdn.storage.counter.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.Counter
import io.vertx.core.shareddata.SharedData
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.storage.counter.CTROP
import net.iowntheinter.kvdn.storage.KvdnSession

/**
 * Created by grant on 11/19/15.
 */
@TypeChecked
@CompileStatic
class CtrOp extends KvdnOperation implements CTROP {

    _data_impl D = new _data_impl(vertx)

    private class _data_impl {
        SharedData sd
        Vertx vertx
        String name

        _data_impl(Vertx vertx) {

        }

        void getCtr(CtrOp txn, Handler<AsyncResult> cb) {
            vertx = txn.vertx
            name = txn.strAddr
            this.sd = vertx.sharedData()
            sd.getCounter("${name}", cb)
            logger.trace("starting kvdn counter operation ${name}, ${txid} with vertx.isClustered() == ${vertx.isClustered()}")
        }
    }

    CtrOp(String sa, UUID txid, KvdnSession session, Vertx vertx) {
        // keys = new ORSet()
        this.vertx = vertx
        this.session = session as KvdnSession
        this.txid = txid
        this.strAddr = sa
        this.logger = LoggerFactory.getLogger(this.class.name.toString().concat(strAddr.toString()))
        this.sd = vertx.sharedData() as SharedData
        this.eb = vertx.eventBus() as EventBus
    }


    @Override
    void snapshot(Handler<AsyncResult> cb) {}


    @Override
    void get(Handler<AsyncResult<Long>> cb) {
        startOperation(TXTYPE.CTR_GET, {


            D.getCtr(this, { AsyncResult res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_READ)) {
                    Counter ctr = res.result() as Counter
                    ctr.get({ resGet ->
                        if (resGet.succeeded()) {
                            logger.trace("take:${strAddr}")
                            (this.session as KvdnSession).finishOp(this, {
                                cb.handle(Future.succeededFuture(resGet.result()))
                            })
                        } else {
                            abortOperation(res, this, cb as Handler<AsyncResult>)
                        }
                    })
                } else {
                    abortOperation(res, this, cb as Handler<AsyncResult>)
                }
            })
        })
    }

    @Override
    void addAndGet(long value, Handler<AsyncResult<Long>> cb) {
        startOperation(TXTYPE.CTR_ADDNGET, {
            D.getCtr(this, { AsyncResult<Counter> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    Counter ctr = res.result()
                    ctr.addAndGet(value, { resGet ->
                        if (resGet.succeeded()) {
                            logger.trace("take:${strAddr}")
                            (this.session as KvdnSession).finishOp(this, {
                                if (value > 0)
                                    eb.publish("_KVDN_c+${strAddr}", new JsonObject().put('ctr', strAddr))
                                else
                                    eb.publish("_KVDN_c-${strAddr}", new JsonObject().put('ctr', strAddr))
                                cb.handle(Future.succeededFuture(resGet.result()))
                            })
                        } else {
                            abortOperation(res, this, cb as Handler<AsyncResult>)
                        }
                    })
                } else {
                    abortOperation(res, this, cb as Handler<AsyncResult>)
                }
            })
        })
    }

    @Override
    void getAndAdd(long value, Handler<AsyncResult<Long>> cb) {
        startOperation(TXTYPE.CTR_GETNADD, {
            D.getCtr(this, { AsyncResult<Counter> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    Counter ctr = res.result()
                    ctr.getAndAdd(value, { AsyncResult<Long> resGet ->
                        if (resGet.succeeded()) {
                            logger.trace("take:${strAddr}")
                            (this.session as KvdnSession).finishOp(this, {
                                if (value > 0)
                                    eb.publish("_KVDN_c+${strAddr}", new JsonObject().put('ctr', strAddr))
                                else
                                    eb.publish("_KVDN_c-${strAddr}", new JsonObject().put('ctr', strAddr))
                                cb.handle(Future.succeededFuture(resGet.result()))
                            })
                        } else {
                            abortOperation(res, this, cb as Handler<AsyncResult>)
                        }
                    })
                } else {
                    abortOperation(res, this, cb as Handler<AsyncResult>)
                }
            })
        })
    }

    @Override
    void compareAndSet(long oldv, long newv, Handler<AsyncResult<Boolean>> cb) {
        startOperation(TXTYPE.CTR_COMPSET, {

            D.getCtr(this, { AsyncResult<Counter> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    Counter ctr = res.result()
                    ctr.compareAndSet(oldv, newv, { resGet ->
                        if (resGet.succeeded()) {
                            logger.trace("ctr cas:${strAddr}")
                            (this.session as KvdnSession).finishOp(this, {
                                eb.publish("_KVDN_c=${strAddr}", new JsonObject().put('ctr', strAddr))
                                cb.handle(Future.succeededFuture(resGet.result()))
                            })
                        } else {
                            abortOperation(res, this, cb as Handler<AsyncResult>)
                        }
                    })
                } else {
                    abortOperation(res, this, cb as Handler<AsyncResult>)
                }
            })
        })
    }


}