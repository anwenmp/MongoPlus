package com.mongoplus.annotation.index.search;

import com.mongoplus.enums.Analyzer;

import java.lang.annotation.*;

/**
 * MongoDB Search 搜索索引配置
 * <p>同一实体类可重复声明多个索引，名称不能重复；字段、排序、源文档存储和同义词配置按索引隔离。</p>
 * <p>字段、排序、源文档存储和同义词使用嵌套注解；自定义分析器和动态类型集使用 JSON 对象字符串数组。</p>
 * @see <a href="https://www.mongodb.com/docs/search/indexes/index-definitions/">MongoDB Search 索引定义</a>
 * @author anwen
 */
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MongoSearchIndexes.class)
public @interface MongoSearchIndex {

    /**
     * 索引名称，默认 default；显式为空时不向 Driver 指定名称，服务端仍使用 default。
     * <p>同一类中的索引名称必须唯一，空名称与 default 视为同名。</p>
     * <p>类上只有一个索引时，实体字段注解的 name 可省略；多个索引时必须填写所属索引名称。</p>
     * <p>索引名称属于创建索引的参数，不属于索引定义中的 definition。</p>
     * @author anwen
     */
    String name() default "default";

    /**
     * 指定在建立索引时应用于字符串字段的分析器
     * <p>字段未单独指定分析器时使用该配置，字段级配置优先。</p>
     * <p>{@link Analyzer#DEFAULT} 表示不显式指定，由 MongoDB 使用 lucene.standard，
     * 不应将 lucene.default 写入索引定义。</p>
     * @author anwen
     */
    Analyzer analyzer() default Analyzer.DEFAULT;

    /**
     * 静态字段映射，对应 mappings.fields
     * <p>可在此数组中配置，也可将 {@link MongoSearchField} 标注在实体字段上。</p>
     * <p>数组中的 fieldName 必填，name 留空；同名字段的多个定义组成映射数组。
     * 嵌套映射使用 {@link MongoSearchField#fields()}。</p>
     * @author anwen
     */
    MongoSearchField[] fields() default {};

    /**
     * 建立索引时的预排序配置，对应 definition.sort。
     * <p>可在此数组中配置，也可将 {@link MongoSearchSort} 标注在实体字段上。
     * 两处均未声明时省略 sort；数组中的 fieldName 必填，name 留空。</p>
     * <p>数组顺序决定排序优先级，忽略数组元素的 priority；
     * 字段注解按 priority 升序、同优先级按实际字段路径字典序排列，追加到数组配置之后。
     * 同一索引内重复的排序路径应报配置错误，不能覆盖或重复输出。</p>
     * <p>noData 未配置时输出字段对应的 1 或 -1；
     * 显式配置时输出包含 order、noData 的对象。</p>
     * <p>排序字段必须静态映射为 boolean、date、number、objectId、token 或 uuid；
     * 多类型映射至少包含其中一种。仅通过 dynamic 或 typeSet 映射的排序字段会导致索引构建失败。</p>
     * <p>嵌套路径可使用点号，但所有父字段必须具有静态 document 映射；
     * 仅有 embeddedDocuments 映射不能满足排序要求。不能使用 searchScore 元表达式。</p>
     * <p>每个索引仅配置一组 sort，可包含多个字段；查询仍需指定匹配此配置或其前缀的
     * $search.sort 才能利用预排序，可通过 explain 中的 usesIndexSort 核实。</p>
     * @see <a href="https://www.mongodb.com/docs/search/indexes/sort/">Sorted Index</a>
     * @author anwen
     */
    MongoSearchSort[] sort() default {};

    /**
     * 建立索引时使用的自定义分析器名称，为空时使用 {@link #analyzer()}
     * <p>名称需与 {@link #analyzers()} 中定义的 name 一致。</p>
     * <p>使用该配置时，{@link #analyzer()} 应保持为 {@link Analyzer#DEFAULT}。</p>
     * @author anwen
     */
    String customAnalyzer() default "";

    /**
     * 指定在搜索前应用于查询文本的分析器
     * <p>{@link Analyzer#DEFAULT} 表示不显式指定，默认使用索引的 analyzer；
     * 两者均未指定时使用 lucene.standard。</p>
     * <p>不应将 lucene.default 写入索引定义。</p>
     * @author anwen
     */
    Analyzer searchAnalyzer() default Analyzer.DEFAULT;

    /**
     * 查询文本使用的自定义分析器名称，为空时使用 {@link #searchAnalyzer()}
     * <p>名称需与 {@link #analyzers()} 中定义的 name 一致。</p>
     * <p>使用该配置时，{@link #searchAnalyzer()} 应保持为 {@link Analyzer#DEFAULT}。</p>
     * @author anwen
     */
    String customSearchAnalyzer() default "";

    /**
     * 自定义分析器定义，每个值为一个 JSON 对象，默认不配置
     * <p>每个对象必须包含 name 和 tokenizer，可选配置 charFilters、tokenFilters。</p>
     * <p>name 在索引内必须唯一，且不能以 lucene.、builtin. 或 mongodb. 开头。</p>
     * <p>例：{@code {"name":"myAnalyzer","tokenizer":{"type":"standard"}}}</p>
     * @author anwen
     */
    String[] analyzers() default {};

    /**
     * 是否动态映射字段，对应 mappings.dynamic，默认 false
     * <p>设为 true 时递归索引所有支持动态映射的字段类型；设为 false 时不动态索引字段。</p>
     * <p>未配置 {@link #dynamicTypeSet()} 且该值为 false 时，必须通过 {@link #fields()}
     * 或实体字段上的 {@link MongoSearchField} 指定静态索引字段。</p>
     * <p>{@link #fields()} 中显式指定的字段配置优先于动态映射。</p>
     * @author anwen
     */
    boolean dynamic() default false;

    /**
     * 动态映射使用的类型集名称，对应 mappings.dynamic.typeSet，默认不配置
     * <p>名称需与 {@link #typeSets()} 中定义的 name 一致，用于递归索引类型集指定的字段类型。</p>
     * <p>该配置使用 mappings.dynamic 的对象形式，与布尔形式互斥；使用时 {@link #dynamic()} 应保持默认值。</p>
     * @author anwen
     */
    String dynamicTypeSet() default "";

    /**
     * 索引分区数量，对应 definition.numPartitions，默认 1，仅支持 1、2、4。
     * <p>1 表示单分区；2、4 分别最多划分为两个、四个分区，仍是同一个搜索索引。
     * 使用额外分区需要独立部署的 Search Nodes；该配置不用于设置数据库分片数量。</p>
     * <p>每分区最多支持二十亿个索引对象；嵌套 embeddedDocuments 也会增加索引对象，
     * 容量不能仅按集合的顶层文档数估算。</p>
     * <p>配置或修改分区数量会触发索引重建；存在多个分区时不能移除所有 Search Nodes
     * 并切换到 mongod、mongot 同节点的部署模式。</p>
     * <p>分区查询由 MongoDB Search 分发并合并结果，查询级 concurrent 选项需在查询中另行设置。</p>
     * @see <a href="https://www.mongodb.com/docs/search/indexes/index-partition/">Index Partitions</a>
     * @author anwen
     */
    int numPartitions() default 1;

    /**
     * 源文档字段存储配置，对应 definition.storedSource，默认不存储源文档。
     * <p>通过嵌套注解配置布尔形式或 include/exclude 对象形式，三种方式互斥。</p>
     * <p>例：{@code @MongoSearchStoredSource(include = {"title", "publishedAt"})}，
     * 或 {@code @MongoSearchStoredSource(exclude = {"content"})}，全量存储使用 {@code @MongoSearchStoredSource(true)}。</p>
     * <p>存储字段不等于为字段建立索引，查询时可通过 returnStoredSource 返回存储的字段。</p>
     * <p>仅适用于 MongoDB 7.0 及以上版本；索引包含 vector 类型字段时，不能使用 true，需排除该字段。</p>
     * @author anwen
     */
    MongoSearchStoredSource storedSource() default @MongoSearchStoredSource;

    /**
     * 同义词映射定义，对应 definition.synonyms，空数组表示不配置。
     * <p>每个映射必须包含唯一且非空的 name、同数据库的 source.collection 和 analyzer。
     * 分析器配置及限制见 {@link MongoSearchSynonym}。</p>
     * <p>例：{@code @MongoSearchSynonym(name = "mySynonyms", sourceCollection = "synonyms")}。</p>
     * <p>免费 M0 集群中，一个索引最多配置一个映射，来源集合最多包含一万条文档。</p>
     * <p>Atlas Flex 同样使用单个映射；多映射需要 M10 或更高等级。
     * 本地解析器不识别部署等级，具体配额由服务端检查。</p>
     * <p>此配置不创建来源集合或写入同义词；查询需通过 text 或 phrase 引用映射名称。
     * 来源集合的合法变更由 MongoDB Search 异步更新到同义词映射。</p>
     * @see <a href="https://www.mongodb.com/docs/search/indexes/synonyms/">Synonym Mappings</a>
     * @author anwen
     */
    MongoSearchSynonym[] synonyms() default {};

    /**
     * 动态映射类型集定义，每个值为一个 JSON 对象，默认不配置
     * <p>每个对象必须包含 name 和 types，types 中每个对象必须通过 type 指定要动态索引的字段类型。</p>
     * <p>同一类型集不能重复定义相同 type；不能包含 document、embeddedDocuments、vector 或已废弃的类型。</p>
     * <p>通过 {@link #dynamicTypeSet()} 引用类型集，也可在 {@link #fields()} 的嵌套字段映射中引用。</p>
     * <p>例：{@code {"name":"myTypeSet","types":[{"type":"string"},{"type":"number"}]}}</p>
     * @author anwen
     */
    String[] typeSets() default {};

}
