package com.mongoplus.domain;

/**
 * 初始化连接异常
 * @author JiaChaoYang
 */
public class InitMongoCollectionException extends MongoPlusException {
    private String message;

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public InitMongoCollectionException(String message){
        super(message);
        this.message = message;
    }

}
