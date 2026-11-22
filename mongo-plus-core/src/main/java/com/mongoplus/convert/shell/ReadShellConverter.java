package com.mongoplus.convert.shell;

import org.bson.BsonDocument;

/**
 * 转换不依赖多批次游标的去重和估算计数命令。
 */
final class ReadShellConverter {

    private static final String[] DISTINCT_OPTIONS = {"collation", "comment", "maxTimeMS", "readConcern",
            "$readPreference"};
    private static final String[] COUNT_OPTIONS = {"comment", "maxTimeMS", "readConcern", "$readPreference"};

    private ReadShellConverter() {
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 去重命令 BSON
     * @return distinct 语句，选项无法可靠映射时为 null
     */
    static String distinct(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        BsonDocument options = ShellCommandSupport.options(command, DISTINCT_OPTIONS,
                command.getFirstKey(), "key", "query");
        if (collection == null || !command.isString("key") || options == null
                || !ShellCommandSupport.documentOptions(options, "collation") || !validOptions(options)) {
            return null;
        }
        return ShellCommandSupport.statement(collection,
                "distinct", options, command.get("key"), command.getDocument("query", new BsonDocument()));
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command count 命令 BSON
     * @return 估算计数语句，带匹配或分页条件的旧 count 命令返回 null
     */
    static String count(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        BsonDocument options = ShellCommandSupport.options(command, COUNT_OPTIONS,
                command.getFirstKey());
        if (collection == null || options == null || !validOptions(options)) {
            return null;
        }
        return ShellCommandSupport.statement(collection,
                "estimatedDocumentCount", options);
    }

    /**
     * 校验读关注、读偏好和时限。
     */
    private static boolean validOptions(BsonDocument options) {
        return ShellCommandSupport.normalizeInteger(options, ShellCommandSupport.MAX_TIME_MS, 0)
                && ShellCommandSupport.normalizeReadOptions(options);
    }
}
