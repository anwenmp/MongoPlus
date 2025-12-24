package com.mongoplus.property;

import com.mongoplus.cache.global.PropertyCache;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * configuration属性配置
 *
 * @author JiaChaoYang
 **/
@ConfigurationProperties(prefix = "mongo-plus.configuration")
public class MongoDBConfigurationProperty {

    /**
     * banner打印
    */
    private Boolean banner = true;

    /**
     * 存放自增id的集合
     */
    private String autoIdCollectionName;

    /**
     * 是否开启小黑子模式
     */
    private Boolean ikun = false;

    /**
     * 自动转换ObjectId
     */
    private Boolean autoConvertObjectId = true;

    /**
     * 是否将Id字段的ObjectId转换为字段的类型
     */
    private Boolean objectIdConvertType = false;

    /**
     * 自动创建索引
     */
    private Boolean autoCreateIndex = false;

    /**
     * 自动创建时间序列
     */
    private Boolean autoCreateTimeSeries = false;

    /**
     * 自动创建相关操作所需配置的包路径,避免多模块下扫描不到
     */
    private List<String> autoScanPackages;

    public List<String> getAutoScanPackages() {
        return autoScanPackages;
    }

    public void setAutoScanPackages(List<String> autoScanPackages) {
        this.autoScanPackages = autoScanPackages;
    }

    public Boolean getAutoCreateTimeSeries() {
        return autoCreateTimeSeries;
    }

    public void setAutoCreateTimeSeries(Boolean autoCreateTimeSeries) {
        this.autoCreateTimeSeries = autoCreateTimeSeries;
    }

    public Boolean getAutoCreateIndex() {
        return autoCreateIndex;
    }

    public void setAutoCreateIndex(Boolean autoCreateIndex) {
        this.autoCreateIndex = autoCreateIndex;
    }

    public Boolean getIkun() {
        return ikun;
    }

    public void setIkun(Boolean ikun) {
        PropertyCache.ikun = ikun;
        this.ikun = ikun;
    }

    public Boolean getAutoConvertObjectId() {
        return autoConvertObjectId;
    }

    public void setAutoConvertObjectId(Boolean autoConvertObjectId) {
        this.autoConvertObjectId = autoConvertObjectId;
        PropertyCache.autoConvertObjectId = autoConvertObjectId;
    }

    public String getAutoIdCollectionName() {
        return autoIdCollectionName;
    }

    public void setAutoIdCollectionName(String autoIdCollectionName) {
        PropertyCache.autoIdCollectionName = autoIdCollectionName;
        this.autoIdCollectionName = autoIdCollectionName;
    }

    public Boolean getObjectIdConvertType() {
        return objectIdConvertType;
    }

    public void setObjectIdConvertType(Boolean objectIdConvertType) {
        PropertyCache.objectIdConvertType = objectIdConvertType;
        this.objectIdConvertType = objectIdConvertType;
    }

    public Boolean getBanner() {
        return banner;
    }

    public void setBanner(Boolean banner) {
        this.banner = banner;
    }
}
