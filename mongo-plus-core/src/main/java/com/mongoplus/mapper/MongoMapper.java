package com.mongoplus.mapper;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.PageParam;
import com.mongoplus.model.PageResult;
import com.mongoplus.support.SFunction;
import org.bson.Document;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * mapper层接口，只继承接口即可使用，如果实现类实现了MongoMapperImpl类，则不会自动为接口创建实现类
 */
public interface MongoMapper<T> {

    BaseMapper getBaseMapper();

    /**
     * 获取当前service所对应的泛型类
     * @return {@link Class<T>}
     * @author JiaChaoYang
     */
    Class<T> getGenericityClass();

    /**
     * 获取当前service所对应的MongoCollection
     * @author JiaChaoYang
     */
    MongoCollection<Document> getCollection();

    /**
     * 添加
     * @param entity 添加的对象
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:27
     */
    default Boolean save(T entity){
        return save(entity, (InsertOneOptions) null);
    }

    /**
     * 添加
     * @param entity 添加的对象
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:27
     */
    @Deprecated
    Boolean save(T entity, InsertManyOptions options);

    /**
     * 添加
     * @param entity 添加的对象
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:27
     */
    Boolean save(T entity, InsertOneOptions options);

    /**
     * 添加多个
     * @param entityList 对象集合
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:56
     */
    default Boolean saveBatch(Collection<T> entityList){
        return saveBatch(entityList,null);
    }

    /**
     * 添加多个
     * @param entityList 对象集合
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:56
     */
    Boolean saveBatch(Collection<T> entityList,InsertManyOptions options);

    /**
     * 添加或修改
     * @param entity 对象
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:57
     */
    Boolean saveOrUpdate(T entity);

    /**
     * 添加或修改
     * @param entity 对象
     * @param isQueryDatabase 是否查询数据库判断添加或修改
     * @return {@link boolean}
     * @author anwen
     */
    boolean saveOrUpdate(T entity, boolean isQueryDatabase);

    /**
     * 根据传入wrapper条件判断添加修改，传递_id并不会修改
     * @param entity 对象
     * @param queryChainWrapper 条件
     * @return java.lang.Boolean
     * @author JiaChaoYang
     */
    Boolean saveOrUpdateWrapper(T entity, QueryChainWrapper<T ,?> queryChainWrapper);

    /**
     * 批量添加或修改
     * @param entityList 对象集合
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:57
     */
    Boolean saveOrUpdateBatch(Collection<T> entityList);

    /**
     * 批量添加或修改
     * @param entityList 对象集合
     * @param isQueryDatabase 是否查询数据库判断添加或修改
     * @return {@link boolean}
     * @author anwen
     */
    boolean saveOrUpdateBatch(Collection<T> entityList,boolean isQueryDatabase);

    /**
     * 根据传入wrapper条件判断批量添加修改，传递_id并不会修改
     * @param entityList 对象集合
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:57
     */
    Boolean saveOrUpdateBatchWrapper(Collection<T> entityList,QueryChainWrapper<T,?> queryChainWrapper);

    /**
     * 修改
     * @param entity 修改的对象，需要包含id
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:28
     */
    default Boolean updateById(T entity){
        return updateById(entity,null);
    }

    /**
     * 修改
     * @param entity 修改的对象，需要包含id
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:28
     */
    Boolean updateById(T entity, UpdateOptions options);

    /**
     * 根据id修改多个
     * @param entityList 修改的对象，需要包含id
     * @return {@link java.lang.Boolean}
     * @author anwen
     */
    Boolean updateBatchByIds(Collection<T> entityList);

    /**
     * 通过列进行修改
     * @param entity 修改的实体
     * @param column 根据什么列修改
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:46
     */
    default Boolean updateByColumn(T entity, SFunction<T, Object> column){
        return updateByColumn(entity,column,null);
    }

    /**
     * 通过列进行修改
     * @param entity 修改的实体
     * @param column 根据什么列修改
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:46
     */
    Boolean updateByColumn(T entity, SFunction<T, Object> column,UpdateOptions options);

    /**
     * 通过列进行修改
     * @param entity 修改的实体
     * @param column 根据什么列修改
     * @return {@link java.lang.Boolean}
     * @author anwen
     */
    default Boolean updateByColumn(T entity, String column){
        return updateByColumn(entity,column,null);
    }

    /**
     * 通过列进行修改
     * @param entity 修改的实体
     * @param column 根据什么列修改
     * @return {@link java.lang.Boolean}
     * @author anwen
     */
    Boolean updateByColumn(T entity, String column,UpdateOptions options);

    /**
     * 根据条件删除
     * @param updateChainWrapper 条件
     * @return java.lang.Boolean
     * @author JiaChaoYang
     */
    default Boolean remove(UpdateChainWrapper<T,?> updateChainWrapper){
        return remove(updateChainWrapper,null);
    }

    /**
     * 根据条件删除
     * @param updateChainWrapper 条件
     * @return java.lang.Boolean
     * @author JiaChaoYang
     */
    Boolean remove(UpdateChainWrapper<T,?> updateChainWrapper, DeleteOptions options);

    /**
     * 根据条件修改
     * @param updateChainWrapper 条件
     * @return java.lang.Boolean
     * @author JiaChaoYang
     */
    default Boolean update(UpdateChainWrapper<T,?> updateChainWrapper){
        return update(updateChainWrapper,null);
    }

    /**
     * 根据条件修改
     * @param updateChainWrapper 条件
     * @return java.lang.Boolean
     * @author JiaChaoYang
     */
    Boolean update(UpdateChainWrapper<T,?> updateChainWrapper,UpdateOptions options);

    /**
     * 根据条件修改
     * @author JiaChaoYang
     */
    default Boolean update(T entity,QueryChainWrapper<T,?> queryChainWrapper){
        return update(entity,queryChainWrapper,null);
    }

    /**
     * 根据条件修改
     * @author JiaChaoYang
     */
    Boolean update(T entity,QueryChainWrapper<T,?> queryChainWrapper,UpdateOptions options);

    /**
     * 根据id删除
     * @param id 数据id
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:47
     */
    default Boolean removeById(Serializable id){
        return removeById(id,null);
    }

    /**
     * 根据id删除
     * @param id 数据id
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:47
     */
    Boolean removeById(Serializable id,DeleteOptions options);

    /**
     * 根据字段删除
     * @param column 字段名
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 14:01
     */
    default Boolean removeByColumn(SFunction<T, Object> column, Object value){
        return removeByColumn(column,value,null);
    }

    /**
     * 根据字段删除
     * @param column 字段名
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 14:01
     */
    Boolean removeByColumn(SFunction<T, Object> column, Object value,DeleteOptions options);

    /**
     * 根据字段删除
     * @param column 字段
     * @param value 值
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 14:05
     */
    default Boolean removeByColumn(String column,Object value){
        return removeByColumn(column,value,null);
    }

    /**
     * 根据字段删除
     * @param column 字段
     * @param value 值
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 14:05
     */
    Boolean removeByColumn(String column,Object value,DeleteOptions options);

    /**
     * 根据id批量删除
     * @param idList id集合
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:59
     */
    default Boolean removeBatchByIds(Collection<? extends Serializable> idList){
        return removeBatchByIds(idList,null);
    }

    /**
     * 根据id批量删除
     * @param idList id集合
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @since 2023/2/9 13:59
     */
    Boolean removeBatchByIds(Collection<? extends Serializable> idList,DeleteOptions options);

    /**
     * 查询所有
     * @return java.util.List<T>
     * @author JiaChaoYang
     * @since 2023/2/10 9:48
     */
    List<T> list();

    /**
     * 查询所有，返回指定类型
     * @param rClazz 返回类型
     * @return {@link java.util.List<R>}
     * @author anwen
     */
    <R> List<R> list(Class<R> rClazz);

    /**
     * 查询所有，返回指定类型
     * @param typeReference 返回类型
     * @author anwen
     */
    <R> List<R> list(TypeReference<R> typeReference);

    /**
     * 管道查询
     * @param aggregate 管道
     * @return {@link List<T>}
     * @author anwen
     */
    List<T> list(Aggregate<?> aggregate);

    /**
     * 管道查询
     * @param aggregate 管道
     * @param rClass 返回值类型
     * @return {@link java.util.List<R>}
     * @author anwen
     */
    <R> List<R> list(Aggregate<?> aggregate, Class<R> rClass);

    /**
     * 管道查询
     * @param aggregate 管道
     * @param typeReference 返回值类型
     * @return {@link java.util.List<R>}
     * @author anwen
     */
    <R> List<R> list(Aggregate<?> aggregate, TypeReference<R> typeReference);

    /**
     * 查询单个
     * @param queryChainWrapper 条件
     * @return {@link T}
     * @author anwen
     */
    T one(QueryChainWrapper<T,?> queryChainWrapper);

    /**
     * 查询单个
     * @param queryChainWrapper 条件
     * @param rClazz 返回值类型
     * @return {@link R}
     * @author anwen
     */
    <R> R one(QueryChainWrapper<T,?> queryChainWrapper,Class<R> rClazz);

    /**
     * 查询单个
     * @param queryChainWrapper 条件
     * @param typeReference 返回值类型
     * @return {@link R}
     * @author anwen
     */
    <R> R one(QueryChainWrapper<T,?> queryChainWrapper,TypeReference<R> typeReference);

    /**
     * 查询单个
     * @param aggregate 条件
     * @return {@link T}
     * @author anwen
     */
    T one(Aggregate<?> aggregate);

    /**
     * 查询单个
     * @param aggregate 条件
     * @param rClazz 返回值类型
     * @return {@link R}
     * @author anwen
     */
    <R> R one(Aggregate<?> aggregate,Class<R> rClazz);

    /**
     * 查询单个
     * @param aggregate 条件
     * @param typeReference 返回值类型
     * @return {@link R}
     * @author anwen
     */
    <R> R one(Aggregate<?> aggregate,TypeReference<R> typeReference);

    /**
     * 查询列表
     * @param queryChainWrapper 条件
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    List<T> list(QueryChainWrapper<T ,?> queryChainWrapper);

    /**
     * 查询列表
     * @param queryChainWrapper 条件
     * @param rClazz 返回值类型
     * @return {@link java.util.List<R>}
     * @author anwen
     */
    <R> List<R> list(QueryChainWrapper<T ,?> queryChainWrapper,Class<R> rClazz);

    /**
     * 查询列表
     * @param queryChainWrapper 条件
     * @param typeReference 返回值类型
     * @return {@link java.util.List<R>}
     * @author anwen
     */
    <R> List<R> list(QueryChainWrapper<T ,?> queryChainWrapper,TypeReference<R> typeReference);

    /**
     * 获取总数
     * @return {@link long}
     * @author anwen
     */
    long count();

    /**
     * 获取总数
     * @param queryChainWrapper 条件
     * @return {@link long}
     * @author anwen
     */
    long count(QueryChainWrapper<T,?> queryChainWrapper);

    /**
     * 分页查询
     * @param pageParam 分页参数对象
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    PageResult<T> page(PageParam pageParam);

    /**
     * 分页查询
     * @param pageParam 分页参数对象
     * @param rClazz 返回值类型
     * @return {@link com.mongoplus.model.PageResult<R>}
     * @author anwen
     */
    <R> PageResult<R> page(PageParam pageParam,Class<R> rClazz);

    /**
     * 分页查询
     * @param pageParam 分页参数对象
     * @param typeReference 返回值类型
     * @return {@link com.mongoplus.model.PageResult<R>}
     * @author anwen
     */
    <R> PageResult<R> page(PageParam pageParam,TypeReference<R> typeReference);

    /**
     * 分页查询
     * @param pageParam 分页参数对象
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    PageResult<T> page(PageParam pageParam, Integer recentPageNum);

    /**
     * 分页查询
     * @param pageParam 分页参数对象
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @param rClazz 返回值类型
     * @return {@link com.mongoplus.model.PageResult<R>}
     * @author anwen
     */
    <R> PageResult<R> page(PageParam pageParam, Integer recentPageNum,Class<R> rClazz);

    /**
     * 分页查询
     * @param pageParam 分页参数对象
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @param typeReference 返回值类型
     * @return {@link com.mongoplus.model.PageResult<R>}
     * @author anwen
     */
    <R> PageResult<R> page(PageParam pageParam, Integer recentPageNum,TypeReference<R> typeReference);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    PageResult<T> page(Integer pageNum,Integer pageSize);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param rClazz 返回值类型
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    <R> PageResult<R> page(Integer pageNum,Integer pageSize,Class<R> rClazz);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param typeReference 返回值类型
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    <R> PageResult<R> page(Integer pageNum,Integer pageSize,TypeReference<R> typeReference);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    PageResult<T> page(Integer pageNum,Integer pageSize, Integer recentPageNum);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param queryChainWrapper 条件
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    PageResult<T> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize);

    /**
     * 分页查询
     * @param queryChainWrapper 条件
     * @param pageParam 分页参数对象
     * @return {@link com.mongoplus.model.PageResult<T>}
     * @author anwen
     */
    PageResult<T> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam);

    /**
     * 分页查询
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @return {@link com.mongoplus.model.PageResult<T>}
     * @author anwen
     */
    PageResult<T> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum);

    /**
     * 分页查询
     * @param queryChainWrapper 条件
     * @param pageParam 分页参数对象
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @return {@link com.mongoplus.model.PageResult<T>}
     * @author anwen
     */
    PageResult<T> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam, Integer recentPageNum);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @param rClazz 返回值类型
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    <R> PageResult<R> page(Integer pageNum,Integer pageSize, Integer recentPageNum,Class<R> rClazz);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @param typeReference 返回值类型
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    <R> PageResult<R> page(Integer pageNum,Integer pageSize, Integer recentPageNum,TypeReference<R> typeReference);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param queryChainWrapper 条件
     * @param rClazz 返回值类型
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize,Class<R> rClazz);

    /**
     * 分页查询
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param queryChainWrapper 条件
     * @param typeReference 返回值类型
     * @return com.mongoplus.sql.model.PageResult<T>
     * @author JiaChaoYang
     */
    <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize,TypeReference<R> typeReference);

    /**
     * 分页查询
     * @param queryChainWrapper 条件
     * @param pageParam 分页参数对象
     * @param rClazz 返回值类型
     * @return {@link com.mongoplus.model.PageResult<T>}
     * @author anwen
     */
    <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam,Class<R> rClazz);

    /**
     * 分页查询
     * @param queryChainWrapper 条件
     * @param pageParam 分页参数对象
     * @param typeReference 返回值类型
     * @return {@link com.mongoplus.model.PageResult<T>}
     * @author anwen
     */
    <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam,TypeReference<R> typeReference);

    /**
     * 分页查询
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @param rClazz 返回值类型
     * @return {@link com.mongoplus.model.PageResult<T>}
     * @author anwen
     */
    <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum,Class<R> rClazz);

    /**
     * 分页查询
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @param typeReference 返回值类型
     * @return {@link com.mongoplus.model.PageResult<T>}
     * @author anwen
     */
    <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum,TypeReference<R> typeReference);

    /**
     * 分页查询
     * @param queryChainWrapper 条件
     * @param pageParam 分页参数对象
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @param rClazz 返回值类型
     * @return {@link com.mongoplus.model.PageResult<T>}
     * @author anwen
     */
    <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam, Integer recentPageNum,Class<R> rClazz);

    /**
     * 分页查询
     * @param queryChainWrapper 条件
     * @param pageParam 分页参数对象
     * @param recentPageNum 查询最近n页的数据  {参数=null 表示仅查询当前页数据}  {参数取值[5-50] 表示查询最近[5-50]页的数据 建议recentPageNum等于10 参考 百度分页检索}
     * @param typeReference 返回值类型
     * @return {@link com.mongoplus.model.PageResult<T>}
     * @author anwen
     */
    <R> PageResult<R> page(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam, Integer recentPageNum,TypeReference<R> typeReference);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param pageParam 分页参数对象
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    List<T> pageList(PageParam pageParam);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param pageParam 分页参数对象
     * @param rClazz 返回值类型
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <R> List<R> pageList(PageParam pageParam,Class<R> rClazz);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param pageParam 分页参数对象
     * @param typeReference 返回值类型
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <R> List<R> pageList(PageParam pageParam, TypeReference<R> typeReference);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    List<T> pageList(Integer pageNum,Integer pageSize);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param rClazz 返回值类型
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <R> List<R> pageList(Integer pageNum,Integer pageSize,Class<R> rClazz);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param typeReference 返回值类型
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <R> List<R> pageList(Integer pageNum,Integer pageSize,TypeReference<R> typeReference);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    List<T> pageList(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param rClazz 返回值类型
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize,Class<R> rClazz);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param typeReference 返回值类型
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper, Integer pageNum, Integer pageSize,TypeReference<R> typeReference);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param queryChainWrapper 条件
     * @param pageParam 分页参数对象
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    List<T> pageList(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param queryChainWrapper 条件
     * @param pageParam 分页参数对象
     * @param rClazz 返回值类型
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam,Class<R> rClazz);

    /**
     * 返回List的page，无需进行count查询，速度会比较快
     * @param queryChainWrapper 条件
     * @param pageParam 分页参数对象
     * @param typeReference 返回值类型
     * @return {@link java.util.List<T>}
     * @author anwen
     */
    <R> List<R> pageList(QueryChainWrapper<T, ?> queryChainWrapper, PageParam pageParam,TypeReference<R> typeReference);

    /**
     * 根据id查询单个
     * @param id id
     * @return T
     * @author JiaChaoYang
     */
    T getById(Serializable id);

    /**
     * 根据id查询单个
     * @param id id
     * @return T
     * @author JiaChaoYang
     */
    <R> R getById(Serializable id,Class<R> rClazz);

    /**
     * 根据id查询单个
     * @param id id
     * @return T
     * @author JiaChaoYang
     */
    <R> R getById(Serializable id,TypeReference<R> typeReference);

    /**
     * 根据多个id查询
     * @param ids id集合
     * @return {@link List<T>}
     * @author anwen
     */
    List<T> getByIds(Collection<? extends Serializable> ids);

    /**
     * 根据多个id查询
     * @param ids id集合
     * @return {@link List<T>}
     * @author anwen
     */
    <R> List<R> getByIds(Collection<? extends Serializable> ids,Class<R> rClazz);

    /**
     * 根据多个id查询
     * @param ids id集合
     * @return {@link List<T>}
     * @author anwen
     */
    <R> List<R> getByIds(Collection<? extends Serializable> ids,TypeReference<R> typeReference);

    /**
     * 命令查询接口，传入值为json，如{eq:XXX}
     * @param command 命令json
     * @return java.util.List<T>
     * @author JiaChaoYang
     */
    List<T> queryCommand(String command);

    /**
     * 命令查询接口，传入值为json，如{eq:XXX}
     * @param command 命令json
     * @return java.util.List<T>
     * @author JiaChaoYang
     */
    <R> List<R> queryCommand(String command,Class<R> rClazz);

    /**
     * 命令查询接口，传入值为json，如{eq:XXX}
     * @param command 命令json
     * @return java.util.List<T>
     * @author JiaChaoYang
     */
    <R> List<R> queryCommand(String command,TypeReference<R> typeReference);

    /**
     * 根据某一列查询
     * @param field 字段
     * @param fieldValue 字段值
     * @return List<T>
     * @author JiaChaoYang
     */
    List<T> getByColumn(SFunction<T,Object> field,Object fieldValue);

    /**
     * 根据某一列查询
     * @param field 字段
     * @param fieldValue 字段值
     * @return List<T>
     * @author JiaChaoYang
     */
    <R> List<R> getByColumn(SFunction<T,Object> field,Object fieldValue,Class<R> rClazz);

    /**
     * 根据某一列查询
     * @param field 字段
     * @param fieldValue 字段值
     * @return List<T>
     * @author JiaChaoYang
     */
    <R> List<R> getByColumn(SFunction<T,Object> field,Object fieldValue,TypeReference<R> typeReference);

    /**
     * 根据某一列查询
     * @param field 字段
     * @param fieldValue 字段值
     * @return T
     * @author JiaChaoYang
     */
    List<T> getByColumn(String field,Object fieldValue);

    /**
     * 根据某一列查询
     * @param field 字段
     * @param fieldValue 字段值
     * @return T
     * @author JiaChaoYang
     */
    <R> List<R> getByColumn(String field,Object fieldValue,Class<R> rClazz);

    /**
     * 根据某一列查询
     * @param field 字段
     * @param fieldValue 字段值
     * @return T
     * @author JiaChaoYang
     */
    <R> List<R> getByColumn(String field,Object fieldValue,TypeReference<R> typeReference);

    /**
     * 是否存在
     * @param id id
     * @return java.lang.Boolean
     * @author JiaChaoYang
     */
    Boolean exist(Serializable id);

    /**
     * 是否存在
     * @param queryChainWrapper wrapper条件
     * @return java.lang.Boolean
     * @author JiaChaoYang
     */
    Boolean exist(QueryChainWrapper<T,?> queryChainWrapper);

}
