package net.iowntheinter.kvdn.gremlin.adapters

import org.apache.tinkerpop.gremlin.structure.VertexProperty
import org.jetbrains.annotations.NotNull
/*
This refrences a KVDNMap of VP addresses

 */
class KVDNVPList implements List<VertexProperty> {
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
    Iterator<VertexProperty> iterator() {
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
    boolean add(VertexProperty vertexProperty) {
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
    boolean addAll(@NotNull Collection<? extends VertexProperty> c) {
        return false
    }

    @Override
    boolean addAll(int index, @NotNull Collection<? extends VertexProperty> c) {
        return false
    }

    @Override
    boolean removeAll(@NotNull Collection<?> c) {
        return false
    }

    @Override
    boolean retainAll(@NotNull Collection<?> c) {
        return false
    }

    @Override
    void clear() {

    }

    @Override
    VertexProperty get(int index) {
        return null
    }

    @Override
    VertexProperty set(int index, VertexProperty element) {
        return null
    }

    @Override
    void add(int index, VertexProperty element) {

    }

    @Override
    VertexProperty remove(int index) {
        return null
    }

    @Override
    int indexOf(Object o) {
        return 0
    }

    @Override
    int lastIndexOf(Object o) {
        return 0
    }

    @Override
    ListIterator<VertexProperty> listIterator() {
        return null
    }

    @Override
    ListIterator<VertexProperty> listIterator(int index) {
        return null
    }

    @Override
    List<VertexProperty> subList(int fromIndex, int toIndex) {
        return null
    }
}
