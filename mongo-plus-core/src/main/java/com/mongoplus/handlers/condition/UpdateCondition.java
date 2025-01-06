package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.model.BuildUpdate;

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

    /**
     * $currentDate操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午6:40
     */
    BasicDBObject buildCurrentDateCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $rename操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午6:40
     */
    BasicDBObject buildRenameCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $unset操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午6:40
     */
    BasicDBObject buildUnsetCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $addToSet操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午6:40
     */
    BasicDBObject buildAddToSetCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

    /**
     * $pull操作符具体的构建方法
     * @param compareConditionList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     * @date 2024/8/1 下午6:40
     */
    BasicDBObject buildPullCondition(List<CompareCondition> compareConditionList, BuildUpdate buildUpdate);

}
