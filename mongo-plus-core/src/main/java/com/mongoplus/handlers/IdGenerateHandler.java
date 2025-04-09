package com.mongoplus.handlers;

import com.mongoplus.enums.IdTypeEnum;
import com.mongoplus.mapping.TypeInformation;

import java.io.Serializable;

/**
 * id生成处理器
 * @author anwen
 */
public interface IdGenerateHandler {

    /**
     * 生成id
     * @param idTypeEnum id类型
     * @param typeInformation 类信息
     * @return {@link java.io.Serializable}
     * @author anwen
     */
    Serializable generateId(IdTypeEnum idTypeEnum, TypeInformation typeInformation);

}
