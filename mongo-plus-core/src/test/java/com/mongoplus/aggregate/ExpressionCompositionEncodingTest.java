package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.aggregate.pipeline.AggregateOperator;
import com.mongoplus.aggregate.pipeline.Projections;
import com.mongoplus.conditions.operation.ConditionOperators;
import com.mongoplus.toolkit.Filters;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

/** 表达式操作数的顺序、实际 Java 类型和 eq → expr → match BSON 闭环。 */
public class ExpressionCompositionEncodingTest {
    @Test
    public void fieldAndBoundVariableKeepTheirDistinctPrefixes() {
        BsonDocument actual = encode("$_id", "$$userId");
        Assert.assertEquals(BsonDocument.parse("{$match:{$expr:{$eq:['$_id','$$userId']}}}"), actual);
        Assert.assertEquals("$_id", actual.getDocument("$match").getDocument("$expr")
                .getArray("$eq").get(0).asString().getValue());
        Assert.assertEquals("$$userId", actual.getDocument("$match").getDocument("$expr")
                .getArray("$eq").get(1).asString().getValue());
    }

    @Test
    public void fieldAndPlainStringLiteralKeepOperandOrder() {
        Assert.assertEquals(BsonDocument.parse("{$match:{$expr:{$eq:['$status','PAID']}}}"),
                encode("$status", "PAID"));
        Assert.assertEquals(BsonDocument.parse("{$match:{$expr:{$eq:['PAID','$status']}}}"),
                encode("PAID", "$status"));
    }

    @Test
    public void integerLiteralsUseInt32() {
        BsonDocument actual = encode(1, 1);
        Assert.assertEquals(BsonDocument.parse("{$match:{$expr:{$eq:[1,1]}}}"), actual);
        Assert.assertEquals(BsonType.INT32, actual.getDocument("$match").getDocument("$expr")
                .getArray("$eq").get(0).getBsonType());
        Assert.assertEquals(BsonType.INT32, actual.getDocument("$match").getDocument("$expr")
                .getArray("$eq").get(1).getBsonType());
    }

    @Test
    public void nestedExpressionsAreAcceptedOnBothSides() {
        Assert.assertEquals(BsonDocument.parse("{$match:{$expr:{$eq:[{$multiply:['$quantity',2]},4]}}}"),
                encode(ConditionOperators.multiply("$quantity", 2), 4));
        Assert.assertEquals(BsonDocument.parse("{$match:{$expr:{$eq:[4,{$eq:['$status','PAID']}]}}}"),
                encode(4, AggregateOperator.eq("$status", "PAID")));
        // Bson 接口实现也必须可编码，不能仅验证 Document 实现。
        Assert.assertEquals(BsonDocument.parse("{$match:{$expr:{$eq:[{value:'$quantity'},4]}}}"),
                encode(Projections.computed("value", "$quantity"), 4));
    }

    @Test
    public void matchAppendsOneStageAndKeepsReceiverAndOrder() {
        Aggregate<?> aggregate = new AggregateWrapper();
        aggregate.limit(2);
        Assert.assertSame(aggregate, aggregate.match(Filters.expr(AggregateOperator.eq("$_id", "$$userId"))));
        aggregate.limit(1);
        Assert.assertEquals(3, aggregate.getAggregateConditionList().size());
        Assert.assertEquals(BsonDocument.parse("{$limit:2}"), aggregate.getAggregateConditionList().get(0)
                .toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry()));
        Assert.assertEquals(encode("$_id", "$$userId"), aggregate.getAggregateConditionList().get(1)
                .toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry()));
        Assert.assertEquals(BsonDocument.parse("{$limit:1}"), aggregate.getAggregateConditionList().get(2)
                .toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry()));
    }

    private static BsonDocument encode(Object left, Object right) {
        Bson expression = AggregateOperator.eq(left, right);
        Bson body = Filters.expr(expression);
        Aggregate<?> aggregate = new AggregateWrapper();
        aggregate.match(body);
        return aggregate.getAggregateConditionList().get(0)
                .toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
    }
}
