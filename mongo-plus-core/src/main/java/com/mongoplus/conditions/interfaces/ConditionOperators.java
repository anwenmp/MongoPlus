package com.mongoplus.conditions.interfaces;

import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.enums.AggregateEnum;
import com.mongoplus.enums.CommonOperators;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongoplus.enums.CommonOperators.*;

/**
 * 条件构造
 *
 * @author anwen
 */
public class ConditionOperators {

    /**
     * $cond操作符
     * @author anwen
     */
    public static Bson cond(Object ifValue, Object thenValue, Object elseValue) {
        return new Document(COND.getOperator(),
                new Document() {{
                    put("if", ifValue);
                    put("then", thenValue);
                    put("else", elseValue);
                }});
    }

    /**
     * $cond操作符，数组写法
     * @author anwen
     */
    public static Bson condArray(Object ifValue, Object thenValue, Object elseValue) {
        return new Document(COND.getOperator(), new ArrayList<Object>(){{
            add(ifValue);
            add(thenValue);
            add(elseValue);
        }});
    }

    /**
     * $cond操作符
     * @author anwen
     */
    public static Bson cond(String ifCondition, Collection<?> ifValue, Object thenValue, Object elseValue){
        return cond(new Document(ifCondition.startsWith("$") ? ifCondition : "$" + ifCondition, ifValue),thenValue,elseValue);
    }

    /**
     * $cond操作符，数组写法
     * @author anwen
     */
    public static Bson condArray(String ifCondition, Collection<?> ifValue, Object thenValue, Object elseValue){
        return cond(new Document(ifCondition.startsWith("$") ? ifCondition : "$" + ifCondition, ifValue),thenValue,elseValue);
    }

    /**
     * $multiply操作符
     * @author anwen
     */
    public static Bson multiply(Object... values) {
        return multiply(new ArrayList<>(Arrays.asList(values)));
    }

    /**
     * $multiply操作符
     * @author anwen
     */
    public static Bson multiply(SFunction<?,?>... values) {
        return multiply(Arrays.stream(values).map(SFunction::getFieldNameLineOption).collect(Collectors.toList()));
    }

    /**
     * $multiply操作符
     * @author anwen
     */
    public static Bson multiply(Collection<?> values) {
        return new Document("$multiply", values);
    }

    /**
     * $multiply操作符
     * @author anwen
     */
    public static Bson multiplyLambda(Collection<SFunction<?,?>> values) {
        return new Document(MULTIPLY.getOperator(), values.stream().map(SFunction::getFieldNameLineOption).collect(Collectors.toList()));
    }
    
    /**
     * $dateToString操作符
     * @author anwen
     */
    public static Bson dateToString(String date){
        return dateToString(null,date);
    }

    /**
     * $dateToString操作符
     * @author anwen
     */
    public static <T> Bson dateToString(SFunction<T,?> date){
        return dateToString(null,date.getFieldNameLineOption());
    }

    /**
     * $dateToString操作符
     * @author anwen
     */
    public static Bson dateToString(String format,String date){
        return dateToString(format,date,null);
    }

    /**
     * $dateToString操作符
     * @author anwen
     */
    public static <T> Bson dateToString(String format,SFunction<T,?> date){
        return dateToString(format,date.getFieldNameLineOption(),null);
    }

    /**
     * $dateToString操作符
     * @author anwen
     */
    public static Bson dateToString(String format,String date,String timezone){
        return dateToString(format,date,timezone,null);
    }

    /**
     * $dateToString操作符
     * @author anwen
     */
    public static <T> Bson dateToString(String format,SFunction<T,?> date,String timezone){
        return dateToString(format,date.getFieldNameLineOption(),timezone,null);
    }

    /**
     * $dateToString操作符
     * @author anwen
     */
    public static Bson dateToString(String format,String date,String timezone,Object onNull){
        return new Document(DATE_TO_STRING.getOperator(),new MongoPlusDocument(){{
            putIsNotNull("date",date);
            putIsNotNull("format",format);
            putIsNotNull("timezone",timezone);
            putIsNotNull("onNull",onNull);
        }});
    }

    /**
     * $dateToString操作符
     * @author anwen
     */
    public static <T> Bson dateToString(String format,SFunction<T,?> date,String timezone,Object onNull){
        return dateToString(format,date.getFieldNameLineOption(),timezone,onNull);
    }

    /**
     * $mergeObjects操作符
     * @param value 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Document mergeObjects(String value){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),value);
    }

    /**
     * $mergeObjects操作符
     * @param value 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Document mergeObjects(SFunction<T,?> value){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),value.getFieldNameLine());
    }

    /**
     * $mergeObjects操作符
     * @param value 值，带$符
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Document mergeObjectsOption(SFunction<T,?> value){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),value.getFieldNameLineOption());
    }

    /**
     * $mergeObjects操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Document mergeObjects(Collection<?> values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),values);
    }

    /**
     * $mergeObjects操作符
     * @param functions 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Document mergeObjectsLambda(Collection<? extends SFunction<T,?>> functions){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),functions.stream()
                .map(SFunction::getFieldNameLine).collect(Collectors.toList()));
    }

    /**
     * $mergeObjects操作符
     * @param functions 值，带$符
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Document mergeObjectsLambdaOption(Collection<? extends SFunction<T,?>> functions){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),functions.stream()
                .map(SFunction::getFieldNameLineOption).collect(Collectors.toList()));
    }

    /**
     * $mergeObjects操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Document mergeObjects(Object... values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), Arrays.stream(values).collect(Collectors.toList()));
    }

    /**
     * $each操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson each(Object... values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), Arrays.stream(values).collect(Collectors.toList()));
    }

    /**
     * $each and $position操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson eachPosition(Number position,Object... values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), Arrays.stream(values).collect(Collectors.toList()))
                .append(AggregateEnum.POSITION.getValue(), position);
    }

    /**
     * $each and $slice操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson eachSlice(Number slice,Object... values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), Arrays.stream(values).collect(Collectors.toList()))
                .append(AggregateEnum.SLICE.getValue(), slice);
    }

    /**
     * $each and $sort操作符
     * @param sort sort
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson eachSort(Object sort,Object... values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),Arrays.stream(values).collect(Collectors.toList()))
                .append(AggregateEnum.SORT.getValue(), sort);
    }

    /**
     * $each操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson each(Collection<?> values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), values);
    }

    /**
     * $each and $position操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson eachPosition(Number position,Collection<?> values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), values)
                .append(AggregateEnum.POSITION.getValue(), position);
    }

    /**
     * $each and $slice操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson eachSlice(Number slice,Collection<?> values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), values)
                .append(AggregateEnum.SLICE.getValue(), slice);
    }

    /**
     * $each and $sort操作符
     * @param sort sort
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson eachSort(Object sort,Collection<?> values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),values)
                .append(AggregateEnum.SORT.getValue(), sort);
    }

    /**
     * $abs操作符
     * @author anwen
     */
    public static Bson abs(Number value){
        return new Document(CommonOperators.ABS.getOperator(),value);
    }

    /**
     * $toDate操作符
     * @param expression 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <TExpression> Bson toDate(TExpression expression){
        return new Document(TO_DATE.getOperator(),expression);
    }

    /**
     * $toDate操作符
     * @param field 引用的字段
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson toDate(SFunction<?,?> field){
        return new Document(TO_DATE.getOperator(),field.getFieldNameLineOption());
    }
    
    /**
     * $dateFromString操作符
     * @param dateString 要转换为日期对象的日期/时间字符串
     * @param format 可选。dateString 的日期格式规范
     * @param timezone 可选。用于设置日期格式的时区
     * @param onError 可选。如果 $dateFromString 在解析给定的 dateString 时遇到错误，它会输出所提供的onError表达式的结果值
     * @param onNull 可选。如果为 $dateFromString 提供的 dateString 为 null 或缺失，则会输出所提供的onNull 表达式的结果值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson dateFromString(
            Object dateString, Object format, Object timezone, Object onError, Object onNull){
        MongoPlusDocument dateFromString = new MongoPlusDocument();
        dateFromString.putIsNotNull("dateString",dateString);
        dateFromString.putIsNotNull("format",format);
        dateFromString.putIsNotNull("timezone",timezone);
        dateFromString.putIsNotNull("onError",onError);
        dateFromString.putIsNotNull("onNull",onNull);
        return new Document(DATE_FROM_STRING.getOperator(),dateFromString);
    }

    /**
     * $dateFromString操作符
     * @param dateString 要转换为日期对象的日期/时间字符串
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson dateFromString(Object dateString){
        return dateFromString(dateString,null,null,null,null);
    }

    /**
     * $dateFromString操作符
     * @param field 文档字段
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson dateFromString(SFunction<?,?> field){
        return dateFromString(field.getFieldNameLineOption());
    }

    /**
     * $dateFromString操作符
     * @param dateString 要转换为日期对象的日期/时间字符串 文档字段
     * @param timezone 可选。用于设置日期格式的时区
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson dateFromString(SFunction<?,?> dateString,Object timezone){
        return dateFromString(dateString.getFieldNameLineOption());
    }

    /**
     * $dateFromString操作符
     * @param dateString 要转换为日期对象的日期/时间字符串 文档字段
     * @param timezone 可选。用于设置日期格式的时区
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson dateFromString(SFunction<?,?> dateString,SFunction<?,?> timezone){
        return dateFromString(dateString,timezone.getFieldNameLineOption());
    }

    /**
     * $toBool操作符
     * @param expression 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <TExpression> Bson toBool(TExpression expression){
        return new Document(TO_BOOL.getOperator(),expression);
    }

    /**
     * $toBool操作符
     * @param field 字段
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Bson toBool(SFunction<T,Object> field){
        return toBool(field.getFieldNameLineOption());
    }

    /**
     * $toDecimal操作符
     * @param expression 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <TExpression> Bson toDecimal(TExpression expression){
        return new Document(TO_DECIMAL.getOperator(),expression);
    }

    /**
     * $toDecimal操作符
     * @param field 字段
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Bson toDecimal(SFunction<T,Object> field){
        return toDecimal(field.getFieldNameLineOption());
    }

    /**
     * $toDouble操作符
     * @param expression 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <TExpression> Bson toDouble(TExpression expression){
        return new Document(TO_DOUBLE.getOperator(),expression);
    }

    /**
     * $toHashedIndexKey操作符
     * @param key string to hash
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Bson toHashedIndexKey(String key){
        return new Document(TO_HASHED_INDEX_KEY.getOperator(),key);
    }

    /**
     * $toHashedIndexKey操作符
     * @param field 字段
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Bson toHashedIndexKey(SFunction<T,Object> field){
        return toHashedIndexKey(field.getFieldNameLineOption());
    }

    /**
     * $toInt操作符
     * @param expression 表达式
     * @return {@link Bson}
     * @author anwen
     */
    public static <TExpression> Bson toInt(TExpression expression){
        return new Document(TO_INT.getOperator(),expression);
    }

    /**
     * $toInt操作符
     * @param field 字段
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Bson toInt(SFunction<T,Object> field){
        return toInt(field.getFieldNameLineOption());
    }

    /**
     * $toLong操作符
     * @param expression 表达式
     * @return {@link Bson}
     * @author anwen
     */
    public static <TExpression> Bson toLong(TExpression expression){
        return new Document(TO_LONG.getOperator(),expression);
    }

    /**
     * $toLong操作符
     * @param field 字段
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Bson toLong(SFunction<T,Object> field){
        return toLong(field.getFieldNameLineOption());
    }

    /**
     * $toObjectId操作符
     * @param expression 表达式
     * @return {@link Bson}
     * @author anwen
     */
    public static <TExpression> Bson toObjectId(TExpression expression){
        return new Document(TO_OBJECT_ID.getOperator(),expression);
    }

    /**
     * $toObjectId操作符
     * @param field 字段
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Bson toObjectId(SFunction<T,Object> field){
        return toObjectId(field.getFieldNameLineOption());
    }

    /**
     * $toString操作符
     * @param expression 表达式
     * @return {@link Bson}
     * @author anwen
     */
    public static <TExpression> Bson toString(TExpression expression){
        return new Document(TO_STRING.getOperator(),expression);
    }

    /**
     * $toString操作符
     * @param field 字段
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Bson toString(SFunction<T,Object> field){
        return toString(field.getFieldNameLineOption());
    }

    /**
     * $substrBytes操作符
     * @param field 字段
     * @param index 指示子字符串的点
     * @param count 字节数
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Bson substrBytes(SFunction<T,?> field,Number index,Number count){
        return new Document(SUBSTR_BYTES.getOperator(),Arrays.asList(field.getFieldNameLineOption(),index,count));
    }

    /**
     * $substrBytes操作符
     * @param expression 表达式
     * @param index 指示子字符串的点
     * @param count 字节数
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <TExpression> Bson substrBytes(TExpression expression,Number index,Number count){
        return new Document(SUBSTR_BYTES.getOperator(),Arrays.asList(expression,index,count));
    }

    /**
     * $substrBytes操作符
     * @param expression 表达式
     * @param index 指示子字符串的点
     * @param count 字节数
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <TExpression> Bson substrBytes(TExpression expression,Object index,Object count){
        return new Document(SUBSTR_BYTES.getOperator(),Arrays.asList(expression,index,count));
    }

    /**
     * $substrBytes操作符
     * @param field 字段
     * @param index 指示子字符串的点
     * @param count 字节数
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static <T> Bson substrBytes(SFunction<T,?> field,Object index,Object count){
        return new Document(SUBSTR_BYTES.getOperator(),Arrays.asList(field.getFieldNameLineOption(),index,count));
    }

    /**
     * $ifNull操作符
     * @param inputExpressions 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson ifNull(List<?> inputExpressions){
        return new Document(IF_NULL.getOperator(),inputExpressions);
    }

    /**
     * $ifNull操作符
     * @param inputExpressions 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson ifNull(Object... inputExpressions){
        return ifNull(Arrays.asList(inputExpressions));
    }


    /**
     * 不作为累加器的$sum
     * @param expressions 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    public static <TExpression> Bson sum(TExpression... expressions){
        return new Document(SUM.getOperator(),Arrays.asList(expressions));
    }

    /**
     * 不作为累加器的$sum
     * @param expressions 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    public static <T> Bson sum(SFunction<T,?>... expressions){
        return new Document(SUM.getOperator(),
                Arrays.stream(expressions).map(SFunction::getFieldNameLineOption).collect(Collectors.toList()));
    }

    /**
     * 不作为累加器的$sum
     * @param expressions 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson sum(List<?> expressions){
        return new Document(SUM.getOperator(),expressions);
    }

    /**
     * $add操作符
     * @param expressions 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    public static <TExpression> Bson add(TExpression... expressions){
        return new Document(ADD.getOperator(),Arrays.asList(expressions));
    }

    /**
     * $add操作符
     * @param expressions 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    public static <T> Bson add(SFunction<T,?>... expressions){
        return new Document(ADD.getOperator(),
                Arrays.stream(expressions).map(SFunction::getFieldNameLineOption).collect(Collectors.toList()));
    }

    /**
     * $add操作符
     * @param expressions 表达式
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public static Bson add(List<?> expressions){
        return new Document(ADD.getOperator(),expressions);
    }

}
