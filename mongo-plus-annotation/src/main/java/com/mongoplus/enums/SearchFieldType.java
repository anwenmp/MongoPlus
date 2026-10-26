package com.mongoplus.enums;

/**
 * MongoDB Search 字段类型，不是 BSON 类型或 Java 类型。
 */
public enum SearchFieldType {

    /**
     * 全文检索字符串。
     */
    STRING("string"),

    /**
     * 自动补全字符串。
     */
    AUTOCOMPLETE("autocomplete"),

    /**
     * 用于等值查询、排序与分面的字符串。
     */
    TOKEN("token"),

    /**
     * 布尔值。
     */
    BOOLEAN("boolean"),

    /**
     * 日期。
     */
    DATE("date"),

    /**
     * int32、int64 或 double 数值。
     */
    NUMBER("number"),

    /**
     * BSON ObjectId。
     */
    OBJECT_ID("objectId"),

    /**
     * BSON Binary Subtype 4 的 UUID。
     */
    UUID("uuid"),

    /**
     * 对象或子文档。
     */
    DOCUMENT("document"),

    /**
     * 需要按数组元素独立匹配的对象数组。
     */
    EMBEDDED_DOCUMENTS("embeddedDocuments"),

    /**
     * GeoJSON 地理对象。
     */
    GEO("geo"),

    /**
     * MongoDB Search 索引中的向量字段。
     */
    VECTOR("vector"),

    /**
     * 旧日期分面类型。
     * @deprecated 使用 DATE。
     */
    @Deprecated
    DATE_FACET("dateFacet"),

    /**
     * 旧数值分面类型。
     * @deprecated 使用 NUMBER。
     */
    @Deprecated
    NUMBER_FACET("numberFacet"),

    /**
     * 旧字符串分面类型。
     * @deprecated 使用 TOKEN。
     */
    @Deprecated
    STRING_FACET("stringFacet"),

    /**
     * 旧向量类型。
     * @deprecated 使用 VECTOR。
     */
    @Deprecated
    KNN_VECTOR("knnVector");

    private final String value;

    SearchFieldType(String value) {
        this.value = value;
    }

    /**
     * 返回 MongoDB 索引定义使用的值。
     * @return 配置值
     */
    public String getValue() {
        return value;
    }
}
