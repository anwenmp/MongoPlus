package com.anwen.mongo.model.command;

/**
 * 基础命令
 * @author JiaChaoYang
 * @date 2023-11-22 14:17
 **/
public class BaseCommand {

    /**
     * 命令名称
     * @date 2023/11/22 14:21
    */
    private String commandName;

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public BaseCommand(String commandName) {
        this.commandName = commandName;
    }

    public BaseCommand() {
    }
}
