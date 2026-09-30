package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.aggregate.pipeline.Projections;
import com.mongoplus.conditions.operation.ConditionOperators;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** 编译并验证 Resolver 的 computed + fields + stage 调用树。 */
public class AddFieldsSetDocumentReductionTest {
    @Test
    public void addFieldsKeepsBothComputedFields() {
        verify("$addFields", false);
        verify("$addFields", true);
    }

    @Test
    public void setKeepsBothComputedFields() {
        verify("$set", false);
        verify("$set", true);
    }

    @Test
    public void addFieldsKeepsThreeComputedFieldsInOrder() {
        verifyThree("$addFields");
    }

    @Test
    public void setKeepsThreeComputedFieldsInOrder() {
        verifyThree("$set");
    }

    private static void verify(String stage, boolean listOverload) {
        Bson first = Projections.computed("quantityInt", ConditionOperators.toInt("$quantity"));
        Bson second = Projections.computed("amountText", ConditionOperators.toString("$amount"));
        Bson body = listOverload ? Projections.fields(List.of(first, second))
                : Projections.fields(first, second);
        Aggregate<?> aggregate = new AggregateWrapper();
        if ("$addFields".equals(stage)) {
            aggregate.addFields(body);
        } else {
            aggregate.set(body);
        }
        assertStage(aggregate, stage,
                "{quantityInt:{$toInt:'$quantity'},amountText:{$toString:'$amount'}}",
                List.of("quantityInt", "amountText"));
    }

    private static void verifyThree(String stage) {
        Aggregate<?> aggregate = new AggregateWrapper();
        Bson body = Projections.fields(
                Projections.computed("zeta", ConditionOperators.toInt("$quantity")),
                Projections.computed("alpha", ConditionOperators.toString("$amount")),
                Projections.computed("middle", ConditionOperators.toInt("$price")));
        if ("$addFields".equals(stage)) {
            aggregate.addFields(body);
        } else {
            aggregate.set(body);
        }
        assertStage(aggregate, stage,
                "{zeta:{$toInt:'$quantity'},alpha:{$toString:'$amount'},middle:{$toInt:'$price'}}",
                List.of("zeta", "alpha", "middle"));
    }

    private static void assertStage(Aggregate<?> aggregate, String stage, String bodyJson,
                                    List<String> fieldOrder) {
        Assert.assertEquals(1, aggregate.getAggregateConditionList().size());
        BsonDocument actual = aggregate.getAggregateConditionList().get(0)
                .toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
        Assert.assertEquals(BsonDocument.parse("{" + stage + ":" + bodyJson + "}"), actual);
        Assert.assertEquals(List.of(stage), new ArrayList<>(actual.keySet()));
        Assert.assertEquals(fieldOrder, new ArrayList<>(actual.getDocument(stage).keySet()));
    }
}
