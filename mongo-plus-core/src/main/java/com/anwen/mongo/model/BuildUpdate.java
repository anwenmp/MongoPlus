package com.anwen.mongo.model;

import com.anwen.mongo.conditions.interfaces.condition.CompareCondition;
import com.mongodb.BasicDBObject;

/**
 * @author anwen
 */
public class BuildUpdate {

    private CompareCondition currentCompareCondition;

    private BasicDBObject updateBasicDBObject;

    public BuildUpdate(CompareCondition currentCompareCondition, BasicDBObject updateBasicDBObject) {
        this.currentCompareCondition = currentCompareCondition;
        this.updateBasicDBObject = updateBasicDBObject;
    }

    public CompareCondition getCurrentCompareCondition() {
        return currentCompareCondition;
    }

    public void setCurrentCompareCondition(CompareCondition currentCompareCondition) {
        this.currentCompareCondition = currentCompareCondition;
    }

    public BasicDBObject getUpdateBasicDBObject() {
        return updateBasicDBObject;
    }

    public void setUpdateBasicDBObject(BasicDBObject updateBasicDBObject) {
        this.updateBasicDBObject = updateBasicDBObject;
    }
}
