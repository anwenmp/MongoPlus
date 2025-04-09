package com.mongoplus.model.command;

/**
 * 基础命令
 * @author JiaChaoYang
 **/
public class BaseCommand {

    /**
     * 命令名称
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
