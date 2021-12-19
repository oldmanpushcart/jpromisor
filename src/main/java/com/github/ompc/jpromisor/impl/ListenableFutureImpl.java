package com.github.ompc.jpromisor.impl;

import com.github.ompc.jpromisor.FutureListener;
import com.github.ompc.jpromisor.ListenableFuture;
import com.github.ompc.jpromisor.Promise;
import com.github.ompc.jpromisor.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;

class ListenableFutureImpl<V> extends FutureImpl<V> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Executor self = Runnable::run;

    /*
     * 等待通知集合
     */
    private final Collection<FutureListener<V>> notifies = new ArrayList<>();

    /*
     * 已通知标记
     */
    private volatile boolean notified;

    /**
     * <p>重点，此处为通知核心逻辑!</p>
     *
     * @param listener 等待通知的执行器，可为空
     */
    private void notify(FutureListener<V> listener) {

        // 立即通知集合
        final Collection<FutureListener<V>> immediateNotifies = new ArrayList<>();

        // 监听器加入到立即通知集合，将会在本次完成通知
        if (null != listener) {
            immediateNotifies.add(listener);
        }

        // 如果是第一次通知，需要将之前等待的监听器合入
        if (!notified) {
            synchronized (this) {

                // double check
                if (!notified) {

                    // 克隆并清空原有监听器队列
                    immediateNotifies.addAll(notifies);
                    notifies.clear();

                    // 标记为已通知
                    notified = true;

                }

            }
        }

        // 立即进行通知
        immediateNotifies.forEach(notify -> notify.onDone(this));

    }

    @Override
    public ListenableFuture<V> appendListener(FutureListener<V> listener) {
        return appendListener(self, listener);
    }

    @Override
    public ListenableFuture<V> appendListener(Executor executor, FutureListener<V> listener) {

        // 将监听器封装为异步执行
        final FutureListener<V> wrap = future -> executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.onDone(ListenableFutureImpl.this);
                } catch (Throwable cause) {
                    logger.warn("{} notify listener error!", this, cause);
                }
            }
        });

        // 如若从未进行过通知，则将监听器加入到等待通知集合
        if (!notified) {
            synchronized (this) {
                if (!notified) {
                    notifies.add(wrap);
                    return this;
                }
            }
        }

        // 如果已通知过，则需要自行进行通知
        notify(wrap);
        return this;
    }

    @Override
    public ListenableFuture<V> removeListener(FutureListener<V> listener) {
        synchronized (this) {
            notifies.remove(listener);
        }
        return this;
    }

    @Override
    public <T> ListenableFuture<T> then(Then<V, T> then) {
        return then(self, then);
    }

    @Override
    public <T> ListenableFuture<T> then(Executor executor, Then<V, T> then) {
        final Promise<T> thenP = new PromiseImpl<>();

        // 监听器挂钩
        onDone(executor, future -> {

            // exception
            if (future.isException()) {
                thenP.tryException(future.getException());
            }

            // cancelled
            else if (future.isCancelled()) {
                thenP.tryCancel();
            }

            // success
            else if (future.isSuccess()) {
                try {
                    thenP.trySuccess(then.then(getSuccess()));
                } catch (Exception cause) {
                    thenP.tryException(cause);
                }
            }

            // other
            else {
                throw new IllegalStateException();
            }

        });

        return thenP;
    }

    @Override
    boolean tryCancel() {
        if (super.tryCancel()) {
            notify(null);
            return true;
        }
        return false;
    }

    @Override
    boolean tryException(Throwable cause) {
        if (super.tryException(cause)) {
            notify(null);
            return true;
        }
        return false;
    }

    @Override
    boolean trySuccess(V value) {
        if (super.trySuccess(value)) {
            notify(null);
            return true;
        }
        return false;
    }

}
