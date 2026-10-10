package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/** 查询证据准入专项；通用构造项解释器仅用于上游验证，不实现 Resolver 或候选选择。 */
public final class QueryPredicateEvidenceSelfTest {
    private static final String OWNER = "com.mongoplus.toolkit.Filters";
    private static int positive;
    private static int negative;
    private static MongoPlusApiIndex index;

    private QueryPredicateEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]);
        index = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).build()).generate();
        require(((List<?>) index.asMap().get("requiredCapabilities")).contains("QUERY_PREDICATE_CONSTRUCTION_V1"),
                "查询能力必须独立声明");
        List<Map<String, Object>> query = methods(index).stream()
                .filter(m -> "QUERY_PREDICATE".equals(m.get("resultSemanticType"))).toList();
        require(query.size() == 8, "两个单字段工厂及三个双 overload 组合器");
        for (Map<String, Object> method : query) {
            require(((List<?>) method.get("mongoExpressions")).isEmpty(), "Query 不是 Expression 映射");
            require("ESTABLISHED".equals(candidate(method).get("proofStatus")), "逐 overload 构造闭合");
            require(parameters(method).size() == ((List<?>) candidate(method).get("parameterBindings")).size(),
                    "每个参数都有真实 Java 表示和绑定");
            mutations(method);
        }
        Call eq = call("eq", false, "status", "PAID");
        Call gte = call("gte", false, "amount", 100);
        Doc q01 = doc("status", "PAID", "amount", doc("$gte", 100));
        Doc q02 = doc("$and", List.of(doc("status", "PAID"), doc("amount", doc("$gte", 100))));
        Doc q03 = doc("$or", List.of(doc("status", "PAID"), doc("status", "FINISHED")));
        for (boolean iterable : List.of(false, true)) {
            require(q01.equals(prove(call("fields", iterable, eq, gte))), "Q01 保留多字段和隐式等值");
            require(q02.equals(prove(call("and", iterable, eq, gte))), "Q02 保留显式 AND 数组");
            require(q03.equals(prove(call("or", iterable, eq, call("eq", false, "status", "FINISHED")))),
                    "Q03 保留 OR 数组顺序");
            require(!q01.equals(prove(call("fields", iterable, gte, eq))), "字段顺序是结果的一部分");
            require(!q02.equals(prove(call("and", iterable, gte, eq))), "数组顺序是结果的一部分");
            refuse(call("fields", iterable, eq, call("eq", false, "status", "FINISHED")), "重复键不能覆盖");
            refuse(call("and", iterable), "空逻辑输入未建立服务器有效性");
            refuse(call("or", iterable, Map.of("status", "PAID")), "raw Document/Map 缺构造来源");
            Map<String, Object> expression = index.getMethodFamilies().stream().map(QueryPredicateEvidenceSelfTest::map)
                    .filter(f -> "gte".equals(f.get("name")) && "PIPELINE_EXPRESSION".equals(f.get("apiCategory")))
                    .flatMap(f -> ((List<?>) f.get("overloads")).stream()).map(QueryPredicateEvidenceSelfTest::map)
                    .filter(m -> "com.mongoplus.aggregate.pipeline.AggregateOperator".equals(m.get("declaredIn")))
                    .filter(m -> parameters(m).size() == 2 && "Object".equals(parameters(m).get(0).get("type")))
                    .findFirst().orElseThrow();
            refuse(call("and", iterable, new Call(expression, List.of("$amount", 100))), "Q04 表达式不能成为查询子节点");
            refuse(call("fields", iterable, new Call(method("expr", false), List.of(new Call(expression,
                    List.of("$amount", 100))))), "expr 的 STAGE_BODY_DOCUMENT 不能借普通 Query 角色进入组合器");
        }
        for (String name : List.of("fields", "and", "or")) {
            Map<String, Object> array = method(name, false);
            Map<String, Object> iterable = method(name, true);
            require(!candidate(array).get("parameterBindings").equals(candidate(iterable).get("parameterBindings")),
                    "合法 overload 的真实 Java 表示分别保留");
            require(prove(new Call(array, List.of(eq, gte))).equals(prove(new Call(iterable, List.of(eq, gte)))),
                    "同一完整调用树的条件等价不授予强制选择");
        }
        for (Object value : Arrays.asList(100, 100L, 1.25D, true, "$amount", "$$unbound", null)) {
            require(doc("amount", doc("$gte", value)).equals(prove(call("gte", false, "amount", value))),
                    "查询值不解释字段/变量引用且保留 Java 数值类型");
        }
        require(!prove(call("gte", false, "amount", 100)).equals(prove(call("gte", false, "amount", 100L))),
                "Int32 和 Int64 不等价");
        refuse(call("gte", false, "amount", List.of(1, 2)), "P0-07B 数组 Literal 未闭合");
        refuse(call("eq", false, "x", Map.of("a", 1)), "对象 Literal 未闭合");
        refuse(call("eq", false, "$expr", "bad"), "字段名不能充当查询操作符");
        refuse(call("gte", false, "x", new Object()), "未知 runtime Codec 未闭合");
        refuse(new Call(method("gte", false), List.of("amount", 100), false), "已知类型仍须独立叶子 Codec 证明");
        refuse(call("and", false, new Call(method("eq", false), List.of("status", "PAID"), false)),
                "子节点缺 Codec 不能由相同 registry 补齐");
        stageAndExpressionBoundary();
        sourceFixtures(project);
        System.out.println("QueryPredicateEvidenceSelfTest PASSED: " + positive + " positive; "
                + negative + " rejection checks; Q01-Q04; all legal overloads retained; no selection");
    }

    private record Call(Map<String, Object> method, List<Object> values, boolean codecProven) {
        Call(Map<String, Object> method, List<Object> values) { this(method, values, true); }
    }
    private record Entry(String key, Object value) { }
    private record Doc(List<Entry> entries) { }
    private static Doc doc(Object... pairs) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < pairs.length; i += 2) { entries.add(new Entry((String) pairs[i], pairs[i + 1])); }
        return new Doc(entries);
    }

    private static Doc prove(Call call) {
        Map<String, Object> method = call.method();
        Map<String, Object> candidate = candidate(method);
        List<Map<String, Object>> parameters = parameters(method);
        if (!call.codecProven() || !"QUERY_PREDICATE".equals(method.get("resultSemanticType"))
                || !method.containsKey("resultSemanticEvidence")
                || !"ESTABLISHED".equals(candidate.get("proofStatus"))
                || !"QUERY_PREDICATE".equals(candidate.get("resultSemanticType"))
                || !"Bson".equals(method.get("returnType"))
                || !Boolean.TRUE.equals(method.get("staticMethod"))
                || !((List<?>) method.getOrDefault("mongoExpressions", List.of())).isEmpty()
                || !((List<?>) method.getOrDefault("mongoStages", List.of())).isEmpty()
                || !(candidate.get("sourceEvidence") instanceof List<?> sources) || sources.size() < 2
                || !candidate.containsKey("bsonTerm")) { return null; }
        List<?> bindings = (List<?>) candidate.get("parameterBindings");
        if (bindings == null || bindings.size() != parameters.size()) { return null; }
        for (int i = 0; i < parameters.size(); i++) {
            Map<String, Object> p = parameters.get(i);
            Map<String, Object> b = map(bindings.get(i));
            if (!p.containsKey("semanticEvidence") || !p.get("semanticEvidence").equals(b.get("semanticEvidence"))
                    || !Objects.equals(p.get("semanticType"), b.get("semanticType"))
                    || !Objects.equals(p.get("semanticScope"), b.get("semanticScope"))
                    || !Objects.equals(p.get("type"), b.get("declaredJavaType"))
                    || !b.containsKey("javaRepresentation")) { return null; }
        }
        String relation = String.join(" + ", parameters.stream().map(p -> (String) p.get("semanticType")).toList())
                + " -> QUERY_PREDICATE";
        if (!List.of(relation).equals(method.get("compositionSemantics"))) { return null; }
        Map<String, Object> term = map(candidate.get("bsonTerm"));
        if ("DOCUMENT_ENTRIES".equals(term.get("op"))) {
            if (call.values().size() != 2 || !(call.values().get(0) instanceof String key)
                    || key.isEmpty() || key.startsWith("$") || key.indexOf(0) >= 0) { return null; }
            Object value = call.values().get(1);
            if (value != null && !(value instanceof String || value instanceof Integer || value instanceof Long
                    || value instanceof Double || value instanceof Boolean)) { return null; }
            Map<String, Object> valueTerm = map(term.get("value"));
            if ("DOCUMENT".equals(valueTerm.get("op"))) { value = doc(valueTerm.get("key"), value); }
            else if (!"BUILDERS_HELPER_CODEC".equals(valueTerm.get("op"))) { return null; }
            return doc(key, value);
        }
        if (call.values().isEmpty()) { return null; }
        List<Doc> children = new ArrayList<>();
        for (Object value : call.values()) {
            if (!(value instanceof Call child)) { return null; }
            Doc proved = prove(child);
            if (proved == null) { return null; }
            children.add(proved);
        }
        if ("DOCUMENT".equals(term.get("op"))
                && "BSON_DOCUMENT_ARRAY".equals(map(term.get("value")).get("op"))) {
            return doc(term.get("key"), children);
        }
        if ("QUERY_DOCUMENT_MERGE".equals(term.get("op"))) {
            Set<String> names = new HashSet<>();
            List<Entry> entries = new ArrayList<>();
            for (Doc child : children) {
                for (Entry entry : child.entries()) {
                    if (!names.add(entry.key())) { return null; }
                    entries.add(entry);
                }
            }
            return new Doc(entries);
        }
        return null;
    }

    private static void mutations(Map<String, Object> method) {
        Object[] args = parameters(method).size() == 2 ? new Object[]{"x", 100}
                : new Object[]{call("eq", false, "x", 1)};
        require(prove(new Call(method, Arrays.asList(args))) != null, "原候选可闭合");
        for (String key : List.of("resultSemanticType", "resultSemanticEvidence", "compositionSemantics",
                "candidateSemantics")) {
            Map<String, Object> changed = copy(method);
            changed.remove(key);
            refuse(new Call(changed, Arrays.asList(args)), "缺 " + key);
        }
        for (String key : List.of("sourceEvidence", "bsonTerm", "parameterBindings")) {
            Map<String, Object> changed = copy(method);
            candidate(changed).remove(key);
            refuse(new Call(changed, Arrays.asList(args)), "缺候选 " + key);
        }
        for (int i = 0; i < parameters(method).size(); i++) {
            for (String key : List.of("semanticEvidence", "semanticType", "semanticScope")) {
                Map<String, Object> changed = copy(method);
                parameters(changed).get(i).remove(key);
                refuse(new Call(changed, Arrays.asList(args)), "缺逐参数 " + key);
            }
        }
    }

    private static void stageAndExpressionBoundary() {
        Map<String, Object> match = index.getMethodFamilies().stream().map(QueryPredicateEvidenceSelfTest::map)
                .filter(f -> "match".equals(f.get("name"))).flatMap(f -> ((List<?>) f.get("overloads")).stream())
                .map(QueryPredicateEvidenceSelfTest::map).filter(m -> parameters(m).size() == 1
                        && "Bson".equals(parameters(m).get(0).get("type"))).findFirst().orElseThrow();
        require("ESTABLISHED".equals(candidate(match).get("proofStatus")), "match 的实际 body 包装证据");
        require(map(match.get("pipelineEffect")).equals(map(candidate(match).get("receiverEffect"))),
                "Stage 数量和 receiver effect 一致");
        require(((List<?>) match.get("compositionSemantics")).contains("QUERY_PREDICATE -> PIPELINE_STAGE_DOCUMENT"),
                "查询 body 到 Stage 有显式桥接");
        require(doc("$match", doc("status", "PAID", "amount", doc("$gte", 100))).equals(
                proveStage(match, call("fields", false, call("eq", false, "status", "PAID"),
                        call("gte", false, "amount", 100)))), "Q01 完整 Stage 证据树");
        Map<String, Object> missingBridge = copy(match);
        missingBridge.remove("compositionSemantics");
        require(proveStage(missingBridge, call("eq", false, "status", "PAID")) == null,
                "有 Bson 包装能力也不能跳过 Query 到 Stage 的桥接证据");
        for (String key : List.of("semanticEvidence", "semanticType", "documentInputBinding")) {
            Map<String, Object> missingInput = copy(match);
            parameters(missingInput).get(0).remove(key);
            require(proveStage(missingInput, call("eq", false, "status", "PAID")) == null,
                    "Stage 缺独立输入证据仍未闭合: " + key);
        }
        Map<String, Object> expr = method("expr", false);
        require("STAGE_BODY_DOCUMENT".equals(expr.get("resultSemanticType")), "Q04 保持 Expression wrapper 角色");
        require("PIPELINE_EXPRESSION".equals(parameters(expr).get(0).get("semanticType")), "expr 消费 Expression");
        require(!expr.containsKey("mongoQueryPredicates"), "expr 不发布普通 Query 映射");
        for (Map<String, Object> family : index.getMethodFamilies().stream().map(QueryPredicateEvidenceSelfTest::map).toList()) {
            if ("PIPELINE_QUERY_PREDICATE".equals(family.get("apiCategory"))) {
                require(((List<?>) family.get("mongoExpressions")).isEmpty(), "查询 family 与表达式 family 隔离");
            }
        }
    }

    private static Doc proveStage(Map<String, Object> method, Call body) {
        Map<String, Object> candidate = candidate(method);
        if (!(method.get("compositionSemantics") instanceof List<?> relations)
                || !relations.contains("QUERY_PREDICATE -> PIPELINE_STAGE_DOCUMENT")
                || !method.containsKey("resultSemanticEvidence")
                || !"ESTABLISHED".equals(candidate.get("proofStatus"))
                || !"PIPELINE_STAGE_DOCUMENT".equals(candidate.get("resultSemanticType"))
                || !candidate.containsKey("bsonTerm") || !candidate.containsKey("sourceEvidence")
                || !method.containsKey("pipelineEffect")
                || !method.get("pipelineEffect").equals(candidate.get("receiverEffect"))) { return null; }
        List<Map<String, Object>> parameters = parameters(method);
        if (parameters.size() != 1 || !"STAGE_BODY_DOCUMENT".equals(parameters.get(0).get("semanticType"))
                || !"VALUE".equals(parameters.get(0).get("semanticScope"))
                || !parameters.get(0).containsKey("semanticEvidence")
                || !parameters.get(0).containsKey("documentInputBinding")
                || !"WRAP_DECLARED_STAGE".equals(map(parameters.get(0).get("documentInputBinding")).get("encoding"))) {
            return null;
        }
        Map<String, Object> effect = map(method.get("pipelineEffect"));
        if (!"APPEND_STAGE".equals(effect.get("operation")) || !"ONE".equals(effect.get("count"))
                || !"RECEIVER".equals(effect.get("target")) || !"CALL_ORDER".equals(effect.get("order"))) { return null; }
        Doc child = prove(body);
        if (child == null) { return null; }
        Map<String, Object> term = map(candidate.get("bsonTerm"));
        if (!"DOCUMENT".equals(term.get("op"))
                || !"BSON_DOCUMENT_INPUT".equals(map(term.get("value")).get("op"))) { return null; }
        return doc(term.get("key"), child);
    }

    private static void sourceFixtures(Path project) throws Exception {
        String source = Files.readString(project.resolve("mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java"));
        Path root = Files.createTempDirectory("query-evidence-");
        try {
            Path aggregate = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(aggregate.getParent());
            Files.writeString(aggregate, "package com.mongoplus.aggregate; public interface Aggregate<C> {}");
            Path file = root.resolve("com/mongoplus/toolkit/Filters.java");
            Files.createDirectories(file.getParent());
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                    .pipeline(true).addSourceRoot(root).addExpressionRoot(OWNER).build());
            for (String tag : List.of("@mongoParam fieldName FIELD_NAME VALUE", "@mongoParam value QUERY_VALUE VALUE",
                    "@mongoComposition FIELD_NAME + QUERY_VALUE -> QUERY_PREDICATE", "@mongoQuery $gte")) {
                Files.writeString(file, source.replace(tag, ""));
                Map<String, Object> gte = methods(generator.generate()).stream()
                        .filter(m -> "gte".equals(m.get("name"))).findFirst().orElseThrow();
                require("NOT_ESTABLISHED".equals(candidate(gte).get("proofStatus"))
                        && !candidate(gte).containsKey("bsonTerm"), "删源码标签不能由名称或相邻 API 补齐: " + tag);
            }
            String tag = "@mongoParam filters QUERY_PREDICATE ELEMENT";
            int offset = source.indexOf(tag);
            Files.writeString(file, source.substring(0, offset) + source.substring(offset + tag.length()));
            List<Map<String, Object>> and = methods(generator.generate()).stream()
                    .filter(m -> "and".equals(m.get("name"))).toList();
            require(and.stream().filter(m -> "NOT_ESTABLISHED".equals(candidate(m).get("proofStatus"))).count() == 1
                    && and.stream().filter(m -> "ESTABLISHED".equals(candidate(m).get("proofStatus"))).count() == 1,
                    "一个 overload 缺证据不能借用另一个合法 overload，也不能隐藏另一个候选");
            Files.writeString(file, source.replace("@mongoQuery $gte", "")
                    .replace("public final class Filters", "/** @mongoQuery $gte */\npublic final class Filters"));
            require(methods(generator.generate()).stream().filter(m -> "gte".equals(m.get("name")))
                    .allMatch(m -> !m.containsKey("mongoQueryPredicates")
                            && "NOT_ESTABLISHED".equals(candidate(m).get("proofStatus"))),
                    "类级 Query 标签不替代逐 overload 映射");
            Files.writeString(file, source.replace(" gte(", " renamed("));
            require(methods(generator.generate()).stream().anyMatch(m -> "renamed".equals(m.get("name"))
                    && "ESTABLISHED".equals(candidate(m).get("proofStatus"))), "改名不改变显式证据");
            Files.writeString(file, source.replaceAll("(?m)^\\s*\\* @mongoCandidateSource [^\\r\\n]*\\r?\\n", ""));
            boolean rejected = false;
            try { generator.generate(); } catch (IllegalArgumentException expected) { rejected = true; }
            require(rejected, "缺源码来源必须拒绝发布正式构造项");
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static Call call(String name, boolean iterable, Object... values) {
        return new Call(method(name, iterable), Arrays.asList(values));
    }
    private static Map<String, Object> method(String name, boolean iterable) {
        return methods(index).stream().filter(m -> name.equals(m.get("name")))
                .filter(m -> name.equals("eq") || name.equals("gte") ? parameters(m).size() == 2
                        : name.equals("expr") || parameters(m).get(0).get("type").toString().startsWith("Iterable") == iterable)
                .findFirst().orElseThrow();
    }
    private static List<Map<String, Object>> methods(MongoPlusApiIndex value) {
        return value.list("types").stream().map(QueryPredicateEvidenceSelfTest::map)
                .filter(t -> OWNER.equals(t.get("qualifiedName")))
                .flatMap(t -> ((List<?>) t.get("publicMethods")).stream()).map(QueryPredicateEvidenceSelfTest::map).toList();
    }
    private static Map<String, Object> candidate(Map<String, Object> method) {
        return map(method.getOrDefault("candidateSemantics", Collections.emptyMap()));
    }
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> parameters(Map<String, Object> method) {
        return (List<Map<String, Object>>) method.get("parameters");
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) { return (Map<String, Object>) value; }
    @SuppressWarnings("unchecked")
    private static <T> T copy(T value) {
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> result.put(k, copy(v)));
            return (T) result;
        }
        if (value instanceof List<?> list) { return (T) list.stream().map(QueryPredicateEvidenceSelfTest::copy)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)); }
        return value;
    }
    private static void refuse(Call call, String message) {
        if (prove(call) != null) { throw new AssertionError("未拒绝: " + message); }
        negative++;
    }
    private static void require(boolean value, String message) {
        if (!value) { throw new AssertionError(message); }
        positive++;
    }
}
