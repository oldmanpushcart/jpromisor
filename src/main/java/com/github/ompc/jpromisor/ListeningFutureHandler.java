package com.github.ompc.jpromisor;

/**
 * 处理器
 */
public interface ListeningFutureHandler {

    /**
     * 开始
     *
     * @param future Future
     */
    void onBegin(ListenableFuture<?> future);

    /**
     * 结束
     *
     * @param future Future
     */
    void onCompleted(ListenableFuture<?> future);

    /**
     * 监听器：开始
     *
     * @param future   Future
     * @param listener 监听器
     */
    void onListeningBegin(ListenableFuture<?> future, FutureListener<?> listener);

    /**
     * 监听器：完成
     *
     * @param future   Future
     * @param listener 监听器
     */
    void onListeningCompleted(ListenableFuture<?> future, FutureListener<?> listener);

    /**
     * 监听器：异常
     *
     * @param future   Future
     * @param listener 监听器
     * @param cause    监听器异常原因
     */
    void onListeningException(ListenableFuture<?> future, FutureListener<?> listener, Exception cause);

}
