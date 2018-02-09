package net.iowntheinter.kvdn.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.iowntheinter.kvdn.service.kvsvc;
import net.iowntheinter.kvdn.storage.KvdnSession;
import net.iowntheinter.kvdn.storage.kv.impl.KvTx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KvdnService implements kvsvc {
    private Vertx vertx;
    private KvdnSession session;
    private static final Logger logger = LoggerFactory.getLogger(KvdnService.class);

    public KvdnService(Vertx vertx) {
        this.vertx = vertx;
        logger.info("started kvdnservice");
    }

    public void setup(Handler cb) {
        logger.info("service setup called");

        try {
            this.session = new KvdnSession(vertx);
            session.init(cb, (Handler<Throwable>) logger::error);

        } catch (Exception e) {
            System.out.println("problem getting cassandra session: ");
            e.printStackTrace();
        }

    }

    @Override
    public void set(String straddr, String key, String value, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler) {
        //  mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx) this.session.newTx(straddr, KvdnSession.DATATYPE.KV);
        tx.set(key, value, resultHandler);
    }

    @Override
    public void submit(String straddr, String value, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler) {
        //   mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx) this.session.newTx(straddr, KvdnSession.DATATYPE.KV);


        tx.submit(value, resultHandler);
    }

    @Override
    public void get(String straddr, String key, JsonObject options, Handler<AsyncResult<String>> resultHandler) {
        //     mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx) this.session.newTx(straddr, KvdnSession.DATATYPE.KV);
        tx.get(key, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> event) {
                logger.trace("inside service get with result:  " + event.result());
                resultHandler.handle(event);

            }

        });
    }

    @Override
    public void getKeys(String straddr, JsonObject options, Handler<AsyncResult<JsonArray>> resultHandler) {
        logger.trace("getting keys request " + straddr);
        setjsoninterceptor sji = new setjsoninterceptor().setCb(resultHandler);
        KvTx tx = (KvTx) this.session.newTx(straddr, KvdnSession.DATATYPE.KV);
        tx.getKeys(sji);

    }

    @Override
    public void size(String straddr, JsonObject options, Handler<AsyncResult<Integer>> resultHandler) {
        logger.trace("getting keys request " + straddr);
        //mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx) this.session.newTx(straddr, KvdnSession.DATATYPE.KV);
        tx.size(resultHandler);
    }

    @Override
    public void del(String straddr, String key, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler) {
        // mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx) this.session.newTx(straddr, KvdnSession.DATATYPE.KV);
        tx.del(key, resultHandler);
    }

    @Override
    public void query(String stradd, JsonObject query, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler) {
        logger.fatal("no query provider loaded");
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
}