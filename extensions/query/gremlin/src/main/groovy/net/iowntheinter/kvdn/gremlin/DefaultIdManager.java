package net.iowntheinter.kvdn.gremlin;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.iowntheinter.kvdn.gremlin.KVDNGraph;

import java.util.UUID;
import java.util.stream.Stream;


/**
 * A default set of {@link IdManager} implementations for common identifier types.
 */
public enum DefaultIdManager implements IdManager {

    /**
     * Manages identifiers of type {@code Long}. Will convert any class that extends from {@link Number} to a
     * {@link Long} and will also attempt to convert {@code String} values
     */
    LONG {
        private final Logger logger = LoggerFactory.getLogger(KVDNHelper.class.getName());

        @Override
        public Long getNextId(final KVDNGraph graph) {
            System.out.println("LOG_IDMG:getNextID on LONG");
            return Stream.generate(() -> (graph.currentId.incrementAndGet())).filter(id -> !graph.getvstore().containsKey(id) && !graph.getestore().containsKey(id)).findAny().get();
        }

        @Override
        public Object convert(final Object id) {
            System.out.println("LOG_IDMG:convert on LONG");
            if (null == id)
                return null;
            else if (id instanceof Long)
                return id;
            else if (id instanceof Number)
                return ((Number) id).longValue();
            else if (id instanceof String)
                return Long.parseLong((String) id);
            else
                throw new IllegalArgumentException(String.format("Expected an id that is convertible to Long but received %s", id.getClass()));
        }

        @Override
        public boolean allow(final Object id) {
            System.out.println("LOG_IDMG:allow on LONG");
            return id instanceof Number || id instanceof String;
        }
    },

    /**
     * Manages identifiers of type {@code Integer}. Will convert any class that extends from {@link Number} to a
     * {@link Integer} and will also attempt to convert {@code String} values
     */
    INTEGER {
        private final Logger logger = LoggerFactory.getLogger(KVDNHelper.class.getName());

        @Override
        public Integer getNextId(final KVDNGraph graph) {
            System.out.println("LOG_IDMG:genNextID on INTEGER");
            return Stream.generate(() -> (graph.currentId.incrementAndGet())).map(Long::intValue).filter(id -> !graph.getvstore().containsKey(id) && !graph.getestore().containsKey(id)).findAny().get();
        }

        @Override
        public Object convert(final Object id) {
            System.out.println("LOG_IDMG:convert on INTEGER");
            if (null == id)
                return null;
            else if (id instanceof Integer)
                return id;
            else if (id instanceof Number)
                return ((Number) id).intValue();
            else if (id instanceof String)
                return Integer.parseInt((String) id);
            else
                throw new IllegalArgumentException(String.format("Expected an id that is convertible to Integer but received %s", id.getClass()));
        }

        @Override
        public boolean allow(final Object id) {
            System.out.println("LOG_IDMG:allow on INTEGER");
            return id instanceof Number || id instanceof String;
        }
    },

    /**
     * Manages identifiers of type {@link java.util.UUID}. Will convert {@code String} values to
     * {@link java.util.UUID}.
     */
    UUID {
        private final Logger logger = LoggerFactory.getLogger(KVDNHelper.class.getName());

        @Override
        public java.util.UUID getNextId(final KVDNGraph graph) {
            System.out.println("LOG_IDMG:getNextId on UUID");
            return java.util.UUID.randomUUID();
        }

        @Override
        public Object convert(final Object id) {
            System.out.println("LOG_IDMG:convert on UUID");
            if (null == id)
                return null;
            else if (id instanceof java.util.UUID)
                return id;
            else if (id instanceof String)
                return java.util.UUID.fromString((String) id);
            else
                throw new IllegalArgumentException(String.format("Expected an id that is convertible to UUID but received %s", id.getClass()));
        }

        @Override
        public boolean allow(final Object id) {
            logger.info("allow on UUID");
            return id instanceof UUID || id instanceof String;
        }
    },

    /**
     * Manages identifiers of any type.  This represents the default way {@link KVDNGraph} has always worked.
     * In other words, there is no identifier conversion so if the identifier of a vertxio is a {@code Long}, then
     * trying to request it with an {@code Integer} will have no effect. Also, like the original
     * {@link KVDNGraph}, it will generate {@link Long} values for identifiers.
     */
    ANY {
        private final Logger logger = LoggerFactory.getLogger(KVDNHelper.class.getName());

        @Override
        public Long getNextId(final KVDNGraph graph) {
            System.out.println("LOG_IDMG:getNextId on ANY");
            return Stream.generate(() -> (graph.currentId.incrementAndGet())).filter(id -> !graph.getvstore().containsKey(id) && !graph.getestore().containsKey(id)).findAny().get();
        }

        @Override
        public Object convert(final Object id) {
            String msg = "null";
            if (id != null)
                msg = id.toString();

            System.out.println("LOG_IDMG:convert on ANY: " + msg);
            return id;
        }

        @Override
        public boolean allow(final Object id) {
            System.out.println("LOG_IDMG:allow on ANY");
            return true;
        }
    }
}