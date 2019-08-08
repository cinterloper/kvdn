package net.iowntheinter.kvdn.gremlin;

import net.iowntheinter.kvdn.gremlin.KVDNGraph;

public interface IdManager<T> {
    /**
     * Generate an identifier which should be unique to the {@link KVDNGraph} instance.
     */
    T getNextId(final KVDNGraph graph);

    /**
     * Convert an identifier to the type required by the manager.
     */
    T convert(final Object id);

    /**
     * Determine if an identifier is allowed by this manager given its type.
     */
    boolean allow(final Object id);
}
