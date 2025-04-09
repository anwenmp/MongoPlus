package com.mongoplus.execute;

import com.mongodb.client.ClientSession;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.execute.instance.DefaultExecute;
import com.mongoplus.execute.instance.SessionExecute;
import com.mongoplus.interceptor.AdvancedInterceptorChain;
import com.mongoplus.proxy.ExecutorProxy;

import java.util.Optional;

/**
 * 执行器工厂
 * @author JiaChaoYang
 **/
public class ExecutorFactory {

    public ExecutorFactory() {
    }

    /**
     * 获取被代理后的执行器
     * @return {@link Execute}
     * @author anwen
     */
    public Execute getExecute(){
        Execute execute = getOriginalExecute();
        Class<? extends Execute> clazz = execute.getClass();
        // 包装一层高级代理对象，高级拦截器替代替换器，和普通拦截器形成拦截器责任链
        // 保证高级拦截器在普通拦截器之后执行，可以将普通拦截器认为成一种过滤器
        execute = AdvancedInterceptorChain.wrap(execute);
        // 普通代理类
        return ExecutorProxy.wrap(execute);
    }

    /**
     * 获取一个原始的不被代理的执行器
     * @return {@link Execute}
     * @author anwen
     */
    public Execute getOriginalExecute() {
        return Optional.ofNullable(MongoTransactionContext.getClientSessionContext())
                .map(this::getSessionExecute)
                .orElseGet(this::getDefaultExecute);
    }

    /**
     * 获取一个普通的执行器
     * @return {@link com.mongoplus.execute.Execute}
     * @author anwen
     */
    public Execute getDefaultExecute() {
        return new DefaultExecute();
    }

    /**
     * 获取一个事务执行器
     * @return {@link com.mongoplus.execute.Execute}
     * @author anwen
     */
    public Execute getSessionExecute(){
        return getSessionExecute(MongoTransactionContext.getClientSessionContext());
    }

    /**
     * 获取一个事务执行器
     * @param clientSession clientSession
     * @return {@link com.mongoplus.execute.Execute}
     * @author anwen
     */
    public Execute getSessionExecute(ClientSession clientSession){
        return new SessionExecute(clientSession);
    }

}
