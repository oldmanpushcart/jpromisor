package io.github.oldmanpushcart.jpromisor;

public interface TokenFuture<V> extends ListenableFuture<V> {

    String getToken();

}
