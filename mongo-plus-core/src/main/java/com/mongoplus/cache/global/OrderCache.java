package com.mongoplus.cache.global;

/**
 * 拦截器排序媒介
 * @author JiaChaoYang
 **/
public class OrderCache {
    
    /**
     * 日志拦截器order
    */
    public static int LOG_ORDER = 0;
    
    /**
     * 防止全集合更新删除拦截器的order
    */
    public static int BLOCK_ATTACK_INNER_ORDER = 1;
    
}
