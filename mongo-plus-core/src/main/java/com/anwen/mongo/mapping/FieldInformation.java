package com.anwen.mongo.mapping;

import java.lang.reflect.Field;

/**
 * @author JiaChaoYang
 **/
public interface FieldInformation {

    Object getValue();

    String getName();

    boolean isMap();

    Field getField();

    Class<?> getType();

    Class<?> mapValueType();

    Class<?> collectionValueType();

    boolean isCollection();

    boolean isSimpleType();

}
