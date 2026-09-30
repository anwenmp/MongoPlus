package com.mongoplus.indexer;

import com.mongoplus.indexer.json.JsonWriter;
import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 两条目标管道所需的封闭 evidence；通过真实源码删标签和无关名称夹具证明非名称推断。 */
public final class PipelineConstructionEvidenceSelfTest {
    private static final String AGGREGATE = "com.mongoplus.aggregate.Aggregate";
    private static final String BUILDER = "com.mongoplus.aggregate.AggregateWrapper";
    private static final String ENTRY = "com.mongoplus.aggregate.pipeline.Facet";
    private static final String DRIVER_ENTRY = "com.mongodb.client.model.Facet";
    private static final String CORE = "mongo-plus-core/src/main/java/";
    private static final String FACTORY = "@mongoPipelineFactory receiver=NEW initial=EMPTY ownership=INDEPENDENT";
    private static final String EFFECT = "@mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER";
    private static final String REPRESENTATION = "@mongoPipelineRepresentation source=RECEIVER semanticType=PIPELINE order=CALL_ORDER access=LIVE_VIEW";
    private static final String INPUT = "@mongoPipelineInput aggregateChainWrapper extractor=" + AGGREGATE + "#getAggregateConditionList()";
    private static final String COMPOSITION = "@mongoComposition OUTPUT_FIELD_NAME + PIPELINE -> NAMED_PIPELINE";
    private static final String CONTAINER = "@mongoPipelineContainer facets operation=NAMED_PIPELINES result=STAGE_BODY_DOCUMENT order=INPUT invocation=SINGLE";
    private static int cases;

    private PipelineConstructionEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Paths.get(args[0]).toAbsolutePath();
        MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).build());
        MongoPlusApiIndex index = generator.generate();
        complete(index);
        cases++;
        require(json(index).equals(json(generator.generate())), "正式 Index 连续两次必须字节稳定");
        cases++;
        mutations(project);
        genericNames();
        System.out.println("PipelineConstructionEvidenceSelfTest PASSED: " + cases
                + " cases; real factory/effect/extraction/constructor/two containers; metadata removal; generic names; deterministic");
    }

    private static void complete(MongoPlusApiIndex index) {
        Map<?, ?> builder = type(index, BUILDER);
        Map<?, ?> factory = callable(builder, "constructors", "AggregateWrapper()");
        Map<?, ?> creation = contract(factory, "pipelineFactory");
        require("NEW".equals(creation.get("receiver")) && "EMPTY".equals(creation.get("initial"))
                && "INDEPENDENT".equals(creation.get("ownership"))
                && BUILDER.equals(creation.get("receiverType")), "每次 factory 必须创建独立空 receiver");
        require(((List<?>) builder.get("extendsOrImplements")).contains("LambdaAggregateWrapper<AggregateWrapper>"),
                "factory 的真实 Aggregate 实现继承链");
        require(((List<?>) type(index, "com.mongoplus.aggregate.LambdaAggregateWrapper").get("extendsOrImplements"))
                .contains("Aggregate<Children>"), "receiver 可作为 Aggregate<?> 使用");
        Map<?, ?> aggregate = type(index, AGGREGATE);
        for (String signature : List.of("limit(int limit)", "sort(String field, Integer value)",
                "facet(Facet... facets)", "facet(List<Facet> facets)")) {
            Map<?, ?> method = callable(aggregate, "publicMethods", signature);
            Map<?, ?> effect = contract(method, "pipelineEffect");
            require("APPEND_STAGE".equals(effect.get("operation")) && "RECEIVER".equals(effect.get("target"))
                    && "ONE".equals(effect.get("count")) && "CALL_ORDER".equals(effect.get("order")),
                    "Stage 必须按调用顺序追加一个完整 Stage: " + signature);
            require(method.equals(overload(index, signature)), "family/publicMethods 两个视图完整一致");
        }
        Map<?, ?> getter = callable(aggregate, "publicMethods", "getAggregateConditionList()");
        Map<?, ?> representation = contract(getter, "pipelineRepresentation");
        require("PIPELINE".equals(getter.get("resultSemanticType"))
                && "RECEIVER".equals(representation.get("source"))
                && "CALL_ORDER".equals(representation.get("order"))
                && "LIVE_VIEW".equals(representation.get("access")), "完整有序 PIPELINE 的直接视图");
        Map<?, ?> entryType = type(index, ENTRY);
        Map<?, ?> entry = callable(entryType, "constructors", "Facet(String name, Aggregate<?> aggregateChainWrapper)");
        semantic(parameter(entry, 0), "OUTPUT_FIELD_NAME", "VALUE");
        semantic(parameter(entry, 1), "PIPELINE", "VALUE");
        require("NAMED_PIPELINE".equals(entry.get("resultSemanticType"))
                && List.of(COMPOSITION.substring("@mongoComposition ".length())).equals(entry.get("compositionSemantics")),
                "名称和完整 PIPELINE 组合为一个 entry");
        require((AGGREGATE + "#getAggregateConditionList()")
                .equals(contract(parameter(entry, 1), "pipelineExtraction").get("apiRef")), "构造器消费真实 extractor");
        require(((List<?>) entryType.get("extendsOrImplements")).contains(DRIVER_ENTRY), "entry 可赋给真实 Driver 容器");
        for (String signature : List.of("facet(Facet... facets)", "facet(List<Facet> facets)")) {
            Map<?, ?> method = callable(aggregate, "publicMethods", signature);
            Map<?, ?> input = parameter(method, 0);
            semantic(input, "NAMED_PIPELINE", "ELEMENT");
            require(DRIVER_ENTRY.equals(input.get("elementJavaType")), "容器元素 Java 类型匹配 entry 父类型");
            Map<?, ?> container = contract(method, "pipelineContainer");
            require("NAMED_PIPELINES".equals(container.get("operation")) && "INPUT".equals(container.get("order"))
                    && "SINGLE".equals(container.get("invocation"))
                    && "STAGE_BODY_DOCUMENT".equals(container.get("result"))
                    && "OUTPUT_FIELD_NAME".equals(container.get("keySemanticType"))
                    && "PIPELINE".equals(container.get("valueSemanticType")),
                    "多个独立命名 entry 必须一次合成同一 Stage body");
        }
        require(((List<?>) index.asMap().get("requiredCapabilities")).contains("PIPELINE_CONSTRUCTION_V1"), "能力声明");
        require(!callable(aggregate, "publicMethods", "limit(long limit)").containsKey("pipelineEffect"), "不传播相邻 overload");
        require(!callable(entryType, "constructors", "Facet(SFunction<T, ?> name, Aggregate<?> aggregateChainWrapper)")
                .containsKey("resultSemanticType"), "不传播相邻 constructor");
    }

    private static void mutations(Path project) throws Exception {
        Path root = Files.createTempDirectory("pipeline-construction-mutations-");
        try {
            for (String owner : List.of(AGGREGATE, BUILDER, ENTRY)) {
                Path file = root.resolve(owner.replace('.', '/') + ".java");
                Files.createDirectories(file.getParent());
                Files.copy(project.resolve(CORE + owner.replace('.', '/') + ".java"), file);
            }
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project)
                    .addSourceRoot(root).build());
            // 夹具覆盖真实声明；先闭合，随后逐项去掉 metadata，原验收必须失败。
            complete(generator.generate());
            for (String[] mutation : List.of(new String[]{BUILDER, FACTORY}, new String[]{AGGREGATE, EFFECT},
                    new String[]{AGGREGATE, REPRESENTATION}, new String[]{ENTRY, "@mongoParam name OUTPUT_FIELD_NAME VALUE"},
                    new String[]{ENTRY, "@mongoParam aggregateChainWrapper PIPELINE VALUE"}, new String[]{ENTRY, INPUT},
                    new String[]{ENTRY, COMPOSITION}, new String[]{AGGREGATE, "@mongoParam facets NAMED_PIPELINE ELEMENT"},
                    new String[]{AGGREGATE, CONTAINER})) {
                Path file = root.resolve(mutation[0].replace('.', '/') + ".java");
                String source = Files.readString(file);
                require(source.contains(mutation[1]), "mutation 必须删除真实源码标签");
                Files.writeString(file, source.replace(mutation[1], ""));
                boolean failed = false;
                try { complete(generator.generate()); }
                catch (IllegalArgumentException | AssertionError expected) { failed = true; }
                require(failed, "缺失标签后不可通过原验收: " + mutation[1]);
                cases++;
                Files.writeString(file, source);
            }
            Path aggregate = root.resolve(AGGREGATE.replace('.', '/') + ".java");
            String source = Files.readString(aggregate);
            for (String change : List.of(source.replace("count=ONE", "count=TWO"),
                    source.replace("order=INPUT", "order=REVERSED"),
                    source.replace("final Facet... facets", "final List<List<Facet>> facets"))) {
                Files.writeString(aggregate, change);
                reject(generator);
            }
        } finally { removeFixture(root); }
    }

    private static void genericNames() throws Exception {
        Path root = Files.createTempDirectory("pipeline-construction-generic-");
        try {
            Path aggregate = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(aggregate.getParent());
            Files.writeString(aggregate, "package com.mongoplus.aggregate; import java.util.List; import fixture.Sequence; "
                    + "public interface Aggregate<C> {\n/**\n * " + REPRESENTATION
                    + "\n */ List<org.bson.conversions.Bson> expose();\n/**\n * @mongoStage $arbitrary\n * " + EFFECT
                    + "\n */ C append(int amount);\n/**\n * @mongoStage $other\n * @mongoParam entries NAMED_PIPELINE ELEMENT\n * "
                    + CONTAINER.replace("facets", "entries") + "\n * " + EFFECT + "\n */ C bundle(Sequence... entries); }");
            Path builder = root.resolve("fixture/Builder.java");
            Files.createDirectories(builder.getParent());
            Files.writeString(builder, "package fixture; public class Builder {\n/** " + FACTORY + " */ public Builder() {} }");
            Files.writeString(root.resolve("fixture/Sequence.java"), "package fixture; import com.mongoplus.aggregate.Aggregate; "
                    + "public class Sequence {\n/**\n * @mongoParam key OUTPUT_FIELD_NAME VALUE\n * @mongoParam body PIPELINE VALUE\n * "
                    + INPUT.replace("aggregateChainWrapper", "body").replace("getAggregateConditionList", "expose")
                    + "\n * " + COMPOSITION + "\n */ public Sequence(String key, Aggregate<?> body) {} }");
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder().addSourceRoot(root)
                    .pipeline(true).addConstructionRoot("fixture.Builder").build());
            MongoPlusApiIndex index = generator.generate();
            contract(callable(type(index, "fixture.Builder"), "constructors", "Builder()"), "pipelineFactory");
            contract(callable(type(index, AGGREGATE), "publicMethods", "append(int amount)"), "pipelineEffect");
            contract(callable(type(index, AGGREGATE), "publicMethods", "expose()"), "pipelineRepresentation");
            require("NAMED_PIPELINE".equals(callable(type(index, "fixture.Sequence"), "constructors",
                    "Sequence(String key, Aggregate<?> body)").get("resultSemanticType")), "无关类名仍能构造命名管道");
            contract(callable(type(index, AGGREGATE), "publicMethods", "bundle(Sequence... entries)"), "pipelineContainer");
            cases++;
            String source = Files.readString(aggregate);
            Files.writeString(aggregate, source.replace(EFFECT, ""));
            require(!callable(type(generator.generate(), AGGREGATE), "publicMethods", "append(int amount)")
                    .containsKey("pipelineEffect"), "只有 Stage 标签不能推断 append effect");
            cases++;
        } finally { removeFixture(root); }
    }

    private static void reject(MongoPlusIndexer generator) throws Exception {
        try { generator.generate(); }
        catch (IllegalArgumentException expected) { cases++; return; }
        throw new AssertionError("非法显式契约必须拒绝");
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
                .filter(m -> AGGREGATE.equals(m.get("declaredIn")) && signature.equals(m.get("signature")))
                .findFirst().orElseThrow();
    }

    private static Map<?, ?> parameter(Map<?, ?> method, int offset) { return (Map<?, ?>) ((List<?>) method.get("parameters")).get(offset); }

    private static Map<?, ?> contract(Map<?, ?> value, String name) {
        require(value.get(name) instanceof Map, "缺少 " + name);
        Map<?, ?> contract = (Map<?, ?>) value.get(name);
        require("JAVADOC".equals(((Map<?, ?>) contract.get("sourceEvidence")).get("source")), "契约必须来自源码标签");
        return contract;
    }

    private static void semantic(Map<?, ?> parameter, String semantic, String scope) {
        require(semantic.equals(parameter.get("semanticType")) && scope.equals(parameter.get("semanticScope"))
                && parameter.containsKey("semanticEvidence"), "缺少显式参数语义: " + semantic);
    }

    private static String json(MongoPlusApiIndex index) throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter.write(index.asMap(), writer);
        return writer.toString();
    }

    private static void removeFixture(Path root) throws Exception {
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
