package com.mongoplus.indexer.scanner;

import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/** 通用 document entry 构造及有序 typed List 收集；不依赖方法名、字段名或 Stage 名。 */
final class EntryContainerConstructionContract {
    static final String CAPABILITY = "ENTRY_CONTAINER_CONSTRUCTION_V1";
    static final String ENTRY_TAG = "mongoEntryConstruction";
    static final String CONTAINER_TAG = "mongoTypedContainer";
    static final String SOURCE_TAG = "mongoEntryConstructionSource";
    private static final String EXPRESSION_CONCEPT = "PIPELINE_EXPRESSION_FIELD_REFERENCE";

    private EntryContainerConstructionContract() { }

    static boolean declares(Map<String, List<String>> tags, String parameter) {
        return tags.getOrDefault(CONTAINER_TAG, Collections.<String>emptyList()).stream()
                .anyMatch(raw -> parameter.equals(raw.split("\\s+")[0]));
    }

    @SuppressWarnings("unchecked")
    static boolean apply(String declaration, Map<String, List<String>> tags, Map<String, Object> method,
                         Function<String, Tree> parameterTree, Function<String, String> resolveName,
                         Map<String, Map<String, String>> genericDeclarations, Path repository, Consumer<String> concept) {
        Map<String, Map<String, String>> entries = attributes(declaration, tags, ENTRY_TAG,
                "constructor", "artifact", "key", "value", "keySemantic", "valueSemantic", "result");
        Map<String, Map<String, String>> containers = attributes(declaration, tags, CONTAINER_TAG,
                "input", "order", "target");
        Map<String, Map<String, String>> sources = attributes(declaration, tags, SOURCE_TAG,
                "artifact", "symbols", "mechanism");
        if (!entries.keySet().equals(containers.keySet()) || !entries.keySet().equals(sources.keySet())) {
            throw invalid(declaration, "entry、typed-container 及构造来源必须逐参数配对");
        }
        for (String name : entries.keySet()) {
            Map<String, String> entry = entries.get(name);
            Map<String, String> container = containers.get(name);
            Map<String, String> audit = sources.get(name);
            Map<String, Object> parameter = null;
            for (Object item : (List<?>) method.get("parameters")) {
                Map<String, Object> candidate = (Map<String, Object>) item;
                if (name.equals(candidate.get("name"))) { parameter = candidate; break; }
            }
            if (parameter == null || !"ELEMENT".equals(parameter.get("semanticScope"))
                    || !entry.get("result").equals(parameter.get("semanticType"))
                    || !parameter.containsKey("semanticEvidence")
                    || !StageParameterConcepts.acceptsConcept(entry.get("result"),
                    (String) parameter.get("conceptRef"))) {
                throw invalid(declaration, "entry 结果必须匹配显式 ELEMENT 参数及 concept: " + name);
            }
            if (!"DOCUMENT_ENTRIES".equals(container.get("input")) || !"INPUT".equals(container.get("order"))
                    || !"java.util.List".equals(container.get("target"))) {
                throw invalid(declaration, "typed-container 必须为 DOCUMENT_ENTRIES、INPUT 和 java.util.List");
            }
            int keyIndex = argumentIndex(declaration, entry.get("key"));
            int valueIndex = argumentIndex(declaration, entry.get("value"));
            if (keyIndex == valueIndex || !entry.get("artifact").equals(audit.get("artifact"))) {
                throw invalid(declaration, "entry 参数位置冲突或来源 artifact 不一致");
            }
            String keyConcept = StageParameterConcepts.conceptId(entry.get("keySemantic"));
            if (!StageParameterConcepts.contains(entry.get("keySemantic"))
                    || !StageParameterConcepts.accepts(entry.get("keySemantic"), "java.lang.String", "VALUE")) {
                throw invalid(declaration, "entry key 缺少已登记的 String 语义");
            }
            String valueSemantic = entry.get("valueSemantic");
            String valueConcept;
            if ("PIPELINE_EXPRESSION".equals(valueSemantic)) { valueConcept = EXPRESSION_CONCEPT; }
            else if ("FIELD_REFERENCE".equals(valueSemantic)) {
                valueConcept = StageParameterConcepts.conceptId(valueSemantic);
            } else { throw invalid(declaration, "entry value 需要显式表达式或字段引用语义"); }

            Tree targetTree = parameterTree.apply(name);
            if (!(targetTree instanceof ParameterizedTypeTree)) {
                throw invalid(declaration, "typed-container 拒绝 raw container");
            }
            ParameterizedTypeTree list = (ParameterizedTypeTree) targetTree;
            if (!container.get("target").equals(resolveName.apply(list.getType().toString()))
                    || list.getTypeArguments().size() != 1
                    || !(list.getTypeArguments().get(0) instanceof ParameterizedTypeTree)) {
                throw invalid(declaration, "typed-container 需要单层 List<显式泛型元素>");
            }
            ParameterizedTypeTree element = (ParameterizedTypeTree) list.getTypeArguments().get(0);
            String elementRaw = resolveName.apply(element.getType().toString());
            if (!elementRaw.equals(entry.get("constructor")) || element.getTypeArguments().size() != 1) {
                throw invalid(declaration, "entry 构造类型须与目标元素精确匹配，不能用 raw/subtype List 替代");
            }
            Tree argumentTree = element.getTypeArguments().get(0);
            if (argumentTree.getKind() != Tree.Kind.IDENTIFIER && argumentTree.getKind() != Tree.Kind.MEMBER_SELECT) {
                throw invalid(declaration, "泛型实参须为显式引用类型或无界类型变量，不接受 wildcard/nested container");
            }
            String argumentName = argumentTree.toString();
            Map<String, Object> argument;
            if (genericDeclarations.containsKey(argumentName)) {
                String generic = genericDeclarations.get(argumentName).get("declaration");
                if (!(argumentName.equals(generic) || (argumentName + " extends Object").equals(generic)
                        || (argumentName + " extends java.lang.Object").equals(generic))) {
                    throw invalid(declaration, "当前 entry generic 仅支持 Object 上界");
                }
                argument = object("kind", "TYPE_VARIABLE", "name", argumentName,
                        "declaredBy", genericDeclarations.get(argumentName).get("declaredBy"),
                        "upperBounds", Arrays.asList("java.lang.Object"));
            } else {
                argumentName = resolveName.apply(argumentName);
                argument = object("kind", "REFERENCE", "rawType", argumentName);
            }
            Map<String, Object> constructor = ArtifactConstructorEvidence.inspect(repository, entry.get("artifact"),
                    elementRaw, keyIndex, valueIndex);
            String formalGeneric = (String) ((List<?>) constructor.get("typeParameters")).get(0);
            Map<String, Object> elementType = object("kind", "PARAMETERIZED", "rawType", elementRaw,
                    "typeArguments", Arrays.asList(argument));
            Map<String, Object> targetType = object("kind", "PARAMETERIZED", "rawType", "java.util.List",
                    "typeArguments", Arrays.asList(elementType));
            String elementJavaType = elementRaw + "<" + argumentName + ">";
            String targetJavaType = "java.util.List<" + elementJavaType + ">";
            constructor.put("parameters", Arrays.asList(
                    role(keyIndex, "ENTRY_KEY", entry.get("keySemantic"), keyConcept, "java.lang.String"),
                    role(valueIndex, "ENTRY_VALUE", valueSemantic, valueConcept, formalGeneric)));
            constructor.put("resultSemanticType", entry.get("result"));
            constructor.put("resultJavaType", elementJavaType);
            constructor.put("genericBindings", Arrays.asList(object("typeParameter", formalGeneric,
                    "declaredBy", elementRaw, "argument", argument, "source", "ENTRY_VALUE")));
            constructor.put("valueAssignability", object("source", "ENTRY_VALUE", "target", argument,
                    "rule", "JAVA_ASSIGNABLE", "validation", "REQUIRED_BEFORE_CONSTRUCTION"));
            constructor.put("semanticSourceEvidence", new LinkedHashMap<String, String>(audit));
            constructor.put("semanticDeclarationEvidence", source(SOURCE_TAG, raw(tags, SOURCE_TAG, name)));
            parameter.put("elementJavaType", elementJavaType);
            parameter.put("entryConstruction", object("operation", "CONSTRUCT_ENTRY", "input", "DOCUMENT_ENTRY",
                    "constructor", constructor, "resultSemanticType", entry.get("result"),
                    "sourceEvidence", source(ENTRY_TAG, raw(tags, ENTRY_TAG, name))));
            parameter.put("typedContainerConstruction", object("operation", "COLLECT_ENTRIES", "input", "DOCUMENT",
                    "entryIteration", "ORDERED_ENTRIES", "order", "INPUT", "inputOrderPreserved", true,
                    "elementConstructionRef", declaration + ":" + name + ".entryConstruction",
                    "elementType", elementType, "genericArgument", argument,
                    "targetContainerType", targetType, "targetJavaType", targetJavaType,
                    "elementAssignability", object("kind", "IDENTITY", "from", elementType, "to", elementType,
                            "genericArguments", "EXACT_AFTER_SUBSTITUTION", "verified", true),
                    "containerAssignability", object("kind", "IDENTITY", "from", targetType, "to", targetType,
                            "genericVariance", "INVARIANT", "verified", true),
                    "genericArgumentInference", object("source", "ALL_ENTRY_VALUES", "target", argument,
                            "rule", "TYPE_VARIABLE".equals(argument.get("kind"))
                                    ? "COMMON_ASSIGNABLE_TYPE" : "ASSIGNABLE_TO_DECLARED_ARGUMENT",
                            "sharedAcrossEntries", true, "upperBounds", Arrays.asList("java.lang.Object")),
                    "sourceEvidence", source(CONTAINER_TAG, raw(tags, CONTAINER_TAG, name))));
            concept.accept(keyConcept);
            concept.accept(valueConcept);
            concept.accept((String) parameter.get("conceptRef"));
        }
        return !entries.isEmpty();
    }

    private static Map<String, Object> role(int index, String input, String semantic, String concept, String javaType) {
        return object("argumentIndex", index, "input", input, "semanticType", semantic, "semanticScope", "VALUE",
                "conceptRef", concept, "javaType", javaType, "encoding", "UNCHANGED");
    }

    /** 每项一次、固定属性集合；mechanism 的尾部说明允许空格。 */
    private static Map<String, Map<String, String>> attributes(String declaration, Map<String, List<String>> tags,
                                                              String tag, String... keys) {
        Map<String, Map<String, String>> result = new LinkedHashMap<String, Map<String, String>>();
        for (String raw : tags.getOrDefault(tag, Collections.<String>emptyList())) {
            String[] tokens = raw.split("\\s+", SOURCE_TAG.equals(tag) ? keys.length + 1 : 0);
            if (tokens.length != keys.length + 1) { throw invalid(declaration, tag + " 属性数量不匹配"); }
            Map<String, String> values = new LinkedHashMap<String, String>();
            for (int i = 1; i < tokens.length; i++) {
                int equals = tokens[i].indexOf('=');
                if (equals <= 0 || equals == tokens[i].length() - 1) { throw invalid(declaration, "非法属性"); }
                String key = tokens[i].substring(0, equals);
                if (!Arrays.asList(keys).contains(key) || values.putIfAbsent(key,
                        tokens[i].substring(equals + 1)) != null) { throw invalid(declaration, "未知/重复属性: " + key); }
            }
            if (result.putIfAbsent(tokens[0], values) != null) { throw invalid(declaration, "重复参数: " + tokens[0]); }
        }
        return result;
    }

    private static int argumentIndex(String declaration, String value) {
        if (!("0".equals(value) || "1".equals(value))) { throw invalid(declaration, "entry 位置只接受 0/1"); }
        return Integer.parseInt(value);
    }

    private static String raw(Map<String, List<String>> tags, String tag, String name) {
        return tags.get(tag).stream().filter(value -> name.equals(value.split("\\s+")[0])).findFirst().get();
    }

    private static Map<String, Object> source(String tag, String raw) {
        return object("source", "JAVADOC", "tag", tag, "value", raw);
    }

    private static Map<String, Object> object(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) { result.put((String) pairs[i], pairs[i + 1]); }
        return result;
    }

    private static IllegalArgumentException invalid(String declaration, String reason) {
        return new IllegalArgumentException("非法 entry/container construction: " + declaration + "，原因: " + reason);
    }
}
