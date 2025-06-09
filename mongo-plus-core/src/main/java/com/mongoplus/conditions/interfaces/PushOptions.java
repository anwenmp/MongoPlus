package com.mongoplus.conditions.interfaces;

import com.mongoplus.annotation.comm.Nullable;
import org.bson.Document;

import java.util.Objects;

public class PushOptions {
    private Integer position;
    private Integer slice;
    private Integer sort;
    private Document sortDocument;

    /**
     * 获取在数组中添加推送值的位置.
     *
     * @return 位置，可能为空
     */
    @Nullable
    public Integer getPosition() {
        return position;
    }

    /**
     * 设置在数组中添加推送值的位置.
     *
     * @param position 位置
     * @return this
     */
    public PushOptions position(@Nullable final Integer position) {
        this.position = position;
        return this;
    }

    /**
     * 获取切片值，即允许的数组元素数量的限制.
     *
     * @return 表示允许的数组元素数量的限制的切片值
     */
    @Nullable
    public Integer getSlice() {
        return slice;
    }

    /**
     * 设置允许的数组元素数量的限制.
     *
     * @param slice 极限
     * @return this
     */
    public PushOptions slice(@Nullable final Integer slice) {
        this.slice = slice;
        return this;
    }

    /**
     * 获取对非文档数组元素进行排序的排序方向.
     *
     * @return 排序方向
     */
    @Nullable
    public Integer getSort() {
        return sort;
    }

    /**
     * 设置对非文档数组元素进行排序的排序方向.
     *
     * @param sort 排序方向
     * @return this
     * @throws IllegalStateException 如果 sortDocument 属性已设置
     */
    public PushOptions sort(@Nullable final Integer sort) {
        if (sortDocument != null) {
            throw new IllegalStateException("sort can not be set if sortDocument already is");
        }
        this.sort = sort;
        return this;
    }

    /**
     * 获取对文档数组元素进行排序的排序方向.
     *
     * @return 排序文档
     */
    @Nullable
    public Document getSortDocument() {
        return sortDocument;
    }

    /**
     * 设置对文档数组元素进行排序的排序方向.
     *
     * @param sortDocument 排序文档
     * @return this
     * @throws IllegalStateException 如果已设置 sort 属性
     */
    public PushOptions sortDocument(@Nullable final Document sortDocument) {
        if (sort != null) {
            throw new IllegalStateException("sortDocument can not be set if sort already is");
        }
        this.sortDocument = sortDocument;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PushOptions that = (PushOptions) o;

        if (!Objects.equals(position, that.position)) {
            return false;
        }
        if (!Objects.equals(slice, that.slice)) {
            return false;
        }
        if (!Objects.equals(sort, that.sort)) {
            return false;
        }
        return Objects.equals(sortDocument, that.sortDocument);
    }

    @Override
    public int hashCode() {
        int result = position != null ? position.hashCode() : 0;
        result = 31 * result + (slice != null ? slice.hashCode() : 0);
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        result = 31 * result + (sortDocument != null ? sortDocument.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Push Options{"
                + "position=" + position
                + ", slice=" + slice
                + ((sort == null) ? "" : ", sort=" + sort)
                + ((sortDocument == null) ? "" :  ", sortDocument=" + sortDocument)
                + '}';
    }
}
