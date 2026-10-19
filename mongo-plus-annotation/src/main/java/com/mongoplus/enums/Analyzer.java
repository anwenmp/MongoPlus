package com.mongoplus.enums;

/**
 * 分析器枚举
 *
 * @author anwen
 */

public enum Analyzer {

    /**
     * 不显式指定分析器，使用 MongoDB 默认行为。
     */
    DEFAULT("default"),

    // ==================== 通用分析器 ====================

    /**
     * 标准分析器
     * <p>standard分析器是所有MongoDB Search索引和查询的默认分析器。它根据单词边界将文本划分为术语，因此在大多数使用案例中都与语言无关。</p>
     * <p>它将所有术语转换为小写并删除标点符号。它提供基于语法的分词功能，可识别电子邮件地址、首字母缩略词、中日韩字符、字母数字等。</p>
     */
    STANDARD("standard"),

    /**
     * 简单分析器
     * <p>只要simple分析器找到非字母字符（例如空格、标点符号或者一个或多个数字），它就会将文本划分为可搜索术语（词元）。</p>
     * <p>它将所有文本转换为小写。</p>
     */
    SIMPLE("simple"),

    /**
     * 空格分析器
     * <p>只要找到空白字符，whitespace分析器就会将文本划分为可搜索词语（词元）。</p>
     * <p>它将所有文本保留为原始字母大小写。</p>
     */
    WHITESPACE("whitespace"),

    /**
     * 关键字分析器
     * <p>keyword分析器将整个字段值作为单个词元进行索引，不进行常规单词拆分。</p>
     * <p>它将所有文本保留为原始字母大小写。</p>
     */
    KEYWORD("keyword"),

    // ==================== 语言分析器 ====================

    /**
     * 阿拉伯语分析器
     * <p>适用于阿拉伯语文本的全文检索。</p>
     * <p>根据阿拉伯语的语言特点进行分词、停用词过滤及词形规范化。</p>
     */
    ARABIC("arabic"),

    /**
     * 亚美尼亚语分析器
     * <p>适用于亚美尼亚语文本的全文检索。</p>
     */
    ARMENIAN("armenian"),

    /**
     * 巴斯克语分析器
     * <p>适用于巴斯克语文本的全文检索。</p>
     */
    BASQUE("basque"),

    /**
     * 孟加拉语分析器
     * <p>适用于孟加拉语文本的全文检索。</p>
     */
    BENGALI("bengali"),

    /**
     * 巴西葡萄牙语分析器
     * <p>适用于巴西葡萄牙语文本的全文检索。</p>
     * <p>针对巴西葡萄牙语的词形变化和停用词进行处理。</p>
     */
    BRAZILIAN("brazilian"),

    /**
     * 保加利亚语分析器
     * <p>适用于保加利亚语文本的全文检索。</p>
     */
    BULGARIAN("bulgarian"),

    /**
     * 加泰罗尼亚语分析器
     * <p>适用于加泰罗尼亚语文本的全文检索。</p>
     */
    CATALAN("catalan"),

    /**
     * 中文分析器
     * <p>适用于中文文本的全文检索。</p>
     * <p>提供针对中文的分词和文本分析功能。</p>
     */
    CHINESE("chinese"),

    /**
     * 中日韩通用分析器
     * <p>CJK（Chinese、Japanese、Korean）分析器适用于中文、日文和韩文文本。</p>
     * <p>使用适合中日韩文字的分析方式，适用于多语言混合文本检索。</p>
     */
    CJK("cjk"),

    /**
     * 捷克语分析器
     * <p>适用于捷克语文本的全文检索。</p>
     */
    CZECH("czech"),

    /**
     * 丹麦语分析器
     * <p>适用于丹麦语文本的全文检索。</p>
     */
    DANISH("danish"),

    /**
     * 荷兰语分析器
     * <p>适用于荷兰语文本的全文检索。</p>
     */
    DUTCH("dutch"),

    /**
     * 英语分析器
     * <p>适用于英语文本的全文检索。</p>
     * <p>支持英语停用词过滤和词干提取，例如将不同形式的单词归一化，以提高搜索匹配能力。</p>
     */
    ENGLISH("english"),

    /**
     * 芬兰语分析器
     * <p>适用于芬兰语文本的全文检索。</p>
     */
    FINNISH("finnish"),

    /**
     * 法语分析器
     * <p>适用于法语文本的全文检索。</p>
     * <p>支持法语停用词过滤及词干处理。</p>
     */
    FRENCH("french"),

    /**
     * 加利西亚语分析器
     * <p>适用于加利西亚语文本的全文检索。</p>
     */
    GALICIAN("galician"),

    /**
     * 德语分析器
     * <p>适用于德语文本的全文检索。</p>
     * <p>支持德语相关的词形处理与停用词过滤。</p>
     */
    GERMAN("german"),

    /**
     * 希腊语分析器
     * <p>适用于希腊语文本的全文检索。</p>
     */
    GREEK("greek"),

    /**
     * 印地语分析器
     * <p>适用于印地语文本的全文检索。</p>
     */
    HINDI("hindi"),

    /**
     * 匈牙利语分析器
     * <p>适用于匈牙利语文本的全文检索。</p>
     */
    HUNGARIAN("hungarian"),

    /**
     * 印度尼西亚语分析器
     * <p>适用于印度尼西亚语文本的全文检索。</p>
     */
    INDONESIAN("indonesian"),

    /**
     * 爱尔兰语分析器
     * <p>适用于爱尔兰语文本的全文检索。</p>
     */
    IRISH("irish"),

    /**
     * 意大利语分析器
     * <p>适用于意大利语文本的全文检索。</p>
     */
    ITALIAN("italian"),

    /**
     * 日语分析器
     * <p>适用于日语文本的全文检索。</p>
     */
    JAPANESE("japanese"),

    /**
     * 韩语分析器
     * <p>适用于韩语文本的全文检索。</p>
     */
    KOREAN("korean"),

    /**
     * 日语Kuromoji分析器
     * <p>基于Lucene Kuromoji的日语分析器。</p>
     * <p>专门针对日语文本进行词语切分和词形分析。</p>
     */
    KUROMOJI("kuromoji"),

    /**
     * 拉脱维亚语分析器
     * <p>适用于拉脱维亚语文本的全文检索。</p>
     */
    LATVIAN("latvian"),

    /**
     * 立陶宛语分析器
     * <p>适用于立陶宛语文本的全文检索。</p>
     */
    LITHUANIAN("lithuanian"),

    /**
     * 波兰语Morfologik分析器
     * <p>基于Morfologik词形分析技术的波兰语分析器。</p>
     * <p>支持针对波兰语的词形还原，适用于词形变化较复杂的文本检索。</p>
     */
    MORFOLOGIK("morfologik"),

    /**
     * 韩语Nori分析器
     * <p>基于Lucene Nori的韩语分析器。</p>
     * <p>提供针对韩语文本的形态分析和分词处理。</p>
     */
    NORI("nori"),

    /**
     * 挪威语分析器
     * <p>适用于挪威语文本的全文检索。</p>
     */
    NORWEGIAN("norwegian"),

    /**
     * 波斯语分析器
     * <p>适用于波斯语文本的全文检索。</p>
     */
    PERSIAN("persian"),

    /**
     * 波兰语分析器
     * <p>适用于波兰语文本的全文检索。</p>
     */
    POLISH("polish"),

    /**
     * 葡萄牙语分析器
     * <p>适用于葡萄牙语文本的全文检索。</p>
     */
    PORTUGUESE("portuguese"),

    /**
     * 罗马尼亚语分析器
     * <p>适用于罗马尼亚语文本的全文检索。</p>
     */
    ROMANIAN("romanian"),

    /**
     * 俄语分析器
     * <p>适用于俄语文本的全文检索。</p>
     */
    RUSSIAN("russian"),

    /**
     * 中文智能分析器
     * <p>基于Lucene Smart Chinese Analysis的中文分析器。</p>
     * <p>针对简体中文文本进行词语切分，适用于中文文章、商品名称及内容搜索。</p>
     */
    SMARTCN("smartcn"),

    /**
     * 索拉尼库尔德语分析器
     * <p>适用于索拉尼库尔德语文本的全文检索。</p>
     */
    SORANI("sorani"),

    /**
     * 西班牙语分析器
     * <p>适用于西班牙语文本的全文检索。</p>
     */
    SPANISH("spanish"),

    /**
     * 瑞典语分析器
     * <p>适用于瑞典语文本的全文检索。</p>
     */
    SWEDISH("swedish"),

    /**
     * 泰语分析器
     * <p>适用于泰语文本的全文检索。</p>
     */
    THAI("thai"),

    /**
     * 土耳其语分析器
     * <p>适用于土耳其语文本的全文检索。</p>
     */
    TURKISH("turkish"),

    /**
     * 乌克兰语分析器
     * <p>适用于乌克兰语文本的全文检索。</p>
     */
    UKRAINIAN("ukrainian");

    private final String analyzer;

    Analyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getAnalyzer() {
        return "lucene." + analyzer;
    }
}

