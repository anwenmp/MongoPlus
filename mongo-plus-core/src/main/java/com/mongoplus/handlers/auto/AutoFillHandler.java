package com.mongoplus.handlers.auto;

import com.mongoplus.enums.FieldFill;
import com.mongoplus.mapping.MappingMongoConverter;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeInformation;
import org.bson.Document;

public interface AutoFillHandler {

    /**
     * 处理方法
     *
     * @param source          源文档
     * @param typeInformation 类封装信息
     * @param converter 转换器
     * @author anwen
     */
    void handle(Document source, TypeInformation typeInformation, FieldFill fieldFill, MongoConverter converter);

}
