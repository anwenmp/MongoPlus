package com.mongoplus.convert.shell;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonValue;

/**
 * 按原样保留聚合管道，并映射 aggregate 及游标选项。
 */
final class AggregateShellConverter {

    private static final String[] OPTIONS = {
            "allowDiskUse", "collation", "hint", "comment", "let", "maxTimeMS",
            "readConcern", "writeConcern", "$readPreference", "bypassDocumentValidation"
    };

    private AggregateShellConverter() {
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 聚合命令 BSON
     * @return 集合级或数据库级 aggregate 语句，无法安全传递选项时为 null
     */
    static String convert(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        BsonDocument options = ShellCommandSupport.options(command, OPTIONS,
                command.getFirstKey(), "pipeline", "cursor");
        BsonValue pipeline = command.get("pipeline");
        if (options == null || !ShellCommandSupport.documentArray(pipeline) || !validOptions(options)) {
            return null;
        }
        boolean writes = writesToCollection(pipeline.asArray());
        if (!mapCursor(command, options, writes)
                || (writes && options.containsKey("readPreference"))
                || options.containsKey(ShellCommandSupport.WRITE_CONCERN)) {
            return null;
        }
        if (collection != null) {
            return ShellCommandSupport.statement(collection, "aggregate", options,
                    pipeline);
        }
        if (new BsonInt32(1).equals(command.get(command.getFirstKey()))) {
            return ShellCommandSupport.statement(databaseExpression, "aggregate", options, pipeline);
        }
        return null;
    }

    /**
     * 将首批大小映射为 aggregate 选项。Node Driver 会忽略 size=0 和写出管道的 batchSize。
     */
    private static boolean mapCursor(BsonDocument command, BsonDocument options, boolean writes) {
        if (!command.isDocument("cursor")) {
            return false;
        }
        BsonDocument cursor = command.getDocument("cursor");
        if (cursor.isEmpty()) {
            return true;
        }
        if (writes || cursor.size() != 1 || !cursor.containsKey("batchSize")) {
            return false;
        }
        options.put("batchSize", cursor.get("batchSize"));
        return ShellCommandSupport.normalizeInteger(options, "batchSize", 1);
    }

    /**
     * @return 管道以写入集合的阶段结尾时返回 true
     */
    private static boolean writesToCollection(BsonArray pipeline) {
        if (pipeline.isEmpty()) {
            return false;
        }
        BsonDocument lastStage = pipeline.get(pipeline.size() - 1).asDocument();
        return lastStage.containsKey("$out") || lastStage.containsKey("$merge");
    }

    /**
     * 检查 Shell 聚合选项可传递的 BSON 类型。
     */
    private static boolean validOptions(BsonDocument options) {
        return ShellCommandSupport.validHint(options)
                && ShellCommandSupport.documentOptions(options, "collation", "let")
                && ShellCommandSupport.booleanOptions(options, "allowDiskUse", "bypassDocumentValidation")
                && ShellCommandSupport.normalizeInteger(options, ShellCommandSupport.MAX_TIME_MS, 0)
                && ShellCommandSupport.normalizeReadOptions(options);
    }
}
