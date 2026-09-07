package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Stage 参数 evidence 的消费测试；不建立生产 Resolver，不从描述或名称推断角色。 */
public final class PipelineStageSemanticsSelfTest {
    private PipelineStageSemanticsSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args[0]);
        MongoPlusApiIndex index = generate(root);
        verifyAudit(index, root);
        verifyRoles(index);
        verifyPipelines(index);
        verifyExplicitTags();
        System.out.println("PipelineStageSemanticsSelfTest PASSED: 33 families / 140 overloads / 291 parameters; "
                + "219 Stage additions + 2 option setter parameters; role negatives PASS");
    }

    private static MongoPlusApiIndex generate(Path root) throws Exception {
        return new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(root).build()).generate();
    }

    private static void verifyAudit(MongoPlusApiIndex index, Path root) throws Exception {
        List<Map<?, ?>> stages = maps(index.getMethodFamilies()).stream()
                .filter(f -> "PIPELINE_STAGE".equals(f.get("apiCategory"))).toList();
        Set<String> actual = new HashSet<>();
        Set<String> actualMethods = new HashSet<>();
        int overloads = 0;
        for (Map<?, ?> family : stages) {
            for (Map<?, ?> method : maps((List<?>) family.get("overloads"))) {
                overloads++;
                actualMethods.add(method.get("declaredIn") + "\t" + method.get("signature"));
                Map<?, ?> owner = maps(index.list("types")).stream()
                        .filter(t -> method.get("declaredIn").equals(t.get("qualifiedName"))).findFirst().orElseThrow();
                Map<?, ?> declaration = maps((List<?>) owner.get("publicMethods")).stream()
                        .filter(m -> method.get("signature").equals(m.get("signature"))).findFirst().orElseThrow();
                require(parameters(method).equals(parameters(declaration)), "type/overload evidence 不一致");
                for (Map<?, ?> parameter : parameters(method)) {
                    actual.add(key(method.get("declaredIn"), method.get("signature"), parameter.get("name"),
                            parameter.get("semanticType"), parameter.get("semanticScope"))
                            + "\t" + (parameter.get("conceptRef") == null ? "" : parameter.get("conceptRef")));
                    if (parameter.containsKey("semanticEvidence")) {
                        require(marked(index, parameter, parameter.get("semanticType").toString(),
                                parameter.get("semanticScope").toString()), "显式证据必须能找到匹配 concept");
                    }
                }
            }
        }
        Set<String> expected = new HashSet<>();
        int added = 0;
        List<String> auditLines = Files.readAllLines(root.resolve(
                "mongo-plus-indexer/src/test/resources/pipeline-stage-parameter-audit.tsv"), StandardCharsets.UTF_8);
        require(auditLines.size() == 292, "参数审计表必须恰好含 291 行及表头");
        for (String line : auditLines.subList(1, auditLines.size())) {
            String[] row = line.split("\t", -1);
            expected.add(key(row[13], row[2], row[3], row[6], row[7]) + "\t" + row[15]);
            if ("ADD".equals(row[8])) { added++; }
        }
        require(stages.size() == 33 && overloads == 140 && actual.size() == 291, "Stage surface 变化");
        require(actual.equals(expected), "逐参数 evidence 尚未补齐或偏离独立审计表");
        Set<String> expectedMethods = new HashSet<>();
        List<String> methodLines = Files.readAllLines(root.resolve(
                "mongo-plus-indexer/src/test/resources/pipeline-stage-overload-audit.tsv"), StandardCharsets.UTF_8);
        for (String line : methodLines.subList(1, methodLines.size())) {
            String[] row = line.split("\t", -1);
            expectedMethods.add(row[4] + "\t" + row[2]);
        }
        require(actualMethods.equals(expectedMethods), "完整 overload 清单（含无参方法）变化");
        require(added == 219, "新增参数计数错误");
        Map<?, ?> options = maps(index.list("types")).stream()
                .filter(t -> "com.mongoplus.aggregate.pipeline.UnwindOption".equals(t.get("qualifiedName")))
                .findFirst().orElseThrow();
        long optionNames = maps((List<?>) options.get("publicMethods")).stream()
                .flatMap(m -> parameters(m).stream()).filter(p -> marked(index, p, "OUTPUT_FIELD_NAME", "VALUE")).count();
        require(optionNames == 2, "UnwindOption 输出名的两种表示需独立标记");
    }

    private static String key(Object owner, Object signature, Object parameter, Object semantic, Object scope) {
        return owner + "\t" + signature + "\t" + parameter + "\t" + semantic + "\t" + (scope == null ? "" : scope);
    }

    private static void verifyRoles(MongoPlusApiIndex index) {
        Map<?, ?> lookup = methods(index, "mongoStages", "$lookup").stream()
                .filter(m -> parameters(m).size() == 4 && parameters(m).stream().allMatch(p -> "String".equals(p.get("type"))))
                .findFirst().orElseThrow();
        String[] roles = {"COLLECTION_NAME", "LOCAL_FIELD_NAME", "FOREIGN_FIELD_NAME", "OUTPUT_FIELD_NAME"};
        for (int i = 0; i < roles.length; i++) {
            Map<?, ?> parameter = parameters(lookup).get(i);
            require(acceptsName(index, parameter, roles[i], "orders"), "同 String 的真实角色必须可区分");
            for (String role : roles) {
                if (!role.equals(roles[i])) { require(!acceptsName(index, parameter, role, "orders"), "禁止按 String 混用角色"); }
            }
            require(!acceptsName(index, parameter, "FIELD_REFERENCE", "$orders"), "名字槽不能证明字段引用");
        }
        Map<?, ?> unwind = methods(index, "mongoStages", "$unwind").stream()
                .filter(m -> parameters(m).size() == 1 && "String".equals(parameters(m).get(0).get("type")))
                .findFirst().orElseThrow();
        Map<?, ?> reference = parameters(unwind).get(0);
        require(acceptsName(index, reference, "FIELD_REFERENCE", "$orders"), "字段引用表示缺失");
        require(!acceptsName(index, reference, "FIELD_REFERENCE", "orders"), "不得自动补美元前缀");
        require(!acceptsName(index, reference, "FIELD_REFERENCE", "$$orders"), "变量引用不能冒充 unwind 字段引用");
        for (String role : roles) { require(!acceptsName(index, reference, role, "orders"), "引用参数不能接受名字意图"); }
        Object evidence = reference.remove("semanticEvidence");
        require(!acceptsName(index, reference, "FIELD_REFERENCE", "$orders"), "缺少标签时禁止猜测");
        put(reference, "semanticEvidence", evidence);
        Object concept = reference.remove("conceptRef");
        require(!acceptsName(index, reference, "FIELD_REFERENCE", "$orders"), "缺少 conceptRef 时禁止猜测");
        put(reference, "conceptRef", concept);
        put(reference, "semanticScope", "ELEMENT");
        require(!acceptsName(index, reference, "FIELD_REFERENCE", "$orders"), "不得把元素 scope 当成单值");
        put(reference, "semanticScope", "VALUE");
        Map<?, ?> lambda = methods(index, "mongoStages", "$unwind").stream()
                .filter(m -> parameters(m).size() == 1 && parameters(m).get(0).get("type").toString().startsWith("SFunction<"))
                .findFirst().orElseThrow();
        require(!acceptsName(index, parameters(lambda).get(0), "FIELD_REFERENCE", "$orders"), "语义不能放宽 getter 的 Java 类型");
        Map<?, ?> named = parameters(lookup).get(0);
        Object original = named.get("conceptRef");
        put(named, "conceptRef", reference.get("conceptRef"));
        require(!acceptsName(index, named, "COLLECTION_NAME", "orders"), "角色与 concept 不一致必须拒绝");
        put(named, "conceptRef", original);
        Map<?, ?> graph = methods(index, "mongoStages", "$graphLookup").stream()
                .filter(m -> parameters(m).size() == 5 && "String".equals(parameters(m).get(2).get("type")))
                .findFirst().orElseThrow();
        Map<?, ?> from = concept(index, parameters(graph).get(2).get("conceptRef"));
        Map<?, ?> to = concept(index, parameters(graph).get(3).get("conceptRef"));
        require("TRAVERSAL_SOURCE".equals(from.get("fieldRole")) && "TRAVERSAL_TARGET".equals(to.get("fieldRole")),
                "graphLookup 两个外部字段的遍历方向必须由 concept 区分");
        require(!from.equals(to), "不能按同为 FOREIGN_FIELD_NAME 合并方向证据");
    }

    private static boolean acceptsName(MongoPlusApiIndex index, Map<?, ?> parameter, String role, String value) {
        if (!"String".equals(parameter.get("type")) || !marked(index, parameter, role, "VALUE")) { return false; }
        Map<?, ?> concept = concept(index, parameter.get("conceptRef"));
        for (Map<?, ?> representation : maps((List<?>) concept.get("representations"))) {
            if (!"java.lang.String".equals(representation.get("javaType"))
                    || !"UNCHANGED".equals(representation.get("encoding"))) { continue; }
            return (!representation.containsKey("prefix") || value.startsWith(representation.get("prefix").toString()))
                    && (!representation.containsKey("excludedPrefix") || !value.startsWith(representation.get("excludedPrefix").toString()));
        }
        return false;
    }

    private static boolean marked(MongoPlusApiIndex index, Map<?, ?> parameter, String semantic, String scope) {
        if (!semantic.equals(parameter.get("semanticType")) || !scope.equals(parameter.get("semanticScope"))) { return false; }
        Map<?, ?> concept = concept(index, parameter.get("conceptRef"));
        if (concept == null || !semantic.equals(concept.get("semanticType"))
                || !(parameter.get("semanticEvidence") instanceof Map)) { return false; }
        Map<?, ?> source = (Map<?, ?>) parameter.get("semanticEvidence");
        String expected = parameter.get("name") + " " + semantic + " " + scope;
        return "JAVADOC".equals(source.get("source")) && "mongoParam".equals(source.get("tag"))
                && (expected.equals(source.get("value")) || (expected + " " + concept.get("id")).equals(source.get("value")));
    }

    private static Map<?, ?> concept(MongoPlusApiIndex index, Object id) {
        return maps(index.list("concepts")).stream().filter(c -> c.get("id").equals(id)).findFirst().orElse(null);
    }

    /** 对五条组合检查可连接的参数角色和 Java 返回类型；实际 BSON 由独立 Core 编码探针验证。 */
    private static void verifyPipelines(MongoPlusApiIndex index) {
        require(expression(index, "$sum", "BsonField") && expression(index, "$avg", "BsonField")
                && stage(index, "$group", "PIPELINE_EXPRESSION"), "group + accumulators");
        require(expression(index, "$ifNull", "Bson") && expression(index, "$multiply", "Bson")
                && stage(index, "$project", "STAGE_DOCUMENT"), "ifNull -> multiply -> project");
        require(expression(index, "$cond", "Bson") && stage(index, "$project", "STAGE_DOCUMENT"), "cond -> project");
        require(expression(index, "$dateToString", "Bson") && stage(index, "$project", "STAGE_DOCUMENT"), "dateToString -> project");
        require(stage(index, "$lookup", "COLLECTION_NAME") && stage(index, "$unwind", "FIELD_REFERENCE")
                && stage(index, "$group", "PIPELINE_EXPRESSION") && stage(index, "$sort", "FIELD_NAME"), "lookup -> unwind -> group -> sort");
        System.out.println("Five complex Pipeline evidence checks PASS");
    }

    private static boolean expression(MongoPlusApiIndex index, String operator, String result) {
        return methods(index, "mongoExpressions", operator).stream().anyMatch(m -> result.equals(m.get("returnType"))
                && parameters(m).stream().anyMatch(p -> marked(index, p, "PIPELINE_EXPRESSION",
                        Boolean.TRUE.equals(p.get("varargs")) || p.get("type").toString().startsWith("List<")
                                || p.get("type").toString().startsWith("Collection<") ? "ELEMENT" : "VALUE")));
    }

    private static boolean stage(MongoPlusApiIndex index, String operator, String semantic) {
        return methods(index, "mongoStages", operator).stream().anyMatch(m -> parameters(m).stream()
                .anyMatch(p -> marked(index, p, semantic, "VALUE")));
    }

    private static void verifyExplicitTags() throws Exception {
        Path root = Files.createTempDirectory("pipeline-stage-tags-");
        Path file = root.resolve("com/mongoplus/aggregate/Aggregate.java");
        Files.createDirectories(file.getParent());
        try {
            String prefix = "package com.mongoplus.aggregate;\n/** @mongoParam a COLLECTION_NAME VALUE */\npublic interface Aggregate<C> {\n";
            String source = prefix + "/** @mongoStage $lookup\n * @mongoParam a COLLECTION_NAME VALUE\n */ C arbitrary(String a);\n"
                    + "/** @mongoStage $lookup */ C arbitrary(String a, String b);\n"
                    + "/** @mongoStage $unwind\n * @param fieldName must start with $\n */ C unwind(String fieldName);\n}";
            Files.writeString(file, source, StandardCharsets.UTF_8);
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder().addSourceRoot(root).pipeline(true).build());
            MongoPlusApiIndex index = generator.generate();
            require(methods(index, "mongoStages", "$lookup").stream().filter(m -> parameters(m).size() == 2)
                    .flatMap(m -> parameters(m).stream()).noneMatch(p -> p.containsKey("semanticEvidence")), "同名重载/类标签不能传播");
            require(methods(index, "mongoStages", "$unwind").stream().flatMap(m -> parameters(m).stream())
                    .noneMatch(p -> p.containsKey("semanticEvidence")), "描述及方法名不能生成规则");
            for (String declaration : List.of(
                    "/** @mongoStage $lookup\n * @mongoParam a COLLECTION_NAME VALUE\n * @mongoParam a OUTPUT_FIELD_NAME VALUE\n */ C f(String a);",
                    "/** @mongoStage $lookup\n * @mongoParam missing COLLECTION_NAME VALUE\n */ C f(String a);",
                    "/** @mongoStage $lookup\n * @mongoParam a COLLECTION_NAME ELEMENT\n */ C f(String a);",
                    "/** @mongoStage $lookup\n * @mongoParam a COLLECTION_NAME VALUE\n */ C f(int a);",
                    "/** @mongoStage $lookup\n * @mongoParam a COLLECTION_NAME VALUE UNKNOWN_CONCEPT\n */ C f(String a);",
                    "/** @mongoStage $lookup\n * @mongoParam a INVENTED_ROLE VALUE\n */ C f(String a);")) {
                Files.writeString(file, prefix + declaration + "\n}", StandardCharsets.UTF_8);
                boolean rejected = false;
                try { new MongoPlusIndexer(MongoPlusIndexerConfig.builder().addSourceRoot(root).pipeline(true).build()).generate(); }
                catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "非法或矛盾标签必须拒绝: " + declaration);
            }
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) { Files.delete(path); }
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
    private static List<Map<?, ?>> maps(List<?> items) { return items.stream().<Map<?, ?>>map(v -> (Map<?, ?>) v).toList(); }
    @SuppressWarnings("unchecked")
    private static void put(Map<?, ?> map, String key, Object value) { ((Map<String, Object>) map).put(key, value); }
    private static void require(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } }
}
