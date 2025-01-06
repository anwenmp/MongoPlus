package com.mongoplus.handlers.write;

import com.mongodb.MongoNamespace;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.enums.MultipleWrite;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.toolkit.CollUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MultipleWriteHandler implements MultipleWriteStrategy {

    private final Map<MultipleWrite, List<String>> multipleWriteStrategy = new ConcurrentHashMap<>();

    public MultipleWriteHandler(MongoPlusClient mongoPlusClient) {
        List<String> dataSourceNameList = mongoPlusClient.getDataSourceNameList();
        multipleWriteStrategy.put(MultipleWrite.SAVE,dataSourceNameList);
        multipleWriteStrategy.put(MultipleWrite.REMOVE,dataSourceNameList);
        multipleWriteStrategy.put(MultipleWrite.UPDATE,dataSourceNameList);
        multipleWriteStrategy.put(MultipleWrite.BULK_WRITE,dataSourceNameList);
    }

    /**
     * 添加多写映射策略
     * @param multipleWrite 操作枚举
     * @param dsNameList 数据源名称集合
     */
    public void addMultipleWriteStrategy(MultipleWrite multipleWrite,List<String> dsNameList) {
        multipleWriteStrategy.put(multipleWrite,dsNameList);
    }

    @Override
    public List<String> multipleWrite(String currentDataSource, MongoNamespace namespace) {
        return null;
    }

    /**
     * 获取本次操作对应的数据源list
     * @param multipleWrite 本次操作枚举
     * @param namespace 命名空间
     * @return {@code List<String>} 数据源名称List
     */
    public List<String> getMultipleWrite(MultipleWrite multipleWrite, MongoNamespace namespace) {
        List<String> customDsNameList = multipleWrite(DataSourceNameCache.getDataSource(), namespace);
        if (CollUtil.isNotEmpty(customDsNameList)) {
            return customDsNameList;
        }
        return multipleWriteStrategy.get(multipleWrite);
    }

}
