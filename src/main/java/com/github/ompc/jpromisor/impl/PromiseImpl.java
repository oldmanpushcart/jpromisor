package com.github.ompc.jpromisor.impl;

import com.github.ompc.jpromisor.FutureFunction.FutureCallable;
import com.github.ompc.jpromisor.ListenableFuture;
import com.github.ompc.jpromisor.Promise;

import java.util.concurrent.Executor;

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
    public boolean tryException(Exception cause) {
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

    @Override
    public ListenableFuture<V> fulfill(Executor executor, FutureCallable<V> callable) {
        executor.execute(() -> {
            if (isDone()) {
                return;
            }
            try {
                trySuccess(callable.call());
            } catch (InterruptedException cause) {
                tryCancel();
                Thread.currentThread().interrupt();
            } catch (Exception cause) {
                tryException(cause);
            }
        });
        return this;
    }

}
