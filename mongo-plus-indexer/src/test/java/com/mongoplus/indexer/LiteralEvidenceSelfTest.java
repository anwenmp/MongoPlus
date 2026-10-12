package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/** Literal 数据域及删证据负例；只验证契约准入，不搜索候选或实现 Expression Resolver。 */
public final class LiteralEvidenceSelfTest {
    private static final String OWNER = "com.mongoplus.aggregate.pipeline.AggregateOperator";
    private static int positive;
    private static int negative;

    private LiteralEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]);
        MongoPlusApiIndex index = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).build()).generate();
        Map<String, Object> literal = method(index, OWNER, "literal");
        require(((List<?>) index.asMap().get("requiredCapabilities")).contains("LITERAL_VALUE_CONSTRUCTION_V1"), "独立能力门禁");
        require("literal(Object value)".equals(literal.get("signature")), "唯一真实 Object overload");
        require("PIPELINE_EXPRESSION".equals(literal.get("resultSemanticType")), "Literal 结果可组合为 Expression");
        Map<String, Object> candidate = candidate(literal);
        require("org.bson.Document".equals(candidate.get("javaResultRepresentation")), "Bson 静态返回，Document runtime 结果");
        require(Map.of("operation", "NONE", "count", "ZERO").equals(candidate.get("receiverEffect")), "工厂无 receiver Stage 副作用");
        Map<String, Object> domain = domain(literal);
        require("NONE_RECURSIVELY".equals(domain.get("operatorInterpretation")), "递归操作符解释被禁止");
        require("NONE".equals(domain.get("fieldReferences")) && "NONE".equals(domain.get("variableReferences")), "美元字符串始终为数据");
        require("FORBID".equals(domain.get("nestedExpressionCalls")), "内部不是子表达式调用树");
        require("FORBID".equals(domain.get("unprovedDocumentOrList")), "容器类型不替代构造来源");
        require("org.bson.Document".equals(map(domain.get("documentConstruction")).get("javaType")), "Document 数据构造来源");
        require("java.util.Arrays.asList(T...)".equals(map(domain.get("arrayConstruction")).get("factory")), "数组真实泛型工厂来源");
        // 以下记录表示已逐构造器/工厂、参数、runtime Codec 证明的数据树，不是 raw Map/Bson。
        DataDoc l03 = doc("$gt", array("$amount", 100));
        DataArray l04 = array("$amount", doc("$add", array(1, 2)));
        for (Object value : Arrays.asList("$amount", 100, true, l03, l04, null, 100L, 1.25D, "$$unbound", doc(), array())) {
            require(admit(literal, value, true), "L01-L04 及边界数据的构造和 Codec 准入");
        }
        require(!l03.equals(doc("$gt", array(100, "$amount"))), "数据数组序独立保留");
        require(!doc("a", 1, "b", 2).equals(doc("b", 2, "a", 1)), "数据字段序独立保留");
        require(!array(100).equals(array(100L)), "Java 数值表示不同，不强制等价");
        Map<String, Object> gt = method(index, OWNER, "gt");
        require("PIPELINE_EXPRESSION".equals(parameters(gt).get(0).get("semanticType")), "L05 保持普通 Expression 参数域");
        require("ARRAY_ARGUMENTS_RUNTIME_CODEC".equals(map(map(candidate(gt).get("bsonTerm")).get("value")).get("op")), "L05 保持既有数组构造项");
        refuse(literal, gt, true, "把 $gt 调用节点当 Literal 数据");
        refuse(literal, method(index, "com.mongoplus.toolkit.Filters", "gte"), true, "Query Predicate 不能当 Literal 数据");
        refuse(method(index, "com.mongoplus.toolkit.Filters", "gte"), "$amount", true, "Query 工厂不能充当 Literal 工厂");
        refuse(literal, Map.of("$gt", List.of("$amount", 100)), true, "未经证明的 raw Document/Map");
        refuse(literal, new DataDoc(l03.entries(), false), true, "Document 构造来源缺失");
        refuse(literal, new DataArray(l04.values(), false), true, "List 工厂来源缺失");
        refuse(literal, doc("$gt", new Object()), true, "嵌套未知 Java 类型");
        refuse(literal, l04, false, "已知容器仍缺叶子 Codec");
        refuse(literal, doc("a", 1, "a", 2), true, "重复键未建立原始字节保持");
        refuse(literal, doc("bad\0key", 1), true, "BSON 键含 NUL");
        for (String key : List.of("resultSemanticType", "resultSemanticEvidence", "compositionSemantics", "expressionShape")) {
            Map<String, Object> changed = copy(literal);
            changed.remove(key);
            refuse(changed, l03, true, "缺 " + key);
        }
        for (String key : List.of("sourceEvidence", "parameterBindings", "bsonTerm")) {
            Map<String, Object> changed = copy(literal);
            candidate(changed).remove(key);
            refuse(changed, l04, true, "缺候选 " + key);
        }
        for (String key : List.of("requiredDocumentCodec", "requiredCollectionCodec", "requiredValueCodecProvider",
                "sourceEvidence", "evaluation", "documentConstruction", "arrayConstruction")) {
            Map<String, Object> changed = copy(literal);
            domain(changed).remove(key);
            refuse(changed, l04, true, "缺数据域 " + key);
        }
        Map<String, Object> changed = copy(literal);
        parameters(changed).get(0).put("semanticType", "PIPELINE_EXPRESSION");
        refuse(changed, "$amount", true, "Literal 不能降为普通字段引用参数");
        changed = copy(literal);
        map(map(candidate(changed).get("bsonTerm")).get("value")).put("op", "RUNTIME_CODEC_VALUE");
        refuse(changed, l03, true, "普通 runtime 编码不能替代不递归求值边界");
        Map<String, Object> family = index.getMethodFamilies().stream().map(LiteralEvidenceSelfTest::map)
                .filter(f -> "literal".equals(f.get("name"))).findFirst().orElseThrow();
        require(((List<?>) family.get("overloads")).size() == 1, "没有凭名称新增或选择重载");
        require(candidate.equals(candidate(map(((List<?>) family.get("overloads")).get(0)))), "两视图证据一致");
        sourceFixtures(project);
        System.out.println("LiteralEvidenceSelfTest PASSED: " + positive + " positive; " + negative + " rejection checks; L01-L05; opaque data boundary");
    }

    private record Entry(String key, Object value) { }
    private record DataDoc(List<Entry> entries, boolean constructionProven) { }
    private record DataArray(List<Object> values, boolean constructionProven) { }
    private static DataDoc doc(Object... values) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.length; i += 2) { entries.add(new Entry((String) values[i], values[i + 1])); }
        return new DataDoc(entries, true);
    }
    private static DataArray array(Object... values) { return new DataArray(Arrays.asList(values), true); }

    /** 仅检查发布域的必要条件；数据递归与 Expression 调用递归严格分开。 */
    private static boolean admit(Map<String, Object> method, Object value, boolean codecProven) {
        Map<String, Object> candidate = candidate(method);
        Map<String, Object> domain = domain(method);
        List<Map<String, Object>> parameters = parameters(method);
        if (!codecProven || !"ESTABLISHED".equals(candidate.get("proofStatus")) || parameters.size() != 1
                || !"LITERAL_VALUE".equals(parameters.get(0).get("semanticType"))
                || !parameters.get(0).containsKey("semanticEvidence") || !method.containsKey("resultSemanticEvidence")
                || !"PIPELINE_EXPRESSION".equals(method.get("resultSemanticType"))
                || !List.of("LITERAL_VALUE -> PIPELINE_EXPRESSION").equals(method.get("compositionSemantics"))
                || !"VALUE".equals(method.get("expressionShape"))
                || !(candidate.get("sourceEvidence") instanceof List<?>) || !candidate.containsKey("parameterBindings")
                || !domain.containsKey("sourceEvidence") || !domain.containsKey("documentConstruction")
                || !domain.containsKey("arrayConstruction") || !"OPAQUE_DATA".equals(domain.get("evaluation"))
                || !"org.bson.codecs.DocumentCodec".equals(domain.get("requiredDocumentCodec"))
                || !"org.bson.codecs.CollectionCodec".equals(domain.get("requiredCollectionCodec"))
                || !"org.bson.codecs.ValueCodecProvider".equals(domain.get("requiredValueCodecProvider"))) { return false; }
        Map<String, Object> term = map(candidate.getOrDefault("bsonTerm", Map.of()));
        if (!"LITERAL_DATA_RUNTIME_CODEC".equals(map(term.getOrDefault("value", Map.of())).get("op"))) { return false; }
        return data(value, domain);
    }

    private static boolean data(Object value, Map<String, Object> domain) {
        if (value == null) { return true; }
        if (((List<?>) domain.get("runtimeScalarTypes")).contains(value.getClass().getName())) { return true; }
        if (value instanceof DataDoc document && document.constructionProven()) {
            Set<String> keys = new HashSet<>();
            return document.entries().stream().allMatch(e -> e.key().indexOf(0) < 0 && keys.add(e.key()) && data(e.value(), domain));
        }
        return value instanceof DataArray array && array.constructionProven() && array.values().stream().allMatch(v -> data(v, domain));
    }

    private static void sourceFixtures(Path project) throws Exception {
        String source = Files.readString(project.resolve("mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java"));
        source = source.substring(source.indexOf("    /**"), source.indexOf("    /**", source.indexOf("public static Bson literal")));
        source = "package neutral; import org.bson.Document; import org.bson.conversions.Bson; public class Factory {\n" + source + "}";
        Path root = Files.createTempDirectory("literal-evidence-");
        try {
            Path aggregate = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(aggregate.getParent());
            Files.writeString(aggregate, "package com.mongoplus.aggregate; public interface Aggregate<C> {}");
            Path file = root.resolve("neutral/Factory.java");
            Files.createDirectories(file.getParent());
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder().pipeline(true)
                    .addSourceRoot(root).addExpressionRoot("neutral.Factory").build());
            Files.writeString(file, source);
            require("ESTABLISHED".equals(candidate(method(generator.generate(), "neutral.Factory", "literal")).get("proofStatus")), "独立源码夹具闭合");
            List<String> removals = new ArrayList<>(List.of("@mongoExpression $literal", "@mongoExpressionShape VALUE",
                    "@mongoParam value LITERAL_VALUE VALUE", "@mongoComposition LITERAL_VALUE -> PIPELINE_EXPRESSION"));
            source.lines().filter(line -> line.contains("@mongoCandidateSource")).forEach(removals::add);
            for (String removed : removals) {
                Files.writeString(file, source.replace(removed, ""));
                Map<String, Object> contract = candidate(method(generator.generate(), "neutral.Factory", "literal"));
                reject("NOT_ESTABLISHED".equals(contract.get("proofStatus")) && !contract.containsKey("bsonTerm"), "删源码证据: " + removed);
            }
            Files.writeString(file, source.replace("literal", "renamed"));
            require("ESTABLISHED".equals(candidate(method(generator.generate(), "neutral.Factory", "renamed")).get("proofStatus")), "改名后仍按显式来源建立通用数据项");
            Files.writeString(file, source.substring(0, source.lastIndexOf('}'))
                    + "public static Bson literal(String value){return new Document(\"$literal\",value);}}");
            Map<String, Object> sibling = generator.generate().list("types").stream().map(LiteralEvidenceSelfTest::map)
                    .filter(t -> "neutral.Factory".equals(t.get("qualifiedName")))
                    .flatMap(t -> ((List<?>) t.get("publicMethods")).stream()).map(LiteralEvidenceSelfTest::map)
                    .filter(m -> "literal(String value)".equals(m.get("signature"))).findFirst().orElseThrow();
            reject(!sibling.containsKey("candidateSemantics"), "同名相邻 overload 不继承 Literal 证据");
            for (String token : List.of("documentCodec=org.bson.codecs.DocumentCodec", "collectionCodec=org.bson.codecs.CollectionCodec",
                    "valueCodecProvider=org.bson.codecs.ValueCodecProvider", "evaluation=OPAQUE_DATA")) {
                Files.writeString(file, source.replace(token, token.substring(0, token.indexOf('=') + 1) + "UNKNOWN"));
                Map<String, Object> contract = candidate(method(generator.generate(), "neutral.Factory", "literal"));
                reject("NOT_ESTABLISHED".equals(contract.get("proofStatus")) && !contract.containsKey("bsonTerm"), "未知 Codec 或求值语义不得闭合");
            }
            for (String replacement : List.of("String value", "unknown.Type value", "Object... value")) {
                Files.writeString(file, source.replace("Object value)", replacement + ")"));
                boolean rejected;
                try { rejected = !"ESTABLISHED".equals(candidate(method(generator.generate(), "neutral.Factory", "literal")).get("proofStatus")); }
                catch (IllegalArgumentException expected) { rejected = true; }
                reject(rejected, "错误 Java 参数表示不能闭合");
            }
            Files.writeString(file, source.replaceAll("(?m)^\\s*\\* @mongoCandidateSource [^\\r\\n]*\\r?\\n", ""));
            boolean rejected = false;
            try { generator.generate(); } catch (IllegalArgumentException expected) { rejected = true; }
            reject(rejected, "无 sourceEvidence 拒绝生成");
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static Map<String, Object> method(MongoPlusApiIndex index, String owner, String name) {
        return index.list("types").stream().map(LiteralEvidenceSelfTest::map).filter(t -> owner.equals(t.get("qualifiedName")))
                .flatMap(t -> ((List<?>) t.get("publicMethods")).stream()).map(LiteralEvidenceSelfTest::map)
                .filter(m -> name.equals(m.get("name"))).findFirst().orElseThrow();
    }
    private static Map<String, Object> candidate(Map<String, Object> method) { return map(method.getOrDefault("candidateSemantics", Map.of())); }
    private static Map<String, Object> domain(Map<String, Object> method) { return map(map(candidate(method).getOrDefault("applicability", Map.of())).getOrDefault("literalValueDomain", Map.of())); }
    @SuppressWarnings("unchecked") private static List<Map<String, Object>> parameters(Map<String, Object> method) { return (List<Map<String, Object>>) method.getOrDefault("parameters", List.of()); }
    @SuppressWarnings("unchecked") private static Map<String, Object> map(Object value) { return (Map<String, Object>) value; }
    @SuppressWarnings("unchecked") private static <T> T copy(T value) {
        if (value instanceof Map<?, ?> original) {
            Map<Object, Object> result = new LinkedHashMap<>(); original.forEach((k, v) -> result.put(k, copy(v))); return (T) result;
        }
        if (value instanceof List<?> original) { return (T) original.stream().map(LiteralEvidenceSelfTest::copy).toList(); }
        return value;
    }
    private static void refuse(Map<String, Object> method, Object value, boolean codecProven, String message) { reject(!admit(method, value, codecProven), message); }
    private static void require(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } positive++; }
    private static void reject(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } negative++; }
}
