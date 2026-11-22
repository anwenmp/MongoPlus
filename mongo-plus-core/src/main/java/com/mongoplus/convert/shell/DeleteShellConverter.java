package com.mongoplus.convert.shell;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;

/**
 * 保留 delete 命令中每一项的删除数量限制、选项和批次顺序。
 */
final class DeleteShellConverter {

    private static final String[] COMMAND_OPTIONS = {"ordered", "writeConcern", "comment", "let", "maxTimeMS"};
    private static final String[] ENTRY_OPTIONS = {"collation", "hint"};
    private static final int DELETE_MANY = 0;
    private static final int DELETE_ONE = 1;

    private DeleteShellConverter() {
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 删除命令 BSON
     * @return 单个删除或 bulkWrite 语句，无法保留整个批次时为 null
     */
    static String convert(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        BsonValue deletes = command.get("deletes");
        BsonDocument options = ShellCommandSupport.options(command, COMMAND_OPTIONS, command.getFirstKey(),
                "deletes");
        if (collection == null || !ShellCommandSupport.documentArray(deletes)
                || deletes.asArray().isEmpty() || options == null
                || !ShellCommandSupport.normalizeWriteOptions(options)) {
            return null;
        }
        boolean bulk = deletes.asArray().size() > 1 || ShellCommandSupport.unordered(options);
        BsonArray models = new BsonArray();
        for (BsonValue item : deletes.asArray()) {
            BsonDocument entry = item.asDocument();
            BsonDocument entryOptions = ShellCommandSupport.options(entry, ENTRY_OPTIONS, "q", "limit");
            if (!entry.isDocument("q") || (!entry.isInt32("limit") && !entry.isInt64("limit")) || entryOptions == null
                    || !ShellCommandSupport.documentOptions(entryOptions, "collation")
                    || !ShellCommandSupport.validHint(entryOptions)) {
                return null;
            }
            long limit = entry.getNumber("limit").longValue();
            if (limit != DELETE_MANY && limit != DELETE_ONE) {
                return null;
            }
            String method = limit == DELETE_ONE ? "deleteOne" : "deleteMany";
            if (!bulk) {
                options.remove("ordered");
                entryOptions.putAll(options);
                return ShellCommandSupport.statement(collection, method, entryOptions, entry.get("q"));
            }
            BsonDocument model = new BsonDocument("filter", entry.get("q"));
            model.putAll(entryOptions);
            models.add(new BsonDocument(method, model));
        }
        return ShellCommandSupport.statement(collection, "bulkWrite", options, models);
    }
}
