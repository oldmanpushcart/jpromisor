package com.github.ompc.jpromisor;

import com.github.ompc.jpromisor.FutureFunction.FutureCallable;
import com.github.ompc.jpromisor.FutureFunction.FutureConsumer;
import com.github.ompc.jpromisor.FutureFunction.FutureExecutable;
import com.github.ompc.jpromisor.impl.NotifiableFuture;

import java.util.concurrent.Executor;

/**
 * 承诺者
 */
public class Promisor {

    private final ListeningFutureHandlerFactory factory;

    /**
     * 承诺者
     */
    public Promisor() {
        this(() -> null);
    }

    /**
     * 承诺者
     *
     * @param factory 处理器
     */
    public Promisor(ListeningFutureHandlerFactory factory) {
        this.factory = factory;
    }

    /**
     * 承诺
     *
     * @param <V> 类型
     * @return 承诺
     */
    public <V> Promise<V> promise() {
        return new NotifiableFuture<>(factory.make());
    }

    /**
     * 承诺
     *
     * @param fn  承诺函数
     * @param <V> 类型
     * @return 承诺
     */
    public <V> Promise<V> promise(FutureConsumer<Promise<V>> fn) {
        final Promise<V> promise = promise();
        return promise(promise, () -> fn.accept(promise));
    }

    /**
     * 承诺
     *
     * @param promise 承诺
     * @param fn      承诺函数
     * @param <V>     类型
     * @return 承诺
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
     * 承诺并履约
     *
     * @param executor 执行器
     * @param fn       履约函数
     * @return 凭证
     */
    public ListenableFuture<Void> fulfill(Executor executor, FutureExecutable fn) {
        return this.<Void>promise().fulfill(executor, fn);
    }

    /**
     * 承诺并履约
     *
     * @param executor 执行器
     * @param fn       履约函数
     * @param <V>      类型
     * @return 凭证
     */
    public <V> ListenableFuture<V> fulfill(Executor executor, FutureCallable<V> fn) {
        return this.<V>promise().fulfill(executor, fn);
    }

}
