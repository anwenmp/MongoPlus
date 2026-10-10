package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.aggregate.pipeline.AggregateOperator;
import com.mongoplus.aggregate.pipeline.Projections;
import com.mongoplus.conditions.operation.ConditionOperators;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.junit.Assert;
import org.junit.Test;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** 验证实际 runtime、Driver 编码与原 API 行为；不验证 MongoDB 服务端求值或 MCP 选择。 */
public class ExpressionResultTypeEncodingTest {
    private static final CodecRegistry DEFAULT = MongoClientSettings.getDefaultCodecRegistry();
    private static final String[] CONVERSIONS = {"toDate", "toBool", "toDecimal", "toDouble",
            "toInt", "toLong", "toObjectId", "toString"};

    @Test
    public void sixRequestedSubtreesCompileThroughPublicApisAndEncodeExactly() {
        assertStage("{$project:{total:{$multiply:[{$toInt:'$price'},'$quantity']}}}",
                project("total", ConditionOperators.multiply(ConditionOperators.toInt("$price"), "$quantity")));
        Aggregate<?> match = new AggregateWrapper();
        match.match(com.mongoplus.toolkit.Filters.expr(AggregateOperator.gt(ConditionOperators.toInt("$score"), 60)));
        assertStage("{$match:{$expr:{$gt:[{$toInt:'$score'},60]}}}", match);
        assertStage("{$project:{value:{$ifNull:['$price',0]}}}", project("value", ConditionOperators.ifNull("$price", 0)));
        assertStage("{$project:{items:{$concatArrays:['$items','$other']}}}",
                project("items", AggregateOperator.concatArraysExpressions("$items", "$other")));
        Aggregate<?> replace = new AggregateWrapper();
        Document merge = ConditionOperators.mergeObjects("$defaults", "$profile");
        Bson assignable = merge;
        Assert.assertSame(merge, assignable);
        replace.replaceWith(merge);
        assertStage("{$replaceWith:{$mergeObjects:['$defaults','$profile']}}", replace);
        assertStage("{$project:{result:{$cond:[{$gt:['$amount',100]},{$toString:'$amount'},'LOW']}}}",
                project("result", ConditionOperators.condArray(AggregateOperator.gt("$amount", 100),
                        ConditionOperators.toString("$amount"), "LOW")));
    }

    @Test
    public void conversionsRetainOperandRuntimeAndBsonTypes() throws Exception {
        Object[] operands = {"$price", "price", "$$bound", -7, 2147483648L, -1.5D,
                Decimal128.parse("12.50"), true, null, ConditionOperators.toInt("$price")};
        for (String conversion : CONVERSIONS) {
            Method factory = ConditionOperators.class.getDeclaredMethod(conversion, Object.class);
            for (Object operand : operands) {
                Bson expression = (Bson) factory.invoke(null, new Object[]{operand});
                Assert.assertEquals(Document.class, expression.getClass());
                Assert.assertSame(operand, ((Document) expression).get("$" + conversion));
                BsonDocument expected = new Document("$" + conversion, operand).toBsonDocument(Document.class, DEFAULT);
                assertBytes(expected, expression, DEFAULT);
            }
        }
        Assert.assertEquals(new BsonInt64(2147483648L), encode(ConditionOperators.toLong(2147483648L), DEFAULT).get("$toLong"));
        Assert.assertEquals(BsonNull.VALUE, encode(ConditionOperators.toInt((Object) null), DEFAULT).get("$toInt"));
        Assert.assertEquals(BsonDocument.parse("{$toHashedIndexKey:'$price'}"),
                encode(ConditionOperators.toHashedIndexKey("$price"), DEFAULT));
    }

    @Test
    public void collectionAndVarargsKeepIndependentPathsAndOrders() {
        List<Object> values = Arrays.asList(ConditionOperators.toInt("$price"), "$quantity", null, 1L);
        assertBytes(encode(ConditionOperators.multiply(values), DEFAULT), ConditionOperators.multiply(values.toArray()), DEFAULT);
        assertBytes(encode(ConditionOperators.ifNull(values), DEFAULT), ConditionOperators.ifNull(values.toArray()), DEFAULT);
        assertBytes(encode(ConditionOperators.mergeObjects(values), DEFAULT), ConditionOperators.mergeObjects(values.toArray()), DEFAULT);
        Assert.assertSame(values, ((Document) ConditionOperators.multiply(values)).get("$multiply"));
        Assert.assertSame(values, ((Document) ConditionOperators.mergeObjects(values)).get("$mergeObjects"));
        Assert.assertEquals(BsonDocument.parse("{$mergeObjects:'$profile'}"), encode(ConditionOperators.mergeObjects("$profile"), DEFAULT));
        Assert.assertEquals(BsonDocument.parse("{$mergeObjects:['$profile']}"),
                encode(ConditionOperators.mergeObjects((Object) "$profile"), DEFAULT));
        Assert.assertFalse(Arrays.equals(bytes(ConditionOperators.mergeObjects("$profile"), DEFAULT),
                bytes(ConditionOperators.mergeObjects((Object) "$profile"), DEFAULT)));
        assertBytes(BsonDocument.parse("{$mergeObjects:[{$mergeObjects:['$defaults','$settings']},{$ifNull:['$profile',null]}]}"),
                ConditionOperators.mergeObjects(ConditionOperators.mergeObjects("$defaults", "$settings"),
                        ConditionOperators.ifNull("$profile", (Object) null)), DEFAULT);
        Assert.assertEquals(org.bson.codecs.DocumentCodec.class, DEFAULT.get(Document.class).getClass());
        Assert.assertEquals("org.bson.codecs.CollectionCodec", DEFAULT.get(values.getClass()).getClass().getName());
    }

    @Test
    public void concatArraysKeepsLegacyArraysAndNewOperandsDistinct() {
        Bson legacy = AggregateOperator.concatArrays(Arrays.asList(1, 2), Collections.singletonList(3L));
        assertBytes(BsonDocument.parse("{$concatArrays:[[1,2],[NumberLong(3)]]}"), legacy, DEFAULT);
        assertBytes(encode(legacy, DEFAULT), AggregateOperator.concatArraysExpressions(Arrays.asList(1, 2),
                Collections.singletonList(3L)), DEFAULT);
        assertBytes(BsonDocument.parse("{$concatArrays:['$items',{$ifNull:['$other',null]}]}"),
                AggregateOperator.concatArraysExpressions("$items", ConditionOperators.ifNull("$other", (Object) null)), DEFAULT);
        assertBytes(BsonDocument.parse("{$concatArrays:[]}"), AggregateOperator.concatArrays(), DEFAULT);
        assertBytes(BsonDocument.parse("{$concatArrays:[]}"), AggregateOperator.concatArraysExpressions(), DEFAULT);
        assertBytes(BsonDocument.parse("{$concatArrays:[null]}"), AggregateOperator.concatArrays((List<?>) null), DEFAULT);
        assertBytes(BsonDocument.parse("{$concatArrays:[null]}"), AggregateOperator.concatArraysExpressions((Object) null), DEFAULT);
        Assert.assertThrows(NullPointerException.class, () -> AggregateOperator.concatArrays((List<?>[]) null));
        Assert.assertThrows(NullPointerException.class, () -> AggregateOperator.concatArraysExpressions((Object[]) null));
    }

    @Test
    public void condObjectAndArrayRemainStrictlyDifferentIncludingLegacyFourArguments() {
        Bson object = ConditionOperators.cond(AggregateOperator.gt("$amount", 100), ConditionOperators.toString("$amount"), "LOW");
        Bson array = ConditionOperators.condArray(AggregateOperator.gt("$amount", 100), ConditionOperators.toString("$amount"), "LOW");
        Assert.assertEquals(Arrays.asList("if", "then", "else"),
                Arrays.asList(encode(object, DEFAULT).getDocument("$cond").keySet().toArray()));
        Assert.assertFalse(Arrays.equals(bytes(object, DEFAULT), bytes(array, DEFAULT)));
        assertBytes(encode(object, DEFAULT), ConditionOperators.condArray("gt", Arrays.asList("$amount", 100),
                ConditionOperators.toString("$amount"), "LOW"), DEFAULT);
        assertBytes(BsonDocument.parse("{$cond:[null,null,null]}"), ConditionOperators.condArray(null, null, null), DEFAULT);
        assertBytes(BsonDocument.parse("{$cond:{if:null,then:null,else:null}}"), ConditionOperators.cond(null, null, null), DEFAULT);
    }

    @Test
    public void documentAssignableDoesNotImplyCodecOrExpressionRole() {
        Assert.assertTrue(Bson.class.isAssignableFrom(Document.class));
        Assert.assertFalse(Document.class.isAssignableFrom(Bson.class));
        Bson opaque = new Bson() {
            public <T> BsonDocument toBsonDocument(Class<T> type, CodecRegistry registry) {
                return BsonDocument.parse("{$toInt:'$price'}");
            }
        };
        // 默认 registry 的 BsonCodec 可以调用任意 Bson.toBsonDocument；编码成功仍缺少表达式来源证明。
        assertBytes(BsonDocument.parse("{$multiply:[{$toInt:'$price'},2]}"),
                ConditionOperators.multiply(opaque, 2), DEFAULT);
        Assert.assertThrows(CodecConfigurationException.class,
                () -> encode(ConditionOperators.toInt(new Object()), DEFAULT));
        Assert.assertThrows(CodecConfigurationException.class,
                () -> encode(ConditionOperators.mergeObjects(new Object(), "$profile"), DEFAULT));
        // Core 只保存字符串；未绑定变量由证据消费者拒绝，编码器不能证明作用域。
        assertBytes(BsonDocument.parse("{$toInt:'$$missing'}"), ConditionOperators.toInt("$$missing"), DEFAULT);
    }

    @Test
    public void customLeafAndContainerCodecsChangeBytesDespiteSameJavaAssignability() {
        Codec<Integer> integerCodec = new Codec<Integer>() {
            public void encode(BsonWriter writer, Integer value, EncoderContext context) { writer.writeInt64(value); }
            public Integer decode(BsonReader reader, DecoderContext context) { return (int) reader.readInt64(); }
            public Class<Integer> getEncoderClass() { return Integer.class; }
        };
        CodecRegistry custom = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(integerCodec), DEFAULT);
        Bson expression = ConditionOperators.ifNull("$price", 0);
        Assert.assertTrue(encode(expression, DEFAULT).getArray("$ifNull").get(1).isInt32());
        Assert.assertTrue(encode(expression, custom).getArray("$ifNull").get(1).isInt64());
        Assert.assertFalse(Arrays.equals(bytes(expression, DEFAULT), bytes(expression, custom)));
        Codec<Collection> collectionCodec = new Codec<Collection>() {
            public void encode(BsonWriter writer, Collection value, EncoderContext context) { writer.writeString("custom"); }
            public Collection decode(BsonReader reader, DecoderContext context) { throw new UnsupportedOperationException(); }
            public Class<Collection> getEncoderClass() { return Collection.class; }
        };
        CodecRegistry containers = CodecRegistries.fromProviders(new org.bson.codecs.configuration.CodecProvider() {
            @SuppressWarnings("unchecked")
            public <T> Codec<T> get(Class<T> type, CodecRegistry registry) {
                return Collection.class.isAssignableFrom(type) ? (Codec<T>) collectionCodec : null;
            }
        });
        CodecRegistry changed = CodecRegistries.fromRegistries(containers, DEFAULT);
        Assert.assertEquals(new BsonString("custom"), encode(expression, changed).get("$ifNull"));
    }

    @Test
    public void publicSignaturesCompileWithoutVarargsAmbiguityAndRejectIncompatibleTypes() throws Exception {
        compile(true, "Bson a=AggregateOperator.concatArrays(); Bson b=AggregateOperator.concatArrays(null);"
                + "Bson c=AggregateOperator.concatArrays(java.util.Arrays.asList(1,2));"
                + "Bson d=AggregateOperator.concatArraysExpressions(); Bson e=AggregateOperator.concatArraysExpressions(null);"
                + "Bson f=AggregateOperator.concatArraysExpressions(\"$items\",\"$other\");"
                + "Document m=ConditionOperators.mergeObjects(\"$defaults\",\"$profile\"); Bson n=m;"
                + "Bson o=ConditionOperators.toInt(n);");
        compile(false, "Bson a=AggregateOperator.concatArrays(\"$items\",\"$other\");");
        compile(false, "Bson a=ConditionOperators.toInt(\"$price\"); Document d=a;");
    }

    private static void compile(boolean expected, String code) throws Exception {
        Path output = Files.createTempDirectory("expression-public-api-");
        try {
            JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///PublicExpressionApi.java"), JavaFileObject.Kind.SOURCE) {
                @Override public CharSequence getCharContent(boolean ignored) {
                    return "import org.bson.Document; import org.bson.conversions.Bson;"
                            + "import com.mongoplus.aggregate.pipeline.AggregateOperator;"
                            + "import com.mongoplus.conditions.operation.ConditionOperators;"
                            + "class PublicExpressionApi {void construct(){" + code + "}}";
                }
            };
            StringWriter diagnostics = new StringWriter();
            boolean actual = ToolProvider.getSystemJavaCompiler().getTask(diagnostics, null, null,
                    Arrays.asList("-proc:none", "--release", "8", "-classpath", System.getProperty("java.class.path"),
                            "-d", output.toString()), null, Collections.singletonList(source)).call();
            Assert.assertEquals(diagnostics.toString(), expected, actual);
        } finally {
            try (Stream<Path> paths = Files.walk(output)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList())) { Files.delete(path); }
            }
        }
    }

    private static Aggregate<?> project(String name, Bson expression) {
        Aggregate<?> aggregate = new AggregateWrapper();
        aggregate.project(Projections.computed(name, expression));
        return aggregate;
    }

    private static void assertStage(String expected, Aggregate<?> actual) {
        Assert.assertEquals(1, actual.getAggregateConditionList().size());
        assertBytes(BsonDocument.parse(expected), actual.getAggregateConditionList().get(0), DEFAULT);
    }

    private static BsonDocument encode(Bson value, CodecRegistry registry) {
        BsonDocument result = value.toBsonDocument(Document.class, registry);
        result.size();
        return result;
    }

    private static byte[] bytes(Bson value, CodecRegistry registry) {
        org.bson.io.BasicOutputBuffer buffer = new org.bson.io.BasicOutputBuffer();
        try (BsonBinaryWriter writer = new BsonBinaryWriter(buffer)) {
            new org.bson.codecs.BsonDocumentCodec().encode(writer, encode(value, registry), EncoderContext.builder().build());
            return buffer.toByteArray();
        } finally { buffer.close(); }
    }

    private static void assertBytes(Bson expected, Bson actual, CodecRegistry registry) {
        Assert.assertArrayEquals(bytes(expected, DEFAULT), bytes(actual, registry));
    }
}
