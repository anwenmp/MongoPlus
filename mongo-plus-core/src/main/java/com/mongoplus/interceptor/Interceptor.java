package com.mongoplus.interceptor;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.QueryParam;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * 拦截器，代理{@link com.mongoplus.execute.Execute}接口，增删改查会经过
 *
 * @author JiaChaoYang
 **/
public interface Interceptor {

    /**
     * 拦截器 排序
     *
     * @return 升序 从小到大
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * 前置处理
     *
     * @param executeMethodEnum 执行类型
     * @param source            值
     * @param collection        集合对象
     * @author anwen
     */
    default void beforeExecute(ExecuteMethodEnum executeMethodEnum,
                               Object[] source,
                               MongoCollection<Document> collection){}

    /**
     * 后置处理
     *
     * @param executeMethodEnum 执行类型
     * @param source            参数值
     * @param result            返回值
     * @param collection        集合对象
     * @author anwen
     */
    default void afterExecute(ExecuteMethodEnum executeMethodEnum,
                              Object[] source,
                              Object result,
                              MongoCollection<Document> collection){}

    /**
     * 添加拦截方法
     *
     * @param documentList 经过添加方法的值
     * @return java.util.List<org.bson.Document>
     * @author JiaChaoYang
     */
    @Deprecated
    default List<Document> executeSave(List<Document> documentList) {
        return documentList;
    }

    /**
     * 删除拦截方法
     * @param filter 条件
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    @Deprecated
    default Bson executeRemove(Bson filter) {
        return filter;
    }

    /**
     * 修改拦截方法
     * @param updatePairList 值 left=查询条件 right=更新条件
     * @return {@link List<MutablePair>}
     * @author anwen
     */
    @Deprecated
    default List<MutablePair<Bson,Bson>> executeUpdate(List<MutablePair<Bson,Bson>> updatePairList){
        return updatePairList;
    }

    /**
     * 查询拦截方法
     * @param queryBasic 条件
     * @param projectionList 显隐字段
     * @param sortCond 排序
     * @return {@link QueryParam}
     * @author anwen
     */
    @Deprecated
    default QueryParam executeQuery(Bson queryBasic, BasicDBObject projectionList, BasicDBObject sortCond) {
        return new QueryParam(queryBasic, projectionList, sortCond);
    }

    /**
     * 管道拦截方法
     * @param aggregateConditionList 管道对象
     * @return {@link List}
     * @author anwen
     */
    @Deprecated
    default List<Bson> executeAggregates(List<Bson> aggregateConditionList) {
        return aggregateConditionList;
    }

    /**
     * 统计拦截方法
     * @param queryBasic 条件
     * @param countOptions 选项
     * @return {@link MutablePair} left = 条件 right = 选项
     * @author anwen
     */
    @Deprecated
    default MutablePair<BasicDBObject, CountOptions> executeCount(BasicDBObject queryBasic,
                                                                  CountOptions countOptions) {
        return new MutablePair<>(queryBasic, countOptions);
    }

    /**
     * 批量操作拦截方法
     * @param writeModelList 操作对象，{@link InsertOneModel}或{@link UpdateManyModel}
     * @return {@link List}
     * @author anwen
     */
    @Deprecated
    default List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList) {
        return writeModelList;
    }

    /**
     * 添加拦截方法
     * @param documentList 添加的值
     * @return {@link List<org.bson.Document>}
     * @author anwen
     */
    default List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection) {
        return documentList;
    }

    /**
     * 添加拦截方法
     * @param document 添加的值
     * @return {@link List<org.bson.Document>}
     * @author anwen
     */
    default Document executeSave(Document document, MongoCollection<Document> collection) {
        return document;
    }

    /**
     * 删除拦截方法
     *
     * @author JiaChaoYang
     */
    default Bson executeRemove(Bson filter, MongoCollection<Document> collection) {
        return filter;
    }

    /**
     * 删除拦截方法
     *
     * @author JiaChaoYang
     */
    default Bson executeRemoveOne(Bson filter, MongoCollection<Document> collection) {
        return filter;
    }

    /**
     * 修改拦截方法
     * @param updatePairList 值 left=查询条件 right=更新条件
     * @return {@link java.util.List}
     * @author anwen
     */
    default List<MutablePair<Bson,Bson>> executeUpdate(List<MutablePair<Bson,Bson>> updatePairList,
                                                       MongoCollection<Document> collection){
        return updatePairList;
    }

    /**
     * 修改拦截方法
     * @param updatePair 值 left=查询条件 right=更新条件
     * @return {@link java.util.List}
     * @author anwen
     */
    default MutablePair<Bson,Bson> executeUpdate(MutablePair<Bson,Bson> updatePair,
                                                       MongoCollection<Document> collection){
        return updatePair;
    }

    /**
     * 查询拦截方法
     *
     * @author JiaChaoYang
     */
    default QueryParam executeQuery(Bson queryBasic, BasicDBObject projectionList, BasicDBObject sortCond,
                                    MongoCollection<Document> collection) {
        return new QueryParam(queryBasic, projectionList, sortCond);
    }

    /**
     * 管道拦截方法
     *
     * @author JiaChaoYang
     */
    default List<Bson> executeAggregates(List<Bson> aggregateConditionList, MongoCollection<Document> collection) {
        return aggregateConditionList;
    }

    /**
     * 统计拦截方法
     *
     * @author JiaChaoYang
     */
    default MutablePair<BasicDBObject, CountOptions> executeCount(BasicDBObject queryBasic,
                                                                  CountOptions countOptions,
                                                                  MongoCollection<Document> collection) {
        return new MutablePair<>(queryBasic, countOptions);
    }

    /**
     * 不接受任何参数的统计
     * @param collection 集合
     * @author anwen
     */
    default void executeEstimatedDocumentCount(MongoCollection<Document> collection){

    }

    /**
     * 批量操作拦截方法
     *
     * @author JiaChaoYang
     */
    default List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                                        MongoCollection<Document> collection) {
        return writeModelList;
    }

}
