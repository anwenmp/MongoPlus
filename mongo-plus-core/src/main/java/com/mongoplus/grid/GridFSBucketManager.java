package com.mongoplus.grid;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongoplus.cache.global.MongoPlusClientCache;
import com.mongoplus.manager.MongoPlusClient;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author anwen
 */
public class GridFSBucketManager {

    private GridFSBucket gridFSBucket;

    /*public GridFSManager(MongoPlusClient mongoPlusClient) {
        this.mongoPlusClient = mongoPlusClient;
    }*/

    private MongoPlusClient mongoPlusClient;

    protected GridFSBucketManager(GridFSBucket gridFSBucket) {
        this.gridFSBucket = gridFSBucket;
    }

    public static GridFSBucketManager create(String database) {
        return create(database,"fs");
    }

    public static GridFSBucketManager create(String database,String bucketName) {
        MongoDatabase mongoDatabase = MongoPlusClientCache.mongoPlusClient.getMongoDatabase(database);
        return new GridFSBucketManager(GridFSBuckets.create(mongoDatabase,bucketName));
    }

    public ObjectId upload(String filePath,String filename) throws IOException {
        return upload(filePath,filename,getEmptyOptions());
    }

    public ObjectId upload(String filePath,String filename,GridFSUploadOptions options) throws IOException {
        return upload(Files.newInputStream(Paths.get(filePath)),filePath,options);
    }

    public ObjectId upload(InputStream inputStream,String filename) throws IOException {
        return upload(inputStream,filename,getEmptyOptions());
    }

    public ObjectId upload(InputStream inputStream,String filename,GridFSUploadOptions options) throws IOException {
        ObjectId objectId = gridFSBucket.uploadFromStream(filename, inputStream,options);
        inputStream.close();
        return objectId;
    }

    public ObjectId upload(byte[] data,String filename) {
        return upload(data,filename,getEmptyOptions());
    }

    public ObjectId upload(byte[] data,String filename,GridFSUploadOptions options) {
        try (GridFSUploadStream stream = gridFSBucket.openUploadStream(filename,options)) {
            stream.write(data);
            stream.flush();
            return stream.getObjectId();
        }
    }

    GridFSUploadOptions getEmptyOptions(){
        return new GridFSUploadOptions();
    }

    public GridFSBucket getGridFSBucket() {
        return gridFSBucket;
    }

    public void setGridFSBucket(GridFSBucket gridFSBucket) {
        this.gridFSBucket = gridFSBucket;
    }

    public MongoPlusClient getMongoPlusClient() {
        return mongoPlusClient;
    }

    public void setMongoPlusClient(MongoPlusClient mongoPlusClient) {
        this.mongoPlusClient = mongoPlusClient;
    }

}
