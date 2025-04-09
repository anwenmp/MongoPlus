package com.mongoplus.parser;

import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.enums.CommandOperate;
import com.mongoplus.model.command.ParseCommand;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抽象的解析器
 * @author anwen
 */
public abstract class AbstractCommandParse implements Parser {

    // 正则表达式：支持 db.user 和 db.getCollection("user")
    static final String pattern = "^([a-zA-Z0-9_]+)\\.(getCollection\\([\"'][a-zA-Z0-9_]+[\"']\\)|[a-zA-Z0-9_]+)\\.(\\w+)\\((.*)\\)$";

    @Override
    public ParseCommand parse(String command) {
        ParseCommand parseCommand = buildParseCommand(command);
        if (parseCommand.getCollection().startsWith("get")){
            parseCommand.setCollection(extractContent(parseCommand.getCollection()));
        }
        CommandOperate commandOperate = CommandOperate.getCommandOperate(parseCommand.getOperate());
        Object actualCommand;
        if (commandOperate == CommandOperate.FIND) {
            actualCommand = parseCommand(parseCommand.getUnresolvedCommand());
        } else {
            actualCommand = parseArrayCommand(parseCommand.getUnresolvedCommand());
        }
        parseCommand.setCommand(actualCommand);
        return parseCommand;
    }

    /**
     * 解析查询语句json
     * @param command 命令
     * @return {@link Bson}
     * @author anwen
     */
    public abstract Bson parseCommand(String command);

    /**
     * 解析管道语句json
     * @param command 命令，一定会是数组
     * @return {@link java.util.List<org.bson.conversions.Bson>}
     * @author anwen
     */
    public abstract List<Bson> parseArrayCommand(String command);

    /**
     * 构建{@link ParseCommand}
     * @param command 命令
     * @return {@link com.mongoplus.model.command.ParseCommand}
     * @author anwen
     */
    public ParseCommand buildParseCommand(String command){
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(command);
        if (matcher.find()) {
            // db 部分
            String db = matcher.group(1);
            // collection 部分
            String collection = matcher.group(2);
            // 操作方法（如：find、aggregate等）
            String operation = matcher.group(3);
            // 查询条件（JSON 格式）
            String queryCondition = matcher.group(4);
            ParseCommand parseCommand = new ParseCommand();
            parseCommand.setOperate(operation);
            parseCommand.setCollection(collection);
            parseCommand.setUnresolvedCommand(queryCondition);
            parseCommand.setOriginal(command);
            return parseCommand;
        }
        throw new MongoPlusConvertException("Unable to parse statement, original -> "+command);
    }

    public static String extractContent(String input) {
        // 使用正则表达式从括号内提取引号中的内容
        String regex = "(?<=['\"]).*?(?=['\"])";
        // 使用正则表达式提取内容
        if (input != null && input.matches(".*['\"].*['\"].*")) {
            // 提取并返回引号内的内容
            return input.replaceAll(".*?['\"](.*?)['\"].*", "$1");
        }
        throw new MongoPlusConvertException("Unable to extract collection name from "+input);
    }

}
