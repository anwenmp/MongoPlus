package com.mongoplus.listener.business;

import com.mongoplus.cache.global.OrderCache;
import com.mongoplus.listener.Listener;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;
import org.bson.BsonValue;

/**
 * 防止全表更新和删除的拦截器
 *
 * @author JiaChaoYang
 **/
public class BlockAttackInnerListener implements Listener {

    Log log = LogFactory.getLog(BlockAttackInnerListener.class);

    @Override
    public void commandStarted(CommandStarted commandStarted) {
        if ("update".equals(commandStarted.getCommandName()) || "delete".equals(commandStarted.getCommandName())) {
            BsonValue filter = commandStarted.getCommandDocument().get(commandStarted.getCommandName() + "s").asArray().get(0).asDocument().get("q");
            if (filter == null || filter.asDocument().isEmpty()) {
                log.error("Prohibition of collection {} operation",commandStarted.getCommandName());
                throw new IllegalArgumentException("Prohibition of collection " + commandStarted.getCommandName() +" operation");
            }
        }
    }

    @Override
    public void commandSucceeded(CommandSucceeded commandSucceeded) {
        //不做任何操作
    }

    @Override
    public void commandFailed(CommandFailed commandFailed) {
        //不做任何操作
    }

    @Override
    public int getOrder() {
        return OrderCache.BLOCK_ATTACK_INNER_ORDER;
    }
}
