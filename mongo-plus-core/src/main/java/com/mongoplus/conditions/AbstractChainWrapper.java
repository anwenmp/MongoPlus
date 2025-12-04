package com.mongoplus.conditions;

import com.mongodb.BasicDBObject;
import com.mongoplus.bson.MongoPlusBasicDBObject;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.conditions.interfaces.query.QueryCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.model.Order;
import com.mongoplus.handlers.condition.Condition;
import com.mongoplus.model.Projection;
import com.mongoplus.support.SFunction;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 查询条件
 * @author JiaChaoYang
 */
public abstract class AbstractChainWrapper<T, Children extends AbstractChainWrapper<T, Children>>
        implements QueryCondition<T, Children> {

    @SuppressWarnings("unchecked")
    protected final Children typedThis = (Children) this;

    /**
     * 构建条件对象
     */
    private final List<ConditionMetaObject> conditionMetaObjects = new CopyOnWriteArrayList<>();

    /**
     * 构建排序对象
     */
    List<Order> orderList = new ArrayList<>();

    /**
     * 构建显示字段
     */
    List<Projection> projectionList = new ArrayList<>();

    /**
     * 自定义条件语句
     */
    List<BasicDBObject> basicDBObjectList = new ArrayList<>();

    public Children getTypedThis() {
        return typedThis;
    }

    public List<ConditionMetaObject> getConditionMetaObjects() {
        return conditionMetaObjects;
    }

    public List<Order> getOrderList() {
        return orderList;
    }

    public List<Projection> getProjectionList() {
        return projectionList;
    }

    public List<BasicDBObject> getBasicDBObjectList() {
        return basicDBObjectList;
    }

    /**
     * 清空所有构建的条件
     * @author anwen
     */
    public synchronized void clear() {
        conditionMetaObjects.clear();
        orderList.clear();
        projectionList.clear();
        basicDBObjectList.clear();
    }

    @Override
    public Children addCondition(ConditionMetaObject conditionMetaObject) {
        this.conditionMetaObjects.add(conditionMetaObject);
        return typedThis;
    }

    @Override
    public Children addCondition(List<Projection> projections) {
        this.projectionList.addAll(projections);
        return typedThis;
    }

    @Override
    public Children addCondition(Order order) {
        this.orderList.add(order);
        return typedThis;
    }


    @Override
    public boolean isNotEmpty(Condition condition) {
        return condition.isNotEmpty(this);
    }
    @Override
    public Children custom(BasicDBObject basicDBObject) {
        this.basicDBObjectList.add(basicDBObject);
        return typedThis;
    }

    @Override
    public Children custom(Bson bson) {
        this.basicDBObjectList.add(BasicDBObject.parse(bson.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry()).toJson()));
        return typedThis;
    }

    @Override
    public Children custom(SFunction<MongoPlusBasicDBObject, MongoPlusBasicDBObject> function) {
        return custom(function.apply(new MongoPlusBasicDBObject()));
    }

    @Override
    public Children custom(MongoPlusBasicDBObject mongoPlusBasicDBObject) {
        this.basicDBObjectList.add(mongoPlusBasicDBObject);
        return typedThis;
    }

    @Override
    public Children custom(List<BasicDBObject> basicDBObjectList) {
        this.basicDBObjectList.addAll(basicDBObjectList);
        return typedThis;
    }

}
