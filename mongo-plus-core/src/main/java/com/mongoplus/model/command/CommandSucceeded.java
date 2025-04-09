package com.mongoplus.model.command;

import com.mongodb.event.CommandSucceededEvent;
import org.bson.BsonDocument;

/**
 * 命令成功信息
 * @author JiaChaoYang
 **/
public class CommandSucceeded extends BaseCommand{

    /**
     * 执行的结果
    */
    private BsonDocument response;

    /**
     * MongoDB提供，比较全的成功结果
    */
    private CommandSucceededEvent commandSucceededEvent;

    public BsonDocument getResponse() {
        return response;
    }

    public void setResponse(BsonDocument response) {
        this.response = response;
    }

    public CommandSucceededEvent getCommandSucceededEvent() {
        return commandSucceededEvent;
    }

    public void setCommandSucceededEvent(CommandSucceededEvent commandSucceededEvent) {
        this.commandSucceededEvent = commandSucceededEvent;
    }

    public CommandSucceeded(String commandName, BsonDocument response, CommandSucceededEvent commandSucceededEvent) {
        super(commandName);
        this.response = response;
        this.commandSucceededEvent = commandSucceededEvent;
    }

    public CommandSucceeded(){
    }
}
