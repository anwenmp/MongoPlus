package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.aggregate.pipeline.Facet;
import java.util.ArrayList;
import java.util.Arrays;
import org.bson.BsonDocument;
import org.junit.Assert;
import org.junit.Test;

/** 仅验证本轮 Index evidence 选择的构造路径与两个目标 BSON，不连接 MongoDB。 */
public class FacetNestedPipelineTest {
    @Test
    public void singleEntryUsesCompleteInnerPipeline() {
        Aggregate<?> inner = new AggregateWrapper();
        inner.limit(2);
        Aggregate<?> outer = new AggregateWrapper();
        outer.facet(new Facet("rows", inner));
        Assert.assertEquals(BsonDocument.parse("{$facet:{rows:[{$limit:2}]}}"), stage(outer));
    }

    @Test
    public void twoIndependentEntriesBecomeOneFacetInOrder() {
        Aggregate<?> rows = new AggregateWrapper();
        Aggregate<?> topRows = new AggregateWrapper();
        Assert.assertNotSame(rows.getAggregateConditionList(), topRows.getAggregateConditionList());
        Assert.assertTrue(rows.getAggregateConditionList().isEmpty());
        Assert.assertTrue(topRows.getAggregateConditionList().isEmpty());
        rows.limit(2);
        topRows.sort("createTime", -1);
        topRows.limit(1);
        Aggregate<?> outer = new AggregateWrapper();
        outer.facet(new Facet("rows", rows), new Facet("topRows", topRows));
        BsonDocument actual = stage(outer);
        Assert.assertEquals(BsonDocument.parse("{$facet:{rows:[{$limit:2}],"
                + "topRows:[{$sort:{createTime:-1}},{$limit:1}]}}"), actual);
        Assert.assertEquals(Arrays.asList("rows", "topRows"), new ArrayList<>(actual.getDocument("$facet").keySet()));
        Assert.assertEquals(1, rows.getAggregateConditionList().size());
        Assert.assertEquals(2, topRows.getAggregateConditionList().size());
    }

    private static BsonDocument stage(Aggregate<?> receiver) {
        Assert.assertEquals(1, receiver.getAggregateConditionList().size());
        Assert.assertSame(receiver.getAggregateConditionList(), receiver.getAggregateConditionList());
        return receiver.getAggregateConditionList().get(0)
                .toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
    }
}
