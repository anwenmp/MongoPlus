package com.mongoplus.domain;

/**
 * MongoPlus转换异常
 * @author JiaChaoYang
 */
public class MongoPlusConvertException extends MongoPlusException {
    public MongoPlusConvertException(String message) {
        super(message);
    }

    public MongoPlusConvertException(String message, Throwable cause) {
        super(message, cause);
    }
}
