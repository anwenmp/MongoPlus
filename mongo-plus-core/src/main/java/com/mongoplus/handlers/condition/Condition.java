package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.AbstractChainWrapper;
import com.mongoplus.conditions.interfaces.Projection;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.MutablePair;
import com.mongoplus.toolkit.CollUtil;

import java.util.List;

/**
 * 条件接口
 * @author anwen
 */
public interface Condition {

    /**
     * 查询条件构建
     * @param compareConditionList 条件集合
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject queryCondition(List<CompareCondition> compareConditionList);

    /**
     * 单个条件构建
     * @param compareCondition 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject queryCondition(CompareCondition compareCondition);

    /**
     * 修改条件构建
     * @param updateChainWrapper 修改条件构造器
     * @return {@link com.mongoplus.model.MutablePair} left=query,right=update
     * @author anwen
     */
    MutablePair<BasicDBObject,BasicDBObject> updateCondition(UpdateChainWrapper<?, ?> updateChainWrapper);

    /**
     * project阶段条件构造
     * @param projectionList project集合
     * @return {@link BasicDBObject}
     * @author anwen
     */
    default BasicDBObject projectionCondition(List<Projection> projectionList){
        return new BasicDBObject(){{
            if (CollUtil.isNotEmpty(projectionList)) {
                projectionList.forEach(projection -> put(projection.getColumn(), projection.getValue()));
            }
        }};
    }

    /**
     * 根据wrapper构建查询条件
     * @param wrapper wrapper
     * @return {@link BaseConditionResult}
     * @author anwen
     */
    BaseConditionResult queryCondition(AbstractChainWrapper<?,?> wrapper);

    /**
     * wrapper是否为空
     * @param wrapper wrapper
     * @return boolean
     */
    default boolean isEmpty(@Nullable AbstractChainWrapper<?,?> wrapper) {
        if (wrapper == null) {
            return true;
        }
        return CollUtil.isEmpty(wrapper.getCompareList()) && CollUtil.isEmpty(wrapper.getBasicDBObjectList());
    }

    /**
     * wrapper是否不为空
     * @param wrapper wrapper
     * @return boolean
     */
    default boolean isNotEmpty(@Nullable AbstractChainWrapper<?,?> wrapper) {
        return !isEmpty(wrapper);
    }

}
