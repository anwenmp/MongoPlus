package com.mongoplus.annotation.index.search;

import com.mongoplus.enums.Analyzer;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MongoDB Search 同义词映射，配置于 {@link MongoSearchIndex#synonyms()}。
 * <p>仅归属包含它的搜索索引；不同索引可使用相同的同义词映射名称。</p>
 * <p>只引用已有的同义词来源集合，不定义或写入集合中的同义词文档。</p>
 * <p>Atlas Free/Flex 使用单个映射，多映射需要 M10 或更高等级；Free 来源集合最多 10000 条文档。
 * 解析器不掌握部署等级，不将此限制硬编码为所有部署的统一数量上限。</p>
 * <p>查询时需在 text 或 phrase 操作符中引用映射名称；目标字段须使用与映射相同的分析器。</p>
 * <p>本注解仅声明配置，名称校验、分析器解析和定义转换由搜索解析器负责。</p>
 *
 * @see <a href="https://www.mongodb.com/docs/search/indexes/synonyms/">Synonym Mappings</a>
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MongoSearchSynonym {

    /**
     * 同义词映射名称，对应 synonyms 中的 name，必填且不能为空白。
     * <p>同一索引内必须唯一；该名称不是索引名称，也不是来源集合名。</p>
     */
    String name();

    /**
     * 同义词来源集合名，对应 source.collection，必填且不能为空白。
     * <p>集合必须与被索引集合位于同一数据库；不能通过此参数指定其他数据库。
     * 集合文档须符合官方 equivalent 或 explicit 同义词格式。</p>
     */
    String sourceCollection();

    /**
     * 同义词分析器，对应必填的 analyzer。
     * <p>DEFAULT 且未配置 customAnalyzer 时解析为 lucene.standard，并显式输出 analyzer；
     * 不能省略该必填属性，也不能输出 lucene.default。</p>
     * <p>不支持 CJK、KUROMOJI。目标搜索字段必须使用相同分析器。</p>
     */
    Analyzer analyzer() default Analyzer.DEFAULT;

    /**
     * 自定义同义词分析器名称，引用 {@link MongoSearchIndex#analyzers()} 中的定义。
     * <p>指定时 analyzer 保持 DEFAULT，使用该名称作为必填 analyzer 的值。</p>
     * <p>不能使用 nGram、edgeGram tokenizer，或 daitchMokotoffSoundex、nGram、
     * edgeGram、shingle、wordDelimiterGraph token filter。</p>
     */
    String customAnalyzer() default "";
}
