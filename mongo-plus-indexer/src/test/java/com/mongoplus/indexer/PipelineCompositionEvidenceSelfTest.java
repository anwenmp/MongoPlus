package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 普通文档组合器的逐 overload 证据；删标签后不得按名称、返回类型或相邻重载补全。 */
public final class PipelineCompositionEvidenceSelfTest {
    private static final String OWNER = "com.mongoplus.aggregate.pipeline.Projections";
    private static final String RELATION = "OUTPUT_FIELD_NAME + PIPELINE_EXPRESSION -> STAGE_BODY_DOCUMENT";
    private static final String FIELD_TAG = "@mongoParam fieldName OUTPUT_FIELD_NAME VALUE";
    private static final String EXPRESSION_TAG = "@mongoParam expression PIPELINE_EXPRESSION VALUE";
    private static final String COMPOSITION_TAG = "@mongoComposition " + RELATION;

    private PipelineCompositionEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Paths.get(args[0]);
        MongoPlusApiIndex index = new MongoPlusIndexer(
                MongoPlusIndexerConfig.forPipelineProject(project).build()).generate();
        List<Map<?, ?>> computed = methods(index, OWNER, "computed");
        require(computed.size() == 2, "必须有两个真实 computed overload");
        for (Map<?, ?> method : computed) { complete(method); }
        require(index.getMethodFamilies().stream().map(item -> (Map<?, ?>) item)
                .noneMatch(item -> "computed".equals(item.get("name"))), "computed 不能成为 MethodFamily");
        require(index.list("types").stream().map(item -> (Map<?, ?>) item)
                .noneMatch(item -> item.toString().contains("SimpleExpression")), "不得暴露 SimpleExpression");
        String source = Files.readString(project.resolve(
                "mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Projections.java"));
        verifyMutations(source);
        System.out.println("PipelineCompositionEvidenceSelfTest PASSED: 2 real overloads; "
                + "missing parameter/composition, overload isolation, renamed API, invalid/conflicting relation negatives");
    }

    private static void verifyMutations(String source) throws Exception {
        Path root = Files.createTempDirectory("pipeline-composition-evidence-");
        try {
            Path aggregate = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(aggregate.getParent());
            Files.writeString(aggregate, "package com.mongoplus.aggregate; public interface Aggregate<C> {} ");
            Path file = root.resolve("com/mongoplus/aggregate/pipeline/Projections.java");
            Files.createDirectories(file.getParent());
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                    .addSourceRoot(root).pipeline(true).addExpressionRoot(OWNER).build());
            Files.writeString(file, source);
            for (Map<?, ?> method : methods(generator.generate(), OWNER, "computed")) { complete(method); }
            for (String tag : List.of(FIELD_TAG, EXPRESSION_TAG, COMPOSITION_TAG)) {
                Files.writeString(file, source.replace(tag, ""));
                for (Map<?, ?> method : methods(generator.generate(), OWNER, "computed")) { absent(method, tag); }
                // 分别移除 String 和 Lambda 上的一个标签，保留另一重载的全部证据。
                for (int occurrence = 0; occurrence < 2; occurrence++) {
                    int offset = source.indexOf(tag);
                    if (occurrence == 1) { offset = source.indexOf(tag, offset + tag.length()); }
                    String changed = source.substring(0, offset)
                            + source.substring(offset + tag.length());
                    Files.writeString(file, changed);
                    for (Map<?, ?> method : methods(generator.generate(), OWNER, "computed")) {
                        boolean string = "String".equals(parameters(method).get(0).get("type"));
                        if (string == (occurrence == 0)) { absent(method, tag); }
                        else { complete(method); }
                    }
                }
            }
            // 相同方法名、Bson 返回值、正文、类级标签都不能为未标记的方法提供证据。
            Files.writeString(file, source.replace(FIELD_TAG, "")
                    .replace(EXPRESSION_TAG, "")
                    .replace(COMPOSITION_TAG, "")
                    .replace("创建字段的投影", RELATION + " 创建字段的投影")
                    .replace("public class Projections", "/** " + COMPOSITION_TAG + " */\npublic class Projections"));
            for (Map<?, ?> method : methods(generator.generate(), OWNER, "computed")) {
                for (String tag : List.of(FIELD_TAG, EXPRESSION_TAG, COMPOSITION_TAG)) { absent(method, tag); }
            }
            // 显式标签在无关类名和方法名上仍有效，证明提取器没有 computed/Projections 特判。
            String renamedOwner = OWNER.replace("Projections", "DocumentComposer");
            Path renamed = file.resolveSibling("DocumentComposer.java");
            Files.writeString(renamed, source.replace("Projections", "DocumentComposer").replace("computed", "assemble"));
            MongoPlusApiIndex renamedIndex = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                    .addSourceRoot(root).pipeline(true).addExpressionRoot(renamedOwner).build()).generate();
            require(methods(renamedIndex, renamedOwner, "assemble").size() == 2, "改名后仍有两个 overload");
            for (Map<?, ?> method : methods(renamedIndex, renamedOwner, "assemble")) { complete(method); }
            for (String invalid : List.of("OUTPUT_FIELD_NAME + -> STAGE_BODY_DOCUMENT",
                    RELATION + "\n * @mongoComposition OUTPUT_FIELD_NAME -> PIPELINE_STAGE_DOCUMENT")) {
                Files.writeString(file, source.replace(RELATION, invalid));
                boolean rejected = false;
                try { generator.generate(); } catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "非法或冲突组合声明必须拒绝");
            }
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static void complete(Map<?, ?> method) {
        require(List.of().equals(method.get("mongoExpressions")), "组合器不能标记为 Expression");
        require(List.of().equals(method.get("mongoStages")), "组合器不能标记为 Stage");
        List<Map<?, ?>> parameters = parameters(method);
        String[] roles = {"OUTPUT_FIELD_NAME", "PIPELINE_EXPRESSION"};
        for (int i = 0; i < roles.length; i++) {
            Map<?, ?> parameter = parameters.get(i);
            require(roles[i].equals(parameter.get("semanticType"))
                    && "VALUE".equals(parameter.get("semanticScope")), "参数必须有独立的 VALUE 语义");
            require(Map.of("source", "JAVADOC", "tag", "mongoParam", "value",
                    parameter.get("name") + " " + roles[i] + " VALUE").equals(parameter.get("semanticEvidence")),
                    "参数证据必须追溯至当前 overload 的 tag");
        }
        require(List.of(RELATION).equals(method.get("compositionSemantics")), "缺少显式组合关系");
        require("STAGE_BODY_DOCUMENT".equals(method.get("resultSemanticType")), "缺少结果语义");
        require(Map.of("source", "JAVADOC", "tag", "mongoComposition", "value", RELATION)
                .equals(method.get("resultSemanticEvidence")), "结果必须追溯至组合 tag");
    }

    private static void absent(Map<?, ?> method, String tag) {
        if (COMPOSITION_TAG.equals(tag)) {
            require(!method.containsKey("compositionSemantics") && !method.containsKey("resultSemanticType")
                    && !method.containsKey("resultSemanticEvidence"), "不能按 Bson/正文/其他 overload 推断结果");
        } else {
            Map<?, ?> parameter = parameters(method).get(FIELD_TAG.equals(tag) ? 0 : 1);
            require(!parameter.containsKey("semanticEvidence") && !parameter.containsKey("semanticScope")
                    && !(FIELD_TAG.equals(tag) ? "OUTPUT_FIELD_NAME" : "PIPELINE_EXPRESSION")
                    .equals(parameter.get("semanticType")), "不能按名称/组合关系/其他 overload 补全参数角色");
        }
    }

    private static List<Map<?, ?>> methods(MongoPlusApiIndex index, String owner, String name) {
        return index.list("types").stream().map(item -> (Map<?, ?>) item)
                .flatMap(type -> ((List<?>) type.get("publicMethods")).stream()).<Map<?, ?>>map(item -> (Map<?, ?>) item)
                .filter(method -> owner.equals(method.get("declaredIn")) && name.equals(method.get("name"))).toList();
    }

    private static List<Map<?, ?>> parameters(Map<?, ?> method) {
        return ((List<?>) method.get("parameters")).stream().<Map<?, ?>>map(item -> (Map<?, ?>) item).toList();
    }

    private static void require(boolean value, String message) {
        if (!value) { throw new AssertionError(message); }
    }
}
