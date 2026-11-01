package com.mongoplus.interceptor.business;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.aggregate.AggregateWrapper;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.enums.AggregateEnum;
import com.mongoplus.handlers.TenantHandler;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.QueryParam;
import com.mongoplus.toolkit.BsonUtil;
import com.mongoplus.toolkit.CollUtil;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongoplus.enums.QueryOperatorEnum.EQ;
import static com.mongoplus.manager.TenantManager.isTenantIgnored;

/**
 * 多租户拦截器
 *
 * @author anwen
 */
public class TenantInterceptor implements Interceptor {

    private final TenantHandler tenantHandler;

    public TenantInterceptor(TenantHandler tenantHandler) {
        this.tenantHandler = tenantHandler;
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection) {
        if (isTenantIgnored(collection, tenantHandler) || tenantHandler.ignoreInsert(new ArrayList<>(documentList.get(0).keySet()),
                tenantHandler.getTenantIdColumn())) {
            return documentList;
        }
        documentList.forEach(document ->
                document.putIfAbsent(tenantHandler.getTenantIdColumn(), tenantHandler.getTenantId())
        );
        return documentList;
    }

    @Override
    public Document executeSave(Document document, MongoCollection<Document> collection) {
        if (isTenantIgnored(collection, tenantHandler) || tenantHandler.ignoreInsert(new ArrayList<>(document.keySet()),
                tenantHandler.getTenantIdColumn())) {
            return document;
        }
        document.putIfAbsent(tenantHandler.getTenantIdColumn(), tenantHandler.getTenantId());
        return document;
    }

    @Override
    public Bson executeRemove(Bson filter, MongoCollection<Document> collection) {
        return appendTenantFilter(filter, collection);
    }

    @Override
    public List<MutablePair<Bson, Bson>> executeUpdate(List<MutablePair<Bson, Bson>> updatePairList,
                                                       MongoCollection<Document> collection) {
        if (!isTenantIgnored(collection, tenantHandler)) {
            updatePairList.forEach(pair -> pair.setLeft(appendTenantFilter(pair.getLeft(), collection)));
        }
        return updatePairList;
    }

    @Override
    public MutablePair<Bson, Bson> executeUpdate(MutablePair<Bson, Bson> updatePair,
                                                       MongoCollection<Document> collection) {
        if (!isTenantIgnored(collection, tenantHandler)) {
            updatePair.setLeft(appendTenantFilter(updatePair.getLeft(), collection));
        }
        return updatePair;
    }

    @Override
    public QueryParam executeQuery(Bson queryBasic, BasicDBObject projectionList, BasicDBObject sortCond,
                                   MongoCollection<Document> collection) {
        return new QueryParam(appendTenantFilter(queryBasic, collection), projectionList, sortCond);
    }

    @Override
    public List<Bson> executeAggregates(List<Bson> aggregateConditionList, MongoCollection<Document> collection) {
        if (!isTenantIgnored(collection, tenantHandler)) {
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
                                        tenantHandler.getTenantIdColumn(),
                                        new BsonDocument(EQ.getOperatorValue(), tenantHandler.getTenantId())
                                );
                                return bsonDocument;
                            }
                            return bson;
                        }).collect(Collectors.toList());
            } else {
                Bson matchBson = new AggregateWrapper().match(new QueryWrapper<>().eq(tenantHandler.getTenantIdColumn(),
                        tenantHandler.getTenantId())).getAggregateConditionList().get(0);
                aggregateConditionList.add(0, matchBson);
            }
        }
        return aggregateConditionList;
    }

    @Override
    public MutablePair<BasicDBObject, CountOptions> executeCount(BasicDBObject queryBasic, CountOptions countOptions,
                                                                 MongoCollection<Document> collection) {
        return new MutablePair<>(appendTenantFilter(queryBasic, collection), countOptions);
    }

    @Override
    public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                                       MongoCollection<Document> collection) {
        if (!isTenantIgnored(collection, tenantHandler) && CollUtil.isNotEmpty(writeModelList)) {
            List<Document> insertDocumentList = writeModelList.stream()
                    .filter(writeModel -> writeModel instanceof InsertOneModel)
                    .map(writeModel -> ((InsertOneModel<Document>) writeModel).getDocument())
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(insertDocumentList)) {
                executeSave(insertDocumentList, collection);
            }

            writeModelList = writeModelList.stream().map(writeModel -> {
                if (writeModel instanceof UpdateManyModel) {
                    UpdateManyModel<Document> updateManyModel = (UpdateManyModel<Document>) writeModel;
                    Bson filter = appendTenantFilter(updateManyModel.getFilter(), collection);
                    if (updateManyModel.getUpdatePipeline() != null) {
                        return new UpdateManyModel<Document>(filter, updateManyModel.getUpdatePipeline(),
                                updateManyModel.getOptions());
                    }
                    if (updateManyModel.getUpdate() != null) {
                        return new UpdateManyModel<Document>(filter, updateManyModel.getUpdate(),
                                updateManyModel.getOptions());
                    }
                } else if (writeModel instanceof UpdateOneModel) {
                    UpdateOneModel<Document> updateOneModel = (UpdateOneModel<Document>) writeModel;
                    Bson filter = appendTenantFilter(updateOneModel.getFilter(), collection);
                    if (updateOneModel.getUpdatePipeline() != null) {
                        return new UpdateOneModel<Document>(filter, updateOneModel.getUpdatePipeline(),
                                updateOneModel.getOptions());
                    }
                    return new UpdateOneModel<Document>(filter, updateOneModel.getUpdate(),
                            updateOneModel.getOptions());
                } else if (writeModel instanceof ReplaceOneModel) {
                    ReplaceOneModel<Document> replaceOneModel = (ReplaceOneModel<Document>) writeModel;
                    Bson filter = appendTenantFilter(replaceOneModel.getFilter(), collection);
                    // A replacement without its tenant field would remove the scoped row's tenant identity.
                    Document replacement = executeSave(replaceOneModel.getReplacement(), collection);
                    return new ReplaceOneModel<Document>(filter, replacement, replaceOneModel.getReplaceOptions());
                } else if (writeModel instanceof DeleteOneModel) {
                    DeleteOneModel<Document> deleteOneModel = (DeleteOneModel<Document>) writeModel;
                    return new DeleteOneModel<Document>(appendTenantFilter(deleteOneModel.getFilter(), collection),
                            deleteOneModel.getOptions());
                } else if (writeModel instanceof DeleteManyModel) {
                    DeleteManyModel<Document> deleteManyModel = (DeleteManyModel<Document>) writeModel;
                    return new DeleteManyModel<Document>(appendTenantFilter(deleteManyModel.getFilter(), collection),
                            deleteManyModel.getOptions());
                }
                return writeModel;
            }).collect(Collectors.toList());
        }
        return writeModelList;
    }

    @SuppressWarnings("unchecked")
    private <T extends Bson> T appendTenantFilter(T filter, MongoCollection<Document> collection) {
        if (filter == null) {
            filter = (T) new Document();
        }
        if (!isTenantIgnored(collection, tenantHandler)) {
            BsonDocument filterDoc = BsonUtil.asBsonDocument(filter);
            if (!filterDoc.containsKey(tenantHandler.getTenantIdColumn())) {
                filter = (T) BsonUtil.addToMap(filter, tenantHandler.getTenantIdColumn(), tenantHandler.getTenantId());
            }
        }
        return filter;
    }
}
