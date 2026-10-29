package com.mongoplus.toolkit;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.SearchIndexModel;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动索引流程中的 Search DDL 执行边界，负责识别重复初始化并创建缺失索引。
 * <p>同名索引必须同时满足定义兼容和状态可接受；不隐式更新、删除或修复已有索引。
 * 创建请求异步执行，本类不等待 READY，也不保证索引立即可查询。</p>
 */
final class SearchIndexCreator {
    private static final Log log = LogFactory.getLog(SearchIndexCreator.class);

    private SearchIndexCreator() {
    }

    /**
     * 校验全部同名索引后，一次提交缺失模型；配置冲突和 Driver 异常继续阻断调用方。
     * <p>列举与创建不是原子操作，并发初始化仍可能收到服务端重名错误。</p>
     *
     * @param collection 已按实体和数据源选定的目标集合
     * @param models 当前实体解析得到的 Search 模型，空列表不访问 Search 管理接口
     */
    static void createMissing(MongoCollection<Document> collection, List<SearchIndexModel> models) {
        if (CollUtil.isEmpty(models)) {
            return;
        }
        Map<String, Document> existing = new HashMap<>();
        // 使用完整游标遍历而非只看首批结果，并在读取或校验失败前释放服务端游标。
        try (MongoCursor<Document> cursor = collection.listSearchIndexes().iterator()) {
            while (cursor.hasNext()) {
                Document index = cursor.next();
                existing.put(index.getString("name"), index);
            }
        }
        List<SearchIndexModel> missing = new ArrayList<>();
        for (SearchIndexModel model : models) {
            // Driver 省略名称时服务端使用 default，重名检查必须使用同一解释。
            String name = model.getName() == null ? "default" : model.getName();
            Document current = existing.get(name);
            if (current == null) {
                missing.add(model);
                continue;
            }
            BsonDocument requested = model.getDefinition().toBsonDocument(Document.class, collection.getCodecRegistry());
            // BUILDING 时实际查询可能仍使用旧定义；重复提交应比较最新请求定义，而非某个节点的 mainIndex。
            Object latest = current.get("latestDefinition");
            BsonDocument actual = latest instanceof Document
                    ? ((Document) latest).toBsonDocument(Document.class, collection.getCodecRegistry()) : null;
            String status = current.getString("status");
            // BSON 对象相等不比较键顺序，但预排序的字段优先顺序有语义，必须单独比较。
            boolean sameSort = actual != null && (!requested.containsKey("sort") && !actual.containsKey("sort")
                    || requested.containsKey("sort") && actual.containsKey("sort")
                    && new ArrayList<>(requested.getDocument("sort").keySet())
                    .equals(new ArrayList<>(actual.getDocument("sort").keySet())));
            // STALE 即使 queryable=true 也已停止同步，不能作为健康索引跳过；未知状态保守拒绝。
            boolean acceptedStatus = "READY".equals(status) || "BUILDING".equals(status) || "PENDING".equals(status);
            if (!"search".equals(current.getString("type")) || actual == null
                    || !normalized(requested).equals(normalized(actual)) || !sameSort || !acceptedStatus
                    || "READY".equals(status) && Boolean.FALSE.equals(current.get("queryable"))) {
                throw new MongoPlusConvertException("Search index " + name
                        + " already exists with a different definition/type or an unusable status (" + status
                        + "); inspect it explicitly");
            }
            if (!"READY".equals(status)) {
                // BUILDING 可能仍查询旧定义；跳过重复提交不代表本次定义已就绪。
                log.warn("Search index {} is {}; creation is already pending and readiness is not confirmed", name, status);
            }
        }
        if (!missing.isEmpty()) {
            // 到此全部已有模型均已检查，避免本批次先创建部分缺失项、随后才发现本地可识别的冲突。
            // Driver 返回只表示接受了异步创建请求，并不表示索引已经 READY。
            collection.createSearchIndexes(missing);
        }
    }

    /**
     * 在副本上消除默认值白名单，兼容服务端将省略选项显式展开的定义。
     * <p>未知属性、数组顺序和 analyzer/searchAnalyzer 保留，不能通过删除继承相关选项消除真实冲突。</p>
     */
    private static BsonDocument normalized(BsonDocument definition) {
        // 转为独立 BSON 树，后续移除选项不能改变 Driver 待提交模型或已有定义的快照。
        BsonDocument copy = BsonDocument.parse(definition.toJson());
        removeDefault(copy, "numPartitions", "1");
        removeDefault(copy, "storedSource", "false");
        if (copy.containsKey("mappings") && copy.get("mappings").isDocument()) {
            normalizeMapping(copy.getDocument("mappings"));
        }
        if (copy.containsKey("typeSets") && copy.get("typeSets").isArray()) {
            for (BsonValue typeSet : copy.getArray("typeSets")) {
                if (typeSet.isDocument() && typeSet.asDocument().containsKey("types")) {
                    normalizeField(typeSet.asDocument().get("types"));
                }
            }
        }
        return copy;
    }

    private static void normalizeMapping(BsonDocument mapping) {
        removeDefault(mapping, "dynamic", "false");
        BsonValue fields = mapping.get("fields");
        if (fields != null && fields.isDocument()) {
            fields.asDocument().values().forEach(SearchIndexCreator::normalizeField);
            // 空 fields 对象可由服务端补齐；字段值中的空数组却表示排除，不能在此一并删除。
            if (fields.asDocument().isEmpty()) {
                mapping.remove("fields");
            }
        }
    }

    /** 按字段类型递归处理默认值，不合并同字段的多类型定义，也不改动映射数组顺序。 */
    private static void normalizeField(BsonValue value) {
        if (value.isArray()) {
            value.asArray().forEach(SearchIndexCreator::normalizeField);
            return;
        }
        if (!value.isDocument()) {
            return;
        }
        BsonDocument field = value.asDocument();
        BsonValue type = field.get("type");
        if (type == null || !type.isString()) {
            // 比较器不承担修复无效结构的职责；保留原值，让严格比较识别差异。
            return;
        }
        switch (type.asString().getValue()) {
            case "document":
            case "embeddedDocuments":
                normalizeMapping(field);
                if ("embeddedDocuments".equals(type.asString().getValue())) {
                    removeDefault(field, "storedSource", "false");
                }
                break;
            case "string":
                removeDefault(field, "indexOptions", "'offsets'");
                removeDefault(field, "store", "true");
                removeDefault(field, "norms", "'include'");
                if (field.containsKey("multi") && field.get("multi").isDocument()) {
                    field.getDocument("multi").values().forEach(SearchIndexCreator::normalizeField);
                }
                break;
            case "autocomplete":
                removeDefault(field, "minGrams", "2");
                removeDefault(field, "maxGrams", "15");
                removeDefault(field, "foldDiacritics", "true");
                removeDefault(field, "tokenization", "'edgeGram'");
                break;
            case "number":
            case "numberFacet":
                removeDefault(field, "representation", "'double'");
                removeDefault(field, "indexIntegers", "true");
                removeDefault(field, "indexDoubles", "true");
                break;
            case "token": removeDefault(field, "normalizer", "'none'"); break;
            case "geo": removeDefault(field, "indexShapes", "false"); break;
            case "vector":
                removeDefault(field, "quantization", "'none'");
                removeDefault(field, "indexingMethod", "'hnsw'");
                break;
            default: break;
        }
    }

    private static void removeDefault(BsonDocument document, String key, String jsonValue) {
        BsonValue expected = BsonDocument.parse("{value:" + jsonValue + "}").get("value");
        BsonValue actual = document.get(key);
        // 整数默认值可能以 int32/int64 返回；只放宽这两种整数表示，不把字符串或浮点数强转后比较。
        boolean sameInteger = expected.isInt32() && actual != null && (actual.isInt32() || actual.isInt64())
                && actual.asNumber().longValue() == expected.asInt32().getValue();
        if (expected.equals(actual) || sameInteger) {
            document.remove(key);
        }
    }
}
