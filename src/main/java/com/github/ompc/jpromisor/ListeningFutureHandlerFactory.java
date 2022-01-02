package com.github.ompc.jpromisor;

/**
 * 凭证处理器工厂
 */
public interface ListeningFutureHandlerFactory {

    /**
     * 生产凭证处理器
     *
     * @return 承诺处理器
     */
    ListeningFutureHandler make();

}
