package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** P0-01 只验收 construction effect，不将其当作参数绑定或完整 Resolver 准入。 */
public final class PipelineStageEffectEvidenceSelfTest {
    private static final String AGGREGATE = "com.mongoplus.aggregate.Aggregate";
    private static final String PROJECT = "com.mongoplus.aggregate.pipeline.Project";
    private static final String EFFECT = "@mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER"
            + " count=ONE order=CALL_ORDER";
    private static final List<String> OUTERS = List.of("facet(Facet... facets)",
            "lookup(String from, Aggregate<?> aggregate, String as)",
            "unionWith(String collectionName, Aggregate<?> aggregate)");
    private static int accepted;
    private static int rejected;

    private PipelineStageEffectEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]).toAbsolutePath();
        if (args.length == 2 && "--graph-lookup-only".equals(args[1])) {
            graphLookupEffects(project);
            return;
        }
        List<String[]> audit = new ArrayList<>();
        List<String> lines = Files.readAllLines(project.resolve("mongo-plus-indexer/src/test/resources/"
                + "pipeline-stage-effect-audit.tsv"));
        for (String line : lines.subList(1, lines.size())) {
            String[] row = line.split("\t", -1);
            require(row.length == 6, "审计行缺少逐 overload 委托链或边界");
            audit.add(row);
        }
        MongoPlusApiIndex index = generate(project);
        for (String[] row : audit) {
            Map<?, ?> method = find(index, owner(row), row[1]);
            if ("EFFECT".equals(row[3])) {
                require(method != null, "正式 Index 缺少已审计 overload: " + row[1]);
                effect(method);
                require(List.of(row[2]).equals(method.get("mongoStages")), "逐 overload 独立 Stage 映射");
                require(method.equals(publicMethod(index, owner(row), row[1])), "两个正式视图一致");
                for (String outer : OUTERS) { constructionEffects(index, outer, List.of(method)); accepted++; }
            } else {
                require(method == null, "未映射/任意 BSON 入口不得传播同名 Stage 或 effect: " + row[1]);
            }
        }
        require(audit.size() == 58 && accepted == 47 * OUTERS.size(), "审计范围为 58 overload、47 effect");
        // 这些事实仍不足，任何 effect 验收都不能替代参数或 composition 验收。
        for (String signature : List.of("skip(int skip)", "skip(long skip)")) {
            Map<?, ?> parameter = (Map<?, ?>) ((List<?>) find(index, AGGREGATE, signature).get("parameters")).get(0);
            require("INTEGER_VALUE".equals(parameter.get("semanticType"))
                    && !parameter.containsKey("objectFieldBinding") && parameter.containsKey("semanticEvidence")
                    && parameter.containsKey("stageValueBinding"),
                    "P0-02 用独立参数标签补足 skip，effect 不能替代或虚构对象字段绑定");
        }
        Map<?, ?> projectionFlag = (Map<?, ?>) ((List<?>) find(index, PROJECT,
                "project(boolean displayId, Projection... projection)").get("parameters")).get(0);
        require(!projectionFlag.containsKey("semanticEvidence"), "不伪造投影模式 evidence");
        Map<?, ?> options = (Map<?, ?>) ((List<?>) find(index, AGGREGATE,
                "unwind(String fieldName, UnwindOption unwindOption)").get("parameters")).get(1);
        require(!options.containsKey("entryConstruction") && !options.containsKey("objectFieldBinding"),
                "effect 不补 Options construction");
        mutations(project, audit);
        System.out.println("PipelineStageEffectEvidenceSelfTest PASSED: 58 audited; 47 effects; "
                + accepted + " effect-only outer/inner pairs; " + rejected
                + " missing-effect rejections; 11 unmapped/raw boundaries; parameter/composition gaps retained");
        graphLookupEffects(project);
    }

    /** 六条正式 graphLookup 映射复用既有 effect 门禁；任意 BSON 入口保持未映射。 */
    private static void graphLookupEffects(Path project) throws Exception {
        List<String> signatures = List.of(
                "graphLookup(String from, Object startWith, String connectFromField, String connectToField, String as)",
                "graphLookup(String from, Object startWith, String connectFromField, String connectToField, String as, GraphLookupOptions options)",
                "graphLookup(String from, Object startWith, SFunction<T, ?> connectFromField, SFunction<R, ?> connectToField, String as)",
                "graphLookup(String from, Object startWith, SFunction<T, ?> connectFromField, SFunction<R, ?> connectToField, String as, GraphLookupOptions options)",
                "graphLookup(String from, SFunction<T, ?> startWith, SFunction<R, ?> connectFromField, SFunction<U, ?> connectToField, String as)",
                "graphLookup(String from, SFunction<T, ?> startWith, SFunction<R, ?> connectFromField, SFunction<U, ?> connectToField, String as, GraphLookupOptions options)");
        MongoPlusApiIndex index = generate(project);
        long mapped = index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                .flatMap(f -> ((List<?>) f.get("overloads")).stream()).map(m -> (Map<?, ?>) m)
                .filter(m -> AGGREGATE.equals(m.get("declaredIn"))
                        && m.get("signature").toString().startsWith("graphLookup(")).count();
        require(mapped == signatures.size(), "graphLookup 仅保留六条正式映射");
        require(find(index, AGGREGATE, "graphLookup(Bson bson)") == null, "不映射任意 BSON 入口");
        List<String[]> audit = new ArrayList<>();
        int sourceRejections = 0;
        for (String signature : signatures) {
            Map<?, ?> method = find(index, AGGREGATE, signature);
            Map<?, ?> publicView = publicMethod(index, AGGREGATE, signature);
            require(method != null && method.equals(publicView), "graphLookup 两个正式视图一致: " + signature);
            require(List.of("$graphLookup").equals(method.get("mongoStages")), "独立 graphLookup Stage 映射");
            for (Map<?, ?> view : List.of(method, publicView)) {
                effect(view);
                Map<Object, Object> missingSource = new LinkedHashMap<>((Map<?, ?>) view.get("pipelineEffect"));
                missingSource.remove("sourceEvidence");
                Map<Object, Object> changed = new LinkedHashMap<>(view);
                changed.put("pipelineEffect", missingSource);
                boolean refused = false;
                try { effect(changed); }
                catch (AssertionError expected) { refused = true; }
                require(refused, "缺 source evidence 必须按同一 effect 契约拒绝: " + signature);
                sourceRejections++;
            }
            for (String outer : OUTERS) { constructionEffects(index, outer, List.of(method)); }
            audit.add(new String[] {"Aggregate", signature, "$graphLookup", "EFFECT"});
        }
        int previousRejections = rejected;
        mutations(project, audit);
        require(rejected - previousRejections == signatures.size() * OUTERS.size(), "逐源码标签删除均被拒绝");
        System.out.println("PipelineStageEffectEvidenceSelfTest graphLookup PASSED: 6 overloads; 12 view checks; "
                + "18 effect-only outer/inner pairs; " + (rejected - previousRejections)
                + " missing-effect rejections; " + sourceRejections + " missing-source rejections; Bson unmapped");
    }

    /** 只复用已有 factory/representation/extraction/container/effect 的静态验收，没有 nested planner。 */
    private static void constructionEffects(MongoPlusApiIndex index, String outer, List<Map<?, ?>> inner) {
        Map<?, ?> factory = publicCallable(index, "com.mongoplus.aggregate.AggregateWrapper",
                "constructors", "AggregateWrapper()");
        require(factory.containsKey("pipelineFactory"), "独立 receiver factory");
        require(publicMethod(index, AGGREGATE, "getAggregateConditionList()")
                .containsKey("pipelineRepresentation"), "有序 receiver representation");
        Map<?, ?> container = find(index, AGGREGATE, outer);
        effect(container);
        if (container.containsKey("pipelineContainer")) {
            Map<?, ?> entry = publicCallable(index, "com.mongoplus.aggregate.pipeline.Facet", "constructors",
                    "Facet(String name, Aggregate<?> aggregateChainWrapper)");
            require("NAMED_PIPELINE".equals(entry.get("resultSemanticType"))
                    && entry.containsKey("compositionSemantics"), "正式命名 entry composition");
            require(((Map<?, ?>) ((List<?>) entry.get("parameters")).get(1))
                    .containsKey("pipelineExtraction"), "命名 entry 的完整 PIPELINE extraction");
        } else {
            require(((List<?>) container.get("parameters")).stream().map(p -> (Map<?, ?>) p)
                    .anyMatch(p -> p.containsKey("pipelineExtraction")), "外层正式 PIPELINE extraction");
        }
        for (Map<?, ?> method : inner) { effect(method); }
    }

    /** 从真实源码逐个删除 effect，三个外层的同一 effect 验收都必须拒绝；相邻 overload 不变。 */
    private static void mutations(Path project, List<String[]> audit) throws Exception {
        Path root = Files.createTempDirectory("stage-effect-removal-");
        try {
            for (String[] row : audit) {
                if (!"EFFECT".equals(row[3])) { continue; }
                String owner = owner(row);
                Path sourceFile = project.resolve("mongo-plus-core/src/main/java/" + owner.replace('.', '/') + ".java");
                String source = Files.readString(sourceFile);
                // Index signature 去除 final/Nullable/声明泛型；按声明位置查 Javadoc，避免删错相邻 overload。
                int declaration = declaration(source, row[1]);
                int start = source.lastIndexOf("/**", declaration);
                int end = source.indexOf("*/", start);
                String comment = source.substring(start, end);
                require(comment.contains(EFFECT), "目标 overload 必须有自己的 effect 标签: " + row[1]);
                Path fixture = root.resolve(owner.replace('.', '/') + ".java");
                Files.createDirectories(fixture.getParent());
                // P0-03 的 body/policy 依赖 effect；隔离 effect 负测时一并移除目标声明的依赖标签。
                Files.writeString(fixture, source.substring(0, start) + comment.replace(EFFECT, "")
                        .replaceAll("(?m)^\\s*\\* @mongoDocument[^\\r\\n]*\\r?\\n", "") + source.substring(end));
                MongoPlusApiIndex changed = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project)
                        .addSourceRoot(root).build()).generate();
                Map<?, ?> removed = find(changed, owner, row[1]);
                require(removed != null && !removed.containsKey("pipelineEffect"), "Stage 名不能恢复缺失 effect");
                for (String outer : OUTERS) {
                    boolean refused = false;
                    try { constructionEffects(changed, outer, List.of(removed)); }
                    catch (AssertionError expected) { refused = true; }
                    require(refused, "缺 effect 必须拒绝: " + outer + " / " + row[1]);
                    rejected++;
                }
                for (String[] other : audit) {
                    if ("EFFECT".equals(other[3]) && !(owner(other).equals(owner) && other[1].equals(row[1]))) {
                        effect(find(changed, owner(other), other[1]));
                    }
                }
                Files.delete(fixture);
            }
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static int declaration(String source, String signature) {
        String normalized = source.replace("@Nullable ", "").replace("final ", "")
                .replaceAll("\\s*,\\s*", ", ").replaceAll("\\s+", " ");
        require(normalized.contains(signature), "审计签名必须存在于当前源码: " + signature);
        String method = signature.substring(0, signature.indexOf('('));
        int offset = 0;
        while ((offset = source.indexOf(method + "(", offset)) >= 0) {
            int end = source.indexOf(';', offset);
            if (end >= 0) {
                String candidate = source.substring(offset, end).replace("@Nullable ", "").replace("final ", "")
                        .replaceAll("\\s*,\\s*", ", ").replaceAll("\\s+", " ");
                if (signature.equals(candidate)) { return offset; }
            }
            offset++;
        }
        throw new AssertionError("无法定位精确声明: " + signature);
    }

    private static MongoPlusApiIndex generate(Path project) throws Exception {
        return new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).build()).generate();
    }

    private static String owner(String[] row) { return "Aggregate".equals(row[0]) ? AGGREGATE : PROJECT; }

    private static Map<?, ?> find(MongoPlusApiIndex index, String owner, String signature) {
        return index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                .flatMap(f -> ((List<?>) f.get("overloads")).stream()).map(m -> (Map<?, ?>) m)
                .filter(m -> owner.equals(m.get("declaredIn")) && signature.equals(m.get("signature")))
                .findFirst().orElse(null);
    }

    private static Map<?, ?> publicMethod(MongoPlusApiIndex index, String owner, String signature) {
        return publicCallable(index, owner, "publicMethods", signature);
    }

    private static Map<?, ?> publicCallable(MongoPlusApiIndex index, String owner, String collection, String signature) {
        Map<?, ?> type = index.list("types").stream().map(t -> (Map<?, ?>) t)
                .filter(t -> owner.equals(t.get("qualifiedName"))).findFirst().orElseThrow();
        return ((List<?>) type.get(collection)).stream().map(m -> (Map<?, ?>) m)
                .filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }

    private static void effect(Map<?, ?> method) {
        require(method != null && method.get("pipelineEffect") instanceof Map, "缺少逐 overload pipelineEffect");
        Map<?, ?> effect = (Map<?, ?>) method.get("pipelineEffect");
        require("APPEND_STAGE".equals(effect.get("operation")) && "RECEIVER".equals(effect.get("target"))
                && "ONE".equals(effect.get("count")) && "CALL_ORDER".equals(effect.get("order")), "单个有序追加 effect");
        Map<?, ?> source = (Map<?, ?>) effect.get("sourceEvidence");
        require(source != null && "JAVADOC".equals(source.get("source")) && "mongoPipelineEffect".equals(source.get("tag")),
                "effect 必须来自当前源码标签");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
