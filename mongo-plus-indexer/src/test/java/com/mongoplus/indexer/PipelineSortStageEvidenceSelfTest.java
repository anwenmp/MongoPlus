package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 只消费正式通用文档 composition / Stage effect；不实现 MCP planner 或候选选择。 */
public final class PipelineSortStageEvidenceSelfTest {
    private static final String AGGREGATE = "com.mongoplus.aggregate.Aggregate";
    private static final String SORTS = "com.mongoplus.aggregate.pipeline.Sorts";
    private static final String SIGNATURE = "sortSpecification(Bson specification)";
    private static final List<String> OUTERS = List.of("facet(Facet... facets)",
            "lookup(String from, Aggregate<?> aggregate, String as)",
            "unionWith(String collectionName, Aggregate<?> aggregate)");
    private static int accepted;
    private static int rejected;

    private PipelineSortStageEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]).toAbsolutePath().normalize();
        MongoPlusApiIndex index = generate(project, null);
        Map<?, ?> stage = method(index, AGGREGATE, SIGNATURE);
        Map<?, ?> familyStage = maps(index.getMethodFamilies()).stream()
                .flatMap(f -> maps((List<?>) f.get("overloads")).stream())
                .filter(m -> SIGNATURE.equals(m.get("signature"))).findFirst().orElseThrow();
        require(stage.equals(familyStage), "publicMethods 与 Stage overload 必须独立一致");
        require("Children".equals(stage.get("returnType")) && !stage.containsKey("resultSemanticType"),
                "方法返回 receiver；body→Stage composition 不能伪造返回 Bson 语义");
        require(stage.get("mongoStages").equals(List.of("$sort")), "自身正式 Stage 映射");
        Map<?, ?> parameter = parameters(stage).get(0);
        require("PIPELINE_PARAMETER_SORT_SPECIFICATION".equals(parameter.get("conceptRef"))
                && ((Map<?, ?>) parameter.get("semanticEvidence")).get("tag").equals("mongoParam"),
                "SORT_SPECIFICATION VALUE 参数独立来源");
        Map<?, ?> binding = (Map<?, ?>) parameter.get("documentInputBinding");
        require(maps((List<?>) binding.get("sourceEvidence")).stream()
                .filter(s -> "mongoDocumentSource".equals(s.get("tag"))).count() == 2,
                "Core 默认实现及固定 Driver 版本的独立包装来源");
        require(((List<?>) index.asMap().get("requiredCapabilities")).containsAll(
                List.of("DOCUMENT_REDUCTION_V1", "DOCUMENT_SHAPE_V1", "PIPELINE_CONSTRUCTION_V1")),
                "复用既有三种 capability");

        Map<?, ?> asc = method(index, SORTS, "asc(String... fieldNames)");
        Map<?, ?> desc = method(index, SORTS, "desc(String... fieldNames)");
        List<Map<?, ?>> reducers = methods(index, SORTS).stream()
                .filter(m -> m.containsKey("reductionContract")).toList();
        require(reducers.size() == 2, "两个合法 reducer 均保留，不选择第一个");
        for (Map<?, ?> reducer : reducers) {
            for (List<Map<String, Object>> parts : List.of(
                    List.of(entry(asc, "score")), List.of(entry(desc, "createTime")),
                    List.of(entry(asc, "score", "id")), List.of(entry(desc, "createTime", "id")),
                    List.of(entry(desc, "createTime"), entry(asc, "score"), entry(desc, "id")))) {
                Map<String, Object> body = reduce(reducer, parts);
                Map<String, Object> wrapped = wrap(stage, "SORT_SPECIFICATION", body);
                require(wrapped.keySet().equals(java.util.Set.of("$sort")) && wrapped.get("$sort") == body,
                        "包装一次，body 原序及固定 Integer 值保持");
                require(new ArrayList<>(((Map<?, ?>) wrapped.get("$sort")).keySet())
                        .equals(new ArrayList<>(body.keySet())), "不能按方向 regroup 字段");
                for (String outer : OUTERS) { nested(index, outer, stage); accepted++; }
            }
        }
        Map<String, Object> body = reduce(reducers.get(0), List.of(
                entry(desc, "createTime"), entry(asc, "score"), entry(desc, "id")));
        require(new ArrayList<>(body.keySet()).equals(List.of("createTime", "score", "id"))
                && new ArrayList<>(body.values()).equals(List.of(-1, 1, -1)), "目标混合方向完整原序");
        refuses(() -> wrap(stage, "STAGE_BODY_DOCUMENT", body));
        refuses(() -> wrap(stage, "PIPELINE_STAGE_DOCUMENT", Map.of("$sort", body)));
        Map<?, ?> legacy = method(index, AGGREGATE, "sort(Bson bson)");
        require(legacy.get("mongoStages").equals(List.of()) && !legacy.containsKey("pipelineEffect"),
                "旧透传不能继承新方法 Stage/effect");
        refuses(() -> wrap(legacy, "SORT_SPECIFICATION", body));

        Map<String, Object> noComposition = copy(stage);
        Map<String, Object> noBindingParameter = copy(parameter);
        noBindingParameter.remove("documentInputBinding");
        noComposition.put("parameters", List.of(noBindingParameter));
        Map<String, Object> noEffect = copy(stage);
        noEffect.remove("pipelineEffect");
        for (Map<?, ?> broken : List.of(noComposition, noEffect)) {
            refuses(() -> wrap(broken, "SORT_SPECIFICATION", body));
            for (String outer : OUTERS) { refuses(() -> nested(index, outer, broken)); }
        }
        sourceMutations(project);
        System.out.println("PipelineSortStageEvidenceSelfTest PASSED: " + accepted
                + " nested composition/effect pairs; " + rejected
                + " rejections; renamed API evidence passed; both reducers preserved; no candidate selection or MCP planner");
    }

    /** 测试中的通用 admission，只读取显式参数角色、包装声明、唯一 Stage 与 effect。 */
    private static Map<String, Object> wrap(Map<?, ?> method, String inputSemantic, Map<String, Object> body) {
        effect(method);
        if (parameters(method).size() != 1) { throw new IllegalArgumentException("单 body 输入未闭合"); }
        Map<?, ?> parameter = parameters(method).get(0);
        if (!inputSemantic.equals(parameter.get("semanticType")) || !"VALUE".equals(parameter.get("semanticScope"))
                || !parameter.containsKey("semanticEvidence")) {
            throw new IllegalArgumentException("输入角色缺失或不匹配");
        }
        Object raw = parameter.get("documentInputBinding");
        if (!(raw instanceof Map<?, ?> binding) || !binding.containsKey("sourceEvidence")
                || !inputSemantic.equals(binding.get("semanticType"))
                || !"WRAP_DECLARED_STAGE".equals(binding.get("encoding"))
                || !"DECLARED_STAGE_WRAPPER".equals(binding.get("bodyToStageConstruction"))) {
            throw new IllegalArgumentException("缺少正式 body→Stage composition");
        }
        List<?> stages = (List<?>) method.get("mongoStages");
        if (stages == null || stages.size() != 1) { throw new IllegalArgumentException("Stage 映射不唯一"); }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(stages.get(0).toString(), body);
        return result;
    }

    private static void nested(MongoPlusApiIndex index, String outerSignature, Map<?, ?> inner) {
        Map<?, ?> factory = callable(index, "com.mongoplus.aggregate.AggregateWrapper", "constructors", "AggregateWrapper()");
        require(factory.containsKey("pipelineFactory"), "独立 receiver factory");
        require(method(index, AGGREGATE, "getAggregateConditionList()").containsKey("pipelineRepresentation"),
                "有序 receiver extraction representation");
        Map<?, ?> outer = method(index, AGGREGATE, outerSignature);
        effect(outer);
        if (outer.containsKey("pipelineContainer")) {
            Map<?, ?> entry = callable(index, "com.mongoplus.aggregate.pipeline.Facet", "constructors",
                    "Facet(String name, Aggregate<?> aggregateChainWrapper)");
            require("NAMED_PIPELINE".equals(entry.get("resultSemanticType"))
                    && entry.containsKey("compositionSemantics")
                    && parameters(entry).get(1).containsKey("pipelineExtraction"), "命名分支 composition/extraction");
        } else {
            require(parameters(outer).stream().anyMatch(p -> p.containsKey("pipelineExtraction")), "PIPELINE extraction");
        }
        wrap(inner, "SORT_SPECIFICATION", new LinkedHashMap<>());
    }

    private static void effect(Map<?, ?> method) {
        Object raw = method.get("pipelineEffect");
        if (!(raw instanceof Map<?, ?> effect) || !"APPEND_STAGE".equals(effect.get("operation"))
                || !"RECEIVER".equals(effect.get("target")) || !"ONE".equals(effect.get("count"))
                || !"CALL_ORDER".equals(effect.get("order")) || !effect.containsKey("sourceEvidence")) {
            throw new IllegalArgumentException("缺少当前 receiver 的单 Stage 追加 effect");
        }
    }

    private static Map<String, Object> entry(Map<?, ?> method, String... fields) {
        Map<?, ?> construction = (Map<?, ?>) method.get("documentEntryConstruction");
        require("SORT_SPECIFICATION".equals(method.get("resultSemanticType"))
                && "INPUT".equals(construction.get("order")), "正式保序 body entry");
        Map<?, ?> value = (Map<?, ?>) construction.get("value");
        require("INT32_EXACT".equals(value.get("encoding")) && value.get("value") instanceof Integer,
                "方向来自固定 Int32 evidence");
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : fields) {
            if (result.putIfAbsent(field, value.get("value")) != null) { throw new IllegalArgumentException("重复字段"); }
        }
        return result;
    }

    private static Map<String, Object> reduce(Map<?, ?> method, List<Map<String, Object>> parts) {
        Map<?, ?> reduction = (Map<?, ?>) method.get("reductionContract");
        require("SORT_SPECIFICATION".equals(method.get("resultSemanticType"))
                && "DOCUMENT_MERGE".equals(reduction.get("operation"))
                && "INPUT".equals(reduction.get("order")), "复用同角色 DOCUMENT_REDUCTION_V1");
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map<String, Object> part : parts) {
            for (Map.Entry<String, Object> field : part.entrySet()) {
                if (result.putIfAbsent(field.getKey(), field.getValue()) != null) {
                    throw new IllegalArgumentException("归约前拒绝重复输入键");
                }
            }
        }
        return result;
    }

    private static void sourceMutations(Path project) throws Exception {
        String source = Files.readString(project.resolve("mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java"));
        int declaration = source.indexOf("default Children sortSpecification(");
        int start = source.lastIndexOf("/**", declaration);
        String comment = source.substring(start, declaration);
        Path root = Files.createTempDirectory("sort-stage-evidence-").toAbsolutePath().normalize();
        try {
            Path file = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(file.getParent());
            String noComposition = comment.replaceAll("(?m)^\\s*\\* @mongoDocument[^\\r\\n]*\\r?\\n", "");
            Files.writeString(file, source.substring(0, start) + noComposition + source.substring(declaration));
            MongoPlusApiIndex changedIndex = generate(project, root);
            Map<?, ?> changed = method(changedIndex, AGGREGATE, SIGNATURE);
            require(!parameters(changed).get(0).containsKey("documentInputBinding"), "名称/Bson/Stage/effect不能补composition");
            refuses(() -> wrap(changed, "SORT_SPECIFICATION", new LinkedHashMap<>()));
            for (String outer : OUTERS) { refuses(() -> nested(changedIndex, outer, changed)); }
            require(parameters(method(changedIndex, AGGREGATE, "sort(Bson bson)")).get(0)
                    .containsKey("documentInputBinding"), "只删除当前声明，相邻旧入口保持不变");
            for (String invalid : List.of(
                    comment.replaceAll("(?m)^\\s*\\* @mongoPipelineEffect[^\\r\\n]*\\r?\\n", ""),
                    comment.replaceAll("(?m)^\\s*\\* @mongoDocumentSource[^\\r\\n]*\\r?\\n", ""),
                    comment.replaceAll("(?m)^\\s*\\* @mongoParam[^\\r\\n]*\\r?\\n", ""),
                    comment.replaceAll("(?m)^\\s*\\* @mongoStage[^\\r\\n]*\\r?\\n", ""),
                    comment.replace("encoding=WRAP_DECLARED_STAGE", "encoding=UNCHANGED"),
                    comment.replace("semantic=SORT_SPECIFICATION", "semantic=PIPELINE_STAGE_DOCUMENT"),
                    comment.replace("parameter=specification", "parameter=missing"))) {
                Files.writeString(file, source.substring(0, start) + invalid + source.substring(declaration));
                refuses(() -> uncheckedGenerate(project, root));
            }
            String noBoth = noComposition.replaceAll("(?m)^\\s*\\* @mongoPipelineEffect[^\\r\\n]*\\r?\\n", "");
            Files.writeString(file, source.substring(0, start) + noBoth + source.substring(declaration));
            Map<?, ?> missing = method(generate(project, root), AGGREGATE, SIGNATURE);
            require(!missing.containsKey("pipelineEffect"), "缺失 effect 不能由相邻排序 overload 推断");
            refuses(() -> wrap(missing, "SORT_SPECIFICATION", new LinkedHashMap<>()));
            Files.writeString(file, source.replace("sortSpecification", "renamedBodyStage"));
            wrap(method(generate(project, root), AGGREGATE, "renamedBodyStage(Bson specification)"),
                    "SORT_SPECIFICATION", new LinkedHashMap<>());
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    require(path.toAbsolutePath().normalize().startsWith(root), "删除必须在fixture目录内");
                    Files.delete(path);
                }
            }
        }
    }

    private static MongoPlusApiIndex generate(Path project, Path extraRoot) throws IOException {
        MongoPlusIndexerConfig.Builder config = MongoPlusIndexerConfig.forPipelineProject(project);
        if (extraRoot != null) { config.addSourceRoot(extraRoot); }
        return new MongoPlusIndexer(config.build()).generate();
    }

    private static MongoPlusApiIndex uncheckedGenerate(Path project, Path extraRoot) {
        try { return generate(project, extraRoot); }
        catch (IOException exception) { throw new IllegalStateException(exception); }
    }

    private static Map<String, Object> copy(Map<?, ?> original) {
        Map<String, Object> copy = new LinkedHashMap<>();
        original.forEach((key, value) -> copy.put(key.toString(), value));
        return copy;
    }

    private static Map<?, ?> method(MongoPlusApiIndex index, String owner, String signature) {
        return callable(index, owner, "publicMethods", signature);
    }

    private static Map<?, ?> callable(MongoPlusApiIndex index, String owner, String collection, String signature) {
        return maps(index.list("types")).stream().filter(t -> owner.equals(t.get("qualifiedName")))
                .flatMap(t -> maps((List<?>) t.get(collection)).stream())
                .filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }

    private static List<Map<?, ?>> methods(MongoPlusApiIndex index, String owner) {
        return maps(index.list("types")).stream().filter(t -> owner.equals(t.get("qualifiedName")))
                .flatMap(t -> maps((List<?>) t.get("publicMethods")).stream()).toList();
    }

    private static List<Map<?, ?>> parameters(Map<?, ?> method) { return maps((List<?>) method.get("parameters")); }
    private static List<Map<?, ?>> maps(List<?> items) { return items.stream().<Map<?, ?>>map(i -> (Map<?, ?>) i).toList(); }

    private static void refuses(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException expected) { rejected++; return; }
        throw new AssertionError("缺证据或角色冲突必须拒绝");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
