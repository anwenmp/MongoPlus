package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import com.mongoplus.indexer.json.JsonWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import javax.tools.ToolProvider;

/** 有序 document entry 到 Driver 泛型元素及精确 List 目标的正式 evidence 验收。 */
public final class VariableEntryConstructionSelfTest {
    private static final String SIGNATURE =
            "lookup(String from, List<Variable<TExpression>> letList, Aggregate<?> aggregate, String as)";
    private static final String DRIVER = "com.mongodb.client.model.Variable";
    private static final String ARTIFACT = "org.mongodb:mongodb-driver-core:5.4.0";
    private static final String AGGREGATE = "com.mongoplus.aggregate.Aggregate";
    private static int cases;

    private VariableEntryConstructionSelfTest() { }

    public static void main(String[] args) throws Exception {
        MongoPlusIndexerConfig config = MongoPlusIndexerConfig.forPipelineProject(Path.of(args[0])).build();
        MongoPlusIndexer generator = new MongoPlusIndexer(config);
        MongoPlusApiIndex index = generator.generate();
        Map<?, ?> target = overload(index, SIGNATURE);
        Map<?, ?> parameter = parameter(target, 1);
        complete(index, target, parameter, "let", DRIVER);
        require(target.equals(callable(type(index, AGGREGATE), SIGNATURE)), "两个正式视图一致");
        require("APPEND_STAGE".equals(contract(target, "pipelineEffect").get("operation"))
                && (AGGREGATE + "#getAggregateConditionList()")
                .equals(contract(parameter(target, 2), "pipelineExtraction").get("apiRef")), "既有管道构造链闭合");
        require(((List<?>) target.get("parameters")).stream().map(p -> (Map<?, ?>) p)
                .allMatch(p -> p.containsKey("objectFieldBinding")), "目标四个字段都有独立绑定");
        Map<?, ?> sibling = overload(index,
                "lookup(String from, List<Variable<TExpression>> letList, Aggregate<?> aggregate, SFunction<T, ?> as)");
        require(!parameter(sibling, 1).containsKey("entryConstruction"), "标签不传播到相邻 overload");
        cases++;
        require(json(index).equals(json(generator.generate())), "正式生成必须稳定");
        cases++;
        Path jar = config.getConstructionArtifactRepository().resolve(
                "org/mongodb/mongodb-driver-core/5.4.0/mongodb-driver-core-5.4.0.jar");
        LinkedHashMap<String, String> single = new LinkedHashMap<String, String>();
        single.put("userId", "$userId");
        verifyEntries(index, parameter, single, jar);
        LinkedHashMap<String, String> multiple = new LinkedHashMap<String, String>(single);
        multiple.put("orderId", "$_id");
        verifyEntries(index, parameter, multiple, jar);
        fixtures(config.getConstructionArtifactRepository());
        System.out.println("VariableEntryConstructionSelfTest PASSED: " + cases
                + " cases; single/multiple entries; userId -> orderId; generated Java -Werror; "
                + "real Driver constructor; generic names/artifact; negative contracts; deterministic");
    }

    private static void complete(MongoPlusApiIndex index, Map<?, ?> target, Map<?, ?> parameter,
                                 String field, String element) {
        require("VARIABLE_DEFINITION".equals(parameter.get("semanticType"))
                && "ELEMENT".equals(parameter.get("semanticScope")) && parameter.containsKey("semanticEvidence"),
                "变量定义元素的显式语义");
        require(field.equals(contract(parameter, "objectFieldBinding").get("field")), "对象字段绑定");
        Map<?, ?> entry = contract(parameter, "entryConstruction");
        require("CONSTRUCT_ENTRY".equals(entry.get("operation")) && "DOCUMENT_ENTRY".equals(entry.get("input")),
                "通用 entry construction");
        Map<?, ?> constructor = contract(entry, "constructor");
        require(element.equals(constructor.get("declaredIn")) && "public".equals(constructor.get("visibility"))
                && "VARIABLE_DEFINITION".equals(constructor.get("resultSemanticType")), "真实公开构造器及结果语义");
        require("ARTIFACT_GENERIC_SIGNATURE".equals(contract(constructor, "sourceEvidence").get("source"))
                && Boolean.TRUE.equals(contract(constructor, "sourceEvidence").get("verified")), "artifact 真实签名核验");
        Map<?, ?> key = (Map<?, ?>) ((List<?>) constructor.get("parameters")).get(0);
        Map<?, ?> value = (Map<?, ?>) ((List<?>) constructor.get("parameters")).get(1);
        require("ENTRY_KEY".equals(key.get("input")) && Integer.valueOf(0).equals(key.get("argumentIndex"))
                && "VARIABLE_NAME".equals(key.get("semanticType")) && "java.lang.String".equals(key.get("javaType")),
                "key -> VARIABLE_NAME");
        require("ENTRY_VALUE".equals(value.get("input")) && Integer.valueOf(1).equals(value.get("argumentIndex"))
                && "PIPELINE_EXPRESSION".equals(value.get("semanticType"))
                && "PIPELINE_EXPRESSION_FIELD_REFERENCE".equals(value.get("conceptRef")), "value -> expression");
        Map<?, ?> expression = concept(index, (String) value.get("conceptRef"));
        Map<?, ?> representation = contract(expression, "fieldReference");
        require("$".equals(representation.get("prefix")) && "$$".equals(representation.get("excludedPrefix"))
                && "UNCHANGED".equals(representation.get("encoding")), "复用既有 FIELD_REFERENCE 表示");
        require(concept(index, (String) key.get("conceptRef")) != null, "名称 concept 已正式输出");
        Map<?, ?> container = contract(parameter, "typedContainerConstruction");
        Map<?, ?> elementType = contract(container, "elementType");
        Map<?, ?> targetType = contract(container, "targetContainerType");
        Map<?, ?> argument = contract(container, "genericArgument");
        require("PARAMETERIZED".equals(elementType.get("kind")) && element.equals(elementType.get("rawType"))
                && ((List<?>) elementType.get("typeArguments")).equals(List.of(argument)), "完整泛型元素类型");
        require("java.util.List".equals(targetType.get("rawType"))
                && ((List<?>) targetType.get("typeArguments")).equals(List.of(elementType)), "精确 typed List 目标");
        Map<?, ?> binding = (Map<?, ?>) ((List<?>) constructor.get("genericBindings")).get(0);
        require(argument.equals(binding.get("argument")) && "ENTRY_VALUE".equals(binding.get("source")),
                "构造器泛型到目标实参的替换");
        require(((List<?>) constructor.get("typeParameterBounds")).equals(List.of("java.lang.Object")), "真实泛型上界");
        Map<?, ?> assignment = contract(container, "elementAssignability");
        require("IDENTITY".equals(assignment.get("kind")) && elementType.equals(assignment.get("from"))
                && elementType.equals(assignment.get("to")) && Boolean.TRUE.equals(assignment.get("verified")),
                "单元素赋值兼容");
        require("INVARIANT".equals(contract(container, "containerAssignability").get("genericVariance"))
                && "IDENTITY".equals(contract(container, "containerAssignability").get("kind")), "最终容器无协变假设");
        require("COLLECT_ENTRIES".equals(container.get("operation")) && "DOCUMENT".equals(container.get("input"))
                && "ORDERED_ENTRIES".equals(container.get("entryIteration")) && "INPUT".equals(container.get("order"))
                && Boolean.TRUE.equals(container.get("inputOrderPreserved")), "保序 entry 收集");
        require(Boolean.TRUE.equals(contract(container, "genericArgumentInference").get("sharedAcrossEntries")),
                "所有 entry 共用一个可赋值泛型实参");
        require((target.get("declaredIn") + "#" + target.get("signature") + ":" + parameter.get("name")
                + ".entryConstruction").equals(container.get("elementConstructionRef")), "entry/container 正式引用");
        require(((List<?>) index.asMap().get("requiredCapabilities")).contains("ENTRY_CONTAINER_CONSTRUCTION_V1"),
                "新能力显式声明");
    }

    /** 从正式 evidence 生成最小 Java，编译器验证泛型不变性；运行结果检查 entry 的原序和原值。 */
    private static void verifyEntries(MongoPlusApiIndex index, Map<?, ?> parameter,
                                      LinkedHashMap<String, String> entries, Path artifact) throws Exception {
        Map<?, ?> constructor = contract(contract(parameter, "entryConstruction"), "constructor");
        Map<?, ?> container = contract(parameter, "typedContainerConstruction");
        Map<?, ?> field = contract(concept(index, "PIPELINE_EXPRESSION_FIELD_REFERENCE"), "fieldReference");
        String valueType = (String) field.get("javaType");
        Map<?, ?> binding = (Map<?, ?>) ((List<?>) constructor.get("genericBindings")).get(0);
        require(contract(container, "genericArgument").equals(binding.get("argument")), "替换前 generic 一致");
        Map<?, ?> argument = contract(container, "genericArgument");
        String actualArgument = "TYPE_VARIABLE".equals(argument.get("kind"))
                ? valueType : (String) argument.get("rawType");
        String element = constructor.get("declaredIn") + "<" + actualArgument + ">";
        String target = (String) container.get("targetJavaType");
        if ("TYPE_VARIABLE".equals(argument.get("kind"))) {
            target = target.replace("<" + argument.get("name") + ">", "<" + actualArgument + ">");
        }
        List<String> expressions = new ArrayList<String>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            require(entry.getValue().startsWith((String) field.get("prefix"))
                    && !entry.getValue().startsWith((String) field.get("excludedPrefix")), "仅验证字段引用输入");
            String[] arguments = new String[2];
            for (Object item : (List<?>) constructor.get("parameters")) {
                Map<?, ?> role = (Map<?, ?>) item;
                arguments[((Number) role.get("argumentIndex")).intValue()] = literal(
                        "ENTRY_KEY".equals(role.get("input")) ? entry.getKey() : entry.getValue());
            }
            expressions.add("new " + element + "(" + String.join(",", arguments) + ")");
        }
        Path temp = Files.createTempDirectory("entry-java-evidence-");
        try {
            Path source = temp.resolve("EvidenceEntries.java");
            Files.writeString(source, "public class EvidenceEntries { public static " + target
                    + " make() { return java.util.Arrays.asList(" + String.join(",", expressions) + "); }}");
            StringWriter diagnostics = new StringWriter();
            try (javax.tools.StandardJavaFileManager files = ToolProvider.getSystemJavaCompiler()
                    .getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
                boolean passed = ToolProvider.getSystemJavaCompiler().getTask(diagnostics, files, null,
                        List.of("-proc:none", "-Xlint:unchecked", "-Werror", "-classpath", artifact.toString(),
                                "-d", temp.toString()), null,
                        files.getJavaFileObjectsFromFiles(List.of(source.toFile()))).call();
                require(passed, "泛型目标必须无 unchecked 转换编译成功: " + diagnostics);
            }
            try (URLClassLoader loader = new URLClassLoader(new URL[]{temp.toUri().toURL(), artifact.toUri().toURL()},
                    ClassLoader.getPlatformClassLoader())) {
                List<?> result = (List<?>) loader.loadClass("EvidenceEntries").getMethod("make").invoke(null);
                List<String> names = new ArrayList<String>();
                List<String> values = new ArrayList<String>();
                for (Object entry : result) {
                    names.add((String) entry.getClass().getMethod("getName").invoke(entry));
                    values.add((String) entry.getClass().getMethod("getValue").invoke(entry));
                }
                require(names.equals(new ArrayList<String>(entries.keySet())), "entry key 的原序");
                require(values.equals(new ArrayList<String>(entries.values())), "entry value 原值及原序");
            }
        } finally { remove(temp); }
        cases++;
    }

    private static void fixtures(Path repository) throws Exception {
        Path root = Files.createTempDirectory("entry-container-fixture-");
        try {
            String tags = tags(DRIVER, ARTIFACT);
            String source = fixture(tags, "<Renamed>", "java.util.List<" + DRIVER + "<Renamed>>");
            MongoPlusApiIndex generic = generate(root, source, repository);
            Map<?, ?> method = (Map<?, ?>) ((List<?>) ((Map<?, ?>) generic.getMethodFamilies().get(0))
                    .get("overloads")).get(0);
            complete(generic, method, parameter(method, 0), "definitions", DRIVER);
            require("Renamed".equals(contract(contract(parameter(method, 0), "typedContainerConstruction"),
                    "genericArgument").get("name")), "泛型改名不影响契约");
            cases++;
            for (String tag : List.of("mongoParam", "mongoEntryConstruction", "mongoTypedContainer",
                    "mongoEntryConstructionSource", "mongoObjectFieldSource")) {
                String changed = source.replaceAll("(?m)^ \\* @" + tag + " .*\\R", "");
                rejected(root, changed, repository, "删除 " + tag);
            }
            for (String changed : List.of(
                    source.replace("key=0", "key=2"), source.replace("value=1", "value=0"),
                    source.replace("order=INPUT", "order=SORTED"),
                    source.replace("valueSemantic=PIPELINE_EXPRESSION", "valueSemantic=VARIABLE_REFERENCE"),
                    source.replace("<Renamed>>", "<?> >"),
                    source.replace("java.util.List<" + DRIVER + "<Renamed>>", "java.util.List"),
                    source.replace("java.util.List<" + DRIVER + "<Renamed>>", "java.util.List<" + DRIVER + ">"),
                    source.replace("java.util.List<" + DRIVER + "<Renamed>>", "java.util.List<? extends " + DRIVER + "<Renamed>>"),
                    source.replace("<Renamed> C", "<Renamed extends Number> C"),
                    source.replace("result=VARIABLE_DEFINITION", "result=NAMED_PIPELINE"),
                    source.replace("constructor=" + DRIVER, "constructor=com.mongoplus.aggregate.pipeline.Variable"),
                    source.replace("key=0", "key=0 key=0"))) {
                rejected(root, changed, repository, "非法 entry/container 契约");
            }
            Path emptyRepository = root.resolve("empty-repository");
            rejected(root, source, emptyRepository, "缺少 artifact 不能伪造签名");
            String noBinding = source.replaceAll("(?m)^ \\* @mongoObjectField(?:Source)? .*\\R", "");
            Map<?, ?> unbound = parameter((Map<?, ?>) ((List<?>) ((Map<?, ?>) generate(root, noBinding, repository)
                    .getMethodFamilies().get(0)).get("overloads")).get(0), 0);
            require(!unbound.containsKey("objectFieldBinding"), "删除字段标签后不推断字段映射");
            cases++;
            String untagged = fixture("", "<Renamed>", "java.util.List<" + DRIVER + "<Renamed>>");
            Map<?, ?> noEvidence = parameter((Map<?, ?>) ((List<?>) ((Map<?, ?>) generate(root, untagged, repository)
                    .getMethodFamilies().get(0)).get("overloads")).get(0), 0);
            require(!noEvidence.containsKey("entryConstruction") && !noEvidence.containsKey("objectFieldBinding"),
                    "相同 Java 类型、无标签不赋予语义");
            cases++;
            String classTags = untagged.replace("public interface", "/**\n" + tags + " */\npublic interface");
            Map<?, ?> classOnly = parameter((Map<?, ?>) ((List<?>) ((Map<?, ?>) generate(root, classTags, repository)
                    .getMethodFamilies().get(0)).get("overloads")).get(0), 0);
            require(!classOnly.containsKey("entryConstruction"), "类级标签不传播");
            cases++;
            for (String argument : List.of("java.lang.String", "java.lang.Object")) {
                MongoPlusApiIndex concrete = generate(root, source.replace("<Renamed>>", "<" + argument + ">>"),
                        repository);
                Map<?, ?> concreteMethod = (Map<?, ?>) ((List<?>) ((Map<?, ?>) concrete.getMethodFamilies().get(0))
                        .get("overloads")).get(0);
                Map<?, ?> concreteParameter = parameter(concreteMethod, 0);
                complete(concrete, concreteMethod, concreteParameter, "definitions", DRIVER);
                require("ASSIGNABLE_TO_DECLARED_ARGUMENT".equals(contract(
                        contract(concreteParameter, "typedContainerConstruction"), "genericArgumentInference").get("rule")),
                        "具体泛型实参只校验赋值，不重新推断");
                LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
                data.put("last", "$last");
                data.put("first", "$first");
                verifyEntries(concrete, concreteParameter, data, repository.resolve(
                        "org/mongodb/mongodb-driver-core/5.4.0/mongodb-driver-core-5.4.0.jar"));
            }
            Path ownRepository = root.resolve("repository");
            Path jar = customArtifact(root, ownRepository);
            MongoPlusApiIndex custom = generate(root, fixture(tags("fixture.Entry", "fixture:entry:1.0"),
                    "<Changed>", "java.util.List<fixture.Entry<Changed>>"), ownRepository);
            Map<?, ?> customMethod = (Map<?, ?>) ((List<?>) ((Map<?, ?>) custom.getMethodFamilies().get(0))
                    .get("overloads")).get(0);
            complete(custom, customMethod, parameter(customMethod, 0), "definitions", "fixture.Entry");
            LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
            data.put("second", "$second");
            data.put("first", "$first");
            verifyEntries(custom, parameter(customMethod, 0), data, jar);
        } finally { remove(root); }
    }

    private static String tags(String type, String artifact) {
        return " * @mongoParam entries VARIABLE_DEFINITION ELEMENT\n"
                + " * @mongoEntryConstruction entries constructor=" + type + " artifact=" + artifact
                + " key=0 value=1 keySemantic=VARIABLE_NAME valueSemantic=PIPELINE_EXPRESSION result=VARIABLE_DEFINITION\n"
                + " * @mongoEntryConstructionSource entries artifact=" + artifact
                + " symbols=constructor;getName;getValue mechanism=Names and values are retained\n"
                + " * @mongoTypedContainer entries input=DOCUMENT_ENTRIES order=INPUT target=java.util.List\n"
                + " * @mongoObjectField entries field=definitions\n"
                + " * @mongoObjectFieldSource entries artifact=" + artifact
                + " symbols=consumer mechanism=Ordered elements are consumed\n";
    }

    private static String fixture(String tags, String generic, String container) {
        return "package com.mongoplus.aggregate; public interface Aggregate<C> {\n/**\n"
                + " * @mongoStage $opaque\n" + tags + " */\n" + generic + " C gather(" + container + " entries);\n}";
    }

    private static MongoPlusApiIndex generate(Path root, String source, Path repository) throws Exception {
        Path file = root.resolve("com/mongoplus/aggregate/Aggregate.java");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source);
        return new MongoPlusIndexer(MongoPlusIndexerConfig.builder().addSourceRoot(root).pipeline(true)
                .constructionArtifactRepository(repository).build()).generate();
    }

    private static void rejected(Path root, String source, Path repository, String label) throws Exception {
        try { generate(root, source, repository); }
        catch (IllegalArgumentException expected) { cases++; return; }
        throw new AssertionError("必须拒绝: " + label);
    }

    private static Path customArtifact(Path root, Path repository) throws Exception {
        Path source = root.resolve("artifact-src/fixture/Entry.java");
        Path classes = root.resolve("artifact-classes");
        Files.createDirectories(source.getParent());
        Files.createDirectories(classes);
        Files.writeString(source, "package fixture; public class Entry<Value> {"
                + "private final String name; private final Value value;"
                + "public Entry(String name, Value value) { this.name=name; this.value=value; }"
                + "public String getName() { return name; } public Value getValue() { return value; }}");
        require(ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", classes.toString(),
                source.toString()) == 0, "通用 artifact 夹具编译");
        Path jar = repository.resolve("fixture/entry/1.0/entry-1.0.jar");
        Files.createDirectories(jar.getParent());
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, "1.0");
        try (JarOutputStream archive = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
            archive.putNextEntry(new JarEntry("fixture/Entry.class"));
            Files.copy(classes.resolve("fixture/Entry.class"), archive);
            archive.closeEntry();
        }
        return jar;
    }

    private static String literal(String value) { return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }

    private static Map<?, ?> overload(MongoPlusApiIndex index, String signature) {
        return index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                .flatMap(f -> ((List<?>) f.get("overloads")).stream()).map(m -> (Map<?, ?>) m)
                .filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }

    private static Map<?, ?> parameter(Map<?, ?> method, int index) {
        return (Map<?, ?>) ((List<?>) method.get("parameters")).get(index);
    }

    private static Map<?, ?> contract(Map<?, ?> value, String key) {
        require(value.get(key) instanceof Map, "缺少 " + key);
        return (Map<?, ?>) value.get(key);
    }

    private static Map<?, ?> concept(MongoPlusApiIndex index, String id) {
        return index.list("concepts").stream().map(c -> (Map<?, ?>) c)
                .filter(c -> id.equals(c.get("id"))).findFirst().orElseThrow();
    }

    private static Map<?, ?> type(MongoPlusApiIndex index, String name) {
        return index.list("types").stream().map(t -> (Map<?, ?>) t)
                .filter(t -> name.equals(t.get("qualifiedName"))).findFirst().orElseThrow();
    }

    private static Map<?, ?> callable(Map<?, ?> type, String signature) {
        return ((List<?>) type.get("publicMethods")).stream().map(m -> (Map<?, ?>) m)
                .filter(m -> signature.equals(m.get("signature"))).findFirst().orElseThrow();
    }

    private static String json(MongoPlusApiIndex index) throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter.write(index.asMap(), writer);
        return writer.toString();
    }

    private static void remove(Path root) throws Exception {
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
