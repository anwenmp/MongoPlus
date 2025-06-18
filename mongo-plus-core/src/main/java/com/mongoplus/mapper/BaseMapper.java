package com.mongoplus.mapper;

import com.mongodb.client.model.*;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.PageResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;


/**
 * mapper层
 * TODO 名称待定，方法留CRUD，不同数据库实现该接口
 * @author JiaChaoYang
 **/
public interface BaseMapper extends Mapper {

    /**
     * 添加单个
     * @author anwen
     */
    default <T> boolean save(T entity){
        return save(entity, (InsertOneOptions) null);
    }

    /**
     * 添加单个
     * @author anwen
     */
    @Deprecated
    <T> boolean save(T entity,InsertManyOptions options);

    /**
     * 添加单个
     * @author anwen
     */
    <T> boolean save(T entity,InsertOneOptions options);

    /**
     * 添加多个
     * @author anwen
     */
    default <T> Boolean saveBatch(Collection<T> entityList){
        return saveBatch(entityList,null);
    }

    /**
     * 添加多个
     * @author anwen
     */
    <T> Boolean saveBatch(Collection<T> entityList,InsertManyOptions options);

    /**
     * 批量操作
     * @param writeModelList writeModelList
     * @param clazz class
     * @return {@link Integer}
     * @author anwen
     */
    default Integer bulkWrite(List<WriteModel<Document>> writeModelList,Class<?> clazz){
        return bulkWrite(writeModelList,clazz,null);
    }

    /**
     * 批量操作
     * @param writeModelList writeModelList
     * @param clazz class
     * @return {@link Integer}
     * @author anwen
     */
    Integer bulkWrite(List<WriteModel<Document>> writeModelList,Class<?> clazz,BulkWriteOptions options);

    /**
     * 根据queryWrapper修改entity
     * @author anwen
     */
    default <T> Boolean update(T entity,QueryChainWrapper<T,?> queryChainWrapper){
        return update(entity,queryChainWrapper,null);
    }

    /**
     * 根据queryWrapper修改entity
     * @author anwen
     */
    <T> Boolean update(T entity,QueryChainWrapper<T,?> queryChainWrapper,UpdateOptions options);

    /**
     * 是否存在
     * @param id id
     * @param clazz class
     * @return {@link boolean}
     * @author anwen
     */
    boolean isExist(Serializable id,Class<?> clazz);

    /**
     * 根据条件查询是否存在
     * @param queryChainWrapper 条件
     * @param clazz class
     * @return {@link boolean}
     * @author anwen
     */
    boolean isExist(QueryChainWrapper<?,?> queryChainWrapper,Class<?> clazz);


    /**
     * 修改，直接根据UpdateWrapper
     * @author anwen
     */
    default Boolean update(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz){
        return update(updateChainWrapper, clazz,null);
    }

    /**
     * 修改，直接根据UpdateWrapper
     * @author anwen
     */
    Boolean update(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz,UpdateOptions options);

    /**
     * 修改，直接根据UpdateWrapper
     * @author anwen
     */
    Boolean updateOne(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz,UpdateOptions options);

    /**
     * 删除，直接根据UpdateWrapper
     * @param updateChainWrapper 条件
     * @param clazz class
     * @return {@link Boolean}
     * @author anwen
     */
    default Boolean remove(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz){
        return remove(updateChainWrapper, clazz,null);
    }

    /**
     * 删除，直接根据UpdateWrapper
     * @param updateChainWrapper 条件
     * @param clazz class
     * @return {@link Boolean}
     * @author anwen
     */
    Boolean remove(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz,DeleteOptions options);

    /**
     * 删除，直接根据UpdateWrapper
     * @param updateChainWrapper 条件
     * @param clazz class
     * @return {@link Boolean}
     * @author anwen
     */
    default Boolean removeOne(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz){
        return removeOne(updateChainWrapper, clazz,null);
    }

    /**
     * 删除，直接根据UpdateWrapper
     * @param updateChainWrapper 条件
     * @param clazz class
     * @return {@link Boolean}
     * @author anwen
     */
    Boolean removeOne(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz,DeleteOptions options);

    /**
     * 根据条件删除
     * @param filter 条件
     * @param clazz class
     * @return {@link Long}
     * @author anwen
     */
    default Long remove(Bson filter,Class<?> clazz){
        return remove(filter, clazz,null);
    }

    /**
     * 根据条件删除
     * @param filter 条件
     * @param clazz class
     * @return {@link Long}
     * @author anwen
     */
    Long remove(Bson filter,Class<?> clazz,DeleteOptions options);

    /**
     * 根据条件删除
     * @param filter 删除条件
     * @param clazz class
     * @return {@link Long}
     * @author anwen
     */
    default Long removeOne(Bson filter,Class<?> clazz){
        return removeOne(filter, clazz,null);
    }

    /**
     * 根据条件删除
     * @param filter 删除条件
     * @param clazz class
     * @return {@link Long}
     * @author anwen
     */
    Long removeOne(Bson filter,Class<?> clazz,@Nullable DeleteOptions options);

    /**
     * 根据条件查询总数
     * @param queryChainWrapper 条件
     * @param clazz class
     * @return {@link long}
     * @author anwen
     */
    long count(QueryChainWrapper<?, ?> queryChainWrapper,Class<?> clazz);

    /**
     * 返回第N页
     * @author anwen
     */
    long recentPageCount(List<CompareCondition> compareConditionList,Class<?> clazz, Integer pageNum, Integer pageSize, Integer recentPageNum);


    /**
     * 查询所有
     * @param clazz 操作的class
     * @param rClazz 返回的class
     * @return {@link List <T>}
     * @author anwen
     */
    <T,R> List<R> list(Class<T> clazz,Class<R> rClazz);

    /**
     * 查询所有
     * @param clazz 操作的class
     * @return {@link List <T>}
     * @author anwen
     */
    default <T> List<T> list(Class<T> clazz){
        return list(clazz, clazz);
    }

    /**
     * 查询所有
     * @param clazz 操作的class
     * @param typeReference 返回的class
     * @return {@link List <T>}
     * @author anwen
     */
    <T,R> List<R> list(Class<T> clazz, TypeReference<R> typeReference);

    /**
     * 根据条件查询
     * @param queryChainWrapper 条件
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    <T,R> List<R> list(QueryChainWrapper<T,?> queryChainWrapper, Class<T> clazz, Class<R> rClazz);

    /**
     * 根据条件查询
     * @param queryChainWrapper 条件
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    default <T,R> List<R> list(QueryChainWrapper<T,?> queryChainWrapper, Class<R> clazz){
        return list(queryChainWrapper, (Class<T>) clazz, clazz);
    }

    /**
     * 根据条件查询
     * @param queryChainWrapper 条件
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    <T,R> List<R> list(QueryChainWrapper<T,?> queryChainWrapper, Class<T> clazz, TypeReference<R> typeReference);

    /**
     * 管道查询
     * @param aggregate 管道构建
     * @param clazz class
     * @param rClazz 返回值类型
     * @return {@link List<T>}
     * @author anwen
     */
    <T,R> List<R> aggregateList(Aggregate<?> aggregate, Class<T> clazz, Class<R> rClazz);

    /**
     * 管道查询，返回单个
     * @param aggregate 管道构建
     * @param clazz class
     * @param rClazz 返回值类型
     * @return {@link java.util.List<R>}
     * @author anwen
     */
    <T,R> R aggregateOne(Aggregate<?> aggregate, Class<T> clazz, Class<R> rClazz);

    /**
     * 管道查询
     * @param aggregate 管道构建
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    default <T> List<T> aggregateList(Aggregate<?> aggregate, Class<T> clazz){
        return aggregateList(aggregate, clazz, clazz);
    }

    /**
     * 管道查询，返回单个
     * @param aggregate 管道构建
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    default <T> T aggregateOne(Aggregate<?> aggregate, Class<T> clazz){
        return aggregateOne(aggregate, clazz, clazz);
    }

    /**
     * 管道查询
     * @param aggregate 管道构建
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    <T,R> List<R> aggregateList(Aggregate<?> aggregate, Class<T> clazz, TypeReference<R> typeReference);

    /**
     * 管道查询，返回单个
     * @param aggregate 管道构建
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    <T,R> R aggregateOne(Aggregate<?> aggregate, Class<T> clazz, TypeReference<R> typeReference);

    /**
     * 根据条件查询单个
     * @param queryChainWrapper 条件
     * @param clazz class
     * @return {@link T}
     * @author anwen
     */
    <T,R> R one(QueryChainWrapper<T,?> queryChainWrapper,Class<T> clazz,Class<R> rClazz);

    /**
     * 根据条件查询单个
     * @param queryChainWrapper 条件
     * @param clazz class
     * @return {@link T}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    default <T,R> R one(QueryChainWrapper<T,?> queryChainWrapper,Class<R> clazz){
        return one(queryChainWrapper,(Class<T>) clazz, clazz);
    }

    /**
     * 根据条件查询单个
     * @param queryChainWrapper 条件
     * @param clazz class
     * @return {@link T}
     * @author anwen
     */
    <T,R> R one(QueryChainWrapper<T,?> queryChainWrapper,Class<T> clazz,TypeReference<R> typeReference);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param clazz class
     * @return {@link PageResult <T>}
     * @author anwen
     */
    <T,R> PageResult<R> page(Integer pageNum, Integer pageSize, Class<T> clazz,Class<R> rClazz);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param clazz class
     * @return {@link PageResult <T>}
     * @author anwen
     */
    default <T> PageResult<T> page(Integer pageNum, Integer pageSize, Class<T> clazz){
        return page(pageNum, pageSize, clazz, clazz);
    }

    /**
     * 分页查询，如果queryWrapper有条件，查询会慢，因为需要重新进行count查询
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param clazz class
     * @return {@link PageResult <T>}
     * @author anwen
     */
    <T,R> PageResult<R> page(QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<T> clazz,Class<R> rClazz);

    /**
     * 分页查询，如果queryWrapper有条件，查询会慢，因为需要重新进行count查询
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param clazz class
     * @return {@link PageResult <T>}
     * @author anwen
     */
    <T,R> PageResult<R> page(QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<T> clazz,TypeReference<R> typeReference);

    /**
     * 分页查询，返回List，不进行count查询，比page查询效率高
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    default <T,R> List<R> pageList(Integer pageNum, Integer pageSize, Class<T> clazz,Class<R> rClazz){
        return pageList(new QueryWrapper<>(),pageNum, pageSize, clazz, rClazz);
    }

    /**
     * 分页查询，返回List，不进行count查询，比page查询效率高
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    default <T> List<T> pageList(Integer pageNum, Integer pageSize, Class<T> clazz){
        return pageList(pageNum, pageSize, clazz, clazz);
    }

    /**
     * 分页查询，返回List，不进行count查询，比page查询效率高
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    <T,R> List<R> pageList(QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<T> clazz,Class<R> rClazz);

    /**
     * 分页查询，返回List，不进行count查询，比page查询效率高
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param clazz class
     * @return {@link List<T>}
     * @author anwen
     */
    <T,R> List<R> pageList(QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<T> clazz,TypeReference<R> typeReference);

    /**
     * 分页查询，查询最近n页的数据
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近N页的数据
     * @param clazz class
     * @return {@link PageResult<T>}
     * @author anwen
     */
    default <T,R> PageResult<R> page(Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz,Class<R> rClazz){
        return page(new QueryWrapper<>(),pageNum, pageSize, recentPageNum, clazz, rClazz);
    }

    /**
     * 分页查询，查询最近n页的数据
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近N页的数据
     * @param clazz class
     * @return {@link PageResult<T>}
     * @author anwen
     */
    default <T> PageResult<T> page(Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz){
        return page(pageNum, pageSize, recentPageNum, clazz, clazz);
    }

    /**
     * 分页查询，查询最近n页的数据
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近N页的数据
     * @param clazz class
     * @return {@link PageResult<T>}
     * @author anwen
     */
    <T,R> PageResult<R> page(QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz,Class<R> rClazz);

    /**
     * 分页查询，查询最近n页的数据
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近N页的数据
     * @param clazz class
     * @return {@link PageResult<T>}
     * @author anwen
     */
    <T,R> PageResult<R> page(QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz,TypeReference<R> typeReference);

    /**
     * 根据多个id查询
     * @param ids ids
     * @param clazz class
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <T,R> List<R> getByIds(Collection<? extends Serializable> ids, Class<T> clazz,Class<R> rClazz);

    /**
     * 根据多个id查询
     * @param ids ids
     * @param clazz class
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    default <T> List<T> getByIds(Collection<? extends Serializable> ids, Class<T> clazz){
        return getByIds(ids, clazz, clazz);
    }

    /**
     * 根据多个id查询
     * @param ids ids
     * @param clazz class
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <T,R> List<R> getByIds(Collection<? extends Serializable> ids, Class<T> clazz,TypeReference<R> typeReference);

    /**
     * 根据id查询单个
     * @author anwen
     */
    <T,R> R getById(Serializable id,Class<T> clazz,Class<R> rClazz);

    /**
     * 根据id查询单个
     * @author anwen
     */
    default <T> T getById(Serializable id,Class<T> clazz){
        return getById(id, clazz, clazz);
    }

    /**
     * 根据id查询单个
     * @author anwen
     */
    <T,R> R getById(Serializable id,Class<T> clazz,TypeReference<R> typeReference);

    /**
     * 根据传入命令进行查询
     * @param command 命令，请传入mongo命令的find中完整的json
     * @param clazz class
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <T,R> List<R> queryCommand(String command,Class<T> clazz,Class<R> rClazz);

    /**
     * 根据传入命令进行查询
     * @param command 命令，请传入mongo命令的find中完整的json
     * @param clazz class
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    default <T> List<T> queryCommand(String command,Class<T> clazz){
        return queryCommand(command, clazz, clazz);
    }

    /**
     * 根据传入命令进行查询
     * @param command 命令，请传入mongo命令的find中完整的json
     * @param clazz class
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <T,R> List<R> queryCommand(String command,Class<T> clazz,TypeReference<R> typeReference);

    /**
     * 根据某列进行查询
     * @author anwen
     */
    <T,R> List<R> getByColumn(String column,Object value,Class<T> clazz,Class<R> rClazz);

    /**
     * 根据某列进行查询
     * @author anwen
     */
    default <T> List<T> getByColumn(String column,Object value,Class<T> clazz){
        return getByColumn(column, value, clazz, clazz);
    }

    /**
     * 根据某列进行查询
     * @author anwen
     */
    <T,R> List<R> getByColumn(String column,Object value,Class<T> clazz,TypeReference<R> typeReference);

    /**
     * 查询总数，estimatedDocumentCount高效率查询，但是不接收条件
     * @author anwen
     */
    long count(Class<?> clazz);

}
