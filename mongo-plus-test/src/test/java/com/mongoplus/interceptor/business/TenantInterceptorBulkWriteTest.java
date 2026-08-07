package com.mongoplus.interceptor.business;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.handlers.TenantHandler;
import com.mongoplus.toolkit.BsonUtil;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class TenantInterceptorBulkWriteTest {

    private final TenantInterceptor interceptor = new TenantInterceptor(new TenantHandler() {
        @Override
        public BsonString getTenantId() {
            return new BsonString("tenant-a");
        }
    });
    private final MongoCollection<Document> collection = collection();

    @Test
    public void rebuildsUpdateManyAndPreservesOrderUpdateAndOptions() {
        InsertOneModel<Document> insert = new InsertOneModel<>(new Document("name", "insert"));
        UpdateOptions options = new UpdateOptions().upsert(true);
        Bson update = Updates.set("name", "updated");
        UpdateManyModel<Document> original = new UpdateManyModel<>(eq("status", "OPEN"), update, options);
        List<WriteModel<Document>> models = Arrays.<WriteModel<Document>>asList(insert, original);

        List<WriteModel<Document>> result = interceptor.executeBulkWrite(models, collection);

        assertSame(insert, result.get(0));
        UpdateManyModel<Document> rebuilt = (UpdateManyModel<Document>) result.get(1);
        assertNotSame(original, rebuilt);
        assertSame(update, rebuilt.getUpdate());
        assertSame(options, rebuilt.getOptions());
        assertTenantFilter(rebuilt.getFilter());
    }

    @Test
    public void rebuildsPipelineUpdateManyAndPreservesPipelineAndOptions() {
        List<Bson> pipeline = Arrays.<Bson>asList(new Document("$set", new Document("name", "updated")));
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateManyModel<Document> original = new UpdateManyModel<>(eq("status", "OPEN"), pipeline, options);

        List<WriteModel<Document>> result = interceptor.executeBulkWrite(
                Arrays.<WriteModel<Document>>asList(original), collection);

        UpdateManyModel<Document> rebuilt = (UpdateManyModel<Document>) result.get(0);
        assertNotSame(original, rebuilt);
        assertSame(pipeline, rebuilt.getUpdatePipeline());
        assertSame(options, rebuilt.getOptions());
        assertTenantFilter(rebuilt.getFilter());
    }

    private void assertTenantFilter(Bson filter) {
        BsonDocument document = BsonUtil.asBsonDocument(filter);
        assertEquals(new BsonString("tenant-a"), document.get("tenant_id"));
    }

    @SuppressWarnings("unchecked")
    private MongoCollection<Document> collection() {
        return (MongoCollection<Document>) Proxy.newProxyInstance(
                MongoCollection.class.getClassLoader(),
                new Class<?>[]{MongoCollection.class},
                (proxy, method, args) -> {
                    if ("getNamespace".equals(method.getName())) {
                        return new MongoNamespace("tenant_test.items");
                    }
                    throw new AssertionError("Unexpected collection method: " + method.getName());
                }
        );
    }
}
