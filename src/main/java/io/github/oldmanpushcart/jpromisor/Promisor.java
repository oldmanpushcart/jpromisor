package io.github.oldmanpushcart.jpromisor;

import io.github.oldmanpushcart.jpromisor.impl.NotifiableFuture;
import io.github.oldmanpushcart.jpromisor.FutureFunction.FutureCallable;
import io.github.oldmanpushcart.jpromisor.FutureFunction.FutureConsumer;
import io.github.oldmanpushcart.jpromisor.FutureFunction.FutureExecutable;

import java.util.concurrent.Executor;

/**
 * 承诺者
 */
public class Promisor {

    private final ListeningInterceptor interceptor;

    /**
     * 承诺者
     */
    public Promisor() {
        this(ListeningInterceptor.empty);
    }

    /**
     * 承诺者
     *
     * @param interceptor 监听拦截器
     */
    public Promisor(ListeningInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    /**
     * Promise
     *
     * @param <V> 类型
     * @return Promise
     */
    public <V> Promise<V> promise() {
        return new NotifiableFuture<>(interceptor);
    }

    /**
     * Promise
     *
     * @param fn  函数
     * @param <V> 类型
     * @return Promise
     */
    public <V> Promise<V> promise(FutureConsumer<Promise<V>> fn) {
        final Promise<V> promise = promise();
        return promise(promise, () -> fn.accept(promise));
    }

    /**
     * Promise
     *
     * @param promise Promise
     * @param fn      函数
     * @param <V>     类型
     * @return Promise
     */
    public <V> Promise<V> promise(Promise<V> promise, FutureExecutable fn) {
        try {
            if (!promise.isDone()) {
                fn.execute();
            }
        } catch (Exception cause) {
            promise.tryException(cause);
        }
        return promise;
    }

    /**
     * 履约
     *
     * @param executor 执行器
     * @param fn       函数
     * @return Future
     */
    public ListenableFuture<Void> fulfill(Executor executor, FutureExecutable fn) {
        return this.<Void>promise().fulfill(executor, fn);
    }

    /**
     * 履约
     *
     * @param executor 执行器
     * @param fn       函数
     * @param <V>      类型
     * @return Future
     */
    public <V> ListenableFuture<V> fulfill(Executor executor, FutureCallable<V> fn) {
        return this.<V>promise().fulfill(executor, fn);
    }

}
