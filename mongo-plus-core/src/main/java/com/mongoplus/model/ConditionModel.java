package com.mongoplus.model;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 条件构造
 * @author anwen
 */
public class ConditionModel {

    /**
     * 构建条件对象
     */
    List<ConditionMetaObject> conditionMetaObjects;

    /**
     * 构建排序对象
     */
    List<Order> orderList;

    /**
     * 构建显示字段
     */
    List<Projection> projectionList;

    /**
     * 自定义条件语句
     */
    List<BasicDBObject> basicDBObjectList;

    public ConditionModel() {
        this.conditionMetaObjects = new CopyOnWriteArrayList<>();
        this.orderList = new ArrayList<>();
        this.projectionList = new ArrayList<>();
        this.basicDBObjectList = new ArrayList<>();
    }

    public List<ConditionMetaObject> getConditionMetaObjects() {
        return conditionMetaObjects;
    }

    public void setConditionMetaObjects(List<ConditionMetaObject> conditionMetaObjects) {
        this.conditionMetaObjects = conditionMetaObjects;
    }

    public List<Order> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    public List<Projection> getProjectionList() {
        return projectionList;
    }

    public void setProjectionList(List<Projection> projectionList) {
        this.projectionList = projectionList;
    }

    public List<BasicDBObject> getBasicDBObjectList() {
        return basicDBObjectList;
    }

    public void setBasicDBObjectList(List<BasicDBObject> basicDBObjectList) {
        this.basicDBObjectList = basicDBObjectList;
    }

}
