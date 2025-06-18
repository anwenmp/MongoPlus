package com.mongoplus.mapper;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.annotation.ID;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.PageParam;
import com.mongoplus.model.PageResult;
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * mapper层MongoMapper实现类，继承此类则不会自动为接口创建实现类
 */
@SuppressWarnings("unchecked")
public class MongoMapperImpl<T> implements MongoMapper<T> {

    protected BaseMapper baseMapper;

    @Override
    public BaseMapper getBaseMapper() {
        return baseMapper;
    }

    public void setBaseMapper(BaseMapper baseMapper) {
        this.baseMapper = baseMapper;
    }

    protected Class<T> clazz;

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = (Class<T>) clazz;
    }
    Class<T> getGenericityClass(Class<?> clazz) {
        Type superClassType = clazz.getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) superClassType;
        Type genType = pt.getActualTypeArguments()[0];

        if (genType instanceof Class) {
            return (Class<T>) genType;
        } else if (genType instanceof TypeVariable) {
            // 处理泛型类型是 TypeVariable 的情况
            return (Class<T>) Object.class;
        } else {
            throw new IllegalArgumentException("Unsupported generic type: " + genType);
        }
    }

    @Override
    public Class<T> getGenericityClass() {
        if (this.clazz != null) {
            return clazz;
        }
        return getGenericityClass(getClass());
    }

    @Override
    public MongoCollection<Document> getCollection() {
        String database = DataSourceNameCache.getDatabase();
        String annotationDatabase = AnnotationOperate.getDatabase(clazz);
        if (StringUtils.isNotBlank(annotationDatabase)) {
            database = annotationDatabase;
        }
        return baseMapper.getMongoPlusClient().getCollection(database, clazz);
    }

    @Override
    public Boolean save(T entity, InsertManyOptions options) {
        return baseMapper.save(entity,options);
    }

    @Override
    public Boolean save(T entity, InsertOneOptions options) {
        return baseMapper.save(entity,options);
    }

    @Override
    public Boolean saveBatch(Collection<T> entityList, InsertManyOptions options) {
        return baseMapper.saveBatch(entityList,options);
    }

    @Override
    public Boolean saveOrUpdate(T entity) {
        Object idByEntity = ClassTypeUtil.getIdByEntity(entity, true);
        if (idByEntity == null) {
            return save(entity);
        }
        return updateById(entity);
    }

    @Override
    public boolean saveOrUpdate(T entity, boolean isQueryDatabase) {
        if (!isQueryDatabase) {
            return saveOrUpdate(entity);
        }
        Object idByEntity = ClassTypeUtil.getIdByEntity(entity, true);
        long count = count(new QueryWrapper<T>().eq(SqlOperationConstant._ID, idByEntity));
        if (count > 0) {
            return updateById(entity);
        }
        return save(entity);
    }

    @Override
    public Boolean saveOrUpdateWrapper(T entity, QueryChainWrapper<T, ?> queryChainWrapper) {
        long count = count(queryChainWrapper);
        if (count > 0) {
            return baseMapper.update(entity, queryChainWrapper);
        }
        return save(entity);
    }

    @Override
    public Boolean saveOrUpdateBatch(Collection<T> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            throw new MongoPlusException("entityList is null");
        }
        List<WriteModel<Document>> writeModelList = new ArrayList<>();
        entityList.forEach(entity -> {
            Object idByEntity = ClassTypeUtil.getIdByEntity(entity, true);
            if (idByEntity == null) {
                writeModelList.add(new InsertOneModel<>(baseMapper.getMongoConverter().writeBySave(entity)));
            } else {
                MutablePair<BasicDBObject, BasicDBObject> basicDBObjectPair = ConditionUtil.getUpdate(
                        entity,
                        baseMapper.getMongoConverter()
                );
                writeModelList.add(new UpdateManyModel<>(basicDBObjectPair.getLeft(), basicDBObjectPair.getRight()));
            }
        });
        return baseMapper.bulkWrite(writeModelList, entityList
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new MongoPlusException("entityList is null")).getClass()
        ) == entityList.size();
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<T> entityList, boolean isQueryDatabase) {
        if (CollUtil.isEmpty(entityList)) {
            throw new MongoPlusException("entityList is null");
        }
        if (!isQueryDatabase) {
            return saveOrUpdateBatch(entityList);
        }
        List<Object> idList = entityList.stream()
                .map(entity -> ClassTypeUtil.getIdByEntity(entity, true))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<WriteModel<Document>> writeModelList = entityList.stream()
                .filter(entity -> ClassTypeUtil.getIdByEntity(entity, true) == null)
                .map(entity -> new InsertOneModel<>(
                        baseMapper.getMongoConverter()
                                .writeBySave(entity)))
                .collect(Collectors.toList());
        Set<Object> existingIdSet;
        if (CollUtil.isNotEmpty(idList)) {
            existingIdSet = list(new QueryWrapper<T>().in(SqlOperationConstant._ID, idList))
                    .stream()
                    .map(entity -> ClassTypeUtil.getIdByEntity(entity, true))
                    .collect(Collectors.toSet());
        } else {
            existingIdSet = new HashSet<>();
        }
        writeModelList.addAll(
                entityList.stream()
                        .filter(entity -> {
                            Object id = ClassTypeUtil.getIdByEntity(entity, true);
                            return id != null && existingIdSet.contains(id);
                        })
                        .map(entity -> {
                            MutablePair<BasicDBObject, BasicDBObject> basicDBObjectPair = ConditionUtil
                                    .getUpdate(entity, baseMapper.getMongoConverter());
                            return new UpdateManyModel<Document>(
                                    basicDBObjectPair.getLeft(),
                                    basicDBObjectPair.getRight()
                            );
                        })
                        .collect(Collectors.toList())
        );
        writeModelList.addAll(
                entityList.stream()
                        .filter(entity -> {
                            Object id = ClassTypeUtil.getIdByEntity(entity, true);
                            return id != null && !existingIdSet.contains(id);
                        })
                        .map(entity -> new InsertOneModel<>(baseMapper.getMongoConverter()
                                .writeBySave(entity)))
                        .collect(Collectors.toList())
        );
        return baseMapper.bulkWrite(writeModelList,
                entityList
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new MongoPlusException("entityList is null")).getClass()
        ) == entityList.size();
    }

    @Override
    public Boolean saveOrUpdateBatchWrapper(Collection<T> entityList, QueryChainWrapper<T, ?> queryChainWrapper) {
        Class<?> clazz = entityList.stream().findFirst().orElseThrow(() ->
                new MongoPlusException("entityList is null")).getClass();
        List<WriteModel<Document>> writeModelList = new ArrayList<>();
        long count = baseMapper.count(queryChainWrapper, clazz);
        entityList.forEach(entity -> {
            if (count > 0) {
                MutablePair<BasicDBObject, BasicDBObject> updatePair = ConditionUtil.
                        getUpdateCondition(
                                queryChainWrapper.getCompareList(),
                                entity,
                                baseMapper.getMongoConverter()
                        );
                writeModelList.add(new UpdateManyModel<>(updatePair.getLeft(), updatePair.getRight()));
            } else {
                writeModelList.add(new InsertOneModel<>(baseMapper.getMongoConverter().writeBySave(entity)));
            }
        });
        return baseMapper.bulkWrite(
                writeModelList,
                entityList
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new MongoPlusException("entityList is null")).getClass()
        ) == entityList.size();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Boolean updateById(T entity, UpdateOptions options) {
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq(SqlOperationConstant._ID, TypeInformation
                .of(entity)
                .getAnnotationField(ID.class, "@ID is not found")
                .getValue()
        );
        return baseMapper.updateOne(entity, wrapper,options);
    }

    @Override
    public Boolean updateBatchByIds(Collection<T> entityList) {
        List<WriteModel<Document>> writeModelList = new ArrayList<>();
        entityList.forEach(entity -> {
            MutablePair<BasicDBObject, BasicDBObject> basicDBObjectPair = ConditionUtil
                    .getUpdate(entity, baseMapper.getMongoConverter());
            writeModelList.add(new UpdateManyModel<>(basicDBObjectPair.getLeft(), basicDBObjectPair.getRight()));
        });
        return baseMapper.bulkWrite(
                writeModelList,
                entityList
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new MongoPlusException("entityList is null")).getClass()
        ) == entityList.size();
    }

    @Override
    public Boolean updateByColumn(T entity, SFunction<T, Object> column, UpdateOptions options) {
        return updateByColumn(entity, column.getFieldNameLine(),options);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Boolean updateByColumn(T entity, String column,UpdateOptions options) {
        Object filterValue = ClassTypeUtil.getClassFieldValue(entity, column);
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq(column, filterValue);
        return update(entity, wrapper,options);
    }

    @Override
    public Boolean remove(UpdateChainWrapper<T, ?> updateChainWrapper, DeleteOptions options) {
        return baseMapper.remove(updateChainWrapper, clazz,options);
    }

    @Override
    public Boolean update(UpdateChainWrapper<T, ?> updateChainWrapper,UpdateOptions options) {
        return baseMapper.update(updateChainWrapper, clazz,options);
    }

    @Override
    public Boolean update(T entity, QueryChainWrapper<T, ?> queryChainWrapper,UpdateOptions options) {
        return baseMapper.update(entity, queryChainWrapper,options);
    }

    @Override
    public Boolean removeById(Serializable id, DeleteOptions options) {
        Bson filterId = new Document(SqlOperationConstant._ID, ObjectIdUtil.getObjectIdValue(id));
        return baseMapper.removeOne(filterId, clazz,options) >= 1;
    }

    @Override
    public Boolean removeByColumn(SFunction<T, Object> column, Object value,DeleteOptions options) {
        return removeByColumn(column.getFieldNameLine(), value,options);
    }

    @Override
    public Boolean removeByColumn(String column, Object value,DeleteOptions options) {
        Bson filter = new Document(column, ObjectIdUtil.getObjectIdValue(value));
        return baseMapper.remove(filter, clazz,options) >= 1;
    }

    @Override
    public Boolean removeBatchByIds(Collection<? extends Serializable> idList,DeleteOptions options) {
        return baseMapper.remove(BsonUtil.getIdsCondition(idList), clazz,options) >= 1;
    }

    @Override
    public List<T> list(Aggregate<?> aggregate) {
        return list(aggregate, clazz);
    }

    @Override
    public <R> List<R> list(Aggregate<?> aggregate, Class<R> rClass) {
        return baseMapper.aggregateList(aggregate, clazz, rClass);
    }

    @Override
    public <R> List<R> list(Aggregate<?> aggregate, TypeReference<R> typeReference) {
        return baseMapper.aggregateList(aggregate, clazz, typeReference);
    }

    @Override
    public T one(QueryChainWrapper<T, ?> queryChainWrapper) {
        return one(queryChainWrapper, clazz);
    }

    @Override
    public <R> R one(QueryChainWrapper<T, ?> queryChainWrapper, Class<R> rClazz) {
        return baseMapper.one(queryChainWrapper, clazz, rClazz);
    }

    @Override
    public <R> R one(QueryChainWrapper<T, ?> queryChainWrapper, TypeReference<R> typeReference) {
        return baseMapper.one(queryChainWrapper, clazz, typeReference);
    }

    @Override
    public T one(Aggregate<?> aggregate) {
        return one(aggregate,clazz);
    }

    @Override
    public <R> R one(Aggregate<?> aggregate, Class<R> rClazz) {
        return (R) baseMapper.aggregateOne(aggregate,clazz);
    }

    @Override
    public <R> R one(Aggregate<?> aggregate, TypeReference<R> typeReference) {
        return baseMapper.aggregateOne(aggregate,clazz,typeReference);
    }

    @Override
    public List<T> list() {
        return list(clazz);
    }

    @Override
    public <R> List<R> list(Class<R> rClazz) {
        return baseMapper.list(clazz, rClazz);
    }

    @Override
    public <R> List<R> list(TypeReference<R> typeReference) {
        return baseMapper.list(clazz, typeReference);
    }

    @Override
    public List<T> list(QueryChainWrapper<T, ?> queryChainWrapper) {
        return list(queryChainWrapper, clazz);
    }

    @Override
    public <R> List<R> list(QueryChainWrapper<T, ?> queryChainWrapper, Class<R> rClazz) {
        return baseMapper.list(queryChainWrapper, clazz, rClazz);
    }

    @Override
    public <R> List<R> list(QueryChainWrapper<T, ?> queryChainWrapper, TypeReference<R> typeReference) {
        return baseMapper.list(queryChainWrapper, clazz, typeReference);
    }

    @Override
    public long count() {
        return baseMapper.count(clazz);
    }

    @Override
    public long count(QueryChainWrapper<T, ?> queryChainWrapper) {
        return baseMapper.count(queryChainWrapper, clazz);
    }

    @Override
    public PageResult<T> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize) {
        return page(queryChainWrapper, pageNum, pageSize, clazz);
    }

    @Override
    public PageResult<T> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam) {
        return page(queryChainWrapper, pageParam.getPageNum(), pageParam.getPageSize());
    }

    @Override
    public PageResult<T> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize,
                              Integer recentPageNum) {
        return page(queryChainWrapper, pageNum, pageSize, recentPageNum, clazz);
    }

    @Override
    public PageResult<T> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam, Integer recentPageNum) {
        return page(queryChainWrapper, pageParam.getPageNum(), pageParam.getPageSize(), recentPageNum);
    }

    @Override
    public <R> PageResult<R> page(Integer pageNum, Integer pageSize, Integer recentPageNum, Class<R> rClazz) {
        return baseMapper.page(new QueryWrapper<>(), pageNum, pageSize, recentPageNum, clazz, rClazz);
    }

    @Override
    public <R> PageResult<R> page(Integer pageNum, Integer pageSize, Integer recentPageNum,
                                  TypeReference<R> typeReference) {
        return baseMapper.page(new QueryWrapper<>(), pageNum, pageSize, recentPageNum, clazz, typeReference);
    }

    @Override
    public <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz) {
        return baseMapper.page(queryChainWrapper, pageNum, pageSize, clazz, rClazz);
    }

    @Override
    public <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize,
                                  TypeReference<R> typeReference) {
        return baseMapper.page(queryChainWrapper, pageNum, pageSize, clazz, typeReference);
    }

    @Override
    public <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam, Class<R> rClazz) {
        return baseMapper.page(queryChainWrapper, pageParam.getPageNum(), pageParam.getPageSize(), clazz, rClazz);
    }

    @Override
    public <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam,
                                  TypeReference<R> typeReference) {
        return baseMapper.page(queryChainWrapper, pageParam.getPageNum(), pageParam.getPageSize(), clazz, typeReference);
    }

    @Override
    public <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize,
                                  Integer recentPageNum, Class<R> rClazz) {
        return baseMapper.page(queryChainWrapper, pageNum, pageSize, recentPageNum, clazz, rClazz);
    }

    @Override
    public <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize,
                                  Integer recentPageNum, TypeReference<R> typeReference) {
        return baseMapper.page(queryChainWrapper, pageNum, pageSize, recentPageNum, clazz, typeReference);
    }

    @Override
    public <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam,
                                  Integer recentPageNum, Class<R> rClazz) {
        return baseMapper.page(queryChainWrapper, pageParam.getPageNum(), pageParam.getPageSize(),
                recentPageNum, clazz, rClazz);
    }

    @Override
    public <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam,
                                  Integer recentPageNum, TypeReference<R> typeReference) {
        return baseMapper.page(queryChainWrapper, pageParam.getPageNum(), pageParam.getPageSize(),
                recentPageNum, clazz, typeReference);
    }

    @Override
    public List<T> pageList(PageParam pageParam) {
        return pageList(pageParam.getPageNum(), pageParam.getPageSize());
    }

    @Override
    public <R> List<R> pageList(PageParam pageParam, Class<R> rClazz) {
        return pageList(pageParam.getPageNum(), pageParam.getPageSize(), rClazz);
    }

    @Override
    public <R> List<R> pageList(PageParam pageParam, TypeReference<R> typeReference) {
        return pageList(pageParam.getPageNum(), pageParam.getPageSize(), typeReference);
    }

    @Override
    public List<T> pageList(Integer pageNum, Integer pageSize) {
        return pageList(new QueryWrapper<>(), pageNum, pageSize, clazz);
    }

    @Override
    public <R> List<R> pageList(Integer pageNum, Integer pageSize, Class<R> rClazz) {
        return baseMapper.pageList(new QueryWrapper<>(), pageNum, pageSize, clazz, rClazz);
    }

    @Override
    public <R> List<R> pageList(Integer pageNum, Integer pageSize, TypeReference<R> typeReference) {
        return baseMapper.pageList(new QueryWrapper<>(), pageNum, pageSize, clazz, typeReference);
    }

    @Override
    public List<T> pageList(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize) {
        return pageList(queryChainWrapper, pageNum, pageSize, clazz);
    }

    @Override
    public <R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum,
                                Integer pageSize, Class<R> rClazz) {
        return baseMapper.pageList(queryChainWrapper, pageNum, pageSize, clazz, rClazz);
    }

    @Override
    public <R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum,
                                Integer pageSize, TypeReference<R> typeReference) {
        return baseMapper.pageList(queryChainWrapper, pageNum, pageSize, clazz, typeReference);
    }

    @Override
    public List<T> pageList(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam) {
        return pageList(queryChainWrapper, pageParam.getPageNum(), pageParam.getPageSize(), clazz);
    }

    @Override
    public <R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper,
                                PageParam pageParam, Class<R> rClazz) {
        return baseMapper.pageList(queryChainWrapper, pageParam.getPageNum(), pageParam.getPageSize(), clazz, rClazz);
    }

    @Override
    public <R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper,
                                PageParam pageParam, TypeReference<R> typeReference) {
        return baseMapper.pageList(queryChainWrapper, pageParam.getPageNum(),
                pageParam.getPageSize(), clazz, typeReference);
    }

    @Override
    public PageResult<T> page(PageParam pageParam) {
        return page(pageParam.getPageNum(), pageParam.getPageSize());
    }

    @Override
    public <R> PageResult<R> page(PageParam pageParam, Class<R> rClazz) {
        return page(pageParam.getPageNum(), pageParam.getPageSize(), rClazz);
    }

    @Override
    public <R> PageResult<R> page(PageParam pageParam, TypeReference<R> typeReference) {
        return page(pageParam.getPageNum(), pageParam.getPageSize(), typeReference);
    }

    @Override
    public PageResult<T> page(PageParam pageParam, Integer recentPageNum) {
        return page(pageParam.getPageNum(), pageParam.getPageSize(), recentPageNum);
    }

    @Override
    public <R> PageResult<R> page(PageParam pageParam, Integer recentPageNum, Class<R> rClazz) {
        return page(pageParam.getPageNum(), pageParam.getPageSize(), recentPageNum, rClazz);
    }

    @Override
    public <R> PageResult<R> page(PageParam pageParam, Integer recentPageNum, TypeReference<R> typeReference) {
        return page(pageParam.getPageNum(), pageParam.getPageSize(), recentPageNum, typeReference);
    }

    @Override
    public PageResult<T> page(Integer pageNum, Integer pageSize) {
        return page(new QueryWrapper<>(), pageNum, pageSize);
    }

    @Override
    public <R> PageResult<R> page(Integer pageNum, Integer pageSize, Class<R> rClazz) {
        return baseMapper.page(new QueryWrapper<>(), pageNum, pageSize, clazz, rClazz);
    }

    @Override
    public <R> PageResult<R> page(Integer pageNum, Integer pageSize, TypeReference<R> typeReference) {
        return baseMapper.page(new QueryWrapper<>(), pageNum, pageSize, clazz, typeReference);
    }

    @Override
    public PageResult<T> page(Integer pageNum, Integer pageSize, Integer recentPageNum) {
        return page(new QueryWrapper<>(), pageNum, pageSize, recentPageNum, clazz);
    }

    @Override
    public T getById(Serializable id) {
        return getById(id, clazz);
    }

    @Override
    public <R> R getById(Serializable id, Class<R> rClazz) {
        return baseMapper.getById(id, clazz, rClazz);
    }

    @Override
    public <R> R getById(Serializable id, TypeReference<R> typeReference) {
        return baseMapper.getById(id, clazz, typeReference);
    }

    @Override
    public List<T> getByIds(Collection<? extends Serializable> ids) {
        return getByIds(ids, clazz);
    }

    @Override
    public <R> List<R> getByIds(Collection<? extends Serializable> ids, Class<R> rClazz) {
        return baseMapper.getByIds(ids, clazz, rClazz);
    }

    @Override
    public <R> List<R> getByIds(Collection<? extends Serializable> ids, TypeReference<R> typeReference) {
        return baseMapper.getByIds(ids, clazz, typeReference);
    }

    @Override
    public List<T> queryCommand(String command) {
        return queryCommand(command, clazz);
    }

    @Override
    public <R> List<R> queryCommand(String command, Class<R> rClazz) {
        return baseMapper.queryCommand(command, clazz, rClazz);
    }

    @Override
    public <R> List<R> queryCommand(String command, TypeReference<R> typeReference) {
        return baseMapper.queryCommand(command, clazz, typeReference);
    }

    @Override
    public List<T> getByColumn(SFunction<T, Object> field, Object fieldValue) {
        return getByColumn(field.getFieldNameLine(), fieldValue, clazz);
    }

    @Override
    public <R> List<R> getByColumn(SFunction<T, Object> field, Object fieldValue, Class<R> rClazz) {
        return baseMapper.getByColumn(field.getFieldNameLine(), fieldValue, clazz, rClazz);
    }

    @Override
    public <R> List<R> getByColumn(SFunction<T, Object> field, Object fieldValue, TypeReference<R> typeReference) {
        return baseMapper.getByColumn(field.getFieldNameLine(), fieldValue, clazz, typeReference);
    }

    @Override
    public List<T> getByColumn(String field, Object fieldValue) {
        return getByColumn(field, fieldValue, clazz);
    }

    @Override
    public <R> List<R> getByColumn(String field, Object fieldValue, Class<R> rClazz) {
        return baseMapper.getByColumn(field, fieldValue, clazz, rClazz);
    }

    @Override
    public <R> List<R> getByColumn(String field, Object fieldValue, TypeReference<R> typeReference) {
        return baseMapper.getByColumn(field, fieldValue, clazz, typeReference);
    }

    @Override
    public Boolean exist(Serializable id) {
        return baseMapper.isExist(id, clazz);
    }

    @Override
    public Boolean exist(QueryChainWrapper<T, ?> queryChainWrapper) {
        return baseMapper.isExist(queryChainWrapper, clazz);
    }

}
