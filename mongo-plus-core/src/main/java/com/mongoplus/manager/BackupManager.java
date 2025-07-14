package com.mongoplus.manager;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.execute.Execute;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.listener.BackupListener;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.meta.MongoPlusVersion;
import com.mongoplus.toolkit.Assert;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.toolkit.StringUtils;
import com.mongoplus.toolkit.ZipUtil;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.Document;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

/**
 * 备份管理器
 * @author anwen
 */
public class BackupManager {

    private final Log log = LogFactory.getLog(BackupManager.class);

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DateTimeFormatter nameFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ExecutorFactory factory = new ExecutorFactory();

    /**
     * 备份文件存储路径
     */
    private String path;

    /**
     * 需要备份的集合名称
     */
    private final List<String> collectionNames;

    /**
     * 数据源名称，默认当前数据源
     */
    private String dataSourceName;

    /**
     * mongoPlusClient
     */
    private MongoPlusClient mongoPlusClient;

    /**
     * 每批次导出的数量，默认1000
     */
    private Integer limit = 1000;

    /**
     * 备份监听器
     */
    private final List<BackupListener> backupListeners = new ArrayList<>();

    /**
     * 创建一个BackupManager实例
     * @author anwen
     */
    public BackupManager(String path,List<String> collectionNames,MongoPlusClient mongoPlusClient){
        String finalPath = path;
        if (StringUtils.isNotBlank(finalPath) && !path.endsWith("/")){
            finalPath = path+"/";
        }
        this.path = finalPath;
        this.collectionNames = collectionNames;
        this.dataSourceName = DataSourceNameCache.getDataSource();
        this.mongoPlusClient = mongoPlusClient;
    }

    /**
     * 创建一个BackupManager实例，不包含collectionNames
     */
    public BackupManager(String path,MongoPlusClient mongoPlusClient){
        this(path,new ArrayList<>(),mongoPlusClient);
    }

    /**
     * 创建一个BackupManager实例，只包含mongoPlusClient
     * @param mongoPlusClient mongoPlusClient
     * @author anwen
     */
    public BackupManager(MongoPlusClient mongoPlusClient){
        this(null,new ArrayList<>(),mongoPlusClient);
    }

    /**
     * 设置集合名称
     * @param collectionNames class集合
     * @author anwen
     */
    public void setCollectionNames(List<Class<?>> collectionNames){
        this.collectionNames.addAll(collectionNames.stream()
                .map(AnnotationOperate::getCollectionName)
                .collect(Collectors.toList()));
    }

    /**
     * 设置集合名称
     * @param collectionNames 集合名称
     * @author anwen
     */
    public void setCollectionNamesStr(List<String> collectionNames) {
        this.collectionNames.addAll(collectionNames);
    }

    /**
     * 设置存储路径
     * @author anwen
     */
    public void setPath(String path){
        this.path = path;
    }

    /**
     * 导出备份
     * @author anwen
     */
    public Map<String,String> export(){
        Assert.hasLength(path,"'path' is null");
        Assert.isTrue(CollUtil.isNotEmpty(collectionNames),"'collectionNames' is null");
        HashMap<String, String> resultMap = new HashMap<>();
        this.collectionNames.forEach(collectionName -> {
            MongoCollection<Document> collection = mongoPlusClient.getCollection(
                    this.dataSourceName,
                    DataSourceNameCache.getDatabase(this.dataSourceName),
                    collectionName
            );
            String path = backupCollectionToJSON(collection);
            resultMap.put(collectionName,path);
            log.info(collectionName+" -> "+path);
        });
        return resultMap;
    }

    /**
     * 导入备份数据
     * @author anwen
     */
    public void imports(String path) {
        try(ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(path)))){
            int num = 0;
            while (zipInputStream.getNextEntry() != null) {
                // 读取文件内容
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                // 获取文件的内容
                String content = byteArrayOutputStream.toString("UTF-8");
                BsonArray bsonArray = BsonArray.parse(content);
                BsonDocument document = bsonArray.get(0).asDocument();
                BsonDocument information = document.get("information").asDocument();
                if (information == null){
                    throw new MongoPlusException("Unable to parse this file, it may not have been generated through MongoPlus");
                }
                String dataSource = information.getString("data_source").getValue();
                String database = information.getString("database").getValue();
                bsonArray.remove(0);
                String collectionName = information.getString("collection_name").getValue();
                MongoCollection<Document> collection = mongoPlusClient.getCollection(dataSourceName, database, collectionName);
                if (num <= 0){
                    collection.drop();
                }
                // 使用执行器工厂获取执行器，通过执行器执行，方便事务控制
                Execute execute = factory.getExecute();
                List<Document> documentList = bsonArray.stream()
                        .map(bd -> new Document(bd.asDocument()))
                        .collect(Collectors.toList());
                execute.executeSave(documentList,null,collection);
                // 关闭当前条目
                zipInputStream.closeEntry();
                num++;
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * 获取文件名称
     * @param collectionName 集合名称
     * @return {@link String}
     * @author anwen
     */
    public String getFileName(String collectionName){
        return collectionName+"-"+System.currentTimeMillis()+".json";
    }

    String backupCollectionToJSON(MongoCollection<Document> collection) {
        MongoNamespace namespace = collection.getNamespace();
        String collectionName = namespace.getCollectionName();
        long totalDocuments = collection.estimatedDocumentCount();
        log.info("Collection document count: " + totalDocuments);

        if (totalDocuments <= 0) {
            log.error("Collection is empty");
            return null;
        }

        int count = 0, skip = 0, num = 0;
        boolean hasMore = true;

        String filePath = path + collectionName;
        File dir = new File(filePath);
        if (!dir.exists() && dir.mkdirs()) {
            log.info("Directory created: " + filePath);
        }

        // 开始备份
        while (hasMore) {
            num++;
            String finalPath = String.format("%s/%d-%s", filePath, num, getFileName(collectionName));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(finalPath))) {
                writer.write("[\n");
                writeCollectionInfo(namespace, writer);

                try (MongoCursor<Document> cursor = collection
                        .find()
                        .skip(skip * limit)
                        .limit(limit)
                        .batchSize(limit)
                        .iterator()) {

                    if (!cursor.hasNext()) {
                        hasMore = false;
                        continue;
                    }

                    // 逐条处理 Mongo 数据
                    while (cursor.hasNext()) {
                        count++;
                        Document document = cursor.next();
                        if (CollUtil.isNotEmpty(backupListeners)) {
                            backupListeners.forEach(backupListener ->
                                    backupListener.export(finalPath,collectionName,document));
                        }
                        writer.write(document.toJson(MapCodecCache.getDefaultCodec()));

                        // 不是最后一条数据时加逗号
                        if (count < totalDocuments) {
                            writer.write(",\n");
                        }
                        hasMore = count < totalDocuments;
                    }
                }

                // 结束 JSON 数组
                writer.write("\n]");

                log.info("Backup for collection '" + collectionName + "' batch " + num + " successful.");
            } catch (IOException e) {
                log.error("Backup failed for collection: " + collectionName, e);
                break;
            }
            // 更新分页，跳到下一批次
            skip++;
        }

        // 执行压缩和删除操作
        String zipFilePath = path + collectionName + "-" + currentDateTime(nameFormatter) + ".zip";
        ZipUtil.zipDirectory(filePath, zipFilePath);
        log.info("Zipping completed successfully: " + zipFilePath);

        // 删除临时文件
        deleteFiles(filePath);
        log.info("Temporary files deleted.");
        return zipFilePath;
    }


    @SuppressWarnings("all")
    void deleteFiles(String filePath){
        // 使用 walkFileTree 递归删除文件夹及其内容
        try {
            Files.walkFileTree(Paths.get(filePath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);  // 删除文件
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);  // 删除目录
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeCollectionInfo(MongoNamespace namespace,BufferedWriter writer) throws IOException {
        Document collectionDocument = new Document();
        collectionDocument.put("origin","MongoPlus");
        collectionDocument.put("version","v"+ MongoPlusVersion.getVersion());
        collectionDocument.put("homepage","https://www.mongoplus.com/");
        collectionDocument.put("data_source",this.dataSourceName);
        collectionDocument.put("database",namespace.getDatabaseName());
        collectionDocument.put("collection_name",namespace.getCollectionName());
        collectionDocument.put("date_time", currentDateTime());
        collectionDocument.put("time_stamp", System.currentTimeMillis());
        writer.write(new Document("information",collectionDocument).toJson(MapCodecCache.getDefaultCodec()));
        writer.write(",\n");
    }

    String currentDateTime(){
        return LocalDateTime.now().format(formatter);
    }

    String currentDateTime(DateTimeFormatter formatter){
        return LocalDateTime.now().format(formatter);
    }

    public String getPath() {
        return path;
    }

    public List<String> getCollectionNames() {
        return collectionNames;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public MongoPlusClient getMongoPlusClient() {
        return mongoPlusClient;
    }

    public void setMongoPlusClient(MongoPlusClient mongoPlusClient) {
        this.mongoPlusClient = mongoPlusClient;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * 设置监听器
     * @param backupListeners 监听器
     * @author anwen
     */
    public void setBackupListeners(BackupListener... backupListeners){
        this.backupListeners.addAll(Arrays.asList(backupListeners));
    }


    static class DocumentArray extends ArrayList<Document> {

        public String toJson(){
            Iterator<Document> it = iterator();
            if (!it.hasNext())
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (;;) {
                Document document = it.next();
                if (document != null){
                    sb.append(document.toJson());
                }
                if (!it.hasNext())
                    return sb.append(']').toString();
                sb.append(',').append(' ');
            }
        }

        @Override
        public String toString(){
            return toJson();
        }
    }


}