package net.iowntheinter.kvdn.query.impl

import net.iowntheinter.kvdn.service.KvdnService
import org.graalvm.polyglot.Context

class GraalPYExecutor {
    final KvdnService svc

    GraalPYExecutor(KvdnService svc) {
        this.svc = svc

    }

    void eval(String script, Map<String,Object> ctx) {

        try (Context context = Context.create()) {
            context.getBindings('py').putMember("kvdn", svc)
            context.eval("py", script)
        }

    }


}
