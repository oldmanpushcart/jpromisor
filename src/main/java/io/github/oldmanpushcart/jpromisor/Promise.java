package io.github.oldmanpushcart.jpromisor;

import java.util.concurrent.Executor;

/**
 * Promise
 *
 * @param <V> 类型
 */
public interface Promise<V> extends ListenableFuture<V> {

    /**
     * 自身（无意义）
     *
     * @return this
     */
    Promise<V> self();

    /**
     * 自身（无意义），强制返回指定的目标类型
     *
     * @param <P> 目标类型
     * @return this
     * @since 1.1.0
     */
    <P extends Promise<V>> P promise();

    /**
     * 尝试取消
     *
     * @return TRUE | FALSE
     */
    boolean tryCancel();

    /**
     * 尝试失败
     *
     * @param cause 失败原因
     * @return TRUE | FALSE
     */
    boolean tryException(Exception cause);

    /**
     * 尝试成功
     *
     * @param value 返回数据
     * @return TRUE | FALSE
     */
    boolean trySuccess(V value);

    /**
     * 尝试成功（设置{@code null}）
     *
     * @return TRUE | FALSE
     */
    boolean trySuccess();

    /**
     * 履约
     *
     * @param executor 执行器
     * @param fn       履约函数
     * @return Future
     */
    ListenableFuture<V> fulfill(Executor executor, FutureFunction.FutureCallable<V> fn);

    /**
     * 履约
     *
     * @param fn 履约函数
     * @return Future
     * @since 1.1.0
     */
    ListenableFuture<V> fulfill(FutureFunction.FutureCallable<V> fn);

    /**
     * 执行
     *
     * @param executor 执行器
     * @param fn       执行函数
     * @return this
     * @since 1.1.0
     */
    Promise<V> execute(Executor executor, FutureFunction.FutureConsumer<Promise<V>> fn);

    /**
     * 执行
     *
     * @param fn 执行函数
     * @return this
     * @since 1.1.0
     */
    Promise<V> execute(FutureFunction.FutureConsumer<Promise<V>> fn);

    /**
     * 接受目标Future的结果
     *
     * @param future 目标Future
     * @return this
     * @since 1.1.0
     */
    Promise<V> accept(ListenableFuture<V> future);

    /**
     * 接受目标Future的结果
     *
     * @param executor 执行器
     * @param future   目标Future
     * @return this
     * @since 1.1.0
     */
    Promise<V> accept(Executor executor, ListenableFuture<V> future);

    /**
     * 接受目标Future的失败
     *
     * @param future 目标Future
     * @return this
     * @since 1.1.0
     */
    Promise<V> acceptFail(ListenableFuture<?> future);

    /**
     * 接受目标Future的失败
     *
     * @param executor 执行器
     * @param future   目标Future
     * @return this
     * @since 1.1.0
     */
    Promise<V> acceptFail(Executor executor, ListenableFuture<?> future);

}
