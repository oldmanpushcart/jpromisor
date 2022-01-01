package com.github.ompc.jpromisor;

import com.github.ompc.jpromisor.FutureFunction.FutureCallable;
import com.github.ompc.jpromisor.FutureFunction.FutureConsumer;
import com.github.ompc.jpromisor.FutureFunction.FutureExecutable;
import com.github.ompc.jpromisor.impl.NotifiablePromise;

import java.util.concurrent.Executor;

/**
 * 承诺者
 */
public class Promisor {

    /**
     * 承诺
     *
     * @param <V> 类型
     * @return 承诺
     */
    public static <V> Promise<V> promise() {
        return new NotifiablePromise<>();
    }

    /**
     * 承诺
     *
     * @param fn  承诺函数
     * @param <V> 类型
     * @return 承诺
     */
    public static <V> Promise<V> promise(FutureConsumer<Promise<V>> fn) {
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
    public static <V> Promise<V> promise(Promise<V> promise, FutureExecutable fn) {
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
    public static ListenableFuture<Void> promise(Executor executor, FutureExecutable fn) {
        return Promisor.<Void>promise().fulfill(executor, fn);
    }

    /**
     * 承诺并履约
     *
     * @param executor 执行器
     * @param fn       履约函数
     * @param <V>      类型
     * @return 凭证
     */
    public static <V> ListenableFuture<V> promise(Executor executor, FutureCallable<V> fn) {
        return Promisor.<V>promise().fulfill(executor, fn);
    }

}
