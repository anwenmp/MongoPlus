package com.mongoplus.listener;

import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;

/**
 * 监听器
 * @author JiaChaoYang
 **/
public interface Listener {

    /**
     * 最高优先级
     * @see java.lang.Integer#MIN_VALUE
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * 最低优先级
     * @see java.lang.Integer#MAX_VALUE
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * 处理命令开始信息
     * @param commandStarted 命令执行开始信息对象
     * @author JiaChaoYang
    */
    void commandStarted(CommandStarted commandStarted);

    /**
     * 处理命令成功信息
     * @param commandSucceeded 命令成功信息对象
     * @author JiaChaoYang
    */
    void commandSucceeded(CommandSucceeded commandSucceeded);

    /**
     * 处理命令失败信息
     * @param commandFailed 处理命令失败信息对象
     * @author JiaChaoYang
    */
    void commandFailed(CommandFailed commandFailed);

    /**
     * 指定拦截器排序
     * @return int
     * @author JiaChaoYang
    */
    int getOrder();

}
