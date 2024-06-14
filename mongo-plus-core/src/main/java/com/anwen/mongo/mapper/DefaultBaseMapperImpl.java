package com.anwen.mongo.mapper;

import com.anwen.mongo.cache.global.CollectionLogicDeleteCache;
import com.anwen.mongo.conditions.BuildCondition;
import com.anwen.mongo.conditions.aggregate.AggregateChainWrapper;
import com.anwen.mongo.conditions.interfaces.condition.CompareCondition;
import com.anwen.mongo.conditions.query.QueryChainWrapper;
import com.anwen.mongo.conditions.update.UpdateChainWrapper;
import com.anwen.mongo.constant.SqlOperationConstant;
import com.anwen.mongo.enums.SpecialConditionEnum;
import com.anwen.mongo.execute.Execute;
import com.anwen.mongo.execute.ExecutorFactory;
import com.anwen.mongo.logging.Log;
import com.anwen.mongo.logging.LogFactory;
import com.anwen.mongo.logic.LogicDeleteHandler;
import com.anwen.mongo.manager.MongoPlusClient;
import com.anwen.mongo.mapping.MongoConverter;
import com.anwen.mongo.mapping.TypeReference;
import com.anwen.mongo.model.*;
import com.anwen.mongo.toolkit.Filters;
import com.anwen.mongo.toolkit.*;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertManyResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * baseMapper默认实现
 *
 * @author JiaChaoYang
 * @project mongo-plus
 * @date 2024-02-05 11:47
 **/
public class DefaultBaseMapperImpl implements BaseMapper {

    private final Log log = LogFactory.getLog(DefaultBaseMapperImpl.class);

    private final MongoPlusClient mongoPlusClient;

    private final LambdaOperate lambdaOperate = new LambdaOperate();

    private final ExecutorFactory factory = new ExecutorFactory();

    private final MongoConverter mongoConverter;

    public DefaultBaseMapperImpl(MongoPlusClient mongoPlusClient,MongoConverter mongoConverter) {
        this.mongoPlusClient = mongoPlusClient;
        this.mongoConverter = mongoConverter;
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
    public <T> boolean save(T entity){
        try {
            Document document = new Document();
            mongoConverter.writeBySave(entity, document);
            InsertManyResult insertManyResult = factory.getExecute().executeSave(Collections.singletonList(document), mongoPlusClient.getCollection(ClassTypeUtil.getClass(entity)));
            mongoConverter.reSetIdValue(entity, document);
            return insertManyResult.wasAcknowledged();
        } catch (Exception e) {
            log.error("save fail , error info : {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public <T> Boolean saveBatch(Collection<T> entityList) {
        try {
            List<Document> documentList = new ArrayList<>(entityList.size());
            mongoConverter.writeBySaveBatch(entityList, documentList);
            MongoCollection<Document> collection = mongoPlusClient.getCollection(entityList.iterator().next().getClass());
            InsertManyResult insertManyResult = factory.getExecute().executeSave(documentList, collection);
            mongoConverter.batchReSetIdValue(entityList, documentList);
            return insertManyResult.getInsertedIds().size() == entityList.size();
        } catch (Exception e) {
            log.error("saveBatch fail , error info : {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Long update(Bson queryBasic, Bson updateBasic, Class<?> clazz) {
        return factory.getExecute().executeUpdate(
                queryBasic,
                updateBasic,
                mongoPlusClient.getCollection(clazz)
        ).getModifiedCount();
    }

    @Override
    public Integer bulkWrite(List<WriteModel<Document>> writeModelList, Class<?> clazz) {
        BulkWriteResult bulkWriteResult = factory.getExecute().executeBulkWrite(writeModelList,mongoPlusClient.getCollection(clazz));
        return bulkWriteResult.getModifiedCount() + bulkWriteResult.getInsertedCount();
    }

    @Override
    public <T> Boolean update(T entity,QueryChainWrapper<T,?> queryChainWrapper){
        MutablePair<BasicDBObject, BasicDBObject> updatePair = ConditionUtil.getUpdateCondition(queryChainWrapper.getCompareList(), entity,mongoConverter);
        return factory.getExecute().executeUpdate(updatePair.getLeft(),updatePair.getRight(),mongoPlusClient.getCollection(ClassTypeUtil.getClass(entity))).getModifiedCount() > 0;
    }

    /**
     * 查询所有
     * @param clazz 操作的class
     * @param rClazz 返回的class
     * @return {@link List <T>}
     * @author anwen
     * @date 2024/5/4 下午1:24
     */
    @Override
    public <T,R> List<R> list(Class<T> clazz,Class<R> rClazz) {
        FindIterable<Document> findIterable = factory.getExecute().executeQuery(null, null, null, mongoPlusClient.getCollection(clazz), Document.class);
        return mongoConverter.read(findIterable, rClazz);
    }

    @Override
    public <T, R> List<R> list(Class<T> clazz, TypeReference<R> typeReference) {
        FindIterable<Document> findIterable = factory.getExecute().executeQuery(null, null, null, mongoPlusClient.getCollection(clazz), Document.class);
        return mongoConverter.read(findIterable, typeReference);
    }

    @Override
    public <T,R> List<R> list(QueryChainWrapper<T,?> queryChainWrapper, Class<T> clazz,Class<R> rClazz) {
        BaseLambdaQueryResult baseLambdaQuery = lambdaOperate.baseLambdaQuery(queryChainWrapper.getCompareList(),queryChainWrapper.getOrderList(),queryChainWrapper.getProjectionList(),queryChainWrapper.getBasicDBObjectList());
        FindIterable<Document> documentFindIterable = factory.getExecute().executeQuery(baseLambdaQuery.getCondition(), baseLambdaQuery.getProjection(), baseLambdaQuery.getSort(), mongoPlusClient.getCollection(clazz), Document.class);
        return mongoConverter.read(documentFindIterable, rClazz);
    }

    @Override
    public <T,R> List<R> aggregateList(AggregateChainWrapper<T, ?> queryChainWrapper, Class<T> clazz,Class<R> rClazz){
        List<BaseAggregate> aggregateList = queryChainWrapper.getBaseAggregateList();
        List<AggregateBasicDBObject> basicDBObjectList = queryChainWrapper.getBasicDBObjectList();
        BasicDBObject optionsBasicDBObject = queryChainWrapper.getOptionsBasicDBObject();
        List<AggregateBasicDBObject> aggregateConditionList = new ArrayList<AggregateBasicDBObject>() {{
            aggregateList.forEach(aggregate -> add(new AggregateBasicDBObject("$" + aggregate.getType(), aggregate.getPipelineStrategy().buildAggregate(),aggregate.getOrder())));
            addAll(basicDBObjectList);
        }};
        aggregateConditionList.sort(Comparator.comparingInt(AggregateBasicDBObject::getOrder));
        AggregateIterable<Document> aggregateIterable = factory.getExecute().executeAggregate(aggregateConditionList, mongoPlusClient.getCollection(clazz),Document.class);
        AggregateUtil.aggregateOptions(aggregateIterable,optionsBasicDBObject);
        return mongoConverter.read(aggregateIterable,rClazz);
    }

    @Override
    public <T,R> R one(QueryChainWrapper<T,?> queryChainWrapper,Class<T> clazz,Class<R> rClazz) {
            BaseLambdaQueryResult baseLambdaQuery = lambdaOperate.baseLambdaQuery(queryChainWrapper.getCompareList(),queryChainWrapper.getOrderList(),queryChainWrapper.getProjectionList(),queryChainWrapper.getBasicDBObjectList());
        return mongoConverter.readDocument(factory.getExecute().executeQuery(baseLambdaQuery.getCondition(),baseLambdaQuery.getProjection(),baseLambdaQuery.getSort(),mongoPlusClient.getCollection(clazz),Document.class).limit(1),rClazz);
    }

    @Override
    public <T,R> PageResult<R> page(QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize,Class<T> clazz,Class<R> rClazz) {
        BaseLambdaQueryResult baseLambdaQuery = lambdaOperate.baseLambdaQuery(queryChainWrapper.getCompareList(),queryChainWrapper.getOrderList(),queryChainWrapper.getProjectionList(),queryChainWrapper.getBasicDBObjectList());
        MongoCollection<Document> collection = mongoPlusClient.getCollection(clazz);
        long count;
        if (!(CollectionLogicDeleteCache.open && Objects.nonNull(LogicDeleteHandler.mapper().get(clazz))) && CollUtil.isEmpty(queryChainWrapper.getCompareList())){
            count = factory.getExecute().estimatedDocumentCount(collection);
        }else {
            count = count(queryChainWrapper,clazz);
        }
        FindIterable<Document> iterable = factory.getExecute().executeQuery(baseLambdaQuery.getCondition(), baseLambdaQuery.getProjection(), baseLambdaQuery.getSort(), collection,Document.class);
        return lambdaOperate.getLambdaQueryResultPage(iterable,count,new PageParam(pageNum,pageSize),rClazz,mongoConverter);
    }

    @Override
    public <T,R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<T> clazz,Class<R> rClazz) {
        BaseLambdaQueryResult baseLambdaQuery = lambdaOperate.baseLambdaQuery(queryChainWrapper.getCompareList(),queryChainWrapper.getOrderList(),queryChainWrapper.getProjectionList(),queryChainWrapper.getBasicDBObjectList());
        FindIterable<Document> iterable = factory.getExecute().executeQuery(baseLambdaQuery.getCondition(), baseLambdaQuery.getProjection(), baseLambdaQuery.getSort(), mongoPlusClient.getCollection(clazz),Document.class);
        return mongoConverter.read(iterable.skip((pageNum - 1) * pageSize).limit(pageSize), rClazz);
    }

    @Override
    public <T,R> PageResult<R> page(QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz,Class<R> rClazz) {
        BaseLambdaQueryResult baseLambdaQuery = lambdaOperate.baseLambdaQuery(queryChainWrapper.getCompareList(),queryChainWrapper.getOrderList(),queryChainWrapper.getProjectionList(),queryChainWrapper.getBasicDBObjectList());
        MongoCollection<Document> collection = mongoPlusClient.getCollection(clazz);
        long count;
        if (!(CollectionLogicDeleteCache.open && Objects.nonNull(LogicDeleteHandler.mapper().get(clazz))) && CollUtil.isEmpty(queryChainWrapper.getCompareList())){
            count = factory.getExecute().estimatedDocumentCount(collection);
        }else {
            count = recentPageCount(queryChainWrapper.getCompareList(),clazz, pageNum,  pageSize, recentPageNum);
        }
        FindIterable<Document> iterable = factory.getExecute().executeQuery(baseLambdaQuery.getCondition(), baseLambdaQuery.getProjection(), baseLambdaQuery.getSort(), collection,Document.class);
        return lambdaOperate.getLambdaQueryResultPage(iterable, count,new PageParam(pageNum,pageSize),rClazz,mongoConverter);
    }

    @Override
    public <T,R> R getById(Serializable id,Class<T> clazz,Class<R> rClazz) {
        BasicDBObject queryBasic = new BasicDBObject(SqlOperationConstant._ID, new BasicDBObject(SpecialConditionEnum.EQ.getCondition(), ObjectId.isValid(String.valueOf(id)) ? new ObjectId(String.valueOf(id)) : id));
        return mongoConverter.read(factory.getExecute().executeQuery(queryBasic,null,null,mongoPlusClient.getCollection(clazz),Document.class).first(),rClazz);
    }

    @Override
    public boolean isExist(Serializable id,Class<?> clazz){
        BasicDBObject queryBasic = new BasicDBObject(SqlOperationConstant._ID, new BasicDBObject(SpecialConditionEnum.EQ.getCondition(), ObjectId.isValid(String.valueOf(id)) ? new ObjectId(String.valueOf(id)) : id));
        return factory.getExecute().executeCount(queryBasic,null, mongoPlusClient.getCollection(clazz)) >= 1;
    }

    @Override
    public boolean isExist(QueryChainWrapper<?,?> queryChainWrapper,Class<?> clazz){
        BasicDBObject basicDBObject = BuildCondition.buildQueryCondition(queryChainWrapper.getCompareList());
        return factory.getExecute().executeCount(basicDBObject,null,mongoPlusClient.getCollection(clazz)) >= 1;
    }

    @Override
    public <T,R> List<R> getByIds(Collection<? extends Serializable> ids,Class<T> clazz,Class<R> rClazz) {
        BasicDBObject basicDBObject = checkIdType(ids);
        FindIterable<Document> iterable = factory.getExecute().executeQuery(basicDBObject,null,null, mongoPlusClient.getCollection(clazz),Document.class);
        return mongoConverter.read(iterable, rClazz);
    }

    @Override
    public Boolean update(UpdateChainWrapper<?, ?> updateChainWrapper,Class<?> clazz) {
        List<CompareCondition> compareConditionList = new ArrayList<>();
        compareConditionList.addAll(updateChainWrapper.getCompareList());
        compareConditionList.addAll(updateChainWrapper.getUpdateCompareList());
        BasicDBObject queryBasic = BuildCondition.buildQueryCondition(compareConditionList);
        List<CompareCondition> pushConditionList = compareConditionList.stream().filter(compareCondition -> Objects.equals(compareCondition.getCondition(), SpecialConditionEnum.PUSH.getSubCondition())).collect(Collectors.toList());
        List<CompareCondition> setConditionList = compareConditionList.stream().filter(compareCondition -> Objects.equals(compareCondition.getCondition(), SpecialConditionEnum.SET.getSubCondition())).collect(Collectors.toList());
        BasicDBObject basicDBObject = new BasicDBObject() {{
            if (CollUtil.isNotEmpty(setConditionList)){
                append(SpecialConditionEnum.SET.getCondition(), BuildCondition.buildUpdateValue(setConditionList));
            }
            if (CollUtil.isNotEmpty(pushConditionList)){
                append(SpecialConditionEnum.PUSH.getCondition(), BuildCondition.buildPushUpdateValue(pushConditionList));
            }
        }};
        BasicDBObject targetBasicDBObject = new BasicDBObject();
        mongoConverter.write(basicDBObject,targetBasicDBObject);
        return factory.getExecute().executeUpdate(queryBasic,targetBasicDBObject,mongoPlusClient.getCollection(clazz)).getModifiedCount() >= 1;
    }

    @Override
    public Boolean remove(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz) {
        return remove(BuildCondition.buildQueryCondition(updateChainWrapper.getCompareList()),clazz) >= 1;
    }

    @Override
    public Long remove(Bson filter, Class<?> clazz) {
        return factory.getExecute().executeRemove(filter,mongoPlusClient.getCollection(clazz)).getDeletedCount();
    }

    @Override
    public long count(QueryChainWrapper<?, ?> queryChainWrapper,Class<?> clazz){
        Execute execute = factory.getExecute();
        MongoCollection<Document> collection = mongoPlusClient.getCollection(clazz);
        long line;
        if (!(CollectionLogicDeleteCache.open && Objects.nonNull(LogicDeleteHandler.mapper().get(clazz))) && CollUtil.isEmpty(queryChainWrapper.getCompareList())){
            line = execute.estimatedDocumentCount(collection);
        } else {
            line = execute.executeCount(BuildCondition.buildQueryCondition(queryChainWrapper.getCompareList()),null,collection);
        }
        return line;
    }

    /**
     * 分页查询 查询总条数
     * @param compareConditionList 条件集合
     * @param clazz result class
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @return long
     */
    @Override
    public long recentPageCount(List<CompareCondition> compareConditionList,Class<?> clazz, Integer pageNum, Integer pageSize, Integer recentPageNum){
        if (recentPageNum == null || !(recentPageNum <= 50 && recentPageNum >= 5)) {
            // 返回-1 表示不查询总条数
            return -1L;
        }
        //分页查询  不查询实际总条数  需要单独查询  是否有数据
        //如果recentPageNum = 10  第1-6页  总页数=10  从第7页开始 需要往后 + 4 页
        int limitParam = (pageNum < (recentPageNum / 2 + 1 + recentPageNum % 2) ? recentPageNum : (pageNum + (recentPageNum / 2 + recentPageNum % 2 - 1))) * pageSize;
        CountOptions countOptions = new CountOptions();
        countOptions.skip(limitParam).limit(1);
        long isExists = factory.getExecute().executeCount(BuildCondition.buildQueryCondition(compareConditionList),countOptions, mongoPlusClient.getCollection(clazz));
        //如果查询结果为空 则查询总条数，如果不为空则 limitParam为总条数
        if (isExists == 0) {
            // 查询真实总条数
            CountOptions countOptionsReal = new CountOptions();
            countOptionsReal.limit(limitParam);
            return factory.getExecute().executeCount(BuildCondition.buildQueryCondition(compareConditionList),countOptions, mongoPlusClient.getCollection(clazz));
        }
        return limitParam;
    }

    @Override
    public long count(Class<?> clazz){
        MongoCollection<Document> mongoCollection = mongoPlusClient.getCollection(clazz);
        Execute execute = factory.getExecute();
        long line;
        if (!(CollectionLogicDeleteCache.open && Objects.nonNull(LogicDeleteHandler.mapper().get(clazz)))){
            line = execute.estimatedDocumentCount(mongoCollection);
        } else {
            line = execute.executeCount(null,null,mongoCollection);
        }
        return line;
    }

    @Override
    public <T,R> List<R> queryCommand(String command,Class<T> clazz,Class<R> rClazz){
        FindIterable<Document> iterable = factory.getExecute().executeQuery(BasicDBObject.parse(command),null,null, mongoPlusClient.getCollection(clazz),Document.class);
        return mongoConverter.read(iterable,rClazz);
    }

    @Override
    public <T,R> List<R> getByColumn(String column,Object value,Class<T> clazz,Class<R> rClazz){
        Bson filter = Filters.eq(column, ObjectId.isValid(String.valueOf(value)) ? new ObjectId(String.valueOf(value)) : value);
        return mongoConverter.read(factory.getExecute().executeQuery(filter,null,null,mongoPlusClient.getCollection(clazz),Document.class),rClazz);
    }

    @Override
    public String createIndex(Bson bson,Class<?> clazz){
        return factory.getExecute().doCreateIndex(bson,mongoPlusClient.getCollection(clazz));
    }

    @Override
    public String createIndex(Bson bson, IndexOptions indexOptions, Class<?> clazz){
        return factory.getExecute().doCreateIndex(bson,indexOptions,mongoPlusClient.getCollection(clazz));
    }

    @Override
    public List<String> createIndexes(List<IndexModel> indexes,Class<?> clazz){
        return factory.getExecute().doCreateIndexes(indexes,mongoPlusClient.getCollection(clazz));
    }

    @Override
    public List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions,Class<?> clazz){
        return factory.getExecute().doCreateIndexes(indexes,createIndexOptions,mongoPlusClient.getCollection(clazz));
    }

    @Override
    public List<Document> listIndexes(Class<?> clazz){
        return factory.getExecute().doListIndexes(mongoPlusClient.getCollection(clazz));
    }

    @Override
    public void dropIndex(String indexName,Class<?> clazz){
        factory.getExecute().doDropIndex(indexName,mongoPlusClient.getCollection(clazz));
    }

    @Override
    public void dropIndex(String indexName,DropIndexOptions dropIndexOptions,Class<?> clazz){
        factory.getExecute().doDropIndex(indexName,dropIndexOptions,mongoPlusClient.getCollection(clazz));
    }

    @Override
    public void dropIndex(Bson keys,Class<?> clazz){
        factory.getExecute().doDropIndex(keys,mongoPlusClient.getCollection(clazz));
    }

    @Override
    public void dropIndex(Bson keys,DropIndexOptions dropIndexOptions,Class<?> clazz){
        factory.getExecute().doDropIndex(keys,dropIndexOptions,mongoPlusClient.getCollection(clazz));
    }

    @Override
    public void dropIndexes(Class<?> clazz){
        factory.getExecute().doDropIndexes(mongoPlusClient.getCollection(clazz));
    }

    @Override
    public void dropIndexes(DropIndexOptions dropIndexOptions,Class<?> clazz){
        factory.getExecute().doDropIndexes(dropIndexOptions,mongoPlusClient.getCollection(clazz));
    }

    protected BasicDBObject checkIdType(Collection<? extends Serializable> ids) {
        List<Serializable> convertedIds = ids.stream()
                .map(id -> ObjectId.isValid(String.valueOf(id)) ? new ObjectId(String.valueOf(id)) : id)
                .collect(Collectors.toList());
        return new BasicDBObject(SqlOperationConstant._ID, new BasicDBObject(SpecialConditionEnum.IN.getCondition(), convertedIds));
    }
}
