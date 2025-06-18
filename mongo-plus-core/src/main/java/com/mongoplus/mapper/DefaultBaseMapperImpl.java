package com.mongoplus.mapper;

import com.mongodb.client.model.*;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.PageResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * baseMapper默认实现
 *
 * @author JiaChaoYang
 **/
public class DefaultBaseMapperImpl extends AbstractBaseMapper {

    private final MongoPlusClient mongoPlusClient;

    public DefaultBaseMapperImpl(MongoPlusClient mongoPlusClient, MongoConverter mongoConverter) {
        super(mongoPlusClient, mongoConverter, new ExecutorFactory());
        this.mongoPlusClient = mongoPlusClient;
    }

    public DefaultBaseMapperImpl(MongoPlusClient mongoPlusClient, MongoConverter mongoConverter, ExecutorFactory factory) {
        super(mongoPlusClient, mongoConverter, factory);
        this.mongoPlusClient = mongoPlusClient;
    }

    @Override
    public <T> boolean save(T entity,InsertManyOptions options) {
        MutablePair<String, String> namespace = getNamespace(entity.getClass());
        return save(namespace.left, namespace.right, entity,options);
    }

    @Override
    public <T> boolean save(T entity, InsertOneOptions options) {
        MutablePair<String, String> namespace = getNamespace(entity.getClass());
        return save(namespace.left, namespace.right, entity,options);
    }

    @Override
    public <T> Boolean saveBatch(Collection<T> entityList,InsertManyOptions options) {
        Class<?> clazz = entityList.iterator().next().getClass();
        MutablePair<String, String> namespace = getNamespace(clazz);
        return saveBatch(namespace.left, namespace.right, entityList,options);
    }

    @Override
    public Integer bulkWrite(List<WriteModel<Document>> writeModelList, Class<?> clazz,BulkWriteOptions options) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return bulkWrite(namespace.left, namespace.right, writeModelList,options);
    }

    @Override
    public <T> Boolean update(T entity, QueryChainWrapper<T, ?> queryChainWrapper,UpdateOptions options) {
        MutablePair<String, String> namespace = getNamespace(entity.getClass());
        return update(namespace.left, namespace.right, entity, queryChainWrapper,options);
    }

    /**
     * 查询所有
     *
     * @param clazz  操作的class
     * @param rClazz 返回的class
     * @return {@link List <T>}
     * @author anwen
     */
    @Override
    public <T, R> List<R> list(Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return list(namespace.left, namespace.right, rClazz);
    }

    @Override
    public <T, R> List<R> list(Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return list(namespace.left, namespace.right, typeReference);
    }

    @Override
    public <T, R> List<R> list(QueryChainWrapper<T, ?> queryChainWrapper, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return list(namespace.left, namespace.right, queryChainWrapper, rClazz);
    }

    @Override
    public <T, R> List<R> list(QueryChainWrapper<T, ?> queryChainWrapper, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return list(namespace.left, namespace.right, queryChainWrapper, typeReference);
    }

    @Override
    public <T, R> List<R> aggregateList(Aggregate<?> aggregate, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return aggregateList(namespace.left, namespace.right, aggregate, rClazz);
    }

    @Override
    public <T, R> R aggregateOne(Aggregate<?> aggregate, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return aggregateOne(namespace.left, namespace.right, aggregate, rClazz);
    }

    @Override
    public <T, R> List<R> aggregateList(Aggregate<?> aggregate, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return aggregateList(namespace.left, namespace.right, aggregate, typeReference);
    }

    @Override
    public <T, R> R aggregateOne(Aggregate<?> aggregate, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return aggregateOne(namespace.left, namespace.right, aggregate, typeReference);
    }

    @Override
    public <T, R> R one(QueryChainWrapper<T, ?> queryChainWrapper, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return one(namespace.left, namespace.right, queryChainWrapper, rClazz);
    }

    @Override
    public <T, R> R one(QueryChainWrapper<T, ?> queryChainWrapper, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return one(namespace.left, namespace.right, queryChainWrapper, typeReference);
    }

    @Override
    public <T, R> PageResult<R> page(Integer pageNum, Integer pageSize, Class<T> clazz, Class<R> rClazz) {
        return page(new QueryWrapper<>(),pageNum,pageSize,clazz,rClazz);
    }

    @Override
    public <T, R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return page(namespace.left, namespace.right, queryChainWrapper, pageNum, pageSize, rClazz);
    }

    @Override
    public <T, R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return page(namespace.left, namespace.right, queryChainWrapper, pageNum, pageSize, typeReference);
    }

    @Override
    public <T, R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return pageList(namespace.left, namespace.right, queryChainWrapper, pageNum, pageSize, rClazz);
    }

    @Override
    public <T, R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return pageList(namespace.left, namespace.right, queryChainWrapper, pageNum, pageSize, typeReference);
    }

    @Override
    public <T, R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return page(namespace.left, namespace.right, queryChainWrapper, pageNum, pageSize, recentPageNum, rClazz);
    }

    @Override
    public <T, R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return page(namespace.left, namespace.right, queryChainWrapper, pageNum, pageSize, recentPageNum, typeReference);
    }

    @Override
    public <T, R> R getById(Serializable id, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return getById(namespace.left, namespace.right, id, rClazz);
    }

    @Override
    public <T, R> R getById(Serializable id, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return getById(namespace.left, namespace.right, id, typeReference);
    }

    @Override
    public boolean isExist(Serializable id, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return isExist(namespace.left, namespace.right, id);
    }

    @Override
    public boolean isExist(QueryChainWrapper<?, ?> queryChainWrapper, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return isExist(namespace.left, namespace.right, queryChainWrapper);
    }

    @Override
    public <T, R> List<R> getByIds(Collection<? extends Serializable> ids, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return getByIds(namespace.left, namespace.right, ids, rClazz);
    }

    @Override
    public <T, R> List<R> getByIds(Collection<? extends Serializable> ids, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return getByIds(namespace.left, namespace.right, ids, typeReference);
    }

    @Override
    public Boolean update(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz,UpdateOptions options) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return update(namespace.left, namespace.right, updateChainWrapper,options);
    }

    @Override
    public Boolean updateOne(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz,UpdateOptions options) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return updateOne(namespace.left, namespace.right, updateChainWrapper,options);
    }

    @Override
    public Boolean remove(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz,DeleteOptions options) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return remove(namespace.left, namespace.right, updateChainWrapper,options);
    }

    @Override
    public Boolean removeOne(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz,DeleteOptions options) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return removeOne(namespace.left, namespace.right, updateChainWrapper,options);
    }

    @Override
    public Long remove(Bson filter, Class<?> clazz,DeleteOptions options) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return remove(namespace.left, namespace.right, filter,options);
    }

    @Override
    public Long removeOne(Bson filter, Class<?> clazz, DeleteOptions options) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return removeOne(namespace.left, namespace.right, filter,options);
    }

    @Override
    public long count(QueryChainWrapper<?, ?> queryChainWrapper, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return count(namespace.left, namespace.right, queryChainWrapper);
    }

    /**
     * 分页查询 查询总条数
     *
     * @param compareConditionList 条件集合
     * @param clazz                result class
     * @param pageNum              当前页
     * @param pageSize             每页显示行数
     * @param recentPageNum        查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @return long
     */
    @Override
    public long recentPageCount(List<CompareCondition> compareConditionList, Class<?> clazz, Integer pageNum, Integer pageSize, Integer recentPageNum) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return recentPageCount(namespace.left, namespace.right, compareConditionList, pageNum, pageSize, recentPageNum);
    }

    @Override
    public long count(Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return count(namespace.left, namespace.right);
    }

    @Override
    public <T, R> List<R> queryCommand(String command, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return queryCommand(namespace.left, namespace.right, command, rClazz);
    }

    @Override
    public <T, R> List<R> queryCommand(String command, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return queryCommand(namespace.left, namespace.right, command, typeReference);
    }

    @Override
    public <T, R> List<R> getByColumn(String column, Object value, Class<T> clazz, Class<R> rClazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return getByColumn(namespace.left, namespace.right, column, value, rClazz);
    }

    @Override
    public <T, R> List<R> getByColumn(String column, Object value, Class<T> clazz, TypeReference<R> typeReference) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return getByColumn(namespace.left, namespace.right, column, value, typeReference);
    }

}
