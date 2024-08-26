package com.anwen.mongo.enums;

/**
 * 文本搜索语言，默认{@code ENGLISH}
 * @author anwen
 */
public enum TextLanguages {

    /**
     * 如果指定为none，文本索引会解析字段中的每个单词，包括停用语，并忽略后缀词干
     */
    NONE("none"),

    /**
     * 丹麦语
     */
    DANISH("danish"),

    /**
     * 荷兰语
     */
    DUTCH("dutch"),

    /**
     * 英语
     */
    ENGLISH("english"),

    /**
     * 芬兰语
     */
    FINNISH("finnish"),

    /**
     * 法语
     */
    FRENCH("french"),

    /**
     * 德语
     */
    GERMAN("german"),

    /**
     * 匈牙利语
     */
    HUNGARIAN("hungarian"),

    /**
     * 意大利语
     */
    ITALIAN("italian"),

    /**
     * 挪威语
     */
    NORWEGIAN("norwegian"),

    /**
     * 葡萄牙语
     */
    PORTUGUESE("portuguese"),

    /**
     * 罗马尼亚语
     */
    ROMANIAN("romanian"),

    /**
     * 俄语
     */
    RUSSIAN("russian"),

    /**
     * 西班牙语
     */
    SPANISH("spanish"),

    /**
     * 瑞典语
     */
    SWEDISH("swedish"),

    /**
     * 土耳其语
     */
    TURKISH("turkish");

    private final String language;

    TextLanguages(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

}
