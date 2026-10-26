package com.mongoplus.annotation.index.search;

import com.mongoplus.enums.IndexDirection;
import com.mongoplus.enums.SearchSortNoData;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MongoDB Search 索引预排序中的单个字段配置。
 * <p>可用于 {@link MongoSearchIndex#sort()} 或直接标注在实体字段上。
 * 仅声明排序，不替代 {@link MongoSearchField} 的静态字段映射。</p>
 * <p>同一字段可为不同索引重复声明；同一索引内不能重复声明相同排序路径。</p>
 *
 * @see <a href="https://www.mongodb.com/docs/search/indexes/sort/">Sorted Index</a>
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(MongoSearchSorts.class)
public @interface MongoSearchSort {

    /**
     * 所属搜索索引名称，与 MongoSearchIndex.name 匹配。
     * <p>类上只有一个索引时可留空，自动归属该索引；多个索引时必须填写，且必须匹配已声明索引。
     * 空索引名可用 default 引用；未声明类级索引时，按名称分组使用默认配置。</p>
     * <p>在 MongoSearchIndex.sort 中使用时应留空，由外层索引确定归属；
     * 此参数不写入排序 JSON。</p>
     */
    String name() default "";

    /**
     * 实际 MongoDB 排序字段路径。
     * <p>在 MongoSearchIndex.sort 中使用时必填；标注在实体字段上时，
     * 空值表示使用该字段映射后的 MongoDB 字段名。</p>
     * <p>嵌套路径允许点号，但所有父字段必须具有静态 document 映射。</p>
     */
    String fieldName() default "";

    /**
     * 排序方向，默认升序；ASC 对应 1，DESC 对应 -1。
     */
    IndexDirection order() default IndexDirection.ASC;

    /**
     * null、缺失值的比较位置，DEFAULT 表示省略 noData，使用服务端默认行为。
     * <p>LOWEST、HIGHEST 分别将其视为最低值、最高值，最终位置取决于升降序；
     * 例如 DESC 配合 LOWEST 将其放在末尾。</p>
     */
    SearchSortNoData noData() default SearchSortNoData.DEFAULT;

    /**
     * 实体字段上的排序优先级，数值越小越靠前，默认 0；不写入排序 JSON。
     * <p>同优先级按解析后的 MongoDB 字段路径字典序排列，避免依赖反射返回字段的顺序。</p>
     * <p>在 MongoSearchIndex.sort 数组内使用时忽略此参数，以数组顺序为准；
     * 两种声明并用时先放数组中的配置，再追加按优先级排列的字段配置。</p>
     */
    int priority() default 0;
}
