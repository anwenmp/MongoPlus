package com.mongoplus.toolkit;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.enums.SpecialConditionEnum;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.model.MutablePair;
import org.bson.Document;

import java.util.List;

import static com.mongoplus.handlers.condition.BuildCondition.condition;

public class ConditionUtil {

    /**
     * 将条件转为MongoDB可用条件
     * @author anwen
     */
    public static MutablePair<BasicDBObject,BasicDBObject> getUpdateCondition(
            List<CompareCondition> compareConditionList, Object sourceObj, MongoConverter mongoConverter){
        BasicDBObject queryBasic = condition().queryCondition(compareConditionList);
        Document document = mongoConverter.writeByUpdate(sourceObj);
        document.remove(SqlOperationConstant._ID);
        BasicDBObject updateField = new BasicDBObject(SpecialConditionEnum.SET.getCondition(), document);
        return new MutablePair<>(queryBasic,updateField);
    }

    /**
     * 将实体构建为Id条件
     * @param sourceObj 实体
     * @param mongoConverter 转换器
     * @return {@link MutablePair}
     * @author anwen
     */
    public static MutablePair<BasicDBObject,BasicDBObject> getUpdate(Object sourceObj,MongoConverter mongoConverter) {
        Document document = mongoConverter.writeByUpdate(sourceObj);
        BasicDBObject filter = ExecuteUtil.getFilter(document);
        BasicDBObject update = new BasicDBObject(SpecialConditionEnum.SET.getCondition(), document);
        return new MutablePair<>(filter,update);
    }

}
