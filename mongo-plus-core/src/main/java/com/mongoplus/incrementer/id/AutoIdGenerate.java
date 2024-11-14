package com.mongoplus.incrementer.id;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自增id生成
 * @author JiaChaoYang
 * @date 2023-11-24 09:19
 **/
public class AutoIdGenerate {

    private final AtomicInteger counter;

    private volatile static AutoIdGenerate instance;

    /**
     * 双重检查锁定，保证线程安全，基于懒汉
     * @author JiaChaoYang
     * @date 2023/11/24 10:36
    */
    public static AutoIdGenerate getInstance() {
        if (instance == null) {
            synchronized (AutoIdGenerate.class) {
                if (instance == null) {
                    instance = new AutoIdGenerate();
                }
            }
        }
        return instance;
    }

    /**
     * 默认从0开始
     * @author JiaChaoYang
     * @date 2023/11/24 9:22
    */
    private AutoIdGenerate(){
        counter = new AtomicInteger(0);
    }


    /**
     * 获取下一个id
     * @author JiaChaoYang
     * @date 2023/11/24 9:21
    */
    public synchronized int getNextId() {
        return counter.incrementAndGet();
    }

    public synchronized void addAndGet(int delta){
        counter.addAndGet(delta);
    }

}
