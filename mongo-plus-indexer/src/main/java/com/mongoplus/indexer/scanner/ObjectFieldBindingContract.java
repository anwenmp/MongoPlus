package com.mongoplus.indexer.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 对象字段到方法参数的显式契约；编码和范围仅在参数语义要求时声明。 */
final class ObjectFieldBindingContract {
    static final String CAPABILITY = "STAGE_OBJECT_FIELD_BINDING_V1";
    static final String TAG = "mongoObjectField";
    static final String SOURCE_TAG = "mongoObjectFieldSource";
    private static final List<String> ATTRIBUTES = Arrays.asList("field", "encoding", "minimum", "maximum");

    private ObjectFieldBindingContract() { }

    @SuppressWarnings("unchecked")
    static boolean apply(String declaration, List<String> tags, List<String> sources, Map<String, Object> method) {
        if (tags.isEmpty() && sources.isEmpty()) { return false; }
        if (((List<?>) method.get("mongoStages")).size() != 1 || !((List<?>) method.get("mongoExpressions")).isEmpty()) {
            throw invalid(declaration, "对象字段绑定需要唯一 Stage 映射");
        }
        Map<String, Map<String, Object>> bindings = new LinkedHashMap<String, Map<String, Object>>();
        Set<String> fields = new HashSet<String>();
        for (String raw : tags) {
            String[] tokens = raw.split("\\s+");
            Map<String, Object> parameter = null;
            for (Object item : (List<?>) method.get("parameters")) {
                Map<String, Object> candidate = (Map<String, Object>) item;
                if (tokens[0].equals(candidate.get("name"))) { parameter = candidate; break; }
            }
            if (parameter == null) { throw invalid(declaration, "参数不存在: " + tokens[0]); }
            if (!parameter.containsKey("semanticEvidence")) {
                throw invalid(declaration, "缺少显式 @mongoParam: " + tokens[0]);
            }
            Map<String, String> attributes = attributes(declaration, tokens);
            String field = attributes.get("field");
            if (!field.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                throw invalid(declaration, "field 必须为单层对象字段标识符: " + field);
            }
            Map<String, Object> binding = new LinkedHashMap<String, Object>();
            binding.put("field", field);
            String semantic = (String) parameter.get("semanticType");
            if ("INTEGER_VALUE".equals(semantic)) {
                if (!"VALUE".equals(parameter.get("semanticScope"))) {
                    throw invalid(declaration, "整数绑定仍要求 INTEGER_VALUE VALUE");
                }
                integerEncoding(declaration, attributes, binding);
            } else if (attributes.size() != 1) {
                throw invalid(declaration, "INT32_EXACT 需要 INTEGER_VALUE VALUE 及匹配的 concept；其他语义不声明整数编码/范围");
            }
            boolean constructedEntries = "ELEMENT".equals(parameter.get("semanticScope"))
                    && parameter.containsKey("entryConstruction") && parameter.containsKey("typedContainerConstruction");
            if (!("VALUE".equals(parameter.get("semanticScope")) || constructedEntries)
                    || !StageParameterConcepts.acceptsConcept(semantic, (String) parameter.get("conceptRef"))) {
                throw invalid(declaration, "对象字段绑定需要 VALUE 或完整 typed entry construction，及匹配的 semantic concept");
            }
            binding.put("sourceEvidence", new ArrayList<Map<String, Object>>());
            if (bindings.putIfAbsent(tokens[0], binding) != null) { throw invalid(declaration, "重复绑定: " + tokens[0]); }
            if (!fields.add(field)) { throw invalid(declaration, "重复对象字段: " + field); }
            parameter.put("objectFieldBinding", binding);
        }
        Set<String> seenSources = new HashSet<String>();
        for (String raw : sources) {
            String[] tokens = raw.split("\\s+", 4);
            if (tokens.length != 4 || !(tokens[1].startsWith("path=") || tokens[1].startsWith("artifact="))
                    || !tokens[2].startsWith("symbols=") || !tokens[3].startsWith("mechanism=")) {
                throw invalid(declaration, "来源语法必须为 <参数名> path|artifact=<位置> symbols=<符号> mechanism=<说明>");
            }
            Map<String, Object> binding = bindings.get(tokens[0]);
            if (binding == null) { throw invalid(declaration, "来源没有对应绑定: " + tokens[0]); }
            if (!seenSources.add(raw)) { throw invalid(declaration, "重复来源: " + raw); }
            Map<String, Object> evidence = new LinkedHashMap<String, Object>();
            for (int i = 1; i < tokens.length; i++) {
                int equals = tokens[i].indexOf('=');
                String value = tokens[i].substring(equals + 1).trim();
                if (value.isEmpty()) { throw invalid(declaration, "来源语法不允许空属性"); }
                evidence.put(tokens[i].substring(0, equals), value);
            }
            ((List<Map<String, Object>>) binding.get("sourceEvidence")).add(evidence);
        }
        for (Map.Entry<String, Map<String, Object>> entry : bindings.entrySet()) {
            if (((List<?>) entry.getValue().get("sourceEvidence")).isEmpty()) {
                throw invalid(declaration, "缺少来源证据: " + entry.getKey());
            }
        }
        return !bindings.isEmpty();
    }

    private static Map<String, String> attributes(String declaration, String[] tokens) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (int i = 1; i < tokens.length; i++) {
            int equals = tokens[i].indexOf('=');
            if (equals <= 0 || equals == tokens[i].length() - 1 || tokens[i].indexOf('=', equals + 1) >= 0) {
                throw invalid(declaration, "属性必须为 key=value: " + tokens[i]);
            }
            String key = tokens[i].substring(0, equals);
            if (!ATTRIBUTES.contains(key)) { throw invalid(declaration, "未知属性: " + key); }
            if (result.putIfAbsent(key, tokens[i].substring(equals + 1)) != null) {
                throw invalid(declaration, "重复属性: " + key);
            }
        }
        if (!result.containsKey("field")) { throw invalid(declaration, "缺少属性: field"); }
        return result;
    }

    /** 对象字段和 Stage body 共用同一精确 Int32 编码/范围校验，既有输出结构不变。 */
    static void integerEncoding(String declaration, Map<String, String> attributes, Map<String, Object> binding) {
        for (String key : Arrays.asList("encoding", "minimum", "maximum")) {
            if (!attributes.containsKey(key)) { throw invalid(declaration, "缺少属性: " + key); }
        }
        if (!"INT32_EXACT".equals(attributes.get("encoding"))) {
            throw invalid(declaration, "不支持的 encoding: " + attributes.get("encoding"));
        }
        long minimum = bound(declaration, attributes.get("minimum"));
        long maximum = bound(declaration, attributes.get("maximum"));
        if (minimum > maximum) { throw invalid(declaration, "Int32 范围 minimum 不能大于 maximum"); }
        binding.put("encoding", attributes.get("encoding"));
        binding.put("minimum", minimum);
        binding.put("maximum", maximum);
    }

    private static long bound(String declaration, String value) {
        try {
            long bound = Long.parseLong(value);
            if (bound >= Integer.MIN_VALUE && bound <= Integer.MAX_VALUE) { return bound; }
        } catch (NumberFormatException ignored) {
            // 非整数或溢出统一拒绝，不能使用截断转换补救 metadata。
        }
        throw invalid(declaration, "必须为精确 Int32 范围整数: " + value);
    }

    private static IllegalArgumentException invalid(String declaration, String reason) {
        return new IllegalArgumentException("非法 @" + TAG + ": " + declaration + "，原因: " + reason);
    }
}
