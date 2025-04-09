package com.mongoplus.domain;

/**
 * 危险操作异常
 * @author JiaChaoYang
 **/
public class MongoPlusInterceptorException extends Error {

    public MongoPlusInterceptorException(String message){
        super(message);
    }

    public MongoPlusInterceptorException() {
    }

    public MongoPlusInterceptorException(Throwable cause) {
        super(cause);
    }
}
