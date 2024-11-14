package com.mongoplus.property;

import com.mongoplus.model.BaseProperty;
import com.mongoplus.model.SlaveDataSource;
import org.noear.solon.annotation.Configuration;

import java.util.List;

/**
 * @author JiaChaoYang
 * 属性文件配置
 * @since 2023-02-09 14:29
 **/
@Configuration
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
