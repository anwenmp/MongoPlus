package com.mongoplus.annotation.index.search;

import com.mongoplus.enums.Analyzer;
import com.mongoplus.enums.SearchFieldType;
import com.mongoplus.enums.SearchIndexOptions;
import com.mongoplus.enums.SearchIndexingMethod;
import com.mongoplus.enums.SearchNormalizer;
import com.mongoplus.enums.SearchNorms;
import com.mongoplus.enums.SearchQuantization;
import com.mongoplus.enums.SearchRepresentation;
import com.mongoplus.enums.SearchSimilarity;
import com.mongoplus.enums.SearchTokenization;
import com.mongoplus.enums.SearchVectorSimilarity;

import java.lang.annotation.*;

/**
 * MongoDB Search 静态字段映射配置，对应 mappings.fields 中的一个字段定义。
 * <p>同一字段可重复声明不同类型；相同索引名称、字段名称的定义组成映射数组。</p>
 * <p>静态映射覆盖该字段的动态映射，不会自动叠加动态类型。数组按元素类型配置，不存在 array 类型。</p>
 * <p>类型专属选项只能用于对应类型，解析时不得将其他类型的默认选项写入定义。
 * 空字符串和数值 -1 表示省略该选项，不是发送给 MongoDB 的值。</p>
 * <p>本注解声明配置契约，搜索索引解析及创建能力由对应解析器提供。</p>
 *
 * @see <a href="https://www.mongodb.com/docs/search/indexes/define-field-mappings/">字段映射</a>
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(MongoSearchFields.class)
public @interface MongoSearchField {

    /**
     * 索引名称
     * <p>用于区分所属搜索索引，不属于字段映射 JSON 的属性。</p>
     * <p>实体类只有一个 {@link MongoSearchIndex} 时，留空自动归属该索引；有多个时必须填写。
     * 类上声明了索引时，显式名称必须匹配其中一个索引；空索引名可用 default 引用。
     * 未声明类级索引时，保留按名称分组创建默认配置的行为。</p>
     * <p>在 {@link MongoSearchIndex#fields()} 中配置时应留空，由外层索引确定归属。</p>
     * @author anwen
     */
    String name() default "default";

    /**
     * 字段名称
     * <p>标注在实体字段上时，空值表示使用该字段映射后的 MongoDB 字段名。</p>
     * <p>如果此注解写在{@link MongoSearchIndex#fields()}中, 则此字段必填</p>
     * <p>显式名称使用实际 MongoDB 字段名，不能以 $ 开头。
     * 嵌套字段通过父字段的 {@link #fields()} 配置，不能使用点路径代替嵌套映射。</p>
     * @author anwen
     */
    String fieldName() default "";

    /**
     * 搜索字段类型，默认 string，不根据 Java 字段类型推断。
     * <p>例如数值字段应显式指定 NUMBER，对象数组应根据查询语义选择 DOCUMENT 或 EMBEDDED_DOCUMENTS。</p>
     */
    SearchFieldType type() default SearchFieldType.STRING;

    /**
     * 排除字段，对应 {@code "字段名": []}，可用于覆盖动态映射。
     * <p>为 true 时只使用 name、fieldName，不输出 type 或其他选项；
     * 同一索引的同一字段不能同时声明排除和其他映射。</p>
     */
    boolean exclude() default false;

    /**
     * document、embeddedDocuments：是否启用嵌套字段动态映射，默认 false。
     * <p>未启用动态映射且未指定 dynamicTypeSet 时，必须通过 fields 指定嵌套字段。</p>
     */
    boolean dynamic() default false;

    /**
     * document、embeddedDocuments：动态类型集名称，对应 dynamic.typeSet。
     * <p>引用 {@link MongoSearchIndex#typeSets()} 中的名称；指定时 dynamic 应保持 false，
     * 输出对象形式而不是布尔形式。</p>
     */
    String dynamicTypeSet() default "";

    /**
     * document、embeddedDocuments：嵌套字段映射 JSON 对象，空字符串表示未配置。
     * <p>使用实际 MongoDB 字段名；对象值可为单个定义、多个定义的数组或用于排除字段的空数组。</p>
     * <p>例：{@code {"title":{"type":"string"},"items":{"type":"document","dynamic":true}}}</p>
     * <p>Java 注解不允许循环成员类型，因此使用 JSON 表达任意深度的嵌套映射。</p>
     */
    String fields() default "";

    /**
     * string、autocomplete：索引分析器，DEFAULT 表示省略，由 MongoDB 决定继承及默认行为。
     * <p>不得输出 lucene.default。autocomplete 不支持 lucene.kuromoji。</p>
     */
    Analyzer analyzer() default Analyzer.DEFAULT;

    /**
     * string、autocomplete：自定义索引分析器名称，引用 {@link MongoSearchIndex#analyzers()}。
     * <p>指定时 analyzer 应保持 DEFAULT。autocomplete 不支持使用 nGram、edgeGram tokenizer，
     * 以及 daitchMokotoffSoundex、nGram、edgeGram、shingle token filter 的自定义分析器。</p>
     */
    String customAnalyzer() default "";

    /**
     * string：查询分析器，DEFAULT 表示省略，不得输出 lucene.default。
     * <p>未指定时依次使用字段 analyzer、索引 searchAnalyzer、索引 analyzer、lucene.standard。</p>
     */
    Analyzer searchAnalyzer() default Analyzer.DEFAULT;

    /**
     * string：自定义查询分析器名称，引用 {@link MongoSearchIndex#analyzers()}。
     * <p>指定时 searchAnalyzer 应保持 DEFAULT。</p>
     */
    String customSearchAnalyzer() default "";

    /**
     * string：索引保存的信息量，默认 offsets。
     * <p>phrase、span 至少需要 positions；highlight 需要 offsets 且 store=true。</p>
     */
    SearchIndexOptions indexOptions() default SearchIndexOptions.OFFSETS;

    /**
     * string：是否在搜索索引中保存原始文本，默认 true；highlight 要求 true。
     * <p>与 storedSource 的源文档存储配置不同。</p>
     */
    boolean store() default true;

    /**
     * string：允许索引的字段值最大字符数，超过时不索引整个值；-1 表示未指定。
     */
    int ignoreAbove() default -1;

    /**
     * string：是否在评分时考虑字段长度，默认 include。
     */
    SearchNorms norms() default SearchNorms.INCLUDE;

    /**
     * string、autocomplete：文本评分算法，对应对象 {@code similarity: {"type":"bm25"}}。
     * <p>向量字段使用 {@link #vectorSimilarity()}，不能将文本算法写成向量 similarity 字符串。</p>
     */
    SearchSimilarity similarity() default SearchSimilarity.NONE;

    /**
     * string：备用字符串映射 JSON 对象，键为查询时使用的 multi 名称。
     * <p>例：{@code {"english":{"type":"string","analyzer":"lucene.english"}}}</p>
     * <p>用于同一字符串的多分析器配置；不同字段类型应使用重复注解，不使用 multi。</p>
     */
    String multi() default "";

    /**
     * autocomplete：分词策略，默认 edgeGram。
     */
    SearchTokenization tokenization() default SearchTokenization.NONE;

    /**
     * autocomplete：最小 gram 长度，默认 2，必须为正数且不大于 maxGrams。
     */
    int minGrams() default 2;

    /**
     * autocomplete：最大 gram 长度，默认 15。
     * <p>较大值会增加索引开销，超过该长度的查询词元会被截断。</p>
     */
    int maxGrams() default 15;

    /**
     * autocomplete：是否折叠变音符号，默认 true，例如允许 cafe 匹配 café。
     */
    boolean foldDiacritics() default true;

    /**
     * token：字符串规范化方式，默认 none；lowercase 用于忽略大小写。
     */
    SearchNormalizer normalizer() default SearchNormalizer.NONE;

    /**
     * number、旧 numberFacet：数值表示方式，默认 double。
     * <p>int64 可保留大整数精度，但会将 double 四舍五入为整数，且不能索引超出范围的大 double。</p>
     */
    SearchRepresentation representation() default SearchRepresentation.DOUBLE;

    /**
     * number、旧 numberFacet：是否索引 int32、int64，默认 true。
     * <p>与 indexDoubles 至少一个为 true。</p>
     */
    boolean indexIntegers() default true;

    /**
     * number、旧 numberFacet：是否索引 double，默认 true。
     * <p>与 indexIntegers 至少一个为 true。</p>
     */
    boolean indexDoubles() default true;

    /**
     * geo：是否索引线、多边形等形状，默认 false，仅索引点。
     */
    boolean indexShapes() default false;

    /**
     * embeddedDocuments：源字段存储配置，JSON 布尔值或 include/exclude 对象。
     * <p>空字符串表示省略，MongoDB 默认 false；include 和 exclude 不能同时指定。
     * 路径相对于当前嵌套文档，例：{@code {"include":["title"]}}。</p>
     */
    String storedSource() default "";

    /**
     * vector：向量维数，使用该类型时必须指定 1～8192；-1 表示未配置。
     * <p>binary 自动量化或 int1 向量要求维数为 8 的倍数；旧 knnVector 使用 dimensions。</p>
     */
    int numDimensions() default -1;

    /**
     * 旧 knnVector：向量维数，使用该类型时必须指定 1～8192；-1 表示未配置。
     * @deprecated 新定义使用 VECTOR 和 numDimensions。
     */
    @Deprecated
    int dimensions() default -1;

    /**
     * vector、旧 knnVector：向量相似度，对应 similarity 字符串。
     * <p>DEFAULT 表示未配置，不能输出为空字符串；向量映射必须显式选择算法。</p>
     */
    SearchVectorSimilarity vectorSimilarity() default SearchVectorSimilarity.DEFAULT;

    /**
     * vector：自动量化方式，默认 none。自动量化仅用于 float/double 向量。
     * <p>预量化输入应使用 none；binary 要求 numDimensions 为 8 的倍数。</p>
     */
    SearchQuantization quantization() default SearchQuantization.EMPTY;

    /**
     * vector：索引结构，默认 hnsw；flat 使用平面索引，不配置 hnswOptions。
     */
    SearchIndexingMethod indexingMethod() default SearchIndexingMethod.NONE;

    /**
     * vector：HNSW 构建参数 JSON 对象，空字符串表示使用服务端默认值。
     * <p>仅用于 hnsw；maxEdges 范围 16～64，默认 16；
     * numEdgeCandidates 范围 100～3200，默认 100。</p>
     * <p>例：{@code {"maxEdges":32,"numEdgeCandidates":200}}。
     * 此选项在官方文档中标为 Preview，需核对目标部署的支持情况。</p>
     */
    String hnswOptions() default "";

}
