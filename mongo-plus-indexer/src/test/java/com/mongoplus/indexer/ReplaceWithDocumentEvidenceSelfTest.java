package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** 条件化 Document 表达式域的完整树代入；不搜索或选择候选，也不推断未知 Java/Codec。 */
public final class ReplaceWithDocumentEvidenceSelfTest {
    private static final String DOCUMENT = "org.bson.Document";
    private static final String DOCUMENT_CODEC = "org.bson.codecs.DocumentCodec";
    private static final String BSON_DOCUMENT_CODEC = "org.bson.codecs.BsonDocumentCodec";
    private static final String EXPRESSION = "PIPELINE_EXPRESSION";
    private static final String RELATION = "PIPELINE_EXPRESSION -> PIPELINE_STAGE_DOCUMENT";
    private static final String AGGREGATE = "com.mongoplus.aggregate.Aggregate";
    private static MongoPlusApiIndex index;
    private static Constructor<?> expressionConstructor;
    private static Method proveExpression;
    private static int positive;
    private static int negative;

    private ReplaceWithDocumentEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]).toAbsolutePath().normalize();
        index = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).build()).generate();
        prepareIndependentExpressionProof();
        Map<String, Object> documentMethod = method(AGGREGATE, "replaceWith(Document value)");
        Map<String, Object> genericMethod = method(AGGREGATE, "replaceWith(TExpression fieldName)");
        Expression expression = expression("mergeObjects(Object... values)", "$defaults", "$profile");
        Route document = route(documentMethod, expression, DOCUMENT, null);
        Route generic = route(genericMethod, expression, "java.lang.Object", "java.lang.Object");
        equal(document, generic, "两条父候选完整调用树");
        equal(route(documentMethod, expression("mergeObjects(Collection<?> values)", "$defaults", "$profile"), DOCUMENT, null),
                generic, "子候选分别闭合后可代入父候选");
        Expression nested = expression("mergeObjects(Object... values)", "$defaults",
                expression("ifNull(Object... inputExpressions)", "$profile", "$defaults"));
        equal(route(documentMethod, nested, DOCUMENT, null), route(genericMethod, nested, "java.lang.Object", "java.lang.Object"),
                "递归 expression 构造和 runtime Codec 闭合");
        Expression typed = expression("mergeObjects(Object... values)", "$defaults", 1, 2147483648L, true);
        equal(route(documentMethod, typed, DOCUMENT, null), route(genericMethod, typed, "java.lang.Object", "java.lang.Object"),
                "BSON 文档/数组/整数宽度/布尔类型和值保留");
        Outcome result = evaluate(document);
        require(result.bson().equals(document("$replaceWith", document("$mergeObjects", array(List.of(
                scalar("STRING", "$defaults"), scalar("STRING", "$profile")))))), "有序带类型 BSON 精确结构");
        require(result.effects().equals(List.of(List.of("root", "APPEND_STAGE", "RECEIVER", "ONE", "CALL_ORDER"))),
                "一条 Stage 只追加至同一 receiver"); positive++;
        boundaries(document, generic);
        evidenceMutations(document, generic);
        fixtures(project);
        System.out.println("ReplaceWithDocumentEvidenceSelfTest PASSED: " + positive + " equivalence/structure checks; "
                + negative + " rejection/non-equivalence checks; concrete Java binding, exact runtime Document, Codec,"
                + " recursive typed ordered BSON and receiver effects; no candidate selection");
    }

    private record Expression(Map<String, Object> method, List<Object> operands, boolean codecsProven, String scope) { }
    /** 这些是消费者必须独立提供的调用点证明，不能由 runtimeClass 或相同 BSON 反推。 */
    private record Route(Map<String, Object> method, Object input, String sourceType, String staticType,
                         String genericInstantiation, String actualSignature, boolean actualOverloadProven,
                         boolean documentSourceProven, String runtimeClass, String documentCodec,
                         boolean codecsProven, String codecContext, String snapshot, String receiver, String bsonDocumentCodec) {
        private Route(Map<String, Object> method, Object input, String sourceType, String staticType,
                      String genericInstantiation, String actualSignature, boolean actualOverloadProven,
                      boolean documentSourceProven, String runtimeClass, String documentCodec,
                      boolean codecsProven, String codecContext, String snapshot, String receiver) {
            this(method, input, sourceType, staticType, genericInstantiation, actualSignature, actualOverloadProven,
                    documentSourceProven, runtimeClass, documentCodec, codecsProven, codecContext, snapshot, receiver, BSON_DOCUMENT_CODEC);
        }
    }
    private record Outcome(Object bson, List<Object> effects, List<Object> scopes, String codecContext, String snapshot) { }

    private static Expression expression(String signature, Object... values) {
        return new Expression(method("com.mongoplus.conditions.operation.ConditionOperators", signature), Arrays.asList(values), true, "scopeA");
    }

    private static Route route(Map<String, Object> method, Object input, String staticType, String generic) {
        return new Route(method, input, DOCUMENT, staticType, generic, String.valueOf(method.get("signature")), true,
                true, DOCUMENT, DOCUMENT_CODEC, true, "DRIVER_5_4_DEFAULT_SAME_CONTEXT", "snapshotA", "root");
    }

    private static Route change(Route route, Map<String, Object> method) {
        return new Route(method, route.input(), route.sourceType(), route.staticType(), route.genericInstantiation(),
                route.actualSignature(), route.actualOverloadProven(), route.documentSourceProven(), route.runtimeClass(),
                route.documentCodec(), route.codecsProven(), route.codecContext(), route.snapshot(), route.receiver(), route.bsonDocumentCodec());
    }

    private static void boundaries(Route document, Route generic) {
        for (Object input : Arrays.asList("ordinary", "$profile", new Object(), Map.of("$mergeObjects", List.of("$profile")), null)) {
            refuses(route(document.method(), input, DOCUMENT, null), "String/raw BSON/未知对象/null 不属于 Document expression 证明域");
            refuses(route(generic.method(), input, "java.lang.Object", "java.lang.Object"), "泛型不扩大未证明的输入域");
        }
        for (String runtime : List.of("org.bson.conversions.Bson", "java.lang.String", "java.lang.Object", "custom.DocumentSubclass")) {
            refuses(new Route(generic.method(), generic.input(), DOCUMENT, "java.lang.Object", "java.lang.Object", generic.actualSignature(), true,
                    true, runtime, DOCUMENT_CODEC, true, generic.codecContext(), generic.snapshot(), "root"), "runtime 必须为精确 Document 类");
        }
        for (String codec : List.of("UNKNOWN", "custom.DocumentCodec", "org.bson.codecs.BsonDocumentCodec")) {
            refuses(new Route(generic.method(), generic.input(), DOCUMENT, "java.lang.Object", "java.lang.Object", generic.actualSignature(), true,
                    true, DOCUMENT, codec, true, generic.codecContext(), generic.snapshot(), "root"), "Codec 类型必须独立闭合");
        }
        refuses(new Route(generic.method(), generic.input(), DOCUMENT, "java.lang.Object", "java.lang.Object", generic.actualSignature(), true,
                true, DOCUMENT, DOCUMENT_CODEC, true, generic.codecContext(), generic.snapshot(), "root", "UNKNOWN"), "BuildersHelper 中间 BsonDocumentCodec 必须闭合");
        refuses(new Route(generic.method(), generic.input(), DOCUMENT, "java.lang.Object", "java.lang.Object", generic.actualSignature(), true,
                true, DOCUMENT, DOCUMENT_CODEC, false, generic.codecContext(), generic.snapshot(), "root"), "同 registry 不能代替每个叶子 Codec 证明");
        for (String staticType : List.of("java.lang.String", "org.bson.conversions.Bson", "unknown.E")) {
            refuses(route(generic.method(), generic.input(), staticType, staticType), "runtime Document 不能反推静态类型或未知泛型");
        }
        refuses(route(document.method(), document.input(), "java.lang.Object", null), "Object 静态实参不能绑定 Document overload");
        refuses(route(generic.method(), generic.input(), DOCUMENT, "java.lang.Object"), "泛型实例化和实参类型独立匹配");
        refuses(route(generic.method(), generic.input(), DOCUMENT, null), "泛型实例化缺失");
        refuses(new Route(generic.method(), generic.input(), "java.lang.Object", "java.lang.Object", "java.lang.Object",
                generic.actualSignature(), true, false, DOCUMENT, DOCUMENT_CODEC, true, generic.codecContext(), generic.snapshot(), "root"),
                "Object widening 缺独立 Document 来源");
        refuses(new Route(generic.method(), generic.input(), DOCUMENT, DOCUMENT, DOCUMENT, document.actualSignature(), true,
                true, DOCUMENT, DOCUMENT_CODEC, true, generic.codecContext(), generic.snapshot(), "root"), "实际调用错 overload");
        refuses(new Route(generic.method(), generic.input(), DOCUMENT, "java.lang.Object", "java.lang.Object", generic.actualSignature(), false,
                true, DOCUMENT, DOCUMENT_CODEC, true, generic.codecContext(), generic.snapshot(), "root"), "缺真实调用点证明");
        different(document, new Route(generic.method(), generic.input(), DOCUMENT, "java.lang.Object", "java.lang.Object", generic.actualSignature(), true,
                true, DOCUMENT, DOCUMENT_CODEC, true, generic.codecContext(), generic.snapshot(), "sibling"), "不同 receiver");
        different(document, new Route(generic.method(), generic.input(), DOCUMENT, "java.lang.Object", "java.lang.Object", generic.actualSignature(), true,
                true, DOCUMENT, DOCUMENT_CODEC, true, "ANOTHER_CONTEXT", generic.snapshot(), "root"), "不同编码上下文");
        different(document, new Route(generic.method(), generic.input(), DOCUMENT, "java.lang.Object", "java.lang.Object", generic.actualSignature(), true,
                true, DOCUMENT, DOCUMENT_CODEC, true, generic.codecContext(), "snapshotB", "root"), "不同输入快照");
        different(document, route(generic.method(), expression("mergeObjects(Object... values)", "$profile", "$defaults"), "java.lang.Object", "java.lang.Object"),
                "数组顺序不可交换");
        different(route(document.method(), expression("mergeObjects(Object... values)", "$profile", 1), DOCUMENT, null),
                route(generic.method(), expression("mergeObjects(Object... values)", "$profile", 1L), "java.lang.Object", "java.lang.Object"), "INT32 与 INT64 不等价");
        Expression variable = expression("mergeObjects(Object... values)", "$$bound", "$profile");
        equal(route(document.method(), variable, DOCUMENT, null), route(generic.method(), variable, "java.lang.Object", "java.lang.Object"), "同一变量作用域");
        different(route(document.method(), variable, DOCUMENT, null), route(generic.method(),
                new Expression(variable.method(), variable.operands(), true, "scopeB"), "java.lang.Object", "java.lang.Object"), "相同变量文本不能跨作用域");
        refuses(route(generic.method(), expression("mergeObjects(Object... values)", "$$missing", "$profile"), "java.lang.Object", "java.lang.Object"), "未绑定变量");
        refuses(route(generic.method(), new Expression(variable.method(), variable.operands(), true, ""), "java.lang.Object", "java.lang.Object"), "变量作用域证据缺失");
        require(!List.of(evaluate(document), evaluate(document)).equals(List.of(evaluate(generic))), "Stage 数量不可压缩"); negative++;
        Route other = route(document.method(), expression("mergeObjects(Object... values)", "$other", "$profile"), DOCUMENT, null);
        require(!List.of(evaluate(document), evaluate(other)).equals(List.of(evaluate(other), evaluate(generic))), "Stage 调用顺序不可交换"); negative++;
    }

    private static void evidenceMutations(Route document, Route generic) {
        for (Route route : List.of(document, generic)) {
            for (String key : List.of("candidateSemantics", "pipelineEffect", "compositionSemantics", "resultSemanticType", "resultSemanticEvidence", "mongoStages")) {
                mutate(route, changed -> changed.remove(key), "父缺 " + key);
            }
            for (String key : List.of("sourceEvidence", "bsonTerm", "applicability", "parameterBindings", "receiverEffect", "receiverIdentity", "resultSemanticType")) {
                mutate(route, changed -> map(changed.get("candidateSemantics")).remove(key), "构造缺 " + key);
            }
            for (String key : List.of("codec", "codecContext", "evaluation", "javaBinding", "bindingAdmission", "allArgumentsConsumed", "allChildrenRequireIndependentEvidence", "documentExpressionDomain")) {
                mutate(route, changed -> applicability(changed).remove(key), "约束缺 " + key);
            }
            for (String key : List.of("parameter", "sourceJavaType", "runtimeJavaType", "runtimeTypeMatch", "requiredDocumentCodec", "requiredBsonDocumentCodec", "childEvidence", "javaBinding", "bsonType", "nullInput", "unprovedTypes", "encoding")) {
                mutate(route, changed -> map(applicability(changed).get("documentExpressionDomain")).remove(key), "Document 域缺 " + key);
            }
            for (String key : List.of("semanticType", "semanticScope", "semanticEvidence", "conceptRef", "type")) {
                mutate(route, changed -> parameters(changed).get(0).remove(key), "参数缺 " + key);
            }
            mutate(route, changed -> parameters(changed).get(0).put("semanticScope", "ELEMENT"), "错误 scope");
            mutate(route, changed -> map(map(changed.get("candidateSemantics")).get("bsonTerm")).put("key", "$other"), "Stage 名与独立 mapping 不匹配");
            mutate(route, changed -> map(changed.get("pipelineEffect")).put("count", "TWO"), "effect 数量错误");
            mutate(route, changed -> map(changed.get("pipelineEffect")).put("target", "OTHER"), "effect 目标错误");
            mutate(route, changed -> map(changed.get("pipelineEffect")).put("order", "REVERSED"), "effect 顺序错误");
            mutate(route, changed -> map(map(changed.get("candidateSemantics")).get("receiverEffect")).put("operation", "NONE"), "候选与独立 effect 不一致");
            mutate(route, changed -> map(map(changed.get("candidateSemantics")).get("bsonTerm")).put("op", "ARRAY"), "错误 BSON 类型");
        }
        Expression expression = (Expression) generic.input();
        for (String key : List.of("candidateSemantics", "resultSemanticType", "resultSemanticEvidence", "compositionSemantics", "expressionShape")) {
            Map<String, Object> changed = copy(expression.method()); changed.remove(key);
            refuses(route(generic.method(), new Expression(changed, expression.operands(), true, "scopeA"), "java.lang.Object", "java.lang.Object"), "子节点缺 " + key);
        }
        Map<String, Object> changed = copy(expression.method());
        map(changed.get("candidateSemantics")).remove("sourceEvidence");
        refuses(route(generic.method(), new Expression(changed, expression.operands(), true, "scopeA"), "java.lang.Object", "java.lang.Object"), "子源码证据缺失");
        refuses(route(generic.method(), new Expression(expression.method(), expression.operands(), false, "scopeA"), "java.lang.Object", "java.lang.Object"), "子 Codec 缺失");
        refuses(route(generic.method(), expression("mergeObjects(Object... values)", new Object(), "$profile"), "java.lang.Object", "java.lang.Object"), "深层未知类型/Codec");
    }

    private static void mutate(Route route, Consumer<Map<String, Object>> mutation, String message) {
        Map<String, Object> changed = copy(route.method()); mutation.accept(changed); refuses(change(route, changed), message);
    }

    private static Outcome evaluate(Route route) {
        Map<String, Object> method = route.method();
        Map<String, Object> candidate = map(method.get("candidateSemantics"));
        requireEvidence(candidate != null && "CANDIDATE_SEMANTICS_V1".equals(candidate.get("capability"))
                && "ESTABLISHED".equals(candidate.get("proofStatus")) && "RAW_BSON_ORDERED_TYPED".equals(candidate.get("comparison")), "正式构造证据");
        requireEvidence(Boolean.FALSE.equals(method.get("staticMethod")) && "PIPELINE_STAGE_DOCUMENT".equals(method.get("resultSemanticType"))
                && List.of(RELATION).equals(method.get("compositionSemantics"))
                && Map.of("source", "JAVADOC", "tag", "mongoComposition", "value", RELATION).equals(method.get("resultSemanticEvidence")), "显式 Stage composition/result");
        requireEvidence(candidate.get("sourceEvidence") instanceof List<?> sources
                && sources.stream().anyMatch(value -> map(value).containsKey("path"))
                && sources.stream().anyMatch(value -> map(value).containsKey("reference")), "独立 Core 和 Driver 来源");
        List<Map<String, Object>> parameters = parameters(method);
        requireEvidence(parameters.size() == 1, "单一参数");
        Map<String, Object> parameter = parameters.get(0);
        requireEvidence(EXPRESSION.equals(parameter.get("semanticType")) && "VALUE".equals(parameter.get("semanticScope"))
                && "PIPELINE_EXPRESSION_FIELD_REFERENCE".equals(parameter.get("conceptRef"))
                && Map.of("source", "JAVADOC", "tag", "mongoParam", "value", parameter.get("name") + " PIPELINE_EXPRESSION VALUE")
                    .equals(parameter.get("semanticEvidence")), "独立参数语义及作用域");
        Map<String, Object> applicability = map(candidate.get("applicability"));
        requireEvidence(applicability != null
                && Boolean.TRUE.equals(applicability.get("allArgumentsConsumed"))
                && Boolean.TRUE.equals(applicability.get("allChildrenRequireIndependentEvidence"))
                && "ACTUAL_CALL_SITE_TYPES_MUST_MATCH_PARAMETER_BINDINGS".equals(applicability.get("javaBinding"))
                && "INDEPENDENT_SEMANTIC_AND_SCOPE_VALIDATION".equals(applicability.get("bindingAdmission"))
                && "AUDITED_DRIVER_5_4_DEFAULT_CODECS_OR_INDEPENDENT_EXACT_ENCODING_PROOF".equals(applicability.get("codec"))
                && "SAME_REGISTRY_DOCUMENT_CLASS_ENCODER_CONTEXT_AND_DETERMINISTIC_ENCODING".equals(applicability.get("codecContext"))
                && "SAME_IMMUTABLE_INPUT_SNAPSHOT".equals(applicability.get("evaluation")), "全局 Java/Codec/闭包/快照约束");
        Map<String, Object> expectedDomain = object("parameter", parameter.get("name"), "sourceJavaType", DOCUMENT,
                "runtimeJavaType", DOCUMENT, "runtimeTypeMatch", "EXACT_CLASS", "requiredDocumentCodec", DOCUMENT_CODEC,
                "requiredBsonDocumentCodec", BSON_DOCUMENT_CODEC,
                "childEvidence", "ESTABLISHED_PIPELINE_EXPRESSION_RESULT_AND_COMPLETE_CALL_TREE",
                "javaBinding", "VERIFY_ACTUAL_OVERLOAD_AND_CONCRETE_GENERIC_INSTANTIATION_WITH_DOCUMENT_SOURCE",
                "bsonType", "DOCUMENT", "nullInput", "FORBID", "unprovedTypes", "FORBID", "encoding", "BUILDERS_HELPER_CODEC_RUNTIME_DOCUMENT");
        requireEvidence(expectedDomain.equals(applicability.get("documentExpressionDomain")), "Document 输入域不能扩大");
        requireEvidence(route.actualOverloadProven() && route.documentSourceProven() && DOCUMENT.equals(route.sourceType())
                && method.get("signature").equals(route.actualSignature()) && DOCUMENT.equals(route.runtimeClass())
                && DOCUMENT_CODEC.equals(route.documentCodec()) && BSON_DOCUMENT_CODEC.equals(route.bsonDocumentCodec()) && route.codecsProven()
                && route.codecContext() != null && route.snapshot() != null && route.receiver() != null, "独立调用点/运行类/Codec/context/receiver 证明");
        List<Map<String, Object>> bindings = maps(candidate.get("parameterBindings"));
        requireEvidence(bindings.size() == 1, "完整参数绑定");
        Map<String, Object> binding = bindings.get(0);
        requireEvidence(parameter.get("name").equals(binding.get("parameter")) && parameter.get("type") != null
                && parameter.get("type").equals(binding.get("declaredJavaType"))
                && EXPRESSION.equals(binding.get("semanticType")) && "VALUE".equals(binding.get("semanticScope"))
                && parameter.get("semanticEvidence").equals(binding.get("semanticEvidence")) && "SINGLE".equals(binding.get("invocation")), "实际参数绑定一致");
        Map<String, Object> representation = map(binding.get("javaRepresentation"));
        requireEvidence(representation != null && "SCALAR".equals(representation.get("container")), "标量 Java 表示");
        Object javaType = representation.get("javaType");
        if (DOCUMENT.equals(javaType)) {
            requireEvidence(DOCUMENT.equals(route.staticType()) && route.genericInstantiation() == null, "真实 Document overload 静态绑定");
        } else {
            Map<String, Object> generic = map(representation.get("genericDeclaration"));
            requireEvidence(generic != null && javaType.equals(generic.get("declaration"))
                    && List.of(DOCUMENT, "java.lang.Object").contains(route.staticType())
                    && route.staticType().equals(route.genericInstantiation()), "真实无界方法泛型及具体实例化");
        }
        Map<String, Object> effect = map(method.get("pipelineEffect"));
        requireEvidence(effect != null && "APPEND_STAGE".equals(effect.get("operation")) && "RECEIVER".equals(effect.get("target"))
                && "ONE".equals(effect.get("count")) && "CALL_ORDER".equals(effect.get("order")) && effect.get("sourceEvidence") instanceof Map<?, ?>
                && effect.equals(candidate.get("receiverEffect")) && "SYMBOLIC_CALL_SITE_RECEIVER".equals(candidate.get("receiverIdentity"))
                && "PIPELINE_STAGE_DOCUMENT".equals(candidate.get("resultSemanticType")), "独立 receiver effect 和候选 effect 完全一致");
        Map<String, Object> term = map(candidate.get("bsonTerm"));
        requireEvidence(term != null && "DOCUMENT".equals(term.get("op")) && List.of(term.get("key")).equals(method.get("mongoStages")), "完整 Stage 文档及 mapping");
        Map<String, Object> value = map(term.get("value"));
        requireEvidence(value != null && "BUILDERS_HELPER_CODEC".equals(value.get("op")) && value.get("input") instanceof Map<?, ?> input
                && "INPUT".equals(input.get("op")) && parameter.get("name").equals(input.get("parameter")), "明确 Driver 编码路径");
        requireEvidence(route.input() instanceof Expression, "仅接受独立 expression 结果及完整调用树");
        Expression expression = (Expression) route.input();
        requireEvidence(prove(expression), "独立 expression Java/semantic/Codec/scope 闭合");
        List<Object> scopes = new ArrayList<>();
        Object bson = document(term.get("key").toString(), expressionBson(expression, scopes));
        return new Outcome(bson, List.of(List.of(route.receiver(), effect.get("operation"), effect.get("target"), effect.get("count"), effect.get("order"))),
                scopes, route.codecContext(), route.snapshot());
    }

    /** 借用已有独立 expression 证明器，避免把父级运行类型事实当作子节点语义或 Codec 证明。 */
    private static void prepareIndependentExpressionProof() throws Exception {
        Field field = ExpressionResultTypeEvidenceSelfTest.class.getDeclaredField("index"); field.setAccessible(true); field.set(null, index);
        Class<?> call = Class.forName(ExpressionResultTypeEvidenceSelfTest.class.getName() + "$Call");
        expressionConstructor = call.getDeclaredConstructor(Map.class, List.class, boolean.class); expressionConstructor.setAccessible(true);
        proveExpression = ExpressionResultTypeEvidenceSelfTest.class.getDeclaredMethod("prove", call); proveExpression.setAccessible(true);
    }

    private static Object independentCall(Expression expression) throws ReflectiveOperationException {
        List<Object> operands = new ArrayList<>();
        for (Object operand : expression.operands()) { operands.add(operand instanceof Expression child ? independentCall(child) : operand); }
        return expressionConstructor.newInstance(expression.method(), operands, expression.codecsProven());
    }

    private static boolean prove(Expression expression) {
        try { return Boolean.TRUE.equals(proveExpression.invoke(null, independentCall(expression))); }
        catch (ReflectiveOperationException | RuntimeException expected) { return false; }
    }

    private static Object expressionBson(Expression expression, List<Object> scopes) {
        Map<String, Object> term = map(map(expression.method().get("candidateSemantics")).get("bsonTerm"));
        Map<String, Object> value = map(term.get("value"));
        String op = value.get("op").toString();
        List<Object> encoded = new ArrayList<>();
        for (Object operand : expression.operands()) {
            if (operand instanceof Expression child) { encoded.add(expressionBson(child, scopes)); }
            else if (operand == null) { encoded.add(scalar("NULL", "BSON_NULL")); }
            else {
                if (operand instanceof String text && text.startsWith("$$")) {
                    requireEvidence(expression.scope() != null && !expression.scope().isEmpty(), "独立变量作用域"); scopes.add(List.of(text, expression.scope()));
                }
                String type = operand instanceof String ? "STRING" : operand instanceof Integer ? "INT32"
                        : operand instanceof Long ? "INT64" : operand instanceof Double ? "DOUBLE" : operand instanceof Boolean ? "BOOLEAN" : "UNKNOWN";
                requireEvidence(!"UNKNOWN".equals(type), "叶子 BSON 类型及 Codec"); encoded.add(scalar(type, operand));
            }
        }
        Object body = switch (op) {
            case "ARRAY_RUNTIME_CODEC", "ARRAY_ARGUMENTS_RUNTIME_CODEC" -> array(encoded);
            case "RUNTIME_CODEC_VALUE" -> encoded.get(0);
            case "ORDERED_DOCUMENT_ARGUMENTS" -> {
                List<Object> entries = new ArrayList<>(); List<?> declared = (List<?>) value.get("entries");
                for (int i = 0; i < declared.size(); i++) { entries.add(List.of(map(declared.get(i)).get("key"), encoded.get(i))); }
                yield List.of("DOCUMENT", entries);
            }
            default -> throw new IllegalArgumentException("未知 expression 构造项");
        };
        return document(term.get("key").toString(), body);
    }

    private static void fixtures(Path project) throws Exception {
        Path root = Files.createTempDirectory("stage-expression-document-");
        try {
            Path file = root.resolve("com/mongoplus/aggregate/Aggregate.java"); Files.createDirectories(file.getParent());
            String source = "package com.mongoplus.aggregate; import org.bson.Document; public class Aggregate<C> {\n/**\n"
                    + " * @mongoStage $neutralStage\n * @mongoParam alpha PIPELINE_EXPRESSION VALUE\n"
                    + " * @mongoComposition PIPELINE_EXPRESSION -> PIPELINE_STAGE_DOCUMENT\n"
                    + " * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER\n"
                    + " * @mongoCandidate operation=STAGE_EXPRESSION_DOCUMENT runtimeJava=org.bson.Document runtimeCodec=org.bson.codecs.DocumentCodec bsonDocumentCodec=org.bson.codecs.BsonDocumentCodec\n"
                    + " * @mongoCandidateSource path=com/mongoplus/aggregate/Aggregate.java symbols=assemble mechanism=explicitCoreDelegate\n"
                    + " * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/BuildersHelper.java symbols=encodeValue mechanism=auditedRuntimeDocument\n"
                    + " */ public <E> Aggregate<C> assemble(E alpha) { return this; } }";
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder().addSourceRoot(root).pipeline(true)
                    .constructionArtifactRepository(MongoPlusIndexerConfig.forPipelineProject(project).build().getConstructionArtifactRepository()).build());
            Files.writeString(file, source);
            requireEstablished(fixtureMethod(generator.generate()), true, "中性方法/operator/参数/泛型名称复用通用契约");
            Files.writeString(file, source.replace("<E> Aggregate<C> assemble(E alpha)", "Aggregate<C> assemble(Document alpha)"));
            requireEstablished(fixtureMethod(generator.generate()), true, "中性 Document 静态参数");
            for (String removed : List.of("@mongoParam alpha PIPELINE_EXPRESSION VALUE",
                    "@mongoComposition PIPELINE_EXPRESSION -> PIPELINE_STAGE_DOCUMENT",
                    "@mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER")) {
                Files.writeString(file, source.replace(removed, ""));
                requireEstablished(fixtureMethod(generator.generate()), false, "真实源码缺独立依赖不生成构造项");
            }
            for (String invalid : List.of(source.replace("<E>", "<E extends Number>"), source.replace("assemble(E alpha)", "assemble(String alpha)"),
                    source.replace("assemble(E alpha)", "assemble(org.bson.conversions.Bson alpha)"),
                    source.replace("assemble(E alpha)", "assemble(Object alpha)"), source.replace("runtimeJava=org.bson.Document", "runtimeJava=java.lang.Object"),
                    source.replace("runtimeCodec=org.bson.codecs.DocumentCodec", "runtimeCodec=unknown.Codec"),
                    source.replace("bsonDocumentCodec=org.bson.codecs.BsonDocumentCodec", "bsonDocumentCodec=unknown.Codec"))) {
                Files.writeString(file, invalid); boolean refused = false;
                try { refused = !established(fixtureMethod(generator.generate())); } catch (IllegalArgumentException expected) { refused = true; }
                require(refused, "错误 Java 输入/有界泛型/未知 runtime/Codec 声明拒绝"); negative++;
            }
            for (String removed : List.of(" * @mongoCandidateSource path=com/mongoplus/aggregate/Aggregate.java symbols=assemble mechanism=explicitCoreDelegate\n",
                    " * @mongoCandidateSource reference=https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/BuildersHelper.java symbols=encodeValue mechanism=auditedRuntimeDocument\n")) {
                Files.writeString(file, source.replace(removed, "")); boolean refused = false;
                try { refused = !established(fixtureMethod(generator.generate())); } catch (IllegalArgumentException expected) { refused = true; }
                require(refused, "Core/Driver 缺一不能形成正式候选证明"); negative++;
            }
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static Map<String, Object> fixtureMethod(MongoPlusApiIndex fixture) {
        return fixture.list("types").stream().map(ReplaceWithDocumentEvidenceSelfTest::map)
                .flatMap(type -> maps(type.get("publicMethods")).stream()).filter(method -> "assemble".equals(method.get("name"))).findFirst().orElseThrow();
    }
    private static boolean established(Map<String, Object> method) { return method.get("candidateSemantics") instanceof Map<?, ?> value && "ESTABLISHED".equals(value.get("proofStatus")); }
    private static void requireEstablished(Map<String, Object> method, boolean expected, String message) {
        require(established(method) == expected, message);
        if (expected) { positive++; } else { require(!map(method.get("candidateSemantics")).containsKey("bsonTerm"), "拒绝后不能留下部分构造项"); negative++; }
    }
    private static Map<String, Object> method(String owner, String signature) {
        return index.getMethodFamilies().stream().map(ReplaceWithDocumentEvidenceSelfTest::map)
                .flatMap(family -> maps(family.get("overloads")).stream())
                .filter(method -> owner.equals(method.get("declaredIn")) && signature.equals(method.get("signature"))).findFirst().orElseThrow();
    }
    private static List<Map<String, Object>> parameters(Map<String, Object> method) { return maps(method.get("parameters")); }
    private static Map<String, Object> applicability(Map<String, Object> method) { return map(map(method.get("candidateSemantics")).get("applicability")); }
    private static List<Map<String, Object>> maps(Object raw) { return ((List<?>) raw).stream().map(ReplaceWithDocumentEvidenceSelfTest::map).toList(); }
    @SuppressWarnings("unchecked") private static Map<String, Object> map(Object raw) { return (Map<String, Object>) raw; }
    @SuppressWarnings("unchecked") private static <T> T copy(T value) {
        if (value instanceof Map<?, ?> values) { Map<String, Object> result = new LinkedHashMap<>(); values.forEach((key, item) -> result.put(key.toString(), copy(item))); return (T) result; }
        if (value instanceof List<?> values) { return (T) new ArrayList<>(values.stream().map(ReplaceWithDocumentEvidenceSelfTest::copy).toList()); }
        return value;
    }
    private static Map<String, Object> object(Object... values) { Map<String, Object> result = new LinkedHashMap<>(); for (int i = 0; i < values.length; i += 2) { result.put(values[i].toString(), values[i + 1]); } return result; }
    private static Object document(String key, Object value) { return List.of("DOCUMENT", List.of(List.of(key, value))); }
    private static Object array(List<Object> values) { return List.of("ARRAY", values); }
    private static Object scalar(String type, Object value) { return List.of(type, value); }
    private static void equal(Route left, Route right, String message) { require(evaluate(left).equals(evaluate(right)), message); positive++; }
    private static void different(Route left, Route right, String message) { require(!evaluate(left).equals(evaluate(right)), message); negative++; }
    private static void refuses(Route route, String message) {
        boolean refused = false; try { evaluate(route); } catch (IllegalArgumentException | NullPointerException expected) { refused = true; }
        require(refused, message); negative++;
    }
    private static void requireEvidence(boolean valid, String reason) { if (!valid) { throw new IllegalArgumentException(reason); } }
    private static void require(boolean valid, String message) { if (!valid) { throw new AssertionError(message); } }
}
