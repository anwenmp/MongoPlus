package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.BsonField;
import com.mongoplus.aggregate.pipeline.Facet;
import com.mongoplus.aggregate.pipeline.Projections;
import com.mongoplus.aggregate.pipeline.Sorts;
import com.mongoplus.conditions.operation.ConditionOperators;
import com.mongoplus.support.SFunction;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.RawBsonDocument;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

/** 真实 Core/Driver 构造的逐字节验证：BsonDocument.equals 不足以验证字段顺序。 */
public class CandidateEquivalenceEncodingTest {
    @Test
    public void scalarRangeAndSingletonNormalizationHaveExplicitBoundaries() {
        for (int value : new int[] {0, 1, Integer.MAX_VALUE}) {
            Aggregate<?> left = new AggregateWrapper();
            Aggregate<?> right = new AggregateWrapper();
            Assert.assertSame(left, left.skip(value));
            Assert.assertSame(right, right.skip((long) value));
            same(single(left), single(right));
            Assert.assertEquals(new BsonInt32(value), doc(single(right)).get("$skip"));
            different(single(right), new BsonDocument("$skip", new BsonInt64(value)));
        }
        Aggregate<?> outside = new AggregateWrapper();
        Assert.assertThrows(ArithmeticException.class, () -> outside.skip(2147483648L));
        Assert.assertTrue(outside.getAggregateConditionList().isEmpty());
        for (List<String> fields : Arrays.asList(Collections.singletonList("a"), Arrays.asList("a", "b"),
                Arrays.asList("b", "a"))) {
            Aggregate<?> left = new AggregateWrapper();
            Aggregate<?> right = new AggregateWrapper();
            left.unset(fields.toArray(new String[0]));
            right.unset(fields);
            same(single(left), single(right));
        }
        Aggregate<?> normalized = new AggregateWrapper();
        normalized.unset(Collections.singletonList("a"));
        same(single(normalized), new BsonDocument("$unset", new BsonString("a")));
        different(single(normalized), BsonDocument.parse("{$unset:['a']}"));
    }

    @Test
    public void completeSortTreesCanCrossApisWithoutChangingStageCount() {
        for (int direction : new int[] {1, -1}) {
            Aggregate<?> old = new AggregateWrapper();
            Aggregate<?> modern = new AggregateWrapper();
            old.sort("a", direction);
            modern.sortSpecification(direction == 1 ? Sorts.asc("a") : Sorts.desc(Arrays.asList("a")));
            same(single(old), single(modern));
        }
        Aggregate<?> old = new AggregateWrapper();
        Aggregate<?> modern = new AggregateWrapper();
        old.sortAsc(Arrays.asList("b", "a"));
        modern.sortSpecification(Sorts.orderBy(Sorts.asc("b", "a")));
        same(single(old), single(modern));
        Aggregate<?> separated = new AggregateWrapper();
        separated.sortAsc("b");
        separated.sortAsc("a");
        Assert.assertEquals(2, separated.getAggregateConditionList().size());
        Aggregate<?> raw = new AggregateWrapper();
        raw.sort(Sorts.asc("a"));
        different(single(raw), single(modern));
        different(Sorts.asc("a", "b"), Sorts.asc("b", "a"));
        different(single(modern), BsonDocument.parse("{$sort:{b:1.0,a:1.0}}"));
    }

    @Test
    public void containerRoutesRetainFieldsValuesAndDuplicatePositions() {
        for (List<String> fields : Arrays.asList(Arrays.asList("b", "a"), Arrays.asList("a", "b", "a"))) {
            same(Sorts.asc(fields), Sorts.asc(fields.toArray(new String[0])));
            same(Sorts.desc(fields), Sorts.desc(fields.toArray(new String[0])));
            same(Projections.include(fields), Projections.include(fields.toArray(new String[0])));
            same(Projections.exclude(fields), Projections.exclude(fields.toArray(new String[0])));
        }
        List<Bson> parts = Arrays.asList(Sorts.asc("a", "b"), Sorts.desc("a"));
        same(Sorts.orderBy(parts), Sorts.orderBy(parts.toArray(new Bson[0])));
        same(Projections.fields(parts), Projections.fields(parts.toArray(new Bson[0])));
        Assert.assertEquals(Arrays.asList("a", "b"), Arrays.asList(doc(Sorts.orderBy(parts)).keySet().toArray()));
        Assert.assertEquals(Arrays.asList("b", "a"), Arrays.asList(doc(Projections.fields(parts)).keySet().toArray()));
        different(Sorts.orderBy(parts), Projections.fields(parts));
        // 无重键时两个不同 reducer 的原始 BSON 可以相同；结果角色仍必须由各自调用上下文检查。
        List<Bson> disjoint = Arrays.asList(Sorts.asc("a"), Sorts.desc("b"));
        same(Sorts.orderBy(disjoint), Projections.fields(disjoint));
        different(Projections.include("a"), Projections.exclude("a"));
    }

    @Test
    public void getterRoutesAreEqualOnlyAfterActualFieldExtraction() {
        SFunction<Row, ?> a = Row::getAlpha;
        SFunction<Row, ?> b = Row::getBeta;
        same(Sorts.asc(a, b), Sorts.asc("alpha", "beta"));
        same(Sorts.descLambda(Arrays.asList(b, a)), Sorts.desc(Arrays.asList("beta", "alpha")));
        same(Projections.include(a, b), Projections.include(Arrays.asList("alpha", "beta")));
        same(Projections.excludeLambda(Arrays.asList(b, a)), Projections.exclude("beta", "alpha"));
        different(Sorts.asc(a), Sorts.asc("$alpha"));
    }

    @Test
    public void expressionContainersPreserveOperandOrderAndNumericBsonTypes() {
        List<Object> operands = Arrays.<Object>asList("$price", 2, 3L, 0.5D);
        same(ConditionOperators.multiply(operands), ConditionOperators.multiply(operands.toArray()));
        different(ConditionOperators.multiply("$price", 2, 3L), ConditionOperators.multiply("$price", 2L, 3));
        different(ConditionOperators.multiply("$price", "$quantity"),
                ConditionOperators.multiply("$quantity", "$price"));
        Object unknown = new Object();
        Assert.assertThrows(org.bson.codecs.configuration.CodecConfigurationException.class,
                () -> bytes(ConditionOperators.multiply(Arrays.asList(unknown, 1))));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void sameRegistryDoesNotProveDifferentContainerCodecsEquivalent() {
        Codec<ArrayList> custom = new Codec<ArrayList>() {
            public Class<ArrayList> getEncoderClass() { return ArrayList.class; }
            public void encode(org.bson.BsonWriter writer, ArrayList value, EncoderContext context) {
                writer.writeStartArray();
                writer.writeInt32(99);
                writer.writeEndArray();
            }
            public ArrayList decode(org.bson.BsonReader reader, DecoderContext context) {
                throw new UnsupportedOperationException("编码反例不使用解码");
            }
        };
        CodecRegistry registry = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(custom),
                MongoClientSettings.getDefaultCodecRegistry());
        Bson arrayRoute = ConditionOperators.multiply("$price", 2);
        Bson collectionRoute = ConditionOperators.multiply(new LinkedList<Object>(Arrays.<Object>asList("$price", 2)));
        Assert.assertFalse(Arrays.equals(bytes(arrayRoute, registry), bytes(collectionRoute, registry)));
    }

    @Test
    public void projectionEquivalenceIncludesEveryNestedExpressionRoute() {
        Aggregate<?> left = new AggregateWrapper();
        Aggregate<?> right = new AggregateWrapper();
        left.project(Projections.fields(Projections.include("name"),
                Projections.computed("total", ConditionOperators.multiply("$price", "$quantity")),
                Projections.excludeId()));
        right.project(Projections.fields(Arrays.asList(Projections.include(Arrays.asList("name")),
                Projections.computed("total", ConditionOperators.multiply(Arrays.asList("$price", "$quantity"))),
                Projections.excludeId())));
        same(single(left), single(right));
        Assert.assertEquals(Arrays.asList("name", "total", "_id"),
                Arrays.asList(doc(single(left)).getDocument("$project").keySet().toArray()));
    }

    @Test
    public void groupRoutesRequireSameIdAndOrderedNamedExpressionEntries() {
        List<BsonField> fields = Arrays.asList(new BsonField("total", new Document("$sum", "$price")),
                new BsonField("count", new Document("$sum", 1)));
        for (Object id : Arrays.<Object>asList("$category", null, 1, 1L)) {
            Aggregate<?> left = new AggregateWrapper();
            Aggregate<?> right = new AggregateWrapper();
            left.group(id, fields.toArray(new BsonField[0]));
            right.group(id, fields);
            same(single(left), single(right));
        }
        Aggregate<?> one = new AggregateWrapper();
        Aggregate<?> empty = new AggregateWrapper();
        one.group("$category");
        empty.group((Object) "$category", Collections.<BsonField>emptyList());
        same(single(one), single(empty));
        Aggregate<?> reversed = new AggregateWrapper();
        Aggregate<?> ordered = new AggregateWrapper();
        reversed.group("$category", Arrays.asList(fields.get(1), fields.get(0)));
        ordered.group("$category", fields);
        different(single(reversed), single(ordered));
        Aggregate<?> numericA = new AggregateWrapper();
        Aggregate<?> numericB = new AggregateWrapper();
        numericA.group(1, fields);
        numericB.group(1L, fields);
        different(single(numericA), single(numericB));
        Aggregate<?> getter = new AggregateWrapper();
        Aggregate<?> raw = new AggregateWrapper();
        getter.group((SFunction<Row, ?>) Row::getAlpha);
        raw.group("alpha");
        different(single(getter), single(raw));
    }

    @Test
    public void nestedEquivalentTreesRetainIndependentReceiverEffects() {
        for (int outer = 0; outer < 3; outer++) {
            Aggregate<?> leftInner = new AggregateWrapper();
            Aggregate<?> rightInner = new AggregateWrapper();
            leftInner.skip(1);
            leftInner.sort("a", -1);
            leftInner.unset("hidden");
            rightInner.skip(1L);
            rightInner.sortSpecification(Sorts.desc(Arrays.asList("a")));
            rightInner.unset(Collections.singletonList("hidden"));
            Aggregate<?> left = new AggregateWrapper();
            Aggregate<?> right = new AggregateWrapper();
            Aggregate<?> sibling = new AggregateWrapper();
            sibling.skip(9);
            if (outer == 0) {
                left.facet(new Facet("rows", leftInner), new Facet("other", sibling));
                right.facet(new Facet("rows", rightInner), new Facet("other", sibling));
            } else if (outer == 1) {
                left.lookup("items", leftInner, "rows");
                right.lookup("items", rightInner, "rows");
            } else {
                left.unionWith("items", leftInner);
                right.unionWith("items", rightInner);
            }
            same(single(left), single(right));
            Assert.assertEquals(3, leftInner.getAggregateConditionList().size());
            Assert.assertEquals(3, rightInner.getAggregateConditionList().size());
            Assert.assertEquals(1, sibling.getAggregateConditionList().size());
            Assert.assertNotSame(left.getAggregateConditionList(), leftInner.getAggregateConditionList());
            Assert.assertNotSame(leftInner.getAggregateConditionList(), rightInner.getAggregateConditionList());
        }
    }

    private static Bson single(Aggregate<?> aggregate) {
        Assert.assertEquals(1, aggregate.getAggregateConditionList().size());
        return aggregate.getAggregateCondition(0);
    }

    private static BsonDocument doc(Bson value) {
        return value.toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
    }

    private static byte[] bytes(Bson value) {
        return bytes(value, MongoClientSettings.getDefaultCodecRegistry());
    }

    private static byte[] bytes(Bson value, CodecRegistry registry) {
        org.bson.ByteBuf buffer = new RawBsonDocument(value.toBsonDocument(Document.class, registry),
                new BsonDocumentCodec()).getByteBuffer();
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }

    private static void same(Bson left, Bson right) { Assert.assertArrayEquals(bytes(left), bytes(right)); }
    private static void different(Bson left, Bson right) { Assert.assertFalse(Arrays.equals(bytes(left), bytes(right))); }

    public static class Row {
        private String alpha;
        private String beta;
        public String getAlpha() { return alpha; }
        public String getBeta() { return beta; }
    }
}
