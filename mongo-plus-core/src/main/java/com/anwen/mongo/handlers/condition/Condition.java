package com.anwen.mongo.handlers.condition;

import com.anwen.mongo.conditions.interfaces.aggregate.pipeline.Projection;
import com.anwen.mongo.conditions.interfaces.condition.CompareCondition;
import com.anwen.mongo.conditions.update.UpdateChainWrapper;
import com.anwen.mongo.model.MutablePair;
import com.anwen.mongo.toolkit.CollUtil;
import com.mongodb.BasicDBObject;

import java.util.List;

/**
 * 条件接口
 * @author anwen
 * @date 2024/8/1 下午2:19
 */
public interface Condition {

    /**
     * 查询条件构建
     * @param compareConditionList 条件集合
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午2:20
     */
    BasicDBObject queryCondition(List<CompareCondition> compareConditionList);

    /**
     * 单个条件构建
     * @param compareCondition 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午2:26
     */
    BasicDBObject queryCondition(CompareCondition compareCondition);

    /**
     * 修改条件构建
     * @param updateChainWrapper 修改条件构造器
     * @return {@link com.anwen.mongo.model.MutablePair} left=query,right=update
     * @author anwen
     * @date 2024/8/1 下午2:21
     */
    MutablePair<BasicDBObject,BasicDBObject> updateCondition(UpdateChainWrapper<?, ?> updateChainWrapper);

    /**
     * project阶段条件构造
     * @param projectionList project集合
     * @return {@link BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午8:25
     */
    default BasicDBObject projectionCondition(List<Projection> projectionList){
        return new BasicDBObject(){{
            if (CollUtil.isNotEmpty(projectionList)) {
                projectionList.forEach(projection -> put(projection.getColumn(), projection.getValue()));
            }
        }};
    }

}
