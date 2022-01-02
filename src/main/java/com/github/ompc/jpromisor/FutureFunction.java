package com.github.ompc.jpromisor;

/**
 * 凭证函数
 *
 * @param <V> 入参类型
 * @param <T> 返回类型
 */
public interface FutureFunction<V, T> {

    /**
     * 函数应用
     *
     * @param v 入参值
     * @return 返回值
     * @throws Exception 函数应用异常
     */
    T apply(V v) throws Exception;

    /**
     * 调用函数
     *
     * @param <T> 返回类型
     */
    interface FutureCallable<T> extends FutureFunction<Void, T> {

        @Override
        default T apply(Void unused) throws Exception {
            return call();
        }

        /**
         * 函数调用
         *
         * @return 返回值
         * @throws Exception 函数调用异常
         */
        T call() throws Exception;

    }

    /**
     * 执行函数
     */
    interface FutureExecutable extends FutureCallable<Void> {

        @Override
        default Void call() throws Exception {
            execute();
            return null;
        }

        /**
         * 函数执行
         *
         * @throws Exception 函数执行异常
         */
        void execute() throws Exception;

    }

    /**
     * 消费函数
     *
     * @param <V> 消费类型
     */
    interface FutureConsumer<V> extends FutureFunction<V, Void> {

        @Override
        default Void apply(V v) throws Exception {
            accept(v);
            return null;
        }

        /**
         * 消费
         *
         * @param v 消费值
         * @throws Exception 消费失败
         */
        void accept(V v) throws Exception;

    }

}
