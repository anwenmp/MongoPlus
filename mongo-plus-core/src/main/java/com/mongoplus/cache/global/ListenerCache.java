package com.mongoplus.cache.global;

import com.mongoplus.listener.Listener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 监听器
 * @author JiaChaoYang
 **/
public class ListenerCache {

    public static List<Listener> listeners = new ArrayList<>();

    public static void sorted() {
        listeners = listeners.stream().sorted(Comparator.comparing(Listener::getOrder)).collect(Collectors.toList());
    }

}
