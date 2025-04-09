package com.mongoplus.model.command;

import com.mongodb.event.CommandFailedEvent;

/**
 * 命令失败信息
 * @author JiaChaoYang
 **/
public class CommandFailed extends BaseCommand {

    /**
     * 命令失败的异常
    */
    private Throwable throwable;

    /**
     * MongoDB提供，比较全的失败信息
    */
    private CommandFailedEvent commandFailedEvent;

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public CommandFailedEvent getCommandFailedEvent() {
        return commandFailedEvent;
    }

    public void setCommandFailedEvent(CommandFailedEvent commandFailedEvent) {
        this.commandFailedEvent = commandFailedEvent;
    }

    public CommandFailed(String commandName, Throwable throwable, CommandFailedEvent commandFailedEvent) {
        super(commandName);
        this.throwable = throwable;
        this.commandFailedEvent = commandFailedEvent;
    }
}
