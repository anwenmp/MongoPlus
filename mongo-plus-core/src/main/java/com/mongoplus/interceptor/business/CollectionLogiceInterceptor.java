package com.mongoplus.interceptor.business;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.aggregate.AggregateWrapper;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.enums.AggregateEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.logic.LogicDeleteHandler;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.model.LogicDeleteResult;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.QueryParam;
import com.mongoplus.toolkit.BsonUtil;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mongoplus.enums.QueryOperatorEnum.EQ;

/**
 * 逻辑删除拦截器
 *
 * @author loser
 */
public class CollectionLogiceInterceptor implements Interceptor {

    @Override
    public Bson executeRemove(Bson filter, MongoCollection<Document> collection) {
        return remove(filter, collection);
    }

    @Override
    public Bson executeRemoveOne(Bson filter, MongoCollection<Document> collection) {
        return remove(filter, collection);
    }

    Bson remove(Bson filter, MongoCollection<Document> collection) {
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
    public QueryParam executeQuery(Bson queryBasic, BasicDBObject projectionList, BasicDBObject sortCond,
                                   MongoCollection<Document> collection) {

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
    public MutablePair<BasicDBObject, CountOptions> executeCount(BasicDBObject queryBasic, CountOptions countOptions,
                                                                 MongoCollection<Document> collection) {

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
    public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                                       MongoCollection<Document> collection) {

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
                Optional.ofNullable(umm.getOptions())
                        .map(u -> new UpdateManyModel(filter, umm.getUpdate(),u))
                        .orElseGet(() -> new UpdateManyModel(filter, umm.getUpdate()));
                return new UpdateManyModel<Document>(filter, umm.getUpdate());
            }
            return item;
        }).collect(Collectors.toList());

    }

    @Override
    public List<Bson> executeAggregates(List<Bson> aggregateConditionList, MongoCollection<Document> collection) {
        if (LogicManager.isIgnoreLogic()) {
            return aggregateConditionList;
        }
        Class<?> clazz = LogicDeleteHandler.getBeanClass(collection);
        if (LogicDeleteHandler.close() || Objects.isNull(clazz)) {
            return aggregateConditionList;
        }
        LogicDeleteResult result = LogicDeleteHandler.getLogicDeletedResult(clazz);
        if (Objects.isNull(result)) {
            return aggregateConditionList;
        }
        boolean hasMatch = aggregateConditionList.stream()
                .anyMatch(bson -> BsonUtil.asBsonDocument(bson)
                        .containsKey(AggregateEnum.MATCH.getValue()));
        if (hasMatch) {
            aggregateConditionList = aggregateConditionList.stream()
                    .map(bson -> {
                        BsonDocument bsonDocument = bson.toBsonDocument(BsonDocument.class,
                                MapCodecCache.getDefaultCodecRegistry());
                        if (bsonDocument.containsKey(AggregateEnum.MATCH.getValue())) {
                            BsonDocument matchBsonDocument = bsonDocument.get(AggregateEnum.MATCH.getValue())
                                    .asDocument();
                            matchBsonDocument.putIfAbsent(
                                    result.getColumn(),
                                    new BsonDocument(EQ.getOperatorValue(),
                                            result.getLogicNotDeleteBsonValue())
                            );
                            return bsonDocument;
                        }
                        return bson;
                    })
                    .collect(Collectors.toList());
        } else {
            Bson matchBson = new AggregateWrapper().match(matchWrapper ->
                            matchWrapper.eq(result.getColumn(), result.getLogicNotDeleteValue()))
                    .getAggregateConditionList().get(0);
            aggregateConditionList.add(matchBson);
        }
        return aggregateConditionList;
    }
}
