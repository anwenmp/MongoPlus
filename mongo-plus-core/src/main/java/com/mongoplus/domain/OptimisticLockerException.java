package com.mongoplus.domain;

/**
 * 乐观锁异常
 *
 * @author anwen
 */
public class OptimisticLockerException extends MongoPlusException {

    public OptimisticLockerException(String message) {
        super(message);
    }

    public OptimisticLockerException(String message, Throwable cause) {
        super(message, cause);
    }
}
