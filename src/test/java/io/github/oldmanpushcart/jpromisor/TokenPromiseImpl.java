package io.github.oldmanpushcart.jpromisor;

import io.github.oldmanpushcart.jpromisor.impl.NotifiableFuture;

public class TokenPromiseImpl<V> extends NotifiableFuture<V> implements TokenPromise<V> {

    private final String token;

    public TokenPromiseImpl(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }

}
