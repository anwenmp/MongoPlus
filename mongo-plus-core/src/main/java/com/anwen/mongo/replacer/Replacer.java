package com.anwen.mongo.replacer;

import com.anwen.mongo.support.BoolFunction;
import com.mongodb.client.MongoCollection;

import javax.swing.text.Document;
import java.lang.reflect.Method;

/**
 * 替换器接口
 *
 * @author loser
 * @date 2024/4/30
 */
public interface Replacer {

    default int order() {
        return Integer.MIN_VALUE;
    }

    /**
     * 执行方法
     *
     * @param proxy 代理对象
     * @param target 目标对象
     * @param method 方法
     * @param args 参数 数组的最后一个值为 MongoCollection<Document>
     * @return Object
     * @author JiaChaoYang
     */
    Object invoke(Object proxy, Object target, Method method, Object[] args) throws Throwable;

    @SuppressWarnings("unchecked")
    default MongoCollection<Document> getMongoCollection(Object[] args) {
        return (MongoCollection<Document>) args[args.length-1];
    }

    BoolFunction supplier();

}