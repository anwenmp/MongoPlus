package com.mongoplus.toolkit;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.TimeSeriesOptions;
import com.mongoplus.annotation.collection.TimeSeries;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import org.bson.Document;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mongoplus.toolkit.ClassTypeUtil.getFieldNameAndCheck;

/**
 * 自动操作
 * @author anwen
 */
public class AutoUtil {

    private static final Log log = LogFactory.getLog(AutoUtil.class);

    public static void autoCreateTimeSeries(Collection<Class<?>> classCollection, MongoPlusClient mongoPlusClient){
        if (CollUtil.isEmpty(classCollection)) {
            return;
        }
        classCollection.forEach(collectionClass -> {
            TimeSeries timeSeries = collectionClass.getAnnotation(TimeSeries.class);
            String dataSource = DataSourceNameCache.getDataSource();
            if (StringUtils.isNotBlank(timeSeries.dataSource())){
                dataSource = timeSeries.dataSource();
            }
            MongoClient mongoClient = mongoPlusClient.getMongoClient(dataSource);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(mongoPlusClient.getDatabase(collectionClass));
            Document paramDocument = new Document();
            paramDocument.put("listCollections",1);
            paramDocument.put("filter",new Document("type","timeseries"));
            Document document = mongoDatabase.runCommand(paramDocument);
            List<String> timeSeriesList = document.get("cursor", Document.class)
                    .getList("firstBatch", Document.class)
                    .stream().map(doc -> doc.getString("name"))
                    .collect(Collectors.toList());
            String collectionName = AnnotationOperate.getCollectionName(collectionClass);
            if (timeSeriesList.contains(collectionName)){
                log.warn("The {} temporal collection already exists",collectionName);
                return;
            }
            TypeInformation typeInformation = TypeInformation.of(collectionClass);
            TimeSeriesOptions options = new TimeSeriesOptions(getFieldNameAndCheck(typeInformation,timeSeries.timeField()));
            options.granularity(timeSeries.granularity());
            if (StringUtils.isNotBlank(timeSeries.metaField())){
                options.metaField(getFieldNameAndCheck(typeInformation,timeSeries.metaField()));
            }
            if (timeSeries.bucketMaxSpan() > 0){
                options.bucketMaxSpan(timeSeries.bucketMaxSpan(), TimeUnit.SECONDS);
                options.metaField(null);
            }
            if (timeSeries.bucketRounding() > 0){
                options.bucketRounding(timeSeries.bucketRounding(), TimeUnit.SECONDS);
                options.metaField(null);
            }
            CreateCollectionOptions createCollectionOptions = new CreateCollectionOptions();
            createCollectionOptions.timeSeriesOptions(options);
            if (timeSeries.expireAfter() > 0){
                createCollectionOptions.expireAfter(timeSeries.expireAfter(), TimeUnit.SECONDS);
            }
            try {
                mongoDatabase.createCollection(
                        collectionName,
                        createCollectionOptions);
            } catch (MongoCommandException ignored){}
        });
    }

    public static void autoCreateIndexes(Collection<Class<?>> classCollection, MongoPlusClient mongoPlusClient){
        if (CollUtil.isEmpty(classCollection)) {
            return;
        }
        List<IndexMetaObject> indexMetaObjectList = IndexUtil.getIndex(classCollection);
        if (CollUtil.isNotEmpty(indexMetaObjectList)) {
            indexMetaObjectList.forEach(indexMetaObject -> {
                if (CollUtil.isNotEmpty(indexMetaObject.getIndexModels())){
                    String dataSource = DataSourceNameCache.getDataSource();
                    if (StringUtils.isNotBlank(indexMetaObject.getDataSource())){
                        dataSource = indexMetaObject.getDataSource();
                    }
                    Class<?> clazz = indexMetaObject.getTypeInformation().getClazz();
                    MongoCollection<Document> collection = mongoPlusClient.getCollectionManager(dataSource,clazz)
                            .getCollection(clazz);
                    collection.createIndexes(indexMetaObject.getIndexModels());
                }
            });
        }
    }

}
