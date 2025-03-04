package com.mongoplus.toolkit;

import com.mongodb.BasicDBObject;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.domain.MongoPlusFieldException;

import java.util.Map;

/**
 * @author JiaChaoYang
 **/
public class ExecuteUtil {

    public static BasicDBObject getFilter(Map<String, Object> entityMap) {
        if (!entityMap.containsKey(SqlOperationConstant._ID)) {
            throw new MongoPlusFieldException("_id undefined");
        }
        Object _idValue = entityMap.get(SqlOperationConstant._ID);
        BasicDBObject filter = new BasicDBObject(SqlOperationConstant._ID, ObjectIdUtil.getObjectIdValue(_idValue));
        entityMap.remove(SqlOperationConstant._ID);
        return filter;
    }

}
