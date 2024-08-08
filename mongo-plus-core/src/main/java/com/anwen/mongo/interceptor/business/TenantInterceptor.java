package com.anwen.mongo.interceptor.business;

import com.anwen.mongo.aggregate.AggregateWrapper;
import com.anwen.mongo.cache.codec.MapCodecCache;
import com.anwen.mongo.cache.global.DataSourceNameCache;
import com.anwen.mongo.cache.global.TenantCache;
import com.anwen.mongo.conditions.query.QueryWrapper;
import com.anwen.mongo.enums.AggregateEnum;
import com.anwen.mongo.handlers.TenantHandler;
import com.anwen.mongo.interceptor.Interceptor;
import com.anwen.mongo.model.MutablePair;
import com.anwen.mongo.model.QueryParam;
import com.anwen.mongo.toolkit.BsonUtil;
import com.anwen.mongo.toolkit.CollUtil;
import com.anwen.mongo.toolkit.Filters;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 多租户拦截器
 *
 * @author anwen
 * @date 2024/6/27 上午10:56
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
        if (isTenantIgnored(collection) || tenantHandler.ignoreInsert(new ArrayList<>(documentList.get(0).keySet()),
                tenantHandler.getTenantIdColumn())) {
            return documentList;
        }
        documentList.forEach(document ->
                document.putIfAbsent(tenantHandler.getTenantIdColumn(), tenantHandler.getTenantId())
        );
        return documentList;
    }

    @Override
    public Bson executeRemove(Bson filter, MongoCollection<Document> collection) {
        return appendTenantFilter(filter, collection);
    }

    @Override
    public List<MutablePair<Bson, Bson>> executeUpdate(List<MutablePair<Bson, Bson>> updatePairList,
                                                       MongoCollection<Document> collection) {
        if (!isTenantIgnored(collection)) {
            updatePairList.forEach(pair -> pair.setLeft(appendTenantFilter(pair.getLeft(), collection)));
        }
        return updatePairList;
    }

    @Override
    public QueryParam executeQuery(Bson queryBasic, BasicDBObject projectionList, BasicDBObject sortCond,
                                   MongoCollection<Document> collection) {
        return new QueryParam(appendTenantFilter(queryBasic, collection), projectionList, sortCond);
    }

    @Override
    public List<Bson> executeAggregates(List<Bson> aggregateConditionList, MongoCollection<Document> collection) {
        if (!isTenantIgnored(collection)) {
            Bson matchBson = new AggregateWrapper().match(new QueryWrapper<>().eq(tenantHandler.getTenantIdColumn(),
                    tenantHandler.getTenantId())).getAggregateConditionList().get(0);
            boolean hasMatch = aggregateConditionList.stream()
                    .anyMatch(bson -> bson.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry())
                            .containsKey(AggregateEnum.MATCH.getValue()));

            if (hasMatch) {
                aggregateConditionList.forEach(bson -> {
                    BsonDocument bsonDocument = bson.toBsonDocument(BsonDocument.class,
                            MapCodecCache.getDefaultCodecRegistry());
                    if (bsonDocument.containsKey(AggregateEnum.MATCH.getValue())) {
                        BsonDocument matchBsonDocument = bsonDocument.get(AggregateEnum.MATCH.getValue()).asDocument();
                        matchBsonDocument.putIfAbsent(tenantHandler.getTenantIdColumn(), tenantHandler.getTenantId());
                    }
                });
            } else {
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
        if (!isTenantIgnored(collection) && CollUtil.isNotEmpty(writeModelList)) {
            List<Document> insertDocumentList = writeModelList.stream()
                    .filter(writeModel -> writeModel instanceof InsertOneModel)
                    .map(writeModel -> ((InsertOneModel<Document>) writeModel).getDocument())
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(insertDocumentList)) {
                executeSave(insertDocumentList, collection);
            }

            List<MutablePair<Bson, Bson>> updatePairList = writeModelList.stream()
                    .filter(writeModel -> writeModel instanceof UpdateManyModel)
                    .map(writeModel -> new MutablePair<>(((UpdateManyModel<Document>) writeModel).getFilter(),
                            ((UpdateManyModel<Document>) writeModel).getUpdate()))
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(updatePairList)) {
                executeUpdate(updatePairList, collection);
            }
        }
        return writeModelList;
    }

    private boolean isTenantIgnored(MongoCollection<Document> collection) {
        MongoNamespace namespace = collection.getNamespace();
        String collectionName = namespace.getCollectionName();
        String databaseName = namespace.getDatabaseName();
        String dataSource = DataSourceNameCache.getDataSource();
        Boolean ignoreTenant = TenantCache.getIgnoreTenant();

        return ignoreTenant != null ?
                ignoreTenant :
                tenantHandler.ignoreCollection(collectionName) ||
                        tenantHandler.ignoreDatabase(databaseName) ||
                        tenantHandler.ignoreDataSource(dataSource);
    }

    @SuppressWarnings("unchecked")
    private <T extends Bson> T appendTenantFilter(T filter, MongoCollection<Document> collection) {
        if (filter == null){
            filter = (T) new Document();
        }
        if (!isTenantIgnored(collection)) {
            BsonDocument filterDoc = filter.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry());
            if (!filterDoc.containsKey(tenantHandler.getTenantIdColumn())) {
                BsonUtil.addToMap(filter, tenantHandler.getTenantIdColumn(), tenantHandler.getTenantId());
            }
        }
        return filter;
    }
}
