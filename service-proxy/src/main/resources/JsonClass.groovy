import groovy.json.JsonOutput

import java.lang.reflect.Method
import java.lang.reflect.Parameter

mtbl = [:]

Class clazz = this.class.classLoader.loadClass(args[0])

clazz.getMethods().each { Method m ->
    Parameter[] parameters = m.getParameters()

    def ps = []
    int i = 0
    for (Parameter p : parameters) {
        if (p.name != "resultHandler")
            ps[i] = p.name
        i++
    }

    if (m.name != "create" && m.name != "createProxy")
        mtbl[m.getName()] = ps
}
println(JsonOutput.toJson(mtbl))

