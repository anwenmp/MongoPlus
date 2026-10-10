package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.annotation.collection.CollectionName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

/** 验证正式 out overload 的单次追加、精确 BSON 和独立的结果消费标志，不连接数据库。 */
public class OutStageEffectTest {
    @Test
    public void collectionNameAppendsOneStringStage() {
        assertEffect(receiver -> receiver.out("orders"), "{$out:'orders'}");
    }

    @Test
    public void annotatedClassResolvesCollectionBeforeAppending() {
        assertEffect(receiver -> receiver.out(Output.class), "{$out:'archived_orders'}");
    }

    @Test
    public void databaseAndCollectionPreserveDocumentFieldOrder() {
        Aggregate<?> receiver = assertEffect(receiverValue -> receiverValue.out("archive", "orders"),
                "{$out:{db:'archive',coll:'orders'}}");
        Assert.assertEquals(Arrays.asList("db", "coll"),
                new ArrayList<>(bson(receiver, 1).getDocument("$out").keySet()));
    }

    @Test
    public void bsonOverloadSetsFlagButPassesInputThroughWithoutWrapping() {
        Aggregate<?> receiver = new AggregateWrapper();
        Bson input = new Document("$limit", 7);
        Assert.assertSame(receiver, receiver.out(input));
        Assert.assertSame(input, receiver.getAggregateConditionList().get(0));
        Assert.assertEquals(BsonDocument.parse("{$limit:7}"), bson(receiver, 0));
        Assert.assertTrue(receiver.isSkip());
        // 仅透传任意 BSON，不能因方法名 out 就把该入口标为固定 $out Stage。
        Aggregate<?> customReceiver = new AggregateWrapper();
        customReceiver.custom(new Document("$out", "orders"));
        Assert.assertFalse(customReceiver.isSkip());
    }

    @Test
    public void flagDoesNotFilterReorderOrPreventLaterCalls() {
        Aggregate<?> receiver = new AggregateWrapper();
        List<Bson> liveView = receiver.getAggregateConditionList();
        receiver.limit(1);
        receiver.out("first");
        receiver.count("after");
        receiver.out("archive", "second");
        Assert.assertSame(liveView, receiver.getAggregateConditionList());
        Assert.assertEquals(4, liveView.size());
        Assert.assertEquals(BsonDocument.parse("{$limit:1}"), bson(receiver, 0));
        Assert.assertEquals(BsonDocument.parse("{$out:'first'}"), bson(receiver, 1));
        Assert.assertEquals(BsonDocument.parse("{$count:'after'}"), bson(receiver, 2));
        Assert.assertEquals(BsonDocument.parse("{$out:{db:'archive',coll:'second'}}"), bson(receiver, 3));
        Assert.assertTrue(receiver.isSkip());
        // 此处只固定 Core 构造顺序，不将这个序列声明为服务器合法管道。
    }

    @Test
    public void failedStringConstructionDoesNotSetFlagOrAppend() {
        Aggregate<?> receiver = new AggregateWrapper();
        receiver.limit(1);
        try {
            receiver.out((String) null);
            Assert.fail("Driver BsonString 必须保留 null 参数异常");
        } catch (IllegalArgumentException expected) {
            Assert.assertFalse(receiver.isSkip());
            Assert.assertEquals(1, receiver.getAggregateConditionList().size());
            Assert.assertEquals(BsonDocument.parse("{$limit:1}"), bson(receiver, 0));
        }
    }

    private static Aggregate<?> assertEffect(Function<Aggregate<?>, Object> call, String expected) {
        Aggregate<?> receiver = new AggregateWrapper();
        Aggregate<?> sibling = new AggregateWrapper();
        Assert.assertFalse(receiver.isSkip());
        receiver.limit(1);
        Assert.assertSame(receiver, call.apply(receiver));
        Assert.assertEquals(2, receiver.getAggregateConditionList().size());
        Assert.assertEquals(BsonDocument.parse("{$limit:1}"), bson(receiver, 0));
        Assert.assertEquals(BsonDocument.parse(expected), bson(receiver, 1));
        Assert.assertTrue(receiver.isSkip());
        Assert.assertTrue(sibling.getAggregateConditionList().isEmpty());
        Assert.assertFalse(sibling.isSkip());
        return receiver;
    }

    private static BsonDocument bson(Aggregate<?> receiver, int index) {
        return receiver.getAggregateConditionList().get(index)
                .toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
    }

    @CollectionName("archived_orders")
    private static class Output { }
}
