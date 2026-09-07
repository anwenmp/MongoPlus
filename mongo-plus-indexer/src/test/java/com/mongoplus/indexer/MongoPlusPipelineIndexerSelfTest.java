package com.mongoplus.indexer;

import com.mongoplus.indexer.json.JsonWriter;
import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 聚合索引的源码夹具和真实源码回归，不依赖测试框架。 */
public final class MongoPlusPipelineIndexerSelfTest {
    private MongoPlusPipelineIndexerSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args[0]);
        MongoPlusIndexer real = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(root).build());
        MongoPlusApiIndex index = real.generate();
        require(((Number) index.object("scanStatistics").get("pipelineExpressionCount")).intValue() > 0,
                "正式 Pipeline 入口必须直接收录 Expression API");
        require(index.getMethodFamilies().size() == 82, "真实源码应收录 33 个 stage 和 49 个 expression family");
        require(index.asMap().get("expressionRoots").equals(MongoPlusIndexerConfig.PIPELINE_EXPRESSION_ROOTS),
                "正式 Expression 入口必须明确且不包含 deprecated 旧包");
        int realOverloads = 0;
        int expressionOverloads = 0;
        java.util.Set<String> declarations = new java.util.TreeSet<String>();
        java.util.Set<String> evidenceKeys = new java.util.HashSet<String>();
        for (Object item : index.getMethodFamilies()) {
            Map<?, ?> family = (Map<?, ?>) item;
            boolean stage = "PIPELINE_STAGE".equals(family.get("apiCategory"));
            for (Object itemOverload : (List<?>) family.get("overloads")) {
                Map<?, ?> overload = (Map<?, ?>) itemOverload;
                require(evidenceKeys.add(family.get("apiCategory") + ":" + overload.get("declaredIn")
                        + ":" + overload.get("signature")), "同一声明不能因多个入口或依赖路径重复收录");
                if (!stage) {
                    require(!((List<?>) overload.get("mongoExpressions")).isEmpty(), "每个 expression 必须有自身标签");
                    require(((List<?>) overload.get("mongoStages")).isEmpty(), "Expression 不能推断 Stage 映射");
                    require(!((List<?>) overload.get("availableIn")).contains(MongoPlusIndexerConfig.PIPELINE_ENTRY_TYPE),
                            "独立表达式工厂不能伪装成 Aggregate 继承 API");
                    declarations.add((String) overload.get("declaredIn"));
                    expressionOverloads++;
                }
            }
            if (!stage) { continue; }
            require(!java.util.Arrays.asList("custom", "metaTextScore", "eq", "each").contains(family.get("name")),
                    "透传、排序规范和 Query API 不能成为 Stage");
            require(((List<?>) family.get("mongoExpressions")).isEmpty(), "不能从描述推断 expression");
            for (Object overload : (List<?>) family.get("overloads")) {
                require(!((List<?>) ((Map<?, ?>) overload).get("mongoStages")).isEmpty(), "每个 overload 必须有自身 stage evidence");
                realOverloads++;
            }
        }
        require(realOverloads == 140, "真实源码 stage overload evidence 数量变化");
        require(expressionOverloads == 157, "正式 Expression 应排除旧包副本的 54 条 evidence");
        require(declarations.equals(new java.util.TreeSet<String>(MongoPlusIndexerConfig.PIPELINE_EXPRESSION_ROOTS)),
                "Expression 声明只能来自已核对的五个公开工厂");
        require(!json(index).contains("com.mongoplus.conditions.interfaces.ConditionOperators"),
                "deprecated 旧包不能重复进入正式 Index");
        require(((Number) index.object("scanStatistics").get("pipelineStageCount")).intValue() == 33
                && ((Number) index.object("scanStatistics").get("pipelineExpressionCount")).intValue() == 49
                && ((Number) index.object("scanStatistics").get("overloadCount")).intValue() == 297,
                "正式扫描统计必须与 evidence 一致");
        require(((List<?>) named(index.getMethodFamilies(), "sum").get("overloads")).size() == 6,
                "累加器与普通 sum 的真实声明需保留，旧包副本需排除");
        MongoPlusApiIndex stageOnly = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                .addSourceRoot(root.resolve("mongo-plus-core/src/main/java"))
                .addSourceRoot(root.resolve("mongo-plus-annotation/src/main/java"))
                .pipeline(true).build()).generate();
        require(stageFamilies(index).equals(stageFamilies(stageOnly)), "增加 Expression 入口不能改变任何 Stage evidence");
        require(((List<?>) named(index.getMethodFamilies(), "group").get("overloads")).size() == 6,
                "group(Bson) 透传重载不能借用其他 group 的映射");
        require(((List<?>) named(index.getMethodFamilies(), "sort").get("overloads")).size() == 2,
                "sort(Bson) 透传重载不能借用其他 sort 的映射");
        require(((List<?>) named(index.getMethodFamilies(), "projectDisplay").get("overloads")).size() == 4,
                "必须保留 Project 父接口的所有已标记重载");
        named(index.list("types"), "QueryWrapper");
        require(json(index).equals(json(real.generate())), "真实源码重复生成不一致");
        Path fixture = Files.createTempDirectory("pipeline-index-test-");
        try {
            source(fixture, "com/mongoplus/aggregate/Aggregate.java",
                    "package com.mongoplus.aggregate; import example.*; "
                    + "public interface Aggregate<C> extends Parent<C> {\n"
                    + "/** @mongoStage $group */\n C group(Box<Mode> value, Callback callback);\n"
                    + "/** $group 描述不能提供映射 */\n C group(String value);\n"
                    + "/** @mongoExpression $sum */\n C sum(int value);\n"
                    + "/** @mongoStage $fake invalid */\n C invalid();\n"
                    + "private void hidden() {}\n }");
            source(fixture, "example/Parent.java", "package example; public interface Parent<C> {\n"
                    + "/** @mongoStage $project */\n C project(String... fields);\n"
                    + "/** @mongoStage $notInherited */\n static void utility() {}\n }");
            source(fixture, "example/Box.java", "package example; public class Box<T> { "
                    + "public Box(Seed seed) {} private Box(String secret) {} "
                    + "public Box<T> next(Mode mode) { return this; } "
                    + "/** @mongoExpression $sum */\n public static Box<Mode> sum(int value) { return null; } }");
            source(fixture, "example/Mode.java", "package example; public enum Mode { Z, A }");
            source(fixture, "example/Seed.java", "package example; public class Seed { public Seed(Box<Mode> box) {} }");
            source(fixture, "example/Callback.java", "package example; @FunctionalInterface "
                    + "public interface Callback { Box<Mode> apply(Seed seed); }");
            source(fixture, "example/Unrelated.java", "package example; public class Unrelated {\n"
                    + "/** @mongoStage $unrelated */\n public void dump() {} }");
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                    .addSourceRoot(fixture).addPrimaryPackage("ignored").pipeline(true).mongoPlusVersion("test").build());
            MongoPlusApiIndex first = generator.generate();
            require(first.getMethodFamilies().size() == 3, "按 category 和 name 分族且排除不可达类型");
            Map<String, Object> group = named(first.getMethodFamilies(), "group");
            require(((List<?>) group.get("overloads")).size() == 1, "不能为未标记同名重载传播映射");
            require(((List<?>) group.get("mongoStages")).contains("$group"), "丢失显式 stage");
            Map<String, Object> sum = named(first.getMethodFamilies(), "sum");
            require(((List<?>) sum.get("overloads")).size() == 2, "必须保留不同声明类型的同签名方法");
            Map<String, Object> box = named(first.list("types"), "Box");
            require(((List<?>) box.get("constructors")).size() == 2, "构造器可见性证据丢失");
            require(!((List<?>) box.get("publicMethods")).isEmpty(), "依赖类型公开 API 丢失");
            named(first.list("types"), "Seed");
            named(first.list("types"), "Callback");
            Map<String, Object> mode = named(first.list("types"), "Mode");
            require(((List<?>) mode.get("constants")).size() == 2, "枚举证据丢失");
            require(first.list("types").size() == 6, "类型闭包应排除无关源码");
            require(first.list("specialTypes").isEmpty() && first.list("concepts").isEmpty(), "不能注入无关概念");
            require(json(first).equals(json(generator.generate())), "夹具重复生成不一致");
            require(!json(first).contains("$notInherited") && !json(first).contains("$unrelated"), "扫描边界错误");
            require(json(first).contains("parameterTypes") && json(first).contains("example.Callback"), "调用证据缺失");
            // 根类型与签名依赖重合、重复配置时仍只收录一次；构造器依赖也必须进入闭包。
            source(fixture, "example/ExpressionFactory.java", "package example; public class ExpressionFactory { "
                    + "public ExpressionFactory(RootSeed seed) {} private ExpressionFactory(PrivateSeed seed) {} "
                    + "/** @mongoExpression $sum */\n public static Box<Mode> sum(Seed seed) { return null; } "
                    + "/** $add 描述不能提供映射 */\n public static Object add(Object value) { return value; } }");
            source(fixture, "example/RootSeed.java", "package example; public class RootSeed {} ");
            source(fixture, "example/PrivateSeed.java", "package example; public class PrivateSeed {} ");
            MongoPlusApiIndex withRoots = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                    .addSourceRoot(fixture).pipeline(true).addExpressionRoot("example.ExpressionFactory")
                    .addExpressionRoot("example.Box").addExpressionRoot("example.Box").build()).generate();
            require(((List<?>) named(withRoots.getMethodFamilies(), "sum").get("overloads")).size() == 3,
                    "Expression 根与依赖重合不能重复 evidence");
            named(withRoots.list("types"), "RootSeed");
            require(withRoots.getMethodFamilies().size() == 3 && !json(withRoots).contains("\"qualifiedName\": \"example.PrivateSeed\""),
                    "未标记方法和私有构造器依赖不能扩张扫描边界");
            MongoPlusApiIndex reordered = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                    .addSourceRoot(fixture).pipeline(true).addExpressionRoot("example.Box")
                    .addExpressionRoot("example.ExpressionFactory").build()).generate();
            require(json(withRoots).equals(json(reordered)), "根顺序及重复配置不能影响 deterministic");
            // 验证真实 special type 与 concept 仅随签名依赖带入。
            source(fixture, "com/mongoplus/aggregate/Aggregate.java",
                    "package com.mongoplus.aggregate; import com.mongoplus.function.FieldChain; "
                    + "public interface Aggregate<C> {\n"
                    + "/** @mongoStage $project */\n C project(FieldChain<?> chain); }");
            for (String name : new String[] {"com/mongoplus/function/FieldChain.java",
                    "com/mongoplus/support/SFunction.java"}) {
                source(fixture, name, new String(Files.readAllBytes(root.resolve(
                        "mongo-plus-core/src/main/java").resolve(name)), StandardCharsets.UTF_8));
            }
            MongoPlusApiIndex withSpecial = generator.generate();
            require(withSpecial.list("specialTypes").size() == 2, "special type 闭包丢失");
            require(withSpecial.list("concepts").size() == 1, "FieldChain concept 丢失");
            require(json(withSpecial).equals(json(generator.generate())), "special type 顺序不稳定");
            boolean rejected = false;
            try {
                MongoPlusIndexerConfig.builder().addSourceRoot(fixture).addPrimaryPackage("ignored")
                        .pipeline(true).includeGeneratedAt(true).build();
            } catch (IllegalArgumentException expected) { rejected = true; }
            require(rejected, "必须拒绝非确定性时间戳");

            // 保留真实入口的方法名与描述，仅移除显式标签后必须恢复空映射。
            Path core = root.resolve("mongo-plus-core/src/main/java");
            for (String name : new String[] {"com/mongoplus/aggregate/Aggregate.java",
                    "com/mongoplus/aggregate/pipeline/Project.java"}) {
                source(fixture, name, new String(Files.readAllBytes(core.resolve(name)), StandardCharsets.UTF_8)
                        .replaceAll("(?m)^\\s*\\* @mongo(?:Stage|Expression) \\$[A-Za-z][A-Za-z0-9]*\\r?\\n", ""));
            }
            MongoPlusIndexer evidence = generator;
            require(evidence.generate().getMethodFamilies().isEmpty(), "真实方法名、包名和描述不能代替标签");

            // Stage 不再人为引用工厂；真实 Expression Roots 必须独立生效。
            source(fixture, "com/mongoplus/aggregate/Aggregate.java",
                    "package com.mongoplus.aggregate; public interface Aggregate<C> {} ");
            for (String name : new String[] {"aggregate/pipeline/Accumulators.java",
                    "aggregate/pipeline/AggregateOperator.java", "aggregate/pipeline/Projections.java",
                    "aggregate/pipeline/Sorts.java", "conditions/operation/ConditionOperators.java",
                    "conditions/interfaces/ConditionOperators.java"}) {
                String relative = "com/mongoplus/" + name;
                source(fixture, relative, new String(Files.readAllBytes(core.resolve(relative)), StandardCharsets.UTF_8));
            }
            MongoPlusIndexerConfig.Builder expressionConfig = MongoPlusIndexerConfig.builder()
                    .addSourceRoot(fixture).pipeline(true);
            for (String expressionRoot : MongoPlusIndexerConfig.PIPELINE_EXPRESSION_ROOTS) {
                expressionConfig.addExpressionRoot(expressionRoot);
            }
            evidence = new MongoPlusIndexer(expressionConfig.build());
            MongoPlusApiIndex expressionEvidence = evidence.generate();
            int fixtureExpressionOverloads = 0;
            for (Object item : expressionEvidence.getMethodFamilies()) {
                Map<?, ?> family = (Map<?, ?>) item;
                if ("PIPELINE_EXPRESSION".equals(family.get("apiCategory"))) {
                    fixtureExpressionOverloads += ((List<?>) family.get("overloads")).size();
                    require(!((List<?>) family.get("mongoExpressions")).isEmpty(), "表达式标签丢失");
                }
            }
            require(fixtureExpressionOverloads == 157, "仅五个现行工厂的 157 条 expression evidence 应可解析");
            require(((List<?>) named(expressionEvidence.getMethodFamilies(), "sum").get("overloads")).size() == 6,
                    "累加器与普通 sum 表达式的不同声明不能折叠");
            require(json(expressionEvidence).equals(json(evidence.generate())), "真实表达式标签生成顺序不稳定");
            require(expressionEvidence.list("specialTypes").size() == 1, "Expression 签名应独立带入 SFunction");
            require(!json(expressionEvidence).contains("com.mongoplus.conditions.interfaces.ConditionOperators"),
                    "同源码根存在 deprecated 副本不代表应扫描它");
            for (String expressionRoot : MongoPlusIndexerConfig.PIPELINE_EXPRESSION_ROOTS) {
                String name = expressionRoot.replace('.', '/') + ".java";
                source(fixture, name, new String(Files.readAllBytes(core.resolve(name)), StandardCharsets.UTF_8)
                        .replaceAll("(?m)^\\s*\\* @mongoExpression \\$[A-Za-z][A-Za-z0-9]*\\r?\\n", ""));
            }
            require(evidence.generate().getMethodFamilies().isEmpty(), "Expression 根、方法名和描述不能代替显式标签");
        } finally {
            try (Stream<Path> paths = Files.walk(fixture)) {
                for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                    Files.delete(path);
                }
            }
        }
        System.out.println("MongoPlusPipelineIndexerSelfTest PASSED");
    }

    private static void source(Path root, String name, String text) throws Exception {
        Path file = root.resolve(name);
        Files.createDirectories(file.getParent());
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
    }

    private static String json(MongoPlusApiIndex index) throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter.write(index.asMap(), writer);
        return writer.toString();
    }

    private static List<Object> stageFamilies(MongoPlusApiIndex index) {
        List<Object> stages = new java.util.ArrayList<Object>();
        for (Object item : index.getMethodFamilies()) {
            if ("PIPELINE_STAGE".equals(((Map<?, ?>) item).get("apiCategory"))) { stages.add(item); }
        }
        return stages;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> named(List<Object> items, String name) {
        for (Object item : items) {
            Map<String, Object> value = (Map<String, Object>) item;
            if (name.equals(value.get("name"))) { return value; }
        }
        throw new AssertionError("缺少 " + name);
    }

    private static void require(boolean value, String message) {
        if (!value) { throw new AssertionError(message); }
    }
}
