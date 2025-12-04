package com.mongoplus.conditions.update;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.AbstractChainWrapper;
import com.mongoplus.conditions.interfaces.update.UpdateCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.handlers.condition.Condition;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.MutablePair;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.mongoplus.handlers.condition.BuildCondition.condition;

/**
 * update接口实现
 * @author JiaChaoYang
*/
public class UpdateChainWrapper<T,Children extends UpdateChainWrapper<T,Children>>
        extends AbstractChainWrapper<T, Children> implements UpdateCondition<T,Children> {

    @SuppressWarnings("unchecked")
    protected final Children typedThis = (Children) this;

    private final List<ConditionMetaObject> updateCompareList = new CopyOnWriteArrayList<>();

    private final List<Bson> updateBson = new CopyOnWriteArrayList<>();

    public List<Bson> getUpdateBson() {
        return updateBson;
    }

    public List<ConditionMetaObject> getUpdateCompareList() {
        return updateCompareList;
    }

    @Override
    public synchronized void clear() {
        super.clear();
        updateCompareList.clear();
        updateBson.clear();
    }

    @Override
    public Children addUpdateCondition(ConditionMetaObject conditionMetaObject) {
        this.updateCompareList.add(conditionMetaObject);
        return typedThis;
    }

    /**
     * 构建修改条件
     * @return {@link com.mongoplus.model.MutablePair}
     * @author anwen
     */
    public MutablePair<BasicDBObject, BasicDBObject> buildUpdateCondition(){
        return buildUpdateCondition(condition());
    }

    /**
     * 构建修改条件
     * @param condition 条件构造器
     * @return {@link com.mongoplus.model.MutablePair}
     * @author anwen
     */
    public MutablePair<BasicDBObject, BasicDBObject> buildUpdateCondition(Condition condition){
        return condition.updateCondition(this);
    }

    @Override
    public Children updateCustom(Bson bson) {
        updateBson.add(bson);
        return typedThis;
    }

    @Override
    public BaseConditionResult buildCondition(Condition condition) {
        return condition.queryCondition(this);
    }
}
