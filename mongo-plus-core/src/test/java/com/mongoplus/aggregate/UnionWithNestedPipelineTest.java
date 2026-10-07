package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import org.bson.BsonDocument;
import org.junit.Assert;
import org.junit.Test;

/** U02/U03 的实际 Core/Driver BSON smoke，不连接 MongoDB。 */
public class UnionWithNestedPipelineTest {
    @Test
    public void u02ConsumesIndependentLimitPipeline() {
        Aggregate<?> inner = new AggregateWrapper();
        inner.limit(2);
        Assert.assertEquals(BsonDocument.parse("{$unionWith:{coll:'history_orders',pipeline:[{$limit:2}]}}"), union(inner));
    }

    @Test
    public void u03PreservesSortThenLimit() {
        Aggregate<?> inner = new AggregateWrapper();
        inner.sort("createTime", -1);
        inner.limit(2);
        Assert.assertEquals(BsonDocument.parse("{$unionWith:{coll:'history_orders',pipeline:[{$sort:{createTime:-1}},{$limit:2}]}}"), union(inner));
    }

    private static BsonDocument union(Aggregate<?> inner) {
        Aggregate<?> outer = new AggregateWrapper();
        Assert.assertNotSame(inner.getAggregateConditionList(), outer.getAggregateConditionList());
        Assert.assertTrue(outer.getAggregateConditionList().isEmpty());
        int innerCount = inner.getAggregateConditionList().size();
        Assert.assertSame(outer, outer.unionWith("history_orders", inner));
        Assert.assertEquals(1, outer.getAggregateConditionList().size());
        Assert.assertEquals(innerCount, inner.getAggregateConditionList().size());
        return outer.getAggregateCondition().toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
    }
}
