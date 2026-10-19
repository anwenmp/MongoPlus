package com.mongoplus.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiIndexerTest {
    @TempDir
    Path temporary;

    @Test
    void extractsDeclarationsWithoutInventingMembers() throws Exception {
        Path root = temporary.resolve("repo");
        write(root.resolve("pom.xml"), "<project><modules><module>library</module></modules></project>");
        write(root.resolve("library/pom.xml"), "<project/>");
        write(root.resolve("library/src/main/java/p/Api.java"), """
                package p;
                /** API docs */
                @Deprecated public interface Api<T> extends Comparable<T> {
                    /** map docs */
                    <R extends Number> R map(String value) throws Exception;
                    void map(String... values);
                    private void helper() {}
                    class Nested {
                        public Nested(int value) {}
                        private Nested() {}
                        public void run() { Runnable r = new Runnable() { public void hidden() {} public void run() {} }; }
                    }
                }
                @interface Flag { String value() default "yes"; }
                enum State implements Runnable { READY; public void run() {} }
                """);
        write(root.resolve("library/src/test/java/Broken.java"), "invalid test source");
        write(root.resolve("library/target/generated-sources/Broken.java"), "invalid generated source");
        write(root.resolve("tools/ignored/src/main/java/Broken.java"), "invalid tool source");
        Path output = temporary.resolve("output");
        new ApiIndexer().generate(root, output);
        List<JsonNode> rows = read(output);
        assertEquals(4, rows.stream().filter(r -> r.path("recordType").asText().equals("type")).count());
        var maps = rows.stream().filter(r -> r.path("name").asText().equals("map")).toList();
        assertEquals(2, maps.size());
        assertNotEquals(maps.get(0).get("id"), maps.get(1).get("id"));
        assertEquals("public", maps.get(0).path("visibility").asText());
        assertEquals("<R extends Number> R map(String value) throws Exception", maps.get(0).path("signature").asText());
        assertEquals("value", maps.get(0).path("parameters").get(0).path("name").asText());
        assertTrue(maps.get(1).path("parameters").get(0).path("varArgs").asBoolean());
        assertEquals(2, rows.stream().filter(r -> r.path("recordType").asText().equals("constructor")).count());
        assertEquals(1, readFile(output.resolve("api-index.jsonl")).stream()
                .filter(r -> r.path("recordType").asText().equals("constructor")).count());
        assertTrue(rows.stream().anyMatch(r -> r.path("qualifiedName").asText().equals("p.Api.Nested")));
        assertFalse(rows.stream().anyMatch(r -> r.path("name").asText().equals("hidden")));
        assertTrue(rows.stream().anyMatch(r -> r.path("name").asText().equals("helper") && r.path("visibility").asText().equals("private")));
        assertTrue(Files.readString(output.resolve("api-index.md")).contains("失败 0"));
    }

    @Test
    void filtersMembersAndEveryEnclosingTypeButPreservesSourceRecords() throws Exception {
        Path root = temporary.resolve("repo");
        write(root.resolve("pom.xml"), "<project/>");
        write(root.resolve("src/main/java/p/Api.java"), """
                package p;
                public class Api {
                    public Api() {} protected Api(int n) {} private Api(String s) {} Api(long n) {}
                    public void call(int n) {} public void call(String s) {}
                    protected void hook() {} private void secret() {} void internal() {}
                    public int visible, array[];
                    protected String inherited;
                    private int hidden; int local;
                    private class Hidden { public class Deep { public void leak() {} } }
                    class PackageNested { public void leak() {} }
                    protected class Extension { protected void hook() {} }
                    public interface Contract { int LIMIT = 1; void apply(); private void helper() {} }
                }
                class Internal { public void leak() {} public Internal() {} }
                """);
        Path output = temporary.resolve("output");
        new ApiIndexer().generate(root, output);
        var source = read(output);
        var api = readFile(output.resolve("api-index.jsonl"));
        // 7 个类型 + 5 个构造方法 + 11 个方法 + 6 个字段；不生成隐式构造方法。
        assertEquals(29, source.size());
        // 可访问声明：3 个类型 + 2 个构造方法 + 5 个方法 + 4 个字段。
        assertEquals(14, api.size());
        assertTrue(source.containsAll(api));
        assertTrue(api.stream().allMatch(r -> List.of("public", "protected").contains(r.path("visibility").asText())));
        assertFalse(api.stream().anyMatch(r -> r.path("name").asText().equals("leak")));
        assertTrue(source.stream().anyMatch(r -> r.path("name").asText().equals("leak")));
        assertEquals(2, api.stream().filter(r -> r.path("recordType").asText().equals("constructor")).count());
        assertEquals(5, source.stream().filter(r -> r.path("recordType").asText().equals("constructor")).count());
        var calls = api.stream().filter(r -> r.path("name").asText().equals("call")).toList();
        assertEquals(2, calls.size());
        assertNotEquals(calls.get(0).get("id"), calls.get(1).get("id"));
        assertTrue(api.stream().anyMatch(r -> r.path("name").asText().equals("array") && r.path("type").asText().equals("int[]")));
        assertTrue(api.stream().anyMatch(r -> r.path("name").asText().equals("LIMIT") && r.path("visibility").asText().equals("public")));
        assertTrue(Files.readString(output.resolve("index.md")).contains("modules/_root/index.md"));
        assertEquals(Files.readString(output.resolve("api-index.md")), Files.readString(output.resolve("modules/_root/index.md")));
    }

    @Test
    void printsOnlyAnnotationExpressionsAndValidIndependentJsonLines() throws Exception {
        Path root = temporary.resolve("repo");
        write(root.resolve("pom.xml"), "<project/>");
        write(root.resolve("src/main/java/Api.java"), """
                /** docs */
                // adjacent type comment
                @Deprecated
                public class Api {
                    // adjacent field comment
                    @SuppressWarnings({"http://example", "/*literal*/"}) public String value;
                    // adjacent method comment
                    @Deprecated public void call(
                        /* adjacent parameter comment */ @Deprecated String text) {}
                }
                """);
        Path output = temporary.resolve("output");
        new ApiIndexer().generate(root, output);
        var rows = read(output);
        assertEquals(3, rows.size());
        for (var row : rows) {
            assertFalse(row.path("annotations").toString().contains("adjacent"));
            assertFalse(row.path("annotations").toString().contains("docs"));
            assertEquals(1, row.path("annotations").size());
        }
        var field = rows.stream().filter(r -> r.path("recordType").asText().equals("field")).findFirst().orElseThrow();
        assertEquals("@SuppressWarnings({ \"http://example\", \"/*literal*/\" })", field.path("annotations").get(0).asText());
        var method = rows.stream().filter(r -> r.path("recordType").asText().equals("method")).findFirst().orElseThrow();
        assertEquals("@Deprecated", method.path("parameters").get(0).path("annotations").get(0).asText());
        for (String filename : List.of("source-index.jsonl", "api-index.jsonl")) {
            var mapper = new ObjectMapper().enable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
            var lines = Files.readAllLines(output.resolve(filename));
            assertEquals(3, lines.size());
            for (String line : lines) {
                assertTrue(mapper.readTree(line).isObject());
            }
        }
    }

    @Test
    void reportsParseFailureAndKeepsSuccessfulFiles() throws Exception {
        Path root = temporary.resolve("repo");
        write(root.resolve("pom.xml"), "<project/>");
        write(root.resolve("src/main/java/Good.java"), "public class Good {}");
        write(root.resolve("src/main/java/Bad.java"), "public class Bad { void broken( }");
        Path output = temporary.resolve("output");
        new ApiIndexer().generate(root, output);
        assertEquals(1, read(output).size());
        assertTrue(Files.readString(output.resolve("api-index.md")).contains("扫描 2；成功 1；失败 1"));
    }

    private static void write(Path path, String content) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static List<JsonNode> read(Path output) throws Exception {
        return readFile(output.resolve("source-index.jsonl"));
    }

    private static List<JsonNode> readFile(Path path) throws Exception {
        List<JsonNode> rows = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (String line : Files.readAllLines(path)) {
            rows.add(mapper.readTree(line));
        }
        return rows;
    }
}
