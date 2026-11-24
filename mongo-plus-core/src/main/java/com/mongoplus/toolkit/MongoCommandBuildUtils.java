package com.mongoplus.toolkit;

import com.mongodb.Function;
import com.mongodb.event.CommandStartedEvent;
import com.mongoplus.enums.CommandEnum;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonRegularExpression;
import org.bson.BsonTimestamp;
import org.bson.BsonValue;
import org.bson.Document;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 根据 命令 构建 mongo 语句
 *
 * @author loser
 */
public class MongoCommandBuildUtils {

    private static final Map<String, Function<CommandStartedEvent, String>> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(CommandEnum.FIND.getCommand(), MongoCommandBuildUtils::buildFindCommand);
        HANDLERS.put(CommandEnum.COUNT.getCommand(), MongoCommandBuildUtils::buildCountCommand);
        HANDLERS.put(CommandEnum.INSERT.getCommand(), MongoCommandBuildUtils::buildInsertCommand);
        HANDLERS.put(CommandEnum.DELETE.getCommand(), MongoCommandBuildUtils::buildDeleteCommand);
        HANDLERS.put(CommandEnum.UPDATE.getCommand(), MongoCommandBuildUtils::buildUpdateCommand);
        HANDLERS.put(CommandEnum.AGGREGATE.getCommand(), MongoCommandBuildUtils::buildAggregateCommand);
        HANDLERS.put(CommandEnum.CREATE_INDEXES.getCommand(), MongoCommandBuildUtils::buildIndexCommand);
    }

    /**
     * 构建语句
     *
     * @param event mongo 命令
     * @return 可执行的 json 命令
     */
    public static String buildCommand(CommandStartedEvent event) {

        Function<CommandStartedEvent, String> function = HANDLERS.get(event.getCommandName());
        if (Objects.nonNull(function)) {
            return function.apply(event);
        }
        return event.getCommand().toString();

    }

    private static String buildFindCommand(CommandStartedEvent event) {

        String collection = getJson("find", "", event);
        String json = getJson("filter", "", event);
        String sort = getJson("sort","",event);
        String skip = getJson("skip", "", event);
        String limit = getJson("limit", "", event);
        String command = "db." + collection + "." + event.getCommandName() + "(" + json + ")";
        if (StringUtils.isNotBlank(sort)){
            command += ".sort(" + sort + ")";
        }
        if (StringUtils.isNotBlank(skip) && !skip.equals("0")) {
            command += ".skip(" + skip + ")";
        }
        if (StringUtils.isNotBlank(limit) && !limit.equals("0")) {
            command += ".limit(" + limit + ")";
        }
        return command;

    }

    private static String buildInsertCommand(CommandStartedEvent event) {

        String json = getJson("documents", "", event);
        String collection = getJson("insert", "", event);
        return "db." + collection + ".insert" + "(" + json + ")";

    }

    private static String buildUpdateCommand(CommandStartedEvent event) {

        String collection = getJson("update", "", event);
        String multi;
        BsonValue bsonValue = event.getCommand().get("updates");
        if (bsonValue instanceof BsonArray) {
            BsonArray array = (BsonArray) bsonValue;
            int size = array.size();
            // 构建批量
            if (size > 1) {
                List<String> res = new ArrayList<>();
                for (BsonValue value : array) {
                    String q = getJson(value, "q");
                    String u = getJson(value, "u");
                    multi = getJson(value, "multi");
                    if ("true".equals(multi)) {
                        res.add("db." + collection + ".updateMany" + "(" + q + ", " + u + ")");
                    } else {
                        res.add("db." + collection + ".updateOne" + "(" + q + ", " + u + ")");
                    }
                }
                return res.stream().collect(Collectors.joining(System.lineSeparator()));
            }
        }
        multi = getJson("updates", "multi", event);
        String q = getJson("updates", "q", event);
        String u = getJson("updates", "u", event);
        if ("true".equals(multi)) {
            return "db." + collection + ".updateMany" + "(" + q + ", " + u + ")";
        } else {
            return "db." + collection + ".updateOne" + "(" + q + ", " + u + ")";
        }

    }

    private static String buildDeleteCommand(CommandStartedEvent event) {

        String json = getJson("deletes", "q", event);
        String collection = getJson("delete", "", event);
        return "db." + collection + ".deleteMany" + "(" + json + ")";

    }

    private static String buildCountCommand(CommandStartedEvent event) {

        String json = getJson("counts", "q", event);
        String collection = getJson("count", "", event);
        return "db." + collection + ".count" + "(" + json + ")";

    }

    private static String buildIndexCommand(CommandStartedEvent event){
        Document document = new Document();
        BsonDocument bsonDocument = event.getCommand();
        String db = "getSiblingDB(\""+bsonDocument.getString("$db").getValue()+"\")";
        String collection = bsonDocument.getString("createIndexes").getValue();
        document.put("createIndexes",collection);
        document.put("indexes",bsonDocument.getArray("indexes").getValues());
        if (bsonDocument.containsKey("writeConcern")) {
            document.put("writeConcern", bsonDocument.getDocument("writeConcern"));
        }
        return "db."+db+".runCommand("+document.toJson()+")";
    }

    private static String getJson(BsonValue bs, String item) {

        if (Objects.isNull(bs)) {
            return "";
        }
        if (bs.isDocument()) {
            return buildByDocument((BsonDocument) bs, item);
        }
        if (bs.isArray()) {
            return buildByArray((BsonArray) bs, item);
        }
        return BsonUtil.toJavaType(bs).toString();

    }

    private static String buildByArray(BsonArray bs, String item) {

        List<String> jsons = new ArrayList<>();
        for (BsonValue delete : bs) {
            BsonDocument query = delete.asDocument();
            if (StringUtils.isBlank(item)) {
                jsons.add(toShellJson(query));
                continue;
            }
            BsonValue bsonValue = query.get(item);
            if (Objects.nonNull(bsonValue)) {
                if (bsonValue.isDocument()) {
                    jsons.add(toShellJson(bsonValue.asDocument()));
                } else {
                    jsons.add(BsonUtil.toJavaType(bsonValue).toString());
                }
            }
        }
        if (jsons.isEmpty()) {
            return "";
        } else if (jsons.size() == 1) {
            return jsons.get(0);
        }
        return "[" + String.join(",", jsons) + "]";

    }

    private static String buildByDocument(BsonDocument bs, String item) {

        if (StringUtils.isBlank(item)) {
            return toShellJson(bs);
        } else {
            BsonValue bsonValue = bs.get(item);
            // UpdateOneModel 可省略 multi，缺失字段与数组解析保持一致。
            if (Objects.isNull(bsonValue)) {
                return "";
            }
            if (bsonValue instanceof BsonDocument) {
                return toShellJson(bsonValue.asDocument());
            }
            return BsonUtil.toJavaType(bsonValue).toString();
        }

    }

    private static String getJson(String key, String item, CommandStartedEvent event) {

        BsonDocument filter = event.getCommand();
        BsonValue bs = filter.get(key);
        return getJson(bs, item);

    }

    @SuppressWarnings("unchecked")
    private static String buildAggregateCommand(CommandStartedEvent event) {

        List<BsonDocument> pipeline = (List<BsonDocument>) event.getCommand().get("pipeline");
        String collection = getJson("aggregate", "", event);
        if (CollUtil.isEmpty(pipeline)) {
            return "db." + collection + "." + event.getCommandName() + "()";
        }
        StringBuilder commandBuilder = new StringBuilder("db." + collection + "." + event.getCommandName() + "([");
        for (BsonDocument stage : pipeline) {
            commandBuilder.append(toShellJson(stage)).append(", ");
        }
        commandBuilder.delete(commandBuilder.length() - 2, commandBuilder.length()).append("])");
        return commandBuilder.toString();

    }


    /**
     * 把 bson 转换成可以直接粘进 mongo shell(mongosh) 执行的 javascript 字面量
     * <p>驱动自带的 toJson 输出的是 Extended JSON，日期是 {"$date": "..."}、ObjectId 是 {"$oid": "..."}，
     * 这种格式粘到 shell 里只是一个普通子文档，拿去做查询条件永远匹配不到数据；
     * 这里按 shell 里的写法输出 ISODate(...)、ObjectId(...) 等构造器调用</p>
     *
     * @param value bson 值
     * @return shell 可执行的 javascript 字面量
     */
    private static String toShellJson(BsonValue value) {

        if (Objects.isNull(value)) {
            return "null";
        }
        switch (value.getBsonType()) {
            case DOCUMENT:
                return documentToShellJson(value.asDocument());
            case ARRAY:
                return arrayToShellJson(value.asArray());
            case STRING:
                return quote(value.asString().getValue());
            case SYMBOL:
                return quote(value.asSymbol().getSymbol());
            case INT32:
                return String.valueOf(value.asInt32().getValue());
            case INT64:
                return String.valueOf(value.asInt64().getValue());
            case DOUBLE:
                return doubleToShellJson(value.asDouble().getValue());
            case DECIMAL128:
                return "NumberDecimal(\"" + value.asDecimal128().getValue() + "\")";
            case BOOLEAN:
                return String.valueOf(value.asBoolean().getValue());
            case DATE_TIME:
                return "ISODate(\"" + Instant.ofEpochMilli(value.asDateTime().getValue()) + "\")";
            case OBJECT_ID:
                return "ObjectId(\"" + value.asObjectId().getValue().toHexString() + "\")";
            case BINARY:
                BsonBinary binary = value.asBinary();
                return "BinData(" + binary.getType() + ", \"" + Base64.getEncoder().encodeToString(binary.getData()) + "\")";
            case REGULAR_EXPRESSION:
                BsonRegularExpression regularExpression = value.asRegularExpression();
                return "/" + regularExpression.getPattern().replace("/", "\\/") + "/" + regularExpression.getOptions();
            case TIMESTAMP:
                BsonTimestamp timestamp = value.asTimestamp();
                return "Timestamp(" + timestamp.getTime() + ", " + timestamp.getInc() + ")";
            case JAVASCRIPT:
                return quote(value.asJavaScript().getCode());
            case JAVASCRIPT_WITH_SCOPE:
                return "Code(" + quote(value.asJavaScriptWithScope().getCode())
                        + ", " + documentToShellJson(value.asJavaScriptWithScope().getScope()) + ")";
            case DB_POINTER:
                return "DBPointer(" + quote(value.asDBPointer().getNamespace())
                        + ", ObjectId(\"" + value.asDBPointer().getId().toHexString() + "\"))";
            case MIN_KEY:
                return "MinKey()";
            case MAX_KEY:
                return "MaxKey()";
            case UNDEFINED:
                return "undefined";
            case NULL:
            default:
                return "null";
        }

    }

    private static String documentToShellJson(BsonDocument document) {

        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, BsonValue> entry : document.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(quote(entry.getKey())).append(": ").append(toShellJson(entry.getValue()));
        }
        return builder.append("}").toString();

    }

    private static String arrayToShellJson(BsonArray array) {

        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < array.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(toShellJson(array.get(i)));
        }
        return builder.append("]").toString();

    }

    private static String doubleToShellJson(double value) {

        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }
        return String.valueOf(value);

    }

    private static String quote(String value) {

        StringBuilder builder = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
            }
        }
        return builder.append('"').toString();

    }
}
