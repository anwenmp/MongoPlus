package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** 两个无 let lookup 输入的正式 evidence 闭环，复用既有通用构造契约。 */
public final class PipelineLookupEvidenceSelfTest {
    private static final String AGGREGATE = "com.mongoplus.aggregate.Aggregate";
    private static final String BUILDER = "com.mongoplus.aggregate.AggregateWrapper";
    private static final String SIGNATURE = "lookup(String from, Aggregate<?> aggregate, String as)";

    private PipelineLookupEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        MongoPlusApiIndex index = new MongoPlusIndexer(
                MongoPlusIndexerConfig.forPipelineProject(Path.of(args[0]).toAbsolutePath()).build()).generate();
        complete(index, List.of("limit(int limit)"));
        complete(index, List.of("sort(String field, Integer value)", "limit(int limit)"));
        System.out.println("PipelineLookupEvidenceSelfTest PASSED: 2 inputs; limit; sort then limit; "
                + "factory/representation/extraction/bindings/effects; both index views");
    }

    private static void complete(MongoPlusApiIndex index, List<String> innerStages) {
        Map<?, ?> builder = type(index, BUILDER);
        Map<?, ?> factory = contract(callable(builder, "constructors", "AggregateWrapper()"), "pipelineFactory");
        require("NEW".equals(factory.get("receiver")) && "EMPTY".equals(factory.get("initial"))
                && "INDEPENDENT".equals(factory.get("ownership")) && BUILDER.equals(factory.get("receiverType"))
                && "PIPELINE".equals(factory.get("semanticType")), "独立空 PIPELINE receiver");
        require(((List<?>) builder.get("extendsOrImplements"))
                .contains("LambdaAggregateWrapper<AggregateWrapper>"), "Wrapper 的真实父类型");
        require(((List<?>) type(index, "com.mongoplus.aggregate.LambdaAggregateWrapper")
                .get("extendsOrImplements")).contains("Aggregate<Children>"), "receiver 可赋给 Aggregate<?>");
        for (String signature : innerStages) { effect(overload(index, signature)); }

        Map<?, ?> getter = callable(type(index, AGGREGATE), "publicMethods", "getAggregateConditionList()");
        Map<?, ?> representation = contract(getter, "pipelineRepresentation");
        require("PIPELINE".equals(getter.get("resultSemanticType"))
                && "PIPELINE".equals(representation.get("semanticType"))
                && "RECEIVER".equals(representation.get("source"))
                && "CALL_ORDER".equals(representation.get("order"))
                && "LIVE_VIEW".equals(representation.get("access")), "有序 receiver PIPELINE representation");

        Map<?, ?> target = overload(index, SIGNATURE);
        require(((List<?>) target.get("mongoStages")).equals(List.of("$lookup"))
                && ((List<?>) target.get("mongoExpressions")).isEmpty(), "唯一显式 Stage 映射");
        require(((List<?>) target.get("parameterTypes")).equals(List.of("String", "Aggregate<?>", "String")),
                "目标无 let Java 签名");
        binding(parameter(target, 0), "from", "COLLECTION_NAME", "from");
        binding(parameter(target, 1), "aggregate", "PIPELINE", "pipeline");
        binding(parameter(target, 2), "as", "OUTPUT_FIELD_NAME", "as");
        require(acceptsString(index, parameter(target, 0)) && acceptsString(index, parameter(target, 2)),
                "集合名和输出字段名复用既有 String 原值表示");
        require((AGGREGATE + "#getAggregateConditionList()")
                .equals(contract(parameter(target, 1), "pipelineExtraction").get("apiRef")),
                "Aggregate 参数引用正式 PIPELINE extractor");
        effect(target);
        require(((List<?>) target.get("availableIn")).contains(AGGREGATE), "receiver 实现的契约可调用");
        require(target.equals(callable(type(index, AGGREGATE), "publicMethods", SIGNATURE)), "两个正式视图一致");
        require(((List<?>) index.asMap().get("requiredCapabilities")).containsAll(
                List.of("STAGE_OBJECT_FIELD_BINDING_V1", "PIPELINE_CONSTRUCTION_V1")), "复用既有 capability");
    }

    private static void binding(Map<?, ?> parameter, String name, String semantic, String field) {
        require(name.equals(parameter.get("name")) && semantic.equals(parameter.get("semanticType"))
                && "VALUE".equals(parameter.get("semanticScope"))
                && ("PIPELINE_PARAMETER_" + semantic).equals(parameter.get("conceptRef")), "显式参数语义: " + name);
        Map<?, ?> semanticEvidence = (Map<?, ?>) parameter.get("semanticEvidence");
        require("JAVADOC".equals(semanticEvidence.get("source"))
                && "mongoParam".equals(semanticEvidence.get("tag")), "显式参数标签");
        require(parameter.get("objectFieldBinding") instanceof Map, "缺少 " + field + " 字段绑定");
        Map<?, ?> binding = (Map<?, ?>) parameter.get("objectFieldBinding");
        require(field.equals(binding.get("field")), "对象字段映射: " + field);
        require(!binding.containsKey("encoding") && !binding.containsKey("minimum")
                && !binding.containsKey("maximum"), "非整数绑定不伪造编码/范围");
        List<?> sources = (List<?>) binding.get("sourceEvidence");
        require(sources.stream().map(s -> (Map<?, ?>) s).anyMatch(s ->
                "mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java"
                        .equals(s.get("path")) && ((String) s.get("symbols")).contains("lookup(String,Aggregate,String)")),
                "绑定具有真实 Core 委托来源");
        require(sources.stream().map(s -> (Map<?, ?>) s).anyMatch(s ->
                "org.mongodb:mongodb-driver-core:5.4.0".equals(s.get("artifact"))
                        && ((String) s.get("symbols")).contains("LookupStage.toBsonDocument")), "Driver 字段编码来源");
    }

    private static boolean acceptsString(MongoPlusApiIndex index, Map<?, ?> parameter) {
        if (!"String".equals(parameter.get("type"))) { return false; }
        Map<?, ?> concept = index.list("concepts").stream().map(c -> (Map<?, ?>) c)
                .filter(c -> parameter.get("conceptRef").equals(c.get("id"))).findFirst().orElseThrow();
        return ((List<?>) concept.get("representations")).stream().map(r -> (Map<?, ?>) r)
                .anyMatch(r -> "java.lang.String".equals(r.get("javaType"))
                        && "UNCHANGED".equals(r.get("encoding"))
                        && Boolean.FALSE.equals(r.get("automaticFieldPrefix"))
                        && Boolean.FALSE.equals(r.get("automaticPrefixRemoval")));
    }

    private static void effect(Map<?, ?> method) {
        Map<?, ?> effect = contract(method, "pipelineEffect");
        require("APPEND_STAGE".equals(effect.get("operation")) && "RECEIVER".equals(effect.get("target"))
                && "ONE".equals(effect.get("count")) && "CALL_ORDER".equals(effect.get("order")), "追加一个完整 Stage");
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
                .filter(m -> AGGREGATE.equals(m.get("declaredIn")) && signature.equals(m.get("signature")))
                .findFirst().orElseThrow();
    }

    private static Map<?, ?> parameter(Map<?, ?> method, int offset) {
        return (Map<?, ?>) ((List<?>) method.get("parameters")).get(offset);
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
