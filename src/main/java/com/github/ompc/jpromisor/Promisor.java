package com.github.ompc.jpromisor;

import com.github.ompc.jpromisor.impl.PromiseImpl;

import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
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
        return new PromiseImpl<>();
    }

    public static ListenableFuture<Void> fulfill(Executor executor, Executable executable) {
        return fulfill(executor, (Callable<Void>)executable);
    }

    public static ListenableFuture<Void> fulfill(Promise<Void> promise, Executor executor, Executable executable) {
        return fulfill(promise, executor, (Callable<Void>)executable);
    }

    /**
     * 履约承诺
     *
     * @param executor 执行器
     * @param callable 履约
     * @param <V>      类型
     * @return 凭证
     */
    public static <V> ListenableFuture<V> fulfill(Executor executor, Callable<V> callable) {
        return fulfill(promise(), executor, callable);
    }

    /**
     * 履约承诺
     *
     * @param promise  承诺
     * @param executor 执行器
     * @param callable 履约
     * @param <V>      类型
     * @return 凭证
     */
    public static <V> ListenableFuture<V> fulfill(Promise<V> promise, Executor executor, Callable<V> callable) {
        executor.execute(() -> {
            if (promise.isDone()) {
                return;
            }
            try {
                promise.trySuccess(callable.call());
            } catch (InterruptedException cause) {
                promise.tryCancel();
                Thread.currentThread().interrupt();
            } catch (Throwable cause) {
                promise.tryException(cause);
            }
        });
        return promise;
    }


}
