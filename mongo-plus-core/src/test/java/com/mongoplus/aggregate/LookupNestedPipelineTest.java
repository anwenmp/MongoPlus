package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import org.bson.BsonDocument;
import org.junit.Assert;
import org.junit.Test;

/** 两个无 let lookup 输入的真实 Core/Driver BSON 验证，不连接 MongoDB。 */
public class LookupNestedPipelineTest {
    @Test
    public void consumesIndependentLimitPipeline() {
        Aggregate<?> inner = new AggregateWrapper();
        inner.limit(1);
        Assert.assertEquals(BsonDocument.parse("{$lookup:{from:'users',pipeline:[{$limit:1}],as:'userInfo'}}"),
                lookup(inner));
    }

    @Test
    public void preservesSortThenLimit() {
        Aggregate<?> inner = new AggregateWrapper();
        inner.sort("createTime", -1);
        inner.limit(1);
        Assert.assertEquals(BsonDocument.parse(
                "{$lookup:{from:'users',pipeline:[{$sort:{createTime:-1}},{$limit:1}],as:'userInfo'}}"),
                lookup(inner));
    }

    private static BsonDocument lookup(Aggregate<?> inner) {
        Aggregate<?> outer = new AggregateWrapper();
        Assert.assertNotSame(inner.getAggregateConditionList(), outer.getAggregateConditionList());
        Assert.assertTrue(outer.getAggregateConditionList().isEmpty());
        int innerCount = inner.getAggregateConditionList().size();
        Assert.assertSame(outer, outer.lookup("users", inner, "userInfo"));
        Assert.assertEquals(1, outer.getAggregateConditionList().size());
        Assert.assertEquals(innerCount, inner.getAggregateConditionList().size());
        return outer.getAggregateCondition().toBsonDocument(BsonDocument.class,
                MongoClientSettings.getDefaultCodecRegistry());
    }
}
