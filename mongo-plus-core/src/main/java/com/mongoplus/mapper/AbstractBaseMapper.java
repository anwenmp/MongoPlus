package com.mongoplus.mapper;

import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.enums.CommandOperate;
import com.mongoplus.execute.Execute;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.index.impl.DefaultBaseIndexImpl;
import com.mongoplus.interceptor.InterceptorChain;
import com.mongoplus.interceptor.business.TenantInterceptor;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.logic.LogicDeleteHandler;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.manager.TenantManager;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.PageParam;
import com.mongoplus.model.PageResult;
import com.mongoplus.model.command.ParseCommand;
import com.mongoplus.parser.CommandParse;
import com.mongoplus.toolkit.*;
import com.mongoplus.toolkit.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.mongoplus.constant.SqlOperationConstant._ID;
import static com.mongoplus.enums.SpecialConditionEnum.EQ;
import static com.mongoplus.handlers.condition.BuildCondition.condition;

/**
 * 抽象的baseMapper
 *
 * @author anwen
 */
public abstract class AbstractBaseMapper extends DefaultBaseIndexImpl implements BaseMapper {

    private final Log log = LogFactory.getLog(AbstractBaseMapper.class);

    private final MongoPlusClient mongoPlusClient;

    private final MongoConverter mongoConverter;

    private final ExecutorFactory factory;

    public AbstractBaseMapper(MongoPlusClient mongoPlusClient, MongoConverter mongoConverter, ExecutorFactory factory) {
        super(mongoPlusClient,factory);
        this.mongoPlusClient = mongoPlusClient;
        this.mongoConverter = mongoConverter;
        this.factory = factory;
    }

    @Override
    public MongoPlusClient getMongoPlusClient() {
        return mongoPlusClient;
    }

    @Override
    public MongoConverter getMongoConverter() {
        return this.mongoConverter;
    }

    @Override
    public Execute getExecute() {
        return factory.getExecute();
    }

    @Override
    @Deprecated
    public <T> boolean save(String database, String collectionName, T entity,InsertManyOptions options) {
        Document document = new Document();
        mongoConverter.writeBySave(entity, document);
        InsertManyResult insertManyResult = factory.getExecute().executeSave(Collections.singletonList(document),
                options,
                mongoPlusClient.getCollection(database, collectionName));
        mongoConverter.reSetIdValue(entity, document);
        return insertManyResult.wasAcknowledged();
    }

    @Override
    public <T> boolean save(String database, String collectionName, T entity,InsertOneOptions options) {
        Document document = new Document();
        mongoConverter.writeBySave(entity, document);
        InsertOneResult insertOneResult = factory.getExecute().executeSaveOne(document,
                options,
                mongoPlusClient.getCollection(database, collectionName));
        mongoConverter.reSetIdValue(entity, document);
        return insertOneResult.wasAcknowledged();
    }

    @Override
    public <T> Boolean saveBatch(String database, String collectionName, Collection<T> entityList,InsertManyOptions options) {
        Assert.notEmpty(entityList, "entityList can not be empty");
        List<Document> documentList = new ArrayList<>(entityList.size());
        mongoConverter.writeBySaveBatch(entityList, documentList);
        MongoCollection<Document> collection = mongoPlusClient.getCollection(database, collectionName);
        InsertManyResult insertManyResult = factory.getExecute().executeSave(documentList,options, collection);
        mongoConverter.batchReSetIdValue(entityList, documentList);
        return insertManyResult.getInsertedIds().size() == entityList.size();
    }

    @Override
    public Long update(String database, String collectionName, Bson queryBasic, Bson updateBasic,
                       UpdateOptions options) {
        return factory.getExecute().executeUpdate(
                Collections.singletonList(new MutablePair<>(queryBasic, updateBasic)),
                options,
                mongoPlusClient.getCollection(database, collectionName)
        ).getModifiedCount();
    }

    @Override
    public Long updateOne(String database, String collectionName, Bson queryBasic, Bson updateBasic,
                       UpdateOptions options) {
        return factory.getExecute().executeUpdateOne(
                new MutablePair<>(queryBasic, updateBasic),
                options,
                mongoPlusClient.getCollection(database, collectionName)
        ).getModifiedCount();
    }

    @Override
    public <T> Boolean updateOne(T entity,QueryChainWrapper<T, ?> queryChainWrapper,UpdateOptions options) {
        MutablePair<BasicDBObject, BasicDBObject> updatePair =
                ConditionUtil.getUpdateCondition(queryChainWrapper.getCompareList(), entity, mongoConverter);
        MutablePair<String, String> namespace = getNamespace(entity.getClass());
        return updateOne(namespace.getLeft(), namespace.getRight(), updatePair.getLeft(), updatePair.getRight(),options) > 0;
    }


    @Override
    public Integer bulkWrite(String database, String collectionName, List<WriteModel<Document>> writeModelList,BulkWriteOptions options) {
        Assert.notEmpty(writeModelList, "writeModelList can not be empty");
        BulkWriteResult bulkWriteResult = factory.getExecute().executeBulkWrite(writeModelList, options,
                mongoPlusClient.getCollection(database, collectionName));
        return bulkWriteResult.getModifiedCount() + bulkWriteResult.getInsertedCount();
    }

    @Override
    public <T> Boolean update(String database, String collectionName, T entity,
                              QueryChainWrapper<T, ?> queryChainWrapper,UpdateOptions options) {
        MutablePair<BasicDBObject, BasicDBObject> updatePair =
                ConditionUtil.getUpdateCondition(queryChainWrapper.getCompareList(), entity, mongoConverter);
        return update(database, collectionName, updatePair.getLeft(), updatePair.getRight(),options) > 0;
    }

    @Override
    public boolean isExist(String database, String collectionName, Serializable id) {
        QueryWrapper<Object> wrapper = Wrappers.lambdaQuery().eq(_ID, id);
        return isExist(database, collectionName, wrapper);
    }

    @Override
    public boolean isExist(String database, String collectionName, QueryChainWrapper<?, ?> queryChainWrapper) {
        return factory.getExecute().executeCount(
                condition().queryCondition(queryChainWrapper).getCondition(),
                null,
                mongoPlusClient.getCollection(database, collectionName)) >= 1;
    }

    @Override
    public Boolean update(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper,
                          UpdateOptions options) {
        MutablePair<BasicDBObject, BasicDBObject> pair = updateChainWrapper.buildUpdateCondition();
        BasicDBObject targetBasicDBObject = new BasicDBObject();
        mongoConverter.write(pair.getRight(), targetBasicDBObject);
        return update(database, collectionName, pair.getLeft(), targetBasicDBObject,options) >= 1;
    }

    @Override
    public Boolean updateOne(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper,
                          UpdateOptions options) {
        MutablePair<BasicDBObject, BasicDBObject> pair = updateChainWrapper.buildUpdateCondition();
        BasicDBObject targetBasicDBObject = new BasicDBObject();
        mongoConverter.write(pair.getRight(), targetBasicDBObject);
        return updateOne(database, collectionName, pair.getLeft(), targetBasicDBObject,options) >= 1;
    }

    @Override
    public Boolean remove(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper,
                          DeleteOptions options) {
        return remove(database, collectionName, condition().queryCondition(updateChainWrapper).getCondition(),options) >= 1;
    }

    @Override
    public Boolean removeOne(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper,
                             DeleteOptions options) {
        return removeOne(database, collectionName, condition().queryCondition(updateChainWrapper).getCondition(),options) >= 1;
    }

    @Override
    public Long remove(String database, String collectionName, Bson filter,DeleteOptions options) {
        return factory.getExecute().executeRemove(filter,options, mongoPlusClient.getCollection(database, collectionName))
                .getDeletedCount();
    }

    @Override
    public Long removeOne(String database, String collectionName, Bson filter,DeleteOptions options) {
        return factory.getExecute().executeRemoveOne(filter,options, mongoPlusClient.getCollection(database, collectionName))
                .getDeletedCount();
    }

    @Override
    public long count(String database, String collectionName, QueryChainWrapper<?, ?> queryChainWrapper) {
        Execute execute = factory.getExecute();
        MongoCollection<Document> collection = mongoPlusClient.getCollection(database, collectionName);
        long line;
        if (canEstimatedDocumentCount(collection, queryChainWrapper)) {
            line = execute.estimatedDocumentCount(collection);
        } else {
            line = execute.executeCount(
                    condition().queryCondition(queryChainWrapper).getCondition(),
                    null,
                    collection
            );
        }
        return line;
    }

    /**
     * 判断是否可以使用 mongo  快速估计文档数量
     */
    private boolean canEstimatedDocumentCount(MongoCollection<Document> collection,
                                              QueryChainWrapper<?, ?> queryChainWrapper) {
        // 忽略逻辑删除 + 条件为空 + 忽略多租户
        return LogicDeleteHandler.close(collection)
                && !queryChainWrapper.isNotEmpty()
                && (TenantManager.getIgnoreTenant() != null ||
                InterceptorChain.getInterceptor(interceptor -> interceptor instanceof TenantInterceptor) == null);
    }

    @Override
    public long recentPageCount(String database, String collectionName, List<CompareCondition> compareConditionList,
                                Integer pageNum, Integer pageSize, Integer recentPageNum) {
        if (recentPageNum == null || !(recentPageNum <= 50 && recentPageNum >= 5)) {
            // 返回-1 表示不查询总条数
            return -1L;
        }
        //分页查询  不查询实际总条数  需要单独查询  是否有数据
        //如果recentPageNum = 10  第1-6页  总页数=10  从第7页开始 需要往后 + 4 页
        int limitParam = (pageNum < (recentPageNum / 2 + 1 + recentPageNum % 2) ?
                recentPageNum :
                (pageNum + (recentPageNum / 2 + recentPageNum % 2 - 1))) * pageSize;
        CountOptions countOptions = new CountOptions();
        countOptions.skip(limitParam).limit(1);
        MongoCollection<Document> collection = mongoPlusClient.getCollection(database, collectionName);
        long isExists = factory.getExecute().executeCount(
                condition().queryCondition(compareConditionList),
                countOptions,
                collection);
        //如果查询结果为空 则查询总条数，如果不为空则 limitParam为总条数
        if (isExists == 0) {
            // 查询真实总条数
            CountOptions countOptionsReal = new CountOptions();
            countOptionsReal.limit(limitParam);
            return factory.getExecute().executeCount(
                    condition().queryCondition(compareConditionList),
                    countOptions,
                    collection);
        }
        return limitParam;
    }

    @Override
    public <R> List<R> list(String database, String collectionName, Class<R> rClazz) {
        return list(database, collectionName, new TypeReference<R>(rClazz) {
        });
    }

    @Override
    public <R> List<R> list(String database, String collectionName, TypeReference<R> typeReference) {
        FindIterable<Document> findIterable = factory.getExecute().executeQuery(
                null,
                null,
                null,
                Document.class,
                mongoPlusClient.getCollection(database, collectionName)
        );
        return mongoConverter.read(findIterable, typeReference);
    }

    @Override
    public <T, R> List<R> list(String database, String collectionName, QueryChainWrapper<T, ?> queryChainWrapper,
                               Class<R> rClazz) {
        return list(database, collectionName, queryChainWrapper, new TypeReference<R>(rClazz) {});
    }

    @Override
    public <T, R> List<R> list(String database, String collectionName, QueryChainWrapper<T, ?> queryChainWrapper,
                               TypeReference<R> typeReference) {
        BaseConditionResult baseConditionResult = queryChainWrapper.buildCondition();
        FindIterable<Document> documentFindIterable = factory.getExecute().executeQuery(
                baseConditionResult.getCondition(),
                baseConditionResult.getProjection(),
                baseConditionResult.getSort(),
                Document.class,
                mongoPlusClient.getCollection(database, collectionName)
        );
        return mongoConverter.read(documentFindIterable, typeReference);
    }

    @Override
    public <R> List<R> aggregateList(String database, String collectionName, Aggregate<?> aggregate, Class<R> rClazz) {
        return aggregateList(database, collectionName, aggregate, new TypeReference<R>(rClazz) {
        });
    }

    @Override
    public <R> R aggregateOne(String database, String collectionName, Aggregate<?> aggregate, Class<R> rClazz) {
        return aggregateOne(database, collectionName, aggregate, new TypeReference<R>(rClazz) {
        });
    }

    @Override
    public <R> List<R> aggregateList(String database, String collectionName, Aggregate<?> aggregate,
                                     TypeReference<R> typeReference) {
        List<Bson> aggregateConditionList = aggregate.getAggregateConditionList();
        AggregateIterable<Document> aggregateIterable = factory.getExecute().executeAggregate(
                aggregateConditionList,
                Document.class,
                mongoPlusClient.getCollection(database, collectionName)
        );
        AggregateUtil.aggregateOptions(aggregateIterable, aggregate.getAggregateOptions());
        if (aggregate.isSkip()) {
            aggregateIterable.toCollection();
            return new ArrayList<>();
        }
        return mongoConverter.read(aggregateIterable, typeReference);
    }

    @Override
    public <R> R aggregateOne(String database, String collectionName, Aggregate<?> aggregate,
                              TypeReference<R> typeReference) {
        List<Bson> aggregateConditionList = aggregate.getAggregateConditionList();
        AggregateIterable<Document> aggregateIterable = factory.getExecute().executeAggregate(
                aggregateConditionList,
                Document.class,
                mongoPlusClient.getCollection(database, collectionName)
        );
        AggregateUtil.aggregateOptions(aggregateIterable, aggregate.getAggregateOptions());
        if (aggregate.isSkip()) {
            aggregateIterable.toCollection();
            return null;
        }
        return mongoConverter.readDocument(aggregateIterable, typeReference);
    }

    @Override
    public <T, R> R one(String database, String collectionName, QueryChainWrapper<T, ?> queryChainWrapper,
                        Class<R> rClazz) {
        return one(database, collectionName, queryChainWrapper, new TypeReference<R>(rClazz) {
        });
    }

    @Override
    public <T, R> R one(String database, String collectionName, QueryChainWrapper<T, ?> queryChainWrapper,
                        TypeReference<R> typeReference) {
        BaseConditionResult baseConditionResult = queryChainWrapper.buildCondition();
        return mongoConverter.readDocument(factory.getExecute().executeQuery(
                        baseConditionResult.getCondition(),
                        baseConditionResult.getProjection(),
                        baseConditionResult.getSort(),
                        Document.class,
                        mongoPlusClient.getCollection(database, collectionName)).limit(1),
                typeReference
        );
    }

    @Override
    public <T, R> PageResult<R> page(String database, String collectionName, QueryChainWrapper<T, ?> queryChainWrapper,
                                     Integer pageNum, Integer pageSize, Class<R> rClazz) {
        return page(database, collectionName, queryChainWrapper, pageNum, pageSize, new TypeReference<R>(rClazz) {
        });
    }

    @Override
    public <T, R> PageResult<R> page(String database, String collectionName, QueryChainWrapper<T, ?> queryChainWrapper,
                                     Integer pageNum, Integer pageSize, TypeReference<R> typeReference) {
        BaseConditionResult baseConditionResult = queryChainWrapper.buildCondition();
        MongoCollection<Document> collection = mongoPlusClient.getCollection(database, collectionName);
        long count;
        if (canEstimatedDocumentCount(collection, queryChainWrapper)) {
            count = factory.getExecute().estimatedDocumentCount(collection);
        } else {
            count = count(database, collectionName, queryChainWrapper);
        }
        FindIterable<Document> iterable = factory.getExecute().executeQuery(
                baseConditionResult.getCondition(),
                baseConditionResult.getProjection(),
                baseConditionResult.getSort(),
                Document.class,
                collection
        );
        return getPageResult(
                iterable,
                count,
                new PageParam(pageNum, pageSize),
                typeReference,
                mongoConverter
        );
    }

    @Override
    public <T, R> List<R> pageList(String database, String collectionName, QueryChainWrapper<T, ?> queryChainWrapper,
                                   Integer pageNum, Integer pageSize, Class<R> rClazz) {
        return pageList(database, collectionName, queryChainWrapper, pageNum, pageSize, new TypeReference<R>(rClazz) {
        });
    }

    @Override
    public <T, R> List<R> pageList(String database, String collectionName, QueryChainWrapper<T, ?> queryChainWrapper,
                                   Integer pageNum, Integer pageSize, TypeReference<R> typeReference) {
        BaseConditionResult baseConditionResult = queryChainWrapper.buildCondition();
        FindIterable<Document> iterable = factory.getExecute().executeQuery(
                baseConditionResult.getCondition(),
                baseConditionResult.getProjection(),
                baseConditionResult.getSort(),
                Document.class,
                mongoPlusClient.getCollection(database, collectionName)
        );
        return mongoConverter.read(iterable.skip((pageNum - 1) * pageSize).limit(pageSize), typeReference);
    }

    @Override
    public <T, R> PageResult<R> page(String database, String collectionName, QueryChainWrapper<T, ?> queryChainWrapper,
                                     Integer pageNum, Integer pageSize, Integer recentPageNum, Class<R> rClazz) {
        return page(database, collectionName, queryChainWrapper, pageNum, pageSize, recentPageNum,
                new TypeReference<R>(rClazz) {
                }
        );
    }

    @Override
    public <T, R> PageResult<R> page(String database, String collectionName, QueryChainWrapper<T, ?> queryChainWrapper,
                                     Integer pageNum, Integer pageSize, Integer recentPageNum, TypeReference<R> typeReference) {
        BaseConditionResult baseConditionResult = queryChainWrapper.buildCondition();
        MongoCollection<Document> collection = mongoPlusClient.getCollection(database, collectionName);
        long count;
        if (canEstimatedDocumentCount(collection, queryChainWrapper)) {
            count = factory.getExecute().estimatedDocumentCount(collection);
        } else {
            count = recentPageCount(database, collectionName, queryChainWrapper.getCompareList(), pageNum, pageSize, recentPageNum);
        }
        FindIterable<Document> iterable = factory.getExecute().executeQuery(
                baseConditionResult.getCondition(),
                baseConditionResult.getProjection(),
                baseConditionResult.getSort(),
                Document.class, collection
        );
        return getPageResult(
                iterable,
                count,
                new PageParam(pageNum, pageSize),
                typeReference,
                mongoConverter
        );
    }

    @Override
    public <R> List<R> getByIds(String database, String collectionName, Collection<? extends Serializable> ids,
                                Class<R> rClazz) {
        return getByIds(database, collectionName, ids, new TypeReference<R>(rClazz) {
        });
    }

    @Override
    public <R> List<R> getByIds(String database, String collectionName, Collection<? extends Serializable> ids,
                                TypeReference<R> typeReference) {
        FindIterable<Document> iterable = factory.getExecute().executeQuery(
                BsonUtil.getIdsCondition(ids),
                null,
                null,
                Document.class,
                mongoPlusClient.getCollection(database, collectionName)
        );
        return mongoConverter.read(iterable, typeReference);
    }

    @Override
    public <R> R getById(String database, String collectionName, Serializable id, Class<R> rClazz) {
        return getById(database, collectionName, id, new TypeReference<R>(rClazz) {
        });
    }

    @Override
    public <R> R getById(String database, String collectionName, Serializable id, TypeReference<R> typeReference) {
        BasicDBObject queryBasic = new BasicDBObject(_ID,
                new BasicDBObject(EQ.getCondition(), ObjectIdUtil.getObjectIdValue(id)));
        return mongoConverter.read(factory.getExecute().executeQuery(
                queryBasic,
                null,
                null,
                Document.class,
                mongoPlusClient.getCollection(database, collectionName)).first(), typeReference);
    }


    @Override
    public <R> List<R> queryCommand(String database, String collectionName, String command, Class<R> rClazz) {
        return queryCommand(database, collectionName, command, new TypeReference<R>(rClazz) {
        });
    }

    @Override
    public <R> List<R> queryCommand(String database, String collectionName, String command,
                                    TypeReference<R> typeReference) {
        FindIterable<Document> iterable = factory.getExecute().executeQuery(
                BasicDBObject.parse(command),
                null,
                null,
                Document.class,
                mongoPlusClient.getCollection(database, collectionName)
        );
        return mongoConverter.read(iterable, typeReference);
    }

    @Override
    public <R> List<R> getByColumn(String database, String collectionName, String column, Object value,
                                   Class<R> rClazz) {
        return getByColumn(database, collectionName, column, value, new TypeReference<R>(rClazz) {
        });
    }

    @Override
    public <R> List<R> getByColumn(String database, String collectionName, String column, Object value,
                                   TypeReference<R> typeReference) {
        Bson filter = Filters.eq(column, ObjectIdUtil.getObjectIdValue(value));
        return mongoConverter.read(factory.getExecute().executeQuery(
                        filter,
                        null,
                        null,
                        Document.class,
                        mongoPlusClient.getCollection(database, collectionName)),
                typeReference
        );
    }

    @Override
    public long count(String database, String collectionName) {
        MongoCollection<Document> collection = mongoPlusClient.getCollection(database, collectionName);
        Execute execute = factory.getExecute();
        long line;
        if (canEstimatedDocumentCount(collection, null)) {
            line = execute.estimatedDocumentCount(collection);
        } else {
            line = execute.executeCount(null, null, collection);
        }
        return line;
    }

    @Override
    public <T> List<T> command(String database, String command, TypeReference<T> typeReference) {
        Execute execute = factory.getExecute();
        ParseCommand parseCommand = CommandParse.parserInstance.parse(command);
        CommandOperate commandOperate = CommandOperate.getCommandOperate(parseCommand.getOperate());
        MongoCollection<Document> collection = mongoPlusClient.getCollection(database,parseCommand.getCollection());
        MongoIterable<Document> iterable;
        if (commandOperate == CommandOperate.FIND){
            iterable = execute.executeQuery(
                    parseCommand.getBsonCommand(),
                    null,
                    null,
                    Document.class,
                    collection
            );
        } else {
            iterable = execute.executeAggregate(parseCommand.getBsonListCommand(),Document.class,collection);
        }
        return mongoConverter.read(iterable,typeReference);
    }

    public <T> PageResult<T> getPageResult(FindIterable<Document> documentFindIterable, long totalSize,
                                           PageParam pageParams, TypeReference<T> typeReference,
                                           MongoConverter mongoConverter) {
        List<T> pageContentData = mongoConverter.read(
                documentFindIterable
                        .skip((pageParams.getPageNum() - 1) * pageParams.getPageSize())
                        .limit(pageParams.getPageSize()),
                typeReference
        );
        // 不查询总条数，总条数=当前页的总数
        if (totalSize == -1) {
            totalSize = pageContentData.size();
        }
        return new PageResult<>(
                pageParams.getPageNum(),
                pageParams.getPageSize(),
                totalSize,
                ((totalSize + pageParams.getPageSize() - 1) / pageParams.getPageSize()),
                pageContentData
        );
    }


}
