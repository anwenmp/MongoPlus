package com.anwen.mongo.conditions.interfaces;

import com.anwen.mongo.bson.MongoPlusDocument;
import com.anwen.mongo.enums.AggregateEnum;
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

    public static Document mergeObjects(Collection<?> values){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),values);
    }

    public static <T> Document mergeObjectsLambda(Collection<? extends SFunction<T,?>> functions){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),functions.stream()
                .map(SFunction::getFieldNameLine).collect(Collectors.toList()));
    }

    public static <T> Document mergeObjectsLambdaOption(Collection<? extends SFunction<T,?>> functions){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(),functions.stream()
                .map(SFunction::getFieldNameLineOption).collect(Collectors.toList()));
    }

    public static <T> Document mergeObjects(Object... value){
        return new Document(AggregateEnum.MERGE_OBJECTS.getValue(), Arrays.stream(value).collect(Collectors.toList()));
    }


}
