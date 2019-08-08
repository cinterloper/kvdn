package net.iowntheinter.kvdn.admin.shell.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.shell.command.CommandBuilder
import io.vertx.ext.shell.ShellService
import io.vertx.ext.shell.command.CommandRegistry
import io.vertx.core.Vertx


class Shell {

    ShellService service

    Shell(Vertx vertx) {
        def helloWorld = CommandBuilder.command("hello-world").processHandler({ process ->
            process.write("hello world\n")
            process.end()
        }).build(vertx)

        service = ShellService.create(vertx, [
                telnetOptions: [
                        host: "localhost",
                        port: 3000
                ]
        ])
        CommandRegistry.getShared(vertx).registerCommand(helloWorld)

    }

    void start(Handler<AsyncResult> cb) {
        service.start(cb)
    }
}
