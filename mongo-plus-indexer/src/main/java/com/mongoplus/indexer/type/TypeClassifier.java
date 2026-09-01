package com.mongoplus.indexer.type;

/** 对方法签名中的类型做有界分类，防止引用扫描无限扩张。 */
public final class TypeClassifier {
    public String classify(String type) {
        return classify(type, "", "", "", "");
    }

    /**
     * 结合泛型和调用上下文分类参数语义。
     *
     * <p>SFunction 的用途由泛型边界决定：包装器入参与返回值表示条件分组 Lambda，
     * MongoPlusBasicDBObject 入参与返回值表示文档构建 Lambda，其余 getter 形态表示字段引用。</p>
     */
    public String classify(String type, String methodName, String parameterName,
                           String declaringType, String parameterDescription) {
        String simple = simpleName(type);
        if ("SFunction".equals(simple)) {
            if (type.contains("Wrapper<")) { return "CONDITION_GROUP_LAMBDA"; }
            if (type.contains("MongoPlusBasicDBObject")) { return "DOCUMENT_LAMBDA"; }
            if ("function".equals(parameterName)
                    && ("and".equals(methodName) || "or".equals(methodName) || "nor".equals(methodName)
                    || "not".equals(methodName) || "expr".equals(methodName)
                    || parameterDescription.contains("链式查询"))) {
                return "CONDITION_GROUP_LAMBDA";
            }
            if ("custom".equals(methodName) && "function".equals(parameterName)
                    && declaringType.contains("QueryCondition")) {
                return "DOCUMENT_LAMBDA";
            }
            return "LAMBDA_FIELD";
        }
        if ("FieldChain".equals(simple)) { return "NESTED_FIELD_BUILDER"; }
        if (isJdkType(simple)) { return "JDK_TYPE"; }
        if ("RegexOptions".equals(simple)) { return "ENUM"; }
        if ("Bson".equals(simple) || "BasicDBObject".equals(simple) || "BsonValue".equals(simple)) {
            return "EXTERNAL_TYPE";
        }
        return "MONGO_PLUS_TYPE";
    }

    private boolean isJdkType(String type) {
        return "boolean".equals(type) || "byte".equals(type) || "short".equals(type)
                || "int".equals(type) || "long".equals(type) || "float".equals(type)
                || "double".equals(type) || "char".equals(type) || "void".equals(type)
                || "String".equals(type) || "Object".equals(type) || "Class".equals(type)
                || "List".equals(type) || "Set".equals(type) || "Map".equals(type)
                || "Collection".equals(type) || "Pattern".equals(type) || "Consumer".equals(type)
                || "Function".equals(type) || "Number".equals(type) || "Comparable".equals(type);
    }

    public static String simpleName(String type) {
        String raw = type.replace("...", "");
        int generic = raw.indexOf('<');
        if (generic >= 0) { raw = raw.substring(0, generic); }
        int dot = raw.lastIndexOf('.');
        return dot >= 0 ? raw.substring(dot + 1) : raw;
    }
}
