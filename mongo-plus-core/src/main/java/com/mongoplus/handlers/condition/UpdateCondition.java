package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.condition.ConditionMetaObject;
import com.mongoplus.model.BuildUpdate;

import java.util.List;

/**
 * 修改条件
 * @author anwen
 */
public interface UpdateCondition {

    /**
     * 通用更新操作符具体的构建方法
     * @param conditionMetaObjectList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildUpdateCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);

    /**
     * $push操作符具体的构建方法
     * @param conditionMetaObjectList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildPushCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);

    /**
     * $currentDate操作符具体的构建方法
     * @param conditionMetaObjectList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildCurrentDateCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);

    /**
     * $rename操作符具体的构建方法
     * @param conditionMetaObjectList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildRenameCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);

    /**
     * $unset操作符具体的构建方法
     * @param conditionMetaObjectList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildUnsetCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);

    /**
     * $addToSet操作符具体的构建方法
     * @param conditionMetaObjectList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildAddToSetCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);

    /**
     * $pull操作符具体的构建方法
     * @param conditionMetaObjectList 条件集合
     * @param buildUpdate 条件
     * @return {@link com.mongodb.BasicDBObject}
     * @author anwen
     */
    BasicDBObject buildPullCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);

}
