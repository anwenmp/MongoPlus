package com.mongoplus.interceptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InterceptorChain {

    /**
     * 拦截器链
     */
    static List<Interceptor> interceptors = new ArrayList<>();

    /**
     * 添加一个拦截器到拦截器链
     * @author anwen
     */
    public static void addInterceptor(Interceptor interceptor){
        interceptors.add(interceptor);
        sorted();
    }

    /**
     * 添加多个拦截器
     * @param interceptorList 拦截器集合
     * @author anwen
     */
    public static void addInterceptors(List<Interceptor> interceptorList){
        interceptors.addAll(interceptorList);
    }

    /**
     * 获取所有拦截器
     * @return {@link java.util.List<com.mongoplus.interceptor.Interceptor>}
     * @author anwen
     */
    public static List<Interceptor> getInterceptors(){
        return interceptors;
    }

    /**
     * 根据断言获取拦截器
     * @param predicate 断言
     * @return {@link com.mongoplus.interceptor.Interceptor}
     * @author anwen
     */
    public static Interceptor getInterceptor(Predicate<? super Interceptor> predicate){
        return interceptors.stream().filter(predicate).findFirst().orElse(null);
    }

    /**
     * 重新排序
     * @author anwen
     */
    public static void sorted() {
        interceptors = interceptors.stream().sorted(Comparator.comparing(Interceptor::order)).collect(Collectors.toList());
    }

}
