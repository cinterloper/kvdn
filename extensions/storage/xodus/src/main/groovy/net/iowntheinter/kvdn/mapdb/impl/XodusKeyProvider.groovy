package net.iowntheinter.kvdn.mapdb.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import jetbrains.exodus.env.Cursor
import jetbrains.exodus.env.Environment
import jetbrains.exodus.env.Store
import jetbrains.exodus.env.Transaction
import jetbrains.exodus.env.TransactionalExecutable
import net.iowntheinter.kvdn.mapdb.XodusExtension
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.kv.key.KeyProvider
import org.jetbrains.annotations.NotNull

@TypeChecked
@CompileStatic
class XodusKeyProvider extends XodusExtension implements KeyProvider {

    private final Vertx vertx
    private final Store store
    private final Environment env
    def DataImpl

    XodusKeyProvider() {
        throw new Exception("YOU MUST PASS THE DATA IMPL TO ${this.class.name} constructor")
    }

    XodusKeyProvider(Vertx vertx, KVData DataImpl) {

        this.store = DataImpl.getdb() as Store
        this.vertx = vertx
        this.DataImpl = DataImpl
    }

    @Override
    void getKeys(String s, Handler cb) {
        Set keys = []
        env.executeInTransaction(new TransactionalExecutable() {
            @Override
            void execute(@NotNull Transaction txn) {
                try (Cursor cursor = store.openCursor(txn)) {
                    while (cursor.getNext()) {
                        keys.add(cursor.getKey().toString())   // current key
                    }
                }
                cb.handle(Future.succeededFuture(keys))
            }
        })
    }
//this should be handled by the underling map
    @Override
    void deleteKey(String mapName, String key, Handler cb) {
        cb.handle(Future.succeededFuture())
    }
//this should be handled by the underling map

    @Override
    void setKey(String mapName, String s1, Handler cb) {
        cb.handle(Future.succeededFuture())
    }
}