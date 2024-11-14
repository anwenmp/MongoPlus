package com.mongoplus.domain;

/**
 * 危险操作异常
 * @author JiaChaoYang
 * @date 2023-11-23 11:57
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
