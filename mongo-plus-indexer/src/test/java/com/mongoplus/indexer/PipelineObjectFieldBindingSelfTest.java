package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 从真实声明重新生成，并用删标签、改名及非法契约验证对象字段绑定不依赖名称推断。 */
public final class PipelineObjectFieldBindingSelfTest {
    private static final String PARAM = "@mongoParam amount INTEGER_VALUE VALUE";
    private static final String BINDING = "@mongoObjectField amount field=quantity encoding=INT32_EXACT minimum=-20 maximum=200";
    private static final String SOURCE = "@mongoObjectFieldSource amount path=fixture/Implementation.java symbols=build(Number) mechanism=显式实现经 intValue 写入 Int32。";
    private static int passed;

    private PipelineObjectFieldBindingSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path project = Path.of(args[0]);
        MongoPlusIndexer realGenerator = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(project).build());
        MongoPlusApiIndex real = realGenerator.generate();
        completeSample(real);
        require(real.asMap().equals(realGenerator.generate().asMap()), "重复真实生成必须确定");
        passed++;
        Path root = Files.createTempDirectory("pipeline-object-field-");
        try {
            Path file = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(file.getParent());
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                    .addSourceRoot(root).pipeline(true).build());
            String realSource = Files.readString(project.resolve("mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java"));
            String stripped = realSource.replaceAll("(?m)^\\s*\\* @mongo(?:Param size INTEGER_VALUE|ObjectField(?:Source)? size)[^\\r\\n]*\\r?\\n", "");
            require(!stripped.equals(realSource), "真实源码必须有可删除的绑定标签");
            Files.writeString(file, stripped);
            MongoPlusApiIndex missing = generator.generate();
            Map<?, ?> unmarked = parameter(sample(missing));
            require("JDK_TYPE".equals(unmarked.get("semanticType")), "删源码标签后恢复 JDK_TYPE");
            require(!unmarked.containsKey("objectFieldBinding"), "删标签不能保留绑定");
            try {
                completeSample(missing);
                throw new IllegalStateException("删源码 metadata 后真实验收应失败");
            } catch (AssertionError expected) {
                require(expected.getMessage().contains("INTEGER_VALUE"), "同一验收必须因缺少语义失败");
            }
            passed++;
            for (String type : List.of("Number", "java.lang.Number", "int", "Integer", "Long", "java.math.BigDecimal")) {
                Files.writeString(file, source(type, PARAM, BINDING, SOURCE));
                MongoPlusApiIndex index = generator.generate();
                Map<?, ?> method = overloads(index).get(0);
                Map<?, ?> binding = (Map<?, ?>) parameter(method).get("objectFieldBinding");
                require("quantity".equals(binding.get("field")), "不同字段名按标签输出");
                require(Long.valueOf(-20).equals(binding.get("minimum")) && Long.valueOf(200).equals(binding.get("maximum")), "范围按声明输出");
                require(List.of("STAGE_OBJECT_FIELD_BINDING_V1").equals(index.asMap().get("requiredCapabilities")), "仅输出实际使用能力");
                require(method.equals(publicMethods(index).get(0)), "两个视图完整一致");
                passed++;
            }
            reject(file, generator, source("Number", "", BINDING, SOURCE), "缺少显式 @mongoParam");
            reject(file, generator, source("Number", PARAM, BINDING, ""), "缺少来源证据");
            reject(file, generator, source("Number", PARAM, "", SOURCE), "没有对应绑定");
            reject(file, generator, source("String", PARAM, BINDING, SOURCE), "Java 表示不匹配");
            reject(file, generator, source("Number[]", PARAM, BINDING, SOURCE), "Java 表示不匹配");
            reject(file, generator, source("Number", PARAM, BINDING, SOURCE).replace("package com.mongoplus.aggregate;", "package com.mongoplus.aggregate; import other.Number;"), "Java 表示不匹配");
            reject(file, generator, source("Number", PARAM.replace("INTEGER_VALUE", "PIPELINE_EXPRESSION"), BINDING, SOURCE), "INTEGER_VALUE VALUE");
            reject(file, generator, source("Number", PARAM, BINDING.replace("amount field", "missing field"), SOURCE), "参数不存在");
            reject(file, generator, source("Number", PARAM, BINDING + "\n * " + BINDING, SOURCE), "重复绑定");
            reject(file, generator, source("Number", PARAM, BINDING, SOURCE.replace("amount path", "missing path")), "没有对应绑定");
            for (String attribute : List.of("field=quantity", "encoding=INT32_EXACT", "minimum=-20", "maximum=200")) {
                reject(file, generator, source("Number", PARAM, BINDING.replace(attribute, ""), SOURCE), "缺少属性");
                reject(file, generator, source("Number", PARAM, BINDING + " " + attribute, SOURCE), "重复属性");
            }
            for (String range : List.of("minimum=-2147483649", "minimum=201", "minimum=1.5", "minimum=NaN", "maximum=2147483648", "maximum=99999999999999999999")) {
                String key = range.substring(0, range.indexOf('='));
                reject(file, generator, source("Number", PARAM, BINDING.replaceAll(key + "=[^ ]+", range), SOURCE), "Int32 范围");
            }
            reject(file, generator, source("Number", PARAM, BINDING.replace("INT32_EXACT", "UNKNOWN"), SOURCE), "不支持的 encoding");
            reject(file, generator, source("Number", PARAM, BINDING.replace("field=quantity", "field=$quantity"), SOURCE), "单层对象字段");
            reject(file, generator, source("Number", PARAM, BINDING + " extra=YES", SOURCE), "未知属性");
            reject(file, generator, source("Number", PARAM, BINDING, SOURCE.replace("symbols=", "unknown=")), "来源语法");
            reject(file, generator, source("Number", PARAM, BINDING, SOURCE.replace("path=fixture/Implementation.java", "path=")), "来源语法");
            reject(file, generator, source("Number", PARAM, BINDING, SOURCE).replace("@mongoStage $arbitrary", "@mongoExpression $arbitrary"), "唯一 Stage 映射");
            // 类级标签和相邻未标记重载均不能提供参数证据。
            Files.writeString(file, source("Number", PARAM, BINDING, SOURCE)
                    .replace("public interface", "/**\n * " + PARAM + "\n * " + BINDING + "\n * " + SOURCE + "\n */\npublic interface")
                    .replace("}\n", "/** @mongoStage $arbitrary */\n C construct(long amount);\n}\n"));
            MongoPlusApiIndex adjacent = generator.generate();
            Map<?, ?> plain = overloads(adjacent).stream().filter(m -> "construct(long amount)".equals(m.get("signature"))).findFirst().orElseThrow();
            require(!parameter(plain).containsKey("semanticEvidence") && !parameter(plain).containsKey("objectFieldBinding"), "类标签和同名重载不传播");
            passed++;
            // 多个拆分参数独立绑定不同字段，来源可声明 path 或 artifact。
            Files.writeString(file, source("Number", PARAM, BINDING, SOURCE)
                    .replace("C construct(Number amount);", "C construct(Number amount, Number total);")
                    .replace(" * @mongoStage", " * @mongoParam total INTEGER_VALUE VALUE\n * @mongoObjectField total field=count encoding=INT32_EXACT minimum=0 maximum=10\n * @mongoObjectFieldSource total artifact=fixture:driver:1 symbols=encode(int) mechanism=独立字段写入 Int32。\n * @mongoStage"));
            MongoPlusApiIndex split = generator.generate();
            Map<?, ?> second = (Map<?, ?>) ((List<?>) overloads(split).get(0).get("parameters")).get(1);
            require("count".equals(((Map<?, ?>) second.get("objectFieldBinding")).get("field")), "多参数绑定");
            require(overloads(split).get(0).equals(publicMethods(split).get(0)), "多参数同步视图");
            passed++;
            reject(file, generator, Files.readString(file).replace("field=count", "field=quantity"), "重复对象字段");
            Files.writeString(file, source("Number", "", "", ""));
            require(!generator.generate().asMap().containsKey("requiredCapabilities"), "无绑定时不声明能力");
            passed++;
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
        System.out.println("PipelineObjectFieldBindingSelfTest PASSED: " + passed + " cases, 0 failures");
    }

    private static String source(String type, String parameter, String binding, String origin) {
        return "package com.mongoplus.aggregate; public interface Aggregate<C> {\n/**\n * "
                + parameter + "\n * " + binding + "\n * " + origin + "\n * @mongoStage $arbitrary\n */\n C construct(" + type + " amount);\n}\n";
    }

    private static void completeSample(MongoPlusApiIndex index) {
        Map<?, ?> method = sample(index);
        Map<?, ?> parameter = parameter(method);
        require("INTEGER_VALUE".equals(parameter.get("semanticType")), "真实 sample 必须为 INTEGER_VALUE");
        require("VALUE".equals(parameter.get("semanticScope")), "VALUE");
        require("PIPELINE_PARAMETER_INTEGER_VALUE".equals(parameter.get("conceptRef")), "INTEGER_VALUE conceptRef");
        Map<?, ?> binding = (Map<?, ?>) parameter.get("objectFieldBinding");
        require(binding != null && "size".equals(binding.get("field")), "size 字段绑定");
        require("INT32_EXACT".equals(binding.get("encoding")), "INT32_EXACT");
        require(Long.valueOf(1).equals(binding.get("minimum")) && Long.valueOf(Integer.MAX_VALUE).equals(binding.get("maximum")), "1..2147483647");
        require(((List<?>) binding.get("sourceEvidence")).size() == 3, "Core/Driver/MongoDB 来源");
        require(method.equals(publicMethods(index).stream().filter(m -> "sample(Number size)".equals(m.get("signature"))).findFirst().orElseThrow()), "sample 两个完整视图一致");
        require(((List<?>) index.asMap().get("requiredCapabilities")).contains("STAGE_OBJECT_FIELD_BINDING_V1"), "绑定能力声明");
        require(index.list("concepts").stream().map(c -> (Map<?, ?>) c).anyMatch(c -> "PIPELINE_PARAMETER_INTEGER_VALUE".equals(c.get("id"))), "整数 Concept 存在");
    }

    private static Map<?, ?> sample(MongoPlusApiIndex index) {
        return overloads(index).stream().filter(m -> "sample(Number size)".equals(m.get("signature"))).findFirst().orElseThrow();
    }

    private static List<Map<?, ?>> overloads(MongoPlusApiIndex index) {
        return index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                .flatMap(f -> ((List<?>) f.get("overloads")).stream()).<Map<?, ?>>map(m -> (Map<?, ?>) m).toList();
    }

    private static List<Map<?, ?>> publicMethods(MongoPlusApiIndex index) {
        return index.list("types").stream().map(t -> (Map<?, ?>) t)
                .flatMap(t -> ((List<?>) t.get("publicMethods")).stream()).<Map<?, ?>>map(m -> (Map<?, ?>) m).toList();
    }

    private static Map<?, ?> parameter(Map<?, ?> method) { return (Map<?, ?>) ((List<?>) method.get("parameters")).get(0); }

    private static void reject(Path file, MongoPlusIndexer generator, String source, String reason) throws Exception {
        Files.writeString(file, source);
        try { generator.generate(); } catch (IllegalArgumentException expected) {
            require(expected.getMessage().contains(reason), "应含原因 " + reason + "，实际: " + expected.getMessage());
            passed++;
            return;
        }
        throw new AssertionError("应拒绝: " + reason);
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
