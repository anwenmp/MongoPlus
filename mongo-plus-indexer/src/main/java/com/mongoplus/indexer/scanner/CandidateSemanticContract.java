package com.mongoplus.indexer.scanner;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WildcardTree;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 候选的条件化 BSON 构造项。这里只发布可递归代入的事实，不选候选，不按 API 名称归类。
 * 类型和顺序属于结果；Java 容器表示属于参数域，二者不能混为等价依据。
 */
final class CandidateSemanticContract {
    static final String CAPABILITY = "CANDIDATE_SEMANTICS_V1";
    private static final String TAG = "mongoCandidate";
    private static final String SOURCE = "mongoCandidateSource";
    private static final String ESTABLISHED = "ESTABLISHED";

    private CandidateSemanticContract() { }

    static boolean hasTags(Map<String, List<String>> tags) {
        return tags.containsKey(TAG) || tags.containsKey(SOURCE);
    }

    @SuppressWarnings("unchecked")
    static boolean apply(String declaration, Map<String, List<String>> tags, Map<String, Object> method,
                         Function<String, Tree> parameterTree, Function<String, String> resolveType,
                         Map<String, Map<String, String>> generics) {
        if (!hasTags(tags)) { return false; }
        List<String> values = tags.getOrDefault(TAG, Collections.emptyList());
        if (values.size() != 1) { throw invalid(declaration, "需要唯一构造声明"); }
        Map<String, String> attrs = attributes(declaration, values.get(0));
        String operation = attrs.get("operation");
        List<Object> sources = sources(declaration, tags.getOrDefault(SOURCE, Collections.emptyList()));
        sources.add(object("source", "JAVADOC", "tag", TAG, "value", values.get(0)));
        Map<String, Object> contract = object("capability", CAPABILITY, "proofStatus", ESTABLISHED,
                "comparison", "RAW_BSON_ORDERED_TYPED", "sourceEvidence", sources,
                "applicability", object("allArgumentsConsumed", true, "allChildrenRequireIndependentEvidence", true,
                        "javaBinding", "ACTUAL_CALL_SITE_TYPES_MUST_MATCH_PARAMETER_BINDINGS",
                        "nullContainers", "FORBID", "nullDocumentOrFieldElements", "FORBID",
                        "bindingAdmission", "INDEPENDENT_SEMANTIC_AND_SCOPE_VALIDATION",
                        "codec", "AUDITED_DRIVER_5_4_DEFAULT_CODECS_OR_INDEPENDENT_EXACT_ENCODING_PROOF",
                        "codecContext", "SAME_REGISTRY_DOCUMENT_CLASS_ENCODER_CONTEXT_AND_DETERMINISTIC_ENCODING",
                        "evaluation", "SAME_IMMUTABLE_INPUT_SNAPSHOT"),
                "normalizations", new ArrayList<>(), "parameterBindings", new ArrayList<>());
        method.put("candidateSemantics", contract);
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) method.get("parameters");
        for (Map<String, Object> parameter : parameters) {
            String name = (String) parameter.get("name");
            Tree tree = parameterTree.apply(name);
            Map<String, Object> binding = object("parameter", name, "declaredJavaType", parameter.get("type"),
                    "invocation", Boolean.TRUE.equals(parameter.get("varargs")) ? "VARARGS" : "SINGLE",
                    "javaRepresentation", representation(declaration, tree, resolveType, generics),
                    "semanticType", parameter.get("semanticType"), "semanticScope", parameter.get("semanticScope"),
                    "semanticEvidence", parameter.get("semanticEvidence"));
            ((List<Object>) contract.get("parameterBindings")).add(binding);
        }
        Map<String, Object> term;
        try {
            switch (operation == null ? "" : operation) {
                case "STAGE_VALUE":
                    keys(declaration, attrs, "operation");
                    Map<String, Object> scalar = only(parameters);
                    Map<String, Object> value = dependency(scalar, "stageValueBinding");
                    Map<String, Object> domain = object("parameter", scalar.get("name"), "constraints", value);
                    ((Map<String, Object>) contract.get("applicability")).put("valueDomain", domain);
                    String encoding = (String) value.get("encoding");
                    if ("INT32_EXACT".equals(encoding)) {
                        term = node("INT32_EXACT", "input", input(scalar), "minimum", value.get("minimum"),
                                "maximum", value.get("maximum"));
                    } else if ("SINGLETON_SCALAR_ELSE_ARRAY".equals(encoding)) {
                        term = node(encoding, "input", input(scalar), "elementBsonType", "STRING");
                        ((List<Object>) contract.get("normalizations")).add(object("operation", encoding,
                                "appliesTo", "DECLARED_JAVA_INPUT_SEQUENCE", "rawBsonRewriteAllowed", false,
                                "sourceEvidence", value.get("sourceEvidence")));
                    } else { throw missing("Stage value 编码未支持"); }
                    term = wrap(method, term);
                    break;
                case "DOCUMENT_ENTRY":
                    keys(declaration, attrs, "operation");
                    Map<String, Object> entry = dependency(method, "documentEntryConstruction");
                    term = node("DOCUMENT_ENTRIES", "key", entry.get("key"), "value", entry.get("value"),
                            "order", entry.get("order"), "duplicatePosition", entry.get("duplicatePosition"));
                    ((Map<String, Object>) contract.get("applicability")).put("entryDomain", entry);
                    contract.put("resultSemanticType", method.get("resultSemanticType"));
                    break;
                case "DOCUMENT_MERGE":
                    keys(declaration, attrs, "operation", "duplicatePosition");
                    if (!Arrays.asList("FIRST", "LAST").contains(attrs.get("duplicatePosition"))) {
                        throw invalid(declaration, "重复字段位置未确认");
                    }
                    Map<String, Object> reduction = dependency(method, "reductionContract");
                    if (reduction.containsKey("duplicatePosition")
                            && !attrs.get("duplicatePosition").equals(reduction.get("duplicatePosition"))) {
                        throw invalid(declaration, "与独立 reduction 的重复字段位置冲突");
                    }
                    term = node("DOCUMENT_MERGE", "input", input(only(parameters)), "order", reduction.get("order"),
                            "duplicateKeys", reduction.get("duplicateKeys"), "depth", reduction.get("depth"),
                            "empty", reduction.get("empty"), "duplicatePosition", attrs.get("duplicatePosition"));
                    contract.put("resultSemanticType", method.get("resultSemanticType"));
                    contract.put("dependencyEvidence", reduction);
                    break;
                case "STAGE_DOCUMENT_INPUT":
                    keys(declaration, attrs, "operation");
                    Map<String, Object> body = only(parameters);
                    Map<String, Object> document = dependency(body, "documentInputBinding");
                    if (!"WRAP_DECLARED_STAGE".equals(document.get("encoding"))) {
                        throw missing("只有显式 Stage body 包装可以建立完整 Stage 构造项");
                    }
                    term = wrap(method, node("BSON_DOCUMENT_INPUT", "input", input(body)));
                    contract.put("dependencyEvidence", document);
                    break;
                case "STAGE_EXPRESSION_DOCUMENT":
                    keys(declaration, attrs, "operation", "runtimeJava", "runtimeCodec", "bsonDocumentCodec");
                    if (sources.stream().noneMatch(source -> ((Map<?, ?>) source).containsKey("path"))
                            || sources.stream().noneMatch(source -> ((Map<?, ?>) source).containsKey("reference"))) {
                        throw missing("缺独立的本地委托与外部编码来源");
                    }
                    Map<String, Object> expression = only(parameters);
                    semantic(expression, "PIPELINE_EXPRESSION");
                    String expressionType = elementRepresentation(contract, expression);
                    boolean unboundedExpression = generics.containsKey(expressionType)
                            && expressionType.equals(generics.get(expressionType).get("declaration"))
                            && ((List<?>) method.get("typeParameters")).contains(expressionType);
                    if (!"org.bson.Document".equals(attrs.get("runtimeJava"))
                            || !"org.bson.codecs.DocumentCodec".equals(attrs.get("runtimeCodec"))
                            || !"org.bson.codecs.BsonDocumentCodec".equals(attrs.get("bsonDocumentCodec"))
                            || !("org.bson.Document".equals(expressionType) || unboundedExpression)
                            || Boolean.TRUE.equals(method.get("staticMethod"))
                            || !"VALUE".equals(expression.get("semanticScope"))
                            || Boolean.TRUE.equals(expression.get("varargs"))) {
                        throw missing("文档表达式需要精确 Document 或独立绑定的无界方法泛型 VALUE 参数");
                    }
                    if (!List.of("PIPELINE_EXPRESSION -> PIPELINE_STAGE_DOCUMENT")
                            .equals(method.get("compositionSemantics"))
                            || !"PIPELINE_STAGE_DOCUMENT".equals(method.get("resultSemanticType"))) {
                        throw missing("缺独立 Expression 到完整 Stage 的 composition/result");
                    }
                    dependency(method, "resultSemanticEvidence");
                    Map<String, Object> expressionEffect = dependency(method, "pipelineEffect");
                    if (!"APPEND_STAGE".equals(expressionEffect.get("operation"))
                            || !"RECEIVER".equals(expressionEffect.get("target"))
                            || !"ONE".equals(expressionEffect.get("count"))
                            || !"CALL_ORDER".equals(expressionEffect.get("order"))) {
                        throw missing("文档表达式 Stage 需要当前 receiver 的单次有序追加证据");
                    }
                    // Java 准入与 runtime 编码分别证明；无界泛型声明不授予任意 Object 输入等价性。
                    ((Map<String, Object>) contract.get("applicability")).put("documentExpressionDomain",
                            object("parameter", expression.get("name"), "sourceJavaType", attrs.get("runtimeJava"),
                                    "runtimeJavaType", attrs.get("runtimeJava"), "runtimeTypeMatch", "EXACT_CLASS",
                                    "requiredDocumentCodec", attrs.get("runtimeCodec"),
                                    "requiredBsonDocumentCodec", attrs.get("bsonDocumentCodec"),
                                    "childEvidence", "ESTABLISHED_PIPELINE_EXPRESSION_RESULT_AND_COMPLETE_CALL_TREE",
                                    "javaBinding", "VERIFY_ACTUAL_OVERLOAD_AND_CONCRETE_GENERIC_INSTANTIATION_WITH_DOCUMENT_SOURCE",
                                    "bsonType", "DOCUMENT", "nullInput", "FORBID", "unprovedTypes", "FORBID",
                                    "encoding", "BUILDERS_HELPER_CODEC_RUNTIME_DOCUMENT"));
                    term = wrap(method, node("BUILDERS_HELPER_CODEC", "input", input(expression)));
                    break;
                case "FIELD_STAGE":
                    if (attrs.containsKey("allowedInt32")) {
                        keys(declaration, attrs, "operation", "key", "value", "allowedInt32");
                    } else { keys(declaration, attrs, "operation", "key", "value"); }
                    Map<String, Object> field = parameter(parameters, attrs.get("key"));
                    semantic(field, "FIELD_NAME");
                    if (!"java.lang.String".equals(elementRepresentation(contract, field))) {
                        throw invalid(declaration, "V1 FIELD_STAGE 只声明真实 String 键，getter 需独立证明");
                    }
                    Map<String, Object> fieldValue;
                    if (attrs.get("value").startsWith("int32:")) {
                        if (attrs.containsKey("allowedInt32")) {
                            throw invalid(declaration, "固定输出常量不能声明不存在的值参数输入域");
                        }
                        fieldValue = object("input", "FIXED", "encoding", "INT32_EXACT",
                                "value", integer(declaration, attrs.get("value").substring(6)));
                        if (parameters.size() != 1) { throw invalid(declaration, "固定值不能遗漏参数"); }
                    } else {
                        Map<String, Object> direction = parameter(parameters, attrs.get("value"));
                        semantic(direction, "INTEGER_VALUE");
                        if (parameters.size() != 2 || field == direction
                                || !"java.lang.Integer".equals(elementRepresentation(contract, direction))) {
                            throw invalid(declaration, "值需要真实 Integer 独立参数");
                        }
                        fieldValue = object("inputParameter", direction.get("name"), "encoding", "INT32_EXACT");
                        if (attrs.containsKey("allowedInt32")) {
                            List<Integer> allowed = Arrays.stream(attrs.get("allowedInt32").split(",", -1))
                                    .map(item -> integer(declaration, item)).toList();
                            if (allowed.stream().distinct().count() != allowed.size()) {
                                throw invalid(declaration, "重复整数输入域值");
                            }
                            ((Map<String, Object>) contract.get("applicability")).put("integerDomain",
                                    object("parameter", direction.get("name"), "allowedValues", allowed,
                                            "encoding", "INT32_EXACT", "enforcement", "BINDING_CONSUMER"));
                        }
                    }
                    term = wrap(method, node("DOCUMENT_ENTRIES", "key", object("inputParameter", field.get("name"),
                            "scope", field.get("semanticScope"), "encoding", "UNCHANGED", "javaType", "java.lang.String"),
                            "value", fieldValue, "order", "INPUT", "duplicatePosition", "FIRST"));
                    break;
                case "EXPRESSION_ARGUMENTS":
                    keys(declaration, attrs, "operation", "collectionCodec", "resultJava");
                    if (parameters.isEmpty()) { throw missing("操作数声明不能为空"); }
                    if (!Arrays.asList("Bson", "org.bson.conversions.Bson").contains(method.get("returnType"))
                            || !"org.bson.Document".equals(attrs.get("resultJava"))
                            || !"org.bson.codecs.CollectionCodec".equals(attrs.get("collectionCodec"))) {
                        throw missing("表达式需要已审计的 Bson 返回类型、Document 表示和 CollectionCodec");
                    }
                    if (!"ARRAY".equals(method.get("expressionShape"))
                            || !"PIPELINE_EXPRESSION".equals(method.get("resultSemanticType"))) {
                        throw missing("操作数需要独立 ARRAY shape 和 expression result");
                    }
                    dependency(method, "resultSemanticEvidence");
                    String relation = String.join(" + ", Collections.nCopies(parameters.size(), "PIPELINE_EXPRESSION"))
                            + " -> PIPELINE_EXPRESSION";
                    if (!List.of(relation).equals(method.get("compositionSemantics"))) {
                        throw missing("操作数 composition 必须与逐参数语义及数量一致");
                    }
                    List<Object> arguments = new ArrayList<>();
                    Map<String, Object> argumentArray;
                    if (parameters.size() == 1 && "ELEMENT".equals(parameters.get(0).get("semanticScope"))) {
                        Map<String, Object> sequence = parameters.get(0);
                        semantic(sequence, "PIPELINE_EXPRESSION");
                        if (!"Object[]".equals(sequence.get("type"))
                                && !"java.lang.Object[]".equals(sequence.get("type"))
                                || !Boolean.TRUE.equals(sequence.get("varargs"))) {
                            throw missing("变长操作数需要真实 Object varargs ELEMENT 参数");
                        }
                        argumentArray = node("ARRAY_RUNTIME_CODEC", "input", input(sequence), "order", "ITERATION",
                                "requiredCollectionCodec", attrs.get("collectionCodec"),
                                "codecEquivalence", "SAME_ELEMENT_DISPATCH_AND_ENCODING_CONFIG_REQUIRED");
                    } else {
                        for (Map<String, Object> argument : parameters) {
                            semantic(argument, "PIPELINE_EXPRESSION");
                            if (!"VALUE".equals(argument.get("semanticScope"))
                                    || Boolean.TRUE.equals(argument.get("varargs"))
                                    || !"java.lang.Object".equals(elementRepresentation(contract, argument))
                                    || !Arrays.asList("Object", "java.lang.Object").contains(argument.get("type"))) {
                                throw missing("固定操作数需要独立的 Object VALUE 参数");
                            }
                            arguments.add(input(argument));
                        }
                        argumentArray = node("ARRAY_ARGUMENTS_RUNTIME_CODEC", "inputs", arguments, "order", "DECLARATION",
                                "requiredCollectionCodec", attrs.get("collectionCodec"),
                                "codecEquivalence", "SAME_ELEMENT_DISPATCH_AND_ENCODING_CONFIG_REQUIRED");
                    }
                    term = node("DOCUMENT", "key", mapping(method, "mongoExpressions"),
                            "value", argumentArray);
                    boolean sequenceInput = "ARRAY_RUNTIME_CODEC".equals(argumentArray.get("op"));
                    ((Map<String, Object>) contract.get("applicability")).put("expressionOperands",
                            object("binding", sequenceInput ? "PARAMETER_ELEMENTS" : "PARAMETER_VALUES",
                                    "minimumCount", sequenceInput ? 0 : parameters.size(),
                                    "maximumCount", sequenceInput ? "UNBOUNDED" : parameters.size(),
                                    "nullValues", "ENCODE_BSON_NULL", "serverEvaluation", "NOT_PERFORMED"));
                    contract.put("javaResultRepresentation", attrs.get("resultJava"));
                    contract.put("runtimeCodecOperandSubstitution", "CHILD_BSON_ALONE_IS_INSUFFICIENT_REQUIRE_RUNTIME_TYPE_CODEC_PROOF");
                    contract.put("resultSemanticType", "PIPELINE_EXPRESSION");
                    break;
                case "EXPRESSION_VALUE":
                case "EXPRESSION_OBJECT_ARGUMENTS":
                    boolean objectArguments = "EXPRESSION_OBJECT_ARGUMENTS".equals(operation);
                    if (objectArguments) { keys(declaration, attrs, "operation", "fields", "resultJava"); }
                    else { keys(declaration, attrs, "operation", "resultJava"); }
                    if (parameters.isEmpty() || !"org.bson.Document".equals(attrs.get("resultJava"))
                            || !Arrays.asList("org.bson.conversions.Bson", "org.bson.Document")
                                    .contains(resolveType.apply(method.get("returnType").toString()))
                            || !"PIPELINE_EXPRESSION".equals(method.get("resultSemanticType"))
                            || !(objectArguments ? "OBJECT" : "VALUE").equals(method.get("expressionShape"))) {
                        throw missing("表达式需要独立 result、shape 和已审计 Document 表示");
                    }
                    dependency(method, "resultSemanticEvidence");
                    String expectedRelation = String.join(" + ", Collections.nCopies(parameters.size(), "PIPELINE_EXPRESSION"))
                            + " -> PIPELINE_EXPRESSION";
                    if (!List.of(expectedRelation).equals(method.get("compositionSemantics"))) {
                        throw missing("composition 必须匹配每个独立操作数");
                    }
                    List<String> fields = objectArguments ? Arrays.asList(attrs.get("fields").split(",", -1)) : List.of();
                    if (objectArguments && (fields.size() != parameters.size()
                            || fields.stream().distinct().count() != fields.size()
                            || fields.stream().anyMatch(String::isEmpty)) || !objectArguments && parameters.size() != 1) {
                        throw invalid(declaration, "固定字段与参数必须一一对应");
                    }
                    List<Object> expressionEntries = new ArrayList<>();
                    for (int i = 0; i < parameters.size(); i++) {
                        Map<String, Object> argument = parameters.get(i);
                        semantic(argument, "PIPELINE_EXPRESSION");
                        String javaType = elementRepresentation(contract, argument);
                        boolean unboundedGeneric = generics.containsKey(javaType)
                                && javaType.equals(generics.get(javaType).get("declaration"));
                        if (!"VALUE".equals(argument.get("semanticScope"))
                                || Boolean.TRUE.equals(argument.get("varargs"))
                                || !(Arrays.asList("java.lang.Object", "java.lang.String").contains(javaType)
                                    || unboundedGeneric)) {
                            throw missing("操作数需要 Object、String 或显式无界类型变量 VALUE 参数");
                        }
                        if (objectArguments) { expressionEntries.add(object("key", fields.get(i), "value", input(argument))); }
                    }
                    Map<String, Object> expressionValue = objectArguments
                            ? node("ORDERED_DOCUMENT_ARGUMENTS", "entries", expressionEntries, "order", "DECLARATION",
                                    "requiredDocumentCodec", "org.bson.codecs.DocumentCodec")
                            : node("RUNTIME_CODEC_VALUE", "input", input(parameters.get(0)));
                    term = node("DOCUMENT", "key", mapping(method, "mongoExpressions"), "value", expressionValue);
                    contract.put("javaResultRepresentation", attrs.get("resultJava"));
                    contract.put("resultSemanticType", "PIPELINE_EXPRESSION");
                    contract.put("runtimeCodecOperandSubstitution", "CHILD_BSON_ALONE_IS_INSUFFICIENT_REQUIRE_RUNTIME_TYPE_CODEC_PROOF");
                    ((Map<String, Object>) contract.get("applicability")).put("expressionOperands",
                            object("binding", "PARAMETER_VALUES", "minimumCount", parameters.size(),
                                    "maximumCount", parameters.size(), "nullValues", "ENCODE_BSON_NULL",
                                    "serverEvaluation", "NOT_PERFORMED"));
                    break;
                case "EXPRESSION_ARRAY":
                    keys(declaration, attrs, "operation", "collectionCodec", "resultJava");
                    Map<String, Object> operands = only(parameters);
                    semantic(operands, "PIPELINE_EXPRESSION");
                    if (!"ELEMENT".equals(operands.get("semanticScope"))) { throw missing("数组元素作用域缺失"); }
                    String element = elementRepresentation(contract, operands);
                    if (!Arrays.asList("java.lang.Object", "?").contains(element)) {
                        throw invalid(declaration, "runtime expression 数组需要真实 Object[] 或 Collection<?>");
                    }
                    term = node("DOCUMENT", "key", mapping(method, "mongoExpressions"),
                            "value", node("ARRAY_RUNTIME_CODEC", "input", input(operands), "order", "ITERATION",
                                    "requiredCollectionCodec", attrs.get("collectionCodec"),
                                    "codecEquivalence", "SAME_ELEMENT_DISPATCH_AND_ENCODING_CONFIG_REQUIRED"));
                    contract.put("javaResultRepresentation", attrs.get("resultJava"));
                    contract.put("runtimeCodecOperandSubstitution", "CHILD_BSON_ALONE_IS_INSUFFICIENT_REQUIRE_RUNTIME_TYPE_CODEC_PROOF");
                    contract.put("resultSemanticType", "PIPELINE_EXPRESSION");
                    break;
                case "PREFIXED_ENTRIES_STAGE":
                    keys(declaration, attrs, "operation", "key", "value", "entries");
                    Map<String, Object> prefixValue = parameter(parameters, attrs.get("value"));
                    semantic(prefixValue, "PIPELINE_EXPRESSION");
                    Object entries;
                    if ("EMPTY".equals(attrs.get("entries"))) {
                        if (parameters.size() != 1) { throw invalid(declaration, "空条目不能遗漏参数"); }
                        entries = Collections.emptyList();
                    } else {
                        Map<String, Object> pairs = parameter(parameters, attrs.get("entries"));
                        if (parameters.size() != 2 || pairs == prefixValue
                                || !"com.mongodb.client.model.BsonField".equals(elementRepresentation(contract, pairs))) {
                            throw invalid(declaration, "命名条目需要实际 BsonField 单层容器");
                        }
                        entries = node("NAMED_EXPRESSION_ENTRIES", "input", input(pairs),
                                "nameAccess", "getName", "valueAccess", "getValue", "encoding", "BUILDERS_HELPER_CODEC",
                                "order", "ITERATION", "constructionEvidence", "REQUIRED_INDEPENDENTLY");
                        for (Object item : (List<?>) contract.get("parameterBindings")) {
                            Map<String, Object> binding = (Map<String, Object>) item;
                            if (pairs.get("name").equals(binding.get("parameter"))) {
                                binding.put("semanticType", "NAMED_EXPRESSION_ENTRIES");
                                binding.put("semanticScope", "ELEMENT");
                                binding.put("semanticEvidence", object("source", "JAVADOC", "tag", TAG,
                                        "value", values.get(0), "sourceEvidence", sources));
                            }
                        }
                    }
                    term = wrap(method, node("PREFIXED_ENTRIES", "key", attrs.get("key"),
                            "value", node("BUILDERS_HELPER_CODEC", "input", input(prefixValue)), "entries", entries));
                    ((Map<String, Object>) contract.get("applicability")).put("namedEntryDomain",
                            object("duplicates", "REJECT", "reservedKeys", List.of(attrs.get("key")),
                                    "idNull", "ENCODE_BSON_NULL", "entriesNull", "FORBID"));
                    break;
                default:
                    throw invalid(declaration, "未知构造 operation: " + operation);
            }
            if (term.get("op").equals("DOCUMENT") && !((List<?>) method.get("mongoStages")).isEmpty()) {
                contract.put("receiverEffect", dependency(method, "pipelineEffect"));
                contract.put("resultSemanticType", "PIPELINE_STAGE_DOCUMENT");
                contract.put("receiverIdentity", "SYMBOLIC_CALL_SITE_RECEIVER");
            } else {
                if (!Boolean.TRUE.equals(method.get("staticMethod"))
                        && !((List<?>) method.get("mongoStages")).isEmpty()) { throw missing("Stage 构造没有完整包装"); }
                contract.put("receiverEffect", object("operation", "NONE", "count", "ZERO"));
            }
            contract.put("bsonTerm", term);
        } catch (MissingEvidence exception) {
            contract.put("proofStatus", "NOT_ESTABLISHED");
            contract.put("reason", exception.getMessage());
            contract.remove("bsonTerm");
        }
        return true;
    }

    static Map<String, Object> concept() {
        return object("id", CAPABILITY, "semanticType", "CONDITIONAL_CANDIDATE_SEMANTICS",
                "constraintEnforcement", "BINDING_CONSUMER", "coreRuntimeValidationImplied", false,
                "equivalenceRelation", object("domain", "INTERSECTION_OF_ADMITTED_CONCRETE_INPUTS",
                        "javaAdmission", "VERIFY_EACH_ROUTE_ACTUAL_ARGUMENT_TYPES_AND_INVOCATION",
                        "instantiate", "SUBSTITUTE_ALL_ARGUMENTS_AND_RECURSIVELY_PROVEN_CHILD_TERMS",
                        "compare", "EXACT_BSON_TYPES_VALUES_DOCUMENT_ENTRY_ORDER_ARRAY_ORDER",
                        "effects", "SAME_RECEIVER_IDENTITY_STAGE_COUNT_CALL_ORDER_AND_SCOPE_BINDINGS",
                        "closure", "EVERY_NODE_AND_LEAF_ESTABLISHED_OR_NO_EQUIVALENCE",
                        "codecObligations", "VERIFY_CONTAINER_AND_EVERY_LEAF_RUNTIME_CODEC_NOT_JUST_REGISTRY_IDENTITY",
                        "javaRoutes", "MAY_DIFFER_AFTER_PROVEN_ENCODING",
                        "normalization", "ONLY_DECLARED_JAVA_INPUT_TRANSFORMS_NO_RAW_BSON_REWRITE"),
                "crossApiCallTrees", true, "methodNameIsEvidence", false, "declarationOrderIsEvidence", false,
                "selectionPolicy", "NOT_PROVIDED", "codecEquivalence", "NOT_INFERRED_FROM_JAVA_TYPE",
                "description", "构造项可代入完整调用树。等价类由具体输入、BSON及作用域/effect决定，"
                        + "不是静态方法分组；缺子节点、类型、变量绑定或 receiver 身份时保留未闭合。");
    }

    private static Map<String, Object> wrap(Map<String, Object> method, Map<String, Object> body) {
        dependency(method, "pipelineEffect");
        return node("DOCUMENT", "key", mapping(method, "mongoStages"), "value", body);
    }

    private static String mapping(Map<String, Object> method, String key) {
        List<?> mappings = (List<?>) method.get(key);
        if (mappings.size() != 1) { throw missing("需要独立且唯一的 " + key); }
        return mappings.get(0).toString();
    }

    private static void semantic(Map<String, Object> parameter, String semantic) {
        if (!semantic.equals(parameter.get("semanticType")) || !parameter.containsKey("semanticEvidence")) {
            throw missing("独立参数语义缺失: " + parameter.get("name"));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> dependency(Map<String, Object> owner, String key) {
        if (!(owner.get(key) instanceof Map)) { throw missing("独立契约缺失: " + key); }
        return (Map<String, Object>) owner.get(key);
    }

    private static Map<String, Object> only(List<Map<String, Object>> parameters) {
        if (parameters.size() != 1) { throw missing("需要唯一输入"); }
        return parameters.get(0);
    }

    private static Map<String, Object> parameter(List<Map<String, Object>> parameters, String name) {
        return parameters.stream().filter(p -> name.equals(p.get("name"))).findFirst()
                .orElseThrow(() -> missing("参数不存在: " + name));
    }

    private static Map<String, Object> input(Map<String, Object> parameter) {
        return node("INPUT", "parameter", parameter.get("name"));
    }

    @SuppressWarnings("unchecked")
    private static String elementRepresentation(Map<String, Object> contract, Map<String, Object> parameter) {
        for (Object item : (List<?>) contract.get("parameterBindings")) {
            Map<?, ?> binding = (Map<?, ?>) item;
            if (parameter.get("name").equals(binding.get("parameter"))) {
                Map<String, Object> representation = (Map<String, Object>) binding.get("javaRepresentation");
                return representation.getOrDefault("elementType", representation.get("javaType")).toString();
            }
        }
        throw missing("实际 Java 参数表示缺失");
    }

    private static Map<String, Object> representation(String declaration, Tree tree,
            Function<String, String> resolveType, Map<String, Map<String, String>> generics) {
        if (tree instanceof ArrayTypeTree) {
            Tree element = ((ArrayTypeTree) tree).getType();
            if (element instanceof ArrayTypeTree) { throw invalid(declaration, "不声明嵌套数组等价"); }
            return object("container", "ARRAY", "elementType", type(element, resolveType, generics), "order", "ITERATION");
        }
        if (tree instanceof ParameterizedTypeTree) {
            ParameterizedTypeTree container = (ParameterizedTypeTree) tree;
            String raw = resolveType.apply(container.getType().toString());
            if ("com.mongoplus.support.SFunction".equals(raw)) {
                return object("container", "SCALAR", "javaType", raw, "typeArguments", tree.toString());
            }
            if (!Arrays.asList("java.util.List", "java.util.Collection").contains(raw)
                    || container.getTypeArguments().size() != 1) {
                throw invalid(declaration, "只声明单层 List/Collection 参数域");
            }
            Tree element = container.getTypeArguments().get(0);
            if (element instanceof ArrayTypeTree || element instanceof ParameterizedTypeTree
                    && !"com.mongoplus.support.SFunction".equals(type(element, resolveType, generics))) {
                throw invalid(declaration, "不声明嵌套容器等价");
            }
            if (element instanceof WildcardTree) {
                if (element.getKind() == Tree.Kind.SUPER_WILDCARD) { throw invalid(declaration, "不声明 lower bound"); }
                Tree bound = ((WildcardTree) element).getBound();
                element = bound == null ? element : bound;
            }
            return object("container", raw, "elementType", type(element, resolveType, generics), "order", "ITERATION");
        }
        String javaType = type(tree, resolveType, generics);
        if (Arrays.asList("java.util.List", "java.util.Collection").contains(javaType)) {
            throw invalid(declaration, "raw 容器不能证明元素表示");
        }
        return object("container", "SCALAR", "javaType", javaType,
                "genericDeclaration", generics.getOrDefault(tree.toString(), Collections.emptyMap()));
    }

    private static String type(Tree tree, Function<String, String> resolveType,
                               Map<String, Map<String, String>> generics) {
        String name = tree.toString();
        if (tree instanceof ParameterizedTypeTree) {
            return resolveType.apply(((ParameterizedTypeTree) tree).getType().toString());
        }
        return "?".equals(name) || generics.containsKey(name) ? name : resolveType.apply(name);
    }

    private static int integer(String declaration, String value) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException exception) { throw invalid(declaration, "Int32 常量不允许舍入或溢出"); }
    }

    private static Map<String, String> attributes(String declaration, String raw) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String token : raw.split("\\s+")) {
            String[] pair = token.split("=", -1);
            if (pair.length != 2 || pair[0].isEmpty() || pair[1].isEmpty()
                    || result.putIfAbsent(pair[0], pair[1]) != null) {
                throw invalid(declaration, "非法或重复属性: " + token);
            }
        }
        return result;
    }

    private static void keys(String declaration, Map<String, String> attrs, String... keys) {
        if (!attrs.keySet().equals(new java.util.HashSet<>(Arrays.asList(keys)))) {
            throw invalid(declaration, "属性必须为 " + Arrays.asList(keys));
        }
    }

    private static List<Object> sources(String declaration, List<String> sources) {
        List<Object> result = new ArrayList<>();
        for (String raw : sources) {
            String[] tokens = raw.split("\\s+", 3);
            if (tokens.length != 3 || !(tokens[0].startsWith("path=") || tokens[0].startsWith("reference="))
                    || !tokens[1].startsWith("symbols=") || !tokens[2].startsWith("mechanism=")) {
                throw invalid(declaration, "来源需要 path/reference、symbols、mechanism");
            }
            Map<String, Object> source = object("source", "JAVADOC", "tag", SOURCE, "value", raw);
            for (String token : tokens) {
                int separator = token.indexOf('=');
                String value = token.substring(separator + 1);
                if (value.isEmpty()) { throw invalid(declaration, "来源属性为空"); }
                String key = token.substring(0, separator);
                if ("reference".equals(key) && !URI.create(value).isAbsolute()) {
                    throw invalid(declaration, "外部来源必须为绝对 URI");
                }
                source.put(key, value);
            }
            if (result.contains(source)) { throw invalid(declaration, "重复来源"); }
            result.add(source);
        }
        if (result.isEmpty()) { throw invalid(declaration, "缺少当前声明的来源"); }
        return result;
    }

    private static Map<String, Object> node(String op, Object... pairs) {
        Map<String, Object> result = object(pairs);
        result.put("op", op);
        return result;
    }

    private static Map<String, Object> object(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) { result.put((String) pairs[i], pairs[i + 1]); }
        return result;
    }

    private static MissingEvidence missing(String reason) { return new MissingEvidence(reason); }

    private static IllegalArgumentException invalid(String declaration, String reason) {
        return new IllegalArgumentException("非法 @" + TAG + ": " + declaration + "，原因: " + reason);
    }

    private static final class MissingEvidence extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private MissingEvidence(String message) { super(message); }
    }
}
