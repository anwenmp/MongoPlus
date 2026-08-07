package com.mongoplus.interceptor.business;

import com.mongodb.client.MongoCollection;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.model.MutablePair;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertSame;

public class CollectionLogiceInterceptorIgnoreUpdateTest {

    private final CollectionLogiceInterceptor interceptor = new CollectionLogiceInterceptor();
    private final Boolean originalOpen = LogicManager.open;
    private final MongoCollection<Document> unusedCollection = unusedCollection();

    @After
    public void restoreLogicContext() {
        LogicManager.restoreLogicCondition();
        LogicManager.open = originalOpen;
    }

    @Test
    public void ignoreLogicKeepsSingleUpdateFilterUnchanged() {
        MutablePair<Bson, Bson> pair = pair("first");
        Bson originalFilter = pair.getLeft();
        LogicManager.open = true;
        LogicManager.ignoreLogicCondition();

        MutablePair<Bson, Bson> result = interceptor.executeUpdate(pair, unusedCollection);

        assertSame(pair, result);
        assertSame(originalFilter, result.getLeft());
    }

    @Test
    public void ignoreLogicKeepsEveryMultiUpdateFilterUnchanged() {
        MutablePair<Bson, Bson> first = pair("first");
        MutablePair<Bson, Bson> second = pair("second");
        Bson firstFilter = first.getLeft();
        Bson secondFilter = second.getLeft();
        List<MutablePair<Bson, Bson>> pairs = Arrays.asList(first, second);
        LogicManager.open = true;
        LogicManager.ignoreLogicCondition();

        List<MutablePair<Bson, Bson>> result = interceptor.executeUpdate(pairs, unusedCollection);

        assertSame(pairs, result);
        assertSame(firstFilter, first.getLeft());
        assertSame(secondFilter, second.getLeft());
    }

    private MutablePair<Bson, Bson> pair(String id) {
        return new MutablePair<Bson, Bson>(
                new Document("_id", id),
                new Document("$set", new Document("name", id))
        );
    }

    @SuppressWarnings("unchecked")
    private MongoCollection<Document> unusedCollection() {
        return (MongoCollection<Document>) Proxy.newProxyInstance(
                MongoCollection.class.getClassLoader(),
                new Class<?>[]{MongoCollection.class},
                (proxy, method, args) -> {
                    throw new AssertionError("Ignored update must not inspect collection metadata");
                }
        );
    }
}
