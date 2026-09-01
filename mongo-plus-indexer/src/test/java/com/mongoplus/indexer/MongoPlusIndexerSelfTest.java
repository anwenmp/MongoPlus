package com.mongoplus.indexer;

import com.mongoplus.indexer.json.JsonWriter;
import com.mongoplus.indexer.model.MongoPlusApiIndex;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/** 不引入测试框架的可执行回归测试。 */
public final class MongoPlusIndexerSelfTest {
    private MongoPlusIndexerSelfTest() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) { throw new IllegalArgumentException("需要传入 MongoPlus 根目录"); }
        Path root = Paths.get(args[0]).toAbsolutePath().normalize();
        Path first = Files.createTempFile("mongo-plus-index-", ".json");
        Path second = Files.createTempFile("mongo-plus-index-", ".json");
        try {
            MongoPlusIndexerConfig config = MongoPlusIndexerConfig.forMongoPlusProject(root)
                    .output(first)
                    .build();
            MongoPlusIndexer indexer = new MongoPlusIndexer(config);
            MongoPlusApiIndex index = indexer.generate();
            assertFamily(index, "eq", 4);
            assertFamily(index, "like", 8);
            assertLikeMetadata(index);
            assertOperator(index, "eq", "$eq");
            assertOperator(index, "gte", "$gte");
            assertOperator(index, "in", "$in");
            assertOperator(index, "or", "$or");
            assertOperator(index, "like", "$regex");
            assertFieldChainMethods(index);
            assertSFunctionSemantics(index);
            assertWrapperConstructors(index);
            assertSpecial(index, "SFunction", "LAMBDA_FIELD");
            assertSpecial(index, "FieldChain", "NESTED_FIELD_BUILDER");
            assertRegexOptions(index);
            indexer.write(index, first);
            indexer.write(indexer.generate(), second);
            require(java.util.Arrays.equals(Files.readAllBytes(first), Files.readAllBytes(second)), "连续生成结果不稳定");
            StringWriter writer = new StringWriter();
            JsonWriter.write(java.util.Arrays.asList("\"", "\\", "\n", "\t", null, true, 1), writer);
            require(writer.toString().contains("\\\"") && writer.toString().contains("\\\\"), "JSON 转义失败");
            System.out.println("MongoPlusIndexerSelfTest PASSED");
        } finally {
            Files.deleteIfExists(first);
            Files.deleteIfExists(second);
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertFamily(MongoPlusApiIndex index, String name, int overloadCount) {
        for (Object item : index.getMethodFamilies()) {
            Map<String, Object> family = (Map<String, Object>) item;
            if (name.equals(family.get("name"))) {
                require(((List<Object>) family.get("overloads")).size() == overloadCount, name + " overload 数量错误");
                return;
            }
        }
        throw new AssertionError("缺少方法族: " + name);
    }

    @SuppressWarnings("unchecked")
    private static void assertSpecial(MongoPlusApiIndex index, String name, String semanticType) {
        for (Object item : index.list("specialTypes")) {
            Map<String, Object> type = (Map<String, Object>) item;
            if (name.equals(type.get("name"))) {
                require(semanticType.equals(type.get("semanticType")), name + " 语义分类错误");
                return;
            }
        }
        throw new AssertionError("缺少特殊类型: " + name);
    }

    @SuppressWarnings("unchecked")
    private static void assertRegexOptions(MongoPlusApiIndex index) {
        for (Object item : index.list("types")) {
            Map<String, Object> type = (Map<String, Object>) item;
            if ("RegexOptions".equals(type.get("name"))) {
                require(((List<Object>) type.get("constants")).size() == 5, "RegexOptions 常量数量错误");
                return;
            }
        }
        throw new AssertionError("缺少 RegexOptions");
    }

    @SuppressWarnings("unchecked")
    private static void assertLikeMetadata(MongoPlusApiIndex index) {
        for (Object item : index.getMethodFamilies()) {
            Map<String, Object> family = (Map<String, Object>) item;
            if ("like".equals(family.get("name"))) {
                require(((List<Object>) family.get("mongoOperators")).contains("$regex"), "like 缺少 MongoDB 操作符");
                require(((List<Object>) family.get("aliases")).size() == 3, "like 别名合并错误");
                return;
            }
        }
        throw new AssertionError("缺少 like 方法族");
    }

    @SuppressWarnings("unchecked")
    private static void assertOperator(MongoPlusApiIndex index, String familyName, String operator) {
        Map<String, Object> family = findNamed(index.getMethodFamilies(), familyName);
        require(((List<Object>) family.get("mongoOperators")).contains(operator),
                familyName + " 缺少操作符 " + operator);
    }

    @SuppressWarnings("unchecked")
    private static void assertFieldChainMethods(MongoPlusApiIndex index) {
        Map<String, Object> fieldChain = findNamed(index.list("specialTypes"), "FieldChain");
        require("NESTED_FIELD_BUILDER".equals(fieldChain.get("semanticType")), "FieldChain 语义错误");
        for (Object item : (List<Object>) fieldChain.get("publicMethods")) {
            Map<String, Object> method = (Map<String, Object>) item;
            if ("build()".equals(method.get("signature"))) {
                require("String".equals(method.get("returnType")), "FieldChain.build() 返回类型错误");
                require(((List<Object>) method.get("parameters")).isEmpty(), "FieldChain.build() 不应有参数");
                assertStringColumnCompatibility(index, (String) method.get("returnType"));
                return;
            }
        }
        throw new AssertionError("缺少结构化 FieldChain.build() 方法");
    }

    @SuppressWarnings("unchecked")
    private static void assertStringColumnCompatibility(MongoPlusApiIndex index, String returnType) {
        Map<String, Object> eq = findNamed(index.getMethodFamilies(), "eq");
        for (Object item : (List<Object>) eq.get("overloads")) {
            Map<String, Object> overload = (Map<String, Object>) item;
            List<Object> parameters = (List<Object>) overload.get("parameters");
            if (parameters.size() == 2) {
                Map<String, Object> column = (Map<String, Object>) parameters.get(0);
                if (returnType.equals(column.get("type"))) { return; }
            }
        }
        throw new AssertionError("FieldChain.build() 返回类型与 eq(String,Object) 不兼容");
    }

    @SuppressWarnings("unchecked")
    private static void assertSFunctionSemantics(MongoPlusApiIndex index) {
        Map<String, Object> eq = findNamed(index.getMethodFamilies(), "eq");
        require(hasSemanticType((List<Object>) eq.get("overloads"), "LAMBDA_FIELD"),
                "eq(SFunction...) 未识别为字段 Lambda");
        Map<String, Object> or = findNamed(index.getMethodFamilies(), "or");
        require(hasSemanticType((List<Object>) or.get("overloads"), "CONDITION_GROUP_LAMBDA"),
                "or(SFunction<QueryChainWrapper...>) 未识别为条件分组 Lambda");
    }

    @SuppressWarnings("unchecked")
    private static boolean hasSemanticType(List<Object> overloads, String semanticType) {
        for (Object item : overloads) {
            Map<String, Object> overload = (Map<String, Object>) item;
            for (Object parameterItem : (List<Object>) overload.get("parameters")) {
                Map<String, Object> parameter = (Map<String, Object>) parameterItem;
                if (semanticType.equals(parameter.get("semanticType"))) { return true; }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static void assertWrapperConstructors(MongoPlusApiIndex index) {
        Map<String, Object> queryWrapper = findNamed(index.list("wrappers"), "QueryWrapper");
        require(Boolean.FALSE.equals(queryWrapper.get("abstractType")), "QueryWrapper 不应为抽象类型");
        assertImplicitPublicNoArgConstructor(queryWrapper, "QueryWrapper");

        Map<String, Object> queryChainWrapper = findNamed(index.list("wrappers"), "QueryChainWrapper");
        require(Boolean.TRUE.equals(queryChainWrapper.get("abstractType")), "QueryChainWrapper 应为抽象类型");
        assertImplicitPublicNoArgConstructor(queryChainWrapper, "QueryChainWrapper");

        Map<String, Object> lambdaQueryChainWrapper = findNamed(
                index.list("wrappers"), "LambdaQueryChainWrapper");
        List<Object> constructors = (List<Object>) lambdaQueryChainWrapper.get("constructors");
        require(constructors.size() == 1, "LambdaQueryChainWrapper constructor 数量错误");
        Map<String, Object> constructor = (Map<String, Object>) constructors.get(0);
        require("LambdaQueryChainWrapper(BaseMapper baseMapper, Class<T> clazz)"
                .equals(constructor.get("signature")), "显式 constructor 签名错误");
        require(Boolean.FALSE.equals(constructor.get("implicit")), "显式 constructor 不应标记 implicit");
        List<Object> parameters = (List<Object>) constructor.get("parameters");
        require(parameters.size() == 2, "显式 constructor 参数数量错误");
        require("BaseMapper".equals(((Map<String, Object>) parameters.get(0)).get("type")),
                "baseMapper 参数类型错误");
        require("Class<T>".equals(((Map<String, Object>) parameters.get(1)).get("type")),
                "clazz 参数类型错误");
    }

    @SuppressWarnings("unchecked")
    private static void assertImplicitPublicNoArgConstructor(Map<String, Object> wrapper, String name) {
        List<Object> constructors = (List<Object>) wrapper.get("constructors");
        require(constructors.size() == 1, name + " constructor 数量错误");
        Map<String, Object> constructor = (Map<String, Object>) constructors.get(0);
        require((name + "()").equals(constructor.get("signature")), name + " constructor 签名错误");
        require("public".equals(constructor.get("visibility")), name + " constructor 可见性错误");
        require(Boolean.TRUE.equals(constructor.get("implicit")), name + " 应记录隐式 constructor");
        require(((List<Object>) constructor.get("parameters")).isEmpty(), name + " constructor 不应有参数");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> findNamed(List<Object> values, String name) {
        for (Object item : values) {
            Map<String, Object> value = (Map<String, Object>) item;
            if (name.equals(value.get("name"))) { return value; }
        }
        throw new AssertionError("缺少索引项: " + name);
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
