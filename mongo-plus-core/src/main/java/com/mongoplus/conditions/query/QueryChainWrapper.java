package com.mongoplus.conditions.query;

import com.mongoplus.conditions.AbstractChainWrapper;
import com.mongoplus.conditions.interfaces.query.order.Ordered;
import com.mongoplus.conditions.interfaces.query.project.Project;
import com.mongoplus.handlers.condition.Condition;
import com.mongoplus.model.BaseConditionResult;

/**
 * AbstractChainWrapper的条件扩展类，查询专有的条件，使用类构造条件时，使用QueryChainWrapper的子类，{@link QueryWrapper}
 * @author JiaChaoYang
*/
public abstract class QueryChainWrapper<T,Children extends QueryChainWrapper<T,Children>>
        extends AbstractChainWrapper<T,Children>
        implements Project<T,Children>, Ordered<T,Children> {
    @Override
    public BaseConditionResult buildCondition(Condition condition) {
        return condition.queryCondition(this);
    }

}
