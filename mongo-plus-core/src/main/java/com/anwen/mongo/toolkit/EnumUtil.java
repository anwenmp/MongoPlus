package com.anwen.mongo.toolkit;

import com.anwen.mongo.annotation.comm.EnumValue;
import com.anwen.mongo.domain.MongoPlusException;
import com.anwen.mongo.mapping.FieldInformation;
import com.anwen.mongo.mapping.TypeInformation;

/**
 * 枚举工具类
 *
 * @author anwen
 */
public class EnumUtil {


    /**
     * 获取标注了 {@link EnumValue} 的字段信息
     *
     * @return {@link com.anwen.mongo.mapping.FieldInformation}
     * @author anwen
     * @date 2024/11/18 17:12
     */
    public static FieldInformation getFieldInformation(Object enumInstance) {
        if (enumInstance instanceof Class){
            throw new MongoPlusException("enumInstance cannot be a Class");
        }
        TypeInformation typeInformation = TypeInformation.of(enumInstance);
        return typeInformation.getAnnotationThisField(EnumValue.class);
    }

}
