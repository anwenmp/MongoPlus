package com.mongoplus.logic;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongoplus.enums.SpecialConditionEnum;
import com.mongoplus.execute.Execute;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.model.LogicDeleteResult;
import com.mongoplus.model.MutablePair;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;

/**
 * @author anwen
 */
public class LogicRemove {

    /**
     * 执行逻辑删除
     * @param invocation invocation
     * @return {@link com.mongodb.client.result.UpdateResult}
     * @author anwen
     */
    public static Object logic(Invocation invocation,MongoCollection<Document> collection) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArgs();
        Object target = invocation.getTarget();
        if (LogicManager.isIgnoreLogic()) {
            return invocation.proceed();
        }
        Class<?> clazz = LogicDeleteHandler.getBeanClass(collection);
        if (Objects.isNull(clazz)) {
            return invocation.proceed();
        }
        LogicDeleteResult result = LogicDeleteHandler.mapper().get(clazz);
        if (Objects.isNull(result)) {
            return invocation.proceed();
        }
        Execute execute = (Execute) target;
        Document updateBasic = new Document(result.getColumn(), result.getLogicDeleteValue());
        BasicDBObject update = new BasicDBObject(SpecialConditionEnum.SET.getCondition(), updateBasic);
        UpdateResult updateResult = execute.executeUpdate(
                Collections.singletonList(new MutablePair<>((Bson) args[0], update)), (UpdateOptions) args[1], collection
        );
        return new DeleteResult() {
            @Override
            public boolean wasAcknowledged() {
                return updateResult.wasAcknowledged();
            }

            @Override
            public long getDeletedCount() {
                return updateResult.getModifiedCount();
            }
        };
    }

}
