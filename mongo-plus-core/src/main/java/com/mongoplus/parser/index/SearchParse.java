package com.mongoplus.parser.index;

import com.mongodb.client.model.SearchIndexModel;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchSort;
import com.mongoplus.annotation.index.search.MongoSearchStoredSource;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.enums.Analyzer;
import com.mongoplus.enums.SearchSortNoData;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mongoplus.parser.index.SearchDefinitionValidator.*;

/**
 * 将搜索索引注解解析为 Driver 搜索索引模型，不执行数据库操作。
 * <p>单索引自动接收未指定 name 的字段注解；多索引要求字段明确指定归属。
 * 所有分组验证成功后才写入元数据，避免失败时留下部分搜索索引。</p>
 *
 * @author anwen
 */
public class SearchParse extends AbstractParser {
    private static final Log log = LogFactory.getLog(SearchParse.class);
    private static final String DEFAULT_INDEX_NAME = "default";

    @Override
    public Function<TypeInformation, Boolean> activate() {
        // 重复声明会被 Java 包装进容器，单注解读取方法无法识别这种情况。
        return type -> type.getClazz().getAnnotationsByType(MongoSearchIndex.class).length > 0
                || type.getFields().stream().anyMatch(field ->
                field.getField().getAnnotationsByType(MongoSearchField.class).length > 0
                        || field.getField().getAnnotationsByType(MongoSearchSort.class).length > 0);
    }

    @Override
    public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject) {
        String entityName = typeInformation.getClazz().getName();
        List<SearchIndexModel> models;
        try {
            Map<String, SearchConfiguration> configurations = configurations(typeInformation);
            // 先完成全部定义转换和校验，再一次性追加；后续索引失败不能污染调用方元数据。
            models = configurations.values().stream().map(SearchConfiguration::model).collect(Collectors.toList());
        } catch (MongoPlusConvertException exception) {
            // 只记录实体和配置错误，不输出整份索引 JSON；异常继续传给启动调用方。
            log.error("Failed to parse MongoDB Search indexes for " + entityName, exception);
            throw failure("Invalid MongoDB Search indexes for " + entityName + ": "
                    + exception.getMessage(), exception);
        }
        if (!models.isEmpty()) {
            if (indexMetaObject.getSearchIndexModels() == null) {
                indexMetaObject.setSearchIndexModels(new ArrayList<>());
            }
            indexMetaObject.getSearchIndexModels().addAll(models);
            log.debug("Parsed MongoDB Search indexes for {}: {}", entityName, models.size());
        }
    }

    /**
     * 按类级声明建立独立配置，再将字段注解分配到所属索引。
     */
    private static Map<String, SearchConfiguration> configurations(TypeInformation typeInformation) {
        MongoSearchIndex[] indexes = typeInformation.getClazz().getAnnotationsByType(MongoSearchIndex.class);
        Map<String, SearchConfiguration> configurations = new LinkedHashMap<>();
        Arrays.stream(indexes).forEachOrdered(index -> {
            String name = indexName(index.name());
            require(!configurations.containsKey(name), "MongoSearchIndex.name", "duplicate index name: " + name);
            SearchConfiguration configuration = new SearchConfiguration(index, index.name());
            configurations.put(name, configuration);
            // 嵌套注解已有明确的外层归属，不能通过 name 跳转到另一个索引。
            Arrays.stream(index.fields()).forEachOrdered(field -> {
                require(field.name().isEmpty(), name + ".fields.name", "must be empty inside MongoSearchIndex");
                configuration.addField(field, fieldName(field.fieldName(), false));
            });
            Arrays.stream(index.sort()).forEachOrdered(sort -> {
                require(sort.name().isEmpty(), name + ".sort.name", "must be empty inside MongoSearchIndex");
                configuration.addSort(sort, fieldName(sort.fieldName(), true));
            });
        });
        // 顺序流保留重复映射声明顺序；字段预排序的优先级在构造 definition 时统一处理。
        typeInformation.getFields().forEach(field -> {
            Arrays.stream(field.getField().getAnnotationsByType(MongoSearchField.class)).forEachOrdered(mapping -> {
                SearchConfiguration configuration = configuration(configurations, indexes, mapping.name(),
                        field.getField().getName() + ".MongoSearchField.name");
                String name = mapping.fieldName().isEmpty() ? field.getIdOrCamelCaseName() : mapping.fieldName();
                configuration.addField(mapping, fieldName(name, false));
            });
            Arrays.stream(field.getField().getAnnotationsByType(MongoSearchSort.class)).forEachOrdered(sort -> {
                SearchConfiguration configuration = configuration(configurations, indexes, sort.name(),
                        field.getField().getName() + ".MongoSearchSort.name");
                String name = sort.fieldName().isEmpty() ? field.getIdOrCamelCaseName() : sort.fieldName();
                configuration.fieldSorts.add(new SortField(sort, fieldName(name, true)));
            });
        });
        return configurations;
    }

    /**
     * 字段映射与排序共用归属规则，避免两种注解对空名称作出不同解释。
     */
    private static SearchConfiguration configuration(Map<String, SearchConfiguration> configurations,
                                                     MongoSearchIndex[] indexes, String name, String path) {
        require(name.isEmpty() || !name.trim().isEmpty(), path, "must not be blank");
        if (indexes.length == 0) {
            // 兼容只在字段上配置搜索索引的用法，此时没有类级声明可供引用。
            return configurations.computeIfAbsent(indexName(name), key -> new SearchConfiguration(null, name));
        }
        require(indexes.length == 1 || !name.isEmpty(), path,
                "must specify an index name when multiple MongoSearchIndex annotations are present");
        String resolvedName = indexName(name.isEmpty() ? indexes[0].name() : name);
        require(configurations.containsKey(resolvedName), path, "unknown index name: " + resolvedName);
        log.debug("Resolved Search annotation {} to index {}", path, resolvedName);
        return configurations.get(resolvedName);
    }

    private static String indexName(String name) {
        require(name.isEmpty() || !name.trim().isEmpty(), "name", "must not be blank");
        // 服务端将省略名称解释为 default，查找和重名校验也必须按同一个名称处理。
        return name.isEmpty() ? DEFAULT_INDEX_NAME : name;
    }

    private static final class SearchConfiguration {
        private final MongoSearchIndex index;
        private final String name;
        private final Document fields = new Document();
        private final Document sort = new Document();
        private final List<SortField> fieldSorts = new ArrayList<>();

        private SearchConfiguration(MongoSearchIndex index, String name) {
            this.index = index;
            this.name = name;
        }

        private SearchIndexModel model() {
            try {
                Document definition = definition();
                return name.isEmpty() ? new SearchIndexModel(definition) : new SearchIndexModel(name, definition);
            } catch (MongoPlusConvertException exception) {
                throw failure("index " + indexName(name) + ": " + exception.getMessage(), exception);
            }
        }

        @SuppressWarnings("unchecked")
        private void addField(MongoSearchField field, String name) {
            // 空数组在 Search 中表示排除字段，不能与该字段的任何类型映射混合。
            Object definition = field.exclude() ? new ArrayList<>() : SearchFieldParser.parse(field);
            if (!fields.containsKey(name)) {
                fields.put(name, definition);
                return;
            }
            Object previous = fields.get(name);
            require(!(definition instanceof List) && (!(previous instanceof List) || !((List<?>) previous).isEmpty()),
                    "fields." + name, "cannot combine exclusion and indexing or repeat an exclusion");
            if (previous instanceof List) {
                ((List<Object>) previous).add(definition);
            } else {
                // 第二种映射出现时才升级为数组，保留单一映射原有的 BSON 结构。
                fields.put(name, new ArrayList<>(Arrays.asList(previous, definition)));
            }
        }

        private void addSort(MongoSearchSort annotation, String name) {
            require(!sort.containsKey(name), "sort." + name, "duplicate sort path");
            Object value = annotation.noData() == SearchSortNoData.DEFAULT ? annotation.order().getValue()
                    : new Document("order", annotation.order().getValue()).append("noData", annotation.noData().getValue());
            sort.put(name, value);
        }

        private Document definition() {
            boolean dynamic = index != null && index.dynamic();
            String typeSet = index == null ? "" : index.dynamicTypeSet();
            Document mappings = new Document("dynamic", dynamicValue(dynamic, typeSet, "mappings.dynamic"));
            if (!fields.isEmpty()) {
                mappings.put("fields", fields);
            }
            Document definition = new Document("mappings", mappings);
            definition.put("numPartitions", index == null ? 1 : index.numPartitions());
            definition.put("storedSource", index == null ? false : storedSource(index.storedSource()));
            if (index != null) {
                putAnalyzer(definition, "analyzer", index.analyzer(), index.customAnalyzer(), false);
                putAnalyzer(definition, "searchAnalyzer", index.searchAnalyzer(), index.customSearchAnalyzer(), false);
                putObjects(definition, "analyzers", index.analyzers());
                putObjects(definition, "typeSets", index.typeSets());
                List<Document> synonyms = Arrays.stream(index.synonyms()).map(synonym -> {
                    Document mapping = new Document("name", required(synonym.name(), "synonyms.name"))
                            .append("source", new Document("collection",
                                    required(synonym.sourceCollection(), "synonyms.source.collection")));
                    putAnalyzer(mapping, "analyzer", synonym.analyzer(), synonym.customAnalyzer(), true);
                    return mapping;
                }).collect(Collectors.toList());
                if (!synonyms.isEmpty()) {
                    definition.put("synonyms", synonyms);
                }
            }
            // 数组配置已按声明顺序写入，字段配置再按优先级追加，不依赖反射枚举顺序。
            fieldSorts.stream().sorted(Comparator.comparingInt((SortField field) -> field.annotation.priority())
                    .thenComparing(field -> field.name)).forEachOrdered(field -> addSort(field.annotation, field.name));
            if (!sort.isEmpty()) {
                definition.put("sort", sort);
            }
            // 每个索引独立校验引用，不能使用其他索引中的分析器、类型集或字段映射。
            new SearchDefinitionValidator(definition).validate();
            return definition;
        }

        private static Object storedSource(MongoSearchStoredSource source) {
            // 布尔全量存储与 include/exclude 对象形式互斥，不能默默覆盖其中一种配置。
            require(source.include().length == 0 || source.exclude().length == 0,
                    "storedSource", "include and exclude are mutually exclusive");
            require(!source.value() || source.include().length + source.exclude().length == 0,
                    "storedSource", "true cannot be combined with include or exclude");
            if (source.include().length > 0) {
                return new Document("include", Arrays.asList(source.include()));
            }
            if (source.exclude().length > 0) {
                return new Document("exclude", Arrays.asList(source.exclude()));
            }
            return source.value();
        }

        private static void putObjects(Document target, String key, String[] values) {
            if (values.length > 0) {
                target.put(key, Arrays.stream(values).map(value -> object(value, key)).collect(Collectors.toList()));
            }
        }
    }

    static Object dynamicValue(boolean dynamic, String typeSet, String path) {
        if (typeSet.isEmpty()) {
            return dynamic;
        }
        require(!dynamic, path, "boolean true and typeSet are mutually exclusive");
        return new Document("typeSet", required(typeSet, path + ".typeSet"));
    }

    static void putAnalyzer(Document target, String key, Analyzer analyzer, String custom, boolean mandatory) {
        if (!custom.isEmpty()) {
            require(analyzer == Analyzer.DEFAULT, key, "custom and built-in analyzer are mutually exclusive");
            target.put(key, required(custom, key));
        } else if (analyzer != Analyzer.DEFAULT || mandatory) {
            target.put(key, (analyzer == Analyzer.DEFAULT ? Analyzer.STANDARD : analyzer).getAnalyzer());
        }
    }

    private static final class SortField {
        private final MongoSearchSort annotation;
        private final String name;

        private SortField(MongoSearchSort annotation, String name) {
            this.annotation = annotation;
            this.name = name;
        }
    }
}
