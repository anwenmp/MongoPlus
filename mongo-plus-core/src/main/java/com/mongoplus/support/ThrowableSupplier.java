package com.mongoplus.support;

/**
 * 支持抛出Throwable的函数式接口
 * @param <T> 结果类型
 */
@FunctionalInterface
public interface ThrowableSupplier<T> {

    /**
     * 获取结果
     * @return 结果
     * @throws Throwable 抛出的异常
     */
    T get() throws Throwable;

}
