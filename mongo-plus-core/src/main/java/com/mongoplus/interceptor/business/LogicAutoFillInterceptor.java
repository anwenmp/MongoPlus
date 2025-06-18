package com.mongoplus.interceptor.business;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.logic.LogicDeleteHandler;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.model.LogicDeleteResult;
import org.bson.Document;

import java.util.List;
import java.util.Objects;

/**
 * 逻辑删除默认字段拦截器(初始化逻辑未删除字段、建议方案：使用数据库默认字段 > 其次是手动设置 > 配置框架提供拦截器 > 自定义拦截器）)
 *
 * @author loser
 */
public class LogicAutoFillInterceptor implements Interceptor {

    @Override
    public List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection) {

        if (LogicManager.isIgnoreLogic()) {
            return documentList;
        }
        Class<?> clazz = LogicDeleteHandler.getBeanClass(collection);
        if (Objects.isNull(clazz)) {
            return documentList;
        }
        LogicDeleteResult result = LogicDeleteHandler.mapper().get(clazz);
        if (Objects.nonNull(result)) {
            for (Document document : documentList) {
                if (!document.containsKey(result.getColumn()) || document.get(result.getColumn()) == null) {
                    document.put(result.getColumn(), result.getLogicNotDeleteValue());
                }
            }
        }
        return documentList;

    }

    @Override
    public Document executeSave(Document document, MongoCollection<Document> collection) {

        if (LogicManager.isIgnoreLogic()) {
            return document;
        }
        Class<?> clazz = LogicDeleteHandler.getBeanClass(collection);
        if (Objects.isNull(clazz)) {
            return document;
        }
        LogicDeleteResult result = LogicDeleteHandler.mapper().get(clazz);
        if (Objects.nonNull(result)) {
            if (!document.containsKey(result.getColumn()) || document.get(result.getColumn()) == null) {
                document.put(result.getColumn(), result.getLogicNotDeleteValue());
            }
        }
        return document;

    }

    @Override
    public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList, MongoCollection<Document> collection) {

        if (LogicManager.isIgnoreLogic()) {
            return writeModelList;
        }
        Class<?> clazz = LogicDeleteHandler.getBeanClass(collection);
        if (Objects.isNull(clazz)) {
            return writeModelList;
        }
        for (WriteModel<Document> documentWriteModel : writeModelList) {
            if (documentWriteModel instanceof InsertOneModel) {
                Document document = ((InsertOneModel<Document>) documentWriteModel).getDocument();
                LogicDeleteResult result = LogicDeleteHandler.mapper().get(clazz);
                if (Objects.nonNull(result)) {
                    if (!document.containsKey(result.getColumn()) || document.get(result.getColumn()) == null) {
                        document.put(result.getColumn(), result.getLogicNotDeleteValue());
                    }
                }
            }
        }
        return writeModelList;

    }

}
