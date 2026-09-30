package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Aggregates;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.junit.Assert;
import org.junit.Test;

/** 固定真实 Core/Driver 的编码边界；Index 不能把 Number.intValue 的截断行为承诺为等价。 */
public class SampleInt32EncodingTest {
    @Test
    public void positiveInt32BoundariesShouldPreserveBsonExactly() {
        for (int size : new int[] {1, 5, 100, Integer.MAX_VALUE}) {
            BsonDocument expected = new BsonDocument("$sample", new BsonDocument("size", new BsonInt32(size)));
            Assert.assertEquals(expected, encode(size));
            Assert.assertEquals(expected, Aggregates.sample(size).toBsonDocument(
                    BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry()));
            Assert.assertTrue(encode(size).getDocument("$sample").get("size").isInt32());
        }
    }

    @Test
    public void exactNumberRepresentationsShouldStillEncodeAsInt32() {
        for (Number size : new Number[] {(byte) 5, (short) 5, 5, 5L, BigInteger.valueOf(5), new BigDecimal("5")}) {
            Assert.assertEquals(BsonDocument.parse("{$sample:{size:5}}"), encode(size));
        }
        // 数值可转成 int 不证明原 BSON 的 Int64/Double/Decimal128 类型也保持不变。
        Assert.assertNotEquals(BsonDocument.parse("{$sample:{size:{$numberLong:'5'}}}"), encode(5L));
        Assert.assertNotEquals(BsonDocument.parse("{$sample:{size:5.0}}"), encode(5.0));
        Assert.assertNotEquals(BsonDocument.parse("{$sample:{size:{$numberDecimal:'5'}}}"), encode(new BigDecimal("5")));
    }

    @Test
    public void valuesOutsideEvidenceShouldExposeExistingLossyConversion() {
        assertEncodedSize(0, 0);
        assertEncodedSize(-1, -1);
        assertEncodedSize(1.5, 1);
        assertEncodedSize(2147483648L, Integer.MIN_VALUE);
        assertEncodedSize(new BigInteger("4294967297"), 1);
        assertEncodedSize(new BigDecimal("1.0000000000000000000000001"), 1);
        assertEncodedSize(Double.NaN, 0);
        assertEncodedSize(Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
        // 自定义 Number 的 intValue 可以任意实现，不能由 Java 参数类型推断精确转换。
        assertEncodedSize(new Number() {
            @Override public int intValue() { return 7; }
            @Override public long longValue() { return 8L; }
            @Override public float floatValue() { return 8.5F; }
            @Override public double doubleValue() { return 8.5; }
        }, 7);
    }

    private static void assertEncodedSize(Number input, int expected) {
        Assert.assertEquals(new BsonInt32(expected), encode(input).getDocument("$sample").get("size"));
    }

    private static BsonDocument encode(Number size) {
        Aggregate<?> aggregate = new AggregateWrapper();
        aggregate.sample(size);
        Assert.assertEquals(1, aggregate.getAggregateConditionList().size());
        return aggregate.getAggregateCondition(0).toBsonDocument(
                BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
    }
}
