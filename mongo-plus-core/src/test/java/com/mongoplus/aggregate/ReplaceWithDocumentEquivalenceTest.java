package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.conditions.operation.ConditionOperators;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.io.BasicOutputBuffer;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.element.ExecutableElement;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** A05 父候选的实际调用树、Java overload 和有序 BSON 验证，不执行 MongoDB 服务端求值。 */
public class ReplaceWithDocumentEquivalenceTest {
    private static final CodecRegistry DEFAULT = MongoClientSettings.getDefaultCodecRegistry();

    @Test
    public void java21CompilationProvesOverloadIdentityAndExecutesCompleteTrees() throws Exception {
        String source = "import com.mongoplus.aggregate.*;"
                + "import com.mongoplus.conditions.operation.ConditionOperators;"
                + "import org.bson.Document;"
                + "public class ReplaceWithActualJava21 {"
                + "public static Object[] direct(){"
                + "Aggregate<?> a=new AggregateWrapper(); a.skip(7);"
                + "Document e=ConditionOperators.mergeObjects(\"$defaults\",\"$profile\");"
                + "Object r=a.replaceWith(e); a.limit(9); return new Object[]{a,r,e};}"
                + "public static Object[] generic(){"
                + "Aggregate<?> a=new AggregateWrapper(); a.skip(7);"
                + "Document e=ConditionOperators.mergeObjects(\"$defaults\",\"$profile\");"
                + "Object r=a.<Object>replaceWith((Object)e); a.limit(9); return new Object[]{a,r,e};}"
                + "public static Object typeWitness(Aggregate<?> a,Document e){"
                + "return a.<Document>replaceWith(e);}"
                + "}";
        Path output = Files.createTempDirectory("replace-with-java21-");
        try {
            List<String> overloads = compile(source, output);
            // 单独的 <Document> 类型实参不会强制选择泛型；必须核验编译器绑定结果。
            Assert.assertEquals(Arrays.asList("org.bson.Document", "TExpression", "org.bson.Document"), overloads);
            try (URLClassLoader loader = new URLClassLoader(new URL[]{output.toUri().toURL()}, getClass().getClassLoader())) {
                Class<?> compiled = Class.forName("ReplaceWithActualJava21", true, loader);
                Object[] direct = (Object[]) compiled.getMethod("direct").invoke(null);
                Object[] generic = (Object[]) compiled.getMethod("generic").invoke(null);
                Aggregate<?> left = (Aggregate<?>) direct[0];
                Aggregate<?> right = (Aggregate<?>) generic[0];
                Assert.assertSame(left, direct[1]);
                Assert.assertSame(right, generic[1]);
                Assert.assertEquals(Document.class, direct[2].getClass());
                Assert.assertEquals(Document.class, generic[2].getClass());
                assertPipeline(left, right, BsonDocument.parse("{$replaceWith:{$mergeObjects:['$defaults','$profile']}}"));
            }
        } finally {
            deleteTemporaryDirectory(output);
        }
    }

    @Test
    public void documentDomainRetainsNestedTypesOrderStageCountAndReceiver() {
        Document expression = ConditionOperators.mergeObjects(new Document("b", 2L).append("a", 1), "$profile");
        Aggregate<?> left = new AggregateWrapper();
        Aggregate<?> right = new AggregateWrapper();
        Aggregate<?> unrelated = new AggregateWrapper();
        left.skip(7);
        right.skip(7);
        Assert.assertSame(left, left.replaceWith(expression));
        Assert.assertSame(right, right.<Object>replaceWith((Object) expression));
        left.limit(9);
        right.limit(9);
        assertPipeline(left, right,
                BsonDocument.parse("{$replaceWith:{$mergeObjects:[{b:NumberLong(2),a:1},'$profile']}}"));
        Assert.assertTrue(unrelated.getAggregateConditionList().isEmpty());
        BsonDocument operand = encode(left.getAggregateConditionList().get(1), DEFAULT)
                .getDocument("$replaceWith").getArray("$mergeObjects").get(0).asDocument();
        Assert.assertEquals(Arrays.asList("b", "a"), new ArrayList<>(operand.keySet()));
        Assert.assertEquals(BsonType.INT64, operand.get("b").getBsonType());
        Assert.assertEquals(BsonType.INT32, operand.get("a").getBsonType());
        Assert.assertEquals(org.bson.codecs.DocumentCodec.class, DEFAULT.get(Document.class).getClass());
        Assert.assertEquals(org.bson.codecs.BsonDocumentCodec.class, DEFAULT.get(BsonDocument.class).getClass());
    }

    @Test
    public void stringUnknownRuntimeAndNullDoNotProveDocumentExpressionDomain() {
        Aggregate<?> string = new AggregateWrapper();
        string.replaceWith("$profile");
        Assert.assertEquals(BsonType.STRING, encode(single(string), DEFAULT).get("$replaceWith").getBsonType());
        Assert.assertFalse(Arrays.equals(bytes(single(string), DEFAULT),
                bytes(BsonDocument.parse("{$replaceWith:{$mergeObjects:['$defaults','$profile']}}"), DEFAULT)));
        Aggregate<?> unknown = new AggregateWrapper();
        unknown.replaceWith(new Object());
        Assert.assertThrows(CodecConfigurationException.class, () -> encode(single(unknown), DEFAULT));
        Aggregate<?> absent = new AggregateWrapper();
        absent.replaceWith((Document) null);
        Assert.assertEquals(BsonType.NULL, encode(single(absent), DEFAULT).get("$replaceWith").getBsonType());
    }

    @Test
    public void bsonStaticTypeSelectsExistingStagePassthroughAndDoesNotWrapExpression() {
        Document expression = ConditionOperators.mergeObjects("$defaults", "$profile");
        Aggregate<?> wrapped = new AggregateWrapper();
        Aggregate<?> passthrough = new AggregateWrapper();
        wrapped.replaceWith(expression);
        Bson staticallyBson = expression;
        passthrough.replaceWith(staticallyBson);
        Assert.assertSame(expression, single(passthrough));
        Assert.assertFalse(encode(single(passthrough), DEFAULT).containsKey("$replaceWith"));
        Assert.assertFalse(Arrays.equals(bytes(single(wrapped), DEFAULT), bytes(single(passthrough), DEFAULT)));
    }

    @Test
    public void subclassAndChangedCodecCannotReuseExactDocumentDefaultCodecProof() {
        Document subclass = new Document("$mergeObjects", Arrays.asList("$defaults", "$profile")) {
            private static final long serialVersionUID = 1L;
            @Override
            public <T> BsonDocument toBsonDocument(Class<T> type, CodecRegistry registry) {
                return new BsonDocument("$unexpected", new BsonString("overridden"));
            }
        };
        Aggregate<?> overridden = new AggregateWrapper();
        overridden.replaceWith(subclass);
        Assert.assertNotEquals(Document.class, subclass.getClass());
        Assert.assertEquals(BsonDocument.parse("{$replaceWith:{$unexpected:'overridden'}}"),
                encode(single(overridden), DEFAULT));
        Codec<String> changedStrings = new Codec<String>() {
            @Override
            public void encode(org.bson.BsonWriter writer, String value, EncoderContext context) {
                writer.writeString("changed:" + value);
            }
            @Override
            public String decode(org.bson.BsonReader reader, DecoderContext context) {
                return reader.readString();
            }
            @Override
            public Class<String> getEncoderClass() {
                return String.class;
            }
        };
        CodecRegistry changed = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(changedStrings), DEFAULT);
        Aggregate<?> normal = new AggregateWrapper();
        normal.replaceWith(ConditionOperators.mergeObjects("$defaults", "$profile"));
        Assert.assertFalse(Arrays.equals(bytes(single(normal), DEFAULT), bytes(single(normal), changed)));
        Assert.assertEquals("changed:$defaults", encode(single(normal), changed).getDocument("$replaceWith")
                .getArray("$mergeObjects").get(0).asString().getValue());
        // BuildersHelper 的 Bson 分支另经 BsonDocumentCodec，输入 DocumentCodec 相同仍不足以证明最终字节。
        Codec<BsonDocument> changedBsonDocuments = new BsonDocumentCodec() {
            @Override
            public void encode(org.bson.BsonWriter writer, BsonDocument value, EncoderContext context) {
                writer.writeStartDocument();
                writer.writeString("$intermediateCodec", "changed");
                writer.writeEndDocument();
            }
        };
        CodecRegistry changedIntermediate = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(changedBsonDocuments), DEFAULT);
        Assert.assertEquals(org.bson.codecs.DocumentCodec.class,
                changedIntermediate.get(Document.class).getClass());
        Assert.assertFalse(Arrays.equals(bytes(single(normal), DEFAULT), bytes(single(normal), changedIntermediate)));
        Assert.assertEquals(BsonDocument.parse("{$replaceWith:{$intermediateCodec:'changed'}}"),
                encode(single(normal), changedIntermediate));
    }

    @Test
    public void changingBsonTypeOrderStageCountOrReceiverBreaksFullTreeIdentity() {
        Document leftExpression = ConditionOperators.mergeObjects(new Document("b", 2L).append("a", 1), "$profile");
        Document wrongType = ConditionOperators.mergeObjects(new Document("b", 2).append("a", 1), "$profile");
        Document wrongOrder = ConditionOperators.mergeObjects(new Document("a", 1).append("b", 2L), "$profile");
        Aggregate<?> left = new AggregateWrapper();
        Aggregate<?> type = new AggregateWrapper();
        Aggregate<?> order = new AggregateWrapper();
        left.replaceWith(leftExpression);
        type.<Object>replaceWith((Object) wrongType);
        order.<Object>replaceWith((Object) wrongOrder);
        Assert.assertFalse(Arrays.equals(bytes(single(left), DEFAULT), bytes(single(type), DEFAULT)));
        Assert.assertFalse(Arrays.equals(bytes(single(left), DEFAULT), bytes(single(order), DEFAULT)));
        Aggregate<?> twice = new AggregateWrapper();
        twice.replaceWith(leftExpression);
        twice.<Object>replaceWith((Object) leftExpression);
        Assert.assertEquals(2, twice.getAggregateConditionList().size());
        Assert.assertNotEquals(left.getAggregateConditionList().size(), twice.getAggregateConditionList().size());
        Aggregate<?> otherReceiver = new AggregateWrapper();
        otherReceiver.<Object>replaceWith((Object) leftExpression);
        Assert.assertNotSame(left, otherReceiver);
    }

    private static List<String> compile(String text, Path output) throws Exception {
        Assert.assertNotNull("该专项测试要求真实 JDK 21 编译器", ToolProvider.getSystemJavaCompiler());
        JavaFileObject source = new SimpleJavaFileObject(
                URI.create("string:///ReplaceWithActualJava21.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignored) {
                return text;
            }
        };
        StringWriter diagnostics = new StringWriter();
        try (StandardJavaFileManager files = ToolProvider.getSystemJavaCompiler()
                .getStandardFileManager(null, null, java.nio.charset.StandardCharsets.UTF_8)) {
            JavacTask task = (JavacTask) ToolProvider.getSystemJavaCompiler().getTask(diagnostics, files, null,
                    Arrays.asList("-proc:none", "--release", "21", "-classpath", System.getProperty("java.class.path"),
                            "-d", output.toString()), null, Collections.singletonList(source));
            Iterable<? extends CompilationUnitTree> units = task.parse();
            task.analyze();
            Trees trees = Trees.instance(task);
            List<String> overloads = new ArrayList<>();
            for (CompilationUnitTree unit : units) {
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethodInvocation(MethodInvocationTree call, Void ignored) {
                        javax.lang.model.element.Element element = trees.getElement(getCurrentPath());
                        if (element instanceof ExecutableElement && element.getSimpleName().contentEquals("replaceWith")) {
                            overloads.add(((ExecutableElement) element).getParameters().get(0).asType().toString());
                        }
                        return super.visitMethodInvocation(call, ignored);
                    }
                }.scan(unit, null);
            }
            task.generate();
            Assert.assertTrue(diagnostics.toString(), Files.isRegularFile(output.resolve("ReplaceWithActualJava21.class")));
            return overloads;
        }
    }

    private static void assertPipeline(Aggregate<?> left, Aggregate<?> right, BsonDocument expectedMiddle) {
        Assert.assertEquals(3, left.getAggregateConditionList().size());
        Assert.assertEquals(3, right.getAggregateConditionList().size());
        BsonDocument[] expected = {BsonDocument.parse("{$skip:7}"), expectedMiddle, BsonDocument.parse("{$limit:9}")};
        for (int index = 0; index < expected.length; index++) {
            Assert.assertArrayEquals(bytes(expected[index], DEFAULT), bytes(left.getAggregateConditionList().get(index), DEFAULT));
            Assert.assertArrayEquals(bytes(expected[index], DEFAULT), bytes(right.getAggregateConditionList().get(index), DEFAULT));
        }
    }

    private static Bson single(Aggregate<?> aggregate) {
        Assert.assertEquals(1, aggregate.getAggregateConditionList().size());
        return aggregate.getAggregateConditionList().get(0);
    }

    private static BsonDocument encode(Bson value, CodecRegistry registry) {
        BsonDocument document = value.toBsonDocument(Document.class, registry);
        document.size();
        return document;
    }

    private static byte[] bytes(Bson value, CodecRegistry registry) {
        try (BasicOutputBuffer buffer = new BasicOutputBuffer(); BsonBinaryWriter writer = new BsonBinaryWriter(buffer)) {
            new BsonDocumentCodec().encode(writer, encode(value, registry), EncoderContext.builder().build());
            return buffer.toByteArray();
        }
    }

    private static void deleteTemporaryDirectory(Path output) throws Exception {
        try (Stream<Path> paths = Files.walk(output)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.delete(path);
            }
        }
    }
}
