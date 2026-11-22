package com.mongoplus.convert.shell;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import static com.mongoplus.convert.shell.ShellCommandSupport.UpdateForm;

/**
 * 根据更新形态、multi 和批次长度转换 update 命令。
 */
final class UpdateShellConverter {

    private static final String[] COMMAND_OPTIONS = {
            "ordered", "bypassDocumentValidation", "writeConcern", "comment", "let", "maxTimeMS"
    };
    private static final String[] ENTRY_OPTIONS = {"upsert", "arrayFilters", "collation", "hint", "sort"};

    private UpdateShellConverter() {
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 更新命令 BSON
     * @return 单个更新、替换或 bulkWrite 语句，无法保留整个批次时为 null
     */
    static String convert(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        BsonValue updates = command.get("updates");
        BsonDocument options = ShellCommandSupport.options(command, COMMAND_OPTIONS, command.getFirstKey(),
                "updates");
        if (collection == null || !ShellCommandSupport.documentArray(updates)
                || updates.asArray().isEmpty() || options == null
                || !ShellCommandSupport.normalizeWriteOptions(options)) {
            return null;
        }
        boolean bulk = updates.asArray().size() > 1 || ShellCommandSupport.unordered(options);
        BsonArray models = new BsonArray();
        for (BsonValue item : updates.asArray()) {
            BsonDocument entry = item.asDocument();
            BsonDocument entryOptions = ShellCommandSupport.options(entry, ENTRY_OPTIONS, "q", "u", "multi");
            if (!entry.isDocument("q") || entryOptions == null || !ShellCommandSupport.booleanOptions(entry, "multi")
                    || !validEntryOptions(entryOptions)) {
                return null;
            }
            boolean multi = entry.getBoolean("multi", BsonBoolean.FALSE).getValue();
            BsonValue update = entry.get("u");
            UpdateForm form = ShellCommandSupport.updateForm(update);
            if (!compatible(form, multi, entryOptions)) {
                return null;
            }
            String method = form == UpdateForm.REPLACEMENT ? "replaceOne" : (multi ? "updateMany" : "updateOne");
            if (!bulk) {
                options.remove("ordered");
                entryOptions.putAll(options);
                return ShellCommandSupport.statement(collection, method, entryOptions, entry.get("q"), update);
            }
            BsonDocument model = new BsonDocument("filter", entry.get("q"))
                    .append(form == UpdateForm.REPLACEMENT ? "replacement" : "update", update);
            model.putAll(entryOptions);
            models.add(new BsonDocument(method, model));
        }
        return ShellCommandSupport.statement(collection, "bulkWrite", options, models);
    }

    /**
     * 检查选项的数据类型。
     */
    private static boolean validEntryOptions(BsonDocument options) {
        return ShellCommandSupport.booleanOptions(options, "upsert")
                && ShellCommandSupport.documentOptions(options, "collation")
                && ShellCommandSupport.validHint(options)
                && ShellCommandSupport.validSort(options.get("sort"))
                && ShellCommandSupport.validArrayFilters(options);
    }

    /**
     * 检查更新形态和选项是否能由相应的 Shell API 共同表达。
     */
    private static boolean compatible(UpdateForm form, boolean multi, BsonDocument options) {
        return form != null
                && (form != UpdateForm.REPLACEMENT || !multi)
                && (form == UpdateForm.OPERATORS || !options.containsKey("arrayFilters"))
                && (!multi || !options.containsKey("sort"));
    }
}
