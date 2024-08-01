package com.anwen.mongo.handlers.condition;

import com.anwen.mongo.conditions.interfaces.condition.CompareCondition;
import com.anwen.mongo.model.BuildUpdate;
import com.mongodb.BasicDBObject;

import java.util.List;

/**
 * 修改条件
 * @author anwen
 * @date 2024/8/1 下午6:42
 */
public interface UpdateCondition {

    /**
     * 通用更新操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午6:40
     */
    BasicDBObject buildUpdateCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $push操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午6:40
     */
    BasicDBObject buildPushCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

}
