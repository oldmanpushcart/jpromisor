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
     * 创建承诺
     *
     * @param <V> 类型
     * @return 承诺
     */
    public static <V> Promise<V> promise() {
        return new NotifiablePromise<>();
    }

    /**
     * 创建承诺
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
     * 创建承诺
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

    public static ListenableFuture<Void> fulfill(Executor executor, FutureExecutable fn) {
        return Promisor.<Void>promise().fulfill(executor, fn);
    }

    public static <V> ListenableFuture<V> fulfill(Executor executor, FutureCallable<V> fn) {
        return Promisor.<V>promise().fulfill(executor, fn);
    }

}
