package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 消费正式 evidence 验证保序与合法绑定；不实现生产 Resolver 或多候选选择。 */
public final class PipelineSortProjectionEvidenceSelfTest {
    private static final String PROJECTIONS = "com.mongoplus.aggregate.pipeline.Projections";
    private static final String SORTS = "com.mongoplus.aggregate.pipeline.Sorts";
    private static final String AGGREGATE = "com.mongoplus.aggregate.Aggregate";
    private static final String ENTRY = "@mongoDocumentEntry key=keys value=int32:1 result=SORT_SPECIFICATION target=RESULT order=INPUT duplicatePosition=FIRST";
    private static final String SOURCE = "@mongoDocumentSource mongoDocumentEntry path=fixture/Factory.java symbols=build mechanism=显式固定Int32键值编码";
    private static int passed;

    private PipelineSortProjectionEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        MongoPlusApiIndex real = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(Path.of(args[0])).build()).generate();
        Map<?, ?> project = method(real, AGGREGATE, "project(Bson bson)");
        Map<?, ?> policy = (Map<?, ?>) project.get("documentModePolicy");
        require(policy != null && Boolean.FALSE.equals(policy.get("coreRuntimeValidationImplied")), "规则由消费者执行，Core 未校验模式");
        require(project.containsKey("pipelineEffect") && ((Map<?, ?>) parameters(project).get(0)).containsKey("documentInputBinding"), "独立包装和Stage effect");
        require(method(real, AGGREGATE, "sort(Bson bson)").get("mongoStages").equals(List.of()), "透传不能借用同名sort映射");
        Map<?, ?> sortInput = (Map<?, ?>) ((Map<?, ?>) parameters(method(real, AGGREGATE, "sort(Bson bson)")).get(0)).get("documentInputBinding");
        require("PIPELINE_STAGE_DOCUMENT".equals(sortInput.get("semanticType"))
                && "NOT_ESTABLISHED".equals(sortInput.get("bodyToStageConstruction")), "旧透传入口不能自动包装排序body");
        Map<?, ?> sortStage = method(real, AGGREGATE, "sortSpecification(Bson specification)");
        Map<?, ?> composition = (Map<?, ?>) ((Map<?, ?>) parameters(sortStage).get(0)).get("documentInputBinding");
        require(sortStage.get("mongoStages").equals(List.of("$sort")) && sortStage.containsKey("pipelineEffect")
                && "SORT_SPECIFICATION".equals(composition.get("semanticType"))
                && "DECLARED_STAGE_WRAPPER".equals(composition.get("bodyToStageConstruction")),
                "独立入口建立body composition和Stage effect，不能借给旧透传入口");
        Map<?, ?> capability = maps(real.list("concepts")).stream().filter(c -> "DOCUMENT_SHAPE_V1".equals(c.get("id"))).findFirst().orElseThrow();
        require(Boolean.FALSE.equals(capability.get("methodOrderIsEvidence")), "顺序不能选择候选");
        for (String owner : List.of(SORTS, PROJECTIONS)) {
            long count = methods(real, owner).stream().filter(m -> m.containsKey("documentEntryConstruction")).count();
            require(count == (SORTS.equals(owner) ? 8 : 11), "String/getter/List/varargs均保留独立证据: " + owner);
            for (Map<?, ?> method : methods(real, owner)) {
                if (!method.containsKey("documentEntryConstruction")) { continue; }
                Map<?, ?> construction = construction(method);
                require("INPUT".equals(construction.get("order")) && construction.containsKey("sourceEvidence"), "字段顺序独立来源");
                if (construction.containsKey("typedContainer")) {
                    Map<?, ?> container = (Map<?, ?>) construction.get("typedContainer");
                    require(container.containsKey("elementJavaType") && container.containsKey("invocation"), "真实typed容器和调用形态");
                }
                passed++;
            }
        }
        List<Map<?, ?>> sortReducers = methods(real, SORTS).stream().filter(m -> m.containsKey("reductionContract")).toList();
        List<Map<?, ?>> projectReducers = methods(real, PROJECTIONS).stream().filter(m -> m.containsKey("reductionContract")).toList();
        require(sortReducers.size() == 2 && projectReducers.size() == 2, "保留两个List/varargs归约，不选第一个");
        Map<?, ?> desc = method(real, SORTS, "desc(String... fieldNames)");
        Map<?, ?> asc = method(real, SORTS, "asc(String... fieldNames)");
        require(fixedMatches(asc, 1) && fixedMatches(desc, -1) && !fixedMatches(asc, -1)
                && !fixedMatches(desc, 1), "每个字段只能绑定自己的固定方向，不能整份文档共用一个方向"); passed++;
        for (Object invalid : List.of(0, 2, -2, new BigDecimal("0.5"), true, "1")) {
            require(!fixedMatches(asc, invalid) && !fixedMatches(desc, invalid), "方向必须匹配来源固定Int32，不能按数字或bool猜: " + invalid); passed++;
        }
        for (Map<?, ?> reducer : sortReducers) {
            LinkedHashMap<String, Object> sorted = reduce(reducer, List.of(entry(desc, List.of("createTime"), null),
                    entry(asc, List.of("score"), null), entry(desc, List.of("id"), null)));
            require(new ArrayList<>(sorted.keySet()).equals(List.of("createTime", "score", "id"))
                    && new ArrayList<>(sorted.values()).equals(List.of(-1, 1, -1)), "三字段独立方向与原序");
            rejects(() -> reduce(reducer, List.of(entry(asc, List.of("score"), null), entry(desc, List.of("score"), null))), "跨entry重键不能静默覆盖输入");
            passed++;
        }
        Map<?, ?> include = method(real, PROJECTIONS, "include(String... fieldNames)");
        Map<?, ?> exclude = method(real, PROJECTIONS, "exclude(String... fieldNames)");
        Map<?, ?> computed = method(real, PROJECTIONS, "computed(String fieldName, TExpression expression)");
        Map<?, ?> excludeId = method(real, PROJECTIONS, "excludeId()");
        require(computed.containsKey("compositionSemantics") && ((Map<?, ?>) computed.get("resultSemanticEvidence")).get("tag").equals("mongoComposition"), "原computed composition/result来源保留");
        Map<?, ?> multiply = familyMethod(real, "multiply(Object... values)");
        require(multiply.get("mongoExpressions").equals(List.of("$multiply"))
                && ((Map<?, ?>) parameters(multiply).get(0)).get("semanticScope").equals("ELEMENT"),
                "嵌套表达式保留原operator/operand evidence，未补P0-06 result契约");
        Object expression = Map.of("$multiply", List.of("$price", "$quantity"));
        for (Map<?, ?> reducer : projectReducers) {
            LinkedHashMap<String, Object> included = reduce(reducer, List.of(entry(include, List.of("name", "age"), null), entry(excludeId, List.of(), null)));
            require("INCLUSION".equals(mode(policy, included)) && new ArrayList<>(included.keySet()).equals(List.of("name", "age", "_id")), "include加_id例外");
            LinkedHashMap<String, Object> excluded = entry(exclude, List.of("name", "phone"), null);
            require("EXCLUSION".equals(mode(policy, excluded)) && new ArrayList<>(excluded.keySet()).equals(List.of("name", "phone")), "exclude-only原序");
            LinkedHashMap<String, Object> combination = reduce(reducer, List.of(entry(include, List.of("name"), null),
                    entry(computed, List.of("total"), expression), entry(excludeId, List.of(), null)));
            require("INCLUSION".equals(mode(policy, combination)) && new ArrayList<>(combination.keySet()).equals(List.of("name", "total", "_id")), "include/computed/_id合法");
            require(expression.equals(combination.get("total")), "expression不丢失不改写");
            LinkedHashMap<String, Object> invalid = reduce(reducer, List.of(entry(include, List.of("name"), null), entry(exclude, List.of("phone"), null)));
            rejects(() -> mode(policy, invalid), "普通include/exclude混合拒绝");
            require(new ArrayList<>(invalid.values()).equals(List.of(1, 0)), "拒绝后不改写输入");
            rejects(() -> mode(policy, ordered("total", expression, "phone", 0)), "computed/exclude混合拒绝");
            rejects(() -> mode(policy, ordered("_id", expression, "phone", 0)), "_id computed不是flag例外");
            rejects(() -> reduce(reducer, List.of(entry(include, List.of("name"), null), entry(exclude, List.of("name"), null))), "投影归约前拒绝重键");
            require("INCLUSION".equals(mode(policy, ordered("total", Map.of("$multiply", List.of(1, "$price"))))), "嵌套数字operand不是顶层投影标志");
            passed += 5;
        }
        for (Object number : List.of(1, 1L, -1, new BigDecimal("0.5"), true)) {
            require("INCLUSION".equals(mode(policy, ordered("name", number, "_id", false))), "非零/bool包含标志"); passed++;
        }
        for (Object zero : List.of(0, 0L, new BigDecimal("0.0"), false)) {
            require("EXCLUSION".equals(mode(policy, ordered("name", zero, "_id", true))), "_id include例外和排除标志"); passed++;
        }
        rejects(() -> mode(policy, Map.of()), "空project必须拒绝");
        rejects(() -> mode(policy, ordered("contact", 1, "contact.name", 1)), "路径碰撞拒绝不能覆盖");
        rejects(() -> entry(include, List.of("name", "name"), null), "单个字段列表重复拒绝");
        require("EXCLUSION".equals(mode(policy, Map.of("_id", 0))) && "INCLUSION".equals(mode(policy, Map.of("_id", 1))), "仅_id模式"); passed++;
        fixtures();
        System.out.println("PipelineSortProjectionEvidenceSelfTest PASSED: " + passed + " cases, 0 failures; explicit core sort specification wrapper; P0-04 candidates preserved");
    }

    private static void fixtures() throws Exception {
        Path root = Files.createTempDirectory("sort-projection-evidence-");
        try {
            Path aggregate = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(aggregate.getParent());
            Files.writeString(aggregate, "package com.mongoplus.aggregate; public interface Aggregate<C> {}");
            Path file = root.resolve("fixture/Factory.java"); Files.createDirectories(file.getParent());
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder().addSourceRoot(root)
                    .pipeline(true).addExpressionRoot("fixture.Factory").build());
            String source = fixture("String...", "@mongoParam keys FIELD_NAME ELEMENT\n * " + ENTRY + "\n * " + SOURCE);
            Files.writeString(file, source);
            Map<?, ?> renamed = methods(generator.generate(), "fixture.Factory").get(0);
            require(construction(renamed).get("resultSemanticType").equals("SORT_SPECIFICATION"), "任意名称依据声明生成"); passed++;
            String second = source.substring(source.indexOf("/**"), source.lastIndexOf('}')).replace("String... keys", "java.util.List<String> keys");
            Files.writeString(file, source.substring(0, source.lastIndexOf('}')) + second + "}");
            MongoPlusApiIndex ordered = generator.generate();
            Files.writeString(file, source.substring(0, source.indexOf("/**")) + second
                    + source.substring(source.indexOf("/**"), source.lastIndexOf('}')) + "}");
            require(ordered.asMap().equals(generator.generate().asMap()) && methods(ordered, "fixture.Factory").size() == 2,
                    "反转声明顺序不丢合法overload也不改变Index"); passed++;
            Files.writeString(file, fixture("String...", "@mongoParam keys FIELD_NAME ELEMENT"));
            require(!methods(generator.generate(), "fixture.Factory").get(0).containsKey("documentEntryConstruction"), "删标签不得由返回Bson或名称补齐"); passed++;
            for (String invalid : List.of(source.replace("@mongoParam keys FIELD_NAME ELEMENT", ""),
                    source.replace(SOURCE, ""), source.replace("int32:1", "int32:1.5"),
                    source.replace("int32:1", "int32:2147483648"), source.replace("order=INPUT", "order=SORTED"),
                    source.replace("duplicatePosition=FIRST", "duplicatePosition=UNKNOWN"),
                    source.replace("result=SORT_SPECIFICATION", "result=PIPELINE_EXPRESSION"),
                    source.replace("String... keys", "Integer... keys"), source.replace("String... keys", "String[][] keys"),
                    source.replace("String... keys", "List<List<String>> keys"), source.replace("String... keys", "List<? super String> keys"),
                    source.replace("String... keys", "List<?> keys"), source.replace("String... keys", "List keys"),
                    source.replace("String... keys", "List<String> keys").replace("import java.util.List;", "import other.List;"), source.replace("value=int32:1", "value=keys"),
                    source.replace("target=RESULT", "target=STAGE_BODY"), source.replace(ENTRY, ENTRY + " extra=YES"),
                    source.replace(ENTRY, ENTRY + " order=INPUT"), source.replace(ENTRY, ENTRY + "\n * " + ENTRY),
                    source.replace(SOURCE, SOURCE + "\n * " + SOURCE), source.replace("Bson renamed", "Object renamed"),
                    source.replace(ENTRY, ENTRY + "\n * @mongoComposition FIELD_NAME -> STAGE_BODY_DOCUMENT"))) {
                Files.writeString(file, invalid);
                rejects(() -> { try { generator.generate(); } catch (java.io.IOException e) { throw new IllegalStateException(e); } }, "非法metadata必须拒绝: " + invalid);
            }
            // 相邻无标签 overload、类级标签均不能继承证据。
            Files.writeString(file, source.replace("public class Factory", "/** " + ENTRY + " */ public class Factory")
                    .replace("public class Factory {", "public class Factory { public static Bson renamed(String key) { return null; }"));
            require(methods(generator.generate(), "fixture.Factory").stream().filter(m -> !m.get("signature").toString().contains("...")).allMatch(m -> !m.containsKey("documentEntryConstruction")), "类级及相邻overload隔离"); passed++;
            for (String semantic : List.of("SORT_SPECIFICATION", "STAGE_BODY_DOCUMENT")) {
                String reduction = "@mongoParam keys " + semantic + " ELEMENT\n * @mongoReduction keys -> " + semantic
                        + " operation=DOCUMENT_MERGE order=INPUT duplicateKeys=LAST_WINS depth=SHALLOW empty=EMPTY_DOCUMENT duplicatePosition=FIRST";
                Files.writeString(file, fixture("List<? extends Bson>", reduction));
                require(methods(generator.generate(), "fixture.Factory").get(0).get("resultSemanticType").equals(semantic), "同一通用reducer应用到不同文档角色"); passed++;
                Files.writeString(file, fixture("List<? extends Bson>", reduction.replace("-> " + semantic, "-> PIPELINE_EXPRESSION")));
                rejects(() -> { try { generator.generate(); } catch (java.io.IOException e) { throw new IllegalStateException(e); } }, "不能跨语义文档归约");
            }
            Files.writeString(file, fixture("String...", ""));
            String body = "package com.mongoplus.aggregate; import org.bson.conversions.Bson; public interface Aggregate<C> {\n"
                    + "/**\n * @mongoStage $arbitrary\n * @mongoParam data STAGE_BODY_DOCUMENT VALUE\n"
                    + " * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER\n"
                    + " * @mongoDocumentInput parameter=data semantic=STAGE_BODY_DOCUMENT encoding=WRAP_DECLARED_STAGE\n"
                    + " * @mongoDocumentSource mongoDocumentInput path=fixture/Core.java symbols=handle mechanism=显式body包装\n"
                    + " * @mongoDocumentPolicy input=FLAT_DOCUMENT numeric=ZERO_NONZERO_FLAGS boolean=FALSE_TRUE_FLAGS exceptionField=uuid mixed=REJECT_NON_EXCEPTION_EXCLUSION empty=REJECT expressions=INDEPENDENT_EVIDENCE\n"
                    + " * @mongoDocumentSource mongoDocumentPolicy reference=https://example.com/rules symbols=flags mechanism=显式规则\n"
                    + " */ C handle(Bson data); }";
            Files.writeString(aggregate, body);
            Map<?, ?> policy = (Map<?, ?>) method(generator.generate(), AGGREGATE, "handle(Bson data)").get("documentModePolicy");
            require("INCLUSION".equals(mode(policy, ordered("name", 1, "uuid", 0))), "例外字段只来自声明，不硬编码_id"); passed++;
            rejects(() -> mode(policy, ordered("name", 1, "_id", 0)), "真实例外改变时不能沿用_id推断");
            for (String invalid : List.of(body.replace("numeric=ZERO_NONZERO_FLAGS", "numeric=EXPRESSION"),
                    body.replace("exceptionField=uuid", "exceptionField=uuid exceptionField=uuid"),
                    body.replace("mixed=REJECT_NON_EXCEPTION_EXCLUSION", "mixed=REWRITE"),
                    body.replace("empty=REJECT", "empty=ALLOW"), body.replace("input=FLAT_DOCUMENT", "input=ANY_DOCUMENT"),
                    body.replace("parameter=data", "parameter=absent"),
                    body.replace("semantic=STAGE_BODY_DOCUMENT", "semantic=SORT_SPECIFICATION"),
                    body.replace("encoding=WRAP_DECLARED_STAGE", "encoding=UNCHANGED"),
                    body.replace("@mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER", ""),
                    body.replace("@mongoDocumentSource mongoDocumentPolicy reference=https://example.com/rules symbols=flags mechanism=显式规则", ""))) {
                Files.writeString(aggregate, invalid);
                rejects(() -> { try { generator.generate(); } catch (java.io.IOException e) { throw new IllegalStateException(e); } }, "非法输入/模式/effect/source拒绝");
            }
            Files.writeString(aggregate, body.replaceAll("(?m)^ \\* @mongoDocumentPolicy.*\\n", "")
                    .replaceAll("(?m)^ \\* @mongoDocumentSource mongoDocumentPolicy.*\\n", ""));
            require(!method(generator.generate(), AGGREGATE, "handle(Bson data)").containsKey("documentModePolicy"), "Stage名称不能恢复缺失模式"); passed++;
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                List<Path> files = paths.sorted(Comparator.reverseOrder()).toList();
                require(files.stream().allMatch(path -> path.toAbsolutePath().normalize().startsWith(root.toAbsolutePath().normalize())), "删除目标在临时fixture目录内");
                for (Path path : files) { Files.delete(path); }
            }
        }
    }

    private static String fixture(String type, String tags) {
        return "package fixture; import java.util.List; import org.bson.conversions.Bson; public class Factory {\n"
                + "/**\n * " + tags + "\n */ public static Bson renamed(" + type + " keys) { return null; } }";
    }

    private static LinkedHashMap<String, Object> entry(Map<?, ?> method, List<String> names, Object expression) {
        Map<?, ?> contract = construction(method);
        Map<?, ?> key = (Map<?, ?>) contract.get("key");
        Map<?, ?> value = (Map<?, ?>) contract.get("value");
        if ("FIXED".equals(key.get("input"))) { names = List.of((String) key.get("value")); }
        require("UNCHANGED".equals(key.get("encoding")), "测试字符串不能转成getter");
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (String name : names) {
            if (result.containsKey(name)) { throw new IllegalArgumentException("duplicate input key"); }
            result.put(name, "FIXED".equals(value.get("input")) ? value.get("value") : expression);
        }
        return result;
    }

    private static LinkedHashMap<String, Object> reduce(Map<?, ?> method, List<LinkedHashMap<String, Object>> parts) {
        Map<?, ?> reduction = (Map<?, ?>) method.get("reductionContract");
        require("DOCUMENT_MERGE".equals(reduction.get("operation")) && "INPUT".equals(reduction.get("order")), "只消费明确有序reduction");
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map<String, Object> part : parts) {
            for (Map.Entry<String, Object> field : part.entrySet()) {
                if (result.containsKey(field.getKey())) { throw new IllegalArgumentException("duplicate input key before LAST_WINS reduction"); }
                result.put(field.getKey(), field.getValue());
            }
        }
        return result;
    }

    private static String mode(Map<?, ?> policy, Map<String, Object> document) {
        require("ZERO_NONZERO_FLAGS".equals(policy.get("numeric")) && "FALSE_TRUE_FLAGS".equals(policy.get("boolean"))
                && "REJECT_NON_EXCEPTION_EXCLUSION".equals(policy.get("mixed")), "使用正式显式模式约束");
        if (document.isEmpty()) { throw new IllegalArgumentException("empty projection"); }
        for (String left : document.keySet()) {
            for (String right : document.keySet()) {
                if (!left.equals(right) && right.startsWith(left + ".")) { throw new IllegalArgumentException("path collision"); }
            }
        }
        boolean included = false; boolean excluded = false; boolean idIncluded = false;
        String exception = (String) policy.get("exceptionField");
        for (Map.Entry<String, Object> field : document.entrySet()) {
            Object value = field.getValue();
            boolean flag = value instanceof Number || value instanceof Boolean;
            boolean truth = flag && (value instanceof Boolean ? (Boolean) value : new BigDecimal(value.toString()).compareTo(BigDecimal.ZERO) != 0);
            if (flag && exception.equals(field.getKey())) { idIncluded = truth; continue; }
            if (flag && !truth) { excluded = true; } else { included = true; }
        }
        if (included && excluded) { throw new IllegalArgumentException("mixed projection modes"); }
        return included || !excluded && idIncluded ? "INCLUSION" : "EXCLUSION";
    }

    private static LinkedHashMap<String, Object> ordered(Object... pairs) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) { result.put((String) pairs[i], pairs[i + 1]); }
        return result;
    }

    private static Map<?, ?> construction(Map<?, ?> method) { return (Map<?, ?>) method.get("documentEntryConstruction"); }
    private static boolean fixedMatches(Map<?, ?> method, Object value) {
        Map<?, ?> fixed = (Map<?, ?>) construction(method).get("value");
        if (!"INT32_EXACT".equals(fixed.get("encoding")) || !(value instanceof Number)) { return false; }
        try { return new BigDecimal(value.toString()).toBigIntegerExact().equals(
                new BigDecimal(fixed.get("value").toString()).toBigIntegerExact()); }
        catch (ArithmeticException | NumberFormatException invalid) { return false; }
    }
    private static List<?> parameters(Map<?, ?> method) { return (List<?>) method.get("parameters"); }
    private static List<Map<?, ?>> methods(MongoPlusApiIndex index, String owner) {
        return maps(index.list("types")).stream().filter(t -> owner.equals(t.get("qualifiedName")))
                .flatMap(t -> maps((List<?>) t.get("publicMethods")).stream()).toList();
    }
    private static Map<?, ?> method(MongoPlusApiIndex index, String owner, String signature) {
        return methods(index, owner).stream().filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow(() -> new AssertionError(signature));
    }
    private static Map<?, ?> familyMethod(MongoPlusApiIndex index, String signature) {
        return maps(index.getMethodFamilies()).stream().flatMap(f -> maps((List<?>) f.get("overloads")).stream())
                .filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }
    private static List<Map<?, ?>> maps(List<?> values) { return values.stream().<Map<?, ?>>map(v -> (Map<?, ?>) v).toList(); }
    private static void rejects(Runnable action, String message) {
        try { action.run(); } catch (IllegalArgumentException expected) { passed++; return; }
        throw new AssertionError(message);
    }
    private static void require(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } }
}
