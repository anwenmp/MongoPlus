package com.mongoplus.toolkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class ExceptionUtil {

    /**
     * 解包异常
     * @param wrapped 异常
     * @return {@link java.lang.Throwable}
     * @author anwen
     */
    public static Throwable unwrapThrowable(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }

    public static <R> R rethrow(final Throwable throwable) {
        // claim that the typeErasure invocation throws a RuntimeException
        return ExceptionUtil.<R, RuntimeException>typeErasure(throwable);
    }

    /**
     * 声称使用类型擦除是另一种异常类型。这隐藏了Java编译器的检查例外，允许在方法的投掷子句中抛出检查的例外。
     * @param throwable 异常
     * @return {@link R}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    private static <R, T extends Throwable> R typeErasure(final Throwable throwable) throws T {
        throw (T) throwable;
    }

}
