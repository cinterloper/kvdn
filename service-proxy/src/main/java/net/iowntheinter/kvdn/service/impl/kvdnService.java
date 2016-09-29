package net.iowntheinter.kvdn.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.iowntheinter.kvdn.service.kvsvc;
import net.iowntheinter.kvdn.storage.kv.impl.KvTx;
import net.iowntheinter.kvdn.storage.kvdnSession;

import java.util.Map;
import java.util.concurrent.Callable;

public class kvdnService implements kvsvc {
    private Vertx vertx;
    private kvdnSession session;
    private static final Logger logger = LoggerFactory.getLogger(kvdnService.class);

    public kvdnService(Vertx vertx) {
        this.vertx = vertx;
        logger.info("started kvdnservice");
        this.setup();
    }

    public void setup() {
        try {
            this.session = new kvdnSession(vertx);

        } catch (Exception e) {
            System.out.println("problem getting cassandra session: ");
            e.printStackTrace();
        }
    }

    @Override
    public void set(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
        mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx)this.session.newTx(document.getString("straddr"));
        tx.set(document.getString("key"),document.getString("value"),mji);
    }

    @Override
    public void submit(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
        mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx)this.session.newTx(document.getString("straddr"));
        tx.submit(document.getString("value"),mji);
    }

    @Override
    public void get(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
        mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx)this.session.newTx(document.getString("straddr"));
        tx.get(document.getString("key"),mji);
    }

    @Override
    public void getKeys(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
        logger.trace("getting keys request " + document.toString());
        mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx)this.session.newTx(document.getString("straddr"));
        tx.getKeys(mji);
    }
    @Override
    public void getSize(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
        logger.trace("getting keys request " + document.toString());
        mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx)this.session.newTx(document.getString("straddr"));
        tx.size(mji);
    }
    @Override
    public void delete(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
        mapjsonintercepter mji = new mapjsonintercepter().setCb(resultHandler);
        KvTx tx = (KvTx)this.session.newTx(document.getString("straddr"));
        tx.del(document.getString("key"),mji);
    }


    private class mapjsonintercepter implements Handler {
        Handler cb ;
        public mapjsonintercepter setCb(Handler<AsyncResult<JsonObject>> nxt){
            cb = nxt;
            return this;
        }
        @Override
        public void handle(Object event) {
            Map m = (Map)event;
            JsonObject j = new JsonObject(m);
            logger.trace("SP json result " + j.toString());
            cb.handle(Future.succeededFuture( j));
        }
        public void call(Map m) throws Exception {
            logger.trace("called "+m);
            this.handle(m);
        }
    }
}