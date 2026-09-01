package com.mongoplus.conditions;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.handlers.condition.Condition;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.ConditionModel;
import com.mongoplus.model.Order;
import com.mongoplus.model.Projection;

import java.util.List;

import static com.mongoplus.handlers.condition.BuildCondition.condition;

/**
 * 条件构造抽象类
 *
 * @author anwen
 */
public abstract class Wrapper<T> {

    private ConditionModel conditionModel;

    protected Wrapper(ConditionModel conditionModel) {
        this.conditionModel = conditionModel;
    }

    protected Wrapper() {
        this(new ConditionModel());
    }

    /**
     * 获取条件元对象列表
     * @author anwen
     */
    public List<ConditionMetaObject> getConditionMetaObjects() {
        return conditionModel.getConditionMetaObjects();
    }

    public void addConditionMetaObject(ConditionMetaObject conditionMetaObject) {
        conditionModel.getConditionMetaObjects().add(conditionMetaObject);
    }

    /**
     * 获取排序列表
     * @author anwen
     */
    public List<Order> getOrderList() {
        return conditionModel.getOrderList();
    }

    /**
     * 添加排序
     * @author anwen
     */
    public void addOrder(Order order) {
        conditionModel.getOrderList().add(order);
    }

    /**
     * 获取投影列表
     * @author anwen
     */
    public List<Projection> getProjectionList() {
        return conditionModel.getProjectionList();
    }

    /**
     * 添加投影
     * @author anwen
     */
    public void addProjection(List<Projection> projection) {
        conditionModel.getProjectionList().addAll(projection);
    }

    /**
     * 获取BasicDBObject列表
     * @author anwen
     */
    public List<BasicDBObject> getBasicDBObjectList() {
        return conditionModel.getBasicDBObjectList();
    }

    /**
     * 添加BasicDBObject
     * @author anwen
     */
    public void addBasicDBObject(BasicDBObject basicDBObject) {
        conditionModel.getBasicDBObjectList().add(basicDBObject);
    }

    public void addBasicDBObject(List<BasicDBObject> basicDBObjects) {
        conditionModel.getBasicDBObjectList().addAll(basicDBObjects);
    }

    /**
     * 清空所有构建的条件
     * @author anwen
     */
    public synchronized void clear() {
        conditionModel = new ConditionModel();
    }

    /**
     * 实体对象（子类实现）
     *
     * @return 泛型 T
     */
    public abstract T getEntity();

    /**
     * 构建条件
     * @author anwen
     */
    public BaseConditionResult buildCondition() {
        return buildCondition(condition());
    }

    /**
     * 构建条件
     * @author anwen
     */
    public abstract BaseConditionResult buildCondition(Condition condition);

    /**
     * 判断是否为空
     */
    public boolean isNotEmpty(){
        return isNotEmpty(condition());
    }

    /**
     * 判断是否为空
     */
    public abstract boolean isNotEmpty(Condition condition);

    public ConditionModel getConditionModel() {
        return conditionModel;
    }
}
