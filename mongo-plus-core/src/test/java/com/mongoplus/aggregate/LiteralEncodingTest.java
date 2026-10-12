package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.aggregate.pipeline.AggregateOperator;
import com.mongoplus.aggregate.pipeline.Projections;
import com.mongoplus.toolkit.Filters;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
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

/** L01-L05：真实 Java 21 调用绑定及 Driver 严格 BSON；不执行服务器。 */
public class LiteralEncodingTest {
    @Test
    public void java21BindsAndExecutesL01ThroughL05() throws Exception {
        String[] bodies = {
                "Projections.computed(\"raw\",AggregateOperator.literal(\"$amount\"))",
                "Projections.fields(Projections.computed(\"fixed\",AggregateOperator.literal(100)),"
                        + "Projections.computed(\"enabled\",AggregateOperator.literal(true)))",
                "Projections.computed(\"value\",AggregateOperator.literal(new Document(\"$gt\",Arrays.asList(\"$amount\",100))))",
                "Projections.computed(\"values\",AggregateOperator.literal(Arrays.asList(\"$amount\",new Document(\"$add\",Arrays.asList(1,2)))))",
                "Projections.computed(\"comparison\",AggregateOperator.gt(\"$amount\",100))",
                "Projections.fields(Arrays.asList(Projections.computed(\"fixed\",AggregateOperator.literal(100)),"
                        + "Projections.computed(\"enabled\",AggregateOperator.literal(true))))"
        };
        String[] expected = {
                "{$project:{raw:{$literal:'$amount'}}}",
                "{$project:{fixed:{$literal:100},enabled:{$literal:true}}}",
                "{$project:{value:{$literal:{$gt:['$amount',100]}}}}",
                "{$project:{values:{$literal:['$amount',{$add:[1,2]}]}}}",
                "{$project:{comparison:{$gt:['$amount',100]}}}",
                "{$project:{fixed:{$literal:100},enabled:{$literal:true}}}"
        };
        StringBuilder source = new StringBuilder("import com.mongoplus.aggregate.*;"
                + "import com.mongoplus.aggregate.pipeline.*;import org.bson.Document;import java.util.*;"
                + "public class LiteralActualJava21 {");
        for (int i = 0; i < bodies.length; i++) {
            source.append("public static Aggregate<?> l").append(i).append("(){Aggregate<?> a=new AggregateWrapper();"
                    + "a.skip(7);if(a.project(").append(bodies[i]).append(")!=a)throw new AssertionError();"
                    + "a.limit(9);return a;}");
        }
        // 对 Document.append 的来源和返回当前 receiver 做额外的实际编译验证。
        source.append("public static Document data(){Document d=new Document();"
                + "if(d.append(\"$gt\",Arrays.asList(\"$amount\",100))!=d)throw new AssertionError();return d;}}");
        Path output = Files.createTempDirectory("literal-java21-");
        try {
            List<String> calls = compile(source.toString(), output);
            Assert.assertEquals(7, Collections.frequency(calls,
                    "com.mongoplus.aggregate.pipeline.AggregateOperator.literal(java.lang.Object)"));
            Assert.assertEquals(1, Collections.frequency(calls,
                    "com.mongoplus.aggregate.pipeline.AggregateOperator.gt(java.lang.Object,java.lang.Object)"));
            Assert.assertEquals(6, Collections.frequency(calls,
                    "com.mongoplus.aggregate.Aggregate.project(org.bson.conversions.Bson)"));
            for (String signature : Arrays.asList("org.bson.Document.<init>(java.lang.String,java.lang.Object)",
                    "org.bson.Document.<init>()", "org.bson.Document.append(java.lang.String,java.lang.Object)",
                    "java.util.Arrays.asList(T[])",
                    "com.mongoplus.aggregate.pipeline.Projections.fields(org.bson.conversions.Bson[])",
                    "com.mongoplus.aggregate.pipeline.Projections.fields(java.util.List<? extends org.bson.conversions.Bson>)",
                    "com.mongoplus.aggregate.pipeline.Projections.computed(java.lang.String,TExpression)")) {
                Assert.assertTrue(calls.toString(), calls.contains(signature));
            }
            Assert.assertTrue(calls.toString(), calls.contains("BOUND com.mongoplus.aggregate.pipeline.Projections.computed(java.lang.String,org.bson.conversions.Bson)"));
            try (URLClassLoader loader = new URLClassLoader(new URL[]{output.toUri().toURL()}, getClass().getClassLoader())) {
                Class<?> type = Class.forName("LiteralActualJava21", true, loader);
                for (int i = 0; i < bodies.length; i++) {
                    Aggregate<?> aggregate = (Aggregate<?>) type.getMethod("l" + i).invoke(null);
                    List<Bson> stages = aggregate.getAggregateConditionList();
                    Assert.assertEquals(3, stages.size());
                    Assert.assertArrayEquals(bytes(BsonDocument.parse("{$skip:7}")), bytes(stages.get(0)));
                    Assert.assertArrayEquals("L0" + (i + 1), bytes(BsonDocument.parse(expected[i])), bytes(stages.get(1)));
                    Assert.assertArrayEquals(bytes(BsonDocument.parse("{$limit:9}")), bytes(stages.get(2)));
                }
                Assert.assertArrayEquals(bytes(new Document("$gt", Arrays.asList("$amount", 100))),
                        bytes((Document) type.getMethod("data").invoke(null)));
            }
        } finally {
            try (Stream<Path> paths = Files.walk(output)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) { Files.delete(path); }
            }
        }
    }

    @Test
    public void literalKeepsTypesValuesAndAllOrders() {
        Assert.assertEquals("java.util.Arrays$ArrayList", Arrays.asList(1, 2).getClass().getName());
        Object[] values = {"$amount", "$$unbound", 100, 100L, 1.25D, true, null, Decimal128.parse("100.250")};
        for (Object value : values) {
            Assert.assertArrayEquals(bytes(new Document("$literal", value)), bytes(AggregateOperator.literal(value)));
        }
        Document data = new Document("z", Arrays.asList("$amount", new Document("$add", Arrays.asList(1, 2)), null))
                .append("_id", 100L).append("$gt", true).append("empty", new Document());
        BsonDocument encoded = AggregateOperator.literal(data)
                .toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry()).getDocument("$literal");
        Assert.assertEquals(Arrays.asList("z", "_id", "$gt", "empty"), new ArrayList<>(encoded.keySet()));
        Assert.assertTrue(encoded.get("_id").isInt64());
        Assert.assertTrue(encoded.getArray("z").get(2).isNull());
        Assert.assertEquals("$amount", encoded.getArray("z").get(0).asString().getValue());
        Assert.assertArrayEquals(bytes(BsonDocument.parse("{$literal:{z:['$amount',{$add:[1,2]},null],_id:NumberLong(100),$gt:true,empty:{}}}")),
                bytes(AggregateOperator.literal(data)));
        Assert.assertFalse(Arrays.equals(bytes(AggregateOperator.literal(100)), bytes(AggregateOperator.literal(100L))));
        Assert.assertFalse(Arrays.equals(bytes(AggregateOperator.literal(Arrays.asList(1, 2))),
                bytes(AggregateOperator.literal(Arrays.asList(2, 1)))));
        Assert.assertFalse(Arrays.equals(bytes(AggregateOperator.literal(new Document("a", 1).append("b", 2))),
                bytes(AggregateOperator.literal(new Document("b", 2).append("a", 1)))));
        Assert.assertArrayEquals(bytes(BsonDocument.parse("{$literal:[]}")), bytes(AggregateOperator.literal(Arrays.asList())));
    }

    @Test
    public void fieldQueryAndNestedExpressionAreDifferentBytes() {
        Bson literal = Projections.computed("raw", AggregateOperator.literal("$amount"));
        Assert.assertFalse(Arrays.equals(bytes(literal), bytes(Projections.computed("raw", "$amount"))));
        Document data = new Document("$gt", Arrays.asList("$amount", 100));
        Assert.assertFalse(Arrays.equals(bytes(AggregateOperator.literal(data)), bytes(AggregateOperator.gt("$amount", 100))));
        Assert.assertFalse(Arrays.equals(bytes(AggregateOperator.literal(data)), bytes(AggregateOperator.literal(true))));
        Assert.assertFalse(Arrays.equals(bytes(AggregateOperator.literal(Arrays.asList("$amount", new Document("$add", Arrays.asList(1, 2))))),
                bytes(AggregateOperator.literal(Arrays.asList("$amount", 3)))));
        Assert.assertFalse(Arrays.equals(bytes(AggregateOperator.literal(data)), bytes(Filters.gte("amount", 100))));
        Assert.assertArrayEquals(bytes(BsonDocument.parse("{$literal:{$literal:1}}")),
                bytes(AggregateOperator.literal(new Document("$literal", 1))));
    }

    @Test
    public void unknownLeafCodecFailsEvenInsideDocumentAndArray() {
        for (Object value : Arrays.asList(new Object(), new Document("$gt", new Object()),
                Arrays.asList("$amount", new Document("$add", Arrays.asList(1, new Object()))))) {
            Assert.assertThrows(CodecConfigurationException.class, () -> bytes(AggregateOperator.literal(value)));
        }
    }

    @Test
    public void customCodecAndMutableInputNeedIndependentProof() {
        org.bson.codecs.Codec<String> codec = new org.bson.codecs.Codec<String>() {
            @Override public void encode(BsonWriter writer, String value, EncoderContext context) { writer.writeString("changed:" + value); }
            @Override public String decode(BsonReader reader, org.bson.codecs.DecoderContext context) { return reader.readString(); }
            @Override public Class<String> getEncoderClass() { return String.class; }
        };
        org.bson.codecs.configuration.CodecRegistry registry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
                org.bson.codecs.configuration.CodecRegistries.fromCodecs(codec), MongoClientSettings.getDefaultCodecRegistry());
        Document data = new Document("$gt", Arrays.asList("$amount", 100));
        Bson value = AggregateOperator.literal(data);
        Assert.assertEquals("changed:$amount", value.toBsonDocument(Document.class, registry)
                .getDocument("$literal").getArray("$gt").get(0).asString().getValue());
        byte[] before = bytes(value);
        data.append("later", true);
        Assert.assertFalse("API 没有快照复制；证据必须固定输入", Arrays.equals(before, bytes(value)));
    }

    private static byte[] bytes(Bson value) {
        BsonDocument document = value.toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
        try (BasicOutputBuffer output = new BasicOutputBuffer(); BsonBinaryWriter writer = new BsonBinaryWriter(output)) {
            new BsonDocumentCodec().encode(writer, document, EncoderContext.builder().build());
            return output.toByteArray();
        }
    }

    private static List<String> compile(String text, Path output) throws Exception {
        Assert.assertEquals("21", System.getProperty("java.specification.version"));
        JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///LiteralActualJava21.java"), JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignored) { return text; }
        };
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager files = ToolProvider.getSystemJavaCompiler().getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            JavacTask task = (JavacTask) ToolProvider.getSystemJavaCompiler().getTask(null, files, diagnostics,
                    Arrays.asList("-proc:none", "--release", "21", "-Xlint:all", "-Werror", "-classpath",
                            System.getProperty("java.class.path"), "-d", output.toString()), null, Collections.singletonList(source));
            Iterable<? extends CompilationUnitTree> units = task.parse();
            task.analyze();
            Trees trees = Trees.instance(task);
            List<String> calls = new ArrayList<>();
            for (CompilationUnitTree unit : units) {
                new TreePathScanner<Void, Void>() {
                    private void record() {
                        javax.lang.model.element.Element element = trees.getElement(getCurrentPath());
                        if (element instanceof ExecutableElement) {
                            ExecutableElement method = (ExecutableElement) element;
                            calls.add(method.getEnclosingElement() + "." + method.getSimpleName() + "("
                                    + method.getParameters().stream().map(p -> p.asType().toString()).collect(Collectors.joining(",")) + ")");
                            if (getCurrentPath().getLeaf() instanceof MethodInvocationTree) {
                                MethodInvocationTree invocation = (MethodInvocationTree) getCurrentPath().getLeaf();
                                javax.lang.model.type.TypeMirror bound = trees.getTypeMirror(new com.sun.source.util.TreePath(getCurrentPath(), invocation.getMethodSelect()));
                                if (bound instanceof javax.lang.model.type.ExecutableType) {
                                    calls.add("BOUND " + method.getEnclosingElement() + "." + method.getSimpleName() + "("
                                            + ((javax.lang.model.type.ExecutableType) bound).getParameterTypes().stream()
                                            .map(Object::toString).collect(Collectors.joining(",")) + ")");
                                }
                            }
                        }
                    }
                    @Override public Void visitMethodInvocation(MethodInvocationTree call, Void ignored) { record(); return super.visitMethodInvocation(call, ignored); }
                    @Override public Void visitNewClass(NewClassTree call, Void ignored) { record(); return super.visitNewClass(call, ignored); }
                }.scan(unit, null);
            }
            task.generate();
            Assert.assertFalse(diagnostics.getDiagnostics().toString(), diagnostics.getDiagnostics().stream().anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR));
            Assert.assertTrue(Files.exists(output.resolve("LiteralActualJava21.class")));
            return calls;
        }
    }
}
