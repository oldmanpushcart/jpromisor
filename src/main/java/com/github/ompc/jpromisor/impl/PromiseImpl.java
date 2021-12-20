package com.github.ompc.jpromisor.impl;

import com.github.ompc.jpromisor.Promise;

import java.util.concurrent.Callable;

public class PromiseImpl<V> extends ListenableFutureImpl<V> implements Promise<V> {

    @Override
    public Promise<V> self() {
        return this;
    }

    @Override
    public boolean tryCancel() {
        return super.tryCancel();
    }

    @Override
    public boolean tryException(Throwable cause) {
        return super.tryException(cause);
    }

    @Override
    public boolean trySuccess(V value) {
        return super.trySuccess(value);
    }

    @Override
    public boolean trySuccess() {
        return super.trySuccess(null);
    }

}
