package com.mongoplus.toolkit;

import com.mongoplus.annotation.comm.EnumValue;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;

/**
 * 枚举工具类
 *
 * @author anwen
 */
public class EnumUtil {


    /**
     * 获取标注了 {@link EnumValue} 的字段信息
     *
     * @return {@link com.mongoplus.mapping.FieldInformation}
     * @author anwen
     */
    public static FieldInformation getFieldInformation(Object enumInstance) {
        if (enumInstance instanceof Class){
            throw new MongoPlusException("enumInstance cannot be a Class");
        }
        TypeInformation typeInformation = TypeInformation.of(enumInstance);
        return typeInformation.getAnnotationThisField(EnumValue.class);
    }

}
