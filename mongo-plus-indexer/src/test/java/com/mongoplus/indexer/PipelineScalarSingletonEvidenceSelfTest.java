package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 仅消费正式逐参数 evidence；测试绑定约束，不实现生产 planner。 */
public final class PipelineScalarSingletonEvidenceSelfTest {
    private static final String CAPABILITY = "STAGE_VALUE_BINDING_V1";
    private static final String CORE = "com.mongoplus.aggregate.Aggregate";
    private static int passed;

    private PipelineScalarSingletonEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]);
        MongoPlusApiIndex real = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).build()).generate();
        require(((List<?>) real.asMap().get("requiredCapabilities")).contains(CAPABILITY), "能力必须显式揭示");
        Map<?, ?> concept = maps(real.list("concepts")).stream().filter(c -> CAPABILITY.equals(c.get("id"))).findFirst().orElseThrow();
        require(Boolean.FALSE.equals(((Map<?, ?>) concept.get("candidateSelection")).get("methodOrderIsEvidence")),
                "候选选择不能按方法排列顺序"); passed++;
        for (String signature : List.of("skip(int skip)", "skip(long skip)")) {
            Map<?, ?> method = method(real, signature);
            Map<?, ?> parameter = parameter(method);
            require("INTEGER_VALUE".equals(parameter.get("semanticType")), "skip 需要显式整数语义");
            Map<?, ?> binding = binding(method);
            require("INT32_EXACT".equals(binding.get("encoding")), "skip(long) 也必须明确 Int32");
            require(Long.valueOf(0).equals(binding.get("minimum"))
                    && Long.valueOf(Integer.MAX_VALUE).equals(binding.get("maximum")), "skip 可绑定范围");
            require(!parameter.containsKey("objectFieldBinding"), "标量没有虚构对象字段");
            for (String value : List.of("0", "2", "2147483647")) { require(integerAccepts(binding, value), value); passed++; }
            for (String value : List.of("-1", "2.5", "2147483648", "9223372036854775807",
                    "9223372036854775808", "9007199254740993", "NaN")) {
                require(!integerAccepts(binding, value), "必须拒绝且不能截断: " + value); passed++;
            }
            require(method.equals(typeMethod(real, signature)), "两个 Index 视图相同");
            nestedEffects(real, method);
        }
        for (String signature : List.of("unset(String... field)", "unset(List<String> fields)")) {
            Map<?, ?> method = method(real, signature);
            Map<?, ?> parameter = parameter(method);
            Map<?, ?> binding = binding(method);
            Map<?, ?> container = (Map<?, ?>) parameter.get("elementContainerBinding");
            require("java.lang.String".equals(container.get("elementJavaType")), "真实泛型元素必须闭合为 String");
            require("VALUE_TO_ELEMENT".equals(((Map<?, ?>) container.get("singletonLifting")).get("operation")), "标量显式 lifting");
            require("SINGLETON_SCALAR_ELSE_ARRAY".equals(binding.get("encoding")), "真实 singleton 压缩编码");
            for (Object input : List.of("name", List.of("name", "age"), List.of("name"), "profile.name")) {
                require(elementsAccept(binding, input), "合法字段输入"); passed++;
            }
            for (Object input : List.of(List.of(), List.of("name", 2), List.of("name", "name"), 2)) {
                require(!elementsAccept(binding, input), "错误输入不能闭合绑定"); passed++;
            }
            require(method.equals(typeMethod(real, signature)), "两个视图容器与编码相同");
            nestedEffects(real, method);
        }
        Map<?, ?> sort = method(real, "sortByCount(String field)");
        require("PIPELINE_EXPRESSION".equals(parameter(sort).get("semanticType"))
                && "UNCHANGED".equals(binding(sort).get("encoding")), "sortByCount 复用显式字符串标量");
        Map<?, ?> stringConstraints = (Map<?, ?>) binding(sort).get("stringConstraints");
        require("$".equals(stringConstraints.get("requiredPrefix")) && Integer.valueOf(2).equals(stringConstraints.get("minimumLength")),
                "sortByCount 字符串合法性必须显式声明");
        require(stringAccepts(binding(sort), "$name"), "合法 sortByCount 字符串"); passed++;
        for (String value : List.of("name", "$", "")) {
            require(!stringAccepts(binding(sort), value), "不能修复普通或空表达式字符串"); passed++;
        }
        require(!parameter(method(real, "unset(SFunction<T, ?>... field)")).containsKey("stageValueBinding"),
                "字符串输入不得假冒 getter 构造能力");
        passed++;
        Path root = Files.createTempDirectory("scalar-singleton-evidence-");
        try {
            Path file = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(file.getParent());
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder().addSourceRoot(root).pipeline(true).build());
            String actual = Files.readString(project.resolve("mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java"));
            Files.writeString(file, actual.replaceAll("(?m)^\\s*\\* @mongoStageValue(?:Source)? [^\\r\\n]*\\r?\\n", ""));
            MongoPlusApiIndex missing = generator.generate();
            for (String signature : List.of("skip(int skip)", "skip(long skip)", "unset(String... field)",
                    "unset(List<String> fields)", "sortByCount(String field)")) {
                require(!parameter(method(missing, signature)).containsKey("stageValueBinding"), "移除 evidence 不得按名称恢复");
                require(method(missing, signature).containsKey("pipelineEffect"), "绑定和 receiver effect 独立"); passed++;
            }
            String scalar = fixture("long", "INTEGER_VALUE VALUE", "encoding=INT32_EXACT minimum=-5 maximum=20");
            Files.writeString(file, scalar);
            Map<?, ?> renamed = onlyMethod(generator.generate());
            require(Long.valueOf(-5).equals(binding(renamed).get("minimum")), "范围不按 skip 硬编码"); passed++;
            String intDeclaration = scalar.substring(scalar.indexOf("/**"), scalar.lastIndexOf("}\n"))
                    .replace("long amount", "int amount");
            String longDeclaration = scalar.substring(scalar.indexOf("/**"), scalar.lastIndexOf("}\n"));
            Files.writeString(file, scalar.replace("}\n", intDeclaration + "}\n"));
            MongoPlusApiIndex ordered = generator.generate();
            Files.writeString(file, scalar.substring(0, scalar.indexOf("/**")) + intDeclaration + longDeclaration + "}\n");
            require(ordered.asMap().equals(generator.generate().asMap()), "反转合法 overload 的源码顺序不改变 Index");
            require(maps((List<?>) maps(ordered.getMethodFamilies()).get(0).get("overloads")).size() == 2,
                    "多个合法 overload 必须全部保留正式 Java 类型"); passed++;
            for (String type : List.of("String...", "java.lang.String[]", "List<String>", "List<? extends String>")) {
                Files.writeString(file, fixture(type, "FIELD_NAME ELEMENT",
                        "encoding=SINGLETON_SCALAR_ELSE_ARRAY minimumSize=1 duplicates=REJECT singleton=VALUE_TO_ELEMENT"));
                Map<?, ?> generic = onlyMethod(generator.generate());
                require("java.lang.String".equals(((Map<?, ?>) parameter(generic).get("elementContainerBinding")).get("elementJavaType")), "AST 容器校验");
                require(elementsAccept(binding(generic), "name"), "不同方法与 Stage 同样 lifting"); passed++;
            }
            Files.writeString(file, fixture("List<String>", "FIELD_NAME ELEMENT",
                    "encoding=SINGLETON_SCALAR_ELSE_ARRAY minimumSize=0 duplicates=PRESERVE singleton=FORBID"));
            Map<?, ?> arrayOnly = onlyMethod(generator.generate());
            require(elementsAccept(binding(arrayOnly), List.of()) && elementsAccept(binding(arrayOnly), List.of("name", "name"))
                    && !elementsAccept(binding(arrayOnly), "name"), "大小、重复及 lifting 规则只取显式声明"); passed++;
            Files.writeString(file, fixture("String", "PIPELINE_EXPRESSION VALUE", "encoding=UNCHANGED prefix=^ minimumLength=3"));
            Map<?, ?> expression = onlyMethod(generator.generate());
            require(stringAccepts(binding(expression), "^ab") && !stringAccepts(binding(expression), "$ab"),
                    "通用字符串约束不按 sortByCount 硬编码"); passed++;
            String container = fixture("List<String>", "FIELD_NAME ELEMENT",
                    "encoding=SINGLETON_SCALAR_ELSE_ARRAY minimumSize=1 duplicates=REJECT singleton=VALUE_TO_ELEMENT");
            for (String type : List.of("List", "List<?>", "List<? super String>", "List<List<String>>", "List<Integer>", "String[][]")) {
                reject(file, generator, container.replace("List<String> amount", type + " amount"));
            }
            reject(file, generator, container.replace("import java.util.List;", "import other.List;"));
            reject(file, generator, container.replace("import java.util.List;", "import java.util.List; import other.String;"));
            reject(file, generator, scalar.replace("@mongoParam amount INTEGER_VALUE VALUE", ""));
            reject(file, generator, scalar.replaceAll("(?m)^ \\* @mongoStageValueSource.*\\n", ""));
            reject(file, generator, scalar.replace("maximum=20", "maximum=2147483648"));
            reject(file, generator, scalar.replace("minimum=-5", "minimum=1.5"));
            reject(file, generator, scalar.replace("minimum=-5", "minimum=21"));
            reject(file, generator, scalar.replace("encoding=INT32_EXACT", "encoding=INT64_EXACT"));
            reject(file, generator, scalar.replace("minimum=-5", "minimum=-5 minimum=-5"));
            reject(file, generator, scalar.replace("maximum=20", "maximum=20 unknown=YES"));
            Files.writeString(file, scalar.replace("@mongoPipelineEffect", "@unrecognized"));
            Map<?, ?> withoutEffect = onlyMethod(generator.generate());
            require(!withoutEffect.containsKey("pipelineEffect") && parameter(withoutEffect).containsKey("stageValueBinding"),
                    "值绑定不能产生 receiver effect，effect 删除不能抹掉独立参数事实"); passed++;
            reject(file, generator, scalar.replace("long amount", "long amount, long other"));
            reject(file, generator, container.replace("singleton=VALUE_TO_ELEMENT", "singleton=AUTO"));
            reject(file, generator, container.replace("minimumSize=1", "minimumSize=-1"));
            reject(file, generator, container.replace("duplicates=REJECT", "duplicates=IGNORE"));
            reject(file, generator, scalar.replace("symbols=write", "unknown=write"));
            Files.writeString(file, scalar.replace("}\n", "/** @mongoStage $unrelated */ C construct(int amount);\n}\n"));
            MongoPlusApiIndex adjacent = generator.generate();
            Map<?, ?> unmarked = maps(adjacent.getMethodFamilies()).stream().flatMap(f -> maps((List<?>) f.get("overloads")).stream())
                    .filter(m -> "construct(int amount)".equals(m.get("signature"))).findFirst().orElseThrow();
            require(!parameter(unmarked).containsKey("stageValueBinding"), "相邻 overload 不传播"); passed++;
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
        System.out.println("PipelineScalarSingletonEvidenceSelfTest PASSED: " + passed + " cases; 12 nested effect checks");
    }

    private static String fixture(String type, String semantic, String attributes) {
        return "package com.mongoplus.aggregate; import java.util.List; public interface Aggregate<C> {\n/**\n"
                + " * @mongoStage $unrelated\n * @mongoParam amount " + semantic + "\n"
                + " * @mongoStageValue amount " + attributes + "\n"
                + " * @mongoStageValueSource amount path=fixture/Impl.java symbols=write mechanism=显式构造和编码。\n"
                + " * @mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER\n"
                + " */ C construct(" + type + " amount);\n}\n";
    }

    private static boolean integerAccepts(Map<?, ?> binding, String value) {
        try {
            BigInteger integer = new BigDecimal(value).toBigIntegerExact();
            return integer.compareTo(BigInteger.valueOf(((Number) binding.get("minimum")).longValue())) >= 0
                    && integer.compareTo(BigInteger.valueOf(((Number) binding.get("maximum")).longValue())) <= 0;
        } catch (NumberFormatException | ArithmeticException exception) { return false; }
    }

    private static boolean elementsAccept(Map<?, ?> binding, Object input) {
        if (!((List<?>) binding.get("inputShapes")).contains(input instanceof List ? "ARRAY" : "VALUE")) { return false; }
        List<?> elements = input instanceof List ? (List<?>) input : List.of(input);
        return elements.size() >= ((Number) binding.get("minimumSize")).intValue()
                && elements.stream().allMatch(item -> item instanceof String)
                && (!"REJECT".equals(binding.get("duplicates")) || elements.stream().distinct().count() == elements.size());
    }

    private static boolean stringAccepts(Map<?, ?> binding, String input) {
        Map<?, ?> constraints = (Map<?, ?>) binding.get("stringConstraints");
        return input.startsWith((String) constraints.get("requiredPrefix"))
                && input.length() >= ((Number) constraints.get("minimumLength")).intValue();
    }

    private static void nestedEffects(MongoPlusApiIndex index, Map<?, ?> method) {
        Map<?, ?> factory = callable(index, "com.mongoplus.aggregate.AggregateWrapper", "constructors", "AggregateWrapper()");
        require("INDEPENDENT".equals(((Map<?, ?>) factory.get("pipelineFactory")).get("ownership")), "独立嵌套 receiver factory");
        Map<?, ?> representation = (Map<?, ?>) typeMethod(index, "getAggregateConditionList()").get("pipelineRepresentation");
        require("RECEIVER".equals(representation.get("source")) && "CALL_ORDER".equals(representation.get("order")), "完整有序 receiver representation");
        for (String outer : List.of("facet(Facet... facets)", "lookup(String from, Aggregate<?> aggregate, String as)",
                "unionWith(String collectionName, Aggregate<?> aggregate)")) {
            Map<?, ?> effect = (Map<?, ?>) method.get("pipelineEffect");
            require(effect != null && "RECEIVER".equals(effect.get("target"))
                    && "CALL_ORDER".equals(effect.get("order")), outer + " 内部不丢失 effect");
            Map<?, ?> owner = method(index, outer);
            require(owner.containsKey("pipelineEffect"), "外层 receiver effect");
            if (owner.containsKey("pipelineContainer")) {
                Map<?, ?> entry = callable(index, "com.mongoplus.aggregate.pipeline.Facet", "constructors",
                        "Facet(String name, Aggregate<?> aggregateChainWrapper)");
                require("NAMED_PIPELINE".equals(entry.get("resultSemanticType"))
                        && maps((List<?>) entry.get("parameters")).get(1).containsKey("pipelineExtraction"), "facet 正式 entry/extraction");
            } else {
                require(maps((List<?>) owner.get("parameters")).stream().anyMatch(p -> p.containsKey("pipelineExtraction")),
                        "lookup/unionWith 正式 extraction");
            }
        }
    }

    private static void reject(Path file, MongoPlusIndexer generator, String source) throws Exception {
        Files.writeString(file, source);
        try { generator.generate(); throw new AssertionError("非法契约不能生成: " + source); }
        catch (IllegalArgumentException expected) { passed++; }
    }

    private static Map<?, ?> onlyMethod(MongoPlusApiIndex index) { return maps((List<?>) maps(index.getMethodFamilies()).get(0).get("overloads")).get(0); }
    private static Map<?, ?> binding(Map<?, ?> method) {
        Object result = parameter(method).get("stageValueBinding");
        require(result instanceof Map, "缺少逐参数 Stage value binding: " + method.get("signature"));
        return (Map<?, ?>) result;
    }
    private static Map<?, ?> parameter(Map<?, ?> method) { return maps((List<?>) method.get("parameters")).get(0); }
    private static List<Map<?, ?>> maps(List<?> list) { return list.stream().<Map<?, ?>>map(item -> (Map<?, ?>) item).toList(); }
    private static Map<?, ?> method(MongoPlusApiIndex index, String signature) {
        return maps(index.getMethodFamilies()).stream().flatMap(f -> maps((List<?>) f.get("overloads")).stream())
                .filter(m -> CORE.equals(m.get("declaredIn")) && signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }
    private static Map<?, ?> typeMethod(MongoPlusApiIndex index, String signature) {
        return maps(index.list("types")).stream().filter(t -> CORE.equals(t.get("qualifiedName")))
                .flatMap(t -> maps((List<?>) t.get("publicMethods")).stream())
                .filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }
    private static Map<?, ?> callable(MongoPlusApiIndex index, String owner, String collection, String signature) {
        return maps(index.list("types")).stream().filter(t -> owner.equals(t.get("qualifiedName")))
                .flatMap(t -> maps((List<?>) t.get(collection)).stream())
                .filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }
    private static void require(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } }
}
