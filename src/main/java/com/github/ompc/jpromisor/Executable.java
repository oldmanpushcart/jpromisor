package com.github.ompc.jpromisor;

import java.util.concurrent.Callable;

public interface Executable extends Callable<Void> {

    @Override
    default Void call() throws Exception {
        execute();
        return null;
    }

    void execute() throws Exception;

}
