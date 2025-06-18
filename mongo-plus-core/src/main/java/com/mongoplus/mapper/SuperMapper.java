package com.mongoplus.mapper;

import com.mongodb.client.model.*;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.execute.Execute;
import com.mongoplus.index.BaseIndex;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.PageResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.mongoplus.toolkit.StringPool.EMPTY;

/**
 * 顶级Mapper接口
 *
 * @author anwen
 */
public interface SuperMapper extends BaseIndex {

    /**
     * 获取MongoPlusClient
     * @author anwen
     */
    MongoPlusClient getMongoPlusClient();

    /**
     * 获取绑定的转换器
     * @author anwen
     */
    MongoConverter getMongoConverter();

    /**
     * 获取执行器
     * @author anwen
     */
    Execute getExecute();

    /**
     * 添加单个
     * @author anwen
     */
    default <T> boolean save(String database, String collectionName, T entity){
        return save(database,collectionName,entity, (InsertOneOptions) null);
    }

    /**
     * 添加单个
     * @author anwen
     */
    @Deprecated
    <T> boolean save(String database, String collectionName, T entity,InsertManyOptions options);

    /**
     * 添加单个
     * @author anwen
     */
    <T> boolean save(String database, String collectionName, T entity,@Nullable InsertOneOptions options);

    /**
     * 添加多个
     * @author anwen
     */
    default <T> Boolean saveBatch(String database,String collectionName,Collection<T> entityList){
        return saveBatch(database,collectionName,entityList,null);
    }

    <T> Boolean saveBatch(String database,String collectionName,Collection<T> entityList,InsertManyOptions options);

    /**
     * 直接通过Bson条件更新，直接使用BaseMapper调用时，最好将构建的Bson，调用一下{@link MongoConverter#writeByUpdate(Object)}
     * @author anwen
     */
    default Long update(String database,String collectionName,Bson queryBasic, Bson updateBasic){
        return update(database,collectionName,queryBasic,updateBasic,null);
    }

    /**
     * 直接通过Bson条件更新，直接使用BaseMapper调用时，最好将构建的Bson，调用一下{@link MongoConverter#writeByUpdate(Object)}
     * @author anwen
     */
    Long update(String database,String collectionName,Bson queryBasic, Bson updateBasic,UpdateOptions options);

    /**
     * 直接通过Bson条件更新，直接使用BaseMapper调用时，最好将构建的Bson，调用一下{@link MongoConverter#writeByUpdate(Object)}
     * @author anwen
     */
    Long updateOne(String database, String collectionName, Bson queryBasic, Bson updateBasic,
                   UpdateOptions options);

    /**
     * 根据queryWrapper修改entity
     * @author anwen
     */
    default <T> Boolean update(String database,String collectionName,T entity, QueryChainWrapper<T,?> queryChainWrapper){
        return update(database,collectionName,entity,queryChainWrapper,null);
    }

    /**
     * 根据queryWrapper修改entity
     * @author anwen
     */
    <T> Boolean update(String database,String collectionName,T entity, QueryChainWrapper<T,?> queryChainWrapper,
                       UpdateOptions options);

    /**
     * 根据queryWrapper修改entity
     * @author anwen
     */
    <T> Boolean updateOne(T entity, QueryChainWrapper<T,?> queryChainWrapper,
                          UpdateOptions options);

    /**
     * 修改，直接根据UpdateWrapper
     * @author anwen
     */
    default Boolean update(String database,String collectionName,UpdateChainWrapper<?, ?> updateChainWrapper){
        return update(database,collectionName,updateChainWrapper,new UpdateOptions());
    }

    /**
     * 修改，直接根据UpdateWrapper
     * @author anwen
     */
    Boolean update(String database,String collectionName,UpdateChainWrapper<?, ?> updateChainWrapper,
                   UpdateOptions options);

    /**
     * 修改，直接根据UpdateWrapper
     * @author anwen
     */
    Boolean updateOne(String database,String collectionName,UpdateChainWrapper<?, ?> updateChainWrapper,
                   UpdateOptions options);

    /**
     * 批量操作
     * @param writeModelList writeModelList
     * @return {@link Integer}
     * @author anwen
     */
    default Integer bulkWrite(String database,String collectionName,List<WriteModel<Document>> writeModelList){
        return bulkWrite(database,collectionName,writeModelList,null);
    }

    /**
     * 批量操作
     * @param writeModelList writeModelList
     * @return {@link Integer}
     * @author anwen
     */
    Integer bulkWrite(String database,String collectionName,List<WriteModel<Document>> writeModelList,BulkWriteOptions options);

    /**
     * 是否存在
     * @param id id
     * @return {@link boolean}
     * @author anwen
     */
    boolean isExist(String database,String collectionName,Serializable id);

    /**
     * 根据条件查询是否存在
     * @param queryChainWrapper 条件
     * @return {@link boolean}
     * @author anwen
     */
    boolean isExist(String database,String collectionName,QueryChainWrapper<?,?> queryChainWrapper);


    /**
     * 删除，直接根据UpdateWrapper
     * @param updateChainWrapper 条件
     * @return {@link Boolean}
     * @author anwen
     */
    default Boolean remove(String database,String collectionName,UpdateChainWrapper<?, ?> updateChainWrapper){
        return remove(database,collectionName,updateChainWrapper,null);
    }

    /**
     * 删除，直接根据UpdateWrapper
     * @param updateChainWrapper 条件
     * @return {@link Boolean}
     * @author anwen
     */
    Boolean remove(String database,String collectionName,UpdateChainWrapper<?, ?> updateChainWrapper,
                   DeleteOptions options);

    /**
     * 删除，直接根据UpdateWrapper
     * @param updateChainWrapper 条件
     * @return {@link Boolean}
     * @author anwen
     */
    default Boolean removeOne(String database,String collectionName,UpdateChainWrapper<?, ?> updateChainWrapper){
        return removeOne(database,collectionName,updateChainWrapper,null);
    }

    /**
     * 删除，直接根据UpdateWrapper
     * @param updateChainWrapper 条件
     * @return {@link Boolean}
     * @author anwen
     */
    Boolean removeOne(String database,String collectionName,UpdateChainWrapper<?, ?> updateChainWrapper,
                      DeleteOptions options);

    /**
     * 根据条件删除
     * @param filter 条件
     * @return {@link Long}
     * @author anwen
     */
    default Long remove(String database,String collectionName,Bson filter){
        return remove(database,collectionName,filter,null);
    }

    /**
     * 根据条件删除
     * @param filter 条件
     * @return {@link Long}
     * @author anwen
     */
    Long remove(String database,String collectionName,Bson filter,DeleteOptions options);

    /**
     * 根据条件删除
     * @param filter 条件
     * @return {@link Long}
     * @author anwen
     */
    default Long removeOne(String database,String collectionName,Bson filter){
        return removeOne(database,collectionName,filter,null);
    }

    /**
     * 根据条件删除
     * @param filter 条件
     * @return {@link Long}
     * @author anwen
     */
    Long removeOne(String database,String collectionName,Bson filter,DeleteOptions options);

    /**
     * 根据条件查询总数
     * @param queryChainWrapper 条件
     * @return {@link long}
     * @author anwen
     */
    long count(String database,String collectionName,QueryChainWrapper<?, ?> queryChainWrapper);

    /**
     * 返回第N页
     * @author anwen
     */
    long recentPageCount(String database,String collectionName,List<CompareCondition> compareConditionList, Integer pageNum, Integer pageSize, Integer recentPageNum);


    /**
     * 查询所有
     * @param rClazz 返回的class
     * @return {@link List <T>}
     * @author anwen
     */
    <R> List<R> list(String database,String collectionName,Class<R> rClazz);

    /**
     * 查询所有
     * @param typeReference 返回的class
     * @return {@link List <T>}
     * @author anwen
     */
    <R> List<R> list(String database,String collectionName, TypeReference<R> typeReference);

    /**
     * 根据条件查询
     * @param queryChainWrapper 条件
     * @return {@link List<T>}
     * @author anwen
     */
    <T,R> List<R> list(String database,String collectionName,QueryChainWrapper<T,?> queryChainWrapper, Class<R> rClazz);

    /**
     * 根据条件查询
     * @param queryChainWrapper 条件
     * @return {@link List<R>}
     * @author anwen
     */
    <T,R> List<R> list(String database,String collectionName,QueryChainWrapper<T,?> queryChainWrapper, TypeReference<R> typeReference);

    /**
     * 管道查询
     * @param aggregate 管道构建
     * @return {@link List<R>}
     * @author anwen
     */
    <R> List<R> aggregateList(String database,String collectionName,Aggregate<?> aggregate, Class<R> rClazz);

    /**
     * 管道查询，返回单个
     * @param aggregate 管道构建
     * @return {@link List<R>}
     * @author anwen
     */
    <R> R aggregateOne(String database,String collectionName,Aggregate<?> aggregate, Class<R> rClazz);

    /**
     * 管道查询
     * @param aggregate 管道构建
     * @return {@link List<R>}
     * @author anwen
     */
    <R> List<R> aggregateList(String database,String collectionName,Aggregate<?> aggregate, TypeReference<R> typeReference);

    /**
     * 管道查询，返回单个
     * @param aggregate 管道构建
     * @return {@link List<R>}
     * @author anwen
     */
    <R> R aggregateOne(String database,String collectionName,Aggregate<?> aggregate, TypeReference<R> typeReference);

    /**
     * 根据条件查询单个
     * @param queryChainWrapper 条件
     * @return {@link T}
     * @author anwen
     */
    <T,R> R one(String database,String collectionName,QueryChainWrapper<T,?> queryChainWrapper,Class<R> rClazz);

    /**
     * 根据条件查询单个
     * @param queryChainWrapper 条件
     * @return {@link T}
     * @author anwen
     */
    <T,R> R one(String database,String collectionName,QueryChainWrapper<T,?> queryChainWrapper,TypeReference<R> typeReference);

    /**
     * 分页查询，如果queryWrapper有条件，查询会慢，因为需要重新进行count查询
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @return {@link PageResult <T>}
     * @author anwen
     */
    <T,R> PageResult<R> page(String database,String collectionName,QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize,Class<R> rClazz);

    /**
     * 分页查询，如果queryWrapper有条件，查询会慢，因为需要重新进行count查询
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @return {@link PageResult <T>}
     * @author anwen
     */
    <T,R> PageResult<R> page(String database,String collectionName,QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize,TypeReference<R> typeReference);

    /**
     * 分页查询，返回List，不进行count查询，比page查询效率高
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @return {@link List<T>}
     * @author anwen
     */
    <T,R> List<R> pageList(String database,String collectionName,QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);

    /**
     * 分页查询，返回List，不进行count查询，比page查询效率高
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @return {@link List<T>}
     * @author anwen
     */
    <T,R> List<R> pageList(String database,String collectionName,QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);

    /**
     * 分页查询，查询最近n页的数据
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近N页的数据
     * @return {@link PageResult<T>}
     * @author anwen
     */
    <T,R> PageResult<R> page(String database,String collectionName,QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum,Class<R> rClazz);

    /**
     * 分页查询，查询最近n页的数据
     * @param queryChainWrapper 条件
     * @param pageNum 当前页
     * @param pageSize 每页显示行数
     * @param recentPageNum 查询最近N页的数据
     * @return {@link PageResult<T>}
     * @author anwen
     */
    <T,R> PageResult<R> page(String database,String collectionName,QueryChainWrapper<T,?> queryChainWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum,TypeReference<R> typeReference);

    /**
     * 根据多个id查询
     * @param ids ids
     * @return {@link java.util.List<R>}
     * @author anwen
     */
    <R> List<R> getByIds(String database,String collectionName,Collection<? extends Serializable> ids,Class<R> rClazz);

    /**
     * 根据多个id查询
     * @param ids ids
     * @return {@link java.util.List<R>}
     * @author anwen
     */
    <R> List<R> getByIds(String database,String collectionName,Collection<? extends Serializable> ids,TypeReference<R> typeReference);

    /**
     * 根据id查询单个
     * @author anwen
     */
    <R> R getById(String database,String collectionName,Serializable id,Class<R> rClazz);

    /**
     * 根据id查询单个
     * @author anwen
     */
    <R> R getById(String database,String collectionName,Serializable id,TypeReference<R> typeReference);

    /**
     * 根据传入命令进行查询
     * @param command 命令，请传入mongo命令的find中完整的json
     * @return {@link java.util.List<R>}
     * @author anwen
     */
    <R> List<R> queryCommand(String database,String collectionName,String command,Class<R> rClazz);

    /**
     * 根据传入命令进行查询
     * @param command 命令，请传入mongo命令的find中完整的json
     * @return {@link java.util.List<R>}
     * @author anwen
     */
    <R> List<R> queryCommand(String database,String collectionName,String command,TypeReference<R> typeReference);

    /**
     * 根据某列进行查询
     * @author anwen
     */
    <R> List<R> getByColumn(String database,String collectionName,String column,Object value,Class<R> rClazz);

    /**
     * 根据某列进行查询
     * @author anwen
     */
    <R> List<R> getByColumn(String database,String collectionName,String column,Object value,TypeReference<R> typeReference);

    /**
     * 查询总数，estimatedDocumentCount高效率查询，但是不接收条件
     * @author anwen
     */
    long count(String database,String collectionName);

    /**
     * 命令查询，支持find和aggregate
     * @param command 命令
     * @param clazz 返回值类型
     * @return {@link List<T>}
     * @author anwen
     */
    default <T> List<T> command(String command,Class<T> clazz){
        return command(command, new TypeReference<T>(clazz) {});
    }

    /**
     * 命令查询，支持find和aggregate
     * @param command 命令
     * @param typeReference 返回值类型
     * @return {@link List<T>}
     * @author anwen
     */
    default <T> List<T> command(String command,TypeReference<T> typeReference){
        return command(EMPTY,command,typeReference);
    }

    /**
     * 命令查询，支持find和aggregate
     * @param command 命令
     * @param clazz 返回值类型
     * @return {@link List<T>}
     * @author anwen
     */
    default <T> List<T> command(String database , String command,Class<T> clazz){
        return command(database, command, new TypeReference<T>(clazz) {});
    }

    /**
     * 命令查询，支持find和aggregate
     * @param command 命令
     * @param typeReference 返回值类型
     * @return {@link List<T>}
     * @author anwen
     */
    <T> List<T> command(String database , String command,TypeReference<T> typeReference);

}
