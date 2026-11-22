package com.mongoplus.convert.shell;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 提供参数选择、选项校验和 Shell 方法调用的公共操作。
 */
final class ShellCommandSupport {

    static final String READ_PREFERENCE = "$readPreference";
    static final String MAX_TIME_MS = "maxTimeMS";
    static final String WRITE_CONCERN = "writeConcern";
    static final String READ_CONCERN = "readConcern";

    private ShellCommandSupport() {
    }

    /**
     * @param databaseExpression 目标数据库表达式
     * @param command 已校验首字段的命令文档
     * @return 集合表达式，命令首参数不是集合名时返回 null
     */
    static String collectionExpression(String databaseExpression, BsonDocument command) {
        BsonValue target = command.get(command.getFirstKey());
        return target.isString() ? databaseExpression + ".getCollection("
                + BsonShellFormatter.quote(target.asString().getValue()) + ")" : null;
    }

    /**
     * 提取受支持的选项。遇到未知字段时拒绝高层转换，避免丢失选项。
     *
     * @return 选项的新文档，存在未知字段时为 null
     */
    static BsonDocument options(BsonDocument source, String[] supported, String... arguments) {
        BsonDocument options = new BsonDocument();
        List<String> argumentNames = Arrays.asList(arguments);
        List<String> optionNames = Arrays.asList(supported);
        for (Map.Entry<String, BsonValue> field : source.entrySet()) {
            String name = field.getKey();
            if (argumentNames.contains(name)) {
                continue;
            }
            if (!optionNames.contains(name)) {
                return null;
            }
            options.put(name, field.getValue());
        }
        return options;
    }

    /**
     * @return 指定 BSON 值为由文档组成的数组时返回 true，允许空数组
     */
    static boolean documentArray(BsonValue value) {
        if (value == null || !value.isArray()) {
            return false;
        }
        for (BsonValue item : value.asArray()) {
            if (!item.isDocument()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return arrayFilters 缺省或为文档数组时返回 true
     */
    static boolean validArrayFilters(BsonDocument options) {
        BsonValue filters = options.get("arrayFilters");
        return filters == null || documentArray(filters);
    }

    /**
     * @return 所有出现的选项都为布尔值时返回 true
     */
    static boolean booleanOptions(BsonDocument options, String... names) {
        for (String name : names) {
            if (options.containsKey(name) && !options.isBoolean(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return 所有出现的选项都为文档时返回 true
     */
    static boolean documentOptions(BsonDocument options, String... names) {
        for (String name : names) {
            if (options.containsKey(name) && !options.isDocument(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return 未声明 hint，或 hint 为索引名称、索引键文档时返回 true
     */
    static boolean validHint(BsonDocument options) {
        BsonValue hint = options.get("hint");
        return hint == null || hint.isString() || hint.isDocument();
    }

    /**
     * mongosh 的 Driver 会重新解析排序方向，因此仅映射可无歧义传递的排序文档。
     */
    static boolean validSort(BsonValue sort) {
        if (sort == null) {
            return true;
        }
        if (!sort.isDocument()) {
            return false;
        }
        for (BsonValue direction : sort.asDocument().values()) {
            boolean numeric = direction.isInt32()
                    && (direction.asInt32().getValue() == 1 || direction.asInt32().getValue() == -1);
            boolean meta = direction.isDocument() && direction.asDocument().size() == 1
                    && direction.asDocument().isString("$meta");
            if (!numeric && !meta) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将指定的整数选项传为 Shell API 所需的 JavaScript number。
     *
     * @return 选项缺省或可以在 int32 范围内无损映射时返回 true
     */
    static boolean normalizeInteger(BsonDocument options, String name, int minimum) {
        BsonValue value = options.get(name);
        if (value == null) {
            return true;
        }
        if (!value.isInt32() && !value.isInt64()) {
            return false;
        }
        long number = value.asNumber().longValue();
        if (number < minimum || number > Integer.MAX_VALUE) {
            return false;
        }
        options.put(name, new BsonInt32((int) number));
        return true;
    }

    /**
     * 检查当前 Shell 能传递的 readConcern 和 readPreference 形态。
     */
    static boolean normalizeReadOptions(BsonDocument options) {
        if (options.containsKey(READ_CONCERN)) {
            if (!options.isDocument(READ_CONCERN)) {
                return false;
            }
            BsonDocument concern = options.getDocument(READ_CONCERN);
            if (!concern.isEmpty() && (concern.size() != 1 || !concern.isString("level"))) {
                return false;
            }
        }
        if (options.containsKey(READ_PREFERENCE)) {
            BsonValue preference = readPreference(options.get(READ_PREFERENCE));
            if (preference == null) {
                return false;
            }
            options.remove(READ_PREFERENCE);
            options.put("readPreference", preference);
        }
        return true;
    }

    /**
     * @return mongosh 可选择的读偏好，无法保留路由选项时返回 null
     */
    static BsonValue readPreference(BsonValue value) {
        if (!value.isDocument()) {
            return null;
        }
        BsonDocument preference = options(value.asDocument(), new String[]{"mode", "tags", "maxStalenessSeconds"});
        if (preference == null || !preference.isString("mode")) {
            return null;
        }
        String mode = preference.getString("mode").getValue();
        if (!Arrays.asList("primary", "primaryPreferred", "secondary", "secondaryPreferred", "nearest")
                .contains(mode)) {
            return null;
        }
        if (preference.containsKey("tags") && !documentArray(preference.get("tags"))) {
            return null;
        }
        if (!normalizeInteger(preference, "maxStalenessSeconds", 90)) {
            return null;
        }
        return preference.size() == 1 ? new BsonString(mode) : preference;
    }

    /**
     * 检查 Shell 的写关注选项，保留服务端可见的 w、j 和 wtimeout。
     */
    static boolean normalizeWriteConcern(BsonDocument options) {
        BsonValue value = options.get(WRITE_CONCERN);
        if (value == null) {
            return true;
        }
        if (!value.isDocument()) {
            return false;
        }
        BsonDocument concern = options(value.asDocument(), new String[]{"w", "j", "wtimeout"});
        if (concern == null || !booleanOptions(concern, "j") || !normalizeInteger(concern, "wtimeout", 0)
                || (!concern.isString("w") && !normalizeInteger(concern, "w", 0))) {
            return false;
        }
        options.put(WRITE_CONCERN, concern);
        return true;
    }

    /**
     * 校验写命令通用选项。
     */
    static boolean normalizeWriteOptions(BsonDocument options) {
        return booleanOptions(options, "ordered", "bypassDocumentValidation")
                && documentOptions(options, "let")
                && normalizeInteger(options, MAX_TIME_MS, 0)
                && normalizeWriteConcern(options);
    }

    /**
     * @return 命令要求无序处理时返回 true
     */
    static boolean unordered(BsonDocument options) {
        return BsonBoolean.FALSE.equals(options.get("ordered"));
    }

    /**
     * 判断服务端更新表达式的形态。混合操作符和普通字段交给 runCommand。
     *
     * @param update 更新值
     * @return 更新形态，无法识别时为 null
     */
    static UpdateForm updateForm(BsonValue update) {
        if (documentArray(update) && !update.asArray().isEmpty()) {
            return UpdateForm.PIPELINE;
        }
        if (update == null || !update.isDocument()) {
            return null;
        }
        BsonDocument document = update.asDocument();
        if (document.isEmpty()) {
            return UpdateForm.REPLACEMENT;
        }
        boolean operator = document.getFirstKey().startsWith("$");
        for (String key : document.keySet()) {
            if (key.startsWith("$") != operator) {
                return null;
            }
        }
        return operator ? UpdateForm.OPERATORS : UpdateForm.REPLACEMENT;
    }

    /**
     * BSON 更新负载的三种形态。
     */
    enum UpdateForm {
        OPERATORS,
        PIPELINE,
        REPLACEMENT
    }

    /**
     * @param target 数据库、集合或游标表达式
     * @param method 已知的 Shell 方法名
     * @param arguments BSON 参数
     * @return 可继续链式调用的 Shell 表达式
     */
    static String call(String target, String method, BsonValue... arguments) {
        StringJoiner call = new StringJoiner(", ", target + "." + method + "(", ")");
        for (BsonValue argument : arguments) {
            call.add(BsonShellFormatter.format(argument));
        }
        return call.toString();
    }

    /**
     * @return 完整的 Shell 语句，选项为空时省略末项参数
     */
    static String statement(String target, String method, BsonDocument options, BsonValue... arguments) {
        if (options.isEmpty()) {
            return call(target, method, arguments) + ";";
        }
        BsonValue[] allArguments = Arrays.copyOf(arguments, arguments.length + 1);
        allArguments[arguments.length] = options;
        return call(target, method, allArguments) + ";";
    }
}
