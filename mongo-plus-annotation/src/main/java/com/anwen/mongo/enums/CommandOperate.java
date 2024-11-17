package com.anwen.mongo.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * 命令操作枚举
 * @author anwen
 */
public enum CommandOperate {

    FIND("find"),

    AGGREGATE("aggregate")

    ;

    CommandOperate(String operate){
        this.operate = operate;
    }

    private final String operate;

    public String getOperate(){
        return this.operate;
    }

    public static CommandOperate getCommandOperate(String operate){
        return Arrays.stream(values())
                .filter(commandOperate -> Objects.equals(commandOperate.getOperate(), operate))
                .findAny()
                .orElse(null);
    }

}
