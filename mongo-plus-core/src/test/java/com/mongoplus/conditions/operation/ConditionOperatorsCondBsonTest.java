package com.mongoplus.conditions.operation;

import com.mongodb.MongoClientSettings;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * {@link ConditionOperators} 中 {@code $cond} 表达式的 BSON 结构回归测试。
 */
public class ConditionOperatorsCondBsonTest {

    private static final String THEN_VALUE = "VIP";

    private static final String ELSE_VALUE = "NORMAL";

    @Test
    public void condWithThreeArgumentsShouldEncodeObjectForm() {
        Bson actual = ConditionOperators.cond("$isVip", THEN_VALUE, ELSE_VALUE);

        assertBsonEquals(
                "{$cond:{if:'$isVip',then:'VIP',else:'NORMAL'}}",
                actual
        );
    }

    @Test
    public void condArrayWithThreeArgumentsShouldEncodeArrayForm() {
        Bson actual = ConditionOperators.condArray("$isVip", THEN_VALUE, ELSE_VALUE);

        assertBsonEquals(
                "{$cond:['$isVip','VIP','NORMAL']}",
                actual
        );
    }

    @Test
    public void condWithFourArgumentsShouldEncodeObjectForm() {
        Bson actual = ConditionOperators.cond(
                "$eq",
                Arrays.asList("$isVip", true),
                THEN_VALUE,
                ELSE_VALUE
        );

        assertBsonEquals(
                "{$cond:{if:{$eq:['$isVip',true]},then:'VIP',else:'NORMAL'}}",
                actual
        );
    }

    @Test
    public void condArrayWithFourArgumentsShouldAlsoEncodeObjectForm() {
        Bson actual = ConditionOperators.condArray(
                "$eq",
                Arrays.asList("$isVip", true),
                THEN_VALUE,
                ELSE_VALUE
        );

        assertBsonEquals(
                "{$cond:{if:{$eq:['$isVip',true]},then:'VIP',else:'NORMAL'}}",
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
