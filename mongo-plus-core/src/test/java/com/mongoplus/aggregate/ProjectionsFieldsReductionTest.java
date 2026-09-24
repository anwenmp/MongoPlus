package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.aggregate.pipeline.Projections;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/** 将归约标签中的输入顺序、覆盖、浅合并及空输入契约固定到实际 BSON 输出。 */
public class ProjectionsFieldsReductionTest {
    @Test
    public void emptyInputsProduceEmptyDocument() {
        assertBoth(new BsonDocument());
    }

    @Test
    public void computedDocumentsMergeInInputOrder() {
        assertBoth(BsonDocument.parse("{first:'$a',second:'$b'}"),
                Projections.computed("first", "$a"), Projections.computed("second", "$b"));
    }

    @Test
    public void laterDuplicateReplacesEarlierValue() {
        Bson first = BsonDocument.parse("{a:1,b:2}");
        Bson last = BsonDocument.parse("{a:3}");
        assertBoth(BsonDocument.parse("{b:2,a:3}"), first, last);
        assertBoth(BsonDocument.parse("{a:1,b:2}"), last, first);
    }

    @Test
    public void duplicateNestedDocumentIsReplacedWithoutRecursiveMerge() {
        assertBoth(BsonDocument.parse("{nested:{newValue:2}}"),
                BsonDocument.parse("{nested:{oldValue:1}}"), BsonDocument.parse("{nested:{newValue:2}}"));
    }

    private static void assertBoth(BsonDocument expected, Bson... inputs) {
        for (Bson actual : Arrays.asList(Projections.fields(inputs),
                Projections.fields(inputs.length == 0 ? Collections.<Bson>emptyList() : Arrays.asList(inputs)))) {
            BsonDocument document = actual.toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
            Assert.assertEquals(expected, document);
            Assert.assertEquals(Arrays.asList(expected.keySet().toArray()), Arrays.asList(document.keySet().toArray()));
        }
    }
}
