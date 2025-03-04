package com.mongoplus.handlers.write;

import com.mongodb.MongoNamespace;

import java.util.List;

/**
 * 多写策略
 */
interface MultipleWriteStrategy {

    /**
     * 写入策略，返回需要写入数据的数据源名称，不存在则抛出异常
     * <p>如果该方法响应数据中存在元素，则直接使用该方法返回值进行写入</p>
     * @param currentDataSource 当前数据源名称
     * @param namespace 集合命名空间
     * @return {@code List<String>} 其他需要写入的数据源名称
     */
    List<String> multipleWrite(String currentDataSource, MongoNamespace namespace);

}
