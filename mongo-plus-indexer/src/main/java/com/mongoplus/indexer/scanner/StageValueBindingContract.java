package com.mongoplus.indexer.scanner;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** 整个 Stage body 的显式值绑定；语义、范围和 singleton lifting 均不从 API 名称推断。 */
final class StageValueBindingContract {
    static final String CAPABILITY = "STAGE_VALUE_BINDING_V1";
    static final String TAG = "mongoStageValue";
    static final String SOURCE_TAG = "mongoStageValueSource";
    private static final String STRING_TYPE = "java.lang.String";

    private StageValueBindingContract() { }

    @SuppressWarnings("unchecked")
    static boolean apply(String declaration, Map<String, List<String>> tags, Map<String, Object> method,
                         Function<String, Tree> parameterType, Function<String, String> resolveType) {
        List<String> values = tags.getOrDefault(TAG, Collections.emptyList());
        List<String> sources = tags.getOrDefault(SOURCE_TAG, Collections.emptyList());
        if (values.isEmpty() && sources.isEmpty()) { return false; }
        if (values.size() != 1 || ((List<?>) method.get("parameters")).size() != 1
                || ((List<?>) method.get("mongoStages")).size() != 1
                || !((List<?>) method.get("mongoExpressions")).isEmpty()) {
            throw invalid(declaration, "整个 Stage body 需要唯一参数、唯一绑定和唯一 Stage");
        }
        String raw = values.get(0);
        String[] tokens = raw.split("\\s+");
        Map<String, Object> parameter = (Map<String, Object>) ((List<?>) method.get("parameters")).get(0);
        if (!tokens[0].equals(parameter.get("name")) || !parameter.containsKey("semanticEvidence")
                || parameter.containsKey("objectFieldBinding")) {
            throw invalid(declaration, "参数必须具有独立 @mongoParam，且不能同时绑定对象字段");
        }
        Map<String, String> attributes = attributes(declaration, tokens);
        List<Object> evidence = sources(declaration, tokens[0], sources);
        evidence.add(object("source", "JAVADOC", "tag", TAG, "value", raw));
        Map<String, Object> binding = object("capability", CAPABILITY, "input", "STAGE_BODY_VALUE",
                "encoding", attributes.get("encoding"), "sourceEvidence", evidence);
        if ("VALUE".equals(parameter.get("semanticScope"))) {
            if ("INTEGER_VALUE".equals(parameter.get("semanticType"))) {
                requireKeys(declaration, attributes, "encoding", "minimum", "maximum");
                // 与对象字段整数绑定复用同一个编码/范围校验，避免两套精度语义。
                try {
                    ObjectFieldBindingContract.integerEncoding(declaration, attributes, binding);
                } catch (IllegalArgumentException exception) {
                    throw invalid(declaration, "数值契约校验失败: " + exception.getMessage());
                }
                binding.put("numericConversion", "EXACT_INTEGER_NO_ROUNDING");
                binding.put("bsonType", "INT32");
                binding.put("inputBsonTypePreservation", "NOT_ESTABLISHED");
                binding.put("inputShapes", Arrays.asList("SCALAR"));
            } else {
                requireKeys(declaration, attributes, "encoding", "prefix", "minimumLength");
                if (!"PIPELINE_EXPRESSION".equals(parameter.get("semanticType"))
                        || !"UNCHANGED".equals(attributes.get("encoding"))
                        || !STRING_TYPE.equals(resolveType.apply(parameterType.apply(tokens[0]).toString()))) {
                    throw invalid(declaration, "V1 非整数标量只接受已有 PIPELINE_EXPRESSION 的真实 String 原值编码");
                }
                binding.put("javaValueType", STRING_TYPE);
                binding.put("inputShapes", Arrays.asList("STRING"));
                binding.put("stringConstraints", object("requiredPrefix", attributes.get("prefix"),
                        "minimumLength", size(declaration, attributes.get("minimumLength"))));
                binding.put("referenceValidation", "REQUIRED_BY_EXPRESSION_CONCEPT");
            }
        } else if ("ELEMENT".equals(parameter.get("semanticScope"))) {
            requireKeys(declaration, attributes, "encoding", "minimumSize", "duplicates", "singleton");
            if (!"FIELD_NAME".equals(parameter.get("semanticType"))
                    || !"SINGLETON_SCALAR_ELSE_ARRAY".equals(attributes.get("encoding"))
                    || !Arrays.asList("REJECT", "PRESERVE").contains(attributes.get("duplicates"))
                    || !Arrays.asList("VALUE_TO_ELEMENT", "FORBID").contains(attributes.get("singleton"))) {
                throw invalid(declaration, "不支持的 ELEMENT 语义、编码、重复或 singleton 规则");
            }
            Tree type = parameterType.apply(tokens[0]);
            // 复用已有的一层 AST 容器契约；ELEMENT 语义仍只来自当前参数标签。
            String elementType = PipelineConstructionContract.elementType(type, resolveType);
            if (!STRING_TYPE.equals(elementType)) { throw invalid(declaration, "值容器元素必须为真实 java.lang.String"); }
            String containerType = type instanceof ArrayTypeTree ? "ARRAY" : "LIST";
            String javaType = type instanceof ParameterizedTypeTree ? "java.util.List" : STRING_TYPE + "[]";
            boolean lifting = "VALUE_TO_ELEMENT".equals(attributes.get("singleton"));
            Map<String, Object> container = object("operation", "COLLECT_ELEMENTS", "containerType", containerType,
                    "containerJavaType", javaType, "elementJavaType", elementType,
                    "elementSemanticType", parameter.get("semanticType"), "conceptRef", parameter.get("conceptRef"),
                    "order", "INPUT", "invocation", Boolean.TRUE.equals(parameter.get("varargs")) ? "VARARGS" : "SINGLE",
                    "inputShapes", lifting ? Arrays.asList("ARRAY", "VALUE") : Arrays.asList("ARRAY"),
                    "sourceEvidence", evidence);
            if (lifting) {
                container.put("singletonLifting", object("operation", "VALUE_TO_ELEMENT", "count", "ONE",
                        "semanticType", parameter.get("semanticType"), "conceptRef", parameter.get("conceptRef"),
                        "elementJavaType", elementType, "elementEncoding", "UNCHANGED"));
            }
            parameter.put("elementContainerBinding", container);
            binding.put("inputShapes", container.get("inputShapes"));
            binding.put("minimumSize", size(declaration, attributes.get("minimumSize")));
            binding.put("duplicates", attributes.get("duplicates"));
            binding.put("encodedDuplicateHandling", "PRESERVE");
            binding.put("elementEncoding", "UNCHANGED");
            binding.put("singletonOutput", "SCALAR");
            binding.put("otherCardinalityOutput", "ARRAY");
            binding.put("fieldPathValidation", "SERVER_VALIDATION_REQUIRED");
        } else { throw invalid(declaration, "未知参数作用范围"); }
        parameter.put("stageValueBinding", binding);
        return true;
    }

    static Map<String, Object> concept() {
        return object("id", CAPABILITY, "semanticType", "STAGE_VALUE_BINDING", "name", "Stage body 标量与元素容器绑定",
                "constraintEnforcement", "BINDING_CONSUMER", "coreRuntimeValidationImplied", false,
                "numericComparison", "EXACT_INTEGER_WITHOUT_FLOATING_INTERMEDIATE",
                "candidateSelection", object("orderIndependent", true, "javaParameterTypes", "PER_OVERLOAD_DECLARATION",
                        "multipleCompatibleCandidates", "REQUIRE_EXPLICIT_TYPE_OR_PROVEN_EQUIVALENCE",
                        "methodOrderIsEvidence", false),
                "singletonLifting", "EXPLICIT_PER_PARAMETER_ONLY", "invocationRequiresIndependentStageEffect", true,
                "description", "消费逐参数 Stage body 编码、合法绑定范围和显式容器提升；保留所有候选，"
                        + "不从 Java 类型或方法名产生语义，不保证任意输入 BSON 数值类型等价，也不替代字段路径的服务端校验。");
    }

    private static Map<String, String> attributes(String declaration, String[] tokens) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 1; i < tokens.length; i++) {
            int separator = tokens[i].indexOf('=');
            if (separator <= 0 || separator == tokens[i].length() - 1 || tokens[i].indexOf('=', separator + 1) >= 0) {
                throw invalid(declaration, "非法属性: " + tokens[i]);
            }
            String key = tokens[i].substring(0, separator);
            if (result.putIfAbsent(key, tokens[i].substring(separator + 1)) != null) {
                throw invalid(declaration, "重复属性: " + key);
            }
        }
        return result;
    }

    private static void requireKeys(String declaration, Map<String, String> attributes, String... keys) {
        if (!attributes.keySet().equals(new HashSet<>(Arrays.asList(keys)))) {
            throw invalid(declaration, "属性缺失或未知，需要: " + Arrays.asList(keys));
        }
    }

    private static int size(String declaration, String raw) {
        try {
            int value = Integer.parseInt(raw);
            if (value >= 0) { return value; }
        } catch (NumberFormatException ignored) {
            // 大小契约不截断小数或溢出值。
        }
        throw invalid(declaration, "minimumSize 必须为非负 Int32 整数");
    }

    private static List<Object> sources(String declaration, String parameter, List<String> sources) {
        List<Object> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String raw : sources) {
            String[] tokens = raw.split("\\s+", 4);
            if (tokens.length != 4 || !parameter.equals(tokens[0]) || !seen.add(raw)
                    || !(tokens[1].startsWith("path=") || tokens[1].startsWith("artifact=") || tokens[1].startsWith("reference="))
                    || !tokens[2].startsWith("symbols=") || !tokens[3].startsWith("mechanism=")) {
                throw invalid(declaration, "来源需要当前参数、path/artifact/reference、symbols 和 mechanism，且不能重复");
            }
            Map<String, Object> source = object("source", "JAVADOC", "tag", SOURCE_TAG, "value", raw);
            for (int i = 1; i < tokens.length; i++) {
                int separator = tokens[i].indexOf('=');
                String value = tokens[i].substring(separator + 1).trim();
                if (value.isEmpty()) { throw invalid(declaration, "来源属性不能为空"); }
                String key = tokens[i].substring(0, separator);
                if ("reference".equals(key) && !URI.create(value).isAbsolute()) { throw invalid(declaration, "reference 需要绝对 URI"); }
                source.put(key, value);
            }
            result.add(source);
        }
        if (result.isEmpty()) { throw invalid(declaration, "缺少来源证据"); }
        return result;
    }

    private static Map<String, Object> object(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) { result.put((String) pairs[i], pairs[i + 1]); }
        return result;
    }

    private static IllegalArgumentException invalid(String declaration, String reason) {
        return new IllegalArgumentException("非法 @" + TAG + ": " + declaration + "，原因: " + reason);
    }
}
