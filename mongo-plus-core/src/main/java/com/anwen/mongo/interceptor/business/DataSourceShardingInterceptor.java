package com.anwen.mongo.interceptor.business;

import com.anwen.mongo.cache.global.DataSourceNameCache;
import com.anwen.mongo.enums.ExecuteMethodEnum;
import com.anwen.mongo.handlers.sharding.DataSourceShardingHandler;
import com.anwen.mongo.handlers.sharding.DataSourceShardingStrategy;
import com.anwen.mongo.interceptor.Interceptor;
import com.anwen.mongo.logging.Log;
import com.anwen.mongo.logging.LogFactory;
import com.anwen.mongo.manager.MongoPlusClient;
import com.anwen.mongo.toolkit.CollUtil;
import com.anwen.mongo.toolkit.StringUtils;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 分片拦截器
 *
 * @author anwen
 */
public class DataSourceShardingInterceptor implements Interceptor {

    private final Log log = LogFactory.getLog(DataChangeRecorderInnerInterceptor.class);

    private final MongoPlusClient mongoPlusClient;

    /**
     * 分片处理器
     *
     * @date 2024/11/14 18:02
     */
    private final DataSourceShardingHandler dataSourceShardingHandler;

    public DataSourceShardingInterceptor(MongoPlusClient mongoPlusClient) {
        this.mongoPlusClient = mongoPlusClient;
        this.dataSourceShardingHandler = new DataSourceShardingHandler();
    }

    public DataSourceShardingInterceptor(MongoPlusClient mongoPlusClient,
                                         DataSourceShardingHandler dataSourceShardingHandler) {
        this.mongoPlusClient = mongoPlusClient;
        this.dataSourceShardingHandler = dataSourceShardingHandler;
    }

    /**
     * {@inheritDoc}
     * <p style='color:red'>要保证分片策略是最后一个拦截器</p>
     *
     * @return {@link int}
     * @author anwen
     * @date 2024/11/14 20:23
     */
    @Override
    public int order() {
        return Interceptor.super.order();
    }

    /**
     * {@inheritDoc}
     *
     * @author anwen
     * @date 2024/11/14 18:03
     */
    @Override
    public void beforeExecute(ExecuteMethodEnum executeMethodEnum, Object[] source,
                              MongoCollection<Document> collection) {
        // 获取所有数据源
        List<String> dataSourceList = new ArrayList<>(DataSourceNameCache.getBasePropertyMap().keySet());

        // 获取分片策略
        List<String> dsNameList = dataSourceShardingHandler.getHandleShardingStrategy(executeMethodEnum);

        DataSourceShardingStrategy shardingStrategy = dataSourceShardingHandler.getDataSourceShardingStrategy();

        String currentDataSourceName = DataSourceNameCache.getDataSource();

        // 根据分片策略决定数据源
        if (shardingStrategy != null) {
            String shardingDsName = shardingStrategy.sharding(currentDataSourceName, executeMethodEnum, source);
            dsNameList = dataSourceShardingHandler.handleDsName(shardingDsName, dataSourceList);
        } else {
            dsNameList = dsNameList.stream()
                    .flatMap(dsName -> dataSourceShardingHandler.handleDsName(dsName, dataSourceList).stream())
                    .collect(Collectors.toList());
        }

        // 当前数据源
        String dsName = currentDataSourceName;

        if (CollUtil.isEmpty(dsNameList)) {
            log.error("No data source hit");
        } else {
            // 去重并选择负载均衡后的数据源
            dsName = dataSourceShardingHandler.loadBalance(dsNameList.stream().distinct().collect(Collectors.toList()));
        }

        if (log.isTraceEnabled()) {
            log.trace("Hit " + dsName + " DataSource");
        }
        if (StringUtils.isBlank(dsName)) {
            log.error("No data source hit, no data source replacement will be performed, dsName value is " + dsName);
        }
        if (!Objects.equals(dsName, currentDataSourceName)) {
            // 获取新的 MongoCollection
            MongoNamespace namespace = collection.getNamespace();
            MongoCollection<Document> newCollection = mongoPlusClient.getCollection(
                    dsName, namespace.getDatabaseName(), namespace.getCollectionName()
            );

            // 更新源数据
            source[source.length - 1] = newCollection;
        }
    }
}
