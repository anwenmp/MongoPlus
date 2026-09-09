package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 消费输入 Pipeline 和 Index；逐参数审计表不参与调用选择，也不预置 Java 调用代码。 */
public final class PipelineExpressionCoverageSelfTest {
    private PipelineExpressionCoverageSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        MongoPlusApiIndex index = generate(root);
        List<Map<String, Object>> multiply = List.of(Map.of("$project",
                Map.of("total", Map.of("$multiply", List.of("$price", "$quantity")))));
        List<Map<String, Object>> concat = List.of(Map.of("$project", Map.of("displayName",
                Map.of("$concat", List.of(Map.of("$ifNull", List.of("$nickName", "$userName")), "-VIP")))));
        // 两种容器重载分别证明，防止其中一个重载掩盖另一个缺失的标签。
        for (boolean varargs : List.of(true, false)) {
            require(provePipeline(index, multiply, varargs), "multiply Pipeline FAIL, varargs=" + varargs);
            require(provePipeline(index, concat, varargs), "concat/ifNull Pipeline FAIL, varargs=" + varargs);
        }
        verifyAccumulatorOutputFields(index);
        verifyAudit(index, root);
        verifyDeclaredTypeLimits(index);
        // 去掉单个操作符的参数证据，即使方法名、映射、类型和其他重载仍在，也必须失败。
        for (String operator : List.of("$multiply", "$concat", "$ifNull")) {
            MongoPlusApiIndex missing = generate(root);
            for (Map<?, ?> method : methods(missing, "mongoExpressions", operator)) {
                for (Map<?, ?> parameter : parameters(method)) { parameter.remove("semanticEvidence"); }
            }
            require(!provePipeline(missing, "$multiply".equals(operator) ? multiply : concat, true),
                    "缺少 " + operator + " 的显式 evidence 时不得证明调用");
        }
        MongoPlusApiIndex missingConcept = generate(root);
        missingConcept.list("concepts").clear();
        require(!provePipeline(missingConcept, concat, true), "缺少 concept 时不得推断字符串表示");
        MongoPlusApiIndex badReturn = generate(root);
        for (Map<?, ?> method : methods(badReturn, "mongoExpressions", "$ifNull")) {
            set(method, "returnType", "void");
        }
        require(!provePipeline(badReturn, concat, true), "嵌套返回类型不兼容时不得证明调用");
        MongoPlusApiIndex badScope = generate(root);
        for (Map<?, ?> method : methods(badScope, "mongoExpressions", "$concat")) {
            for (Map<?, ?> parameter : parameters(method)) { set(parameter, "semanticScope", "VALUE"); }
        }
        require(!provePipeline(badScope, concat, true), "不能把容器的 VALUE 证据当成 ELEMENT");
        System.out.println("Pipeline 1 ($multiply): PASS; Pipeline 2 ($concat/$ifNull): PASS"
                + "; varargs + collection; field reference + plain String + nested Bson");
        System.out.println("PipelineExpressionCoverageSelfTest PASSED: 49 families, 157 overloads, 372 parameters");
    }

    private static MongoPlusApiIndex generate(Path root) throws Exception {
        return new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(root).build()).generate();
    }

    private static boolean provePipeline(MongoPlusApiIndex index, List<Map<String, Object>> pipeline,
                                         boolean varargs) {
        Map<?, ?> concept = maps(index.list("concepts")).stream()
                .filter(item -> "PIPELINE_EXPRESSION_FIELD_REFERENCE".equals(item.get("id")))
                .findFirst().orElse(null);
        if (concept == null) { return false; }
        for (Map<String, Object> stage : pipeline) {
            for (Map.Entry<String, Object> entry : stage.entrySet()) {
                // 从已索引的公开方法签名中寻找 String 输出名 + VALUE 的 BSON 构造入口。
                for (Object expression : ((Map<?, ?>) entry.getValue()).values()) {
                    Set<String> returns = expressionTypes(index, expression, concept, varargs);
                    boolean connected = false;
                    for (Map<?, ?> type : maps(index.list("types"))) {
                        for (Map<?, ?> builder : maps((List<?>) type.get("publicMethods"))) {
                            List<Map<?, ?>> params = parameters(builder);
                            if (params.size() != 2 || !"String".equals(params.get(0).get("type"))
                                    || !"Bson".equals(builder.get("returnType"))
                                    || !Boolean.TRUE.equals(builder.get("staticMethod"))
                                    || !marked(params.get(1), "VALUE", concept)) { continue; }
                            for (String returned : returns) {
                                if (!accepts(builder, (String) params.get(1).get("type"), returned)) { continue; }
                                connected |= methods(index, "mongoStages", entry.getKey()).stream()
                                        .anyMatch(method -> parameters(method).size() == 1
                                                && "Bson".equals(parameters(method).get(0).get("type")));
                            }
                        }
                    }
                    if (!connected) { return false; }
                }
            }
        }
        return true;
    }

    private static Set<String> expressionTypes(MongoPlusApiIndex index, Object input, Map<?, ?> concept,
                                                boolean varargs) {
        if (input instanceof String) {
            String value = (String) input;
            String representation = value.startsWith("$$") ? "variableReference"
                    : value.startsWith("$") ? "fieldReference" : "plainStringValue";
            Map<?, ?> rule = (Map<?, ?>) concept.get(representation);
            if (rule == null || !"UNCHANGED".equals(rule.get("encoding"))
                    || !"java.lang.String".equals(rule.get("javaType"))) { return Set.of(); }
            if (rule.containsKey("prefix") && !value.startsWith((String) rule.get("prefix"))) { return Set.of(); }
            if (rule.containsKey("excludedPrefix") && value.startsWith((String) rule.get("excludedPrefix"))) {
                return Set.of();
            }
            return Set.of("String");
        }
        if (!(input instanceof Map) || ((Map<?, ?>) input).size() != 1) { return Set.of(); }
        Map.Entry<?, ?> entry = ((Map<?, ?>) input).entrySet().iterator().next();
        if (!(entry.getValue() instanceof List)) { return Set.of(); }
        Set<String> result = new HashSet<>();
        for (Map<?, ?> method : methods(index, "mongoExpressions", (String) entry.getKey())) {
            List<Map<?, ?>> params = parameters(method);
            if (params.size() != 1 || !"Bson".equals(method.get("returnType"))
                    || !Boolean.TRUE.equals(method.get("staticMethod"))) { continue; }
            Map<?, ?> parameter = params.get(0);
            if (!marked(parameter, "ELEMENT", concept)
                    || varargs != Boolean.TRUE.equals(parameter.get("varargs"))) { continue; }
            String elementType = elementType((String) parameter.get("type"));
            if (elementType == null) { continue; }
            boolean compatible = true;
            for (Object operand : (List<?>) entry.getValue()) {
                Set<String> operandTypes = expressionTypes(index, operand, concept, varargs);
                compatible &= operandTypes.stream().anyMatch(t -> accepts(method, elementType, t));
            }
            if (compatible) { result.add((String) method.get("returnType")); }
        }
        return result;
    }

    private static String elementType(String type) {
        if (type.endsWith("[]")) { return type.substring(0, type.length() - 2); }
        if (type.endsWith("...")) { return type.substring(0, type.length() - 3); }
        if ((type.startsWith("List<") || type.startsWith("Collection<")) && type.endsWith(">")) {
            String element = type.substring(type.indexOf('<') + 1, type.length() - 1);
            return "?".equals(element) ? "Object" : element;
        }
        return null;
    }

    private static boolean accepts(Map<?, ?> method, String target, String source) {
        if ("void".equals(source)) { return false; }
        if (target.equals(source) || "Object".equals(target)) { return true; }
        // 只接受 Index 显式声明的无界方法泛型，不根据 TExpression 等名字推断。
        return ((List<?>) method.get("typeParameters")).contains(target);
    }

    private static boolean marked(Map<?, ?> parameter, String scope, Map<?, ?> concept) {
        if (!"PIPELINE_EXPRESSION".equals(parameter.get("semanticType"))
                || !scope.equals(parameter.get("semanticScope"))
                || !concept.get("id").equals(parameter.get("conceptRef"))) { return false; }
        if (!(parameter.get("semanticEvidence") instanceof Map)) { return false; }
        Map<?, ?> evidence = (Map<?, ?>) parameter.get("semanticEvidence");
        return "JAVADOC".equals(evidence.get("source")) && "mongoParam".equals(evidence.get("tag"))
                && (parameter.get("name") + " PIPELINE_EXPRESSION " + scope).equals(evidence.get("value"));
    }

    /** semantic evidence 不放宽 Java 类型；受限的 String/List/Number 不变成 Object。 */
    private static void verifyDeclaredTypeLimits(MongoPlusApiIndex index) {
        for (Map<?, ?> method : methods(index, "mongoExpressions", "$concatArrays")) {
            String element = elementType((String) parameters(method).get(0).get("type"));
            require(!accepts(method, element, "String") && !accepts(method, element, "Bson"),
                    "concatArrays 的每个外层操作数仍必须为 List");
        }
        for (Map<?, ?> method : methods(index, "mongoExpressions", "$accumulator")) {
            for (Map<?, ?> parameter : parameters(method)) {
                if (!"ELEMENT".equals(parameter.get("semanticScope"))) { continue; }
                String element = elementType((String) parameter.get("type"));
                require(accepts(method, element, "String") && !accepts(method, element, "Bson"),
                        "accumulator 参数数组仍只接收 String");
            }
        }
        for (Map<?, ?> method : methods(index, "mongoExpressions", "$substrBytes")) {
            for (Map<?, ?> parameter : parameters(method)) {
                if (!"Number".equals(parameter.get("type"))) { continue; }
                require(!accepts(method, "Number", "String") && !accepts(method, "Number", "Bson"),
                        "数值常量重载不能被 concept 放宽");
            }
        }
    }

    /** 全部已收录 overload 独立检查；无参 sum 以及 expression getter 不能产生输出名槽。 */
    private static void verifyAccumulatorOutputFields(MongoPlusApiIndex index) {
        int families = 0;
        int overloads = 0;
        int stringNames = 0;
        int getterNames = 0;
        for (Map<?, ?> family : maps(index.getMethodFamilies())) {
            List<Map<?, ?>> accumulators = maps((List<?>) family.get("overloads")).stream()
                    .filter(method -> "com.mongoplus.aggregate.pipeline.Accumulators".equals(method.get("declaredIn")))
                    .toList();
            if (accumulators.isEmpty()) { continue; }
            families++;
            require("PIPELINE_EXPRESSION".equals(family.get("apiCategory")), "Accumulator 分类改变");
            for (Map<?, ?> method : accumulators) {
                overloads++;
                List<Map<?, ?>> params = parameters(method);
                if (params.isEmpty()) {
                    require("sum".equals(method.get("name")), "仅 sum() 没有输出参数槽");
                    continue;
                }
                Map<?, ?> output = params.get(0);
                require("fieldName".equals(output.get("name"))
                        && "OUTPUT_FIELD_NAME".equals(output.get("semanticType"))
                        && "VALUE".equals(output.get("semanticScope"))
                        && "PIPELINE_PARAMETER_OUTPUT_FIELD_NAME".equals(output.get("conceptRef")),
                        "缺少独立输出字段名 evidence: " + method.get("signature"));
                Map<?, ?> evidence = (Map<?, ?>) output.get("semanticEvidence");
                require(evidence != null && "JAVADOC".equals(evidence.get("source"))
                        && "mongoParam".equals(evidence.get("tag"))
                        && "fieldName OUTPUT_FIELD_NAME VALUE".equals(evidence.get("value")),
                        "不得按名称、类型或同名 overload 推断输出名");
                if ("String".equals(output.get("type"))) { stringNames++; }
                else {
                    require(((String) output.get("type")).startsWith("SFunction<"), "输出名类型改变");
                    getterNames++;
                }
                require(params.subList(1, params.size()).stream()
                        .noneMatch(p -> "OUTPUT_FIELD_NAME".equals(p.get("semanticType"))),
                        "expression、sortBy、函数源码等参数不得误标为输出名");
            }
        }
        require(families == 21 && overloads == 85 && stringNames == 29 && getterNames == 55,
                "Accumulator 审计覆盖必须为 21 families / 85 overloads / 29 String + 55 Lambda 输出槽");
        Map<?, ?> concept = maps(index.list("concepts")).stream()
                .filter(item -> "PIPELINE_PARAMETER_OUTPUT_FIELD_NAME".equals(item.get("id")))
                .findFirst().orElseThrow();
        require(maps((List<?>) concept.get("representations")).stream()
                .anyMatch(item -> "com.mongoplus.support.SFunction".equals(item.get("javaType"))
                        && "GET_FIELD_NAME_LINE".equals(item.get("encoding"))),
                "Lambda 输出名称须复用 getFieldNameLine 表示");
    }

    private static void verifyAudit(MongoPlusApiIndex index, Path root) throws Exception {
        List<Map<?, ?>> families = maps(index.getMethodFamilies());
        require(families.size() == 82, "MethodFamily 数量改变");
        require(families.stream().filter(f -> "PIPELINE_STAGE".equals(f.get("apiCategory"))).count() == 33,
                "Stage family 数量改变");
        List<Map<?, ?>> expressions = families.stream()
                .filter(f -> "PIPELINE_EXPRESSION".equals(f.get("apiCategory"))).toList();
        require(expressions.size() == 49, "Expression family 数量改变");
        require(expressions.stream().mapToInt(f -> ((List<?>) f.get("overloads")).size()).sum() == 157,
                "Expression overload 数量改变");
        require(families.stream().mapToInt(f -> ((List<?>) f.get("overloads")).size()).sum() == 297,
                "总 overload 数量改变");
        Set<String> actual = new HashSet<>();
        for (Map<?, ?> family : expressions) {
            for (Map<?, ?> method : maps((List<?>) family.get("overloads"))) {
                List<Map<?, ?>> params = parameters(method);
                String prefix = method.get("declaredIn") + "\t" + method.get("signature") + "\t";
                if (params.isEmpty()) { actual.add(prefix + "-\tNONE"); }
                for (Map<?, ?> parameter : params) {
                    String scope = parameter.containsKey("semanticEvidence")
                            ? (String) parameter.get("semanticScope") : "UNMARKED";
                    actual.add(prefix + parameter.get("name") + "\t" + scope);
                    require(parameter.containsKey("semanticEvidence") == parameter.containsKey("conceptRef"),
                            "未标记参数不能带 concept");
                }
            }
        }
        Set<String> expected = new HashSet<>();
        int additions = 0;
        int outputNameAdditions = 0;
        for (String line : Files.readAllLines(root.resolve(
                "mongo-plus-indexer/src/test/resources/pipeline-expression-parameter-audit.tsv"), StandardCharsets.UTF_8)) {
            String[] row = line.split("\t");
            expected.add(String.join("\t", row[0], row[1], row[2], row[3]));
            if ("ADDED".equals(row[4])) { additions++; }
            if ("OUTPUT_NAME_ADDED".equals(row[4])) { outputNameAdditions++; }
        }
        require(actual.equals(expected), "逐参数结果与人工源码审计清单不一致");
        require(actual.size() == 373, "372 个参数及 1 个无参方法必须全部审计");
        require(additions == 127, "新增标记数量不一致");
        require(outputNameAdditions == 84, "本次仅新增 84 个 accumulator 输出名槽");
        // types.publicMethods 与 MethodFamily 中相同声明必须保留同一逐参数证据。
        for (Map<?, ?> family : expressions) {
            for (Map<?, ?> method : maps((List<?>) family.get("overloads"))) {
                Map<?, ?> owner = maps(index.list("types")).stream()
                        .filter(t -> method.get("declaredIn").equals(t.get("qualifiedName"))).findFirst().orElseThrow();
                Map<?, ?> declaration = maps((List<?>) owner.get("publicMethods")).stream()
                        .filter(m -> method.get("signature").equals(m.get("signature"))).findFirst().orElseThrow();
                require(parameters(method).equals(parameters(declaration)), "声明和 overload 参数证据不一致");
            }
        }
    }

    private static List<Map<?, ?>> methods(MongoPlusApiIndex index, String mapping, String operator) {
        List<Map<?, ?>> result = new ArrayList<>();
        for (Map<?, ?> family : maps(index.getMethodFamilies())) {
            for (Map<?, ?> method : maps((List<?>) family.get("overloads"))) {
                if (((List<?>) method.get(mapping)).contains(operator)) { result.add(method); }
            }
        }
        return result;
    }

    private static List<Map<?, ?>> parameters(Map<?, ?> method) { return maps((List<?>) method.get("parameters")); }

    private static List<Map<?, ?>> maps(List<?> values) {
        return values.stream().<Map<?, ?>>map(value -> (Map<?, ?>) value).toList();
    }

    @SuppressWarnings("unchecked")
    private static void set(Map<?, ?> value, String key, Object item) { ((Map<String, Object>) value).put(key, item); }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
