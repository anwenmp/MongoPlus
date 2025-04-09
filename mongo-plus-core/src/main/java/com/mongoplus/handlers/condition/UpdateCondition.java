package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.model.BuildUpdate;

import java.util.List;

/**
 * 修改条件
 * @author anwen
 */
public interface UpdateCondition {

    /**
     * 通用更新操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildUpdateCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $push操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildPushCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $currentDate操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildCurrentDateCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $rename操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildRenameCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $unset操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildUnsetCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $addToSet操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildAddToSetCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $pull操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildPullCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

}
