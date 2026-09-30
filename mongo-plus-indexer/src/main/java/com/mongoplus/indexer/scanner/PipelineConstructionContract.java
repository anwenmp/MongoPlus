package com.mongoplus.indexer.scanner;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WildcardTree;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** receiver 状态及命名管道容器的显式契约；不从 API 名称或 Stage 名称生成语义。 */
final class PipelineConstructionContract {
    static final String CAPABILITY = "PIPELINE_CONSTRUCTION_V1";
    static final String FACTORY = "mongoPipelineFactory";
    static final String EFFECT = "mongoPipelineEffect";
    static final String REPRESENTATION = "mongoPipelineRepresentation";
    static final String INPUT = "mongoPipelineInput";
    static final String CONTAINER = "mongoPipelineContainer";
    private static final List<String> TAGS = Arrays.asList(FACTORY, EFFECT, REPRESENTATION, INPUT, CONTAINER);

    private PipelineConstructionContract() { }

    static boolean hasTags(Map<String, List<String>> tags) {
        return TAGS.stream().anyMatch(tags::containsKey);
    }

    static boolean apply(String declaration, String owner, boolean constructor, boolean staticMethod,
                         Map<String, List<String>> tags, Map<String, Object> value) {
        if (!hasTags(tags)) { return false; }
        if (tags.containsKey(FACTORY)) {
            if (!constructor || !parameters(value).isEmpty()) {
                throw invalid(declaration, "factory 必须是显式无参构造器");
            }
            Map<String, Object> contract = fixed(declaration, tags, FACTORY, 0,
                    "receiver=NEW", "initial=EMPTY", "ownership=INDEPENDENT");
            contract.put("receiverType", owner);
            contract.put("semanticType", "PIPELINE");
            value.put("pipelineFactory", contract);
        }
        if (tags.containsKey(EFFECT)) {
            instanceStage(declaration, constructor, staticMethod, value);
            value.put("pipelineEffect", fixed(declaration, tags, EFFECT, 0,
                    "operation=APPEND_STAGE", "target=RECEIVER", "count=ONE", "order=CALL_ORDER"));
        }
        if (tags.containsKey(REPRESENTATION)) {
            if (constructor || staticMethod || !parameters(value).isEmpty()) {
                throw invalid(declaration, "representation 必须是无参实例方法");
            }
            value.put("pipelineRepresentation", fixed(declaration, tags, REPRESENTATION, 0,
                    "source=RECEIVER", "semanticType=PIPELINE", "order=CALL_ORDER", "access=LIVE_VIEW"));
            result(declaration, value, "PIPELINE", REPRESENTATION, single(declaration, tags, REPRESENTATION));
        }
        if (tags.containsKey(INPUT)) {
            String raw = single(declaration, tags, INPUT);
            String[] tokens = raw.split("\\s+");
            if (tokens.length != 2 || !tokens[1].matches("extractor=[A-Za-z_$][\\w.$]*#[A-Za-z_$][\\w$]*\\(\\)")) {
                throw invalid(declaration, "input 语法为 <参数名> extractor=<完整类型>#<无参方法>()");
            }
            Map<String, Object> parameter = parameter(declaration, value, tokens[0], "PIPELINE", "VALUE");
            Map<String, Object> extraction = new LinkedHashMap<String, Object>();
            extraction.put("apiRef", tokens[1].substring("extractor=".length()));
            extraction.put("sourceEvidence", source(INPUT, raw));
            parameter.put("pipelineExtraction", extraction);
        }
        if (tags.containsKey(CONTAINER)) {
            instanceStage(declaration, constructor, staticMethod, value);
            String raw = single(declaration, tags, CONTAINER);
            String input = raw.split("\\s+")[0];
            Map<String, Object> parameter = parameter(declaration, value, input, "NAMED_PIPELINE", "ELEMENT");
            if (!parameter.containsKey("elementJavaType")) {
                throw invalid(declaration, "container 缺少已验证的元素 Java 表示");
            }
            Map<String, Object> contract = fixed(declaration, tags, CONTAINER, 1,
                    "operation=NAMED_PIPELINES", "result=STAGE_BODY_DOCUMENT", "order=INPUT", "invocation=SINGLE");
            contract.put("inputParameter", input);
            contract.put("inputSemanticType", "NAMED_PIPELINE");
            contract.put("inputScope", "ELEMENT");
            contract.put("keySemanticType", "OUTPUT_FIELD_NAME");
            contract.put("valueSemanticType", "PIPELINE");
            value.put("pipelineContainer", contract);
        }
        return true;
    }

    /** ELEMENT 只解开一层 AST 容器；元素类型用于后续构造结果的赋值兼容校验，不推断语义。 */
    static String elementType(Tree type, Function<String, String> resolveName) {
        Tree element;
        if (type instanceof ArrayTypeTree) {
            element = ((ArrayTypeTree) type).getType();
        } else if (type instanceof ParameterizedTypeTree) {
            ParameterizedTypeTree container = (ParameterizedTypeTree) type;
            if (!"java.util.List".equals(resolveName.apply(container.getType().toString()))
                    || container.getTypeArguments().size() != 1) {
                throw new IllegalArgumentException("NAMED_PIPELINE ELEMENT 必须是数组、varargs 或单层 List<T>");
            }
            element = container.getTypeArguments().get(0);
        } else {
            throw new IllegalArgumentException("NAMED_PIPELINE ELEMENT 缺少显式容器元素类型");
        }
        if (element instanceof WildcardTree) {
            if (element.getKind() != Tree.Kind.EXTENDS_WILDCARD) {
                throw new IllegalArgumentException("NAMED_PIPELINE 元素只接受具体类型或 extends 上界");
            }
            element = ((WildcardTree) element).getBound();
        }
        if (element.getKind() != Tree.Kind.IDENTIFIER && element.getKind() != Tree.Kind.MEMBER_SELECT) {
            throw new IllegalArgumentException("NAMED_PIPELINE 元素必须是单个引用类型");
        }
        return resolveName.apply(element.toString());
    }

    /** 引用必须指向本次正式闭包中具备 representation 标签的真实方法。 */
    static void validateExtractions(List<Object> types) {
        Map<String, Map<?, ?>> representations = new LinkedHashMap<String, Map<?, ?>>();
        for (Object item : types) {
            Map<?, ?> type = (Map<?, ?>) item;
            for (Object methodItem : (List<?>) type.get("publicMethods")) {
                Map<?, ?> method = (Map<?, ?>) methodItem;
                if (method.containsKey("pipelineRepresentation")) {
                    representations.put(method.get("declaredIn") + "#" + method.get("signature"), method);
                }
            }
        }
        for (Object item : types) {
            Map<?, ?> type = (Map<?, ?>) item;
            for (String collection : Arrays.asList("constructors", "publicMethods")) {
                for (Object callable : (List<?>) type.get(collection)) {
                    Map<?, ?> method = (Map<?, ?>) callable;
                    for (Object parameterItem : (List<?>) method.get("parameters")) {
                        Map<?, ?> parameter = (Map<?, ?>) parameterItem;
                        if (!parameter.containsKey("pipelineExtraction")) { continue; }
                        String reference = (String) ((Map<?, ?>) parameter.get("pipelineExtraction")).get("apiRef");
                        if (!representations.containsKey(reference)) {
                            throw invalid(type.get("qualifiedName") + "#" + method.get("signature"),
                                    "extractor 缺少显式 PIPELINE representation: " + reference);
                        }
                    }
                }
            }
        }
    }

    private static void instanceStage(String declaration, boolean constructor, boolean staticMethod,
                                      Map<String, Object> value) {
        if (constructor || staticMethod || ((List<?>) value.get("mongoStages")).size() != 1) {
            throw invalid(declaration, "effect/container 必须是有唯一显式 Stage 映射的实例方法");
        }
    }

    private static Map<String, Object> fixed(String declaration, Map<String, List<String>> tags,
                                              String tag, int skip, String... attributes) {
        String raw = single(declaration, tags, tag);
        String[] tokens = raw.split("\\s+");
        Map<String, String> expected = new LinkedHashMap<String, String>();
        for (String attribute : attributes) {
            int equals = attribute.indexOf('=');
            expected.put(attribute.substring(0, equals), attribute.substring(equals + 1));
        }
        Map<String, String> actual = new LinkedHashMap<String, String>();
        for (int i = skip; i < tokens.length; i++) {
            int equals = tokens[i].indexOf('=');
            if (equals <= 0 || equals == tokens[i].length() - 1) { throw invalid(declaration, "非法属性: " + tokens[i]); }
            String key = tokens[i].substring(0, equals);
            if (actual.putIfAbsent(key, tokens[i].substring(equals + 1)) != null) {
                throw invalid(declaration, "重复属性: " + key);
            }
        }
        if (!expected.equals(actual)) { throw invalid(declaration, tag + " 属性必须完整匹配 " + expected); }
        Map<String, Object> contract = new LinkedHashMap<String, Object>(expected);
        contract.put("sourceEvidence", source(tag, raw));
        return contract;
    }

    private static String single(String declaration, Map<String, List<String>> tags, String tag) {
        List<String> values = tags.getOrDefault(tag, Collections.<String>emptyList());
        if (values.size() != 1) { throw invalid(declaration, "只能声明一次 @" + tag); }
        return values.get(0);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parameter(String declaration, Map<String, Object> value,
                                                  String name, String semantic, String scope) {
        for (Object item : parameters(value)) {
            Map<String, Object> parameter = (Map<String, Object>) item;
            if (name.equals(parameter.get("name"))) {
                if (!semantic.equals(parameter.get("semanticType")) || !scope.equals(parameter.get("semanticScope"))
                        || !parameter.containsKey("semanticEvidence")) {
                    throw invalid(declaration, name + " 缺少显式 " + semantic + " " + scope);
                }
                return parameter;
            }
        }
        throw invalid(declaration, "参数不存在: " + name);
    }

    private static List<?> parameters(Map<String, Object> value) { return (List<?>) value.get("parameters"); }

    private static void result(String declaration, Map<String, Object> value, String semantic, String tag, String raw) {
        if (value.containsKey("resultSemanticType") && !semantic.equals(value.get("resultSemanticType"))) {
            throw invalid(declaration, "结果语义冲突");
        }
        value.put("resultSemanticType", semantic);
        value.put("resultSemanticEvidence", source(tag, raw));
    }

    private static Map<String, Object> source(String tag, String raw) {
        Map<String, Object> source = new LinkedHashMap<String, Object>();
        source.put("source", "JAVADOC");
        source.put("tag", tag);
        source.put("value", raw);
        return source;
    }

    private static IllegalArgumentException invalid(String declaration, String reason) {
        return new IllegalArgumentException("非法 Pipeline construction: " + declaration + "，原因: " + reason);
    }
}
