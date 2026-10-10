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

/** 按通用构造项验证封闭子树；不搜索/选择 overload，不以原始 BSON 作为表达式证明。 */
public final class ExpressionResultTypeEvidenceSelfTest {
    private static final String EXPRESSION = "PIPELINE_EXPRESSION";
    private static final String CONCEPT = "PIPELINE_EXPRESSION_FIELD_REFERENCE";
    private static MongoPlusApiIndex index;
    private static int positive;
    private static int negative;

    private ExpressionResultTypeEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        index = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(Path.of(args[0])).build()).generate();
        Map<?, ?> relation = (Map<?, ?>) ((List<?>) index.asMap().get("javaTypeRelations")).get(0);
        require("JAVA_TYPE_RELATIONS_V1".equals(relation.get("capability"))
                && Boolean.TRUE.equals(relation.get("assignable"))
                && Boolean.FALSE.equals(relation.get("reverseAssignable")), "真实 artifact 单向赋值关系");
        require(((List<?>) relation.get("directInterfaces")).contains("org.bson.conversions.Bson")
                && "java.lang.Object".equals(relation.get("directSuperclass")), "反射公开父类型");
        require(Boolean.FALSE.equals(relation.get("semanticRoleImplied"))
                && Boolean.FALSE.equals(relation.get("runtimeCodecImplied"))
                && Boolean.FALSE.equals(relation.get("bsonEquivalenceImplied")), "四种证明互不替代");
        Map<?, ?> source = (Map<?, ?>) relation.get("sourceEvidence");
        require(Boolean.TRUE.equals(source.get("verified"))
                && source.get("artifactSha256").toString().matches("[A-F0-9]{64}")
                && source.get("subtypeClassSha256").toString().matches("[A-F0-9]{64}"), "artifact 和 class 字节指纹");
        require(assignable(index, "org.bson.Document", "org.bson.conversions.Bson"), "Document -> Bson"); positive++;
        require(!assignable(index, "org.bson.conversions.Bson", "org.bson.Document"), "反向赋值拒绝"); negative++;
        MongoPlusApiIndex missingTypes = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(Path.of(args[0])).build()).generate();
        missingTypes.asMap().remove("javaTypeRelations");
        require(!assignable(missingTypes, "org.bson.Document", "org.bson.conversions.Bson"), "缺事实不硬编码 Document"); negative++;

        Call multiply = call("multiply(Object... values)", call("toInt(TExpression expression)", "$price"), "$quantity");
        Call gt = call("gt(Object left, Object right)", call("toInt(TExpression expression)", "$score"), 60);
        Call ifNull = call("ifNull(Object... inputExpressions)", "$price", 0);
        Call concat = call("concatArraysExpressions(Object... operands)", "$items", "$other");
        Call merge = call("mergeObjects(Object... values)", "$defaults", "$profile");
        Call cond = call("condArray(Object ifValue, Object thenValue, Object elseValue)",
                call("gt(Object left, Object right)", "$amount", 100), call("toString(TExpression expression)", "$amount"), "LOW");
        for (Call tree : List.of(multiply, gt, ifNull, concat, merge, cond)) { pass(tree, "六个输入的 expression 子树"); }
        pass(call("mergeObjects(Collection<?> values)", merge,
                call("ifNull(List<?> inputExpressions)", "$profile", null)), "Document 及 Bson 声明的嵌套表达式");
        pass(call("toInt(TExpression expression)", merge), "语义和编码闭合不等于服务端类型合法");
        for (Map<String, Object> method : methods()) {
            if (!newlyAudited(method)) { continue; }
            List<Map<String, Object>> parameters = parameters(method);
            int arity = "ELEMENT".equals(parameters.get(0).get("semanticScope")) ? 2 : parameters.size();
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < arity; i++) { values.add(i == 0 ? "$value" : i); }
            pass(new Call(method, values, true), "逐 overload 的来源/参数/构造闭合");
            for (Object value : Arrays.asList("$field", "ordinary", "$$bound", 1, 2147483648L, 1.5D, true, null)) {
                if ("String".equals(parameters.get(0).get("type")) && !(value instanceof String) && value != null) { continue; }
                List<Object> literals = new ArrayList<>(values);
                literals.set(0, value);
                pass(new Call(method, literals, true), "叶子 codec 与引用角色");
            }
            for (String key : List.of("resultSemanticType", "resultSemanticEvidence", "compositionSemantics",
                    "expressionShape", "mongoExpressions", "returnType", "staticMethod")) {
                Map<String, Object> changed = copy(method);
                changed.remove(key);
                refuse(new Call(changed, values, true), "缺 " + key);
            }
            for (String key : List.of("sourceEvidence", "bsonTerm", "javaResultRepresentation", "parameterBindings", "applicability")) {
                Map<String, Object> changed = copy(method);
                map(changed.get("candidateSemantics")).remove(key);
                refuse(new Call(changed, values, true), "缺构造 " + key);
            }
            for (String key : List.of("semanticEvidence", "conceptRef", "semanticScope", "type")) {
                Map<String, Object> changed = copy(method);
                parameters(changed).get(0).remove(key);
                refuse(new Call(changed, values, true), "缺参数 " + key);
            }
            List<Object> unbound = new ArrayList<>(values); unbound.set(0, "$$missing");
            refuse(new Call(method, unbound, true), "未绑定变量");
            List<Object> unknown = new ArrayList<>(values); unknown.set(0, new Object());
            refuse(new Call(method, unknown, true), "未知 Java 类型");
            refuse(new Call(method, values, false), "自定义或未知 codec 无独立证明");
        }
        Map<String, Object> child = copy(method("toInt(TExpression expression)"));
        child.remove("resultSemanticEvidence");
        refuse(call("multiply(Object... values)", new Call(child, List.of("$price"), true), 2), "深层缺 result");
        refuse(call("multiply(Object... values)", Map.of("$toInt", "$price"), 2), "raw BSON 无来源");
        refuse(call("ifNull(Object... inputExpressions)", new UnprovedCodec("java.lang.Integer", 0)), "Java 类型不能代替 codec");
        refuse(call("concatArraysExpressions(Object... operands)", List.of(1, 2), "$other"), "数组 literal 尚无独立递归 literal 构造证据");
        for (Map<String, Object> method : methods()) {
            if (!"mergeObjects".equals(method.get("name")) || !method.get("declaredIn").toString().endsWith("Accumulators")) { continue; }
            refuse(new Call(method, List.of("$profile"), true), "accumulator 不能作为普通 expression");
        }
        fixtures();
        System.out.println("ExpressionResultTypeEvidenceSelfTest PASSED: " + positive + " positive; " + negative
                + " rejection checks; recursive source/result/type/codec/scope proof; no MCP selection");
    }

    private record Call(Map<String, Object> method, List<Object> values, boolean codecsProven) { }
    private record UnprovedCodec(String javaType, Object value) { }

    private static Call call(String signature, Object... values) { return new Call(method(signature), Arrays.asList(values), true); }

    private static boolean prove(Call call) {
        Map<String, Object> method = call.method();
        List<Map<String, Object>> parameters = parameters(method);
        Map<String, Object> candidate = map(method.get("candidateSemantics"));
        if (!call.codecsProven() || candidate == null || parameters.isEmpty()
                || !Boolean.TRUE.equals(method.get("staticMethod"))
                || !Set.of("Bson", "Document").contains(String.valueOf(method.get("returnType")))
                || !EXPRESSION.equals(method.get("resultSemanticType"))
                || !"ESTABLISHED".equals(candidate.get("proofStatus"))
                || !"org.bson.Document".equals(candidate.get("javaResultRepresentation"))
                || !(candidate.get("sourceEvidence") instanceof List<?> sources) || sources.size() < 2
                || !(candidate.get("parameterBindings") instanceof List<?> bindings) || bindings.size() != parameters.size()
                || !(candidate.get("applicability") instanceof Map<?, ?> applicability)
                || !"INDEPENDENT_SEMANTIC_AND_SCOPE_VALIDATION".equals(applicability.get("bindingAdmission"))) { return false; }
        String relation = String.join(" + ", java.util.Collections.nCopies(parameters.size(), EXPRESSION)) + " -> " + EXPRESSION;
        if (!List.of(relation).equals(method.get("compositionSemantics"))
                || !Map.of("source", "JAVADOC", "tag", "mongoComposition", "value", relation)
                        .equals(method.get("resultSemanticEvidence"))) { return false; }
        for (int i = 0; i < parameters.size(); i++) {
            Map<String, Object> parameter = parameters.get(i);
            Map<?, ?> binding = (Map<?, ?>) bindings.get(i);
            if (!EXPRESSION.equals(parameter.get("semanticType")) || !CONCEPT.equals(parameter.get("conceptRef"))
                    || parameter.get("type") == null || !parameter.get("type").equals(binding.get("declaredJavaType"))
                    || !(binding.get("javaRepresentation") instanceof Map<?, ?>)
                    || !Set.of("ELEMENT", "VALUE").contains(String.valueOf(parameter.get("semanticScope")))
                    || !Map.of("source", "JAVADOC", "tag", "mongoParam", "value", parameter.get("name")
                        + " " + EXPRESSION + " " + parameter.get("semanticScope")).equals(parameter.get("semanticEvidence"))) { return false; }
        }
        Map<String, Object> term = map(candidate.get("bsonTerm"));
        if (term == null || !"DOCUMENT".equals(term.get("op")) || !List.of(term.get("key")).equals(method.get("mongoExpressions"))
                || !(method.get("mongoStages") instanceof List<?> stages) || !stages.isEmpty()) { return false; }
        Map<String, Object> value = map(term.get("value"));
        if (value == null) { return false; }
        String op = String.valueOf(value.get("op"));
        boolean sequence = "ARRAY_RUNTIME_CODEC".equals(op);
        if (!sequence && call.values().size() != parameters.size()) { return false; }
        switch (op) {
            case "ARRAY_RUNTIME_CODEC":
                if (parameters.size() != 1 || !"ELEMENT".equals(parameters.get(0).get("semanticScope"))
                        || !input(value.get("input"), parameters.get(0)) || !"ITERATION".equals(value.get("order"))) { return false; }
                break;
            case "ARRAY_ARGUMENTS_RUNTIME_CODEC":
                if (!"DECLARATION".equals(value.get("order")) || !(value.get("inputs") instanceof List<?> inputs)
                        || inputs.size() != parameters.size()) { return false; }
                for (int i = 0; i < inputs.size(); i++) { if (!input(inputs.get(i), parameters.get(i))) { return false; } }
                break;
            case "RUNTIME_CODEC_VALUE":
                if (parameters.size() != 1 || !input(value.get("input"), parameters.get(0))) { return false; }
                break;
            case "ORDERED_DOCUMENT_ARGUMENTS":
                if (!"DECLARATION".equals(value.get("order"))
                        || !"org.bson.codecs.DocumentCodec".equals(value.get("requiredDocumentCodec"))
                        || !(value.get("entries") instanceof List<?> entries) || entries.size() != parameters.size()) { return false; }
                for (int i = 0; i < entries.size(); i++) {
                    if (!input(((Map<?, ?>) entries.get(i)).get("value"), parameters.get(i))) { return false; }
                }
                break;
            default: return false;
        }
        String expectedShape = op.startsWith("ARRAY") ? "ARRAY" : "ORDERED_DOCUMENT_ARGUMENTS".equals(op) ? "OBJECT" : "VALUE";
        if (!expectedShape.equals(method.get("expressionShape"))
                || op.startsWith("ARRAY") && !"org.bson.codecs.CollectionCodec".equals(value.get("requiredCollectionCodec"))) { return false; }
        for (int i = 0; i < call.values().size(); i++) {
            Map<String, Object> parameter = parameters.get(sequence ? 0 : i);
            Object operand = call.values().get(i);
            if (!operand(operand) || "String".equals(parameter.get("type")) && operand != null && !(operand instanceof String)) { return false; }
        }
        return true;
    }

    private static boolean input(Object raw, Map<?, ?> parameter) {
        return raw instanceof Map<?, ?> value && "INPUT".equals(value.get("op")) && parameter.get("name").equals(value.get("parameter"));
    }

    private static boolean operand(Object value) {
        Map<?, ?> concept = index.list("concepts").stream().map(item -> (Map<?, ?>) item)
                .filter(item -> CONCEPT.equals(item.get("id"))).findFirst().orElseThrow();
        if (value instanceof Call child) {
            Map<?, ?> nested = (Map<?, ?>) concept.get("nestedExpression");
            return Boolean.TRUE.equals(nested.get("requiresResultSemanticEvidence")) && prove(child);
        }
        if (value instanceof String text) {
            String kind = text.startsWith("$$") ? "variableReference" : text.startsWith("$") ? "fieldReference" : "plainStringValue";
            Map<?, ?> rule = (Map<?, ?>) concept.get(kind);
            return "UNCHANGED".equals(rule.get("encoding")) && (!text.startsWith("$$")
                    || "BOUND_VARIABLE".equals(VariableBindingScopeSelfTest.boundReference(index, text, Map.of("bound", "$value"))));
        }
        Map<?, ?> literal = (Map<?, ?>) concept.get("literalValue");
        return Boolean.TRUE.equals(literal.get("requiresCodecEvidence")) && Boolean.TRUE.equals(literal.get("requiresAssignableJavaType"))
                && (value == null || Set.of(Integer.class, Long.class, Double.class, Boolean.class).contains(value.getClass()));
    }

    private static boolean assignable(MongoPlusApiIndex value, String from, String to) {
        if (!(value.asMap().get("javaTypeRelations") instanceof List<?> facts)) { return false; }
        return facts.stream().map(item -> (Map<?, ?>) item).anyMatch(fact -> from.equals(fact.get("subtype"))
                && to.equals(fact.get("supertype")) && Boolean.TRUE.equals(fact.get("assignable"))
                && "ESTABLISHED".equals(fact.get("proofStatus")) && fact.get("sourceEvidence") instanceof Map<?, ?> source
                && Boolean.TRUE.equals(source.get("verified")));
    }

    private static void fixtures() throws Exception {
        Path root = Files.createTempDirectory("expression-result-type-");
        try {
            Path entry = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(entry.getParent()); Files.writeString(entry, "package com.mongoplus.aggregate; public interface Aggregate<C> {}");
            Path file = root.resolve("neutral/Factory.java"); Files.createDirectories(file.getParent());
            String source = "package neutral; import org.bson.conversions.Bson; /**\n"
                    + " * @mongoJavaTypeRelation artifact=org.mongodb:bson:5.4.0 subtype=org.bson.Document supertype=java.util.Map\n"
                    + " */ public class Factory {\n/**\n * @mongoExpression $probe\n * @mongoExpressionShape VALUE\n"
                    + " * @mongoParam alpha PIPELINE_EXPRESSION VALUE\n * @mongoComposition PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION\n"
                    + " * @mongoCandidate operation=EXPRESSION_VALUE resultJava=org.bson.Document\n"
                    + " * @mongoCandidateSource path=neutral/Factory.java symbols=assemble mechanism=explicit\n"
                    + " */ public static <E> Bson assemble(E alpha) {return null;} }";
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder().addSourceRoot(root)
                    .pipeline(true).addExpressionRoot("neutral.Factory").constructionArtifactRepository(
                            MongoPlusIndexerConfig.forPipelineProject(Path.of(".")).build().getConstructionArtifactRepository()).build());
            Files.writeString(file, source);
            MongoPlusApiIndex fixture = generator.generate();
            require(assignable(fixture, "org.bson.Document", "java.util.Map"), "类型目标由声明决定，不按 Bson 类名硬编码"); positive++;
            require("ESTABLISHED".equals(((Map<?, ?>) fixtureMethod(fixture).get("candidateSemantics"))
                    .get("proofStatus")), "中性 operator/generic 工厂复用通用标量契约"); positive++;
            for (String removed : List.of("@mongoExpressionShape VALUE", "@mongoParam alpha PIPELINE_EXPRESSION VALUE",
                    "@mongoComposition PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION")) {
                Files.writeString(file, source.replace(removed, ""));
                Map<?, ?> method = fixtureMethod(generator.generate());
                require("NOT_ESTABLISHED".equals(((Map<?, ?>) method.get("candidateSemantics")).get("proofStatus")), "缺真实源码依赖拒绝"); negative++;
            }
            for (String invalid : List.of(source.replace("<E>", "<E extends Number>"),
                    source.replace("supertype=java.util.Map", "supertype=missing.Type"),
                    source.replace("artifact=org.mongodb:bson:5.4.0", "artifact=org.mongodb:bson:0.0.0"),
                    source.replace("resultJava=org.bson.Document", "resultJava=java.lang.Object"))) {
                Files.writeString(file, invalid);
                boolean rejected = false;
                try {
                    MongoPlusApiIndex changed = generator.generate();
                    rejected = changed.list("types").stream().map(item -> (Map<?, ?>) item)
                            .flatMap(type -> ((List<?>) type.get("publicMethods")).stream()).map(item -> (Map<?, ?>) item)
                            .filter(method -> "assemble".equals(method.get("name")))
                            .anyMatch(method -> "NOT_ESTABLISHED".equals(((Map<?, ?>) method.get("candidateSemantics")).get("proofStatus")));
                } catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "缺 artifact/type/bound/result 来源不能闭合"); negative++;
            }
            Files.writeString(file, source.replace(
                    " * @mongoCandidateSource path=neutral/Factory.java symbols=assemble mechanism=explicit\n", ""));
            boolean missingSourceRejected = false;
            try { generator.generate(); } catch (IllegalArgumentException expected) { missingSourceRejected = true; }
            require(missingSourceRejected, "缺真实源码来源必须拒绝生成"); negative++;
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static Map<?, ?> fixtureMethod(MongoPlusApiIndex value) {
        return value.list("types").stream().map(item -> (Map<?, ?>) item)
                .flatMap(type -> ((List<?>) type.get("publicMethods")).stream()).map(item -> (Map<?, ?>) item)
                .filter(method -> "assemble".equals(method.get("name"))).findFirst().orElseThrow();
    }

    private static boolean newlyAudited(Map<?, ?> method) {
        return "com.mongoplus.conditions.operation.ConditionOperators".equals(method.get("declaredIn"))
                && method.containsKey("resultSemanticEvidence") || "concatArraysExpressions".equals(method.get("name"));
    }
    private static List<Map<String, Object>> methods() {
        return index.getMethodFamilies().stream().map(item -> (Map<?, ?>) item)
                .flatMap(family -> ((List<?>) family.get("overloads")).stream()).map(ExpressionResultTypeEvidenceSelfTest::map).toList();
    }
    private static Map<String, Object> method(String signature) {
        return methods().stream().filter(method -> signature.equals(method.get("signature"))).findFirst().orElseThrow();
    }
    private static List<Map<String, Object>> parameters(Map<?, ?> method) {
        return ((List<?>) method.get("parameters")).stream().map(ExpressionResultTypeEvidenceSelfTest::map).toList();
    }
    @SuppressWarnings("unchecked") private static Map<String, Object> map(Object value) { return (Map<String, Object>) value; }
    @SuppressWarnings("unchecked") private static <T> T copy(T value) {
        if (value instanceof Map<?, ?> values) {
            Map<String, Object> result = new LinkedHashMap<>(); values.forEach((key, item) -> result.put(key.toString(), copy(item))); return (T) result;
        }
        if (value instanceof List<?> values) { return (T) new ArrayList<>(values.stream().map(ExpressionResultTypeEvidenceSelfTest::copy).toList()); }
        return value;
    }
    private static void pass(Call call, String message) { require(prove(call), message); positive++; }
    private static void refuse(Call call, String message) { require(!prove(call), message); negative++; }
    private static void require(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } }
}
