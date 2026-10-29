package com.mongoplus.parser.index;

import com.mongoplus.annotation.index.search.MongoSearchField;
import org.bson.Document;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.mongoplus.parser.index.SearchDefinitionValidator.*;

/**
 * 搜索字段注解的类型专属选项转换；JSON 嵌套映射交给统一定义校验器检查。
 */
final class SearchFieldParser {

    private SearchFieldParser() {
    }

    /** 将一份非排除映射转换为 BSON，按当前类型输出适用选项，并拒绝不适用的非默认属性。 */
    @SuppressWarnings("deprecation")
    static Document parse(MongoSearchField field) {
        Document definition = new Document("type", field.type().getValue());
        Set<String> options = new HashSet<>();
        // 注解包含所有类型的参数，但 BSON 只输出当前类型支持的选项，避免默认值污染其他类型。
        switch (field.type()) {
            case STRING:
                allow(options, "analyzer", "customAnalyzer", "searchAnalyzer", "customSearchAnalyzer",
                        "indexOptions", "store", "ignoreAbove", "norms", "similarity", "multi");
                SearchParse.putAnalyzer(definition, "searchAnalyzer", field.searchAnalyzer(),
                        field.customSearchAnalyzer(), false);
                definition.append("indexOptions", field.indexOptions().getValue()).append("store", field.store())
                        .append("norms", field.norms().getValue());
                if (field.ignoreAbove() != -1) {
                    definition.put("ignoreAbove", field.ignoreAbove());
                }
                if (!field.multi().isEmpty()) {
                    definition.put("multi", object(field.multi(), "multi"));
                }
                textOptions(definition, field);
                break;
            case AUTOCOMPLETE:
                allow(options, "analyzer", "customAnalyzer", "tokenization", "minGrams",
                        "maxGrams", "foldDiacritics", "similarity");
                putOption(definition, "tokenization", field.tokenization().getValue());
                definition.append("minGrams", field.minGrams()).append("maxGrams", field.maxGrams())
                        .append("foldDiacritics", field.foldDiacritics());
                textOptions(definition, field);
                break;
            case DOCUMENT:
            case EMBEDDED_DOCUMENTS:
                allow(options, "dynamic", "dynamicTypeSet", "fields");
                definition.put("dynamic", SearchParse.dynamicValue(field.dynamic(), field.dynamicTypeSet(), "dynamic"));
                if (!field.fields().isEmpty()) {
                    definition.put("fields", object(field.fields(), "fields"));
                }
                if ("embeddedDocuments".equals(field.type().getValue())) {
                    allow(options, "storedSource");
                    if (!field.storedSource().isEmpty()) {
                        // 用临时对象解析单个 JSON 值，保留布尔/对象类型，不能把 JSON 文本作为字符串提交。
                        Document wrapper = object("{\"value\":" + field.storedSource() + "}", "storedSource");
                        require(wrapper.size() == 1, "storedSource", "expected one JSON value");
                        definition.put("storedSource", wrapper.get("value"));
                    }
                }
                break;
            case NUMBER:
            case NUMBER_FACET:
                allow(options, "representation", "indexIntegers", "indexDoubles");
                definition.append("representation", field.representation().getValue())
                        .append("indexIntegers", field.indexIntegers()).append("indexDoubles", field.indexDoubles());
                break;
            case TOKEN:
                allow(options, "normalizer");
                definition.put("normalizer", field.normalizer().getValue());
                break;
            case GEO:
                allow(options, "indexShapes");
                definition.put("indexShapes", field.indexShapes());
                break;
            case VECTOR:
                allow(options, "numDimensions", "vectorSimilarity", "quantization", "indexingMethod", "hnswOptions");
                definition.put("numDimensions", field.numDimensions());
                putOption(definition, "quantization", field.quantization().getValue());
                putOption(definition, "indexingMethod", field.indexingMethod().getValue());
                if (!field.hnswOptions().isEmpty()) {
                    definition.put("hnswOptions", object(field.hnswOptions(), "hnswOptions"));
                }
                definition.put("similarity", field.vectorSimilarity().getValue());
                break;
            case KNN_VECTOR:
                allow(options, "dimensions", "vectorSimilarity");
                definition.append("dimensions", field.dimensions()).append("similarity", field.vectorSimilarity().getValue());
                break;
            default:
                break;
        }
        // 对不适用且被显式改变的参数报错，不能悄悄忽略用户配置。
        validateOptions(field, options);
        return definition;
    }

    private static void textOptions(Document definition, MongoSearchField field) {
        SearchParse.putAnalyzer(definition, "analyzer", field.analyzer(), field.customAnalyzer(), false);
        if (!field.similarity().getValue().isEmpty()) {
            // 文本相似度为对象；向量相似度在对应分支输出字符串，两种 BSON 形态不可混用。
            definition.put("similarity", new Document("type", field.similarity().getValue()));
        }
    }

    private static void putOption(Document definition, String key, String value) {
        // 空字符串是注解的省略哨兵；真实枚举值 none 不为空，仍需正常提交。
        if (!value.isEmpty()) {
            definition.put(key, value);
        }
    }

    private static void allow(Set<String> options, String... names) {
        options.addAll(Arrays.asList(names));
    }

    private static void validateOptions(MongoSearchField field, Set<String> options) {
        // name 等归属参数由外层解析器处理；这里只检查当前类型不支持的字段参数。
        Arrays.stream(MongoSearchField.class.getDeclaredMethods())
                .filter(method -> !Arrays.asList("name", "fieldName", "type", "exclude").contains(method.getName()))
                .filter(method -> !options.contains(method.getName()))
                .forEach(method -> validateDefault(field, method));
    }

    private static void validateDefault(MongoSearchField field, Method method) {
        String name = method.getName();
        try {
            // Java 注解无法区分“未填写”和“显式填写默认值”，这里只能拒绝不适用且偏离默认值的配置。
            require(Objects.deepEquals(method.getDefaultValue(), method.invoke(field)), name,
                    "option does not apply to " + field.type().getValue());
        } catch (ReflectiveOperationException exception) {
            throw failure("Cannot read MongoSearchField." + name, exception);
        }
    }
}
