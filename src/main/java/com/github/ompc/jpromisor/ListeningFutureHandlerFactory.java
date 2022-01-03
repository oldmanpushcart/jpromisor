package com.github.ompc.jpromisor;

/**
 * 处理器工厂
 */
public interface ListeningFutureHandlerFactory {

    /**
     * 生产凭理器
     *
     * @return 处理器
     */
    ListeningFutureHandler make();

}
