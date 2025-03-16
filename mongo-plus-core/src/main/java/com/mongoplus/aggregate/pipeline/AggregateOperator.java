package com.mongoplus.aggregate.pipeline;

import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

import static com.mongoplus.enums.CommonOperators.*;

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


    /**
     * $dateTrunc操作符
     *
     * @param field       字段
     *                    <br>
     * @param unit        单位可以是能被解析为下列值的表达式：year、quarter、week、month、day、hour、minute、second、millisecond，与binSize一起指定时间段
     *                    <br>
     * @param binSize     时间数值，以表达式形式指定，必须是非零正数。默认值为 1。
     *                    <br>
     * @param startOfWeek 指定周开始的天，只有当单位是周时可用，缺省为Sunday，startOfWeek可以是一个表达式，但必须能够被解析为：monday (或 mon)、tuesday (或 tue)、wednesday (或 wed)、thursday (或 thu)、friday (或 fri)、saturday (或 sat)、sunday (或 sun)
     *                    <br>
     * @param timezone    执行操作的时区，<tzExpression>必须是能被解析为奥尔森时区标识符格式的字符串或UTC偏移量，如果timezone不指定，返回值显示为UTC
     */
    public static Bson dateTrunc(SFunction<?, ?> field, String unit, Integer binSize, String startOfWeek, String timezone) {
        return dateTrunc(field.getFieldNameLineOption(), unit, binSize, startOfWeek, timezone);
    }

    public static Bson dateTrunc(String field, String unit, Integer binSize, String startOfWeek, String timezone) {
        return new Document(DATE_TRUNC.getOperator(), new MongoPlusDocument() {{
            putIsNotNull("date", field);
            putIsNotNull("unit", unit);
            putIsNotNull("binSize", binSize);
            putIsNotNull("timezone", timezone);
            putIsNotNull("startOfWeek", startOfWeek);
        }});
    }

    public static Bson dateTrunc(String field, String unit) {
        return dateTrunc(field, unit, null, null, null);
    }

    public static Bson dateTrunc(SFunction<?, ?> field, String unit) {
        return dateTrunc(field, unit, null, null, null);
    }
}
