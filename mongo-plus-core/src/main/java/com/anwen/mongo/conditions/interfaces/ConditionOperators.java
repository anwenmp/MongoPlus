package com.anwen.mongo.conditions.interfaces;

import com.anwen.mongo.bson.MongoPlusDocument;
import com.anwen.mongo.enums.AggregateEnum;
import com.anwen.mongo.enums.CommonOperators;
import com.anwen.mongo.support.SFunction;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 条件构造
 *
 * @author anwen
 */
public class ConditionOperators {

    /**
     * $cond操作符
     * @author anwen
     * @date 2024/8/23 11:11
     */
    public static Bson cond(Object ifValue, Object thenValue, Object elseValue) {
        return new Document("$cond",
                new Document() {{
                    put("if", ifValue);
                    put("then", thenValue);
                    put("else", elseValue);
                }});
    }

    /**
     * $cond操作符
     * @author anwen
     * @date 2024/8/23 11:11
     */
    public static Bson cond(String ifCondition, Collection<?> ifValue, Object thenValue, Object elseValue){
        return cond(new Document(ifCondition.startsWith("$") ? ifCondition : "$" + ifCondition, ifValue),thenValue,elseValue);
    }

    /**
     * $multiply操作符
     * @author anwen
     * @date 2024/8/23 11:13
     */
    public static Bson multiply(Object... values) {
        return multiply(new ArrayList<>(Arrays.asList(values)));
    }

    /**
     * $multiply操作符
     * @author anwen
     * @date 2024/8/23 11:13
     */
    public static Bson multiply(SFunction<?,?>... values) {
        return multiply(Arrays.stream(values).map(SFunction::getFieldNameLineOption).collect(Collectors.toList()));
    }

    /**
     * $multiply操作符
     * @author anwen
     * @date 2024/8/23 11:13
     */
    public static Bson multiply(Collection<?> values) {
        return new Document("$multiply", values);
    }

    /**
     * $multiply操作符
     * @author anwen
     * @date 2024/8/23 11:13
     */
    public static Bson multiplyLambda(Collection<SFunction<?,?>> values) {
        return new Document("$multiply", values.stream().map(SFunction::getFieldNameLineOption).collect(Collectors.toList()));
    }
    
    /**
     * $dateToString操作符
     * @author anwen
     * @date 2024/8/23 11:27
     */
    public static Bson dateToString(String date){
        return dateToString(null,date);
    }

    /**
     * $dateToString操作符
     * @author anwen
     * @date 2024/8/23 11:27
     */
    public static <T> Bson dateToString(SFunction<T,?> date){
        return dateToString(null,date.getFieldNameLineOption());
    }

    /**
     * $dateToString操作符
     * @author anwen
     * @date 2024/8/23 11:27
     */
    public static Bson dateToString(String format,String date){
        return dateToString(format,date,null);
    }

    /**
     * $dateToString操作符
     * @author anwen
     * @date 2024/8/23 11:27
     */
    public static <T> Bson dateToString(String format,SFunction<T,?> date){
        return dateToString(format,date.getFieldNameLineOption(),null);
    }

    /**
     * $dateToString操作符
     * @author anwen
     * @date 2024/8/23 11:27
     */
    public static Bson dateToString(String format,String date,String timezone){
        return dateToString(format,date,timezone,null);
    }

    /**
     * $dateToString操作符
     * @author anwen
     * @date 2024/8/23 11:27
     */
    public static <T> Bson dateToString(String format,SFunction<T,?> date,String timezone){
        return dateToString(format,date.getFieldNameLineOption(),timezone,null);
    }

    /**
     * $dateToString操作符
     * @author anwen
     * @date 2024/8/23 11:27
     */
    public static Bson dateToString(String format,String date,String timezone,Object onNull){
        return new Document("$dateToString",new MongoPlusDocument(){{
            putIsNotNull("date",date);
            putIsNotNull("format",format);
            putIsNotNull("timezone",timezone);
            putIsNotNull("onNull",onNull);
        }});
    }

    /**
     * $dateToString操作符
     * @author anwen
     * @date 2024/8/23 11:27
     */
    public static <T> Bson dateToString(String format,SFunction<T,?> date,String timezone,Object onNull){
        return dateToString(format,date.getFieldNameLineOption(),timezone,onNull);
    }

    /**
     * $mergeObjects操作符
     * @param value 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     * @date 2024/8/23 17:48
     */
    public static Document mergeObjects(String value){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),value);
    }

    /**
     * $mergeObjects操作符
     * @param value 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     * @date 2024/8/23 17:48
     */
    public static <T> Document mergeObjects(SFunction<T,?> value){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),value.getFieldNameLine());
    }

    /**
     * $mergeObjects操作符
     * @param value 值，带$符
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     * @date 2024/8/23 17:48
     */
    public static <T> Document mergeObjectsOption(SFunction<T,?> value){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),value.getFieldNameLineOption());
    }

    /**
     * $mergeObjects操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     * @date 2024/8/23 17:48
     */
    public static Document mergeObjects(Collection<?> values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),values);
    }

    /**
     * $mergeObjects操作符
     * @param functions 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     * @date 2024/8/23 17:48
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
     * @date 2024/8/23 17:48
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
     * @date 2024/8/23 17:48
     */
    public static <T> Document mergeObjects(Object... values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), Arrays.stream(values).collect(Collectors.toList()));
    }

    /**
     * $each操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     * @date 2024/8/24 15:26
     */
    public static Bson each(Object... values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), Arrays.stream(values).collect(Collectors.toList()));
    }

    /**
     * $each and $position操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     * @date 2024/8/24 15:26
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
     * @date 2024/8/24 15:26
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
     * @date 2024/8/24 15:39
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
     * @date 2024/8/24 15:26
     */
    public static Bson each(Collection<?> values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), values);
    }

    /**
     * $each and $position操作符
     * @param values 值
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     * @date 2024/8/24 15:26
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
     * @date 2024/8/24 15:26
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
     * @date 2024/8/24 15:39
     */
    public static Bson eachSort(Object sort,Collection<?> values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),values)
                .append(AggregateEnum.SORT.getValue(), sort);
    }

    /**
     * $abs操作符
     * @author anwen
     * @date 2024/8/25 11:20
     */
    public static Bson abs(Number value){
        return new Document(CommonOperators.ABS.getOperator(),value);
    }


}
