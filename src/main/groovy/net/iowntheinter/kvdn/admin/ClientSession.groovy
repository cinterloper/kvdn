package net.iowntheinter.kvdn.admin

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AbstractUser
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User

class ClientSession {
    static enum Privliges {
        RESTRICTED_QUERY,
        QUERY,
        DELETE,
        READ,
        WRITE,
        EXPORT,
        ADMIN
    }

    static enum Restrictions {
        RATE_LIMIT
    }

    final public boolean internal
    final public boolean anonymous
    final public UUID sessionId = UUID.randomUUID()
    final public User user

    private Privliges[] privileges
    private Restrictions[] restrictions
    protected ClientSession(){
        internal = true
        anonymous = false
        this.user = new AbstractUser() {
            @Override
            protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
                resultHandler.handle(Future.succeededFuture(true))

            }

            @Override
            JsonObject principal() {
                return null
            }

            @Override
            void setAuthProvider(AuthProvider authProvider) {

            }
        }
    }
    ClientSession(User user) {
        this.user = user
        anonymous = user == null

    }
    boolean hasPrivilege(Privliges p){
        return privileges.contains(p)
    }
    boolean hasRestriction(Restrictions r){
        return restrictions.contains(r)
    }


}
