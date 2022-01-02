package com.github.ompc.jpromisor.impl;

import com.github.ompc.jpromisor.ListenableFuture;
import com.github.ompc.jpromisor.ListeningFutureHandler;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 有状态的凭证
 *
 * @param <V>
 */
abstract class StatefulFuture<V> implements ListenableFuture<V> {

    private final AtomicReference<StateResult> resultRef = new AtomicReference<>();
    private final ListeningFutureHandler handler;

    /**
     * 状态凭证
     *
     * @param handler 凭证处理器
     */
    StatefulFuture(ListeningFutureHandler handler) {
        this.handler = handler;
        fireBegin();
    }

    // 通知开始
    private void fireBegin() {
        if (null != handler) {
            handler.onBegin(this);
        }
    }

    private boolean _isDone(StateResult result) {
        return null != result;
    }

    private boolean _isCancelled(StateResult result) {
        return _isDone(result) && result.state == State.CANCEL;
    }

    private boolean _isException(StateResult result) {
        return _isDone(result) && result.state == State.EXCEPTION;
    }

    private boolean _isSuccess(StateResult result) {
        return _isDone(result) && result.state == State.SUCCESS;
    }

    private boolean _isFailure(StateResult result) {
        return _isException(result) || _isCancelled(result);
    }

    @Override
    public boolean isFailure() {
        return _isFailure(resultRef.get());
    }

    @Override
    public boolean isSuccess() {
        return _isSuccess(resultRef.get());
    }

    @Override
    public boolean isException() {
        return _isException(resultRef.get());
    }

    @Override
    public boolean isCancelled() {
        return _isCancelled(resultRef.get());
    }

    @Override
    public boolean isDone() {
        return _isDone(resultRef.get());
    }

    @Override
    public Exception getException() {
        final StateResult result = resultRef.get();
        return _isFailure(result) ? (Exception) result.value : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getSuccess() {
        final StateResult result = resultRef.get();
        return _isSuccess(result) ? (V) result.value : null;
    }

    // 通知完成
    private boolean fireCompleted() {
        if (null != handler) {
            handler.onCompleted(this);
        }
        return true;
    }

    /**
     * 尝试取消
     *
     * @return TRUE | FALSE
     */
    boolean tryCancel() {
        return resultRef.compareAndSet(null, new StateResult(State.CANCEL, new CancellationException()))
                && fireCompleted();
    }

    /**
     * 尝试异常
     *
     * @param cause 异常原因
     * @return TRUE | FALSE
     */
    boolean tryException(Exception cause) {
        return resultRef.compareAndSet(null, new StateResult(State.EXCEPTION, cause))
                && fireCompleted();
    }

    /**
     * 尝试成功
     *
     * @param value 成功值
     * @return TRUE | FALSE
     */
    boolean trySuccess(V value) {
        return resultRef.compareAndSet(null, new StateResult(State.SUCCESS, value))
                && fireCompleted();
    }

    /**
     * 状态
     * EXCEPTIONAL
     * SUCCESSFUL
     * CANCELLED
     */
    private enum State {

        /**
         * 成功
         */
        SUCCESS,

        /**
         * 异常
         */
        EXCEPTION,

        /**
         * 取消
         */
        CANCEL

    }

    /**
     * 状态结果
     */
    private static class StateResult {

        private final State state;
        private final Object value;

        /**
         * 状态结果
         *
         * @param state 状态值
         * @param value 结果值
         */
        private StateResult(State state, Object value) {
            this.state = state;
            this.value = value;
        }

    }

}
