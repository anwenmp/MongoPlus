package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.List;
import java.util.Map;

/** 仅消费 Index evidence 验证管道表达式参数，不预置目标 Java 调用代码。 */
public final class PipelineExpressionSemanticsSelfTest {
    private PipelineExpressionSemanticsSelfTest() { }

    public static void main(String[] args) throws Exception {
        MongoPlusApiIndex index = new MongoPlusIndexer(
                MongoPlusIndexerConfig.forPipelineProject(Paths.get(args[0])).build()).generate();
        // 用户输入管道的数据结构；解析/翻译 Java 调用不属于本次测试范围。
        List<Map<String, Object>> pipeline = List.of(
                Map.of("$group", Map.of("_id", "$userId", "totalAmount", Map.of("$sum", "$amount"),
                        "avgAmount", Map.of("$avg", "$amount"))),
                Map.of("$sort", Map.of("totalAmount", -1)), Map.of("$limit", 10));
        Map<?, ?> concept = index.list("concepts").stream().map(item -> (Map<?, ?>) item)
                .filter(item -> "PIPELINE_EXPRESSION_FIELD_REFERENCE".equals(item.get("id")))
                .findFirst().orElseThrow(() -> new AssertionError("缺少字段引用 concept"));
        for (Map<String, Object> stage : pipeline) {
            String operator = stage.keySet().iterator().next();
            require(!overloads(index, "mongoStages", operator).isEmpty(), "缺少 Stage evidence: " + operator);
        }
        Map<?, ?> groupInput = (Map<?, ?>) pipeline.get(0).get("$group");
        Map<?, ?> group = overloads(index, "mongoStages", "$group").stream()
                .filter(item -> parameters(item).size() == 2)
                .filter(item -> "PIPELINE_EXPRESSION".equals(parameters(item).get(0).get("semanticType")))
                .filter(item -> Boolean.TRUE.equals(parameters(item).get(1).get("varargs")))
                .findFirst().orElseThrow(() -> new AssertionError("缺少接受 expression + 累加器的 Stage overload"));
        proveFieldReference(parameters(group).get(0), groupInput.get("_id"), concept);
        for (Map.Entry<?, ?> output : groupInput.entrySet()) {
            if ("_id".equals(output.getKey())) { continue; }
            Map<?, ?> expression = (Map<?, ?>) output.getValue();
            String operator = (String) expression.keySet().iterator().next();
            String accumulatorType = ((String) parameters(group).get(1).get("type")).replace("[]", "").replace("...", "");
            Map<?, ?> accumulator = overloads(index, "mongoExpressions", operator).stream()
                    .filter(item -> accumulatorType.equals(item.get("returnType")))
                    .filter(item -> parameters(item).size() == 2)
                    .filter(item -> "String".equals(parameters(item).get(0).get("type")))
                    .filter(item -> "PIPELINE_EXPRESSION".equals(parameters(item).get(1).get("semanticType")))
                    .findFirst().orElseThrow(() -> new AssertionError("缺少兼容累加器返回值和表达式参数的 evidence"));
            require(!"PIPELINE_EXPRESSION".equals(parameters(accumulator).get(0).get("semanticType")),
                    "输出字段名不能误标为表达式");
            proveFieldReference(parameters(accumulator).get(1), expression.get(operator), concept);
        }
        Map<?, ?> plainString = (Map<?, ?>) concept.get("plainStringValue");
        require(!"amount".startsWith((String) plainString.get("excludedPrefix"))
                && "UNCHANGED".equals(plainString.get("encoding")), "普通字符串不自动成为字段引用");
        require("NOT_ESTABLISHED".equals(plainString.get("dollarPrefixedLiteralConstruction")),
                "不能发明美元前缀字符串的 literal 转义 API");
        require("$$".equals(((Map<?, ?>) concept.get("variableReference")).get("prefix")), "变量和字段前缀须分开");
        require(!"PIPELINE_EXPRESSION".equals(new com.mongoplus.indexer.type.TypeClassifier().classify("TExpression")),
                "通用分类器不能从泛型名推断语义");
        for (Map<?, ?> count : overloads(index, "mongoExpressions", "$count")) {
            require(parameters(count).stream().noneMatch(p -> "PIPELINE_EXPRESSION".equals(p.get("semanticType"))),
                    "count 的原样编码不足以证明任意字段表达式用法");
        }
        verifyExplicitTags();
        System.out.println("PipelineExpressionSemanticsSelfTest PASSED");
    }

    private static void verifyExplicitTags() throws Exception {
        Path root = Files.createTempDirectory("pipeline-expression-semantics-");
        try {
            Path file = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(file.getParent());
            String source = "package com.mongoplus.aggregate;\n"
                    + "import java.util.List;\nimport java.util.Collection;\n"
                    + "/** @mongoParam expression PIPELINE_EXPRESSION VALUE */\n"
                    + "public interface Aggregate<C> {\n"
                    + "/** @mongoStage $group\n * @mongoParam operand PIPELINE_EXPRESSION VALUE\n */\n"
                    + "<ArbitraryValue> C tagged(ArbitraryValue operand);\n"
                    + "/** @mongoStage $group */\n <TExpression> C untagged(TExpression expression);\n"
                    + "/** @mongoStage $group */\n <TExpression> C tagged(String operand, TExpression expression);\n"
                    + "/** @mongoExpression $sum\n * @mongoParam args PIPELINE_EXPRESSION ELEMENT\n */\n"
                    + "<ArbitraryValue> C elements(ArbitraryValue... args);\n"
                    + "/** @mongoExpression $sum\n * @mongoParam args PIPELINE_EXPRESSION ELEMENT\n */\n"
                    + "C array(Object[] args);\n"
                    + "/** @mongoExpression $sum\n * @mongoParam args PIPELINE_EXPRESSION ELEMENT\n */\n"
                    + "C list(List<?> args);\n"
                    + "/** @mongoExpression $sum\n * @mongoParam args PIPELINE_EXPRESSION ELEMENT\n */\n"
                    + "C collection(Collection<?> args);\n"
                    + "/** @mongoExpression $sum\n * @mongoParam args PIPELINE_EXPRESSION ELEMENT\n */\n"
                    + "C qualified(java.util.Collection<?> args);\n"
                    + "/** @mongoExpression $sum\n * @mongoParam args PIPELINE_EXPRESSION VALUE\n */\n"
                    + "C wholeList(List<?> args);\n"
                    + "/** @mongoExpression $sum\n * @mongoParam args PIPELINE_EXPRESSION VALUE\n */\n"
                    + "C wholeArray(Object[] args);\n"
                    + "/** @mongoExpression $sum */\n C untaggedList(List<?> expression);\n }";
            Files.writeString(file, source, StandardCharsets.UTF_8);
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                    .addSourceRoot(root).pipeline(true).build());
            MongoPlusApiIndex index = generator.generate();
            for (Map<?, ?> overload : overloads(index, "mongoStages", "$group")) {
                boolean marked = "tagged".equals(overload.get("name")) && parameters(overload).size() == 1;
                for (Map<?, ?> parameter : parameters(overload)) {
                    require(marked == "PIPELINE_EXPRESSION".equals(parameter.get("semanticType")),
                            "只认方法的逐参数标签，不认泛型名、参数名、类标签或同名重载");
                }
            }
            for (Map<?, ?> overload : overloads(index, "mongoExpressions", "$sum")) {
                String name = (String) overload.get("name");
                Map<?, ?> parameter = parameters(overload).get(0);
                if ("untaggedList".equals(name)) {
                    require(!parameter.containsKey("semanticEvidence"), "集合类型不能自动产生语义");
                } else {
                    require((name.startsWith("whole") ? "VALUE" : "ELEMENT").equals(parameter.get("semanticScope")),
                            "数组/集合/varargs 的范围必须来自当前参数标签: " + name);
                }
            }
            Files.writeString(file, source.replace("import java.util.List;\nimport java.util.Collection;",
                    "import java.util.*;"), StandardCharsets.UTF_8);
            generator.generate();
            for (String invalidSource : List.of(
                    source.replace("Object[] args", "Object args"),
                    source.replace("List<?> args", "java.util.Map<String, Object> args"),
                    source.replace("import java.util.List;", "import custom.List;"),
                    source.replace("<ArbitraryValue> C elements(ArbitraryValue... args)",
                            "<ArbitraryValue> C elements(ArbitraryValue args)"),
                    source.replace("args PIPELINE_EXPRESSION ELEMENT", "args PIPELINE_EXPRESSION VALUE"))) {
                Files.writeString(file, invalidSource, StandardCharsets.UTF_8);
                boolean rejected = false;
                try { generator.generate(); } catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "非法容器结构或 varargs VALUE 必须拒绝");
            }
            String withoutTags = source.replaceAll("(?m)^ \\* @mongoParam[^\\r\\n]*\\r?\\n", "");
            Files.writeString(file, withoutTags, StandardCharsets.UTF_8);
            require(generator.generate().list("concepts").isEmpty(), "只有类标签时不能注入参数概念");
            for (String invalid : List.of("missing PIPELINE_EXPRESSION VALUE", "operand PIPELINE_EXPRESSION ELEMENT",
                    "operand GUESSED_EXPRESSION VALUE")) {
                Files.writeString(file, source.replace("operand PIPELINE_EXPRESSION VALUE", invalid), StandardCharsets.UTF_8);
                boolean rejected = false;
                try { generator.generate(); } catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "非法参数声明必须被拒绝: " + invalid);
            }
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static void proveFieldReference(Map<?, ?> parameter, Object input, Map<?, ?> concept) {
        require(concept.get("id").equals(parameter.get("conceptRef")), "参数缺少 concept 关联");
        require(parameter.get("semanticEvidence") instanceof Map, "参数缺少显式源码 evidence");
        Map<?, ?> representation = (Map<?, ?>) concept.get("fieldReference");
        require("java.lang.String".equals(representation.get("javaType")), "字段引用缺少 Java 类型");
        String prefix = (String) representation.get("prefix");
        String excluded = (String) representation.get("excludedPrefix");
        require(input instanceof String && ((String) input).startsWith(prefix)
                && !((String) input).startsWith(excluded), "输入不是该 concept 证明的字段引用");
        require("UNCHANGED".equals(representation.get("encoding")), "字段引用应可原样传入");
    }

    private static List<Map<?, ?>> overloads(MongoPlusApiIndex index, String mapping, String operator) {
        return index.getMethodFamilies().stream().map(item -> (Map<?, ?>) item)
                .filter(item -> ((List<?>) item.get(mapping)).contains(operator))
                .flatMap(item -> ((List<?>) item.get("overloads")).stream()).<Map<?, ?>>map(item -> (Map<?, ?>) item)
                .filter(item -> ((List<?>) item.get(mapping)).contains(operator)).toList();
    }

    private static List<Map<?, ?>> parameters(Map<?, ?> overload) {
        return ((List<?>) overload.get("parameters")).stream().<Map<?, ?>>map(item -> (Map<?, ?>) item).toList();
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
