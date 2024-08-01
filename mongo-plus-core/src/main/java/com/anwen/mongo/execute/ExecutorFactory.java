package com.anwen.mongo.execute;

import com.anwen.mongo.context.MongoTransactionContext;
import com.anwen.mongo.execute.instance.DefaultExecute;
import com.anwen.mongo.execute.instance.SessionExecute;
import com.anwen.mongo.proxy.ExecutorProxy;
import com.mongodb.client.ClientSession;

import java.lang.reflect.Proxy;
import java.util.Optional;

/**
 * 执行器工厂
 * @author JiaChaoYang
 * @date 2023-12-28 10:55
 **/
public class ExecutorFactory {

    public ExecutorFactory() {
    }

    public Execute getExecute(){
        ClientSession clientSessionContext = MongoTransactionContext.getClientSessionContext();
        Execute execute = Optional.ofNullable(clientSessionContext)
                .map(clientSession -> (Execute) new SessionExecute(clientSession))
                .orElseGet(DefaultExecute::new);
        Class<? extends Execute> clazz = execute.getClass();
        return (Execute) Proxy.newProxyInstance(clazz.getClassLoader(),clazz.getInterfaces(),new ExecutorProxy(execute));

    }

}
