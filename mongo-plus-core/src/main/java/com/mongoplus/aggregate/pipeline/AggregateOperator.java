package com.mongoplus.aggregate.pipeline;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

import static com.mongoplus.enums.CommonOperators.CONCAT;
import static com.mongoplus.enums.CommonOperators.CONCAT_ARRAYS;

/**
 * 聚合操作符
 *
 * @author anwen
 */
public class AggregateOperator {

    /**
     * $concatArrays阶段
     * @param list 多个数组
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson concatArrays(List<?>... list) {
        return new Document(CONCAT_ARRAYS.getOperator(), Arrays.asList(list));
    }

    /**
     * $concat操作符
     * @author anwen
     */
    public static Bson concat(Object... expression) {
        return concat(Arrays.asList(expression));
    }

    /**
     * $concat操作符
     * @author anwen
     */
    public static Bson concat(List<?> expressions) {
        return new Document(CONCAT.getOperator(), expressions);
    }

}
