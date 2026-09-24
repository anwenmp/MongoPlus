package com.mongoplus.indexer.scanner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 显式文档归约标签的封闭契约；不从名称、返回类型或旧 composition 推断缺失信息。 */
final class DocumentReductionContract {
    static final String CAPABILITY = "DOCUMENT_REDUCTION_V1";
    private static final String TAG = "mongoReduction";
    private static final String DOCUMENT = "STAGE_BODY_DOCUMENT";
    private static final Map<String, String> ATTRIBUTES = attributes();

    private DocumentReductionContract() { }

    static void apply(String declaration, List<String> tags, Map<String, Object> method) {
        if (tags.size() != 1) { throw invalid(declaration, "只能声明一次 @mongoReduction"); }
        String raw = tags.get(0);
        String[] tokens = raw.split("\\s+");
        if (tokens.length < 3 || !"->".equals(tokens[1])) {
            throw invalid(declaration, "语法必须为 <参数名> -> <结果语义> <属性=值>...");
        }
        Map<?, ?> input = null;
        for (Object item : (List<?>) method.get("parameters")) {
            Map<?, ?> parameter = (Map<?, ?>) item;
            if (tokens[0].equals(parameter.get("name"))) { input = parameter; break; }
        }
        if (input == null) { throw invalid(declaration, "参数不存在: " + tokens[0]); }
        if (!input.containsKey("semanticEvidence")) {
            throw invalid(declaration, "归约输入缺少显式 @mongoParam: " + tokens[0]);
        }
        if (!"ELEMENT".equals(input.get("semanticScope"))) {
            throw invalid(declaration, "归约输入作用域必须为 ELEMENT: " + tokens[0]);
        }
        if (!DOCUMENT.equals(input.get("semanticType"))) {
            throw invalid(declaration, "DOCUMENT_MERGE 输入语义必须为 " + DOCUMENT);
        }
        if (!DOCUMENT.equals(tokens[2])) {
            throw invalid(declaration, "DOCUMENT_MERGE 输出语义必须为 " + DOCUMENT + "，实际为 " + tokens[2]);
        }
        Map<String, String> declared = new LinkedHashMap<String, String>();
        for (int i = 3; i < tokens.length; i++) {
            String token = tokens[i];
            int equals = token.indexOf('=');
            if (equals <= 0 || equals == token.length() - 1 || token.indexOf('=', equals + 1) >= 0) {
                throw invalid(declaration, "属性必须为 key=value: " + token);
            }
            String key = token.substring(0, equals);
            String value = token.substring(equals + 1);
            if (!ATTRIBUTES.containsKey(key)) { throw invalid(declaration, "未知属性: " + key); }
            if (declared.putIfAbsent(key, value) != null) { throw invalid(declaration, "重复属性: " + key); }
            if (!ATTRIBUTES.get(key).equals(value)) {
                throw invalid(declaration, "不支持的属性值: " + token + "，当前仅支持 " + ATTRIBUTES.get(key));
            }
        }
        for (String key : ATTRIBUTES.keySet()) {
            if (!declared.containsKey(key)) { throw invalid(declaration, "缺少属性: " + key); }
        }
        if (method.containsKey("resultSemanticType") && !tokens[2].equals(method.get("resultSemanticType"))) {
            throw invalid(declaration, "与 @mongoComposition 结果冲突");
        }
        Map<String, Object> contract = new LinkedHashMap<String, Object>();
        contract.put("inputParameter", tokens[0]);
        contract.put("inputScope", input.get("semanticScope"));
        contract.put("inputSemanticType", input.get("semanticType"));
        // 固定契约字段顺序；原始标签仍在 evidence 中保留其声明顺序。
        for (String key : ATTRIBUTES.keySet()) { contract.put(key, declared.get(key)); }
        Map<String, Object> evidence = new LinkedHashMap<String, Object>();
        evidence.put("source", "JAVADOC");
        evidence.put("tag", TAG);
        evidence.put("value", raw);
        method.put("resultSemanticType", tokens[2]);
        method.put("resultSemanticEvidence", evidence);
        method.put("reductionContract", contract);
        method.put("reductionEvidence", evidence);
    }

    private static Map<String, String> attributes() {
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put("operation", "DOCUMENT_MERGE");
        values.put("order", "INPUT");
        values.put("duplicateKeys", "LAST_WINS");
        values.put("depth", "SHALLOW");
        values.put("empty", "EMPTY_DOCUMENT");
        return java.util.Collections.unmodifiableMap(values);
    }

    private static IllegalArgumentException invalid(String declaration, String reason) {
        return new IllegalArgumentException("非法 @" + TAG + ": " + declaration + "，原因: " + reason);
    }
}
