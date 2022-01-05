package io.github.ompc.jpromisor;

/**
 * 监听拦截器
 * <p>
 * 当前及从当前接力产生的{@link ListenableFuture}所持有的{@link FutureListener}均会被拦截，
 * 拦截器与监听器处于同一个线程
 * </p>
 *
 * <p>
 * 如：
 * <ul>
 * <li>{@link ListenableFuture#then(FutureFunction, FutureFunction)}</li>
 * <li>{@link ListenableFuture#success(FutureFunction)}</li>
 * <li>{@link ListenableFuture#exception(FutureFunction)}</li>
 * </ul>
 * </p>
 */
@FunctionalInterface
public interface ListeningInterceptor {

    /**
     * 拦截监听器运作
     *
     * @param future   Future
     * @param listener 监听器
     * @param <V>      类型
     */
    <V> void onListening(ListenableFuture<V> future, FutureListener<V> listener);

    /**
     * 空拦截器实现
     */
    ListeningInterceptor empty = new ListeningInterceptor() {

        @Override
        public <V> void onListening(ListenableFuture<V> future, FutureListener<V> listener) {
            listener.onDone(future);
        }

    };

}
