package com.mongoplus.indexer.scanner;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** 文档条目、输入角色及模式规则的显式证据；不依据操作符、方法名或参数名推断。 */
final class DocumentShapeContract {
    static final String CAPABILITY = "DOCUMENT_SHAPE_V1";
    private static final String ENTRY = "mongoDocumentEntry";
    private static final String INPUT = "mongoDocumentInput";
    private static final String POLICY = "mongoDocumentPolicy";
    private static final String SOURCE = "mongoDocumentSource";
    private static final List<String> DOCUMENTS = Arrays.asList("STAGE_BODY_DOCUMENT", "SORT_SPECIFICATION");

    private DocumentShapeContract() { }

    static boolean hasTags(Map<String, List<String>> tags) {
        return Arrays.asList(ENTRY, INPUT, POLICY, SOURCE).stream().anyMatch(tags::containsKey);
    }

    @SuppressWarnings("unchecked")
    static boolean apply(String declaration, Map<String, List<String>> tags, Map<String, Object> method,
                         Function<String, Tree> parameterTree, Function<String, String> resolveName) {
        Map<String, List<Object>> sources = sources(declaration, tags);
        boolean applied = false;
        for (String tag : Arrays.asList(ENTRY, INPUT, POLICY)) {
            List<String> values = tags.getOrDefault(tag, Collections.emptyList());
            if (values.isEmpty()) {
                if (sources.containsKey(tag)) { throw invalid(declaration, "来源没有对应声明: " + tag); }
                continue;
            }
            if (values.size() != 1 || !sources.containsKey(tag)) {
                throw invalid(declaration, "每种声明只能一次且必须有独立来源: " + tag);
            }
            String raw = values.get(0);
            Map<String, String> attrs = attributes(declaration, raw);
            Map<String, Object> contract;
            if (ENTRY.equals(tag)) {
                requireKeys(declaration, attrs, "key", "value", "result", "target", "order", "duplicatePosition");
                if (!DOCUMENTS.contains(attrs.get("result")) || !"INPUT".equals(attrs.get("order"))
                        || !Arrays.asList("FIRST", "LAST").contains(attrs.get("duplicatePosition"))) {
                    throw invalid(declaration, "文档结果、输入顺序或重复键位置未闭合");
                }
                String key = attrs.get("key");
                contract = object("operation", "DOCUMENT_ENTRIES", "resultSemanticType", attrs.get("result"),
                        "order", "INPUT", "duplicateKeys", "LAST_WINS", "duplicatePosition", attrs.get("duplicatePosition"));
                if (key.startsWith("literal:")) {
                    if (key.length() == "literal:".length() || !parameters(method).isEmpty()) {
                        throw invalid(declaration, "固定键需要无参数声明且键非空");
                    }
                    contract.put("key", object("input", "FIXED", "value", key.substring("literal:".length()), "encoding", "UNCHANGED"));
                } else {
                    Map<String, Object> parameter = parameter(declaration, method, key);
                    if (!Arrays.asList("FIELD_NAME", "OUTPUT_FIELD_NAME").contains(parameter.get("semanticType"))) {
                        throw invalid(declaration, "key 需要独立字段名语义");
                    }
                    Tree type = parameterTree.apply(key);
                    String scope = (String) parameter.get("semanticScope");
                    String encoding;
                    String javaType;
                    if ("ELEMENT".equals(scope)) {
                        // 复用已审计的一层 AST 容器，保留 List/array/varargs 的真实调用方式。
                        if (type instanceof ArrayTypeTree) { type = ((ArrayTypeTree) type).getType(); }
                        else if (type instanceof ParameterizedTypeTree
                                && "java.util.List".equals(resolveName.apply(((ParameterizedTypeTree) type).getType().toString()))) {
                            type = ((ParameterizedTypeTree) type).getTypeArguments().get(0);
                        } else { throw invalid(declaration, "字段元素需要真实单层 List 或数组"); }
                        contract.put("typedContainer", object("operation", "COLLECT_ELEMENTS", "order", "INPUT",
                                "invocation", Boolean.TRUE.equals(parameter.get("varargs")) ? "VARARGS" : "SINGLE",
                                "containerType", parameterTree.apply(key) instanceof ArrayTypeTree ? "ARRAY" : "LIST",
                                "elementSemanticType", parameter.get("semanticType"), "conceptRef", parameter.get("conceptRef")));
                    }
                    if (type instanceof ParameterizedTypeTree
                            && "com.mongoplus.support.SFunction".equals(resolveName.apply(((ParameterizedTypeTree) type).getType().toString()))) {
                        encoding = "GET_FIELD_NAME_LINE"; javaType = "com.mongoplus.support.SFunction";
                    } else if ("java.lang.String".equals(resolveName.apply(type.toString()))) {
                        encoding = "UNCHANGED"; javaType = "java.lang.String";
                    } else { throw invalid(declaration, "字段键表示必须为真实 String 或 SFunction"); }
                    contract.put("key", object("inputParameter", key, "semanticType", parameter.get("semanticType"),
                            "scope", scope, "encoding", encoding, "javaType", javaType));
                    if (contract.containsKey("typedContainer")) {
                        ((Map<String, Object>) contract.get("typedContainer")).put("elementJavaType", javaType);
                    }
                }
                String value = attrs.get("value");
                if (value.startsWith("int32:")) {
                    int constant;
                    try { constant = Integer.parseInt(value.substring("int32:".length())); }
                    catch (NumberFormatException exception) { throw invalid(declaration, "固定值必须为精确 Int32"); }
                    contract.put("value", object("input", "FIXED", "encoding", "INT32_EXACT", "value", constant));
                } else {
                    Map<String, Object> parameter = parameter(declaration, method, value);
                    if (!"PIPELINE_EXPRESSION".equals(parameter.get("semanticType"))
                            || !"VALUE".equals(parameter.get("semanticScope"))) {
                        throw invalid(declaration, "条目值需要独立 expression VALUE evidence");
                    }
                    contract.put("value", object("inputParameter", value, "semanticType", "PIPELINE_EXPRESSION",
                            "conceptRef", parameter.get("conceptRef"), "encoding", "RUNTIME_CODEC"));
                }
                if ("RESULT".equals(attrs.get("target"))) {
                    int consumed = (key.startsWith("literal:") ? 0 : 1) + (value.startsWith("int32:") ? 0 : 1);
                    if (parameters(method).size() != consumed || key.equals(value)) {
                        throw invalid(declaration, "文档工厂必须完整消费所有独立参数");
                    }
                    if (!((List<?>) method.get("mongoStages")).isEmpty()
                            || !"org.bson.conversions.Bson".equals(resolveName.apply((String) method.get("returnType")))) {
                        throw invalid(declaration, "RESULT 必须为 Bson 文档工厂，不能将 Stage receiver 当文档结果");
                    }
                    result(declaration, method, attrs.get("result"), tag, raw);
                    method.put("documentEntryConstruction", contract);
                } else if ("STAGE_BODY".equals(attrs.get("target")) && method.containsKey("pipelineEffect")) {
                    method.put("stageBodyConstruction", contract);
                } else { throw invalid(declaration, "target 需要 RESULT 或有独立 effect 的 STAGE_BODY"); }
            } else if (INPUT.equals(tag)) {
                requireKeys(declaration, attrs, "parameter", "semantic", "encoding");
                Map<String, Object> parameter = parameter(declaration, method, attrs.get("parameter"));
                String semantic = attrs.get("semantic");
                if (!semantic.equals(parameter.get("semanticType")) || !"VALUE".equals(parameter.get("semanticScope"))
                        || !"org.bson.conversions.Bson".equals(resolveName.apply(parameterTree.apply(attrs.get("parameter")).toString()))) {
                    throw invalid(declaration, "文档输入需要独立 Bson VALUE 语义");
                }
                String encoding = attrs.get("encoding");
                if ("WRAP_DECLARED_STAGE".equals(encoding)) {
                    if (!DOCUMENTS.contains(semantic) || !method.containsKey("pipelineEffect")) {
                        throw invalid(declaration, "包装 body 需要独立 Stage effect");
                    }
                } else if (!"UNCHANGED".equals(encoding) || !"PIPELINE_STAGE_DOCUMENT".equals(semantic)) {
                    throw invalid(declaration, "透传只能消费完整 Stage，不能自动把排序 body 包装为 Stage");
                }
                contract = object("inputParameter", attrs.get("parameter"), "semanticType", semantic, "encoding", encoding,
                        "bodyToStageConstruction", "UNCHANGED".equals(encoding) ? "NOT_ESTABLISHED" : "DECLARED_STAGE_WRAPPER");
                parameter.put("documentInputBinding", contract);
            } else {
                requireKeys(declaration, attrs, "input", "numeric", "boolean", "exceptionField", "mixed", "empty", "expressions");
                if (!"FLAT_DOCUMENT".equals(attrs.get("input")) || !"ZERO_NONZERO_FLAGS".equals(attrs.get("numeric"))
                        || !"FALSE_TRUE_FLAGS".equals(attrs.get("boolean")) || attrs.get("exceptionField").isEmpty()
                        || !"REJECT_NON_EXCEPTION_EXCLUSION".equals(attrs.get("mixed"))
                        || !"REJECT".equals(attrs.get("empty")) || !"INDEPENDENT_EVIDENCE".equals(attrs.get("expressions"))) {
                    throw invalid(declaration, "文档模式属性未闭合");
                }
                if (!method.containsKey("pipelineEffect") || parameters(method).size() != 1
                        || !parameters(method).get(0).containsKey("documentInputBinding")
                        || !"STAGE_BODY_DOCUMENT".equals(parameters(method).get(0).get("semanticType"))) {
                    throw invalid(declaration, "文档模式需要独立 Stage effect 和 body 输入绑定");
                }
                contract = new LinkedHashMap<>(attrs);
                contract.put("numericBooleanLiterals", "REQUIRE_OPAQUE_LITERAL_EXPRESSION");
                contract.put("exceptionScope", "FLAG_ONLY");
                contract.put("nestedDocumentMode", "NOT_ESTABLISHED");
                contract.put("computedMode", "INCLUSION_REQUIRES_EXPRESSION_EVIDENCE");
                contract.put("specialComputedExclusion", "NOT_ESTABLISHED");
                contract.put("constraintEnforcement", "BINDING_CONSUMER");
                contract.put("coreRuntimeValidationImplied", false);
                method.put("documentModePolicy", contract);
            }
            List<Object> evidence = new ArrayList<>(sources.get(tag));
            evidence.add(object("source", "JAVADOC", "tag", tag, "value", raw));
            contract.put("sourceEvidence", evidence);
            applied = true;
        }
        return applied;
    }

    static Map<String, Object> concept() {
        return object("id", CAPABILITY, "semanticType", "DOCUMENT_SHAPE", "name", "有序文档条目和显式模式规则",
                "constraintEnforcement", "BINDING_CONSUMER", "coreRuntimeValidationImplied", false,
                "duplicateInputKeys", "REJECT_BEFORE_REDUCTION", "inputOrder", "PRESERVE",
                "candidateSelection", "PRESERVE_ALL_COMPATIBLE_OVERLOADS", "methodOrderIsEvidence", false,
                "description", "固定 BSON 标志与表达式参数独立；规则只取当前声明，不能补齐缺失 Stage 包装或候选选择。"
                        + "flat document 的所有字段必须消费一次，路径碰撞及未知 nested 形态拒绝，不能覆盖或改写输入。"
                        + "数值标志的语义等价不证明原始 BSON numeric/bool 类型相同。"
                        + "typed-container 不把 String 自动变成 getter，不把普通集合变成 Projection entry。");
    }

    private static Map<String, List<Object>> sources(String declaration, Map<String, List<String>> tags) {
        Map<String, List<Object>> result = new LinkedHashMap<>();
        for (String raw : tags.getOrDefault(SOURCE, Collections.emptyList())) {
            String[] tokens = raw.split("\\s+", 4);
            if (tokens.length != 4 || !Arrays.asList(ENTRY, INPUT, POLICY).contains(tokens[0])
                    || !(tokens[1].startsWith("path=") || tokens[1].startsWith("reference="))
                    || !tokens[2].startsWith("symbols=") || !tokens[3].startsWith("mechanism=")) {
                throw invalid(declaration, "来源需要对应 tag、path/reference、symbols 和 mechanism");
            }
            Map<String, Object> source = object("source", "JAVADOC", "tag", SOURCE, "value", raw);
            for (int i = 1; i < tokens.length; i++) {
                int separator = tokens[i].indexOf('=');
                String value = tokens[i].substring(separator + 1).trim();
                if (value.isEmpty()) { throw invalid(declaration, "来源属性不能为空"); }
                if ("reference".equals(tokens[i].substring(0, separator)) && !URI.create(value).isAbsolute()) {
                    throw invalid(declaration, "reference 需要绝对 URI");
                }
                source.put(tokens[i].substring(0, separator), value);
            }
            List<Object> list = result.computeIfAbsent(tokens[0], key -> new ArrayList<>());
            if (list.contains(source)) { throw invalid(declaration, "重复来源"); }
            list.add(source);
        }
        return result;
    }

    private static void result(String declaration, Map<String, Object> method, String semantic, String tag, String raw) {
        if (method.containsKey("resultSemanticType") && !semantic.equals(method.get("resultSemanticType"))) {
            throw invalid(declaration, "与已有 composition/reduction 结果冲突");
        }
        // 已有 expression composition 的结果证据保留，新增工厂独立声明结果。
        method.put("resultSemanticType", semantic);
        method.putIfAbsent("resultSemanticEvidence", object("source", "JAVADOC", "tag", tag, "value", raw));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> parameters(Map<String, Object> method) {
        return (List<Map<String, Object>>) (List<?>) method.get("parameters");
    }

    private static Map<String, Object> parameter(String declaration, Map<String, Object> method, String name) {
        for (Map<String, Object> parameter : parameters(method)) {
            if (name.equals(parameter.get("name")) && parameter.containsKey("semanticEvidence")) { return parameter; }
        }
        throw invalid(declaration, "缺少当前参数的显式 mongoParam: " + name);
    }

    private static Map<String, String> attributes(String declaration, String raw) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String token : raw.split("\\s+")) {
            int separator = token.indexOf('=');
            if (separator <= 0 || separator == token.length() - 1 || token.indexOf('=', separator + 1) >= 0
                    || result.putIfAbsent(token.substring(0, separator), token.substring(separator + 1)) != null) {
                throw invalid(declaration, "属性格式错误或重复: " + token);
            }
        }
        return result;
    }

    private static void requireKeys(String declaration, Map<String, String> attrs, String... keys) {
        if (!attrs.keySet().equals(new java.util.HashSet<>(Arrays.asList(keys)))) {
            throw invalid(declaration, "属性缺失或未知，需要 " + Arrays.asList(keys));
        }
    }

    private static Map<String, Object> object(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) { result.put((String) pairs[i], pairs[i + 1]); }
        return result;
    }

    private static IllegalArgumentException invalid(String declaration, String reason) {
        return new IllegalArgumentException("非法 DOCUMENT_SHAPE_V1: " + declaration + "，原因: " + reason);
    }
}
