package io.github.oldmanpushcart.jpromisor.impl;

import io.github.oldmanpushcart.jpromisor.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

import static io.github.oldmanpushcart.jpromisor.FutureFunction.identity;
import static io.github.oldmanpushcart.jpromisor.FutureFunction.throwing;

/**
 * 可通知Future实现
 *
 * @param <V> 类型
 */
public class NotifiableFuture<V> extends StatefulFuture<V> implements Promise<V> {

    private static final Executor self = Runnable::run;

    /*
     * 同步器
     */
    private final CountDownLatch latch = new CountDownLatch(1);

    /*
     * 等待通知集合
     */
    private final Collection<FutureListener<V>> notifies = new ArrayList<>();

    /*
     * 已通知标记
     */
    private volatile boolean notified;

    /*
     * 监听拦截器
     */
    private final ListeningInterceptor interceptor;

    /**
     * 可通知Future
     *
     * @param interceptor 监听拦截器
     */
    public NotifiableFuture(ListeningInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public ListenableFuture<V> sync() throws InterruptedException, ExecutionException, CancellationException {
        get();
        return this;
    }

    @Override
    public ListenableFuture<V> syncUninterruptible() throws ExecutionException, CancellationException {
        boolean isInterrupted = false;
        try {
            while (true) {
                try {
                    sync();
                    break;
                } catch (InterruptedException e) {
                    isInterrupted = true;
                }
            }
        } finally {
            if (isInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return this;
    }

    @Override
    public ListenableFuture<V> await() throws InterruptedException {
        latch.await();
        return this;
    }

    @Override
    public ListenableFuture<V> awaitUninterruptible() {
        boolean isInterrupted = false;
        try {
            while (true) {
                try {
                    await();
                    break;
                } catch (InterruptedException e) {
                    isInterrupted = true;
                }
            }
        } finally {
            if (isInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return this;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return tryCancel();
    }

    @Override
    public Promise<V> self() {
        return this;
    }

    @Override
    public boolean tryCancel() {
        if (super.tryCancel()) {
            latch.countDown();
            notify(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean tryException(Exception cause) {
        if (super.tryException(cause)) {
            latch.countDown();
            notify(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean trySuccess(V value) {
        if (super.trySuccess(value)) {
            latch.countDown();
            notify(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean trySuccess() {
        return trySuccess(null);
    }

    @Override
    public ListenableFuture<V> fulfill(Executor executor, FutureFunction.FutureCallable<V> callable) {
        executor.execute(() -> {
            if (isDone()) {
                return;
            }
            try {
                trySuccess(callable.call());
            } catch (InterruptedException cause) {
                tryCancel();
                Thread.currentThread().interrupt();
            } catch (Exception cause) {
                tryException(cause);
            }
        });
        return this;
    }

    private V _get() throws ExecutionException {
        if (isException()) {
            throw new ExecutionException(getException());
        }
        if (isCancelled()) {
            throw (CancellationException) getException();
        }
        if (isSuccess()) {
            return getSuccess();
        }
        throw new IllegalStateException();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        latch.await();
        return _get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!latch.await(timeout, unit)) {
            throw new TimeoutException();
        }
        return _get();
    }

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
        final FutureListener<V> wrap = future -> {

            // 判断是否需要跳过当前listener
            if (listener instanceof FutureListener.OnSuccess) {
                if (!isSuccess()) {
                    return;
                }
            } else if (listener instanceof FutureListener.OnCancelled) {
                if (!isCancelled()) {
                    return;
                }
            } else if (listener instanceof FutureListener.OnException) {
                if (!isException()) {
                    return;
                }
            } else if (listener instanceof FutureListener.OnFailure) {
                if (!isException() && !isCancelled()) {
                    return;
                }
            }

            // 执行监听器
            executor.execute(() -> interceptor.onListening(this, listener));

        };

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
    public <T> ListenableFuture<T> success(FutureFunction<V, T> fn) {
        return success(self, fn);
    }

    @Override
    public <T> ListenableFuture<T> success(Executor executor, FutureFunction<V, T> fn) {
        return then(executor, fn, throwing());
    }

    @Override
    public ListenableFuture<V> exception(FutureFunction<Exception, V> fn) {
        return exception(self, fn);
    }

    @Override
    public ListenableFuture<V> exception(Executor executor, FutureFunction<Exception, V> fn) {
        return then(executor, identity(), fn);
    }

    @Override
    public <T> ListenableFuture<T> then(FutureFunction<V, T> success, FutureFunction<Exception, T> exception) {
        return then(self, success, exception);
    }

    @Override
    public <T> ListenableFuture<T> then(Executor executor, FutureFunction<V, T> success, FutureFunction<Exception, T> exception) {
        final NotifiableFuture<T> thenF = new NotifiableFuture<>(interceptor);

        // 监听器挂钩
        onDone(executor, future -> {

            // exception
            if (future.isException()) {
                try {
                    thenF.trySuccess(exception.apply(future.getException()));
                } catch (InterruptedException cause) {
                    thenF.tryCancel();
                    Thread.currentThread().interrupt();
                } catch (Exception cause) {
                    thenF.tryException(cause);
                }
            }

            // cancelled
            else if (future.isCancelled()) {
                thenF.tryCancel();
            }

            // success
            else if (future.isSuccess()) {
                try {
                    thenF.trySuccess(success.apply(getSuccess()));
                } catch (InterruptedException cause) {
                    thenF.tryCancel();
                    Thread.currentThread().interrupt();
                } catch (Exception cause) {
                    thenF.tryException(cause);
                }
            }

            // other
            else {
                throw new IllegalStateException();
            }

        });

        return thenF;
    }

    @Override
    public <P extends Promise<V>> P assign(P promise) {
        return assign(self, promise);
    }

    @Override
    public <P extends Promise<V>> P assign(Executor executor, P promise) {
        if (promise.isDone()) {
            return promise;
        }
        onDone(executor, future -> {
            if (future.isException()) {
                promise.tryException(future.getException());
            } else if (future.isCancelled()) {
                promise.tryCancel();
            } else if (future.isSuccess()) {
                promise.trySuccess(future.getSuccess());
            } else {
                throw new IllegalStateException();
            }
        });
        return promise;
    }
}
