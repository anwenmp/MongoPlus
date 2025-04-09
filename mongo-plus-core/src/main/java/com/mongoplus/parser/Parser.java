package com.mongoplus.parser;

import com.mongoplus.model.command.ParseCommand;

/**
 * 命令解析器接口，只做查询解析
 * @author anwen
 */
public interface Parser {

    /**
     * 解析命令，只支持find和aggregate，并需要保证find或aggregate中的内容为json
     * @param command 命令
     * @return {@link ParseCommand}
     * @author anwen
     */
    ParseCommand parse(String command);

}
