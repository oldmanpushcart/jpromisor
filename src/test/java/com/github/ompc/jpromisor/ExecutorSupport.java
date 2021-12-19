package com.github.ompc.jpromisor;

import org.junit.AfterClass;

import java.util.concurrent.Executor;

public class ExecutorSupport {

    private final Executor executor;

    public ExecutorSupport(Executor executor) {
        this.executor = executor;
    }

    public Executor getExecutor() {
        return executor;
    }

}
