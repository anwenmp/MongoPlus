package com.mongoplus.convert.shell;

import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import static com.mongoplus.convert.shell.ShellCommandSupport.UpdateForm;

/**
 * 转换原子查找并删除、更新或替换命令。
 */
final class FindAndModifyShellConverter {

    private static final String[] OPTIONS = {
            "sort", "fields", "new", "upsert", "arrayFilters", "bypassDocumentValidation",
            "writeConcern", "collation", "hint", "comment", "let", "maxTimeMS"
    };

    private FindAndModifyShellConverter() {
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command findAndModify 命令 BSON
     * @return 对应的 findOneAnd 方法语句，选项不兼容时为 null
     */
    static String convert(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        BsonDocument options = ShellCommandSupport.options(command, OPTIONS, command.getFirstKey(),
                "query", "remove", "update");
        if (collection == null || options == null || !validOptions(options)
                || !ShellCommandSupport.booleanOptions(command, "remove")) {
            return null;
        }
        BsonDocument filter = command.getDocument("query", new BsonDocument());
        BsonValue projection = options.remove("fields");
        if (projection != null) {
            options.put("projection", projection);
        }
        BsonValue update = command.get("update");
        if (command.getBoolean("remove", BsonBoolean.FALSE).getValue()) {
            if (update != null || options.containsKey("new") || options.containsKey("upsert")
                    || options.containsKey("arrayFilters") || options.containsKey("bypassDocumentValidation")) {
                return null;
            }
            return ShellCommandSupport.statement(collection, "findOneAndDelete", options, filter);
        }
        UpdateForm form = ShellCommandSupport.updateForm(update);
        if (form == null || (form != UpdateForm.OPERATORS && options.containsKey("arrayFilters"))) {
            return null;
        }
        BsonValue returnAfter = options.remove("new");
        if (returnAfter != null) {
            options.put("returnNewDocument", returnAfter);
        }
        String method = form == UpdateForm.REPLACEMENT ? "findOneAndReplace" : "findOneAndUpdate";
        return ShellCommandSupport.statement(collection, method, options, filter, update);
    }

    /**
     * 检查 Shell 查改方法能处理的选项形态。
     */
    private static boolean validOptions(BsonDocument options) {
        return ShellCommandSupport.booleanOptions(options, "new", "upsert")
                && ShellCommandSupport.documentOptions(options, "fields", "collation")
                && ShellCommandSupport.validHint(options)
                && ShellCommandSupport.validSort(options.get("sort"))
                && ShellCommandSupport.validArrayFilters(options)
                && ShellCommandSupport.normalizeWriteOptions(options);
    }
}
