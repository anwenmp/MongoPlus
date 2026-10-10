package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** out 的正式 Effect 及逐声明来源门禁；不将 Stage 追加证据当作执行或完整参数准入。 */
public final class PipelineOutStageEffectEvidenceSelfTest {
    private static final String AGGREGATE = "com.mongoplus.aggregate.Aggregate";
    private static final String VALUE = "operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER";
    private static final List<String> SIGNATURES = List.of("out(String collectionName)",
            "out(Class<?> collection)", "out(String databaseName, String collectionName)");

    private PipelineOutStageEffectEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]).toAbsolutePath();
        MongoPlusApiIndex index = generate(project, null);
        long count = methods(index).filter(m -> List.of("$out").equals(m.get("mongoStages"))).count();
        require(count == SIGNATURES.size(), "仅保留三条正式 out 映射");
        require(find(index, "out(Bson bson)") == null, "Bson 透传入口保持未映射");
        require(((List<?>) index.asMap().get("requiredCapabilities")).contains("PIPELINE_CONSTRUCTION_V1"),
                "复用既有 Effect capability");
        int missingEffect = 0;
        int missingSource = 0;
        for (String signature : SIGNATURES) {
            Map<?, ?> family = find(index, signature);
            Map<?, ?> publicView = publicMethod(index, signature);
            require(family != null && family.equals(publicView), "两个正式视图一致: " + signature);
            for (Map<?, ?> view : List.of(family, publicView)) {
                effect(view);
                Map<Object, Object> changed = new LinkedHashMap<>(view);
                changed.remove("pipelineEffect");
                refuses(changed);
                missingEffect++;
                for (String field : List.of("sourceEvidence", "source", "tag", "value")) {
                    Map<Object, Object> contract = new LinkedHashMap<>((Map<?, ?>) view.get("pipelineEffect"));
                    if ("sourceEvidence".equals(field)) {
                        contract.remove(field);
                    } else {
                        Map<Object, Object> source = new LinkedHashMap<>((Map<?, ?>) contract.get("sourceEvidence"));
                        source.remove(field);
                        contract.put("sourceEvidence", source);
                    }
                    changed = new LinkedHashMap<>(view);
                    changed.put("pipelineEffect", contract);
                    refuses(changed);
                    missingSource++;
                }
            }
        }
        int sourceRemoval = removeRealTags(project, index);
        System.out.println("PipelineOutStageEffectEvidenceSelfTest PASSED: 3 overloads; 6 view checks; "
                + missingEffect + " missing-effect rejections; " + missingSource
                + " missing-source rejections; " + sourceRemoval
                + " real-source-removal view rejections; graphLookup/bucket unchanged; Bson unmapped");
    }

    /** 只修改临时源码夹具；正式源文件和生成后的 API JSON 均不参与负例变更。 */
    private static int removeRealTags(Path project, MongoPlusApiIndex original) throws Exception {
        Path root = Files.createTempDirectory("out-stage-effect-removal-");
        String source = Files.readString(project.resolve("mongo-plus-core/src/main/java/"
                + AGGREGATE.replace('.', '/') + ".java"));
        Path fixture = root.resolve(AGGREGATE.replace('.', '/') + ".java");
        int rejected = 0;
        try {
            Files.createDirectories(fixture.getParent());
            for (String signature : SIGNATURES) {
                String declaration = signature.replace("String ", "final String ")
                        .replace("Class<?> ", "final Class<?> ");
                int offset = source.indexOf(declaration + ";");
                require(offset >= 0, "定位真实接口声明: " + signature);
                int start = source.lastIndexOf("/**", offset);
                int end = source.indexOf("*/", start);
                String comment = source.substring(start, end);
                require(comment.contains("@mongoPipelineEffect " + VALUE), "目标声明有自己的标签");
                Files.writeString(fixture, source.substring(0, start)
                        + comment.replace("@mongoPipelineEffect " + VALUE, "") + source.substring(end));
                MongoPlusApiIndex changed = generate(project, root);
                for (Map<?, ?> view : List.of(find(changed, signature), publicMethod(changed, signature))) {
                    require(!view.containsKey("pipelineEffect"), "Stage 映射不得补回已删除的 Effect");
                    refuses(view);
                    rejected++;
                }
                for (String other : SIGNATURES) {
                    if (!other.equals(signature)) {
                        require(find(original, other).equals(find(changed, other)), "相邻 overload 保持不变");
                    }
                }
                for (String name : List.of("graphLookup", "bucket")) {
                    require(methods(original).filter(m -> name.equals(m.get("name"))).toList()
                            .equals(methods(changed).filter(m -> name.equals(m.get("name"))).toList()),
                            "既有 family 证据保持不变: " + name);
                    require(publicMethods(original).filter(m -> name.equals(m.get("name"))).toList()
                            .equals(publicMethods(changed).filter(m -> name.equals(m.get("name"))).toList()),
                            "既有 publicMethods 证据保持不变: " + name);
                }
            }
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
        return rejected;
    }

    private static MongoPlusApiIndex generate(Path project, Path extraRoot) throws Exception {
        MongoPlusIndexerConfig.Builder config = MongoPlusIndexerConfig.forPipelineProject(project);
        if (extraRoot != null) { config.addSourceRoot(extraRoot); }
        return new MongoPlusIndexer(config.build()).generate();
    }

    private static Stream<Map<?, ?>> methods(MongoPlusApiIndex index) {
        return index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                .flatMap(f -> ((List<?>) f.get("overloads")).stream()).map(m -> (Map<?, ?>) m);
    }

    private static Stream<Map<?, ?>> publicMethods(MongoPlusApiIndex index) {
        Map<?, ?> type = index.list("types").stream().map(t -> (Map<?, ?>) t)
                .filter(t -> AGGREGATE.equals(t.get("qualifiedName"))).findFirst().orElseThrow();
        return ((List<?>) type.get("publicMethods")).stream().map(m -> (Map<?, ?>) m);
    }

    private static Map<?, ?> find(MongoPlusApiIndex index, String signature) {
        return methods(index).filter(m -> AGGREGATE.equals(m.get("declaredIn"))
                && signature.equals(m.get("signature"))).findFirst().orElse(null);
    }

    private static Map<?, ?> publicMethod(MongoPlusApiIndex index, String signature) {
        return publicMethods(index).filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }

    private static void effect(Map<?, ?> method) {
        require(method.get("pipelineEffect") instanceof Map, "必须有逐 overload Effect");
        Map<?, ?> contract = (Map<?, ?>) method.get("pipelineEffect");
        require("APPEND_STAGE".equals(contract.get("operation")) && "RECEIVER".equals(contract.get("target"))
                && "ONE".equals(contract.get("count")) && "CALL_ORDER".equals(contract.get("order")),
                "正常返回时向 receiver 按调用顺序追加一个 Stage");
        require(contract.get("sourceEvidence") instanceof Map, "必须有来源证据");
        Map<?, ?> source = (Map<?, ?>) contract.get("sourceEvidence");
        require("JAVADOC".equals(source.get("source")) && "mongoPipelineEffect".equals(source.get("tag"))
                && VALUE.equals(source.get("value")), "来源和标签值必须完整匹配");
    }

    private static void refuses(Map<?, ?> method) {
        try { effect(method); }
        catch (AssertionError expected) { return; }
        throw new AssertionError("缺失 Effect 或来源证据必须拒绝");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
