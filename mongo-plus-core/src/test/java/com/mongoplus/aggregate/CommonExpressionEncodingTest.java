package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.aggregate.pipeline.AggregateOperator;
import com.mongoplus.aggregate.pipeline.Projections;
import com.mongoplus.toolkit.Filters;
import org.bson.*;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.junit.Assert;
import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

/** 只验证 Java/BSON 构造，不把编码成功等同于服务端表达式运行成功。 */
public class CommonExpressionEncodingTest {
    private static final String[] BINARY_NAMES = {"ne", "gt", "gte", "lt", "lte", "subtract", "divide"};

    private static Bson binary(String name, Object left, Object right) {
        switch (name) {
            case "ne": return AggregateOperator.ne(left, right);
            case "gt": return AggregateOperator.gt(left, right);
            case "gte": return AggregateOperator.gte(left, right);
            case "lt": return AggregateOperator.lt(left, right);
            case "lte": return AggregateOperator.lte(left, right);
            case "subtract": return AggregateOperator.subtract(left, right);
            case "divide": return AggregateOperator.divide(left, right);
            default: throw new AssertionError(name);
        }
    }

    @Test
    public void everyBinaryFactoryKeepsOperatorAndOperandOrder() {
        for (String name : BINARY_NAMES) {
            BsonDocument actual = encode(binary(name, "$amount", 100));
            Assert.assertEquals(BsonDocument.parse("{'$" + name + "':['$amount',100]}"), actual);
            Assert.assertEquals(BsonDocument.parse("{'$" + name + "':[100,'$amount']}"),
                    encode(binary(name, 100, "$amount")));
        }
    }

    @Test
    public void numericTypesNegativesAndNullArePreservedForEveryOperator() {
        Object[] values = {-7, -2147483649L, -1.25D, Decimal128.parse("-123.450"), null};
        BsonValue[] encoded = {new BsonInt32(-7), new BsonInt64(-2147483649L),
                new BsonDouble(-1.25D), new BsonDecimal128(Decimal128.parse("-123.450")), BsonNull.VALUE};
        for (int i = 0; i < values.length; i++) {
            for (String name : BINARY_NAMES) {
                Assert.assertEquals(new BsonArray(Arrays.asList(encoded[i], encoded[i])),
                        encode(binary(name, values[i], values[i])).getArray("$" + name));
            }
            Assert.assertEquals(new BsonArray(Collections.singletonList(encoded[i])),
                    encode(AggregateOperator.not(values[i])).getArray("$not"));
            Assert.assertEquals(new BsonArray(Collections.singletonList(encoded[i])),
                    encode(AggregateOperator.and(values[i])).getArray("$and"));
            Assert.assertEquals(new BsonArray(Collections.singletonList(encoded[i])),
                    encode(AggregateOperator.or(values[i])).getArray("$or"));
        }
    }

    @Test
    public void referencesAndOrdinaryStringsRemainDistinct() {
        for (String name : BINARY_NAMES) {
            Assert.assertEquals(BsonDocument.parse("{'$" + name + "':['$amount','amount']}"),
                    encode(binary(name, "$amount", "amount")));
            Assert.assertEquals(BsonDocument.parse("{'$" + name + "':['$$bound','$amount']}"),
                    encode(binary(name, "$$bound", "$amount")));
        }
        Assert.assertEquals(BsonDocument.parse("{$and:['$amount','$$bound','amount','CANCELLED']}"),
                encode(AggregateOperator.and("$amount", "$$bound", "amount", "CANCELLED")));
    }

    @Test
    public void logicalArraysKeepEmptySingletonAndMultipleInputs() {
        Assert.assertEquals(BsonDocument.parse("{$and:[]}"), encode(AggregateOperator.and()));
        Assert.assertEquals(BsonDocument.parse("{$or:[]}"), encode(AggregateOperator.or()));
        Assert.assertEquals(BsonDocument.parse("{$and:[true]}"), encode(AggregateOperator.and(true)));
        Assert.assertEquals(BsonDocument.parse("{$or:[false]}"), encode(AggregateOperator.or(false)));
        Assert.assertEquals(BsonDocument.parse("{$or:[false,{$gt:['$amount',100]},null]}"),
                encode(AggregateOperator.or(false, AggregateOperator.gt("$amount", 100), null)));
        Assert.assertEquals(BsonDocument.parse("{$not:[null]}"), encode(AggregateOperator.not(null)));
        // 单输入恰好是数组时保留内层数组，不能误当成多个操作数。
        Assert.assertEquals(BsonDocument.parse("{$not:[[1,2]]}"),
                encode(AggregateOperator.not(Arrays.asList(1, 2))));
        Assert.assertEquals(BsonDocument.parse("{$and:[null]}"), encode(AggregateOperator.and((Object) null)));
        Assert.assertEquals(BsonDocument.parse("{$or:[null]}"), encode(AggregateOperator.or((Object) null)));
    }

    @Test
    public void nullVarargsContainersAreRejected() {
        Assert.assertThrows(NullPointerException.class, () -> AggregateOperator.and((Object[]) null));
        Assert.assertThrows(NullPointerException.class, () -> AggregateOperator.or((Object[]) null));
    }

    @Test
    public void bothMinimalInputsCompileAndEncodeThroughExistingStageApis() {
        Aggregate<?> match = new AggregateWrapper();
        match.match(Filters.expr(AggregateOperator.and(
                AggregateOperator.gt("$amount", 100),
                AggregateOperator.lte("$amount", 1000),
                AggregateOperator.ne("$status", "CANCELLED"))));
        Assert.assertEquals(BsonDocument.parse("{$match:{$expr:{$and:[{$gt:['$amount',100]},"
                        + "{$lte:['$amount',1000]},{$ne:['$status','CANCELLED']}]}}}"),
                encode(match.getAggregateConditionList().get(0)));
        Aggregate<?> project = new AggregateWrapper();
        project.project(Projections.fields(
                Projections.computed("difference", AggregateOperator.subtract("$amount", "$discount")),
                Projections.computed("ratio", AggregateOperator.divide("$amount", 100)),
                Projections.computed("valid", AggregateOperator.not(AggregateOperator.eq("$status", "CANCELLED")))));
        Assert.assertEquals(BsonDocument.parse("{$project:{difference:{$subtract:['$amount','$discount']},"
                        + "ratio:{$divide:['$amount',100]},valid:{$not:[{$eq:['$status','CANCELLED']}]}}}"),
                encode(project.getAggregateConditionList().get(0)));
        Assert.assertEquals(1, match.getAggregateConditionList().size());
        Assert.assertEquals(1, project.getAggregateConditionList().size());
    }

    @Test
    public void nestedExpressionsPreserveAllWrappersAndOrders() {
        Assert.assertEquals(BsonDocument.parse("{$or:[{$not:[{$and:[{$gte:['$amount',100]},"
                        + "{$lt:['$amount',1000]}]}]},{$eq:[{$subtract:[{$divide:['$amount',100]},2]},3]}]}"),
                encode(AggregateOperator.or(AggregateOperator.not(AggregateOperator.and(
                        AggregateOperator.gte("$amount", 100), AggregateOperator.lt("$amount", 1000))),
                        AggregateOperator.eq(AggregateOperator.subtract(AggregateOperator.divide("$amount", 100), 2), 3))));
    }

    @Test
    public void factoriesDoNotEvaluateNumbersOrInventRuntimeTypes() {
        Assert.assertEquals(BsonDocument.parse("{$divide:[1,0]}"), encode(AggregateOperator.divide(1, 0)));
        Assert.assertEquals(BsonDocument.parse("{$subtract:[1,2]}"), encode(AggregateOperator.subtract(1, 2)));
        Assert.assertEquals(BsonType.INT32, encode(AggregateOperator.divide(1, 2)).getArray("$divide").get(0).getBsonType());
    }

    @Test
    public void unknownRuntimeCodecIsNotSilentlyAccepted() {
        for (String name : BINARY_NAMES) {
            Assert.assertThrows(CodecConfigurationException.class, () -> encode(binary(name, new Object(), 1)));
        }
        Assert.assertThrows(CodecConfigurationException.class, () -> encode(AggregateOperator.and(new Object())));
        Assert.assertThrows(CodecConfigurationException.class, () -> encode(AggregateOperator.or(new Object())));
        Assert.assertThrows(CodecConfigurationException.class, () -> encode(AggregateOperator.not(new Object())));
    }

    @Test
    public void existingQueryPredicatesKeepTheirDifferentBsonShape() {
        Assert.assertEquals(BsonDocument.parse("{amount:{$gt:100}}"), encode(Filters.gt("amount", 100)));
        Assert.assertEquals(BsonDocument.parse("{amount:{$not:{$gt:100}}}"),
                encode(Filters.not(Filters.gt("amount", 100))));
        Assert.assertEquals(BsonDocument.parse("{$gt:['$amount',100]}"), encode(AggregateOperator.gt("$amount", 100)));
    }

    @Test
    public void wrongFixedOperandCountsFailJavaCompilation() {
        for (String name : BINARY_NAMES) {
            compileMustFail("AggregateOperator." + name + "(1)");
            compileMustFail("AggregateOperator." + name + "(1,2,3)");
        }
        compileMustFail("AggregateOperator.not()");
        compileMustFail("AggregateOperator.not(1,2)");
    }

    private static void compileMustFail(String call) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assert.assertNotNull("数量拒绝测试必须运行在 JDK", compiler);
        SimpleJavaFileObject source = new SimpleJavaFileObject(URI.create("string:///InvalidExpression.java"),
                javax.tools.JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignored) {
                return "import com.mongoplus.aggregate.pipeline.AggregateOperator; class InvalidExpression {"
                        + " Object value = " + call + "; }";
            }
        };
        StringWriter diagnostics = new StringWriter();
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        Assert.assertFalse(call, compiler.getTask(diagnostics, null, errors,
                Arrays.asList("-proc:none", "-classpath", System.getProperty("java.class.path")), null,
                Collections.singletonList(source)).call());
        Assert.assertTrue(call + errors.getDiagnostics(), errors.getDiagnostics().stream()
                .anyMatch(error -> error.getCode().contains("cant.apply.symbol")));
    }

    private static BsonDocument encode(Bson expression) {
        BsonDocument document = expression.toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
        document.size(); // Document 返回延迟 wrapper；强制实际编码，才能捕获缺 codec。
        return document;
    }
}
