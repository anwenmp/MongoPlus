package com.mongoplus.interceptor.business;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.logic.LogicDeleteHandler;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.QueryParam;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 逻辑删除拦截器
 *
 * @author loser
 * @date 2024/4/30
 */
public class CollectionLogiceInterceptor implements Interceptor {

    @Override
    public Bson executeRemove(Bson filter, MongoCollection<Document> collection) {

        if (LogicManager.isIgnoreLogic()) {
            return filter;
        }
        Class<?> clazz = LogicDeleteHandler.getBeanClass(collection);
        if (LogicDeleteHandler.close() || Objects.isNull(clazz)) {
            return filter;
        }
        return LogicDeleteHandler.doBsonLogicDel(filter, clazz);

    }

    @Override
    public QueryParam executeQuery(Bson queryBasic, BasicDBObject projectionList, BasicDBObject sortCond, MongoCollection<Document> collection) {

        if (LogicManager.isIgnoreLogic()) {
            return new QueryParam(queryBasic, projectionList, sortCond);
        }
        Class<?> clazz = LogicDeleteHandler.getBeanClass(collection);
        if (LogicDeleteHandler.close() || Objects.isNull(clazz)) {
            return new QueryParam(queryBasic, projectionList, sortCond);
        }
        Bson query = LogicDeleteHandler.doBsonLogicDel(queryBasic, clazz);
        return new QueryParam(query, projectionList, sortCond);

    }

    @Override
    public MutablePair<BasicDBObject, CountOptions> executeCount(BasicDBObject queryBasic, CountOptions countOptions, MongoCollection<Document> collection) {

        if (LogicManager.isIgnoreLogic()) {
            return new MutablePair<>(queryBasic, countOptions);
        }
        Class<?> clazz = LogicDeleteHandler.getBeanClass(collection);
        if (LogicDeleteHandler.close() || Objects.isNull(clazz)) {
            return new MutablePair<>(queryBasic, countOptions);
        }
        BasicDBObject query = (BasicDBObject) LogicDeleteHandler.doBsonLogicDel(queryBasic, clazz);
        return new MutablePair<>(query, countOptions);

    }

    @Override
    @SuppressWarnings("all")
    public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList, MongoCollection<Document> collection) {

        if (LogicManager.isIgnoreLogic()) {
            return writeModelList;
        }
        Class<?> clazz = LogicDeleteHandler.getBeanClass(collection);
        if (LogicDeleteHandler.close() || Objects.isNull(clazz)) {
            return writeModelList;
        }
        return writeModelList.stream().map(item -> {
            if (item instanceof UpdateManyModel) {
                UpdateManyModel umm = (UpdateManyModel) item;
                Bson filter = LogicDeleteHandler.doBsonLogicDel(umm.getFilter(), clazz);
                return new UpdateManyModel<Document>(filter, umm.getUpdate());
            }
            return item;
        }).collect(Collectors.toList());

    }

}
