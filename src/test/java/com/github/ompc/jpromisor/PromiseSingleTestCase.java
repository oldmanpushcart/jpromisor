package com.github.ompc.jpromisor;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Promise单线程测试用例
 */
public class PromiseSingleTestCase extends ExecutorSupport {

    public PromiseSingleTestCase() {
        super(Executors.newCachedThreadPool());
    }

    // 计算三个数之和，符合预期
    @Test
    public void test$fulfill$sum$success() {
        final int sum = Promisor.fulfill(getExecutor(), () -> 100)
                .then(getExecutor(), num -> num + 100)
                .then(getExecutor(), num -> num + 100)
                .awaitUninterruptible()
                .getSuccess();
        Assert.assertEquals(300, sum);
    }

    // 计算三个数之和，且每个监听器的值符合期待
    @Test
    public void test$fulfill$sum_with_listener$success() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);
        final AtomicInteger countRef = new AtomicInteger();
        final int[] actual = new int[3];
        final int sum = Promisor.fulfill(getExecutor(), () -> 100)
                .onSuccess(getExecutor(), num -> {
                    countRef.incrementAndGet();
                    actual[0] = num;
                    latch.countDown();
                })
                .then(getExecutor(), num -> num + 100)
                .onSuccess(getExecutor(), num -> {
                    countRef.incrementAndGet();
                    actual[1] = num;
                    latch.countDown();
                })
                .then(getExecutor(), num -> num + 100)
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
    public void test$fulfill$sum_with_listener_throw_exception$success() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(3);
        final AtomicInteger countRef = new AtomicInteger();
        final int[] actual = new int[3];
        final int sum = Promisor.fulfill(getExecutor(), () -> 100)

                .then(getExecutor(), num -> num)
                .onSuccess(getExecutor(), num -> {
                    countRef.incrementAndGet();
                    latch.countDown();
                    throw new RuntimeException();
                })
                .onSuccess(num -> actual[0] = num)

                .then(getExecutor(), num -> num + 100)
                .onSuccess(getExecutor(), num -> {
                    countRef.incrementAndGet();
                    latch.countDown();
                    throw new RuntimeException();
                })
                .onSuccess(num -> actual[1] = num)

                .then(getExecutor(), num -> num + 100)
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

    @Test(expected = RuntimeException.class)
    public void test$fulfill$exception() throws Throwable {
        final ListenableFuture<Integer> future = Promisor.fulfill(getExecutor(), () -> 100)
                .then(getExecutor(), num -> {
                    if (num == 100) {
                        throw new RuntimeException("TEST!");
                    }
                    return num;
                })
                .awaitUninterruptible();
        Assert.assertTrue(future.isException());
        Assert.assertTrue(future.isDone());
        Assert.assertFalse(future.isSuccess());
        Assert.assertFalse(future.isCancelled());
        Assert.assertEquals("TEST!", future.getException().getMessage());
        throw future.getException();
    }


}
