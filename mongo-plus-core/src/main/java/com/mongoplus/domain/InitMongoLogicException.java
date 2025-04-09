package com.mongoplus.domain;

/**
 * 初始化MongoPlus逻辑删除异常
 *
 * @author loser
 */
public class InitMongoLogicException extends MongoPlusException {

    public InitMongoLogicException(String message) {
        super(message);
        this.message = message;
    }

}
