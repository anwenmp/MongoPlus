package com.mongoplus.domain;

/**
 * MongoPlus动态数据源异常
 *
 * @author anwen
 */
public class MongoPlusDsException extends MongoPlusException {

    public MongoPlusDsException() {
        super("No data source found");
    }

    public MongoPlusDsException(String message) {
        super(message);
    }

    public MongoPlusDsException(String message,Exception e){
        super(message,e);
    }

}
