package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 复用正式参数、shape、composition 与变量 scope evidence，验证 expression → body → Stage。 */
public final class ExpressionCompositionEvidenceSelfTest {
    private static final String EXPRESSION = "PIPELINE_EXPRESSION";
    private static final String RELATION = "PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION";
    private static final String BODY_RELATION = "PIPELINE_EXPRESSION -> STAGE_BODY_DOCUMENT";
    private static final String CONCEPT = "PIPELINE_EXPRESSION_FIELD_REFERENCE";

    private ExpressionCompositionEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        MongoPlusApiIndex index = generate(Path.of(args[0]));
        Map<?, ?> equality = method(index, "mongoExpressions", "$eq");
        Map<?, ?> wrapper = method(index, "mongoExpressions", "$expr");
        Map<?, ?> stage = method(index, "mongoStages", "$match", "match(Bson bson)");
        require("com.mongoplus.aggregate.pipeline.AggregateOperator".equals(equality.get("declaredIn")),
                "不能把 Query eq 当作 expression");
        require("com.mongoplus.toolkit.Filters".equals(wrapper.get("declaredIn")), "复用真实 Filters.expr");
        require(parameters(equality).stream().map(p -> p.get("name")).toList().equals(List.of("left", "right")),
                "参数保持声明顺序");
        require(parameters(equality).stream().allMatch(p -> "Object".equals(p.get("type"))),
                "两个操作数均为 Object，接收 String、Integer 和 Bson");
        require("ARRAY".equals(equality.get("expressionShape")) && composition(equality, RELATION),
                "完整有序操作数及正式 expression result");
        require("Bson".equals(equality.get("returnType")) && composition(wrapper, BODY_RELATION),
                "Bson expression 进入泛型参数，输出 body");
        Map<?, ?> effect = (Map<?, ?>) stage.get("pipelineEffect");
        require(effect != null && "APPEND_STAGE".equals(effect.get("operation")), "match 必须追加 Stage");
        require(marked(parameters(stage).get(0), "STAGE_BODY_DOCUMENT"), "match 必须消费 body");
        require(((List<?>) index.asMap().get("requiredCapabilities")).containsAll(List.of(
                "VARIABLE_BINDING_SCOPE_V1", "ENTRY_CONTAINER_CONSTRUCTION_V1", "PIPELINE_CONSTRUCTION_V1")),
                "正式 capability 保留");
        List<List<Object>> examples = List.of(List.of("$_id", "$$userId"), List.of("$status", "PAID"), List.of(1, 1));
        List<List<String>> roles = List.of(List.of("FIELD_REFERENCE", "VARIABLE_REFERENCE"),
                List.of("FIELD_REFERENCE", "LITERAL"), List.of("LITERAL", "LITERAL"));
        for (int i = 0; i < examples.size(); i++) {
            List<Object> operands = examples.get(i);
            require(prove(index, equality, wrapper, stage, operands), "E0" + (i + 1) + " formal chain");
            require(operands.stream().map(value -> role(index, value)).toList().equals(roles.get(i)),
                    "E0" + (i + 1) + " operand 顺序与语义");
            System.out.println("E0" + (i + 1) + " PASS: " + roles.get(i) + "; Java "
                    + operands.stream().map(value -> value.getClass().getSimpleName()).toList());
        }
        require(prove(index, equality, wrapper, stage, List.of(Map.of("$eq", List.of(1, 1)), 1)),
                "nested expression 的正式结果进入 operand");
        require(prove(index, equality, wrapper, stage, List.of(1, Map.of("$eq", List.of(1, 1)))),
                "左右 operand 均接收 nested expression");
        require(!prove(index, equality, wrapper, stage, List.of(Map.of("$expr", Map.of("$eq", List.of(1, 1))), 1)),
                "STAGE_BODY_DOCUMENT 不能冒充 nested expression");
        require(!prove(index, equality, wrapper, stage, List.of("$_id", "$$missing")), "未绑定变量拒绝");
        negatives(Path.of(args[0]), examples.get(0));
        fixtures();
        System.out.println("ExpressionCompositionEvidenceSelfTest PASSED: reused composition; nested operands; "
                + "VARIABLE_BINDING_SCOPE_V1; missing evidence negatives; unrelated API fixture");
    }

    private static boolean prove(MongoPlusApiIndex index, Map<?, ?> call, Map<?, ?> wrapper, Map<?, ?> stage,
                                 List<Object> operands) {
        return expression(index, call, operands) && composition(wrapper, BODY_RELATION)
                && marked(parameters(wrapper).get(0), EXPRESSION)
                && "Bson".equals(call.get("returnType"))
                && "Bson".equals(wrapper.get("returnType"))
                && "TExpression".equals(parameters(wrapper).get(0).get("type"))
                && marked(parameters(stage).get(0), "STAGE_BODY_DOCUMENT")
                && "Bson".equals(parameters(stage).get(0).get("type"))
                && stage.get("pipelineEffect") instanceof Map;
    }

    private static boolean expression(MongoPlusApiIndex index, Map<?, ?> call, List<?> operands) {
        if (!composition(call, RELATION) || !"ARRAY".equals(call.get("expressionShape"))
                || !"Bson".equals(call.get("returnType")) || parameters(call).size() != operands.size()) {
            return false;
        }
        for (int i = 0; i < operands.size(); i++) {
            Map<?, ?> parameter = parameters(call).get(i);
            if (!marked(parameter, EXPRESSION) || !"Object".equals(parameter.get("type"))) { return false; }
            Object operand = operands.get(i);
            if (operand instanceof Map<?, ?> nested) {
                Map<?, ?> concept = expressionConcept(index);
                if (concept == null || !(concept.get("nestedExpression") instanceof Map<?, ?> representation)
                        || !EXPRESSION.equals(representation.get("semanticType"))
                        || !"BSON_DOCUMENT".equals(representation.get("encoding"))
                        || !Boolean.TRUE.equals(representation.get("requiresResultSemanticEvidence"))) { return false; }
                if (nested.size() != 1) { return false; }
                String operator = nested.keySet().iterator().next().toString();
                Map<?, ?> child = method(index, "mongoExpressions", operator);
                if (!(nested.get(operator) instanceof List<?> values) || !expression(index, child, values)) { return false; }
            } else {
                String role = role(index, operand);
                if (role == null) { return false; }
                if ("VARIABLE_REFERENCE".equals(role) && !"BOUND_VARIABLE".equals(
                        VariableBindingScopeSelfTest.boundReference(index, operand.toString(), Map.of("userId", "$userId")))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String role(MongoPlusApiIndex index, Object value) {
        Map<?, ?> concept = expressionConcept(index);
        if (concept == null) { return null; }
        if (value instanceof String text) {
            Map<?, ?> variable = (Map<?, ?>) concept.get("variableReference");
            Map<?, ?> field = (Map<?, ?>) concept.get("fieldReference");
            if (text.startsWith((String) variable.get("prefix"))) {
                return "VARIABLE_REFERENCE".equals(variable.get("semanticType"))
                        && "UNCHANGED".equals(variable.get("encoding"))
                        && "VARIABLE_BINDING_SCOPE_V1".equals(variable.get("bindingContractRef"))
                        ? "VARIABLE_REFERENCE" : null;
            }
            if (text.startsWith((String) field.get("prefix"))
                    && !text.startsWith((String) field.get("excludedPrefix"))) {
                return "UNCHANGED".equals(field.get("encoding")) ? "FIELD_REFERENCE" : null;
            }
            return "UNCHANGED".equals(((Map<?, ?>) concept.get("plainStringValue")).get("encoding")) ? "LITERAL" : null;
        }
        return value instanceof Integer && concept.get("literalValue") instanceof Map<?, ?> literal
                && "LITERAL".equals(literal.get("semanticType"))
                && "RUNTIME_CODEC".equals(literal.get("encoding"))
                && Boolean.TRUE.equals(literal.get("requiresCodecEvidence")) ? "LITERAL" : null;
    }

    private static Map<?, ?> expressionConcept(MongoPlusApiIndex index) {
        return index.list("concepts").stream().map(c -> (Map<?, ?>) c)
                .filter(c -> CONCEPT.equals(c.get("id"))).findFirst().orElse(null);
    }

    private static void negatives(Path project, List<Object> operands) throws Exception {
        for (String removed : List.of("left", "right", "expression", "bson", "expressionShape",
                "resultSemanticEvidence", "concept", "pipelineEffect")) {
            MongoPlusApiIndex index = generate(project);
            Map<?, ?> call = method(index, "mongoExpressions", "$eq");
            Map<?, ?> wrapper = method(index, "mongoExpressions", "$expr");
            Map<?, ?> stage = method(index, "mongoStages", "$match", "match(Bson bson)");
            if ("concept".equals(removed)) { index.list("concepts").clear(); }
            else if ("expressionShape".equals(removed) || "resultSemanticEvidence".equals(removed)) { call.remove(removed); }
            else if ("pipelineEffect".equals(removed)) { stage.remove(removed); }
            else {
                Map<?, ?> owner = "expression".equals(removed) ? wrapper : "bson".equals(removed) ? stage : call;
                parameters(owner).stream().filter(p -> removed.equals(p.get("name"))).findFirst().orElseThrow()
                        .remove("semanticEvidence");
            }
            require(!prove(index, call, wrapper, stage, operands), "缺少 " + removed + " 时不得补全");
        }
        for (String removed : List.of("literalValue", "nestedExpression")) {
            MongoPlusApiIndex index = generate(project);
            expressionConcept(index).remove(removed);
            require(!expression(index, method(index, "mongoExpressions", "$eq"),
                    "literalValue".equals(removed) ? List.of(1, 1) : List.of(Map.of("$eq", List.of(1, 1)), 1)),
                    "缺少结构化 " + removed + " 时不得从文字描述推断");
        }
    }

    private static void fixtures() throws Exception {
        Path root = Files.createTempDirectory("expression-composition-");
        try {
            Path entry = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(entry.getParent());
            Files.writeString(entry, "package com.mongoplus.aggregate; public interface Aggregate<C> {}");
            Path file = root.resolve("unrelated/Factory.java");
            Files.createDirectories(file.getParent());
            String source = "package unrelated; import org.bson.conversions.Bson; public class Factory {\n"
                    + "/** @mongoExpression $probe\n * @mongoExpressionShape ARRAY\n"
                    + " * @mongoParam alpha PIPELINE_EXPRESSION VALUE\n"
                    + " * @mongoParam beta PIPELINE_EXPRESSION VALUE\n * @mongoComposition " + RELATION + "\n */\n"
                    + "public static Bson assemble(Object alpha, Object beta) {return null;}\n"
                    + "/** @mongoExpression $host\n * @mongoParam child PIPELINE_EXPRESSION VALUE\n"
                    + " * @mongoComposition " + BODY_RELATION + "\n */\n"
                    + "public static <V> Bson enclose(V child) {return null;} }";
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                    .addSourceRoot(root).pipeline(true).addExpressionRoot("unrelated.Factory").build());
            Files.writeString(file, source);
            require(composition(method(generator.generate(), "mongoExpressions", "$probe"), RELATION),
                    "无关方法名与操作符复用相同 composition");
            require(composition(method(generator.generate(), "mongoExpressions", "$host"), BODY_RELATION),
                    "无关 parent API 复用相同 body composition");
            for (String tag : List.of("@mongoParam alpha PIPELINE_EXPRESSION VALUE",
                    "@mongoParam beta PIPELINE_EXPRESSION VALUE", "@mongoComposition " + RELATION,
                    "@mongoExpressionShape ARRAY")) {
                Files.writeString(file, source.replace(tag, ""));
                Map<?, ?> call = method(generator.generate(), "mongoExpressions", "$probe");
                require(!composition(call, RELATION) || !"ARRAY".equals(call.get("expressionShape"))
                        || parameters(call).stream().anyMatch(p -> !marked(p, EXPRESSION)),
                        "删真实源码标签后不按名称/类型补证据");
            }
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static boolean composition(Map<?, ?> method, String relation) {
        return List.of(relation).equals(method.get("compositionSemantics"))
                && relation.substring(relation.indexOf("->") + 2).trim().equals(method.get("resultSemanticType"))
                && Map.of("source", "JAVADOC", "tag", "mongoComposition", "value", relation)
                .equals(method.get("resultSemanticEvidence"));
    }

    private static boolean marked(Map<?, ?> parameter, String semantic) {
        return semantic.equals(parameter.get("semanticType")) && "VALUE".equals(parameter.get("semanticScope"))
                && (EXPRESSION.equals(semantic) ? CONCEPT : "PIPELINE_PARAMETER_" + semantic)
                .equals(parameter.get("conceptRef"))
                && Map.of("source", "JAVADOC", "tag", "mongoParam", "value",
                parameter.get("name") + " " + semantic + " VALUE").equals(parameter.get("semanticEvidence"));
    }

    private static MongoPlusApiIndex generate(Path root) throws Exception {
        return new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(root).build()).generate();
    }

    private static Map<?, ?> method(MongoPlusApiIndex index, String mapping, String operator) {
        return method(index, mapping, operator, null);
    }

    private static Map<?, ?> method(MongoPlusApiIndex index, String mapping, String operator, String signature) {
        return index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                .flatMap(f -> ((List<?>) f.get("overloads")).stream()).map(m -> (Map<?, ?>) m)
                .filter(m -> ((List<?>) m.get(mapping)).contains(operator))
                .filter(m -> signature == null || signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }

    private static List<Map<?, ?>> parameters(Map<?, ?> method) {
        return ((List<?>) method.get("parameters")).stream().<Map<?, ?>>map(p -> (Map<?, ?>) p).toList();
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
