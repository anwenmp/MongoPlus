package com.mongoplus.handlers.collection;

import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.enums.CollectionNameConvertEnum;
import com.mongoplus.toolkit.StringUtils;

import java.util.function.Function;

import static com.mongoplus.enums.CollectionNameConvertEnum.ALL_CHAR_LOWERCASE;

/**
 * 便捷的注解操作
 *
 * @author anwen
 */
public class AnnotationOperate implements AnnotationHandler {

    private static AnnotationHandler ANNOTATION_HANDLER_INSTANCE = new AnnotationOperate();

    private static CollectionNameConvertEnum collectionNameConvertEnum = ALL_CHAR_LOWERCASE;

    public static AnnotationHandler getAnnotationHandler() {
        return ANNOTATION_HANDLER_INSTANCE;
    }

    public static void setAnnotationHandler(AnnotationHandler annotationHandler) {
        ANNOTATION_HANDLER_INSTANCE = annotationHandler;
    }

    public static void setCollectionNameConvertEnum(CollectionNameConvertEnum collectionNameConvertEnum) {
        AnnotationOperate.collectionNameConvertEnum = collectionNameConvertEnum;
    }

    /**
     * 获取集合名称
     * @param clazz entity
     * @return {@link java.lang.String}
     * @author anwen
     */
    public static String getCollectionName(Class<?> clazz){
        String collectionName = getCollectionInfo(clazz.getAnnotation(CollectionName.class),CollectionName::value);
        if (StringUtils.isBlank(collectionName)){
            collectionName = convert(clazz);
        }
        return collectionName;
    }

    /**
     * 获取集合详情
     * @param collectionName 集合注解
     * @param func 属性
     * @return {@link R}
     * @author anwen
     */
    public static <R> R getCollectionInfo(CollectionName collectionName, Function<? super CollectionName,? extends R> func){
        if (collectionName == null){
            return null;
        }
        return ANNOTATION_HANDLER_INSTANCE.getProperty(collectionName,func);
    }

    /**
     * 获取database
     * @param clazz entity
     * @return {@link java.lang.String}
     * @author anwen
     */
    public static String getDatabase(Class<?> clazz){
        return getCollectionInfo(clazz.getAnnotation(CollectionName.class),CollectionName::database);
    }

    private static String convert(Class<?> clazz){
        String collectionName = null;
        switch (collectionNameConvertEnum) {
            case ALL_CHAR_LOWERCASE: collectionName = clazz.getSimpleName().toLowerCase(); break;
            case FIRST_CHAR_LOWERCASE: collectionName = StringUtils.firstCharToLowerCase(clazz.getSimpleName()); break;
            case CLASS_NAME: collectionName = clazz.getSimpleName(); break;
            case CAMEL_TO_UNDERLINE: collectionName = StringUtils.camelToUnderline(clazz.getSimpleName()); break;
        }
        return collectionName;
    }

}
