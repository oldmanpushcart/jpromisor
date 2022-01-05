package io.github.ompc.jpromisor;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Future
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
    Exception getException();

    /**
     * 获取返回值
     *
     * @return 获取返回值
     */
    V getSuccess();

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
     * 成功接力
     *
     * @param fn  接力函数
     * @param <T> 类型
     * @return 接力Future
     */
    <T> ListenableFuture<T> success(FutureFunction<V, T> fn);

    /**
     * 成功接力
     *
     * @param executor 执行器
     * @param fn       接力函数
     * @param <T>      类型
     * @return 接力Future
     */
    <T> ListenableFuture<T> success(Executor executor, FutureFunction<V, T> fn);

    /**
     * 异常接力
     *
     * @param fn 接力函数
     * @return 接力Future
     */
    ListenableFuture<V> exception(FutureFunction<Exception, V> fn);

    /**
     * 异常接力
     *
     * @param executor 执行器
     * @param fn       接力函数
     * @return 接力Future
     */
    ListenableFuture<V> exception(Executor executor, FutureFunction<Exception, V> fn);

    /**
     * 接力
     *
     * @param success   成功函数
     * @param exception 异常函数
     * @param <T>       类型
     * @return 接力Future
     */
    <T> ListenableFuture<T> then(FutureFunction<V, T> success, FutureFunction<Exception, T> exception);

    /**
     * 接力
     *
     * @param executor  执行器
     * @param success   成功函数
     * @param exception 异常函数
     * @param <T>       类型
     * @return 接力Future
     */
    <T> ListenableFuture<T> then(Executor executor, FutureFunction<V, T> success, FutureFunction<Exception, T> exception);

    /**
     * 当前Future结果赋值给另外一个Promise
     *
     * @param promise Promise
     * @param <P>     类型
     * @return Promise
     */
    <P extends Promise<V>> P assign(P promise);

    /**
     * 当前Future结果赋值给另外一个Promise
     *
     * @param executor 执行器
     * @param promise  Promise
     * @param <P>      类型
     * @return Promise
     */
    <P extends Promise<V>> P assign(Executor executor, P promise);

}
