package com.mongoplus.aggregate;

import com.mongoplus.aggregate.pipeline.Projections;
import com.mongoplus.conditions.operation.ConditionOperators;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

import com.mongodb.MongoClientSettings;

/**
 * 验证 project(Bson) 接收的是投影 body，而不是完整的 {@code $project} stage。
 */
public class ProjectBsonStageBodyTest {

    @Test
    public void projectShouldEncodeCondArrayComputedField() {
        Bson actual = new AggregateWrapper()
                .project(Projections.computed(
                        "level",
                        ConditionOperators.condArray("$isVip", "VIP", "NORMAL")
                ))
                .getAggregateConditionList()
                .get(0);

        assertBsonEquals(
                "{$project:{level:{$cond:['$isVip','VIP','NORMAL']}}}",
                actual
        );
    }

    @Test
    public void projectShouldEncodeNestedMultiplyComputedField() {
        Bson actual = new AggregateWrapper()
                .project(Projections.computed(
                        "total",
                        ConditionOperators.multiply(
                                ConditionOperators.ifNull("$price", 0),
                                ConditionOperators.ifNull("$quantity", 1)
                        )
                ))
                .getAggregateConditionList()
                .get(0);

        assertBsonEquals(
                "{$project:{total:{$multiply:[{$ifNull:['$price',0]},{$ifNull:['$quantity',1]}]}}}",
                actual
        );
    }

    private static void assertBsonEquals(String expectedJson, Bson actual) {
        BsonDocument actualDocument = actual.toBsonDocument(
                BsonDocument.class,
                MongoClientSettings.getDefaultCodecRegistry()
        );
        Assert.assertEquals(BsonDocument.parse(expectedJson), actualDocument);
    }
}
