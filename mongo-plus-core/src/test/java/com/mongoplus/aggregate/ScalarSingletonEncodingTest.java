package com.mongoplus.aggregate;

import com.mongodb.MongoClientSettings;
import com.mongoplus.aggregate.pipeline.Facet;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonType;
import org.junit.Assert;
import org.junit.Test;

/** 固定真实 Java 重载、BSON 类型和容器压缩；服务端合法性不冒充 Core 校验。 */
public class ScalarSingletonEncodingTest {
    @Test
    public void skipOverloadsAlwaysEncodeInt32IncludingZeroAndCoreNegativeValues() {
        for (int value : new int[] {0, 2, Integer.MAX_VALUE, -1, Integer.MIN_VALUE}) {
            Aggregate<?> ints = new AggregateWrapper();
            Aggregate<?> longs = new AggregateWrapper();
            Assert.assertSame(ints, ints.skip(value));
            Assert.assertSame(longs, longs.skip((long) value));
            Assert.assertEquals(new BsonDocument("$skip", new BsonInt32(value)), single(ints));
            Assert.assertEquals(single(ints), single(longs));
            Assert.assertEquals(BsonType.INT32, single(longs).get("$skip").getBsonType());
            Assert.assertNotEquals(new BsonDocument("$skip", new BsonInt64(value)), single(longs));
        }
    }

    @Test
    public void longSkipOverflowNeverAppendsOrRounds() {
        for (long value : new long[] {(long) Integer.MAX_VALUE + 1, (long) Integer.MIN_VALUE - 1,
                Long.MAX_VALUE, Long.MIN_VALUE, 9007199254740993L}) {
            Aggregate<?> aggregate = new AggregateWrapper();
            aggregate.skip(2);
            Assert.assertThrows(ArithmeticException.class, () -> aggregate.skip(value));
            Assert.assertEquals(new BsonDocument("$skip", new BsonInt32(2)), single(aggregate));
        }
    }

    @Test
    public void fractionalSkipAndWrongTypedFieldsDoNotCompile() throws Exception {
        verifyJava("a.skip(2); a.skip(2L); a.unset(\"name\"); a.unset(new String[] {\"name\", \"age\"});"
                + "a.unset(java.util.Arrays.asList(\"name\", \"age\"));", true);
        verifyJava("a.skip(2.5);", false);
        verifyJava("a.unset(java.util.Arrays.asList(1, 2));", false);
        verifyJava("a.unset(new Integer[] {1, 2});", false);
    }

    @Test
    public void unsetStringArrayAndListUseSameCardinalityEncoding() {
        for (String[] fields : new String[][] {{"name"}, {"name", "age"}, {"name", "name"}, {},
                {"profile.name"}, {"name", "name.child"}, {""}, {"$name"}}) {
            Aggregate<?> array = new AggregateWrapper();
            Aggregate<?> list = new AggregateWrapper();
            Assert.assertSame(array, array.unset(fields));
            Assert.assertSame(list, list.unset(Arrays.asList(fields)));
            BsonArray values = new BsonArray();
            for (String field : fields) { values.add(new BsonString(field)); }
            BsonDocument expected = new BsonDocument("$unset", fields.length == 1 ? values.get(0) : values);
            Assert.assertEquals(expected, single(array));
            Assert.assertEquals(expected, single(list));
        }
        Aggregate<?> scalar = new AggregateWrapper();
        scalar.unset("name");
        Assert.assertEquals(BsonDocument.parse("{$unset:'name'}"), single(scalar));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void erasedWrongElementsAndNullFailBeforeAppend() {
        Aggregate<?> aggregate = new AggregateWrapper();
        Assert.assertThrows(ClassCastException.class, () -> aggregate.unset((java.util.List) Arrays.asList("name", 2)));
        Assert.assertThrows(IllegalArgumentException.class, () -> aggregate.unset(Collections.singletonList((String) null)));
        Assert.assertThrows(NullPointerException.class, () -> aggregate.unset((String[]) null));
        Assert.assertTrue(aggregate.getAggregateConditionList().isEmpty());
    }

    @Test
    public void sortByCountKeepsStringExpressionUnchanged() {
        for (String value : new String[] {"$name", "name", "$$item.name"}) {
            Aggregate<?> aggregate = new AggregateWrapper();
            Assert.assertSame(aggregate, aggregate.sortByCount(value));
            Assert.assertEquals(new BsonDocument("$sortByCount", new BsonString(value)), single(aggregate));
        }
    }

    @Test
    public void getterOverloadsKeepTheirIndependentFieldConversion() {
        com.mongoplus.support.SFunction<Fields, String> name = Fields::getName;
        com.mongoplus.support.SFunction<Fields, String> age = Fields::getAge;
        Aggregate<?> varargs = new AggregateWrapper();
        varargs.unset(name, age);
        Aggregate<?> list = new AggregateWrapper();
        list.unsetLambda(Arrays.asList(name, age));
        Assert.assertEquals(BsonDocument.parse("{$unset:['name','age']}"), single(varargs));
        Assert.assertEquals(single(varargs), single(list));
        Aggregate<?> scalar = new AggregateWrapper();
        scalar.unset(name);
        Assert.assertEquals(BsonDocument.parse("{$unset:'name'}"), single(scalar));
        Aggregate<?> sort = new AggregateWrapper();
        sort.sortByCount(name);
        Assert.assertEquals(BsonDocument.parse("{$sortByCount:'$name'}"), single(sort));
    }

    public static class Fields {
        private String name;
        private String age;
        public String getName() { return name; }
        public String getAge() { return age; }
    }

    @Test
    public void nestedReceiversKeepSkipUnsetOrderAndIsolation() {
        Aggregate<?> inner = new AggregateWrapper();
        inner.skip(2);
        inner.unset("name");
        inner.unset(Arrays.asList("name", "age"));
        BsonArray expected = BsonArray.parse("[{$skip:2},{$unset:'name'},{$unset:['name','age']}]");
        Aggregate<?> sibling = new AggregateWrapper();
        sibling.skip(0L);
        Aggregate<?> facet = new AggregateWrapper();
        facet.facet(new Facet("rows", inner), new Facet("other", sibling));
        Assert.assertEquals(expected, single(facet).getDocument("$facet").getArray("rows"));
        Assert.assertEquals(BsonArray.parse("[{$skip:0}]"), single(facet).getDocument("$facet").getArray("other"));
        Aggregate<?> lookup = new AggregateWrapper();
        lookup.lookup("orders", inner, "rows");
        Assert.assertEquals(expected, single(lookup).getDocument("$lookup").getArray("pipeline"));
        Aggregate<?> union = new AggregateWrapper();
        union.unionWith("history", inner);
        Assert.assertEquals(expected, single(union).getDocument("$unionWith").getArray("pipeline"));
        Assert.assertEquals(3, inner.getAggregateConditionList().size());
        Assert.assertEquals(1, sibling.getAggregateConditionList().size());
        Assert.assertNotSame(inner.getAggregateConditionList(), union.getAggregateConditionList());
    }

    private static BsonDocument single(Aggregate<?> aggregate) {
        Assert.assertEquals(1, aggregate.getAggregateConditionList().size());
        return aggregate.getAggregateConditionList().get(0).toBsonDocument(BsonDocument.class,
                MongoClientSettings.getDefaultCodecRegistry());
    }

    private static void verifyJava(String statement, boolean expectedSuccess) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assert.assertNotNull("Java overload 验证需要完整 JDK", compiler);
        Path root = Files.createTempDirectory("scalar-singleton-java-");
        Path source = root.resolve("InvalidCall.java");
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            Files.write(source, ("import com.mongoplus.aggregate.Aggregate; class InvalidCall {"
                    + "void test(Aggregate<?> a) {" + statement + "}}").getBytes(StandardCharsets.UTF_8));
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            boolean success = compiler.getTask(null, manager, diagnostics,
                    Arrays.asList("-proc:none", "-classpath", System.getProperty("java.class.path"), "-d", root.toString()),
                    null, manager.getJavaFileObjects(source.toFile())).call();
            Assert.assertEquals(statement, expectedSuccess, success);
            if (!expectedSuccess) {
                Assert.assertTrue(statement, diagnostics.getDiagnostics().stream()
                        .anyMatch(item -> item.getKind() == javax.tools.Diagnostic.Kind.ERROR));
            }
        } finally {
            Files.deleteIfExists(root.resolve("InvalidCall.class"));
            Files.deleteIfExists(source);
            Files.deleteIfExists(root);
        }
    }
}
