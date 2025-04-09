package com.mongoplus.model;

import com.mongodb.client.model.IndexModel;
import com.mongoplus.enums.IndexType;
import com.mongoplus.mapping.TypeInformation;

import java.util.List;
import java.util.Objects;

/**
 * 索引的元对象
 */
public class IndexMetaObject {

    /**
     * 类信息
     */
    private TypeInformation typeInformation;

    /**
     * 数据源
     */
    private String dataSource;

    /**
     * 索引对象
     */
    private List<IndexModel> indexModels;

    /**
     * 索引类型
     */
    private IndexType indexType;

    public IndexMetaObject() {
    }

    public IndexMetaObject(TypeInformation typeInformation, String dataSource, List<IndexModel> indexModels, IndexType indexType) {
        this.typeInformation = typeInformation;
        this.dataSource = dataSource;
        this.indexModels = indexModels;
        this.indexType = indexType;
    }

    public TypeInformation getTypeInformation() {
        return typeInformation;
    }

    public void setTypeInformation(TypeInformation typeInformation) {
        this.typeInformation = typeInformation;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public List<IndexModel> getIndexModels() {
        return indexModels;
    }

    public void setIndexModels(List<IndexModel> indexModels) {
        this.indexModels = indexModels;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexMetaObject that = (IndexMetaObject) o;
        return Objects.equals(typeInformation, that.typeInformation) && Objects.equals(dataSource, that.dataSource) && Objects.equals(indexModels, that.indexModels) && indexType == that.indexType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeInformation, dataSource, indexModels, indexType);
    }

    @Override
    public String toString() {
        return "{" +
                "typeInformation=" + typeInformation +
                ", dataSource='" + dataSource + '\'' +
                ", indexModels=" + indexModels +
                ", indexType=" + indexType +
                '}';
    }
}
