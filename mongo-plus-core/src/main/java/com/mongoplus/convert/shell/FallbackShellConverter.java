package com.mongoplus.convert.shell;

import org.bson.BsonDocument;
import org.bson.BsonValue;

/**
 * 无可靠 Shell helper 时，以单条 runCommand 保留当前命令和批次。
 */
final class FallbackShellConverter {

    private FallbackShellConverter() {
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 已移除 Driver 传输元数据的命令
     * @return runCommand 语句，无法保留路由时为空字符串
     */
    static String convert(String databaseExpression, BsonDocument command) {
        BsonDocument body = command;
        BsonValue wirePreference = command.get(ShellCommandSupport.READ_PREFERENCE);
        BsonDocument options = new BsonDocument();
        if (wirePreference != null) {
            BsonValue preference = ShellCommandSupport.readPreference(wirePreference);
            if (preference == null) {
                return "";
            }
            body = new BsonDocument();
            body.putAll(command);
            body.remove(ShellCommandSupport.READ_PREFERENCE);
            options.put("readPreference", preference);
        }
        return ShellCommandSupport.statement(databaseExpression, "runCommand", options, body);
    }
}
