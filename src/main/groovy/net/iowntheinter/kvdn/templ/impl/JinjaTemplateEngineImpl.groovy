
package net.iowntheinter.kvdn.templ.impl
import com.hubspot.jinjava.Jinjava
import com.hubspot.jinjava.JinjavaConfig
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.groovy.ext.web.templ.TemplateEngine
import io.vertx.core.Future

class JinjaTemplateEngineImpl implements JinjaTemplateEngine{
    Jinjava J
    JinjavaConfig c
    JinjaTemplateEngineImpl(){
        c = new JinjavaConfig()
        //c.setResourceLocator(new MyCustomResourceLocator());
        j = new Jinjava(c)
    }




    public render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
        handler.handle(Future.succeededFuture(Buffer.buffer(j.render(templateFileName,   context.get('templateMap') as Map))))
    }
}
