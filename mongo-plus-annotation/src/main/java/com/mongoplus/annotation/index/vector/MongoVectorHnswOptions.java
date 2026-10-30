package com.mongoplus.annotation.index.vector;

import java.lang.annotation.*;

/** HNSW 可选参数；-1 表示不发送，由服务端应用默认值。 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MongoVectorHnswOptions {
    /** 最大连接数，范围 16..64；未指定时服务端默认 16。 */
    int maxEdges() default -1;

    /** 构图候选节点数，范围 100..3200；未指定时服务端默认 100。 */
    int numEdgeCandidates() default -1;
}
