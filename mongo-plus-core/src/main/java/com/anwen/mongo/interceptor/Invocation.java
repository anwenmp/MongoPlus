package com.anwen.mongo.interceptor;

import com.anwen.mongo.enums.ExecuteMethodEnum;
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

    /**
     * 执行的方法枚举
     */
    private final ExecuteMethodEnum executeMethod;

    @SuppressWarnings("unchecked")
    public Invocation(Object proxy,Object target, Method method, Object[] args){
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.args = args;
        this.collection = (MongoCollection<Document>) args[args.length-1];
        this.executeMethod = ExecuteMethodEnum.getMethod(method.getName());
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

    /**
     * 获取MongoCollection，MongoCollection一定会是参数的最后一个
     * @return {@link MongoCollection< Document>}
     * @author anwen
     */
    public MongoCollection<Document> getCollection() {
        return collection;
    }

    /**
     * 获取当前执行器执行的方法枚举
     * @return {@link com.anwen.mongo.enums.ExecuteMethodEnum}
     * @author anwen
     */
    public ExecuteMethodEnum getExecuteMethod() {
        return executeMethod;
    }

    /**
     * 执行下一个拦截器
     * @return {@link java.lang.Object}
     * @author anwen
     */
    public Object proceed() throws Throwable {
        return method.invoke(target,args);
    }

}
