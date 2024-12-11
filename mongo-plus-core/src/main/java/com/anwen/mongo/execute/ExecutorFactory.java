package com.anwen.mongo.execute;

import com.anwen.mongo.context.MongoTransactionContext;
import com.anwen.mongo.execute.instance.DefaultExecute;
import com.anwen.mongo.execute.instance.SessionExecute;
import com.anwen.mongo.interceptor.AdvancedInterceptorChain;
import com.anwen.mongo.proxy.ExecutorProxy;
import com.mongodb.client.ClientSession;

import java.util.Optional;

/**
 * 执行器工厂
 * @author JiaChaoYang
 * @date 2023-12-28 10:55
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
        ClientSession clientSessionContext = MongoTransactionContext.getClientSessionContext();
        Execute execute = getOriginalExecute();
        Class<? extends Execute> clazz = execute.getClass();
        // 包装一层高级代理对象，高级拦截器替代替换器，形成拦截器责任链
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
                .map(clientSession -> (Execute) new SessionExecute(clientSession))
                .orElseGet(DefaultExecute::new);
    }

}
