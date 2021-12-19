package com.github.ompc.jpromisor;

@FunctionalInterface
public interface Then<V, T> {

    T then(V v) throws Exception;

}
