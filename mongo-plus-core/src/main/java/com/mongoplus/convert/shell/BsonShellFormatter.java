package com.mongoplus.convert.shell;

import org.bson.BsonDocument;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 按 BSON 结构生成单行 JavaScript 表达式，标量优先复用 Driver 的 Shell 序列化。
 */
final class BsonShellFormatter {

    private static final int MAX_DEPTH = 128;
    private static final long MAX_ARRAY_INDEX = 4_294_967_294L;
    private static final long MAX_DATE_MILLIS = 8_640_000_000_000_000L;
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
    private static final Pattern REGEX_OPTIONS = Pattern.compile("[ilmsux]*");
    private static final DateTimeFormatter ISO_MILLIS = new DateTimeFormatterBuilder().appendInstant(3).toFormatter();
    private static final JsonWriterSettings SHELL = JsonWriterSettings.builder()
            .outputMode(JsonMode.SHELL)
            .int64Converter((value, writer) -> writer.writeRaw("NumberLong(\"" + value + "\")"))
            .doubleConverter((value, writer) -> writer.writeRaw("new Double(" + value + ")"))
            .dateTimeConverter((value, writer) -> writer.writeRaw(dateExpression(value)))
            .timestampConverter((value, writer) -> writer.writeRaw("Timestamp({t: "
                    + Integer.toUnsignedLong(value.getTime()) + ", i: " + Integer.toUnsignedLong(value.getInc())
                    + "})"))
            .regularExpressionConverter((value, writer) -> {
                if (!REGEX_OPTIONS.matcher(value.getOptions()).matches()) {
                    throw new IllegalArgumentException("Unsupported BSON regular expression flags");
                }
                writer.writeRaw("new BSONRegExp(" + quote(value.getPattern()) + ", "
                        + quote(value.getOptions()) + ")");
            })
            .symbolConverter((value, writer) -> writer.writeRaw("new BSONSymbol(" + quote(value) + ")"))
            .javaScriptConverter((value, writer) -> writer.writeRaw("Code(" + quote(value) + ")"))
            .minKeyConverter((value, writer) -> writer.writeRaw("MinKey()"))
            .maxKeyConverter((value, writer) -> writer.writeRaw("MaxKey()"))
            .build();

    private BsonShellFormatter() {
    }

    /**
     * 格式化 BSON 文档、数组或标量。格式化过程中不修改原值。
     *
     * @param value 非空的 BSON 值
     * @return 单行 JavaScript 表达式
     * @throws IllegalArgumentException Shell 无法保留此值的 BSON 类型、范围或字段顺序
     */
    static String format(BsonValue value) {
        StringBuilder output = new StringBuilder();
        appendValue(Objects.requireNonNull(value, "value"), output, 0);
        return output.toString();
    }

    /**
     * @param value 非空的字符串
     * @return 由 Driver 转义的 JavaScript 字符串字面量
     */
    static String quote(String value) {
        return scalar(new BsonString(value));
    }

    /**
     * 递归处理容器和带作用域的代码，其余类型在标量边界转换。
     */
    private static void appendValue(BsonValue value, StringBuilder output, int depth) {
        if (depth > MAX_DEPTH) {
            throw new IllegalArgumentException("BSON nesting limit exceeded");
        }
        switch (value.getBsonType()) {
            case DOCUMENT:
                appendDocument(value.asDocument(), output, depth);
                break;
            case ARRAY:
                output.append('[');
                boolean first = true;
                for (BsonValue item : value.asArray()) {
                    if (!first) {
                        output.append(", ");
                    }
                    appendValue(item, output, depth + 1);
                    first = false;
                }
                output.append(']');
                break;
            case JAVASCRIPT_WITH_SCOPE:
                BsonJavaScriptWithScope code = value.asJavaScriptWithScope();
                output.append("Code(").append(quote(code.getCode())).append(", ");
                appendValue(code.getScope(), output, depth + 1);
                output.append(')');
                break;
            case UNDEFINED:
            case DB_POINTER:
                throw new IllegalArgumentException("BSON type cannot be represented by mongosh: "
                        + value.getBsonType());
            default:
                output.append(scalar(value));
        }
    }

    /**
     * 保留字段顺序，并使用计算属性表示 JavaScript 对象字面量中的特殊属性。
     */
    private static void appendDocument(BsonDocument document, StringBuilder output, int depth) {
        output.append('{');
        boolean first = true;
        boolean seenNamedField = false;
        long previousIndex = -1;
        for (Map.Entry<String, BsonValue> field : document.entrySet()) {
            String name = field.getKey();
            long index = arrayIndex(name);
            if (index >= 0 && (seenNamedField || index <= previousIndex)) {
                throw new IllegalArgumentException("JavaScript would reorder BSON fields");
            }
            if ("_bsontype".equals(name) && !field.getValue().isNull()) {
                throw new IllegalArgumentException("BSON document conflicts with the JavaScript BSON type marker");
            }
            seenNamedField |= index < 0;
            previousIndex = index;
            if (!first) {
                output.append(", ");
            }
            output.append(fieldName(name)).append(": ");
            appendValue(field.getValue(), output, depth + 1);
            first = false;
        }
        output.append('}');
    }

    /**
     * @return ECMAScript 会提前枚举的整数属性名，普通属性名返回 -1
     */
    private static long arrayIndex(String name) {
        if (name.isEmpty() || name.length() > 10 || (name.length() > 1 && name.charAt(0) == '0')) {
            return -1;
        }
        long index = 0;
        for (int i = 0; i < name.length(); i++) {
            char digit = name.charAt(i);
            if (digit < '0' || digit > '9') {
                return -1;
            }
            index = index * 10 + digit - '0';
        }
        return index <= MAX_ARRAY_INDEX ? index : -1;
    }

    /**
     * @return 保留原始名称的对象属性表达式
     */
    private static String fieldName(String name) {
        if ("__proto__".equals(name)) {
            return "[" + quote(name) + "]";
        }
        return IDENTIFIER.matcher(name).matches() ? name : quote(name);
    }

    /**
     * Driver 的 JSON writer 以文档作为根节点。固定封装字段没有参与命令内容。
     */
    private static String scalar(BsonValue value) {
        String wrapped = new BsonDocument("", value).toJson(SHELL);
        return wrapped.substring(wrapped.indexOf(':') + 2, wrapped.length() - 1);
    }

    /**
     * 在 JavaScript Date 的有效范围内保留精确毫秒值。
     */
    private static String dateExpression(long millis) {
        if (millis < -MAX_DATE_MILLIS || millis > MAX_DATE_MILLIS) {
            throw new IllegalArgumentException("BSON date is outside the JavaScript Date range");
        }
        String iso = ISO_MILLIS.format(Instant.ofEpochMilli(millis));
        return iso.length() == 24 ? "ISODate(\"" + iso + "\")" : "new Date(" + millis + ")";
    }
}
