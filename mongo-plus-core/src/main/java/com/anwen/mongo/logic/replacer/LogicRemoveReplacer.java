package com.anwen.mongo.logic.replacer;

import com.anwen.mongo.cache.global.CollectionLogicDeleteCache;
import com.anwen.mongo.enums.ExecuteMethodEnum;
import com.anwen.mongo.enums.SpecialConditionEnum;
import com.anwen.mongo.execute.Execute;
import com.anwen.mongo.logic.LogicDeleteHandler;
import com.anwen.mongo.model.LogicDeleteResult;
import com.anwen.mongo.model.MutablePair;
import com.anwen.mongo.replacer.Replacer;
import com.anwen.mongo.support.BoolFunction;
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
