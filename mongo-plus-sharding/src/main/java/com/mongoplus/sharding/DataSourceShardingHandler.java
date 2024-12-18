package com.mongoplus.sharding;

import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.model.BaseProperty;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class DataSourceShardingHandler extends AbstractDataSourceShardingHandler {

    public DataSourceShardingHandler() {
    }

    public DataSourceShardingHandler(Map<String, List<ExecuteMethodEnum>> shardingStrategy) {
        super(shardingStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String loadBalance(List<String> dsNameList){
        if (dsNameList.size() == 1){
            return dsNameList.get(0);
        }
        Map<String, BaseProperty> propertyMap = DataSourceNameCache.getBasePropertyMap().entrySet().stream()
                .filter(entry -> dsNameList.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 计算所有节点的总权重（通过position的倒数）
        double totalWeight = propertyMap.values().stream().mapToDouble(p -> 1.0 / p.getPosition())
                .sum();

        // 计算一个随机数，决定从哪个节点中选择
        Random rand = new Random();
        double randomWeight = rand.nextDouble() * totalWeight;

        // 遍历节点，基于权重范围选择一个节点
        double currentWeight = 0.0;
        for (Map.Entry<String, BaseProperty> entry : propertyMap.entrySet()) {
            String dsName = entry.getKey();
            BaseProperty property = entry.getValue();
            // 累加每个节点的权重
            currentWeight += 1.0 / property.getPosition();
            if (currentWeight >= randomWeight) {
                // 返回选中的节点
                return dsName;
            }
        }
        // 如果没有选中任何节点（理论上不应该发生），返回列表中的第一个
        return dsNameList.get(0);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientSession handleTransactional(ClientSession currentClientSession, MongoClient mongoClient) {
        return mongoClient.startSession(currentClientSession.getOptions());
    }

}
