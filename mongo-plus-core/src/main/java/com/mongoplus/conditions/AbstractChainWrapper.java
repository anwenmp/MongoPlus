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

/**
 * 查询条件
 * @author JiaChaoYang
 */
public abstract class AbstractChainWrapper<T, Children extends AbstractChainWrapper<T, Children>> extends Wrapper<T>
        implements QueryCondition<T, Children> {

    @SuppressWarnings("unchecked")
    protected final Children typedThis = (Children) this;

    /**
     * 数据库表映射实体类
     */
    private T entity;

    public AbstractChainWrapper() {
        super();
    }

    @Override
    public T getEntity() {
        return entity;
    }

    public Children getTypedThis() {
        return typedThis;
    }

    @Override
    public Children addCondition(ConditionMetaObject conditionMetaObject) {
        super.addConditionMetaObject(conditionMetaObject);
        return typedThis;
    }

    @Override
    public Children addCondition(List<Projection> projections) {
        super.addProjection(projections);
        return typedThis;
    }

    @Override
    public Children addCondition(Order order) {
        super.addOrder(order);
        return typedThis;
    }


    @Override
    public boolean isNotEmpty(Condition condition) {
        return condition.isNotEmpty(this);
    }
    @Override
    public Children custom(BasicDBObject basicDBObject) {
        super.addBasicDBObject(basicDBObject);
        return typedThis;
    }

    @Override
    public Children custom(Bson bson) {
        super.addBasicDBObject(BasicDBObject.parse(bson.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry()).toJson()));
        return typedThis;
    }

    @Override
    public Children custom(SFunction<MongoPlusBasicDBObject, MongoPlusBasicDBObject> function) {
        return custom(function.apply(new MongoPlusBasicDBObject()));
    }

    @Override
    public Children custom(MongoPlusBasicDBObject mongoPlusBasicDBObject) {
        super.addBasicDBObject(mongoPlusBasicDBObject);
        return typedThis;
    }

    @Override
    public Children custom(List<BasicDBObject> basicDBObjectList) {
        super.addBasicDBObject(basicDBObjectList);
        return typedThis;
    }

}
