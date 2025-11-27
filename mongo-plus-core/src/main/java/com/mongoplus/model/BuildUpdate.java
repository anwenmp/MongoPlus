package com.mongoplus.model;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.condition.ConditionMetaObject;

/**
 * @author anwen
 */
public class BuildUpdate {

    private ConditionMetaObject currentConditionMetaObject;

    private BasicDBObject updateBasicDBObject;

    public BuildUpdate(ConditionMetaObject currentConditionMetaObject, BasicDBObject updateBasicDBObject) {
        this.currentConditionMetaObject = currentConditionMetaObject;
        this.updateBasicDBObject = updateBasicDBObject;
    }

    public ConditionMetaObject getCurrentCompareCondition() {
        return currentConditionMetaObject;
    }

    public void setCurrentCompareCondition(ConditionMetaObject currentConditionMetaObject) {
        this.currentConditionMetaObject = currentConditionMetaObject;
    }

    public BasicDBObject getUpdateBasicDBObject() {
        return updateBasicDBObject;
    }

    public void setUpdateBasicDBObject(BasicDBObject updateBasicDBObject) {
        this.updateBasicDBObject = updateBasicDBObject;
    }
}
