package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.aggregate.pipeline.AggregateOperator;
import com.mongoplus.toolkit.Filters;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import org.bson.*;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.conversions.Bson;
import org.bson.io.BasicOutputBuffer;
import org.bson.types.Decimal128;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.element.ExecutableElement;
import javax.tools.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Q01-Q04 的真实 Java 21 绑定与 Driver 字节验证；不执行服务器或下游 Resolver。 */
public class QueryPredicateEncodingTest {
    @Test
    public void java21CompilesAndExecutesEveryRouteWithActualOverloads() throws Exception {
        String[] bodies = {
                "Filters.fields(Filters.eq(\"status\",\"PAID\"),Filters.gte(\"amount\",100))",
                "Filters.fields(Arrays.asList(Filters.eq(\"status\",\"PAID\"),Filters.gte(\"amount\",100)))",
                "Filters.and(Filters.eq(\"status\",\"PAID\"),Filters.gte(\"amount\",100))",
                "Filters.and(Arrays.asList(Filters.eq(\"status\",\"PAID\"),Filters.gte(\"amount\",100)))",
                "Filters.or(Filters.eq(\"status\",\"PAID\"),Filters.eq(\"status\",\"FINISHED\"))",
                "Filters.or(Arrays.asList(Filters.eq(\"status\",\"PAID\"),Filters.eq(\"status\",\"FINISHED\")))",
                "Filters.expr(AggregateOperator.gte(\"$amount\",100))"
        };
        String[] expected = {
                "{$match:{status:'PAID',amount:{$gte:100}}}",
                "{$match:{$and:[{status:'PAID'},{amount:{$gte:100}}]}}",
                "{$match:{$or:[{status:'PAID'},{status:'FINISHED'}]}}",
                "{$match:{$expr:{$gte:['$amount',100]}}}"
        };
        StringBuilder text = new StringBuilder("import com.mongoplus.aggregate.*;"
                + "import com.mongoplus.aggregate.pipeline.AggregateOperator;"
                + "import com.mongoplus.toolkit.Filters;import java.util.*;"
                + "public class QueryActualJava21 {");
        for (int i = 0; i < bodies.length; i++) {
            text.append("public static Aggregate<?> q").append(i).append("(){"
                    + "Aggregate<?> a=new AggregateWrapper();a.skip(7);"
                    + "if(a.match(").append(bodies[i]).append(")!=a)throw new AssertionError();"
                    + "a.limit(9);return a;}");
        }
        text.append("}");
        Path output = Files.createTempDirectory("query-java21-");
        try {
            List<String> calls = compile(text.toString(), output);
            Assert.assertEquals(7, calls.stream().filter(s -> s.equals(
                    "com.mongoplus.aggregate.Aggregate.match(org.bson.conversions.Bson)")).count());
            for (String factory : Arrays.asList("fields", "and", "or")) {
                Assert.assertTrue(calls.toString(), calls.contains("com.mongoplus.toolkit.Filters."
                        + factory + "(org.bson.conversions.Bson[])"));
                Assert.assertTrue(calls.toString(), calls.contains("com.mongoplus.toolkit.Filters."
                        + factory + "(java.lang.Iterable<org.bson.conversions.Bson>)"));
            }
            Assert.assertTrue(calls.contains("com.mongoplus.toolkit.Filters.eq(java.lang.String,TItem)"));
            Assert.assertTrue(calls.contains("com.mongoplus.toolkit.Filters.gte(java.lang.String,TItem)"));
            Assert.assertTrue(calls.toString(), calls.contains(
                    "BOUND com.mongoplus.toolkit.Filters.eq(java.lang.String,java.lang.String)"));
            Assert.assertTrue(calls.toString(), calls.contains(
                    "BOUND com.mongoplus.toolkit.Filters.gte(java.lang.String,java.lang.Integer)"));
            Assert.assertTrue(calls.contains("com.mongoplus.aggregate.pipeline.AggregateOperator.gte(java.lang.Object,java.lang.Object)"));
            try (URLClassLoader loader = new URLClassLoader(new URL[]{output.toUri().toURL()}, getClass().getClassLoader())) {
                Class<?> type = Class.forName("QueryActualJava21", true, loader);
                for (int i = 0; i < bodies.length; i++) {
                    Aggregate<?> aggregate = (Aggregate<?>) type.getMethod("q" + i).invoke(null);
                    List<Bson> stages = aggregate.getAggregateConditionList();
                    Assert.assertEquals("单次 match 只追加一个 Stage", 3, stages.size());
                    Assert.assertArrayEquals(bytes(BsonDocument.parse("{$skip:7}")), bytes(stages.get(0)));
                    Assert.assertArrayEquals(bytes(BsonDocument.parse(expected[i == 6 ? 3 : i / 2])), bytes(stages.get(1)));
                    Assert.assertArrayEquals(bytes(BsonDocument.parse("{$limit:9}")), bytes(stages.get(2)));
                }
            }
        } finally {
            try (Stream<Path> paths = Files.walk(output)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                    Files.delete(path);
                }
            }
        }
    }

    @Test
    public void numericTypesFieldOrderAndNestedArrayOrderRemainExact() {
        Object[] values = {100, 100L, 100.25D, Decimal128.parse("100.250"), null, true, "$amount", "$$unbound"};
        for (Object value : values) {
            Aggregate<?> a = new AggregateWrapper();
            a.match(Filters.fields(Filters.eq("status", "PAID"), Filters.gte("amount", value)));
            Assert.assertArrayEquals(bytes(new Document("$match", new Document("status", "PAID")
                    .append("amount", new Document("$gte", value)))), bytes(a.getAggregateConditionList().get(0)));
        }
        Bson left = Filters.eq("status", "PAID");
        Bson right = Filters.gte("amount", 100L);
        Assert.assertFalse(Arrays.equals(bytes(Filters.fields(left, right)), bytes(Filters.fields(right, left))));
        Assert.assertFalse(Arrays.equals(bytes(Filters.and(left, right)), bytes(Filters.and(right, left))));
        Assert.assertFalse(Arrays.equals(bytes(Filters.gte("amount", 100)), bytes(Filters.gte("amount", 100L))));
        Assert.assertArrayEquals(bytes(BsonDocument.parse("{$or:[{$and:[{status:'PAID'},{amount:{$gte:NumberLong(100)}}]},{status:'FINISHED'}]}")),
                bytes(Filters.or(Filters.and(left, right), Filters.eq("status", "FINISHED"))));
    }

    @Test
    public void duplicateNullEmptyAndUnknownCodecAreRejected() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> bytes(Filters.fields(Filters.eq("status", "PAID"), Filters.eq("status", "FINISHED"))));
        Assert.assertThrows(IllegalArgumentException.class, () -> bytes(Filters.fields()));
        Assert.assertThrows(NullPointerException.class, () -> bytes(Filters.fields((Bson) null)));
        Assert.assertThrows(NullPointerException.class, () -> Filters.fields((Iterable<Bson>) null));
        Assert.assertThrows(CodecConfigurationException.class, () -> bytes(Filters.gte("amount", new Object())));
    }

    @Test
    public void queryGteAndExpressionGteHaveDifferentTypedBson() {
        Bson query = Filters.gte("amount", 100);
        Bson expression = AggregateOperator.gte("$amount", 100);
        Assert.assertFalse(Arrays.equals(bytes(query), bytes(expression)));
        Assert.assertArrayEquals(bytes(BsonDocument.parse("{$expr:{$gte:['$amount',100]}}")),
                bytes(Filters.expr(expression)));
        // Java 的 Bson 接口不会强制语义角色；正式证据的拒绝由 Indexer 专项负例验证。
        Assert.assertArrayEquals(bytes(BsonDocument.parse("{amount:{$gte:'$other'}}")),
                bytes(Filters.gte("amount", "$other")));
    }

    @Test
    public void fieldsForwardsCallerCodecToEveryChild() {
        org.bson.codecs.Codec<String> codec = new org.bson.codecs.Codec<String>() {
            @Override public void encode(BsonWriter writer, String value, EncoderContext context) {
                writer.writeString("changed:" + value);
            }
            @Override public String decode(BsonReader reader, org.bson.codecs.DecoderContext context) {
                return reader.readString();
            }
            @Override public Class<String> getEncoderClass() { return String.class; }
        };
        org.bson.codecs.configuration.CodecRegistry registry = org.bson.codecs.configuration.CodecRegistries
                .fromRegistries(org.bson.codecs.configuration.CodecRegistries.fromCodecs(codec),
                        MongoClientSettings.getDefaultCodecRegistry());
        Bson value = Filters.fields(Filters.eq("status", "PAID"), Filters.gte("amount", 100));
        Assert.assertEquals(BsonDocument.parse("{status:'changed:PAID',amount:{$gte:100}}"),
                value.toBsonDocument(Document.class, registry));
        Assert.assertFalse(Arrays.equals(bytes(value), bytes(value.toBsonDocument(Document.class, registry))));
    }

    private static byte[] bytes(Bson value) {
        BsonDocument doc = value.toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
        try (BasicOutputBuffer output = new BasicOutputBuffer(); BsonBinaryWriter writer = new BsonBinaryWriter(output)) {
            new BsonDocumentCodec().encode(writer, doc, EncoderContext.builder().build());
            return output.toByteArray();
        }
    }

    private static List<String> compile(String text, Path output) throws Exception {
        Assert.assertEquals("必须在 Java 21 运行", "21", System.getProperty("java.specification.version"));
        JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///QueryActualJava21.java"),
                JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignored) { return text; }
        };
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager files = ToolProvider.getSystemJavaCompiler()
                .getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            JavacTask task = (JavacTask) ToolProvider.getSystemJavaCompiler().getTask(null, files, diagnostics,
                    Arrays.asList("-proc:none", "--release", "21", "-Xlint:all", "-Werror",
                            "-classpath", System.getProperty("java.class.path"),
                            "-d", output.toString()), null, Collections.singletonList(source));
            Iterable<? extends CompilationUnitTree> units = task.parse();
            task.analyze();
            Trees trees = Trees.instance(task);
            List<String> calls = new ArrayList<>();
            for (CompilationUnitTree unit : units) {
                new TreePathScanner<Void, Void>() {
                    @Override public Void visitMethodInvocation(MethodInvocationTree call, Void ignored) {
                        javax.lang.model.element.Element element = trees.getElement(getCurrentPath());
                        if (element instanceof ExecutableElement) {
                            ExecutableElement method = (ExecutableElement) element;
                            calls.add(method.getEnclosingElement() + "." + method.getSimpleName() + "("
                                    + method.getParameters().stream().map(p -> p.asType().toString())
                                    .collect(Collectors.joining(",")) + ")");
                            javax.lang.model.type.TypeMirror bound = trees.getTypeMirror(
                                    new com.sun.source.util.TreePath(getCurrentPath(), call.getMethodSelect()));
                            if (bound instanceof javax.lang.model.type.ExecutableType) {
                                calls.add("BOUND " + method.getEnclosingElement() + "." + method.getSimpleName() + "("
                                        + ((javax.lang.model.type.ExecutableType) bound).getParameterTypes().stream()
                                        .map(Object::toString).collect(Collectors.joining(",")) + ")");
                            }
                        }
                        return super.visitMethodInvocation(call, ignored);
                    }
                }.scan(unit, null);
            }
            task.generate();
            Assert.assertFalse(diagnostics.getDiagnostics().toString(),
                    diagnostics.getDiagnostics().stream().anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR));
            Assert.assertTrue(Files.exists(output.resolve("QueryActualJava21.class")));
            return calls;
        }
    }
}
