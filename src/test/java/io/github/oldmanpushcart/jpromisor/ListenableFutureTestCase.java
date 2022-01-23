package io.github.oldmanpushcart.jpromisor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Promise单线程测试用例
 */
@RunWith(Parameterized.class)
public class ListenableFutureTestCase extends ExecutorSupport {

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {Executors.newSingleThreadExecutor()},
                {Executors.newFixedThreadPool(4)}
        });
    }

    public ListenableFutureTestCase(Executor executor) {
        super(executor);
    }

    // Promise的success/cancel/exception/done四个判断校验
    @Test
    public void test$promise$state() throws ExecutionException, InterruptedException, TimeoutException {

        final Promise<Integer> noneP = new Promisor().promise();
        Assert.assertFalse(noneP.isDone());
        Assert.assertFalse(noneP.isSuccess());
        Assert.assertFalse(noneP.isCancelled());
        Assert.assertFalse(noneP.isException());

        final Promise<Integer> successP = new Promisor().promise();
        successP.trySuccess(100);
        Assert.assertTrue(successP.isDone());
        Assert.assertTrue(successP.isSuccess());
        Assert.assertFalse(successP.isCancelled());
        Assert.assertFalse(successP.isException());
        Assert.assertEquals(100, successP.get().intValue());
        Assert.assertEquals(100, successP.get(100, TimeUnit.MILLISECONDS).intValue());
        Assert.assertEquals(100, successP.getSuccess().intValue());

        final Promise<Integer> cancelP = new Promisor().promise();
        cancelP.tryCancel();
        Assert.assertTrue(cancelP.isDone());
        Assert.assertFalse(cancelP.isSuccess());
        Assert.assertTrue(cancelP.isCancelled());
        Assert.assertFalse(cancelP.isException());
        Assert.assertEquals(CancellationException.class, cancelP.getException().getClass());

        final Promise<Integer> exceptionP = new Promisor().promise();
        exceptionP.tryException(new RuntimeException());
        Assert.assertTrue(exceptionP.isDone());
        Assert.assertFalse(exceptionP.isSuccess());
        Assert.assertFalse(exceptionP.isCancelled());
        Assert.assertTrue(exceptionP.isException());
        Assert.assertEquals(RuntimeException.class, exceptionP.getException().getClass());

    }

    // 计算三个数之和，符合预期
    @Test
    public void test$promise$sum() {
        final int sum = new Promisor().fulfill(getExecutor(), () -> 100)
                .success(getExecutor(), num -> num + 100)
                .success(getExecutor(), num -> num + 100)
                .awaitUninterruptible()
                .getSuccess();
        Assert.assertEquals(300, sum);
    }

    // 计算三个数之和，且每个监听器的值符合期待
    @Test
    public void test$promise$sum_with_listener() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);
        final AtomicInteger countRef = new AtomicInteger();
        final int[] actual = new int[3];
        final int sum = new Promisor().fulfill(getExecutor(), () -> 100)
                .onSuccess(getExecutor(), num -> {
                    countRef.incrementAndGet();
                    actual[0] = num;
                    latch.countDown();
                })
                .success(getExecutor(), num -> num + 100)
                .onSuccess(getExecutor(), num -> {
                    countRef.incrementAndGet();
                    actual[1] = num;
                    latch.countDown();
                })
                .success(getExecutor(), num -> num + 100)
                .onSuccess(getExecutor(), num -> {
                    countRef.incrementAndGet();
                    actual[2] = num;
                    latch.countDown();
                })
                .awaitUninterruptible()
                .getSuccess();

        Assert.assertEquals(300, sum);
        latch.await();
        Assert.assertEquals(3, countRef.get());
        Assert.assertArrayEquals(new int[]{100, 200, 300}, actual);
    }

    // 计算三个数之和，监听器抛出异常，且每个监听器的值符合期待
    @Test
    public void test$promise$sum_with_listener_throw_exception() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(3);
        final AtomicInteger countRef = new AtomicInteger();
        final int[] actual = new int[3];
        final int sum = new Promisor().fulfill(getExecutor(), () -> 100)

                .success(getExecutor(), num -> num)
                .onSuccess(getExecutor(), num -> {
                    countRef.incrementAndGet();
                    latch.countDown();
                    throw new RuntimeException();
                })
                .onSuccess(num -> actual[0] = num)

                .success(getExecutor(), num -> num + 100)
                .onSuccess(getExecutor(), num -> {
                    countRef.incrementAndGet();
                    latch.countDown();
                    throw new RuntimeException();
                })
                .onSuccess(num -> actual[1] = num)

                .success(getExecutor(), num -> num + 100)
                .onSuccess(getExecutor(), num -> {
                    countRef.incrementAndGet();
                    latch.countDown();
                    throw new RuntimeException();
                })
                .onSuccess(num -> actual[2] = num)
                .awaitUninterruptible()
                .getSuccess();

        Assert.assertEquals(300, sum);
        latch.await();
        Assert.assertEquals(3, countRef.get());
        Assert.assertArrayEquals(new int[]{100, 200, 300}, actual);

    }

    // 在then中抛出异常，最终结果为异常
    @Test(expected = RuntimeException.class)
    public void test$promise$then_throw_exception() throws Throwable {
        final ListenableFuture<Integer> future = new Promisor().fulfill(getExecutor(), () -> 100)
                .success(getExecutor(), num -> {
                    if (num == 100) {
                        throw new RuntimeException("TEST!");
                    }
                    return num;
                })
                .awaitUninterruptible();
        Assert.assertTrue(future.isException());
        Assert.assertEquals("TEST!", future.getException().getMessage());
        throw future.getException();
    }


    //承诺执行过程中线程被中断，承诺将会被取消
    @Test
    public void test$promise$cancel_by_interrupt() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);
        final ExecutorService work = Executors.newSingleThreadExecutor();
        final ListenableFuture<Void> future = new Promisor().fulfill(work, () -> {
            latch.countDown();
            synchronized (this) {
                this.wait();
            }
        });

        // 等承诺开始执行
        latch.await();

        // 取消work线程
        work.shutdownNow();
        Assert.assertTrue(work.isShutdown());

        // 等待承诺执行完成
        future.awaitUninterruptible();
        Assert.assertTrue(future.isCancelled());

    }

    // 承诺执行过程中，承诺被取消，结果为取消
    @Test
    public void test$promise$cancel() {

        final CountDownLatch latch = new CountDownLatch(1);
        final ListenableFuture<Void> future = new Promisor().fulfill(getExecutor(), latch::await);

        future.cancel(false);
        latch.countDown();

        Assert.assertTrue(future.isCancelled());

    }

    // 同时启动1K个承诺，完成三次计算，最终结果正确
    @Test
    public void test$promise$sum$10K() {

        final int length = 10000;
        final Collection<ListenableFuture<Integer>> futures = new ArrayList<>();
        for (int index = 0; index < length; index++) {
            final ListenableFuture<Integer> future = new Promisor().fulfill(getExecutor(), () -> 100)
                    .success(getExecutor(), num -> num * 2)
                    .success(getExecutor(), num -> num + 100);
            futures.add(future);
        }

        futures.forEach(ListenableFuture::awaitUninterruptible);
        futures.forEach(future -> Assert.assertEquals(300, future.getSuccess().intValue()));

    }


    // 链式调用，rejected会被传递
    @Test
    public void test$promise$chain_rejected() {

        final ListenableFuture<Integer> future = new Promisor().fulfill(getExecutor(), () -> 100)
                .success(getExecutor(), v -> v + 100)
                .success(getExecutor(), v -> v + 100)
                .success(getExecutor(), v -> v + 100)
                .success(getExecutor(), v -> v + 100)
                .success(getExecutor(), v -> v + 100)
                .success(getExecutor(), v -> {
                    if (null != v) {
                        throw new RuntimeException();
                    }
                    return v;
                })
                .success(getExecutor(), v -> v + 100)
                .success(getExecutor(), v -> v + 100)
                .exception(getExecutor(), e -> 333)
                .awaitUninterruptible();

        Assert.assertTrue(future.isSuccess());
        Assert.assertEquals(333, future.getSuccess().intValue());

    }


    // 链式调用赋值给另外一个Promise
    @Test
    public void test$promise$assign() {
        final Promise<Integer> promise = new Promisor().promise();
        new Promisor().fulfill(getExecutor(), () -> "100")
                .success(getExecutor(), Integer::valueOf)
                .exception(getExecutor(), e -> 200)
                .assign(promise)
                .awaitUninterruptible();

        Assert.assertTrue(promise.isSuccess());
        Assert.assertEquals(100, promise.getSuccess().intValue());

    }

    // 增加拦截器拦截数量正确
    @Test
    public void test$promise$interceptor() {

        final AtomicInteger cnt = new AtomicInteger();
        final ListenableFuture<Integer> future = new Promisor(new ListeningInterceptor() {
            @Override
            public <V> void onListening(ListenableFuture<V> future, FutureListener<V> listener) {
                cnt.incrementAndGet();
                listener.onDone(future);
            }
        }).fulfill(getExecutor(), () -> 100)

                // +1
                .onSuccess(v -> {

                })

                // +0
                .onFailure(e -> {

                })

                // +1
                .<Integer>success(v -> {
                    throw new RuntimeException();
                })

                // +1
                .success(v -> v + 1)

                // +1
                .exception(e -> 400)
                .awaitUninterruptible();

        Assert.assertEquals(400, future.getSuccess().intValue());
        Assert.assertEquals(4, cnt.get());

    }

    // self / future / promise能正常转换类型
    @Test
    public void test$self() {

        final TokenFuture<String> future = new TokenPromiseImpl<String>("HELLO")
                .execute(getExecutor(), promise -> {

                })
                .fulfill(getExecutor(), () -> "WORLD!")
                .future();

        future.awaitUninterruptible();
        Assert.assertEquals("HELLO", future.getToken());
        Assert.assertEquals("WORLD!", future.getSuccess());

    }

}
