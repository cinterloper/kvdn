package net.iowntheinter.kvdn.storage.kv.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.kv.AsyncIterator

@TypeChecked
@CompileStatic
class KVIterator  {
    final Handler<AsyncResult> fin
    final String strAddr
    final KvdnSession session
    final Logger logger

    KVIterator(String straddr, KvdnSession session, Handler<AsyncResult> cb) {
        this.fin = cb
        this.strAddr = straddr
        this.session = session
        this.logger = LoggerFactory.getLogger("${this.class.name}:${straddr}" )
    }

    void iterate(Map state = null, KvdnSession.AsyncIterOp iterop) {
        if (state == null) {
            state = [:]
            session.newKvOp(strAddr).getKeys({ AsyncResult<Set<String>> ar ->
                if (ar.failed()) {
                    fin.handle(ar)
                } else {
                    ArrayList<String> res = new ArrayList(ar.result())
                    logger.info("started first of ${res.size()} iterations over ${strAddr}")
                    state.put("keys", res)
                    state.put("index", 0)
                    iterate(state, iterop)
                }
            })
        } else if ((state.index as Integer) >= ((state.keys as ArrayList).size())) {
            fin.handle(Future.succeededFuture())
        } else {
            logger.info("iteration ${state.index}")
            String key = (state.keys as ArrayList<String>)[state.index as Integer]
            session.newKvOp(strAddr).get(key, { AsyncResult<String> res ->
                if (res.failed()) {
                    fin.handle(res)
                } else {
                    MapEntry data = new MapEntry(key, res.result())
                    iterop.call(data, { AsyncResult ar ->
                        if (ar.succeeded()) {
                            state.index = ((state.index as Integer) + 1)
                            iterate(state, iterop)
                        } else {
                            fin.handle(ar)
                        }
                    })
                }
            })

        }
    }
}
