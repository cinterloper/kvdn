package net.iowntheinter.kvdn.util

/**
 * Created by grant on 4/11/16.
 */
class resourceLoader {
    static String getResourceAsString(String name) {
        return getResource(name)
    }

    static String getResource(String name) {
        def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
        def cpth = classloader.findResource(name)
        if (cpth) {
            return (classloader.getResourceAsStream(name).getText())
        } else
            return (-1)
    }

    static byte[] getResourceBytes(String name) {
        def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
        def cpth = classloader.findResource(name)
        if (cpth) {
            return (classloader.getResourceAsStream(name).getBytes())
        } else
            return (-1)
    }
}
