package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** U02/U03 的上游 evidence 闭环；删除真实源码契约后必须无法通过同一验收。 */
public final class PipelineUnionWithEvidenceSelfTest {
    private static final String AGGREGATE = "com.mongoplus.aggregate.Aggregate";
    private static final String BUILDER = "com.mongoplus.aggregate.AggregateWrapper";
    private static final String SIGNATURE = "unionWith(String collectionName, Aggregate<?> aggregate)";
    private static final String INPUT = "@mongoPipelineInput aggregate extractor=" + AGGREGATE + "#getAggregateConditionList()";
    private static final String EFFECT = "@mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER";
    private static final String FACTORY = "@mongoPipelineFactory receiver=NEW initial=EMPTY ownership=INDEPENDENT";
    private static final String REPRESENTATION = "@mongoPipelineRepresentation source=RECEIVER semanticType=PIPELINE order=CALL_ORDER access=LIVE_VIEW";
    private static int cases;

    private PipelineUnionWithEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]).toAbsolutePath();
        MongoPlusApiIndex index = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).build()).generate();
        complete(index, List.of("limit(int limit)"));
        cases++;
        complete(index, List.of("sort(String field, Integer value)", "limit(int limit)"));
        cases++;
        mutations(project);
        System.out.println("PipelineUnionWithEvidenceSelfTest PASSED: " + cases
                + " cases; U02/U03; String/Class representation; six stage effects; binding/extraction/factory/representation/effect removal");
    }

    private static void complete(MongoPlusApiIndex index, List<String> innerStages) {
        Map<?, ?> builder = type(index, BUILDER);
        Map<?, ?> factory = contract(callable(builder, "constructors", "AggregateWrapper()"), "pipelineFactory");
        require("NEW".equals(factory.get("receiver")) && "EMPTY".equals(factory.get("initial"))
                && "INDEPENDENT".equals(factory.get("ownership")) && BUILDER.equals(factory.get("receiverType"))
                && "PIPELINE".equals(factory.get("semanticType")), "独立空 PIPELINE receiver");
        require(((List<?>) builder.get("extendsOrImplements")).contains("LambdaAggregateWrapper<AggregateWrapper>"), "真实 Wrapper 父类型");
        require(((List<?>) type(index, "com.mongoplus.aggregate.LambdaAggregateWrapper").get("extendsOrImplements"))
                .contains("Aggregate<Children>"), "receiver 可赋给 Aggregate<?>");
        for (String signature : innerStages) { effect(overload(index, signature)); }
        Map<?, ?> getter = callable(type(index, AGGREGATE), "publicMethods", "getAggregateConditionList()");
        Map<?, ?> representation = contract(getter, "pipelineRepresentation");
        require("PIPELINE".equals(getter.get("resultSemanticType")) && "PIPELINE".equals(representation.get("semanticType"))
                && "RECEIVER".equals(representation.get("source")) && "CALL_ORDER".equals(representation.get("order"))
                && "LIVE_VIEW".equals(representation.get("access")), "有序 receiver PIPELINE representation");

        Map<?, ?> target = overload(index, SIGNATURE);
        binding(parameter(target, 0), "collectionName", "COLLECTION_NAME", "coll");
        binding(parameter(target, 1), "aggregate", "PIPELINE", "pipeline");
        require("Aggregate<?>".equals(parameter(target, 1).get("type")), "nested receiver 的真实 Java 参数表示");
        require((AGGREGATE + "#getAggregateConditionList()")
                .equals(contract(parameter(target, 1), "pipelineExtraction").get("apiRef")), "复用正式 PIPELINE extractor");
        effect(target);
        require(((List<?>) target.get("availableIn")).contains(AGGREGATE), "目标调用在 receiver 实现的 Aggregate 契约可用");
        require(target.equals(callable(type(index, AGGREGATE), "publicMethods", SIGNATURE)), "两个正式视图一致");

        List<Map<?, ?>> union = index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                .filter(f -> "unionWith".equals(f.get("name")))
                .flatMap(f -> ((List<?>) f.get("overloads")).stream()).<Map<?, ?>>map(m -> (Map<?, ?>) m).toList();
        require(union.size() == 6, "正式 Stage surface 仍为六个 overload");
        for (Map<?, ?> method : union) {
            effect(method);
            require(method.equals(callable(type(index, AGGREGATE), "publicMethods", (String) method.get("signature"))), "所有 overload 视图一致");
        }
        Map<?, ?> classTarget = overload(index, "unionWith(Class<?> collection, Aggregate<?> aggregate)");
        binding(parameter(classTarget, 0), "collection", "COLLECTION_NAME", "coll");
        require("Class<?>".equals(parameter(classTarget, 0).get("type")), "Class overload 要求 Java Class 值");
        require(!acceptsString(index, parameter(classTarget, 0)), "Mongo coll 字符串不能供给 Class 参数");
        require(acceptsString(index, parameter(target, 0)), "String 原值表示合法");
        // 仅检查本次 receiver 路径的 Java 表示，未实现 BSON value construction 或下游 planner。
        long candidates = union.stream().filter(m -> ((List<?>) m.get("parameters")).size() == 2)
                .filter(m -> parameter(m, 1).containsKey("pipelineExtraction"))
                .filter(m -> acceptsString(index, parameter(m, 0))).count();
        require(candidates == 1, "字符串 coll + receiver extraction 只有一个合法表示");
        require(!parameter(overload(index, "unionWith(String collectionName, List<? extends Bson> aggregate)"), 1)
                .containsKey("pipelineExtraction"), "List 不伪造 receiver extractor");
        require(((List<?>) index.asMap().get("requiredCapabilities")).containsAll(
                List.of("STAGE_OBJECT_FIELD_BINDING_V1", "PIPELINE_CONSTRUCTION_V1")), "复用现有 capability");
    }

    private static boolean acceptsString(MongoPlusApiIndex index, Map<?, ?> parameter) {
        if (!List.of("String", "java.lang.String").contains(parameter.get("type"))) { return false; }
        Map<?, ?> concept = index.list("concepts").stream().map(c -> (Map<?, ?>) c)
                .filter(c -> parameter.get("conceptRef").equals(c.get("id"))).findFirst().orElseThrow();
        return ((List<?>) concept.get("representations")).stream().map(r -> (Map<?, ?>) r)
                .anyMatch(r -> "java.lang.String".equals(r.get("javaType")) && "UNCHANGED".equals(r.get("encoding")));
    }

    private static void binding(Map<?, ?> parameter, String name, String semantic, String field) {
        require(name.equals(parameter.get("name")) && semantic.equals(parameter.get("semanticType"))
                && "VALUE".equals(parameter.get("semanticScope")) && parameter.containsKey("semanticEvidence")
                && ("PIPELINE_PARAMETER_" + semantic).equals(parameter.get("conceptRef")), "显式参数语义: " + name);
        require(parameter.get("objectFieldBinding") instanceof Map, "缺少 " + field + " objectFieldBinding");
        Map<?, ?> binding = (Map<?, ?>) parameter.get("objectFieldBinding");
        require(field.equals(binding.get("field")), "对象字段映射: " + field);
        require(!binding.containsKey("encoding") && !binding.containsKey("minimum") && !binding.containsKey("maximum"), "非整数绑定不伪造编码/范围");
        require(!((List<?>) binding.get("sourceEvidence")).isEmpty(), "绑定实现来源");
    }

    private static void effect(Map<?, ?> method) {
        Map<?, ?> effect = contract(method, "pipelineEffect");
        require("APPEND_STAGE".equals(effect.get("operation")) && "RECEIVER".equals(effect.get("target"))
                && "ONE".equals(effect.get("count")) && "CALL_ORDER".equals(effect.get("order")), "追加一个完整 Stage");
    }

    private static void mutations(Path project) throws Exception {
        Path root = Files.createTempDirectory("union-evidence-");
        try {
            Path aggregate = root.resolve(AGGREGATE.replace('.', '/') + ".java");
            Path builder = root.resolve(BUILDER.replace('.', '/') + ".java");
            Files.createDirectories(aggregate.getParent());
            String original = Files.readString(project.resolve("mongo-plus-core/src/main/java/" + AGGREGATE.replace('.', '/') + ".java"));
            String wrapper = Files.readString(project.resolve("mongo-plus-core/src/main/java/" + BUILDER.replace('.', '/') + ".java"));
            Files.writeString(aggregate, original);
            Files.writeString(builder, wrapper);
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).addSourceRoot(root).build());
            complete(generator.generate(), List.of("limit(int limit)"));
            int end = original.indexOf("Children unionWith(final String collectionName,final Aggregate<?> aggregate);");
            require(end >= 0, "真实目标 overload");
            int start = original.lastIndexOf("/**", end);
            String declaration = original.substring(start, end);
            for (String regex : List.of("(?m)^\\s*\\* @mongoObjectField(?:Source)? collectionName[^\\r\\n]*\\r?\\n",
                    "(?m)^\\s*\\* @mongoObjectField(?:Source)? aggregate[^\\r\\n]*\\r?\\n")) {
                String changed = declaration.replaceAll(regex, "");
                require(!changed.equals(declaration), "删除真实字段 metadata");
                Files.writeString(aggregate, original.substring(0, start) + changed + original.substring(end));
                incomplete(generator);
            }
            for (String tag : List.of(INPUT, EFFECT)) {
                require(declaration.contains(tag), "删除真实 extraction/effect 标签");
                Files.writeString(aggregate, original.substring(0, start) + declaration.replace(tag, "") + original.substring(end));
                incomplete(generator);
            }
            require(original.contains(REPRESENTATION) && wrapper.contains(FACTORY), "真实 construction metadata");
            Files.writeString(aggregate, original.replace(REPRESENTATION, ""));
            incomplete(generator);
            Files.writeString(aggregate, original);
            Files.writeString(builder, wrapper.replace(FACTORY, ""));
            incomplete(generator);
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static void incomplete(MongoPlusIndexer generator) throws Exception {
        boolean failed = false;
        try { complete(generator.generate(), List.of("limit(int limit)")); }
        catch (IllegalArgumentException | AssertionError expected) { failed = true; }
        require(failed, "缺少真实 metadata 时同一验收必须失败");
        cases++;
    }

    private static Map<?, ?> contract(Map<?, ?> value, String key) {
        require(value.get(key) instanceof Map, "缺少 " + key);
        Map<?, ?> contract = (Map<?, ?>) value.get(key);
        require("JAVADOC".equals(((Map<?, ?>) contract.get("sourceEvidence")).get("source")), "显式源码 metadata");
        return contract;
    }

    private static Map<?, ?> type(MongoPlusApiIndex index, String name) {
        return index.list("types").stream().map(t -> (Map<?, ?>) t)
                .filter(t -> name.equals(t.get("qualifiedName"))).findFirst().orElseThrow();
    }

    private static Map<?, ?> callable(Map<?, ?> type, String collection, String signature) {
        return ((List<?>) type.get(collection)).stream().map(m -> (Map<?, ?>) m)
                .filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }

    private static Map<?, ?> overload(MongoPlusApiIndex index, String signature) {
        return index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                .flatMap(f -> ((List<?>) f.get("overloads")).stream()).map(m -> (Map<?, ?>) m)
                .filter(m -> AGGREGATE.equals(m.get("declaredIn")) && signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }

    private static Map<?, ?> parameter(Map<?, ?> method, int offset) { return (Map<?, ?>) ((List<?>) method.get("parameters")).get(offset); }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
