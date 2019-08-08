package net.iowntheinter.kvdn.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.iowntheinter.kvdn.service.KvdnService;
import net.iowntheinter.kvdn.storage.KvdnSession;
import net.iowntheinter.kvdn.storage.kv.AsyncIterator;
import net.iowntheinter.kvdn.storage.kv.impl.KvOp;
import net.iowntheinter.kvdn.storage.counter.impl.CtrOp;
import net.iowntheinter.kvdn.storage.queue.impl.QueOp;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/*
    serverside interface
    (client) < - eventbus - > (KvdnServiceImpl -> ( KvOp ( under the hood DB ) )

   everything get stored as buffers by default
   you can tell get() you are retrieving a string or JsonObject ....


 */
public class KvdnServiceImpl implements KvdnService {
    private Vertx vertx;
    private KvdnSession session;
    private static final Logger logger = LoggerFactory.getLogger(KvdnServiceImpl.class);

    public KvdnServiceImpl(Vertx vertx) {
        this.vertx = vertx;
        logger.info("started kvdnservice");
    }

    public void setup(Handler<AsyncResult> cb) {
        logger.info("service setup called");

        try {
            this.session = new KvdnSession(vertx, KvdnSession.SESSIONTYPE.PROXY_SERVER);
            session.init(cb, new Handler<Throwable>() {
                @Override
                public void handle(Throwable event) {
                    logger.fatal(event);
                    System.err.println(event.toString());
                }
            });

        } catch (Exception e) {
            System.out.println("problem getting cassandra session: ");
            e.printStackTrace();
        }

    }
    //@todo implement transaction control (rate limit, etc)


    @Override
    public void set(String straddr, String key, String value, JsonObject options, Handler<AsyncResult<String>> resultHandler) {
        //  mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvOp op = (KvOp) this.session.newOp(straddr, KvdnSession.DATATYPE.KV);
        op.set(key, value, options.getMap(), resultHandler);
    }

    @Override
    public void submit(String straddr, String value, JsonObject options, Handler<AsyncResult<String>> resultHandler) {
        //   mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvOp op = (KvOp) this.session.newOp(straddr, KvdnSession.DATATYPE.KV);
        op.submit(value, options.getMap(), resultHandler);
    }

    @Override
    public void get(String straddr, String key, JsonObject options, Handler<AsyncResult<String>> resultHandler) {
        //     mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvOp op = (KvOp) this.session.newOp(straddr, KvdnSession.DATATYPE.KV);

        //fuck this mess
        op.get(key, options.getMap(), resultHandler);
    }

    @Override
    public void getKeys(String straddr, JsonObject options, Handler<AsyncResult<JsonArray>> resultHandler) {
        logger.trace("getting keys request " + straddr);
        setjsoninterceptor sji = new setjsoninterceptor().setCb(resultHandler);
        KvOp op = (KvOp) this.session.newOp(straddr, KvdnSession.DATATYPE.KV);
        op.getKeys(options.getMap(), sji);

    }

    @Override
    public void size(String straddr, JsonObject options, Handler<AsyncResult<Integer>> resultHandler) {
        logger.trace("getting keys request " + straddr);
        //mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvOp op = (KvOp) this.session.newOp(straddr, KvdnSession.DATATYPE.KV);
        op.size(options.getMap(), resultHandler);
    }

    @Override
    public void del(String straddr, String key, JsonObject options, Handler<AsyncResult<String>> resultHandler) {
        // mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvOp op = (KvOp) this.session.newOp(straddr, KvdnSession.DATATYPE.KV);
        op.del(key, options.getMap(), resultHandler);
    }

    @Override
    public void query(String stradd, JsonObject query, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler) {
        logger.fatal("no query provider loaded");
    }

    @Override
    public void clear(String straddr, JsonObject options, Handler<AsyncResult<Boolean>> resultHandler) {
        KvOp op = (KvOp) this.session.newOp(straddr, KvdnSession.DATATYPE.KV);
        op.clear(options.getMap(), resultHandler);
    }

    @Override
    public void ctrGet(String straddr, JsonObject options, Handler<AsyncResult<Long>> cb) {
        CtrOp op = (CtrOp) this.session.newOp(straddr, KvdnSession.DATATYPE.CTR);
        op.get(cb);


    }

    @Override
    public void addAndGet(String straddr, long value, JsonObject options, Handler<AsyncResult<Long>> cb) {
        CtrOp op = (CtrOp) this.session.newOp(straddr, KvdnSession.DATATYPE.CTR);
        op.addAndGet(value, cb);
    }

    @Override
    public void getAndAdd(String straddr, long value, JsonObject options, Handler<AsyncResult<Long>> cb) {
        CtrOp op = (CtrOp) this.session.newOp(straddr, KvdnSession.DATATYPE.CTR);
        op.getAndAdd(value, cb);
    }

    @Override
    public void ctrCompareAndSet(String straddr, long oldv, long newv, JsonObject options, Handler<AsyncResult<Boolean>> cb) {
        CtrOp op = (CtrOp) this.session.newOp(straddr, KvdnSession.DATATYPE.CTR);
        op.compareAndSet(oldv, newv, cb);
    }

    @Override
    public void enqueue(String straddr, JsonObject options, String value, Handler<AsyncResult<Long>> cb) {
        QueOp op = (QueOp) this.session.newOp(straddr, KvdnSession.DATATYPE.QUE);
        op.enqueue(value, cb);
    }

    @Override
    public void dequeue(String straddr, JsonObject options, Handler<AsyncResult<String>> cb) {
        QueOp op = (QueOp) this.session.newOp(straddr, KvdnSession.DATATYPE.QUE);
        op.dequeue(cb);
    }

    @Override
    public void qPeek(String straddr, JsonObject options, Handler<AsyncResult<String>> cb) {
        QueOp op = (QueOp) this.session.newOp(straddr, KvdnSession.DATATYPE.QUE);
        op.peek(cb);
    }

    @Override
    public void qArrayView(String straddr, JsonObject options, Handler<AsyncResult<JsonObject>> cb) {
        QueOp op = (QueOp) this.session.newOp(straddr, KvdnSession.DATATYPE.QUE);
        op.arrayView(cb);
    }

    //for get keys the service should return the wire-serializeable representation, so a JsonArray instead of a set of keys
    //all other results should be seralized to a String
    private class setjsoninterceptor implements Handler<AsyncResult<Set<String>>> {
        Handler cb;

        setjsoninterceptor setCb(Handler<AsyncResult<JsonArray>> nxt) {
            cb = nxt;
            return this;
        }

        @Override
        public void handle(AsyncResult<Set<String>> event) {
            ArrayList s = new ArrayList<>(event.result());
            JsonArray j = new JsonArray(s);
            logger.trace("SP json result " + j.toString());
            cb.handle(Future.succeededFuture(j));
        }


    }

    private class mapjsonintercepter implements Handler<AsyncResult> {
        Handler cb;

        mapjsonintercepter setCb(Handler<AsyncResult<JsonObject>> nxt) {
            cb = nxt;
            return this;
        }

        @Override
        public void handle(AsyncResult event) {
            Map m = (Map) event;
            JsonObject j = new JsonObject(m);
            logger.trace("SP json result " + j.toString());
            cb.handle(Future.succeededFuture(j.toString()));
        }

    }

    Handler<AsyncResult<Object>> stringToObj(Handler<AsyncResult<String>> h) {
        return new Handler<AsyncResult<Object>>() {
            @Override
            public void handle(AsyncResult<Object> event) {
                if (event.succeeded())
                    h.handle(Future.succeededFuture((String) event.result()));
                else
                    h.handle(Future.failedFuture(event.cause()));
            }
        };
    }

}