package com.github.ompc.jpromisor;

/**
 * Future监听器
 *
 * @param <V> 类型
 */
@FunctionalInterface
public interface FutureListener<V> {

    /**
     * 完成
     *
     * @param future Future
     */
    void onDone(ListenableFuture<V> future);

    /**
     * 成功监听器
     *
     * @param <V> 类型
     */
    @FunctionalInterface
    interface OnSuccess<V> extends FutureListener<V> {

        @Override
        default void onDone(ListenableFuture<V> future) {
            if (future.isSuccess()) {
                onSuccess(future.getSuccess());
            }
        }

        /**
         * 成功
         *
         * @param value 结果
         */
        void onSuccess(V value);

    }

    /**
     * 失败监听器
     *
     * @param <V> 类型
     */
    @FunctionalInterface
    interface OnFailure<V> extends FutureListener<V> {

        @Override
        default void onDone(ListenableFuture<V> future) {
            if (future.isFailure()) {
                onFailure(future.getException());
            }
        }

        /**
         * 失败
         *
         * @param cause 错误
         */
        void onFailure(Exception cause);

    }

    /**
     * 取消监听器
     *
     * @param <V> 类型
     */
    @FunctionalInterface
    interface OnCancelled<V> extends FutureListener<V> {

        @Override
        default void onDone(ListenableFuture<V> future) {
            if (future.isCancelled()) {
                onCancelled();
            }
        }

        /**
         * 取消
         */
        void onCancelled();

    }

    /**
     * 异常监听器
     *
     * @param <V> 类型
     */
    @FunctionalInterface
    interface OnException<V> extends FutureListener<V> {

        @Override
        default void onDone(ListenableFuture<V> future) {
            if (future.isException()) {
                onException(future.getException());
            }
        }

        /**
         * 异常
         *
         * @param future Future
         */
        void onException(Exception future);

    }

    /**
     * 完成监听器
     *
     * @param <V> 类型
     */
    @FunctionalInterface
    interface OnDone<V> extends FutureListener<V> {

    }

}
