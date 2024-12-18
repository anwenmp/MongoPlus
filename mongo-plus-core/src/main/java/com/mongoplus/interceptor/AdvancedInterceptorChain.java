package com.mongoplus.interceptor;

import com.mongoplus.execute.Execute;
import com.mongoplus.proxy.AdvancedProxy;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * 高级拦截器的责任链存储
 * @author anwen
 */
public class AdvancedInterceptorChain {

    /**
     * 拦截器链
     */
    static final List<AdvancedInterceptor> interceptors = new ArrayList<>();

    /**
     * 包装拦截器
     * @param target 执行器实例
     * @return {@link java.lang.Object}
     * @author anwen
     */
    public static Execute wrap(Execute target) {
        for (AdvancedInterceptor interceptor : interceptors) {
            Class<?> clazz = target.getClass();
            target = (Execute) Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    clazz.getInterfaces(),
                    new AdvancedProxy(target, interceptor)
            );
        }
        return target;
    }

    /**
     * 添加一个拦截器到拦截器链
     * @author anwen
     */
    public static void addInterceptor(AdvancedInterceptor interceptor){
        interceptors.add(interceptor);
        sorted();
    }

    /**
     * 添加多个拦截器
     * @param interceptorList 拦截器集合
     * @author anwen
     */
    public static void addInterceptors(List<AdvancedInterceptor> interceptorList){
        interceptors.addAll(interceptorList);
        sorted();
    }

    /**
     * 获取所有拦截器
     * @return {@link java.util.List<com.mongoplus.interceptor.AdvancedInterceptor>}
     * @author anwen
     */
    public static List<AdvancedInterceptor> getInterceptors(){
        return interceptors;
    }

    /**
     * 根据断言获取拦截器
     * @param predicate 断言
     * @return {@link com.mongoplus.interceptor.Interceptor}
     * @author anwen
     */
    public static AdvancedInterceptor getInterceptor(Predicate<? super AdvancedInterceptor> predicate){
        return interceptors.stream().filter(predicate).findFirst().orElse(null);
    }

    /**
     * 重新排序
     * @author anwen
     */
    public static void sorted() {
        interceptors.sort(Comparator.comparingInt(AdvancedInterceptor::order).reversed());
    }

}
