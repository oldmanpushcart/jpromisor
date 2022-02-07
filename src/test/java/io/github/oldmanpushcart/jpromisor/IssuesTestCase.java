package io.github.oldmanpushcart.jpromisor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 这个测试用例集用来验证修复的BUG
 */
@RunWith(Parameterized.class)
public class IssuesTestCase extends ExecutorSupport {

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {Executors.newSingleThreadExecutor()},
                {Executors.newFixedThreadPool(4)}
        });
    }

    public IssuesTestCase(Executor executor) {
        super(executor);
    }

    @Test
    public void test$issues$1() throws Exception {
        final AtomicInteger cnt = new AtomicInteger();
        final FutureListener<Object> listener = future -> cnt.incrementAndGet();

        new Promisor().promise()
                .execute(promise -> {
                    promise.appendListener(listener).removeListener(listener);
                    promise.trySuccess();
                })
                .sync();

        Assert.assertEquals(0, cnt.get());

    }

}
