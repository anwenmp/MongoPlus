package com.mongoplus.enums;

/**
 * 命令方法枚举
 *
 * @author loser
 */
public enum CommandEnum {

    FIND("find"),

    INSERT("insert"),

    DELETE("delete"),

    UPDATE("update"),

    AGGREGATE("aggregate"),

    COUNT("count"),

    CREATE_INDEXES("createIndexes"),

    ;

    private final String command;

    CommandEnum(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
