package com.mongoplus.annotation.index.vector;

import java.lang.annotation.*;

/** Vector Search 源字段存储对象；include 和 exclude 互斥，均为空表示省略 storedSource。 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MongoVectorStoredSource {
    /** 仅存储这些字段（_id 默认保留）；支持实际 MongoDB 路径或 $实体属性路径。 */
    String[] include() default {};

    /** 存储除这些字段之外的源字段；支持实际 MongoDB 路径或 $实体属性路径。 */
    String[] exclude() default {};
}
