package com.mongoplus.listener;

import org.bson.Document;

public interface BackupListener {

    /**
     * 导出
     * @param path 导出路径
     * @param collectionName 集合名称
     * @param document document
     * @author anwen
     */
    void export(String path, String collectionName, Document document);

}
