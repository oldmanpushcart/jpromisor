package com.github.ompc.jpromisor;

/**
 * 承诺
 *
 * @param <V> 类型
 */
public interface Promise<V> extends ListenableFuture<V> {

    /**
     * 承诺自身（无意义）
     *
     * @return this
     */
    Promise<V> self();

    /**
     * 尝试取消
     *
     * @return TRUE | FALSE
     */
    boolean tryCancel();

    /**
     * 尝试失败
     *
     * @param cause 失败原因
     * @return TRUE | FALSE
     */
    boolean tryException(Throwable cause);

    /**
     * 尝试成功
     *
     * @param value 返回数据
     * @return TRUE | FALSE
     */
    boolean trySuccess(V value);

    /**
     * 尝试成功（设置{@code null}）
     *
     * @return TRUE | FALSE
     */
    boolean trySuccess();

}
