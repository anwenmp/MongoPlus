package com.mongoplus.listener;

import com.mongoplus.cache.global.ListenerCache;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;

/**
 * MongoPlus监听器
 * @author JiaChaoYang
 **/
public class MongoPlusListener implements Listener {

    @Override
    public void commandStarted(CommandStarted commandStarted) {
        ListenerCache.listeners.forEach(interceptor -> interceptor.commandStarted(commandStarted));
    }

    @Override
    public void commandSucceeded(CommandSucceeded commandSucceeded) {
        ListenerCache.listeners.forEach(interceptor -> interceptor.commandSucceeded(commandSucceeded));
    }

    @Override
    public void commandFailed(CommandFailed commandFailed) {
        ListenerCache.listeners.forEach(interceptor -> interceptor.commandFailed(commandFailed));
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
