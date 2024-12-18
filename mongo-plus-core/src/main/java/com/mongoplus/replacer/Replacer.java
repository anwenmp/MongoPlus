package com.mongoplus.replacer;

import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.support.BoolFunction;
import com.mongodb.client.MongoCollection;

import javax.swing.text.Document;
import java.lang.reflect.Method;

/**
 * 替换器接口
 * <p>因多个替换器，最终只会生效一个，并不能形成责任链</p>
 * <p>请使用{@link AdvancedInterceptor}类，此类将在之后的版本中删除</p>
 * @author loser
 * @date 2024/4/30
 */
@Deprecated
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