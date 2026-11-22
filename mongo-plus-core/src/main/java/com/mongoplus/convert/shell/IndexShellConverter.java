package com.mongoplus.convert.shell;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.util.Map;
import java.util.StringJoiner;

/**
 * 转换普通索引的创建、列举和删除命令。
 */
final class IndexShellConverter {

    private static final String[] CREATE_OPTIONS = {"commitQuorum", "writeConcern", "comment", "maxTimeMS"};
    private static final String[] INDEX_OPTIONS = {
            "name", "background", "unique", "sparse", "expireAfterSeconds", "v", "weights", "default_language",
            "language_override", "textIndexVersion", "2dsphereIndexVersion", "bits", "min", "max",
            "storageEngine", "partialFilterExpression", "collation", "wildcardProjection", "hidden"
    };
    private static final String[] NO_OPTIONS = {};

    private IndexShellConverter() {
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 创建索引命令 BSON
     * @return 单条索引创建语句，多个索引需要独立选项时为 null
     */
    static String create(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        BsonValue indexes = command.get("indexes");
        BsonDocument options = ShellCommandSupport.options(command, CREATE_OPTIONS, command.getFirstKey(),
                "indexes");
        if (collection == null || !ShellCommandSupport.documentArray(indexes)
                || indexes.asArray().isEmpty() || options == null || !validCreateOptions(options)) {
            return null;
        }

        boolean multiple = indexes.asArray().size() > 1;
        BsonArray keys = new BsonArray();
        BsonDocument sharedOptions = null;
        String sharedExpression = null;
        for (BsonValue item : indexes.asArray()) {
            BsonDocument index = item.asDocument();
            BsonDocument indexOptions = ShellCommandSupport.options(index, INDEX_OPTIONS, "key");
            if (!index.isDocument("key") || !index.isString("name") || indexOptions == null) {
                return null;
            }
            BsonDocument key = index.getDocument("key");
            if (multiple) {
                if (!hasDefaultName(key, index.getString("name").getValue())) {
                    return null;
                }
                indexOptions.remove("name");
            }
            BsonValue version = indexOptions.remove("v");
            if (version != null) {
                indexOptions.put("version", version);
            }
            String expression = BsonShellFormatter.format(indexOptions);
            if (sharedOptions == null) {
                sharedOptions = indexOptions;
                sharedExpression = expression;
            } else if (!sharedExpression.equals(expression)) {
                return null;
            }
            keys.add(key);
        }

        sharedOptions.putAll(options);
        return multiple
                ? ShellCommandSupport.statement(collection, "createIndexes", sharedOptions, keys)
                : ShellCommandSupport.statement(collection, "createIndex", sharedOptions, keys.get(0));
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 列举索引命令 BSON
     * @return getIndexes 语句，命令指定了该 API 不接收的选项时为 null
     */
    static String list(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        if (collection == null || !command.isDocument("cursor")
                || !command.getDocument("cursor").isEmpty()
                || ShellCommandSupport.options(command, NO_OPTIONS, command.getFirstKey(), "cursor") == null) {
            return null;
        }
        return ShellCommandSupport.call(collection, "getIndexes") + ";";
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 删除索引命令 BSON
     * @return dropIndex 或 dropIndexes 语句，存在额外命令选项时为 null
     */
    static String drop(String databaseExpression, BsonDocument command) {
        String collection = ShellCommandSupport.collectionExpression(databaseExpression, command);
        BsonValue index = command.get("index");
        if (collection == null || index == null
                || ShellCommandSupport.options(command, NO_OPTIONS, command.getFirstKey(), "index") == null) {
            return null;
        }
        if (index.isString() && "*".equals(index.asString().getValue())) {
            return ShellCommandSupport.call(collection, "dropIndexes") + ";";
        }
        if (index.isString() || index.isDocument()) {
            return ShellCommandSupport.call(collection, "dropIndex", index) + ";";
        }
        return null;
    }

    /**
     * 检查索引创建命令的共享选项。
     */
    private static boolean validCreateOptions(BsonDocument options) {
        return ShellCommandSupport.normalizeWriteOptions(options)
                && (options.isString("commitQuorum")
                        || ShellCommandSupport.normalizeInteger(options, "commitQuorum", 0));
    }

    /**
     * 对简单索引键核对 mongosh 的默认名称。多个索引无法分别传入 name。
     */
    private static boolean hasDefaultName(BsonDocument key, String name) {
        StringJoiner generated = new StringJoiner("_");
        for (Map.Entry<String, BsonValue> field : key.entrySet()) {
            BsonValue direction = field.getValue();
            generated.add(field.getKey());
            if (direction.isInt32()) {
                generated.add(Integer.toString(direction.asInt32().getValue()));
            } else if (direction.isString()) {
                generated.add(direction.asString().getValue());
            } else {
                return false;
            }
        }
        return name.equals(generated.toString());
    }
}
