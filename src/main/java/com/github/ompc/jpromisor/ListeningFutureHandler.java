package com.github.ompc.jpromisor;

/**
 * 凭证处理器
 */
public interface ListeningFutureHandler {

    /**
     * 凭证开始
     *
     * @param future 凭证
     */
    void onBegin(ListenableFuture<?> future);

    /**
     * 凭证结束
     *
     * @param future 凭证
     */
    void onCompleted(ListenableFuture<?> future);

    /**
     * 凭证监听器：开始
     *
     * @param future   凭证
     * @param listener 监听器
     */
    void onListeningBegin(ListenableFuture<?> future, FutureListener<?> listener);

    /**
     * 凭证监听器：完成
     *
     * @param future   凭证
     * @param listener 监听器
     */
    void onListeningCompleted(ListenableFuture<?> future, FutureListener<?> listener);

    /**
     * 凭证监听器：异常
     *
     * @param future   凭证
     * @param listener 监听器
     * @param cause    监听器异常原因
     */
    void onListeningException(ListenableFuture<?> future, FutureListener<?> listener, Exception cause);

}
