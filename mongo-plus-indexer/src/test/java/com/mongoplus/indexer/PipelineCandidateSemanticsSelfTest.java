package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 上游契约的递归代入验证；没有候选搜索、优先级、调用树选择或 MCP 输出。 */
public final class PipelineCandidateSemanticsSelfTest {
    private static final String AGGREGATE = "com.mongoplus.aggregate.Aggregate";
    private static final String SORTS = "com.mongoplus.aggregate.pipeline.Sorts";
    private static final String PROJECTIONS = "com.mongoplus.aggregate.pipeline.Projections";
    private static final String OPERATORS = "com.mongoplus.conditions.operation.ConditionOperators";
    private static MongoPlusApiIndex index;
    private static int positive;
    private static int negative;

    private PipelineCandidateSemanticsSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]).toAbsolutePath().normalize();
        index = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).build()).generate();
        require(((List<?>) index.asMap().get("requiredCapabilities")).contains("CANDIDATE_SEMANTICS_V1"),
                "正式 capability 必须声明");
        long count = 0;
        for (Map<?, ?> type : maps(index.list("types"))) {
            for (Map<?, ?> method : maps((List<?>) type.get("publicMethods"))) {
                if (method.containsKey("candidateSemantics")) {
                    require("ESTABLISHED".equals(semantics(method).get("proofStatus")), "当前声明闭合");
                    require(!((List<?>) semantics(method).get("sourceEvidence")).isEmpty(), "来源独立");
                    count++;
                }
            }
        }
        require(count == 71, "原 69 个及 Document 表达式 Stage 的 2 个条件化候选事实");
        for (Map<?, ?> family : maps(index.getMethodFamilies())) {
            for (Map<?, ?> overload : maps((List<?>) family.get("overloads"))) {
                if (overload.containsKey("candidateSemantics")) {
                    require(semantics(overload).equals(semantics(method(overload.get("declaredIn").toString(),
                            overload.get("signature").toString()))), "family/publicMethods 证据一致");
                }
            }
        }
        scalarAndContainers();
        completeTrees();
        missingAndScope();
        sourceMutations(project);
        System.out.println("PipelineCandidateSemanticsSelfTest PASSED: " + count + " source-backed recipes; "
                + positive + " equivalence/structure checks; " + negative + " non-equivalence/rejections; "
                + "cross API trees, typed/order/normalization and nested receiver boundaries; no candidate selection");
    }

    private static void scalarAndContainers() {
        for (int value : new int[] {0, 2, Integer.MAX_VALUE}) {
            equal(stage("skip(int skip)", value), stage("skip(long skip)", (long) value));
        }
        refuses(() -> evaluate(stage("skip(long skip)", 2147483648L)));
        refuses(() -> evaluate(stage("skip(int skip)", -1)));
        refuses(() -> evaluate(stage("skip(int skip)", 1.5D)));
        refuses(() -> evaluate(stage("skip(int skip)", 1L)));
        refuses(() -> evaluate(stage("skip(long skip)", new Object())));
        for (List<String> fields : List.of(List.of("a"), List.of("a", "b"), List.of("b", "a"))) {
            equal(stage("unset(String... field)", fields), stage("unset(List<String> fields)", fields));
        }
        require(evaluate(stage("unset(List<String> fields)", List.of("a"))).bson()
                .equals(document("$unset", scalar("STRING", "a"))), "singleton 的真实输出是 String"); positive++;
        require(!evaluate(stage("unset(List<String> fields)", List.of("a"))).bson()
                .equals(document("$unset", scalar("ARRAY", List.of(scalar("STRING", "a"))))),
                "原始数组不能被 Core 输入归一化规则重写"); negative++;
        refuses(() -> evaluate(stage("unset(List<String> fields)", List.of())));
        refuses(() -> evaluate(stage("unset(List<String> fields)", List.of("a", "a"))));
        different(stage("unset(String... field)", List.of("a", "b")),
                stage("unset(String... field)", List.of("b", "a")));
        for (String owner : List.of(SORTS, PROJECTIONS)) {
            for (String name : owner.equals(SORTS) ? List.of("asc", "desc") : List.of("include", "exclude")) {
                equal(call(owner, name + "(String... fieldNames)", List.of("a", "b")),
                        call(owner, name + "(List<String> fieldNames)", List.of("a", "b")));
                different(call(owner, name + "(String... fieldNames)", List.of("a", "b")),
                        call(owner, name + "(String... fieldNames)", List.of("b", "a")));
                equal(call(owner, name + "(String... fieldNames)", List.of("a", "b")),
                        call(owner, name + "(SFunction<T, ?>... fieldNames)", List.of(new Getter("a"), new Getter("b"))));
            }
        }
        different(call(SORTS, "asc(String... fieldNames)", List.of("a")),
                call(SORTS, "desc(String... fieldNames)", List.of("a")));
        refuses(() -> evaluate(stage("sort(String field, Integer value)", "a", 2)));
        List<Object> operands = List.of("$price", 2, 3L, 0.5D);
        equal(call(OPERATORS, "multiply(Object... values)", operands),
                call(OPERATORS, "multiply(Collection<?> values)", operands));
        different(call(OPERATORS, "multiply(Object... values)", List.of("$price", 2)),
                call(OPERATORS, "multiply(Collection<?> values)", List.of("$price", 2L)));
        different(call(OPERATORS, "multiply(Object... values)", List.of("$price", "$quantity")),
                call(OPERATORS, "multiply(Object... values)", List.of("$quantity", "$price")));
        for (String id : List.of("$category", "category")) {
            List<Pair> entries = List.of(new Pair("total", new Closed(document("$sum", scalar("STRING", "$price")))),
                    new Pair("count", new Closed(document("$sum", scalar("INT32", 1)))));
            equal(stage("group(TExpression id, BsonField... fieldAccumulators)", id, entries),
                    stage("group(TExpression id, List<BsonField> fieldAccumulators)", id, entries));
            different(stage("group(TExpression id, BsonField... fieldAccumulators)", id, entries),
                    stage("group(TExpression id, List<BsonField> fieldAccumulators)", id,
                            List.of(entries.get(1), entries.get(0))));
            equal(stage("group(String _id)", id),
                    stage("group(TExpression id, List<BsonField> fieldAccumulators)", id, List.of()));
        }
        equal(stage("group(TExpression id, BsonField... fieldAccumulators)", null, List.of()),
                stage("group(TExpression id, List<BsonField> fieldAccumulators)", null, List.of()));
        different(stage("group(TExpression id, BsonField... fieldAccumulators)", 1, List.of()),
                stage("group(TExpression id, List<BsonField> fieldAccumulators)", 1L, List.of()));
        refuses(() -> evaluate(stage("group(TExpression id, List<BsonField> fieldAccumulators)", "$x",
                List.of(new Pair("_id", 1)))));
    }

    private static void completeTrees() {
        for (String factory : List.of("asc", "desc")) {
            int direction = factory.equals("asc") ? 1 : -1;
            Call body = call(SORTS, factory + "(List<String> fieldNames)", List.of("a"));
            equal(stage("sort(String field, Integer value)", "a", direction),
                    stage("sortSpecification(Bson specification)", body));
            equal(stage((direction == 1 ? "sortAsc" : "sortDesc") + "(String field)", "a"),
                    stage("sortSpecification(Bson specification)", body));
        }
        Call a = call(SORTS, "asc(String... fieldNames)", List.of("a"));
        Call b = call(SORTS, "desc(String... fieldNames)", List.of("b"));
        for (String reducer : List.of("orderBy(Bson... sorts)", "orderBy(List<? extends Bson> sorts)")) {
            Call body = call(SORTS, reducer, List.of(a, b));
            equal(stage("sortSpecification(Bson specification)", body), stage("sortSpecification(Bson specification)",
                    call(SORTS, "orderBy(Bson... sorts)", List.of(a, b))));
        }
        // 不同归约算法在无重键输入域可产生相同 BSON；有重键时字段位置不同。
        equalBson(call(SORTS, "orderBy(Bson... sorts)", List.of(a, b)),
                call(PROJECTIONS, "fields(Bson... projections)", List.of(a, b)));
        Call overwrite = call(SORTS, "asc(String... fieldNames)", List.of("a"));
        different(call(SORTS, "orderBy(Bson... sorts)", List.of(a, b, overwrite)),
                call(PROJECTIONS, "fields(Bson... projections)", List.of(a, b, overwrite)));
        Call multiplyA = call(OPERATORS, "multiply(Object... values)", List.of("$price", "$quantity"));
        Call multiplyB = call(OPERATORS, "multiply(Collection<?> values)", List.of("$price", "$quantity"));
        Call includeA = call(PROJECTIONS, "include(String... fieldNames)", List.of("name"));
        Call includeB = call(PROJECTIONS, "include(List<String> fieldNames)", List.of("name"));
        Call suppress = call(PROJECTIONS, "excludeId()");
        Call computedA = call(PROJECTIONS, "computed(String fieldName, TExpression expression)", "total", multiplyA);
        Call computedB = call(PROJECTIONS, "computed(String fieldName, TExpression expression)", "total", multiplyB);
        equal(stage("project(Bson bson)", call(PROJECTIONS, "fields(Bson... projections)",
                List.of(includeA, computedA, suppress))), stage("project(Bson bson)",
                call(PROJECTIONS, "fields(List<? extends Bson> projections)", List.of(includeB, computedB, suppress))));
        Call left = stage("sort(String field, Integer value)", "a", -1);
        Call right = stage("sortSpecification(Bson specification)", call(SORTS, "desc(String... fieldNames)", List.of("a")));
        for (String signature : List.of("facet(Facet... facets)", "lookup(String from, Aggregate<?> aggregate, String as)",
                "unionWith(String collectionName, Aggregate<?> aggregate)")) {
            Map<?, ?> outer = method(AGGREGATE, signature);
            requireOuter(outer);
            require(pipeline(List.of(on(left, "inner"))).equals(pipeline(List.of(on(right, "inner")))),
                    "同一 nested receiver 与 inner 调用序等价"); positive++;
            require(!pipeline(List.of(on(left, "inner"))).equals(pipeline(List.of(on(right, "sibling")))),
                    "相同 BSON 不允许 receiver 作用域变更"); negative++;
            Map<String, Object> missing = copy(outer); missing.remove("pipelineEffect");
            refuses(() -> requireOuter(missing));
            Map<String, Object> missingComposition = copy(outer);
            missingComposition.remove("pipelineContainer");
            List<Map<String, Object>> brokenParameters = maps((List<?>) outer.get("parameters")).stream()
                    .map(PipelineCandidateSemanticsSelfTest::copy).toList();
            for (Map<String, Object> parameter : brokenParameters) { parameter.remove("pipelineExtraction"); }
            missingComposition.put("parameters", brokenParameters);
            refuses(() -> requireOuter(missingComposition));
        }
        require(!pipeline(List.of(stage("sortAsc(String field)", "a"), stage("sortAsc(String field)", "b")))
                .equals(pipeline(List.of(stage("sortAsc(List<String> field)", List.of("a", "b"))))),
                "两个Stage不能替换一个Stage"); negative++;
    }

    private static void missingAndScope() {
        Call pure = call(OPERATORS, "multiply(Object... values)", List.of("$price", 2));
        Map<String, Object> method = copy(pure.method()); method.remove("candidateSemantics");
        refuses(() -> evaluate(new Call(method, pure.arguments(), "root")));
        Call incomplete = new Call(method, pure.arguments(), "root");
        refuses(() -> evaluate(stage("project(Bson bson)", call(PROJECTIONS,
                "computed(String fieldName, TExpression expression)", "total", incomplete))));
        refuses(() -> evaluate(stage("project(Bson bson)", new Object())));
        refuses(() -> evaluate(new Call(pure.method(), pure.arguments(), "root", "CUSTOM_CODEC_WITHOUT_PROOF")));
        refuses(() -> evaluate(call(OPERATORS, "multiply(Object... values)",
                List.of(call(PROJECTIONS, "include(String... fieldNames)", List.of("a")), 2))));
        refuses(() -> evaluate(call(OPERATORS, "multiply(Object... values)", List.of("$$price", 2))));
        different(call(OPERATORS, "multiply(Object... values)", List.of(new Variable("$$price", "scopeA"), 2)),
                call(OPERATORS, "multiply(Collection<?> values)", List.of(new Variable("$$price", "scopeB"), 2)));
        equal(call(OPERATORS, "multiply(Object... values)", List.of(new Variable("$$price", "scopeA"), 2)),
                call(OPERATORS, "multiply(Collection<?> values)", List.of(new Variable("$$price", "scopeA"), 2)));
        for (String signature : List.of("sort(Bson bson)", "group(SFunction<T, ?> _id)")) {
            require(!method(AGGREGATE, signature).containsKey("candidateSemantics"), "不依据名字或相邻overload恢复");
            negative++;
        }
        require(!method(SORTS, "orderBy(Order... orders)").containsKey("candidateSemantics"),
                "专用Order输入构造仍未闭合"); negative++;
    }

    /** 只解释正式构造项。所有输出使用带类型的有序 entry 列表，避免 Map.equals 忽略顺序。 */
    private static Outcome evaluate(Call call) {
        Map<?, ?> evidence = semantics(call.method());
        if (!"ESTABLISHED".equals(evidence.get("proofStatus"))) { throw new IllegalArgumentException("缺正式证明"); }
        if (!"DRIVER_5_4_DEFAULT".equals(call.codecProfile())) {
            throw new IllegalArgumentException("不同容器或叶子codec缺独立编码证明");
        }
        for (Map<?, ?> binding : maps((List<?>) evidence.get("parameterBindings"))) {
            Map<?, ?> representation = (Map<?, ?>) binding.get("javaRepresentation");
            Object argument = call.arguments().get(binding.get("parameter"));
            if ("SCALAR".equals(representation.get("container"))) {
                Object type = representation.get("javaType");
                if (("int".equals(type) || "java.lang.Integer".equals(type)) && !(argument instanceof Integer)
                        || "long".equals(type) && !(argument instanceof Integer || argument instanceof Long)
                        || "java.lang.String".equals(type) && !(argument instanceof String)
                        || "com.mongoplus.support.SFunction".equals(type) && !(argument instanceof Getter)) {
                    throw new IllegalArgumentException("实际Java参数表示不匹配");
                }
            } else if (!(argument instanceof List)) {
                throw new IllegalArgumentException("完整有序元素容器缺失");
            }
        }
        List<Object> scopes = new ArrayList<>();
        Map<?, ?> applicability = (Map<?, ?>) evidence.get("applicability");
        if (applicability.containsKey("integerDomain")) {
            Map<?, ?> domain = (Map<?, ?>) applicability.get("integerDomain");
            if (!((List<?>) domain.get("allowedValues")).contains(call.arguments().get(domain.get("parameter")))) {
                throw new IllegalArgumentException("独立整数输入域");
            }
        }
        Object bson = evaluateTerm((Map<?, ?>) evidence.get("bsonTerm"), call.arguments(), scopes);
        Map<?, ?> effect = (Map<?, ?>) evidence.get("receiverEffect");
        List<Object> effects = "NONE".equals(effect.get("operation")) ? List.of()
                : List.of(List.of(call.receiver(), effect.get("operation"), effect.get("count"), effect.get("order")));
        return new Outcome(bson, effects, scopes);
    }

    private static Object evaluateTerm(Map<?, ?> term, Map<String, Object> args, List<Object> scopes) {
        String operation = term.get("op").toString();
        switch (operation) {
            case "INPUT": return args.get(term.get("parameter"));
            case "DOCUMENT": return document(term.get("key").toString(),
                    evaluateTerm((Map<?, ?>) term.get("value"), args, scopes));
            case "INT32_EXACT": {
                Object raw = evaluateTerm((Map<?, ?>) term.get("input"), args, scopes);
                if (!(raw instanceof Integer || raw instanceof Long)) { throw new IllegalArgumentException("整数表示"); }
                long value = ((Number) raw).longValue();
                if (value < ((Number) term.get("minimum")).longValue()
                        || value > ((Number) term.get("maximum")).longValue()) { throw new IllegalArgumentException("范围"); }
                return scalar("INT32", Math.toIntExact(value));
            }
            case "SINGLETON_SCALAR_ELSE_ARRAY": {
                List<?> fields = (List<?>) evaluateTerm((Map<?, ?>) term.get("input"), args, scopes);
                if (fields.isEmpty() || fields.stream().distinct().count() != fields.size()
                        || fields.stream().anyMatch(x -> !(x instanceof String))) { throw new IllegalArgumentException("字段域"); }
                List<Object> values = fields.stream().<Object>map(x -> scalar("STRING", x)).toList();
                return fields.size() == 1 ? values.get(0) : scalar("ARRAY", values);
            }
            case "BSON_DOCUMENT_INPUT": return encoded(evaluateTerm((Map<?, ?>) term.get("input"), args, scopes), scopes);
            case "ARRAY_RUNTIME_CODEC": {
                List<?> values = (List<?>) evaluateTerm((Map<?, ?>) term.get("input"), args, scopes);
                for (Object value : values) {
                    if (value instanceof Call && !"org.bson.Document".equals(
                            semantics(((Call) value).method()).get("javaResultRepresentation"))) {
                        throw new IllegalArgumentException("runtime codec输入不能只凭子节点BSON代入");
                    }
                }
                return scalar("ARRAY", values.stream().map(v -> encoded(v, scopes)).toList());
            }
            case "BUILDERS_HELPER_CODEC": return encoded(evaluateTerm((Map<?, ?>) term.get("input"), args, scopes), scopes);
            case "DOCUMENT_ENTRIES": {
                Map<?, ?> key = (Map<?, ?>) term.get("key");
                Map<?, ?> value = (Map<?, ?>) term.get("value");
                Object raw = key.containsKey("inputParameter") ? args.get(key.get("inputParameter")) : key.get("value");
                List<?> keys = "ELEMENT".equals(key.get("scope")) ? (List<?>) raw : List.of(raw);
                LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                for (Object field : keys) {
                    String name;
                    if ("GET_FIELD_NAME_LINE".equals(key.get("encoding"))) {
                        if (!(field instanceof Getter)) { throw new IllegalArgumentException("不能由String伪造getter"); }
                        name = ((Getter) field).field();
                    } else {
                        if (!(field instanceof String)) { throw new IllegalArgumentException("真实String字段"); }
                        name = (String) field;
                    }
                    Object encodedValue = value.containsKey("inputParameter") ? encoded(args.get(value.get("inputParameter")), scopes)
                            : scalar("INT32", value.get("value"));
                    if ("LAST".equals(term.get("duplicatePosition"))) { result.remove(name); }
                    result.put(name, encodedValue);
                }
                return ordered(result);
            }
            case "DOCUMENT_MERGE": {
                List<?> parts = (List<?>) evaluateTerm((Map<?, ?>) term.get("input"), args, scopes);
                LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                for (Object part : parts) {
                    List<?> document = (List<?>) encoded(part, scopes);
                    if (!"DOCUMENT".equals(document.get(0))) { throw new IllegalArgumentException("文档角色"); }
                    for (Object item : (List<?>) document.get(1)) {
                        List<?> entry = (List<?>) item;
                        if ("LAST".equals(term.get("duplicatePosition"))) { result.remove(entry.get(0)); }
                        result.put(entry.get(0).toString(), entry.get(1));
                    }
                }
                return ordered(result);
            }
            case "PREFIXED_ENTRIES": {
                LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                result.put(term.get("key").toString(), evaluateTerm((Map<?, ?>) term.get("value"), args, scopes));
                Object entries = term.get("entries");
                if (entries instanceof Map) {
                    for (Object item : (List<?>) evaluateTerm((Map<?, ?>) entries, args, scopes)) {
                        Pair pair = (Pair) item;
                        if (result.containsKey(pair.name())) { throw new IllegalArgumentException("重键或保留键"); }
                        result.put(pair.name(), encoded(pair.value(), scopes));
                    }
                }
                return ordered(result);
            }
            case "NAMED_EXPRESSION_ENTRIES": return evaluateTerm((Map<?, ?>) term.get("input"), args, scopes);
            default: throw new IllegalArgumentException("未支持构造项: " + operation);
        }
    }

    private static Object encoded(Object value, List<Object> scopes) {
        if (value instanceof Call) {
            Outcome result = evaluate((Call) value);
            if (!result.effects().isEmpty()) { throw new IllegalArgumentException("receiver不能当Bson结果"); }
            scopes.addAll(result.scopes());
            return result.bson();
        }
        if (value instanceof Closed) { return ((Closed) value).bson(); }
        if (value instanceof Variable) {
            Variable variable = (Variable) value;
            scopes.add(List.of(variable.reference(), variable.scope()));
            return scalar("STRING", variable.reference());
        }
        if (value == null) { return scalar("NULL", "null"); }
        if (value instanceof String) {
            if (((String) value).startsWith("$$")) { throw new IllegalArgumentException("变量作用域缺失"); }
            return scalar("STRING", value);
        }
        if (value instanceof Integer) { return scalar("INT32", value); }
        if (value instanceof Long) { return scalar("INT64", value); }
        if (value instanceof Double) { return scalar("DOUBLE", value.toString()); }
        if (value instanceof Boolean) { return scalar("BOOLEAN", value); }
        throw new IllegalArgumentException("缺编码或构造证明");
    }

    private static void sourceMutations(Path project) throws Exception {
        Path root = Files.createTempDirectory("candidate-evidence-").toAbsolutePath().normalize();
        Path file = root.resolve("com/mongoplus/aggregate/Aggregate.java");
        Files.createDirectories(file.getParent());
        String source = Files.readString(project.resolve("mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java"));
        String signature = "sort(String field, Integer value)";
        try {
            for (String tag : List.of("mongoCandidate", "mongoPipelineEffect")) {
                String broken = source.replaceAll("(?m)^\\s*\\* @" + tag + "[^\\r\\n]*\\r?\\n", "");
                if (tag.equals("mongoPipelineEffect")) {
                    int declaration = source.indexOf("Children sort(final String field, final Integer value)");
                    int start = source.lastIndexOf("/**", declaration);
                    broken = source.substring(0, start) + source.substring(start, declaration)
                            .replaceAll("(?m)^\\s*\\* @mongoPipelineEffect[^\\r\\n]*\\r?\\n", "")
                            + source.substring(declaration);
                }
                if (tag.equals("mongoCandidate")) {
                    broken = broken.replaceAll("(?m)^\\s*\\* @mongoCandidateSource[^\\r\\n]*\\r?\\n", "");
                }
                Files.writeString(file, broken);
                Map<?, ?> missing = generatedMethod(project, root, signature);
                if (tag.equals("mongoCandidate")) {
                    require(!missing.containsKey("candidateSemantics"), "缺声明不按名称恢复");
                } else {
                    require("NOT_ESTABLISHED".equals(semantics(missing).get("proofStatus"))
                            && !semantics(missing).containsKey("bsonTerm"), "缺独立effect保留未闭合");
                }
                negative++;
            }
            Files.writeString(file, source.replace("operation=FIELD_STAGE", "operation=UNKNOWN_OPERATION"));
            refuses(() -> uncheckedMethod(project, root, signature));
            Files.writeString(file, source.replace("@mongoCandidateSource", "@unknownSource"));
            refuses(() -> uncheckedMethod(project, root, signature));
            Files.writeString(file, source.replace("key=field value=value", "key=field value=value extra=YES"));
            refuses(() -> uncheckedMethod(project, root, signature));
            Files.writeString(file, source.replace("key=field value=value", "key=field key=field value=value"));
            refuses(() -> uncheckedMethod(project, root, signature));
            Files.writeString(file, source.replace("sort(final String field, final Integer value)",
                    "renamed(final String field, final Integer value)"));
            Map<?, ?> renamed = generatedMethod(project, root, "renamed(String field, Integer value)");
            require(semantics(renamed).equals(semantics(method(AGGREGATE, signature))), "改名不改变独立证据"); positive++;
            Files.writeString(file, source.replace("@mongoParam value INTEGER_VALUE VALUE", ""));
            require("NOT_ESTABLISHED".equals(semantics(generatedMethod(project, root, signature)).get("proofStatus")),
                    "新语义必须有独立逐参数证据"); negative++;
            Files.writeString(file, source.replace("sort(final String field, final Integer value)",
                    "sort(final String field, final Long value)"));
            refuses(() -> uncheckedMethod(project, root, "sort(String field, Long value)"));
            String first = block(source, "Children skip(final long skip);");
            String second = block(source, "Children skip(final int skip);");
            Files.writeString(file, source.replace(first, "P004_SWAP_MARKER").replace(second, first)
                    .replace("P004_SWAP_MARKER", second));
            MongoPlusApiIndex reordered = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project)
                    .addSourceRoot(root).build()).generate();
            require(reordered.asMap().equals(index.asMap()), "反转合法overload声明后正式证据完全一致"); positive++;
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    require(path.toAbsolutePath().normalize().startsWith(root), "fixture 删除边界");
                    Files.delete(path);
                }
            }
        }
    }

    private static String block(String source, String declaration) {
        int end = source.indexOf(declaration) + declaration.length();
        int start = source.lastIndexOf("/**", end);
        return source.substring(start, end);
    }

    private static Map<?, ?> generatedMethod(Path project, Path root, String signature) throws Exception {
        MongoPlusApiIndex generated = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project)
                .addSourceRoot(root).build()).generate();
        return method(generated, AGGREGATE, signature);
    }

    private static void uncheckedMethod(Path project, Path root, String signature) {
        try { generatedMethod(project, root, signature); }
        catch (IllegalArgumentException exception) { throw exception; }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }

    private static Call stage(String signature, Object... values) { return call(AGGREGATE, signature, values); }
    private static Call call(String owner, String signature, Object... values) {
        Map<?, ?> method = method(owner, signature);
        List<Map<?, ?>> parameters = maps((List<?>) method.get("parameters"));
        require(parameters.size() == values.length, "完整消费所有参数");
        Map<String, Object> args = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) { args.put(parameters.get(i).get("name").toString(), values[i]); }
        return new Call(method, args, "root");
    }

    private static Call on(Call call, String receiver) { return new Call(call.method(), call.arguments(), receiver); }
    private static List<Outcome> pipeline(List<Call> stages) { return stages.stream().map(PipelineCandidateSemanticsSelfTest::evaluate).toList(); }
    private static void requireOuter(Map<?, ?> outer) {
        if (!outer.containsKey("pipelineEffect")) { throw new IllegalArgumentException("outer缺effect"); }
        boolean construction = outer.containsKey("pipelineContainer")
                || maps((List<?>) outer.get("parameters")).stream().anyMatch(p -> p.containsKey("pipelineExtraction")
                        && p.containsKey("objectFieldBinding") && "PIPELINE".equals(p.get("semanticType")));
        if (!construction) { throw new IllegalArgumentException("outer缺完整pipeline composition"); }
        Map<?, ?> factoryType = maps(index.list("types")).stream()
                .filter(t -> "com.mongoplus.aggregate.AggregateWrapper".equals(t.get("qualifiedName"))).findFirst().orElseThrow();
        if (maps((List<?>) factoryType.get("constructors")).stream().noneMatch(c -> c.containsKey("pipelineFactory"))
                || !method(AGGREGATE, "getAggregateConditionList()").containsKey("pipelineRepresentation")) {
            throw new IllegalArgumentException("独立receiver工厂或representation缺失");
        }
    }
    private static List<Object> scalar(String type, Object value) { return List.of(type, value); }
    private static Object document(String key, Object value) { return List.of("DOCUMENT", List.of(List.of(key, value))); }
    private static Object ordered(LinkedHashMap<String, Object> entries) {
        return List.of("DOCUMENT", entries.entrySet().stream().map(e -> List.of(e.getKey(), e.getValue())).toList());
    }
    private static void equal(Call left, Call right) { require(evaluate(left).equals(evaluate(right)), "完整调用树应等价"); positive++; }
    private static void equalBson(Call left, Call right) { require(evaluate(left).bson().equals(evaluate(right).bson()), "原始BSON应相同"); positive++; }
    private static void different(Call left, Call right) { require(!evaluate(left).equals(evaluate(right)), "不等价不得合并"); negative++; }
    private static void refuses(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException exception) { negative++; return; }
        throw new AssertionError("缺证据或域外输入必须拒绝");
    }
    private static Map<?, ?> semantics(Map<?, ?> method) {
        if (!(method.get("candidateSemantics") instanceof Map)) { throw new IllegalArgumentException("缺候选构造证明"); }
        return (Map<?, ?>) method.get("candidateSemantics");
    }
    private static Map<?, ?> method(String owner, String signature) { return method(index, owner, signature); }
    private static Map<?, ?> method(MongoPlusApiIndex source, String owner, String signature) {
        return maps(source.list("types")).stream().filter(t -> owner.equals(t.get("qualifiedName")))
                .flatMap(t -> maps((List<?>) t.get("publicMethods")).stream())
                .filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }
    private static Map<String, Object> copy(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>(); source.forEach((k, v) -> result.put(k.toString(), v)); return result;
    }
    private static List<Map<?, ?>> maps(List<?> list) { return list.stream().<Map<?, ?>>map(v -> (Map<?, ?>) v).toList(); }
    private static void require(boolean value, String reason) { if (!value) { throw new AssertionError(reason); } }
    private record Call(Map<?, ?> method, Map<String, Object> arguments, String receiver, String codecProfile) {
        private Call(Map<?, ?> method, Map<String, Object> arguments, String receiver) {
            this(method, arguments, receiver, "DRIVER_5_4_DEFAULT");
        }
    }
    private record Outcome(Object bson, List<Object> effects, List<Object> scopes) { }
    private record Getter(String field) { }
    private record Pair(String name, Object value) { }
    private record Closed(Object bson) { }
    private record Variable(String reference, String scope) { }
}
