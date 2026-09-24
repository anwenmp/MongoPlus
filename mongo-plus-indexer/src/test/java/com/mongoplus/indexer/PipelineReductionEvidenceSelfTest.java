package com.mongoplus.indexer;

import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 归约仅取当前声明的证据；元素表示、契约错误及旧证据隔离均在生成边界验证。 */
public final class PipelineReductionEvidenceSelfTest {
    private static final String OWNER = "fixture.Composer";
    private static final String PARAM = "@mongoParam parts STAGE_BODY_DOCUMENT ELEMENT";
    private static final String REDUCTION = "@mongoReduction parts -> STAGE_BODY_DOCUMENT "
            + "operation=DOCUMENT_MERGE order=INPUT duplicateKeys=LAST_WINS depth=SHALLOW empty=EMPTY_DOCUMENT";
    private static int passed;

    private PipelineReductionEvidenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("pipeline-reduction-");
        try {
            Path aggregate = root.resolve("com/mongoplus/aggregate/Aggregate.java");
            Files.createDirectories(aggregate.getParent());
            Files.writeString(aggregate, "package com.mongoplus.aggregate; public interface Aggregate<C> {}");
            Path file = root.resolve("fixture/Composer.java");
            Files.createDirectories(file.getParent());
            MongoPlusIndexer generator = new MongoPlusIndexer(MongoPlusIndexerConfig.builder()
                    .addSourceRoot(root).pipeline(true).addExpressionRoot(OWNER).build());
            for (String type : List.of("Bson...", "Bson[]", "List<Bson>", "List<? extends Bson>",
                    "java.util.List<? extends org.bson.conversions.Bson>")) {
                Files.writeString(file, source(type, PARAM, REDUCTION));
                complete(generator.generate());
                passed++;
            }
            reject(file, generator, source("List<?>", PARAM, REDUCTION), "无界通配符");
            reject(file, generator, source("List<? super Bson>", PARAM, REDUCTION), "下界通配符");
            reject(file, generator, source("List", PARAM, REDUCTION), "原始 List");
            reject(file, generator, source("List<List<Bson>>", PARAM, REDUCTION), "嵌套容器");
            reject(file, generator, source("List<? extends Bson>", PARAM, REDUCTION)
                    .replace("import org.bson.conversions.Bson;", "import other.Bson;"),
                    "org.bson.conversions.Bson");
            reject(file, generator, source("Bson", PARAM, REDUCTION), "数组、varargs 或 java.util.List");
            reject(file, generator, source("List<String>", PARAM, REDUCTION), "org.bson.conversions.Bson");
            reject(file, generator, source("List<Bson>", "", REDUCTION), "缺少显式 @mongoParam");
            reject(file, generator, source("Bson", PARAM.replace("ELEMENT", "VALUE"), REDUCTION), "必须为 ELEMENT");
            reject(file, generator, source("List<Bson>", PARAM, REDUCTION.replace("parts ->", "missing ->")), "参数不存在");
            reject(file, generator, source("List<Bson>", PARAM, REDUCTION.replace("-> STAGE_BODY_DOCUMENT", "-> UNKNOWN")), "输出语义");
            reject(file, generator, source("List<Bson>", PARAM.replace("STAGE_BODY_DOCUMENT", "PIPELINE_EXPRESSION"), REDUCTION), "输入语义");
            for (String attribute : List.of("operation=DOCUMENT_MERGE", "order=INPUT", "duplicateKeys=LAST_WINS",
                    "depth=SHALLOW", "empty=EMPTY_DOCUMENT")) {
                reject(file, generator, source("List<Bson>", PARAM, REDUCTION.replace(attribute, "")), "缺少属性");
                reject(file, generator, source("List<Bson>", PARAM,
                        REDUCTION.replace(attribute, attribute.substring(0, attribute.indexOf('=') + 1) + "UNKNOWN")),
                        "不支持的属性值");
            }
            reject(file, generator, source("List<Bson>", PARAM, REDUCTION + " order=INPUT"), "重复属性");
            reject(file, generator, source("List<Bson>", PARAM, REDUCTION + " extra=YES"), "未知属性");
            reject(file, generator, source("List<Bson>", PARAM, REDUCTION + "\n * " + REDUCTION), "只能声明一次");
            reject(file, generator, source("List<Bson>", PARAM, REDUCTION
                    + "\n * @mongoComposition STAGE_BODY_DOCUMENT -> PIPELINE_STAGE_DOCUMENT"), "结果冲突");
            Files.writeString(file, source("List<Bson>", PARAM, ""));
            MongoPlusApiIndex withoutReduction = generator.generate();
            require(!withoutReduction.asMap().containsKey("requiredCapabilities"), "未用到归约不能声明能力");
            require(!method(withoutReduction).containsKey("resultSemanticType"), "参数不能推断结果语义");
            passed++;
            Files.writeString(file, source("List<Bson>", "", "")
                    .replace("public class Composer", "/** " + REDUCTION + " */ public class Composer"));
            require(!method(generator.generate()).containsKey("reductionContract"), "类级标签不能传播");
            passed++;
            Files.writeString(file, source("List<Bson>", PARAM, REDUCTION)
                    .replace("public class Composer {", "public class Composer { public static Bson assemble(Bson value) { return null; }"));
            MongoPlusApiIndex isolated = generator.generate();
            for (Map<?, ?> method : methods(isolated)) {
                if (((List<?>) method.get("parameterTypes")).equals(List.of("Bson"))) {
                    require(!method.containsKey("reductionContract") && !method.containsKey("resultSemanticType"),
                            "相邻 overload 不得获得归约证据");
                }
            }
            passed++;
            // 真实 API 仅是上述通用标签的应用，不作为提取器的名称开关。
            MongoPlusApiIndex real = new MongoPlusIndexer(MongoPlusIndexerConfig.forPipelineProject(Path.of(args[0])).build()).generate();
            long fields = real.list("types").stream().map(item -> (Map<?, ?>) item)
                    .flatMap(type -> ((List<?>) type.get("publicMethods")).stream()).map(item -> (Map<?, ?>) item)
                    .filter(method -> "fields".equals(method.get("name")) && method.containsKey("reductionContract")).count();
            require(fields == 2, "两个真实 fields overload 都必须有独立证据");
            passed++;
            System.out.println("PipelineReductionEvidenceSelfTest PASSED: " + passed + " cases, 0 failures");
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    private static String source(String type, String parameter, String reduction) {
        return "package fixture; import java.util.List; import org.bson.conversions.Bson; public class Composer {\n"
                + "/**\n * " + parameter + "\n * " + reduction + "\n */\n"
                + "public static Bson assemble(" + type + " parts) { return null; } }";
    }

    private static void complete(MongoPlusApiIndex index) {
        Map<?, ?> method = method(index);
        require("STAGE_BODY_DOCUMENT".equals(method.get("resultSemanticType")), "结果语义");
        require(Map.of("inputParameter", "parts", "inputScope", "ELEMENT", "inputSemanticType", "STAGE_BODY_DOCUMENT",
                "operation", "DOCUMENT_MERGE", "order", "INPUT", "duplicateKeys", "LAST_WINS",
                "depth", "SHALLOW", "empty", "EMPTY_DOCUMENT").equals(method.get("reductionContract")), "完整归约契约");
        require(Map.of("source", "JAVADOC", "tag", "mongoReduction", "value", REDUCTION.substring(16))
                .equals(method.get("resultSemanticEvidence")), "结果追溯到当前标签");
        Map<?, ?> parameter = (Map<?, ?>) ((List<?>) method.get("parameters")).get(0);
        require("PIPELINE_PARAMETER_STAGE_BODY_DOCUMENT_ELEMENT".equals(parameter.get("conceptRef")), "中性元素 Concept");
        require(List.of("DOCUMENT_REDUCTION_V1").equals(index.asMap().get("requiredCapabilities")), "仅声明实际使用能力");
        require(!method.containsKey("compositionSemantics"), "不伪造旧 composition evidence");
    }

    private static List<Map<?, ?>> methods(MongoPlusApiIndex index) {
        return index.list("types").stream().map(item -> (Map<?, ?>) item)
                .flatMap(type -> ((List<?>) type.get("publicMethods")).stream()).<Map<?, ?>>map(item -> (Map<?, ?>) item)
                .filter(method -> OWNER.equals(method.get("declaredIn"))).toList();
    }

    private static Map<?, ?> method(MongoPlusApiIndex index) { return methods(index).get(0); }

    private static void reject(Path file, MongoPlusIndexer generator, String source, String reason) throws Exception {
        Files.writeString(file, source);
        try { generator.generate(); } catch (IllegalArgumentException expected) {
            require(expected.getMessage().contains(reason), "应包含原因 " + reason + "，实际: " + expected.getMessage());
            System.out.println("EXPECTED REJECTION: " + expected.getMessage());
            passed++;
            return;
        }
        throw new AssertionError("应拒绝: " + reason);
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
