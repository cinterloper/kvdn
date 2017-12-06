package net.iowntheinter.kvdn.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Created by grant on 4/11/16.
 */
@CompileStatic
@TypeChecked
class resourceLoader {
    static String getResourceAsString(String name) {
        return getResource(name)
    }

    static String getResource(String name) throws Exception {
        def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
        def cpth = classloader.findResource(name)
        if (cpth) {
            return (classloader.getResourceAsStream(name).getText())
        } else
            throw new Exception("Resource ${name} not found")
    }

    static byte[] getResourceBytes(String name) throws Exception {
        def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
        def cpth = classloader.findResource(name)
        if (cpth) {
            return (classloader.getResourceAsStream(name).getBytes())
        } else
            throw new Exception("Resource ${name} not found")
    }
}
