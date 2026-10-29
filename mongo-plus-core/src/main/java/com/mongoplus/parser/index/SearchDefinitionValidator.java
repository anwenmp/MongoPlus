package com.mongoplus.parser.index;

import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.enums.SearchFieldType;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.json.JsonReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Search 定义的本地结构与跨选项校验，统一检查注解生成的 BSON 及用户填写的嵌套 JSON。
 * <p>实例只服务一个索引，引用和递归路径不跨索引共享；不推断服务器版本、部署配额或来源集合状态。</p>
 */
final class SearchDefinitionValidator {
    private static final Set<String> SORT_TYPES = set("boolean", "date", "number", "objectId", "token", "uuid");
    private static final Set<String> CONTAINER_TYPES = set("document", "embeddedDocuments");
    private static final Set<String> VECTOR_SIMILARITIES = set("euclidean", "cosine", "dotProduct");
    private static final Set<String> UNSUPPORTED_TOKENIZERS = set("nGram", "edgeGram");
    private static final Set<String> UNSUPPORTED_FILTERS =
            set("daitchMokotoffSoundex", "nGram", "edgeGram", "shingle");
    private final Document definition;
    private final Map<String, Document> analyzers = new HashMap<>();
    private final Map<String, Document> typeSets = new HashMap<>();
    private final List<String> vectorPaths = new ArrayList<>();

    SearchDefinitionValidator(Document definition) {
        this.definition = definition;
    }

    /** 先登记命名对象，再校验映射及依赖映射结果的源存储、排序和同义词配置。 */
    void validate() {
        long partitions = definition.containsKey("numPartitions") ? integer(definition.get("numPartitions"), "numPartitions") : 1;
        require(partitions == 1 || partitions == 2 || partitions == 4, "numPartitions", "must be 1, 2 or 4");
        // 先登记当前索引的命名对象，再解析字段引用；校验器实例不在索引之间复用。
        readNamedObjects("analyzers", analyzers);
        readNamedObjects("typeSets", typeSets);
        for (Map.Entry<String, Document> entry : analyzers.entrySet()) {
            String name = entry.getKey();
            require(!name.startsWith("lucene.") && !name.startsWith("builtin.") && !name.startsWith("mongodb."),
                    "analyzers." + name, "reserved analyzer name");
            required(document(entry.getValue().get("tokenizer"), "analyzers.tokenizer").get("type"), "tokenizer.type");
        }
        for (Document typeSet : typeSets.values()) {
            Set<String> types = new HashSet<>();
            List<?> entries = list(typeSet.get("types"), "typeSets.types");
            require(!entries.isEmpty(), "typeSets.types", "must not be empty");
            for (Object entry : entries) {
                Document field = document(entry, "typeSets.types");
                String type = required(field.get("type"), "typeSets.types.type");
                require(!CONTAINER_TYPES.contains(type) && !type.equals("vector") && !type.equals("knnVector")
                                && !type.endsWith("Facet"), "typeSets.types", "type not supported for dynamic mapping: " + type);
                require(types.add(type), "typeSets.types", "duplicate type: " + type);
                validateField(field, "typeSets.types", null, false);
            }
        }
        validateAnalyzer(definition.get("analyzer"), "analyzer", false, false);
        validateAnalyzer(definition.get("searchAnalyzer"), "searchAnalyzer", false, false);
        Document mappings = document(definition.get("mappings"), "mappings");
        validateMapping(mappings, "mappings", "", false);
        // 递归字段校验已收集向量路径，此时才能判断源文档存储是否覆盖了向量字段。
        if (definition.containsKey("storedSource")) {
            validateStoredSource(definition.get("storedSource"), "storedSource", vectorPaths);
        }
        if (definition.containsKey("sort")) {
            Document sort = document(definition.get("sort"), "sort");
            for (String path : sort.keySet()) {
                fieldName(path, true);
                require(sortable(mappings, path.split("\\."), 0), "sort." + path,
                        "requires a static sortable field and document mappings for all parents");
            }
        }
        Set<String> synonymNames = new HashSet<>();
        // 这里只检查定义内唯一性；Free/Flex 的单映射限制依赖部署，不能对所有索引统一限为一条。
        for (Object value : optionalList(definition, "synonyms")) {
            Document synonym = document(value, "synonyms");
            require(synonymNames.add(required(synonym.get("name"), "synonyms.name")),
                    "synonyms.name", "duplicate name");
            required(document(synonym.get("source"), "synonyms.source").get("collection"), "synonyms.source.collection");
            validateAnalyzer(required(synonym.get("analyzer"), "synonyms.analyzer"), "synonyms.analyzer", false, true);
        }
    }

    private void readNamedObjects(String key, Map<String, Document> target) {
        for (Object value : optionalList(definition, key)) {
            Document entry = document(value, key);
            String name = required(entry.get("name"), key + ".name");
            require(target.put(name, entry) == null, key + ".name", "duplicate name: " + name);
        }
    }

    /** path 用于错误定位，prefix 为实际 MongoDB 路径；embedded 标记沿所有后代映射传播。 */
    private void validateMapping(Document mapping, String path, String prefix, boolean embedded) {
        Object dynamic = mapping.containsKey("dynamic") ? mapping.get("dynamic") : false;
        if (dynamic instanceof Document) {
            Document reference = (Document) dynamic;
            String name = required(reference.get("typeSet"), path + ".dynamic.typeSet");
            require(reference.size() == 1 && typeSets.containsKey(name), path + ".dynamic",
                    "unknown typeSet or invalid reference: " + name);
        } else {
            require(dynamic instanceof Boolean, path + ".dynamic", "expected boolean or typeSet object");
        }
        Document fields = mapping.containsKey("fields") ? document(mapping.get("fields"), path + ".fields") : new Document();
        require(!Boolean.FALSE.equals(dynamic) || !fields.isEmpty(), path + ".fields",
                "static mapping requires fields");
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            fieldName(entry.getKey(), false);
            String fieldPath = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            for (Document field : fieldDefinitions(entry.getValue(), path + ".fields." + entry.getKey())) {
                validateField(field, path + ".fields." + entry.getKey(), fieldPath, embedded);
            }
        }
    }

    private void validateField(Document field, String path, String fieldPath, boolean embedded) {
        String type = required(field.get("type"), path + ".type");
        boolean known = false;
        for (SearchFieldType supported : SearchFieldType.values()) {
            known |= supported.getValue().equals(type);
        }
        require(known, path + ".type", "unknown field type: " + type);
        // 中间再嵌套 document 不会解除外层 embeddedDocuments 对后代类型的限制。
        require(!embedded || !type.endsWith("Facet"), path + ".type",
                "facet field types are not supported inside embeddedDocuments");
        require(!embedded || !type.equals("vector"), path + ".type",
                "vector fields are not supported inside embeddedDocuments");
        validateKeys(field, path, type);
        if (CONTAINER_TYPES.contains(type)) {
            // 保存递归前的边界，只将当前嵌套文档新增的向量路径用于它自己的 storedSource 校验。
            int vectorStart = vectorPaths.size();
            validateMapping(field, path, fieldPath == null ? "" : fieldPath,
                    embedded || type.equals("embeddedDocuments"));
            if (type.equals("embeddedDocuments") && field.containsKey("storedSource")) {
                List<String> relativeVectors = new ArrayList<>();
                for (String vector : vectorPaths.subList(vectorStart, vectorPaths.size())) {
                    if (fieldPath != null && vector.startsWith(fieldPath + ".")) {
                        relativeVectors.add(vector.substring(fieldPath.length() + 1));
                    }
                }
                validateStoredSource(field.get("storedSource"), path + ".storedSource", relativeVectors);
            }
        }
        if (type.equals("string") || type.equals("autocomplete")) {
            boolean autocomplete = type.equals("autocomplete");
            // 官方 Queries and Indexes 的 string|autocomplete 示例说明：字段未配置时使用顶层 analyzer。
            Object inheritedAnalyzer = definition.containsKey("analyzer") ? definition.get("analyzer") : "lucene.standard";
            validateAnalyzer(field.containsKey("analyzer") ? field.get("analyzer") : inheritedAnalyzer,
                    path + ".analyzer", autocomplete, false);
            if (!autocomplete) {
                validateAnalyzer(field.get("searchAnalyzer"), path + ".searchAnalyzer", false, false);
                oneOf(field, "indexOptions", path, "docs", "freqs", "positions", "offsets");
                oneOf(field, "norms", path, "include", "omit");
                bool(field, "store", path);
                if (field.containsKey("ignoreAbove")) {
                    require(integer(field.get("ignoreAbove"), path + ".ignoreAbove") >= 0, path + ".ignoreAbove",
                            "must be non-negative");
                }
                if (field.containsKey("multi")) {
                    Document multi = document(field.get("multi"), path + ".multi");
                    for (Map.Entry<String, Object> entry : multi.entrySet()) {
                        required(entry.getKey(), path + ".multi");
                        Document alternate = document(entry.getValue(), path + ".multi");
                        require("string".equals(alternate.get("type")), path + ".multi", "requires string mappings");
                        validateField(alternate, path + ".multi." + entry.getKey(), fieldPath, embedded);
                    }
                }
            } else {
                oneOf(field, "tokenization", path, "edgeGram", "rightEdgeGram", "nGram");
                long min = field.containsKey("minGrams") ? integer(field.get("minGrams"), path + ".minGrams") : 2;
                long max = field.containsKey("maxGrams") ? integer(field.get("maxGrams"), path + ".maxGrams") : 15;
                require(min > 0 && max >= min, path, "requires 0 < minGrams <= maxGrams");
                bool(field, "foldDiacritics", path);
            }
            if (field.containsKey("similarity")) {
                oneOf(document(field.get("similarity"), path + ".similarity"), "type", path + ".similarity",
                        "bm25", "boolean", "stableTfl");
                required(((Document) field.get("similarity")).get("type"), path + ".similarity.type");
            }
        } else if (type.equals("number") || type.equals("numberFacet")) {
            oneOf(field, "representation", path, "int64", "double");
            bool(field, "indexIntegers", path);
            bool(field, "indexDoubles", path);
            require(!Boolean.FALSE.equals(field.get("indexIntegers")) || !Boolean.FALSE.equals(field.get("indexDoubles")),
                    path, "indexIntegers and indexDoubles cannot both be false");
        } else if (type.equals("token")) {
            oneOf(field, "normalizer", path, "none", "lowercase");
        } else if (type.equals("geo")) {
            bool(field, "indexShapes", path);
        } else if (type.equals("vector") || type.equals("knnVector")) {
            String dimensionKey = type.equals("vector") ? "numDimensions" : "dimensions";
            long dimensions = integer(field.get(dimensionKey), path + "." + dimensionKey);
            require(dimensions > 0 && dimensions <= 8192, path + "." + dimensionKey, "must be in 1..8192");
            require(VECTOR_SIMILARITIES.contains(required(field.get("similarity"), path + ".similarity")),
                    path + ".similarity", "invalid vector similarity");
            if (type.equals("vector")) {
                if (fieldPath != null) {
                    vectorPaths.add(fieldPath);
                }
                oneOf(field, "quantization", path, "none", "scalar", "binary");
                require(!"binary".equals(field.get("quantization")) || dimensions % 8 == 0, path + ".numDimensions",
                        "binary quantization requires a multiple of 8");
                oneOf(field, "indexingMethod", path, "hnsw", "flat");
                if (field.containsKey("hnswOptions")) {
                    require(!"flat".equals(field.get("indexingMethod")), path + ".hnswOptions", "not supported with flat");
                    Document options = document(field.get("hnswOptions"), path + ".hnswOptions");
                    require(set("maxEdges", "numEdgeCandidates").containsAll(options.keySet()),
                            path + ".hnswOptions", "unsupported HNSW option");
                    range(options, "maxEdges", 16, 64, path);
                    range(options, "numEdgeCandidates", 100, 3200, path);
                }
            }
        }
    }

    private void validateAnalyzer(Object value, String path, boolean autocomplete, boolean synonym) {
        if (value == null) {
            return;
        }
        String name = required(value, path);
        require(!"lucene.default".equals(name), path, "DEFAULT must not be emitted as lucene.default");
        boolean builtin = Arrays.stream(com.mongoplus.enums.Analyzer.values())
                .anyMatch(analyzer -> analyzer != com.mongoplus.enums.Analyzer.DEFAULT
                        && analyzer.getAnalyzer().equals(name));
        require(builtin || analyzers.containsKey(name), path, "undefined or unsupported analyzer: " + name);
        if (autocomplete || synonym) {
            require(!name.equals("lucene.kuromoji") && (!synonym || !name.equals("lucene.cjk")),
                    path, "unsupported analyzer: " + name);
            Document custom = analyzers.get(name);
            if (custom != null) {
                Document tokenizer = document(custom.get("tokenizer"), path + ".tokenizer");
                require(!UNSUPPORTED_TOKENIZERS.contains(tokenizer.get("type")), path, "unsupported tokenizer");
                for (Object filter : optionalList(custom, "tokenFilters")) {
                    String type = required(document(filter, path + ".tokenFilters").get("type"), path + ".tokenFilters.type");
                    require(!UNSUPPORTED_FILTERS.contains(type) && (!synonym || !type.equals("wordDelimiterGraph")),
                            path, "unsupported token filter: " + type);
                }
            }
        }
    }

    private boolean sortable(Document mapping, String[] parts, int offset) {
        // 预排序依赖逐级静态 document 映射，动态映射或 embeddedDocuments 不能替代父路径。
        if (!(mapping.get("fields") instanceof Document)) {
            return false;
        }
        Object value = ((Document) mapping.get("fields")).get(parts[offset]);
        if (value == null) {
            return false;
        }
        for (Document field : fieldDefinitions(value, "sort")) {
            if (offset == parts.length - 1 && SORT_TYPES.contains(field.get("type"))) {
                return true;
            }
            if (offset < parts.length - 1 && "document".equals(field.get("type"))
                    && sortable(field, parts, offset + 1)) {
                return true;
            }
        }
        return false;
    }

    /** 校验源存储形态，并确保 include/exclude 均不会把已知静态向量字段纳入存储。 */
    private static void validateStoredSource(Object value, String path, List<String> vectors) {
        if (value instanceof Boolean) {
            require(!Boolean.TRUE.equals(value) || vectors.isEmpty(), path, "true cannot store vector fields");
            return;
        }
        Document source = document(value, path);
        require(source.size() == 1 && (source.containsKey("include") || source.containsKey("exclude")),
                path, "expected exactly one of include or exclude");
        boolean include = source.containsKey("include");
        List<?> paths = list(source.get(include ? "include" : "exclude"), path);
        for (Object field : paths) {
            fieldName(required(field, path), true);
        }
        for (String vector : vectors) {
            boolean covered = false;
            // 选择整个父对象也会包含其中的向量；排除父对象则同时排除其下的所有向量。
            for (Object field : paths) {
                String fieldPath = (String) field;
                covered |= vector.equals(fieldPath) || vector.startsWith(fieldPath + ".");
            }
            require(include ? !covered : covered, path, "vector field must be excluded from storage: " + vector);
        }
    }

    /** 将单映射和多类型映射统一为遍历列表；空数组保留“排除字段”语义，不生成映射。 */
    private static List<Document> fieldDefinitions(Object value, String path) {
        List<Document> fields = new ArrayList<>();
        if (value instanceof List) {
            for (Object field : (List<?>) value) {
                fields.add(document(field, path));
            }
        } else {
            fields.add(document(value, path));
        }
        return fields;
    }

    /** 解析且完整消费一个 JSON 对象，避免合法前缀掩盖尾随配置。 */
    static Document object(String json, String path) {
        require(json.trim().startsWith("{"), path, "expected a JSON object");
        try (JsonReader reader = new JsonReader(json)) {
            Document parsed = MapCodecCache.getDefaultCodec().decode(reader, DecoderContext.builder().build());
            // Driver 解码完成一个对象不代表输入已结束，必须再检查尾部，不能静默丢弃第二个对象。
            require(reader.readBsonType() == BsonType.END_OF_DOCUMENT, path,
                    "expected exactly one JSON object without trailing content");
            return parsed;
        } catch (MongoPlusConvertException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw failure(path + ": invalid JSON object", exception);
        }
    }

    static Document document(Object value, String path) {
        require(value instanceof Document, path, "expected an object");
        return (Document) value;
    }

    static String required(Object value, String path) {
        require(value instanceof String && !((String) value).trim().isEmpty(), path, "expected a non-blank string");
        return (String) value;
    }

    static String fieldName(String name, boolean dotted) {
        required(name, "fieldName");
        require(dotted || !name.contains("."), "fieldName", "static fields require nested mappings instead of dot paths");
        for (String part : name.split("\\.", -1)) {
            require(!part.trim().isEmpty() && !part.startsWith("$") && part.indexOf('\0') < 0, "fieldName",
                    "invalid field path: " + name);
        }
        return name;
    }

    static void require(boolean condition, String path, String message) {
        if (!condition) {
            throw new MongoPlusConvertException(path + ": " + message);
        }
    }

    static MongoPlusConvertException failure(String message, Throwable cause) {
        MongoPlusConvertException exception = new MongoPlusConvertException(message);
        exception.initCause(cause);
        return exception;
    }

    private static Set<String> set(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    /** 嵌套 JSON 不经过注解成员校验，因此也需按类型限制字段属性，防止透传不适用选项。 */
    private static void validateKeys(Document field, String path, String type) {
        Set<String> allowed = set("type");
        switch (type) {
            case "string":
                allowed.addAll(set("analyzer", "searchAnalyzer", "indexOptions", "store", "ignoreAbove", "norms", "similarity", "multi"));
                break;
            case "autocomplete":
                allowed.addAll(set("analyzer", "tokenization", "minGrams", "maxGrams", "foldDiacritics", "similarity"));
                break;
            case "embeddedDocuments":
                allowed.add("storedSource");
                allowed.addAll(set("dynamic", "fields"));
                break;
            case "document":
                allowed.addAll(set("dynamic", "fields"));
                break;
            case "number":
            case "numberFacet":
                allowed.addAll(set("representation", "indexIntegers", "indexDoubles"));
                break;
            case "token": allowed.add("normalizer"); break;
            case "geo": allowed.add("indexShapes"); break;
            case "vector":
                allowed.addAll(set("numDimensions", "similarity", "quantization", "indexingMethod", "hnswOptions"));
                break;
            case "knnVector": allowed.addAll(set("dimensions", "similarity")); break;
            default: break;
        }
        require(allowed.containsAll(field.keySet()), path, "unsupported options for " + type + ": " + field.keySet());
    }

    private static List<?> list(Object value, String path) {
        require(value instanceof List, path, "expected an array");
        return (List<?>) value;
    }

    private static List<?> optionalList(Document document, String key) {
        return document.containsKey(key) ? list(document.get(key), key) : new ArrayList<>();
    }

    private static long integer(Object value, String path) {
        require(value instanceof Integer || value instanceof Long, path, "expected an integer");
        return ((Number) value).longValue();
    }

    private static void oneOf(Document field, String key, String path, String... values) {
        if (field.containsKey(key)) {
            require(Arrays.asList(values).contains(field.get(key)), path + "." + key, "unsupported value: " + field.get(key));
        }
    }

    private static void bool(Document field, String key, String path) {
        if (field.containsKey(key)) {
            require(field.get(key) instanceof Boolean, path + "." + key, "expected a boolean");
        }
    }

    private static void range(Document field, String key, long minimum, long maximum, String path) {
        if (field.containsKey(key)) {
            long value = integer(field.get(key), path + "." + key);
            require(value >= minimum && value <= maximum, path + "." + key, "outside allowed range");
        }
    }
}
