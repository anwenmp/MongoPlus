package com.mongoplus.indexer.scanner;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 已经逐 Core/Driver 实现审计的参数表示；只由显式 mongoParam 标签引用。 */
final class StageParameterConcepts {
    private static final String CORE = "mongo-plus-core/src/main/java/com/mongoplus/";
    private static final List<String> SEMANTICS = Arrays.asList(
            "BUCKET_BOUNDARY", "COLLECTION_NAME", "DATABASE_NAME", "FIELD_NAME", "FIELD_REFERENCE",
            "FOREIGN_FIELD_NAME", "LOCAL_FIELD_NAME", "OUTPUT_FIELD_NAME", "OUTPUT_FIELD_PATH_SEGMENT",
            "PIPELINE", "PIPELINE_STAGE_DOCUMENT", "SORT_SPECIFICATION", "STAGE_BODY_DOCUMENT");

    private StageParameterConcepts() { }

    static boolean contains(String semantic) { return SEMANTICS.contains(semantic); }

    static String conceptId(String semantic) { return "PIPELINE_PARAMETER_" + semantic; }

    static boolean acceptsConcept(String semantic, String reference) {
        return conceptId(semantic).equals(reference) || "FOREIGN_FIELD_NAME".equals(semantic)
                && (conceptId("GRAPH_CONNECT_FROM_FIELD_NAME").equals(reference)
                || conceptId("GRAPH_CONNECT_TO_FIELD_NAME").equals(reference));
    }

    /** 校验显式标签与 Java 表示兼容，不依据类型产生标签。 */
    static boolean accepts(String semantic, String type, String scope) {
        String value = type;
        if ("ELEMENT".equals(scope)) {
            if (value.endsWith("...")) { value = value.substring(0, value.length() - 3); }
            else if (value.endsWith("[]")) { value = value.substring(0, value.length() - 2); }
            else if (value.contains("<")) { value = value.substring(value.indexOf('<') + 1, value.lastIndexOf('>')); }
        }
        value = value.replaceFirst("^\\? extends ", "");
        boolean string = "String".equals(value) || "java.lang.String".equals(value);
        boolean getter = value.startsWith("SFunction<") || value.startsWith("com.mongoplus.support.SFunction<");
        if (Arrays.asList("FIELD_NAME", "FIELD_REFERENCE", "LOCAL_FIELD_NAME", "FOREIGN_FIELD_NAME",
                "OUTPUT_FIELD_NAME").contains(semantic)) { return string || getter; }
        if ("COLLECTION_NAME".equals(semantic)) {
            return "VALUE".equals(scope) && (string || value.startsWith("Class<") || value.startsWith("java.lang.Class<"));
        }
        if ("DATABASE_NAME".equals(semantic)) { return "VALUE".equals(scope) && string; }
        if ("OUTPUT_FIELD_PATH_SEGMENT".equals(semantic)) { return "ELEMENT".equals(scope) && getter; }
        if ("BUCKET_BOUNDARY".equals(semantic)) { return "ELEMENT".equals(scope); }
        if ("PIPELINE".equals(semantic)) {
            return "VALUE".equals(scope) && (value.startsWith("Aggregate<")
                    || value.startsWith("com.mongoplus.aggregate.Aggregate<")
                    || value.matches("(?:java\\.util\\.)?List<\\? extends (?:org\\.bson\\.conversions\\.)?Bson>"));
        }
        return ("Bson".equals(value) || "org.bson.conversions.Bson".equals(value))
                && ("PIPELINE_STAGE_DOCUMENT".equals(semantic) ? "ELEMENT".equals(scope) : "VALUE".equals(scope));
    }

    static Map<String, Object> concept(String reference) {
        String role = reference.substring("PIPELINE_PARAMETER_".length());
        String semantic = role.startsWith("GRAPH_CONNECT_") ? "FOREIGN_FIELD_NAME" : role;
        Map<String, Object> result = object("id", reference, "semanticType", semantic);
        if (role.startsWith("GRAPH_CONNECT_")) {
            result.put("fieldRole", "GRAPH_CONNECT_FROM_FIELD_NAME".equals(role) ? "TRAVERSAL_SOURCE" : "TRAVERSAL_TARGET");
            result.put("bsonSlot", "GRAPH_CONNECT_FROM_FIELD_NAME".equals(role) ? "connectFromField" : "connectToField");
        }
        result.put("name", semantic);
        result.put("description", "仅适用于逐 overload 显式声明的参数；Java 类型限制不变，不验证服务端合法性。");
        List<Object> representations = new java.util.ArrayList<Object>();
        String mechanism;
        String symbols;
        switch (semantic) {
            case "FIELD_REFERENCE":
                representations.add(object("javaType", "java.lang.String", "encoding", "UNCHANGED",
                        "prefix", "$", "excludedPrefix", "$$", "automaticFieldPrefix", false));
                representations.add(getter("GET_FIELD_NAME_LINE_OPTION"));
                result.put("interpretation", "AGGREGATION_FIELD_REFERENCE");
                mechanism = "String 原样进入 unwind 的 BsonString；getter 使用美元前缀加真实字段名。";
                symbols = "unwind; group; bucket; bucketAuto; graphLookup; replaceRoot; replaceWith; sortByCount";
                break;
            case "FIELD_NAME":
            case "LOCAL_FIELD_NAME":
            case "FOREIGN_FIELD_NAME":
            case "OUTPUT_FIELD_NAME":
                representations.add(name());
                representations.add(getter("GET_FIELD_NAME_LINE"));
                result.put("interpretation", "NAME");
                result.put("fieldContext", "LOCAL_FIELD_NAME".equals(semantic) ? "INPUT_DOCUMENT"
                        : "FOREIGN_FIELD_NAME".equals(semantic) ? "FOREIGN_COLLECTION"
                        : "OUTPUT_FIELD_NAME".equals(semantic) ? "OUTPUT_DOCUMENT" : "STAGE_FIELD");
                mechanism = "String 保留原值；getter 取实际字段名，不加/删美元前缀。参数角色由声明指定。";
                symbols = "lookup; graphLookup; count; facet; addFields; set; densify; unset; orderBy; buildProject";
                break;
            case "COLLECTION_NAME":
                representations.add(name());
                representations.add(object("javaType", "java.lang.Class", "encoding", "ANNOTATION_OPERATE_COLLECTION_NAME",
                        "includesDatabase", false));
                result.put("interpretation", "NAME");
                mechanism = "String 是集合名原值；Class 经 CollectionName 注解/配置的类名转换，不携带数据库。";
                symbols = "lookup; out; merge; unionWith; graphLookup";
                break;
            case "DATABASE_NAME":
                representations.add(name());
                result.put("interpretation", "NAME");
                mechanism = "out(String,String) 第一参数原样写入 db，第二参数独立写入 coll。";
                symbols = "out(String,String)";
                break;
            case "OUTPUT_FIELD_PATH_SEGMENT":
                representations.add(getter("GET_FIELD_NAME_LINE"));
                result.put("containerEncoding", "JOIN_DOT_IN_DECLARATION_ORDER");
                result.put("resultRole", "OUTPUT_FIELD_NAME");
                mechanism = "多个 getter 名按序连接为一个输出路径，不是多个赋值项。";
                symbols = "addFields(String,SFunction...); set(String,SFunction...)";
                break;
            case "PIPELINE":
                representations.add(object("javaType", "java.util.List", "encoding", "UNCHANGED_ORDERED_STAGES",
                        "elementJavaType", "org.bson.conversions.Bson"));
                representations.add(object("javaType", "com.mongoplus.aggregate.Aggregate",
                        "encoding", "GET_AGGREGATE_CONDITION_LIST"));
                result.put("elementRole", "PIPELINE_STAGE_DOCUMENT");
                mechanism = "Aggregate 取既有管道列表；每个完整 Stage 按原序编码，不额外包装 Stage。";
                symbols = "facet; lookup; unionWith";
                break;
            case "PIPELINE_STAGE_DOCUMENT":
                representations.add(bson("UNCHANGED"));
                result.put("documentRole", "COMPLETE_STAGE");
                mechanism = "facet 的 Bson varargs 是完整 Stage 元素，经 Arrays.asList 后由 Driver 按序编码。";
                symbols = "facet(String,Bson...)";
                break;
            case "STAGE_BODY_DOCUMENT":
                representations.add(bson("WRAP_WITH_DECLARED_STAGE"));
                result.put("documentRole", "STAGE_BODY");
                mechanism = "Core new BasicDBObject(Stage,bson) 只外包一次；Stage 取该 overload 显式 mongoStages。";
                symbols = "addFields(Bson); set(Bson); bucket(Bson); bucketAuto(Bson); match(Bson); project(Bson)";
                break;
            case "SORT_SPECIFICATION":
                representations.add(bson("UNCHANGED"));
                result.put("documentRole", "SORT_BODY");
                mechanism = "Driver 直接 encodeValue(sortBy) 到 $setWindowFields.sortBy，不移除外层 $sort。";
                symbols = "setWindowFields";
                break;
            case "BUCKET_BOUNDARY":
                representations.add(object("javaType", "DECLARED_ELEMENT_TYPE", "encoding", "RUNTIME_CODEC"));
                result.put("interpretation", "BOUNDARY_VALUE");
                result.put("containerEncoding", "PRESERVE_ORDER");
                mechanism = "Driver 逐个 encodeValue 到 boundaries 数组；边界常量区别于 groupBy 表达式。";
                symbols = "bucket";
                break;
            default:
                throw new IllegalArgumentException("未审计的参数语义: " + semantic);
        }
        result.put("representations", representations);
        result.put("sourceEvidence", Arrays.asList(
                object("path", CORE + "aggregate/LambdaAggregateWrapper.java", "symbols", symbols, "mechanism", mechanism),
                object("path", CORE + "support/SFunction.java", "symbols", "getFieldNameLine; getFieldNameLineOption",
                        "mechanism", "getter 字段映射和美元前缀是两条独立转换路径。"),
                object("path", CORE + "handlers/collection/AnnotationOperate.java", "symbols", "getCollectionName",
                        "mechanism", "Class 转集合名，不读取该类的数据库作为 Stage 参数。"),
                object("artifact", "org.mongodb:mongodb-driver-core:5.4.0", "symbols", "Aggregates; BuildersHelper.encodeValue",
                        "mechanism", "已核对对应 Stage 的 BsonString/writeName/encodeValue 实现。")));
        return result;
    }

    private static Map<String, Object> getter(String encoding) {
        return object("javaType", "com.mongoplus.support.SFunction", "encoding", encoding);
    }

    private static Map<String, Object> name() {
        return object("javaType", "java.lang.String", "encoding", "UNCHANGED", "automaticFieldPrefix", false,
                "automaticPrefixRemoval", false);
    }

    private static Map<String, Object> bson(String encoding) {
        return object("javaType", "org.bson.conversions.Bson", "encoding", encoding);
    }

    private static Map<String, Object> object(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) { result.put((String) pairs[i], pairs[i + 1]); }
        return result;
    }
}
