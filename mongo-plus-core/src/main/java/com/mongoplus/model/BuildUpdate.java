package com.mongoplus.model;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;

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
