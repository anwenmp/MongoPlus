package com.mongoplus.indexer.scanner;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** 从逐声明契约连接变量条目、声明身份和词法环境；不按 API、Stage 或变量名称分支。 */
final class VariableBindingScopeContract {
    static final String CAPABILITY = "VARIABLE_BINDING_SCOPE_V1";
    static final String SCOPE_TAG = "mongoVariableScope";
    static final String SOURCE_TAG = "mongoVariableScopeSource";
    static final String ENVIRONMENT_TAG = "mongoVariableEnvironment";
    private static final String EXPRESSION_CONCEPT = "PIPELINE_EXPRESSION_FIELD_REFERENCE";
    private static final String VARIABLES_REFERENCE =
            "https://www.mongodb.com/docs/manual/reference/aggregation-variables/";

    private VariableBindingScopeContract() { }

    static boolean hasTags(Map<String, List<String>> tags) {
        return tags.containsKey(SCOPE_TAG) || tags.containsKey(SOURCE_TAG) || tags.containsKey(ENVIRONMENT_TAG);
    }

    /** scope 只能消费已证明的 entry/container 和 body 字段；缺少任何依赖都不补全。 */
    static boolean apply(String declaration, Map<String, List<String>> tags, Map<String, Object> method) {
        if (!hasTags(tags)) { return false; }
        List<String> scopes = tags.getOrDefault(SCOPE_TAG, Collections.emptyList());
        List<String> sources = tags.getOrDefault(SOURCE_TAG, Collections.emptyList());
        List<String> environments = tags.getOrDefault(ENVIRONMENT_TAG, Collections.emptyList());
        if (scopes.size() > 1 || scopes.isEmpty() && environments.isEmpty() || sources.isEmpty()) {
            throw invalid(declaration, "必须有 scope/environment 声明及至少一条语义来源；scope 不可重复");
        }
        List<Object> audit = audit(declaration, sources);
        for (String raw : environments) {
            String[] parts = raw.split("\\s+", 2);
            if (parts.length != 2) { throw invalid(declaration, "environment 必须声明 body 参数及固定属性"); }
            Map<String, String> attributes = attributes(declaration, parts[1], false, "source", "inheritance", "exit");
            if (!"ENCLOSING".equals(attributes.get("source"))
                    || !"LEXICAL".equals(attributes.get("inheritance"))
                    || !"RESTORE_PARENT".equals(attributes.get("exit"))) {
                throw invalid(declaration, "environment 必须继承 enclosing lexical 环境并在退出时恢复父环境");
            }
            Map<String, Object> body = parameter(declaration, method, parts[0], "PIPELINE", "VALUE");
            if (body.containsKey("variableEnvironment")) { throw invalid(declaration, "同一 body 的 environment 重复"); }
            List<Object> evidence = new ArrayList<>(audit);
            evidence.add(source(ENVIRONMENT_TAG, raw));
            body.put("variableEnvironment", object("operation", "INHERIT_ENVIRONMENT",
                    "source", "ENCLOSING_ENVIRONMENT", "inheritance", "LEXICAL_PARENT_CHAIN",
                    "body", object("apiRef", declaration, "parameter", body.get("name"), "field", field(declaration, body)),
                    "parentEdgeEvidence", "REQUIRED", "scopeExit", "RESTORE_PARENT", "exportDeclarations", false,
                    "environmentEvidenceRef", CAPABILITY + ".environmentEvidence", "sourceEvidence", evidence));
        }
        if (scopes.isEmpty()) { return true; }
        String raw = scopes.get(0);
        Map<String, String> attributes = attributes(declaration, raw, false,
                "declarations", "body", "parent", "initializer", "inheritance", "shadowing", "exit");
        if (!"ENCLOSING".equals(attributes.get("parent")) || !"PARENT".equals(attributes.get("initializer"))
                || !"LEXICAL".equals(attributes.get("inheritance"))
                || !"NEAREST".equals(attributes.get("shadowing"))
                || !"RESTORE_PARENT".equals(attributes.get("exit"))) {
            throw invalid(declaration, "V1 需要 enclosing parent、parent initializer、lexical inheritance、"
                    + "nearest shadowing 和 restore-parent exit");
        }
        Map<String, Object> definitions = parameter(declaration, method, attributes.get("declarations"),
                "VARIABLE_DEFINITION", "ELEMENT");
        Map<String, Object> body = parameter(declaration, method, attributes.get("body"), "PIPELINE", "VALUE");
        if (body.containsKey("variableEnvironment")) {
            throw invalid(declaration, "同一 body 不能同时声明新 scope 及直接继承环境");
        }
        Map<?, ?> entry = contract(declaration, definitions, "entryConstruction");
        Map<?, ?> container = contract(declaration, definitions, "typedContainerConstruction");
        if (!"CONSTRUCT_ENTRY".equals(entry.get("operation"))
                || !"VARIABLE_DEFINITION".equals(entry.get("resultSemanticType"))
                || !"COLLECT_ENTRIES".equals(container.get("operation"))
                || !"ORDERED_ENTRIES".equals(container.get("entryIteration"))
                || !"INPUT".equals(container.get("order"))
                || !Boolean.TRUE.equals(container.get("inputOrderPreserved"))) {
            throw invalid(declaration, "声明必须消费已证明的有序变量条目");
        }
        Map<?, ?> constructor = contract(declaration, entry, "constructor");
        Map<?, ?> key = role(declaration, constructor, "ENTRY_KEY");
        Map<?, ?> initializer = role(declaration, constructor, "ENTRY_VALUE");
        if (!"VARIABLE_NAME".equals(key.get("semanticType"))
                || !StageParameterConcepts.conceptId("VARIABLE_NAME").equals(key.get("conceptRef"))) {
            throw invalid(declaration, "declarationName 必须来自 ENTRY_KEY 的 VARIABLE_NAME evidence");
        }
        audit.add(source(SCOPE_TAG, raw));
        String scopeRef = declaration + ".variableScope";
        Map<String, Object> name = object("source", "ENTRY_KEY", "semanticType", "VARIABLE_NAME",
                "conceptRef", key.get("conceptRef"), "encoding", "UNCHANGED", "comparison", "EXACT");
        Map<String, Object> identity = declarationIdentity();
        Map<String, Object> declarationRule = object("semanticType", "VARIABLE_DEFINITION",
                "declarationName", name, "declarationIdentity", identity,
                "entryConstructionRef", container.get("elementConstructionRef"),
                "duplicates", "REJECT_DUPLICATE_DECLARATION_NAME");
        Map<String, Object> owner = object("apiRef", declaration, "parameter", definitions.get("name"),
                "field", field(declaration, definitions), "instanceSource", "INPUT_OWNER_NODE_PATH");
        Map<String, Object> scope = object("capability", CAPABILITY, "conceptRef", CAPABILITY,
                "operation", "DECLARE_SCOPE", "declarationOwner", owner, "declaration", declarationRule,
                "scopeIdentity", object("kind", "STRUCTURAL_TUPLE", "components",
                        Arrays.asList("apiRef", "ownerNodePath", "declarationParameter")),
                "body", object("parameter", body.get("name"), "field", field(declaration, body),
                        "environment", "DECLARATION_SCOPE"),
                "parentScope", object("source", "ENCLOSING_ENVIRONMENT", "requiredEvidence", "EXPLICIT_PARENT_EDGE"),
                "initializerEnvironment", object("source", "PARENT_SCOPE", "input", "ENTRY_VALUE",
                        "conceptRef", initializer.get("conceptRef"), "currentDeclarationsVisible", false,
                        "allEntriesUseSameParent", true),
                "inheritance", object("rule", "LEXICAL_PARENT_CHAIN", "parentDeclarationsVisible", true,
                        "edgeEvidence", "REQUIRED_FOR_EACH_NESTED_ENVIRONMENT"),
                "shadowing", object("rule", "NEAREST_VISIBLE_DECLARATION", "comparison", "EXACT"),
                "scopeExit", object("operation", "RESTORE_PARENT", "exportDeclarations", false,
                        "outerVisibility", "PARENT_UNCHANGED", "siblingVisibility", "PARENT_UNCHANGED"),
                "environmentEvidenceRef", CAPABILITY + ".environmentEvidence", "sourceEvidence", audit);
        method.put("variableScope", scope);
        definitions.put("variableDeclaration", object("scopeRef", scopeRef, "declarationName", name,
                "declarationIdentity", identity, "entryConstructionRef", container.get("elementConstructionRef")));
        body.put("variableEnvironment", object("source", "DECLARATION_SCOPE", "scopeRef", scopeRef));
        return true;
    }

    private static List<Object> audit(String declaration, List<String> sources) {
        List<Object> result = new ArrayList<>();
        for (String source : new TreeSet<>(sources)) {
            Map<String, String> values = attributes(declaration, source, true, "reference", "symbols", "mechanism");
            if (!URI.create(values.get("reference")).isAbsolute()) {
                throw invalid(declaration, "语义来源 reference 必须为绝对 URI");
            }
            Map<String, Object> evidence = object("source", "EXPLICIT_SCOPE_CONTRACT");
            evidence.putAll(values);
            evidence.put("declarationEvidence", source(SOURCE_TAG, source));
            result.add(evidence);
        }
        return result;
    }

    /** 原有 String/BSON 编码保持不变；解析结果不能替代引用使用点的绑定校验。 */
    static void describeReference(Map<String, Object> reference, boolean bindingAvailable) {
        reference.put("semanticType", "VARIABLE_REFERENCE");
        reference.put("nameExtraction", object("operation", "REMOVE_PREFIX_SPLIT_FIRST", "delimiter", ".",
                "referenceName", "AFTER_PREFIX_BEFORE_FIRST_DELIMITER", "accessPath", "AFTER_FIRST_DELIMITER",
                "absentAccessPath", null, "trim", false, "emptyNameAllowed", false, "emptyAccessPathAllowed", false,
                "invalidResult", "INVALID_VARIABLE_REFERENCE",
                "sourceEvidence", object("source", "REFERENCE_REPRESENTATION_CONTRACT", "reference", VARIABLES_REFERENCE)));
        if (bindingAvailable) {
            reference.put("bindingContractRef", CAPABILITY);
            reference.put("bindingValidation", "REQUIRED_AT_REFERENCE_USE");
        }
    }

    /** 身份取自输入结构，不能使用变量名称、随机数、对象地址或生成时间充当声明身份。 */
    private static Map<String, Object> declarationIdentity() {
        return object("kind", "STRUCTURAL_TUPLE", "components",
                Arrays.asList("apiRef", "ownerNodePath", "declarationParameter", "entryOrdinal"),
                "ownerNodePathEncoding", "RFC6901_JSON_POINTER", "entryOrdinalSource", "ORDERED_ENTRIES_ZERO_BASED",
                "comparison", "STRUCTURAL_EQUALITY", "stableForSameInput", true);
    }

    static Map<String, Object> concept() {
        return object("id", CAPABILITY, "name", "变量声明身份、词法作用域与引用绑定",
                "semanticType", "VARIABLE_BINDING_SCOPE", "declarationSemanticType", "VARIABLE_DEFINITION",
                "declarationNameSemanticType", "VARIABLE_NAME", "referenceSemanticType", "VARIABLE_REFERENCE",
                "referenceRepresentationRef", EXPRESSION_CONCEPT + ".variableReference",
                "declarationIdentity", declarationIdentity(),
                "environmentEvidence", object("scopeGraph", "EXPLICIT_OWNER_BODY_PARENT_EDGES",
                        "declarations", "EXPLICIT_COMPLETE_DECLARATION_SET", "externalVariables", "EXPLICIT_ONLY",
                        "systemVariables", "EXPLICIT_ONLY", "implicitSystemCatalog", false,
                        "catalogEntryFields", Arrays.asList("declarationName", "declarationIdentity", "sourceEvidence"),
                        "completenessFields", Arrays.asList("scopeGraphComplete", "declarationsComplete",
                                "externalCatalogComplete", "systemCatalogComplete"),
                        "completenessSourceEvidence", "REQUIRED_FOR_EACH_ENVIRONMENT_AND_CATALOG",
                        "emptyCatalog", "EXPLICIT_EMPTY_WITH_COMPLETENESS_EVIDENCE",
                        "missingEvidence", "INCOMPLETE_VARIABLE_ENVIRONMENT"),
                "binding", object("lookup", "NEAREST_SCOPE_THEN_PARENT", "comparison", "EXACT_REFERENCE_NAME",
                        "accessPathAffectsBinding", false, "identityResult", "DECLARATION_IDENTITY",
                        "boundResult", "BOUND_VARIABLE", "unboundResult", "UNBOUND_VARIABLE",
                        "unboundRequires", "COMPLETE_SCOPE_CHAIN_AND_CATALOGS_WITH_NO_MATCH",
                        "incompleteResult", "INCOMPLETE_VARIABLE_ENVIRONMENT",
                        "duplicateDeclarationResult", "DUPLICATE_VARIABLE_DECLARATION",
                        "prefixAloneProvesBinding", false));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parameter(String declaration, Map<String, Object> method, String name,
                                                 String semantic, String scope) {
        for (Object item : (List<?>) method.get("parameters")) {
            Map<String, Object> parameter = (Map<String, Object>) item;
            if (name.equals(parameter.get("name")) && semantic.equals(parameter.get("semanticType"))
                    && scope.equals(parameter.get("semanticScope")) && parameter.containsKey("semanticEvidence")) {
                return parameter;
            }
        }
        throw invalid(declaration, "参数缺少独立语义证据: " + name);
    }

    private static Map<?, ?> role(String declaration, Map<?, ?> constructor, String input) {
        for (Object item : (List<?>) constructor.get("parameters")) {
            Map<?, ?> role = (Map<?, ?>) item;
            if (input.equals(role.get("input"))) { return role; }
        }
        throw invalid(declaration, "entry 缺少角色: " + input);
    }

    private static Object field(String declaration, Map<?, ?> parameter) {
        return contract(declaration, parameter, "objectFieldBinding").get("field");
    }

    private static Map<?, ?> contract(String declaration, Map<?, ?> value, String key) {
        if (!(value.get(key) instanceof Map)) { throw invalid(declaration, "缺少依赖契约: " + key); }
        return (Map<?, ?>) value.get(key);
    }

    private static Map<String, String> attributes(String declaration, String raw, boolean trailingText, String... keys) {
        String[] tokens = raw.split("\\s+", trailingText ? keys.length : 0);
        if (tokens.length != keys.length) { throw invalid(declaration, "属性数量不匹配: " + raw); }
        Map<String, String> result = new LinkedHashMap<>();
        for (String token : tokens) {
            int separator = token.indexOf('=');
            if (separator <= 0 || separator == token.length() - 1) { throw invalid(declaration, "非法属性: " + token); }
            String key = token.substring(0, separator);
            if (!Arrays.asList(keys).contains(key)
                    || result.putIfAbsent(key, token.substring(separator + 1)) != null) {
                throw invalid(declaration, "未知或重复属性: " + key);
            }
        }
        return result;
    }

    private static Map<String, Object> source(String tag, String raw) {
        return object("source", "JAVADOC", "tag", tag, "value", raw);
    }

    private static Map<String, Object> object(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) { result.put((String) pairs[i], pairs[i + 1]); }
        return result;
    }

    private static IllegalArgumentException invalid(String declaration, String reason) {
        return new IllegalArgumentException("非法 variable binding/scope: " + declaration + "，原因: " + reason);
    }
}
