package com.mongoplus.constant;

/**
 * 一些变量常量
 * @author anwen
 */
public class MongoVariableConstant {

    /**
     * 返回当前日期时间值的变量。NOW 会为部署的所有成员返回相同的值，并在聚合管道的所有阶段保持不变
     */
    public static final String NOW = "$$NOW";

    /**
     * 返回当前时间戳值的变量。
     * <p>{@code CLUSTER_TIME}仅适用于副本集和分片的集群。</p>
     * <p>{@code CLUSTER_TIME}会为部署的所有节点返回相同的值，并在管道的所有阶段保持不变</p>
     */
    public static final String CLUSTER_TIME = "$$CLUSTER_TIME";

    /**
     * 引用根文档，即当前正在聚合管道阶段处理的顶层文档
     */
    public static final String ROOT = "$$ROOT";

    /**
     * 引用聚合管道阶段正在处理的字段路径（Field Path）的起始位置。除非另有说明，否则所有阶段都以 {@code CURRENT} 开头，与 {@code ROOT} 相同。
     * {@code CURRENT}是可修改的。但是，由于{@code {3}lt;field>}{3}lt;field> 等效于{@code $CURRENT.<field>} ，
     * 因此重新绑定{@code CURRENT} 会更改{@code $} 访问的含义。
     */
    public static final String CURRENT = "$$CURRENT";

    /**
     * 一个求值为缺失值的变量。允许排除 {@code $addFields} 和 {@code $project} 阶段的字段。
     */
    public static final String REMOVE = "$$REMOVE";

    /**
     * {@code $redact}表达式的允许结果之一。
     */
    public static final String DESCEND = "$$DESCEND";

    /**
     * {@code $redact}表达式的允许结果之一。
     */
    public static final String PRUNE = "$$PRUNE";

    /**
     * {@code $redact}表达式的允许结果之一。
     */
    public static final String KEEP = "$$KEEP";

    /**
     * 存储Atlas Search查询元数据结果的变量。在所有支持的聚合管道阶段中，设立为变量{@code $SEARCH_META} 的字段会返回查询的元数据结果。
     */
    public static final String SEARCH_META = "$$SEARCH_META";

    /**
     * 返回分配给当前用户的角色。
     */
    public static final String USER_ROLES = "$$USER_ROLES";

}
