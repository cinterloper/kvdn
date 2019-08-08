package net.iowntheinter.kvdn.gremlin.adapters

import org.apache.tinkerpop.gremlin.structure.Edge
import org.jetbrains.annotations.NotNull

class KVDNEdgeSet implements Set<Edge>{

    @Override
    int size() {
        return 0
    }

    @Override
    boolean isEmpty() {
        return false
    }

    @Override
    boolean contains(Object o) {
        return false
    }

    @Override
    Iterator<Edge> iterator() {
        return null
    }

    @Override
    Object[] toArray() {
        return new Object[0]
    }

    @Override
    def <T> T[] toArray(@NotNull T[] a) {
        return null
    }

    @Override
    boolean add(Edge edge) {
        return false
    }

    @Override
    boolean remove(Object o) {
        return false
    }

    @Override
    boolean containsAll(@NotNull Collection<?> c) {
        return false
    }

    @Override
    boolean addAll(@NotNull Collection<? extends Edge> c) {
        return false
    }

    @Override
    boolean retainAll(@NotNull Collection<?> c) {
        return false
    }

    @Override
    boolean removeAll(@NotNull Collection<?> c) {
        return false
    }

    @Override
    void clear() {

    }
}

