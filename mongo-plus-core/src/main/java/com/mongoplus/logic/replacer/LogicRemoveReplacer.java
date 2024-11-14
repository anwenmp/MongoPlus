package com.mongoplus.logic.replacer;

import com.mongoplus.cache.global.CollectionLogicDeleteCache;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.enums.SpecialConditionEnum;
import com.mongoplus.execute.Execute;
import com.mongoplus.logic.LogicDeleteHandler;
import com.mongoplus.model.LogicDeleteResult;
import com.mongoplus.model.MutablePair;
import com.mongoplus.replacer.Replacer;
import com.mongoplus.support.BoolFunction;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;

/**
 * 逻辑删除替换器
 *
 * @author loser
 * @date 2024/4/30
 */
public class LogicRemoveReplacer implements Replacer {

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Object target, Method method, Object[] args) throws Throwable {

        if (CollectionLogicDeleteCache.getLogicIgnore()) {
            return method.invoke(target, args);
        }
        MongoCollection<Document> collection = (MongoCollection<Document>) args[1];
        Class<?> clazz = LogicDeleteHandler.getBeanClass(collection);
        if (Objects.isNull(clazz)) {
            return method.invoke(target, args);
        }
        LogicDeleteResult result = LogicDeleteHandler.mapper().get(clazz);
        if (Objects.isNull(result)) {
            return method.invoke(target, args);
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

    @Override
    public BoolFunction supplier() {
        return (proxy, target, method, args) -> CollectionLogicDeleteCache.open && method.getName().equals(ExecuteMethodEnum.REMOVE.getMethod());
    }

}
