package com.mongoplus.mapping;

import org.bson.conversions.Bson;

import java.util.Map;

/**
 * 将java转为mongodb可用类型
 * @author JiaChaoYang
 */
public interface MongoWriter {

    void write(Object sourceObj, Bson bson);

    void write(Map<?,?> map, Bson bson);

}
