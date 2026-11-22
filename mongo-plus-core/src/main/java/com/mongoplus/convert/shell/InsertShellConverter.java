package com.mongoplus.convert.shell;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonValue;

/**
 * 将单个 insert 命令中的文档作为一个批次输出。
 */
final class InsertShellConverter {

    private static final String[] OPTIONS = {
            "ordered", "bypassDocumentValidation", "writeConcern", "comment", "maxTimeMS"
    };

    private InsertShellConverter() {
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 插入命令 BSON
     * @return insertOne 或 insertMany 语句，负载或选项无法映射时为 null
     */
    static String convert(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        BsonValue documents = command.get("documents");
        BsonDocument options = ShellCommandSupport.options(command, OPTIONS, command.getFirstKey(), "documents");
        if (collection == null || !ShellCommandSupport.documentArray(documents)
                || options == null || !ShellCommandSupport.normalizeWriteOptions(options)) {
            return null;
        }
        BsonArray batch = documents.asArray();
        if (batch.isEmpty()) {
            return null;
        }
        for (BsonValue item : batch) {
            BsonValue id = item.asDocument().get("_id");
            if (id == null || id.isNull()) {
                // 保留实发文档中缺省或显式 null 的 _id，避免 Shell 客户端补写。
                options.put("forceServerObjectId", BsonBoolean.TRUE);
                break;
            }
        }
        if (batch.size() == 1 && !ShellCommandSupport.unordered(options)) {
            options.remove("ordered");
            return ShellCommandSupport.statement(collection, "insertOne", options, batch.get(0));
        }
        return ShellCommandSupport.statement(collection, "insertMany", options, batch);
    }
}
