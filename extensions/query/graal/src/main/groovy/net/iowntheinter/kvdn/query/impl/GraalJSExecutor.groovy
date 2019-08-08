package net.iowntheinter.kvdn.query.impl

import net.iowntheinter.kvdn.service.KvdnService
import net.iowntheinter.kvdn.storage.KvdnSession
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.proxy.*;

class GraalJSExecutor {
    final KvdnService svc

    GraalJSExecutor(KvdnService svc) {
        this.svc = svc

    }

    void eval(String script) {

        try (Context context = Context.create()) {
            context.getBindings('js').putMember("kvdn", svc)
            context.eval("js", script)
        }

    }


}
