package com.mongoplus.property;

import com.mongoplus.cache.global.PropertyCache;

/**
 * configuration属性配置
 *
 * @author JiaChaoYang
 **/
public class MongoDBConfigurationProperty {

    /**
     * banner打印
    */
    private Boolean banner = true;

    private Boolean ikun = false;

    /**
     * 存放自增id的集合
     */
    private String autoIdCollectionName;


    /**
     * 是否将Id字段的ObjectId转换为字段的类型
     */
    private Boolean objectIdConvertType = false;

    /**
     * 自动转换ObjectId
     */
    private Boolean autoConvertObjectId = true;

    /**
     * 自动创建索引
     */
    private Boolean autoCreateIndex = false;

    /**
     * 自动创建时间序列
     */
    private Boolean autoCreateTimeSeries = false;

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

    public Boolean getIkun() {
        return ikun;
    }

    public void setIkun(Boolean ikun) {
        PropertyCache.ikun = ikun;
        this.ikun = ikun;
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
