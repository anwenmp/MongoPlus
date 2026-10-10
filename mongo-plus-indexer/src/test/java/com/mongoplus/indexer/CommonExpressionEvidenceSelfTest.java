package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** 上游证据自测：只按正式构造项证明 expression；不选择 MCP overload，不计算服务器结果。 */
public final class CommonExpressionEvidenceSelfTest {
    private static final String EXPRESSION = "PIPELINE_EXPRESSION";
    private static final String CONCEPT = "PIPELINE_EXPRESSION_FIELD_REFERENCE";
    private static final String FACTORY = "com.mongoplus.aggregate.pipeline.AggregateOperator";
    private static final String CODEC = "org.bson.codecs.CollectionCodec";
    private static final List<String> OPERATORS = List.of("$ne", "$gt", "$gte", "$lt", "$lte",
            "$and", "$or", "$not", "$subtract", "$divide", "$eq");
    private static MongoPlusApiIndex index;
    private static int positives;
    private static int negatives;

    private CommonExpressionEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]);
        index = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).build()).generate();
        require(index.asMap().get("expressionRoots").equals(MongoPlusIndexerConfig.PIPELINE_EXPRESSION_ROOTS),
                "沿用精确六个扫描根");
        for (String operator : OPERATORS) {
            Map<String, Object> method = method(operator);
            require(FACTORY.equals(method.get("declaredIn")), "Query Predicate 不能成为 expression");
            require(List.of(operator).equals(method.get("mongoExpressions"))
                    && ((List<?>) method.get("mongoStages")).isEmpty(), "独立操作符映射");
            require(parameters(method).equals(publicMethod(index, method).get("parameters")), "公开方法参数证据一致");
            require(semantics(method).equals(publicMethod(index, method).get("candidateSemantics")), "公开方法构造证据一致");
            boolean sequence = "ARRAY_RUNTIME_CODEC".equals(array(method).get("op"));
            int arity = sequence ? 3 : parameters(method).size();
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < arity; i++) { values.add(i == 0 ? "$amount" : i); }
            pass(new Call(method, values, true), operator + " 独立表达式");
            for (Object value : Arrays.asList(null, -7, -2147483649L, -1.25D, "CANCELLED",
                    "$amount", "$$bound", new Decimal("-123.450"), true)) {
                List<Object> operands = new ArrayList<>();
                for (int i = 0; i < (sequence ? 1 : arity); i++) { operands.add(value); }
                pass(new Call(method, operands, true), operator + " exact literal/引用");
            }
            if (sequence) {
                pass(new Call(method, List.of(), true), "空数组");
                pass(new Call(method, List.of("$amount"), true), "单元素数组");
            } else {
                refuse(new Call(method, List.of(), true), "错误操作数数量");
                List<Object> extra = new ArrayList<>(values);
                extra.add(1);
                refuse(new Call(method, extra, true), "过多操作数");
            }
            List<Object> opaque = new ArrayList<>(values);
            opaque.set(0, new Object());
            refuse(new Call(method, opaque, true), "Object 不能放行未知类型");
            List<Object> missingScope = new ArrayList<>(values);
            missingScope.set(0, "$$missing");
            refuse(new Call(method, missingScope, true), "变量绑定缺失");
            refuse(new Call(method, values, false), "缺少实际容器 codec 证明");
            mutations(method, values);
        }
        Call equality = call("$eq", "$status", "CANCELLED");
        pass(call("$not", equality), "精确 not(eq) 单元素数组");
        pass(call("$and", call("$gt", "$amount", 100), call("$lte", "$amount", 1000),
                call("$ne", "$status", "CANCELLED")), "最小 match 的 expression 树");
        pass(call("$subtract", "$amount", "$discount"), "最小 project difference");
        pass(call("$divide", "$amount", 100), "最小 project ratio");
        pass(call("$or", call("$not", call("$and", call("$gte", "$amount", 100),
                call("$lt", "$amount", 1000))), call("$eq",
                call("$subtract", call("$divide", "$amount", 100), 2), 3)), "深层完整树");
        Map<String, Object> missingChild = copy(method("$eq"));
        missingChild.remove("resultSemanticEvidence");
        refuse(call("$not", new Call(missingChild, equality.operands(), true)), "深层 result 缺失");
        refuse(call("$not", Map.of("$eq", List.of(1, 1))), "raw BSON 无独立 API 来源");
        refuse(call("$not", Map.of("$match", Map.of())), "完整 Stage 不能冒充 expression");
        refuse(call("$and", new UnprovenCodec("java.lang.Integer", 1)), "已知 Java 类型也不能跳过 codec 证明");
        fixtures();
        System.out.println("CommonExpressionEvidenceSelfTest PASSED: " + positives + " positive; "
                + negatives + " rejection checks; 10 new expressions + eq; no operator planner/selection");
    }

    private record Decimal(String value) { }
    private record UnprovenCodec(String javaType, Object value) { }
    private record Call(Map<String, Object> method, List<Object> operands, boolean containerCodecProven) { }

    private static Call call(String operator, Object... operands) {
        return new Call(method(operator), Arrays.asList(operands), true);
    }

    /** 单个通用准入器同时覆盖定参和变参，不按 operator 或方法名分支。 */
    private static boolean prove(Call call) {
        Map<String, Object> method = call.method();
        Map<?, ?> candidate = semantics(method);
        List<Map<String, Object>> params = parameters(method);
        String relation = String.join(" + ", java.util.Collections.nCopies(params.size(), EXPRESSION))
                + " -> " + EXPRESSION;
        if (!call.containerCodecProven() || params.isEmpty() || !"Bson".equals(method.get("returnType"))
                || !"ARRAY".equals(method.get("expressionShape"))
                || !EXPRESSION.equals(method.get("resultSemanticType"))
                || !List.of(relation).equals(method.get("compositionSemantics"))
                || !Map.of("source", "JAVADOC", "tag", "mongoComposition", "value", relation)
                        .equals(method.get("resultSemanticEvidence"))
                || !"ESTABLISHED".equals(candidate.get("proofStatus"))
                || !"org.bson.Document".equals(candidate.get("javaResultRepresentation"))
                || !(candidate.get("sourceEvidence") instanceof List<?> sources) || sources.size() < 2
                || !candidate.containsKey("bsonTerm")) { return false; }
        Map<?, ?> term = (Map<?, ?>) candidate.get("bsonTerm");
        if (!"DOCUMENT".equals(term.get("op")) || !List.of(term.get("key")).equals(method.get("mongoExpressions"))
                || !((List<?>) method.get("mongoStages")).isEmpty()) { return false; }
        Map<?, ?> array = (Map<?, ?>) term.get("value");
        if (!CODEC.equals(array.get("requiredCollectionCodec"))) { return false; }
        boolean sequence = "ARRAY_RUNTIME_CODEC".equals(array.get("op"));
        if (!sequence && !"ARRAY_ARGUMENTS_RUNTIME_CODEC".equals(array.get("op"))) { return false; }
        Map<?, ?> applicability = (Map<?, ?>) candidate.get("applicability");
        if (!Map.of("binding", sequence ? "PARAMETER_ELEMENTS" : "PARAMETER_VALUES",
                "minimumCount", sequence ? 0 : params.size(),
                "maximumCount", sequence ? "UNBOUNDED" : params.size(),
                "nullValues", "ENCODE_BSON_NULL", "serverEvaluation", "NOT_PERFORMED")
                .equals(applicability.get("expressionOperands"))) { return false; }
        List<?> bindings = (List<?>) candidate.get("parameterBindings");
        if (bindings == null || bindings.size() != params.size()) { return false; }
        for (int i = 0; i < params.size(); i++) {
            Map<String, Object> parameter = params.get(i);
            Map<?, ?> binding = (Map<?, ?>) bindings.get(i);
            String scope = sequence ? "ELEMENT" : "VALUE";
            if (!EXPRESSION.equals(parameter.get("semanticType")) || !scope.equals(parameter.get("semanticScope"))
                    || !CONCEPT.equals(parameter.get("conceptRef"))
                    || !Map.of("source", "JAVADOC", "tag", "mongoParam",
                            "value", parameter.get("name") + " " + EXPRESSION + " " + scope)
                            .equals(parameter.get("semanticEvidence"))
                    || !parameter.get("type").equals(binding.get("declaredJavaType"))
                    || !parameter.get("semanticEvidence").equals(binding.get("semanticEvidence"))
                    || !(binding.get("javaRepresentation") instanceof Map<?, ?> javaType)) { return false; }
            if (sequence) {
                if (params.size() != 1 || !"Object[]".equals(parameter.get("type"))
                        || !Boolean.TRUE.equals(parameter.get("varargs"))
                        || !"VARARGS".equals(binding.get("invocation"))
                        || !"ARRAY".equals(javaType.get("container"))
                        || !"java.lang.Object".equals(javaType.get("elementType"))) { return false; }
            } else {
                if (!"Object".equals(parameter.get("type")) || !"SINGLE".equals(binding.get("invocation"))
                        || !"java.lang.Object".equals(javaType.get("javaType"))) { return false; }
            }
        }
        if (!sequence) {
            List<?> inputs = (List<?>) array.get("inputs");
            if (inputs.size() != params.size() || call.operands().size() != inputs.size()
                    || !"DECLARATION".equals(array.get("order"))) { return false; }
            for (int i = 0; i < inputs.size(); i++) {
                if (!params.get(i).get("name").equals(((Map<?, ?>) inputs.get(i)).get("parameter"))) { return false; }
            }
        } else if (!"ITERATION".equals(array.get("order"))
                || !params.get(0).get("name").equals(((Map<?, ?>) array.get("input")).get("parameter"))) { return false; }
        return call.operands().stream().allMatch(CommonExpressionEvidenceSelfTest::operand);
    }

    private static boolean operand(Object value) {
        Map<?, ?> concept = index.list("concepts").stream().map(item -> (Map<?, ?>) item)
                .filter(item -> CONCEPT.equals(item.get("id"))).findFirst().orElseThrow();
        if (value instanceof Call child) {
            Map<?, ?> nested = (Map<?, ?>) concept.get("nestedExpression");
            return nested != null && Boolean.TRUE.equals(nested.get("requiresResultSemanticEvidence"))
                    && EXPRESSION.equals(nested.get("semanticType")) && prove(child);
        }
        if (value instanceof String text) {
            String kind = text.startsWith("$$") ? "variableReference"
                    : text.startsWith("$") ? "fieldReference" : "plainStringValue";
            Map<?, ?> rule = (Map<?, ?>) concept.get(kind);
            if (rule == null || !"UNCHANGED".equals(rule.get("encoding"))) { return false; }
            return !text.startsWith("$$") || "BOUND_VARIABLE".equals(VariableBindingScopeSelfTest.boundReference(
                    index, text, Map.of("bound", "$amount")));
        }
        Map<?, ?> literal = (Map<?, ?>) concept.get("literalValue");
        if (literal == null || !Boolean.TRUE.equals(literal.get("requiresCodecEvidence"))
                || !Boolean.TRUE.equals(literal.get("requiresAssignableJavaType"))
                || !"RUNTIME_CODEC".equals(literal.get("encoding"))) { return false; }
        // 这组有限类型在 Core 使用真实 Driver 5.4 默认 registry 逐个强制编码验证；不是 Object 放行。
        return value == null || Set.of(Integer.class, Long.class, Double.class, Boolean.class, Decimal.class)
                .contains(value.getClass());
    }

    private static void mutations(Map<String, Object> original, List<Object> values) {
        for (String key : List.of("expressionShape", "resultSemanticType", "resultSemanticEvidence",
                "compositionSemantics", "returnType", "mongoExpressions")) {
            Map<String, Object> method = copy(original);
            if ("mongoExpressions".equals(key)) { method.put(key, List.of()); }
            else if ("returnType".equals(key)) { method.put(key, "Object"); }
            else { method.remove(key); }
            refuse(new Call(method, values, true), "缺 " + key);
        }
        for (String key : List.of("semanticEvidence", "conceptRef", "type")) {
            Map<String, Object> method = copy(original);
            if ("type".equals(key)) { parameters(method).get(0).put(key, "String"); }
            else { parameters(method).get(0).remove(key); }
            refuse(new Call(method, values, true), "缺参数 " + key);
        }
        for (String key : List.of("sourceEvidence", "javaResultRepresentation", "parameterBindings")) {
            Map<String, Object> method = copy(original);
            semantics(method).remove(key);
            refuse(new Call(method, values, true), "缺构造 " + key);
        }
        Map<String, Object> method = copy(original);
        map(semantics(method).get("applicability")).remove("expressionOperands");
        refuse(new Call(method, values, true), "缺 operand 数量/null 域");
    }

    private static void fixtures() throws Exception {
        Path root = Files.createTempDirectory("common-expression-evidence-");
        try {
            Path entry = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(entry.getParent());
            Files.writeString(entry, "package com.mongoplus.aggregate; public interface Aggregate<C> {}");
            Path file = root.resolve("neutral/Factory.java");
            Files.createDirectories(file.getParent());
            for (boolean sequence : List.of(false, true)) {
                String parameter = sequence ? "Object... alpha" : "Object alpha";
                String scope = sequence ? "ELEMENT" : "VALUE";
                String source = "package neutral; import org.bson.conversions.Bson; public class Factory {\n"
                        + "/** @mongoExpression $probe\n * @mongoExpressionShape ARRAY\n"
                        + " * @mongoParam alpha PIPELINE_EXPRESSION " + scope + "\n"
                        + " * @mongoComposition PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION\n"
                        + " * @mongoCandidate operation=EXPRESSION_ARGUMENTS collectionCodec=" + CODEC
                        + " resultJava=org.bson.Document\n"
                        + " * @mongoCandidateSource path=neutral/Factory.java symbols=assemble mechanism=explicit\n"
                        + " */ public static Bson assemble(" + parameter + ") {return null;} }";
                MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                        .addSourceRoot(root).pipeline(true).addExpressionRoot("neutral.Factory").build());
                Files.writeString(file, source);
                require("ESTABLISHED".equals(semantics(publicMethod(generator.generate(), "assemble")).get("proofStatus")),
                        "无关方法/操作符名沿用通用契约");
                positives++;
                for (String removed : List.of("@mongoExpressionShape ARRAY",
                        "@mongoParam alpha PIPELINE_EXPRESSION " + scope,
                        "@mongoComposition PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION")) {
                    Files.writeString(file, source.replace(removed, ""));
                    Map<?, ?> contract = semantics(publicMethod(generator.generate(), "assemble"));
                    require("NOT_ESTABLISHED".equals(contract.get("proofStatus")) && !contract.containsKey("bsonTerm"),
                            "真实源码缺依赖不闭合: " + removed);
                    negatives++;
                }
                for (String replacement : List.of("String alpha", "Object[] alpha")) {
                    Files.writeString(file, source.replace(parameter, replacement));
                    boolean rejected = false;
                    try {
                        rejected = "NOT_ESTABLISHED".equals(semantics(publicMethod(generator.generate(), "assemble"))
                                .get("proofStatus"));
                    } catch (IllegalArgumentException expected) {
                        rejected = true; // ELEMENT 与 scalar 不匹配时，逐参数检查先于 candidate 拒绝。
                    }
                    require(rejected, "错误真实 Java 类型不能闭合");
                    negatives++;
                }
                Files.writeString(file, source.replace(
                        " * @mongoCandidateSource path=neutral/Factory.java symbols=assemble mechanism=explicit\n", ""));
                boolean rejected = false;
                try { generator.generate(); } catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "缺来源必须拒绝生成");
                negatives++;
            }
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static Map<String, Object> method(String operator) {
        List<Map<String, Object>> methods = index.getMethodFamilies().stream().map(item -> (Map<?, ?>) item)
                .flatMap(family -> ((List<?>) family.get("overloads")).stream())
                .map(CommonExpressionEvidenceSelfTest::map)
                .filter(method -> ((List<?>) method.get("mongoExpressions")).contains(operator)).toList();
        require(methods.size() == 1, "本轮每个表达式只有一个 API，不新增 overload 选择");
        return methods.get(0);
    }

    private static Map<String, Object> publicMethod(MongoPlusApiIndex value, Map<?, ?> method) {
        return value.list("types").stream().map(CommonExpressionEvidenceSelfTest::map)
                .filter(type -> method.get("declaredIn").equals(type.get("qualifiedName")))
                .flatMap(type -> ((List<?>) type.get("publicMethods")).stream())
                .map(CommonExpressionEvidenceSelfTest::map)
                .filter(item -> method.get("signature").equals(item.get("signature"))).findFirst().orElseThrow();
    }

    private static Map<String, Object> publicMethod(MongoPlusApiIndex value, String name) {
        return value.list("types").stream().map(CommonExpressionEvidenceSelfTest::map)
                .flatMap(type -> ((List<?>) type.get("publicMethods")).stream())
                .map(CommonExpressionEvidenceSelfTest::map)
                .filter(method -> name.equals(method.get("name"))).findFirst().orElseThrow();
    }

    private static Map<String, Object> semantics(Map<?, ?> method) { return map(method.get("candidateSemantics")); }
    private static Map<?, ?> array(Map<?, ?> method) {
        return (Map<?, ?>) ((Map<?, ?>) semantics(method).get("bsonTerm")).get("value");
    }
    private static List<Map<String, Object>> parameters(Map<?, ?> method) {
        return ((List<?>) method.get("parameters")).stream().map(CommonExpressionEvidenceSelfTest::map).toList();
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) { return (Map<String, Object>) value; }
    @SuppressWarnings("unchecked")
    private static <T> T copy(T value) {
        if (value instanceof Map<?, ?> values) {
            Map<String, Object> result = new LinkedHashMap<>();
            values.forEach((key, item) -> result.put(key.toString(), copy(item)));
            return (T) result;
        }
        if (value instanceof List<?> values) { return (T) new ArrayList<>(values.stream().map(CommonExpressionEvidenceSelfTest::copy).toList()); }
        return value;
    }
    private static void pass(Call call, String message) { require(prove(call), message); positives++; }
    private static void refuse(Call call, String message) { require(!prove(call), message); negatives++; }
    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
