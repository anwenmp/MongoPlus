package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.aggregate.pipeline.Facet;
import com.mongoplus.aggregate.pipeline.Sorts;
import java.util.ArrayList;
import java.util.Arrays;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

/** 通过 Aggregate<?> 验证排序 body 的一次包装、receiver effect 和真实嵌套 BSON。 */
public class SortSpecificationStageTest {
    @Test
    public void singleFieldAndSameDirectionBodiesAppendOneCompleteStage() {
        assertStage(Sorts.asc("score"), "{score:1}", "score");
        assertStage(Sorts.desc("createTime"), "{createTime:-1}", "createTime");
        assertStage(Sorts.asc("score", "id"), "{score:1,id:1}", "score", "id");
        assertStage(Sorts.desc(Arrays.asList("createTime", "id")),
                "{createTime:-1,id:-1}", "createTime", "id");
    }

    @Test
    public void mixedDirectionsPreserveOrderAndInt32InBothReducers() {
        Bson[] parts = {Sorts.desc("createTime"), Sorts.asc("score"), Sorts.desc("id")};
        for (Bson body : Arrays.asList(Sorts.orderBy(parts), Sorts.orderBy(Arrays.asList(parts)))) {
            assertStage(body, "{createTime:-1,score:1,id:-1}", "createTime", "score", "id");
        }
    }

    @Test
    public void defaultMethodDelegatesOnceToTheExistingCustomHook() throws Exception {
        RecordingReceiver receiver = new RecordingReceiver();
        Aggregate<?> aggregate = receiver;
        Assert.assertTrue(Aggregate.class.getMethod("sortSpecification", Bson.class).isDefault());
        Assert.assertSame(receiver, aggregate.sortSpecification(mixedBody()));
        Assert.assertEquals(1, receiver.appendCalls);
        Assert.assertEquals(1, receiver.getAggregateConditionList().size());
        assertSort(encode(receiver.getAggregateCondition()),
                "{createTime:-1,score:1,id:-1}", "createTime", "score", "id");
    }

    @Test
    public void nullSpecificationFailsBeforeReceiverMutation() {
        Aggregate<?> aggregate = new AggregateWrapper();
        aggregate.limit(1);
        try {
            aggregate.sortSpecification(null);
            Assert.fail("null body 必须在追加前拒绝");
        } catch (NullPointerException expected) {
            Assert.assertEquals("specification", expected.getMessage());
            Assert.assertEquals(1, aggregate.getAggregateConditionList().size());
            Assert.assertEquals(BsonDocument.parse("{$limit:1}"), encode(aggregate.getAggregateCondition()));
        }
    }

    @Test
    public void legacySortBsonStillPassesTheExactObjectThrough() {
        Aggregate<?> aggregate = new AggregateWrapper();
        Bson body = mixedBody();
        Assert.assertSame(aggregate, aggregate.sort(body));
        Assert.assertSame(body, aggregate.getAggregateCondition());
        Assert.assertFalse(encode(aggregate.getAggregateCondition()).containsKey("$sort"));
        Bson stage = com.mongodb.client.model.Aggregates.sort(body);
        Assert.assertSame(aggregate, aggregate.sort(stage));
        Assert.assertSame(stage, aggregate.getAggregateCondition(1));
        assertSort(encode(aggregate.getAggregateCondition(1)),
                "{createTime:-1,score:1,id:-1}", "createTime", "score", "id");
    }

    @Test
    public void facetKeepsSortOnTheInnerReceiverAndPreservesBranchOrder() {
        Aggregate<?> inner = inner();
        Aggregate<?> sibling = new AggregateWrapper();
        sibling.count("otherCount");
        Aggregate<?> outer = new AggregateWrapper();
        outer.facet(new Facet("rows", inner), new Facet("other", sibling));
        BsonDocument facet = encode(outer.getAggregateCondition()).getDocument("$facet");
        Assert.assertEquals(Arrays.asList("rows", "other"), new ArrayList<>(facet.keySet()));
        assertPipeline(facet.getArray("rows"), inner);
        Assert.assertEquals(BsonDocument.parse("{$count:'otherCount'}"),
                facet.getArray("other").get(0).asDocument());
        Assert.assertEquals(1, sibling.getAggregateConditionList().size());
        Assert.assertEquals(1, outer.getAggregateConditionList().size());
        Assert.assertNotSame(inner.getAggregateConditionList(), sibling.getAggregateConditionList());
    }

    @Test
    public void lookupKeepsOneSortBetweenTheInnerStages() {
        Aggregate<?> inner = inner();
        Aggregate<?> outer = new AggregateWrapper();
        outer.lookup("orders", inner, "rows");
        BsonDocument lookup = encode(outer.getAggregateCondition()).getDocument("$lookup");
        Assert.assertEquals("orders", lookup.getString("from").getValue());
        Assert.assertEquals("rows", lookup.getString("as").getValue());
        assertPipeline(lookup.getArray("pipeline"), inner);
        Assert.assertEquals(1, outer.getAggregateConditionList().size());
        Assert.assertNotSame(inner.getAggregateConditionList(), outer.getAggregateConditionList());
    }

    @Test
    public void unionWithKeepsOneSortBetweenTheInnerStages() {
        Aggregate<?> inner = inner();
        Aggregate<?> outer = new AggregateWrapper();
        outer.unionWith("history", inner);
        BsonDocument union = encode(outer.getAggregateCondition()).getDocument("$unionWith");
        Assert.assertEquals("history", union.getString("coll").getValue());
        assertPipeline(union.getArray("pipeline"), inner);
        Assert.assertEquals(1, outer.getAggregateConditionList().size());
        Assert.assertNotSame(inner.getAggregateConditionList(), outer.getAggregateConditionList());
    }

    private static void assertStage(Bson body, String expected, String... keys) {
        BsonDocument before = encode(body).clone();
        Aggregate<?> aggregate = new AggregateWrapper();
        Aggregate<?> sibling = new AggregateWrapper();
        aggregate.limit(1);
        Assert.assertSame(aggregate, aggregate.sortSpecification(body));
        aggregate.limit(2);
        Assert.assertEquals(3, aggregate.getAggregateConditionList().size());
        Assert.assertEquals(BsonDocument.parse("{$limit:1}"), encode(aggregate.getAggregateCondition(0)));
        assertSort(encode(aggregate.getAggregateCondition(1)), expected, keys);
        Assert.assertEquals(BsonDocument.parse("{$limit:2}"), encode(aggregate.getAggregateCondition(2)));
        Assert.assertEquals(before, encode(body));
        Assert.assertTrue(sibling.getAggregateConditionList().isEmpty());
    }

    private static Aggregate<?> inner() {
        Aggregate<?> aggregate = new AggregateWrapper();
        aggregate.limit(1);
        aggregate.sortSpecification(mixedBody());
        aggregate.limit(2);
        return aggregate;
    }

    private static Bson mixedBody() {
        return Sorts.orderBy(Sorts.desc("createTime"), Sorts.asc("score"), Sorts.desc("id"));
    }

    private static void assertPipeline(BsonArray pipeline, Aggregate<?> inner) {
        Assert.assertEquals(3, pipeline.size());
        Assert.assertEquals(3, inner.getAggregateConditionList().size());
        Assert.assertEquals(BsonDocument.parse("{$limit:1}"), pipeline.get(0).asDocument());
        assertSort(pipeline.get(1).asDocument(),
                "{createTime:-1,score:1,id:-1}", "createTime", "score", "id");
        Assert.assertEquals(BsonDocument.parse("{$limit:2}"), pipeline.get(2).asDocument());
    }

    private static void assertSort(BsonDocument stage, String expected, String... keys) {
        Assert.assertEquals(Arrays.asList("$sort"), new ArrayList<>(stage.keySet()));
        BsonDocument body = stage.getDocument("$sort");
        Assert.assertEquals(BsonDocument.parse(expected), body);
        Assert.assertEquals(Arrays.asList(keys), new ArrayList<>(body.keySet()));
        for (String key : keys) {
            Assert.assertEquals(BsonType.INT32, body.get(key).getBsonType());
        }
    }

    private static BsonDocument encode(Bson bson) {
        return bson.toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
    }

    private static class RecordingReceiver extends LambdaAggregateWrapper<RecordingReceiver> {
        private int appendCalls;

        @Override
        public RecordingReceiver custom(Bson bson) {
            appendCalls++;
            return super.custom(bson);
        }
    }
}
