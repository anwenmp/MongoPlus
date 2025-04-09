package com.mongoplus.property;

import com.mongoplus.model.BaseProperty;
import com.mongoplus.model.SlaveDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author JiaChaoYang
 * 属性文件配置
 * @since 2023-02-09 14:29
 **/
@ConfigurationProperties(prefix = "mongo-plus.data.mongodb")
public class MongoDBConnectProperty extends BaseProperty {

    /**
     * 从数据源
     **/
    private List<SlaveDataSource> slaveDataSource;

    public List<SlaveDataSource> getSlaveDataSource() {
        return this.slaveDataSource;
    }

    public void setSlaveDataSource(final List<SlaveDataSource> slaveDataSource) {
        this.slaveDataSource = slaveDataSource;
    }

    public MongoDBConnectProperty(final List<SlaveDataSource> slaveDataSource) {
        this.slaveDataSource = slaveDataSource;
    }

    public MongoDBConnectProperty() {
    }

}
