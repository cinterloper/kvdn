package net.iowntheinter.kvdn.storage.kv


import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.KvdnOperation.VALUETYPE
import net.iowntheinter.kvdn.query.DBObjectMgr
import net.iowntheinter.kvdn.query.DBSchemaMgr
import net.iowntheinter.kvdn.storage.KvdnSession

@TypeChecked
@CompileStatic
abstract class KVDNRefrence {


    /*
      a KVDNRefrence represents an indirection
      it is implemented as a metadata map
      the data map value that refers to it should be its straddr



      this is NOT the same thing as a direct refrence

      a direct reference could be a psudeo url that points to data on an external system
      in this case there should be a lookup function that yields a KVDNRefrence


      a key can be a ref type, and holds the straddr of the ref data
      derefrenceing the refrence can yeld any of @reftype
      additionally a kvdnref can be a member of an architype and contain
      its own aribtrary key/value pairs
     */

    enum reftype {
        MAP,
        OBJECT,
        VALUE,
        REF,    //refrences can point to other refrences
        EXT
    }
    final String straddr
    final KvdnSession session
    final Vertx vertx
    final Logger logger

    Map<String, String> Architype =
            [
                    REFRENCE: VALUETYPE.STRING.toString(),
                    TYPE:  VALUETYPE.STRING.toString()
            ]

    KVDNRefrence(KvdnSession session, String straddr) {
        this.straddr = straddr
        this.session = session
        this.vertx = session.vertx
        this.logger = LoggerFactory.getLogger(this.class.name)
    }


    String PhyStrLoc  // on this node?
    abstract KvdnOperation.DATATYPE getDatatype()

    abstract DBSchemaMgr getSchema()

    abstract String getStraddr()

    abstract derefrence(Handler<AsyncResult<DBObjectMgr>> handler)

    abstract void toJson(Handler<AsyncResult<JsonObject>> handler)

    String toString() {
        return getStraddr()
    }


    void resolveExternalRef(String ext_ref, Handler<AsyncResult> cb) {
        def extsys = ext_ref.tokenize(':')[0]
        switch (extsys) {
        /*    case 'kvdn': // $@kvdn://this/that?whatever
                def s = new kvdnSession(vertx)
                s.init({
                    def tokens = ext_ref.minus("kvdn://").tokenize('?')[0].tokenize('/')
                    def key =  ext_ref.minus("kvdn://").tokenize('?')[1]
                    KvTx tx = s.newTx("${tokens[0]}:${tokens[1]}") as KvTx
                    tx.get(key,{ res ->
                        if(res.result)
                            configs[path]=res.result
                        cb()
                    })
                },{ error -> logger.error(error)})
                break*/
            case 'http' || 'https':
                HttpClient client = vertx.createHttpClient()
                client.getNow(ext_ref, { resp ->
                    resp.bodyHandler({ body ->
//                        configs[path] = body.toString()
           //             cb()
                    })
                })
                break
            case 'file':
                vertx.fileSystem().readFile(ext_ref.minus("file://"), { asyncResult ->
                    if (asyncResult.succeeded()) {
//                        configs[path] = asyncResult.result()
//                        cb()
                    } else {
                        logger.error(asyncResult.cause())
                        asyncResult.cause().printStackTrace()
                    }
                })
                break
        //example: '$@vault://secret/this/that?akey'
        /*case 'vault':
            def vcl = new vaultConfigLoader(vertx)
            vcl.loadConfig(ext_ref.minus("vault://"),{ vault_result ->
                if(!vault_result.error){
                    configs[path] = vault_result.result
                    cb()
                }else{
                    logger.error(vault_result.error)
                }
            })
            break*/
            default:
                throw new Exception(extsys + " configuration Unimplemented ")
        //comploader(extsys,ext_ref,cb)
        }
    }
}
