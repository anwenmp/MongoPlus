package com.mongoplus.annotation.index.search;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MongoDB Search 源文档存储配置，用于 {@link MongoSearchIndex#storedSource()}。
 * <p>仅归属包含它的搜索索引，多个索引分别配置，彼此不会合并或继承。</p>
 * <p>存储源字段不等于为字段建立索引；字段参与搜索时仍需单独配置映射。
 * 查询时通过 returnStoredSource 请求返回存储的源字段。</p>
 * <p>仅适用于 MongoDB 7.0 及以上版本。本注解仅声明配置，校验和定义转换由搜索解析器负责。</p>
 *
 * @see <a href="https://www.mongodb.com/docs/search/indexes/stored-source-definition/">Stored Source</a>
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MongoSearchStoredSource {

    /**
     * 是否存储完整源文档，对应 storedSource 的布尔形式，默认 false。
     * <p>include、exclude 均为空时，true 表示存储所有字段，false 表示不存储源文档。
     * 两者之一非空时使用对象形式，此值必须保持 false。</p>
     * <p>索引定义包含 vector 类型字段时不能设为 true，应通过 include 选择非向量字段，
     * 或通过 exclude 排除向量字段。存储完整文档会增加索引和查询开销。</p>
     */
    boolean value() default false;

    /**
     * 仅存储指定源字段，对应 storedSource.include；默认不配置。
     * <p>使用实际 MongoDB 字段名或点分隔路径，例如 title、awards.wins；不执行 $field 转换。
     * MongoDB 还会默认存储 _id。若仅需 _id，可显式配置 include={"_id"}。</p>
     * <p>不能与 exclude 或 value=true 同时配置；路径不能为 null、空字符串或空白字符串。
     * 索引包含 vector 类型时，不能选择需要排除的向量字段或包含该字段的整个父对象。</p>
     */
    String[] include() default {};

    /**
     * 存储除指定字段外的其他源字段，对应 storedSource.exclude；默认不配置。
     * <p>使用实际 MongoDB 字段名或点分隔路径；可显式排除 _id。
     * 不能与 include 或 value=true 同时配置，路径不能为空白。</p>
     * <p>索引定义包含 vector 类型字段时，必须排除这些向量字段或其整个父对象。</p>
     */
    String[] exclude() default {};
}
