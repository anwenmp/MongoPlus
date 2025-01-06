package com.mongoplus.interceptor;

import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.execute.ExecutorFactory;
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

    /**
     * 执行器工厂
     */
    private final ExecutorFactory executorFactory;

    @SuppressWarnings("unchecked")
    public Invocation(Object proxy,Object target, Method method, Object[] args){
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.args = args;
        this.collection = (MongoCollection<Document>) args[args.length-1];
        this.executeMethod = ExecuteMethodEnum.getMethod(method.getName());
        this.executorFactory = new ExecutorFactory();
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
     * 获取执行器工厂
     * @return {@link com.mongoplus.execute.ExecutorFactory}
     * @author anwen
     */
    public ExecutorFactory getExecutorFactory() {
        return executorFactory;
    }

    /**
     * 获取MongoCollection，MongoCollection一定会是参数的最后一个
     * @return {@link MongoCollection<Document>}
     * @author anwen
     */
    public MongoCollection<Document> getCollection() {
        return collection;
    }

    /**
     * 获取当前执行器执行的方法枚举
     * @return {@link com.mongoplus.enums.ExecuteMethodEnum}
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

    /**
     * 终止拦截器责任链，从当前拦截器直接执行目标方法
     * @return {@link java.lang.Object}
     * @author anwen
     */
    public Object discontinue() throws Throwable {
        return method.invoke(getExecutorFactory().getOriginalExecute(),args);
    }

}
