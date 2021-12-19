package com.github.ompc.jpromisor;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * 凭证
 * <pre>
 * <table>
 *     <tr>
 *         <th>METHOD</th>
 *         <th>CANCEL</th>
 *         <th>EXCEPTION</th>
 *         <th>SUCCESS</th>
 *     </tr>
 *     <tr>
 *         <td>{@link #isSuccess()}</td>
 *         <td>{@code false}</td>
 *         <td>{@code false}</td>
 *         <td>{@code true}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #isFailure()}</td>
 *         <td>{@code true}</td>
 *         <td>{@code true}</td>
 *         <td>{@code false}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #isCancelled()}</td>
 *         <td>{@code true}</td>
 *         <td>{@code false}</td>
 *         <td>{@code false}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #isException()}</td>
 *         <td>{@code false}</td>
 *         <td>{@code true}</td>
 *         <td>{@code false}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #isDone()}</td>
 *         <td>{@code true}</td>
 *         <td>{@code true}</td>
 *         <td>{@code true}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #getException()}</td>
 *         <td>{@code null}</td>
 *         <td>{@code cause}</td>
 *         <td>{@code null}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #getSuccess()}</td>
 *         <td>{@code null}</td>
 *         <td>{@code null}</td>
 *         <td>{@code value}</td>
 *     </tr>
 * </table>
 * </pre>
 *
 * @param <V> 类型
 */
public interface ListenableFuture<V> extends Future<V> {

    /**
     * 是否失败
     *
     * @return TRUE | FALSE
     */
    boolean isFailure();

    /**
     * 是否成功
     *
     * @return TRUE | FALSE
     */
    boolean isSuccess();

    /**
     * 是否异常
     *
     * @return TRUE | FALSE
     */
    boolean isException();

    /**
     * 是否取消
     *
     * @return TRUE | FALSE
     */
    @Override
    boolean isCancelled();

    /**
     * 获取异常
     *
     * @return 异常
     */
    Throwable getException();

    /**
     * 获取返回值
     *
     * @return 获取返回值
     */
    V getSuccess();

    /**
     * 添加监听器
     * <p>
     * {@link #isDone()} == true 的时候触发
     * </p>
     *
     * @param listener 监听器
     * @return this
     */
    ListenableFuture<V> appendListener(FutureListener<V> listener);

    /**
     * 添加监听器，监听动作由指定执行器完成
     * <p>
     * {@link #isDone()} == true 的时候触发
     * </p>
     *
     * @param executor 执行器
     * @param listener 监听器
     * @return this
     */
    ListenableFuture<V> appendListener(Executor executor, FutureListener<V> listener);

    /**
     * 添加完成监听器
     * <p>
     * {@link #isDone()} == true 的时候触发
     * </p>
     *
     * @param listener 监听器
     * @return this
     */
    default ListenableFuture<V> onDone(FutureListener.OnDone<V> listener) {
        return appendListener(listener);
    }

    /**
     * 添加完成监听器，监听动作由指定执行器完成
     * <p>
     * {@link #isDone()} == true 的时候触发
     * </p>
     *
     * @param executor 执行器
     * @param listener 监听器
     * @return this
     */
    default ListenableFuture<V> onDone(Executor executor, FutureListener.OnDone<V> listener) {
        return appendListener(executor, listener);
    }

    /**
     * 添加成功监听器
     * <p>
     * {@link #isSuccess()} == true 的时候触发
     * </p>
     *
     * @param listener 监听器
     * @return this
     */
    default ListenableFuture<V> onSuccess(FutureListener.OnSuccess<V> listener) {
        return appendListener(listener);
    }

    /**
     * 添加成功监听器，监听动作由指定执行器完成
     * <p>
     * {@link #isSuccess()} == true 的时候触发
     * </p>
     *
     * @param executor 执行器
     * @param listener 监听器
     * @return this
     */
    default ListenableFuture<V> onSuccess(Executor executor, FutureListener.OnSuccess<V> listener) {
        return appendListener(executor, listener);
    }

    /**
     * 添加失败监听器
     * <p>
     * {@link #isCancelled()} ()} == true 或者 {@link #isException()} == true 的时候触发
     * </p>
     *
     * @param listener 监听器
     * @return this
     */
    default ListenableFuture<V> onFailure(FutureListener.OnFailure<V> listener) {
        return appendListener(listener);
    }

    /**
     * 添加失败监听器，监听动作由指定执行器完成
     * <p>
     * {@link #isCancelled()} ()} == true 或者 {@link #isException()} == true 的时候触发
     * </p>
     *
     * @param executor 执行器
     * @param listener 监听器
     * @return this
     */
    default ListenableFuture<V> onFailure(Executor executor, FutureListener.OnFailure<V> listener) {
        return appendListener(executor, listener);
    }

    /**
     * 添加取消监听器
     * <p>
     * {@link #isCancelled()} == true 的时候触发
     * </p>
     *
     * @param listener 监听器
     * @return this
     */
    default ListenableFuture<V> onCancelled(FutureListener.OnCancelled<V> listener) {
        return appendListener(listener);
    }

    /**
     * 添加取消监听器，监听动作由指定执行器完成
     * <p>
     * {@link #isCancelled()} == true 的时候触发
     * </p>
     *
     * @param executor 执行器
     * @param listener 监听器
     * @return this
     */
    default ListenableFuture<V> onCancelled(Executor executor, FutureListener.OnCancelled<V> listener) {
        return appendListener(executor, listener);
    }

    /**
     * 添加异常监听器
     * <p>
     * {@link #isException()} == true 的时候触发
     * </p>
     *
     * @param listener 监听器
     * @return this
     */
    default ListenableFuture<V> onException(FutureListener.OnException<V> listener) {
        return appendListener(listener);
    }

    /**
     * 添加异常监听器，监听动作由指定执行器完成
     * <p>
     * {@link #isException()} == true 的时候触发
     * </p>
     *
     * @param executor 执行器
     * @param listener 监听器
     * @return this
     */
    default ListenableFuture<V> onException(Executor executor, FutureListener.OnException<V> listener) {
        return appendListener(executor, listener);
    }

    /**
     * 移除监听器
     *
     * @param listener 监听器
     * @return this
     */
    ListenableFuture<V> removeListener(FutureListener<V> listener);

    /**
     * 同步等待结果
     *
     * @return this
     * @throws InterruptedException  等待过程被中断
     * @throws ExecutionException    结果为异常
     * @throws CancellationException 结果为取消
     */
    ListenableFuture<V> sync() throws InterruptedException, ExecutionException, CancellationException;

    /**
     * 同步等待结果，等待过程被中断后不会抛出异常，将继续往下执行
     *
     * @return this
     * @throws ExecutionException    结果为异常
     * @throws CancellationException 结果为取消
     */
    ListenableFuture<V> syncUninterruptible() throws ExecutionException, CancellationException;

    /**
     * 阻塞并等待完成
     *
     * @return this
     * @throws InterruptedException 等待过程被中断
     */
    ListenableFuture<V> await() throws InterruptedException;

    /**
     * 阻塞并等待完成，等待过程被中断后不会抛出异常，将继续往下执行
     *
     * @return this
     */
    ListenableFuture<V> awaitUninterruptible();

    /**
     * 然后，常用于ThingFuture的类型转换
     *
     * @param then 值转换器
     * @param <T>  转换后新类型
     * @return 然后凭证
     */
    <T> ListenableFuture<T> then(Then<V, T> then);

    <T> ListenableFuture<T> then(Executor executor, Then<V, T> then);

}
