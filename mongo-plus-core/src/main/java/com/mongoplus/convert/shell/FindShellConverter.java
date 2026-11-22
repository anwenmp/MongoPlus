package com.mongoplus.convert.shell;

import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonValue;

/**
 * 将查询命令转换为 find 及游标选项。
 */
final class FindShellConverter {

    private static final String[] CURSOR_METHODS = {"sort", "skip", "limit", "batchSize"};
    private static final String[] OPTIONS = {
            "sort", "skip", "limit", "batchSize", "singleBatch", "hint", "collation", "maxTimeMS",
            "comment", "min", "max", "returnKey", "showRecordId", "tailable", "awaitData", "oplogReplay",
            "noCursorTimeout", "allowPartialResults", "allowDiskUse", "let", "readConcern", "$readPreference"
    };

    private FindShellConverter() {
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 查询命令 BSON
     * @return find 语句，游标选项无法安全映射时为 null
     */
    static String convert(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        BsonDocument options = ShellCommandSupport.options(command, OPTIONS,
                command.getFirstKey(), "filter", "projection");
        if (collection == null || options == null || !validOptions(options)) {
            return null;
        }
        if (!normalizeBatchOptions(options)) {
            return null;
        }
        BsonDocument filter = command.getDocument("filter", new BsonDocument());
        BsonDocument projection = command.getDocument("projection", new BsonDocument());
        BsonDocument cursorMethods = new BsonDocument();
        for (String method : CURSOR_METHODS) {
            BsonValue value = options.remove(method);
            if (value != null) {
                cursorMethods.put(method, value);
            }
        }

        String find;
        if (!options.isEmpty()) {
            find = ShellCommandSupport.call(collection, "find", filter, projection, options);
        } else if (command.containsKey("projection")) {
            find = ShellCommandSupport.call(collection, "find", filter, projection);
        } else {
            find = ShellCommandSupport.call(collection, "find", filter);
        }
        StringBuilder statement = new StringBuilder(find);
        cursorMethods.forEach((method, value) -> statement.append('.').append(method)
                .append('(').append(BsonShellFormatter.format(value)).append(')'));
        return statement.append(';').toString();
    }

    /**
     * 检查已知选项，避免 Shell 默默忽略类型不匹配的字段。
     */
    private static boolean validOptions(BsonDocument options) {
        return ShellCommandSupport.validSort(options.get("sort"))
                && ShellCommandSupport.validHint(options)
                && ShellCommandSupport.documentOptions(options, "collation", "min", "max", "let")
                && ShellCommandSupport.booleanOptions(options, "singleBatch", "returnKey", "showRecordId",
                        "tailable", "awaitData", "oplogReplay", "noCursorTimeout",
                        "allowPartialResults", "allowDiskUse")
                && ShellCommandSupport.normalizeInteger(options, "skip", 0)
                && ShellCommandSupport.normalizeInteger(options, "limit", 0)
                && ShellCommandSupport.normalizeInteger(options, "batchSize", 0)
                && ShellCommandSupport.normalizeInteger(options, ShellCommandSupport.MAX_TIME_MS, 0)
                && ShellCommandSupport.normalizeReadOptions(options);
    }

    /**
     * 负数 limit 会在 Shell 中生成 singleBatch。相等的正向 limit 和 batchSize 会被 Node Driver 改写。
     */
    private static boolean normalizeBatchOptions(BsonDocument options) {
        BsonValue limit = options.get("limit");
        BsonValue batchSize = options.get("batchSize");
        if (limit != null && batchSize == null && limit.asInt32().getValue() > 0
                && options.getBoolean("singleBatch", BsonBoolean.FALSE).getValue()) {
            options.put("limit", new BsonInt32(-limit.asInt32().getValue()));
            options.remove("singleBatch");
        }
        return limit == null || !limit.equals(batchSize);
    }
}
