package net.iowntheinter.kvdn.query

import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.kv.KVDNRefrence
import net.iowntheinter.kvdn.storage.kv.impl.KvOp
import net.iowntheinter.kvdn.util.DistributedWaitGroup

/*
The union of the metadata + the data is the object
U {
    [ key1 meta, key2 meta ..]
    {
      k1:d1,
      k2,d2
      ...
      }
    straddr
    mapmeta {
    m1:md1
    m2:md2
    m3:md3
    ...
    },
    schema
  }

  as a storage totality
  that information is materalized in an object instance

  object instances implement and potentially extend a schema
 */

class DBObjectMgr {
    final KvdnSession session
    final schemabase = "__SCHEMA_"

    DBObjectMgr(KvdnSession session) {
        this.session = session
    }


    void create(String architype, String straddr, Handler<AsyncResult<KVDNRefrence>> handler) {

        session.newKvOp(architype).getKeys({ AsyncResult<Set<String>> archatypekeys ->
            ArrayList akeys = new ArrayList(archatypekeys.result())
            session.newIterator(straddr, { AsyncResult ->
                handler.handle(Future.succeededFuture(null))

            }).iterate({ MapEntry data, Handler<AsyncResult> cb ->
                try {
                    assert akeys.contains(data.key)
                }catch(Exception e){
                    cb.handle(Future.failedFuture(e))
                    return
                }
                session
                cb.handle(Future.succeededFuture())
            })


        })


        ArrayList<Future> futures = new ArrayList<>()
        Future schema_keys_fut = Future.future()
        futures.push(schema_keys_fut)
        Future obj_keys_fut = Future.future()
        futures.push(obj_keys_fut)

        JsonArray architype_keys
        JsonArray object_keys

        obj_keys_fut.setHandler({ AsyncResult<JsonArray> ar ->
            assert ar.succeeded()
            object_keys = ar.result()
        })
        schema_keys_fut.setHandler({ AsyncResult<JsonArray> ar ->
            assert ar.succeeded()
            architype_keys = ar.result()
        })
        CompositeFuture compositeOp = CompositeFuture.all(futures)
        compositeOp.setHandler({ AsyncResult<CompositeFuture> ar ->
            if (!ar.succeeded()) {
                handler.handle(Future.failedFuture(ar.cause()))
                return
            } else {
                try {
                    architype_keys.each { akey ->
                        assert object_keys.contains(akey)
                    }
                } catch (Exception e) {
                    handler.handle(Future.failedFuture(e))
                    return
                }

            }

            Future metamap = Future.future()


        })


        KvOp schema_keys_op = session.newOp(schemabase + architype, KvdnOperation.DATATYPE.SCHEMA) as KvOp
        schema_keys_op.getKeys(schema_keys_fut)
        KvOp map_keys_op = session.newOp(straddr) as KvOp
        map_keys_op.getKeys(obj_keys_fut)


        //assert the necessary keys are present
        // schema.keys() == KvdnObject(straddr).keys()
        // schema.types() == KvdnObject(straddr).types()

        //assert the typeMap is the same

    }

    void create(String schema, String straddr, JsonObject data, Handler<AsyncResult<KVDNRefrence>> handler) {
        //assert straddr is empty
        //assert data has keys and types == schema


    }

//import refrence system from cornerstone
    //should have pluggable remote derefrenceing
    //derefrence a refrence to a refrence
    //template refrences
    //   kvdn_this.aref = "sometable/${kvdn_this.other_key}?targetkey"
    //you can deref a value or an object
}
