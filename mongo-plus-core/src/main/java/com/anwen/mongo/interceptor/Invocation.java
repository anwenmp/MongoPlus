package com.anwen.mongo.interceptor;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.lang.reflect.Method;

public final class Invocation {

    /**
     * 代理对象
     */
    private final Object proxy;

    /**
     * 执行器对象
     */
    private final Object target;

    /**
     * 执行方法
     */
    private final Method method;

    /**
     * 方法参数
     */
    private final Object[] args;

    /**
     * mongoCollection
     */
    private final MongoCollection<Document> collection;

    @SuppressWarnings("unchecked")
    public Invocation(Object proxy,Object target, Method method, Object[] args){
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.args = args;
        this.collection = (MongoCollection<Document>) args[args.length-1];
    }

    public Object getProxy() {
        return proxy;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    /**
     * 执行
     * @return {@link java.lang.Object}
     * @author anwen
     */
    public Object invoke() throws Throwable {
        return method.invoke(target,args);
    }

}
