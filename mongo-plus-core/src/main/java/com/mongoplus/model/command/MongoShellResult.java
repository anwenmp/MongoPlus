package com.mongoplus.model.command;

/**
 * 统一保存转换结果、Shell 语句及转换状态
 *
 * @author anwen
 */
public class MongoShellResult {

    private final String database;
    private final String collection;
    private final String commandName;
    private final String shell;

    public MongoShellResult(String database,
                              String collection,
                              String commandName,
                              String shell) {
        this.database = database;
        this.collection = collection;
        this.commandName = commandName;
        this.shell = shell;
    }

    public String getDatabase() {
        return database;
    }

    public String getCollection() {
        return collection;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getShell() {
        return shell;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
