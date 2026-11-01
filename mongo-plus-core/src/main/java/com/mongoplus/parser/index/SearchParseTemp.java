package com.mongoplus.parser.index;

import com.mongodb.client.model.SearchIndexModel;
import com.mongodb.client.model.SearchIndexType;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusIndexException;
import com.mongoplus.enums.*;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.function.Function;

/**
 * 将搜索索引注解解析为 Driver 搜索索引模型，不执行数据库操作。
 * <p>单索引自动接收未指定 name 的字段注解；多索引要求字段明确指定归属。
 * 所有分组验证成功后才写入元数据，避免失败时留下部分搜索索引。</p>
 *
 * @author anwen
 */
public class SearchParseTemp extends AbstractParser {
    private static final Log log = LogFactory.getLog(SearchParseTemp.class);
    private static final String DEFAULT_INDEX_NAME = "default";

    @Override
    public Function<TypeInformation, Boolean> activate() {
        return typeInformation -> typeInformation.getAnnotationsByType(MongoSearchIndex.class).length > 0;
    }

    @Override
    public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject) {
        MongoSearchIndex[] mongoSearchIndexes = typeInformation.getClazz().getAnnotationsByType(MongoSearchIndex.class);
        if (ArrayUtils.isEmpty(mongoSearchIndexes)) {
            log.error("No available search index configuration found for type {}", typeInformation.getClazz().getName());
            return;
        }
        Map<String, SearchFieldMapping> searchFieldMap = getSearchFieldMap(typeInformation, mongoSearchIndexes);
        Arrays.stream(mongoSearchIndexes).forEachOrdered(searchIndex -> {
            SearchFieldMapping searchFieldMapping = searchFieldMap.get(searchIndex.name());
            Document definition = new Document();
            definition.put("analyzer", searchIndex.analyzer());
            definition.put("mappings", mappings(searchIndex, searchFieldMapping));
            if (searchIndex.searchAnalyzer() != null && searchIndex.searchAnalyzer() != Analyzer.DEFAULT) {
                definition.put("searchAnalyzer", searchIndex.searchAnalyzer());
            }
            definition.put("numPartitions", searchIndex.numPartitions());
            // TODO 解析storedSource
            definition.put("storedSource", searchIndex.storedSource());
            // TODO 解析同义词映射
            definition.put("synonyms", searchIndex.synonyms());

            SearchIndexModel searchIndexModel = new SearchIndexModel(searchIndex.name(), definition, SearchIndexType.search());
        });
    }

    Map<String, SearchFieldMapping> getSearchFieldMap(TypeInformation typeInformation, MongoSearchIndex[] mongoSearchIndexes) {
        List<FieldInformation> fields = typeInformation.getFields();
        Map<String, SearchFieldMapping> searchFieldMap = new HashMap<>();
        int length = mongoSearchIndexes.length;
        fields.forEach(field -> {
            MongoSearchField[] searchFields = field.getField().getAnnotationsByType(MongoSearchField.class);
            if (ArrayUtils.isNotEmpty(searchFields)) {
                // 如果有多个索引配置，则字段注解名称不能为空
                if (length > 1) {
                    long emptyCount = Arrays.stream(searchFields)
                            .map(MongoSearchField::name)
                            .filter(StringUtils::isBlank)
                            .count();
                    if (emptyCount > 0) {
                        throw new MongoPlusIndexException("When multiple @MongoSearchIndex annotations are present, the name parameter must be specified for the @MongoSearchField on the fields.");
                    }
                }
                searchFieldMap.computeIfAbsent(searchFields[0].name(),
                        k -> new SearchFieldMapping(field, new ArrayList<>()))
                        .addAll(new LinkedList<>(Arrays.asList(searchFields)));
            }
        });
        return searchFieldMap;
    }

    Bson mappings(MongoSearchIndex searchIndex, SearchFieldMapping searchFieldMapping) {
        Document mappings = new Document();

        FieldInformation fieldInformation = searchFieldMapping.getFieldInformation();
        List<MongoSearchField> mongoSearchFields = searchFieldMapping.getMongoSearchFields();

        String fieldName = fieldInformation.getCamelCaseName();
        if (StringUtils.isNotBlank(searchIndex.dynamicTypeSet())) {
            mappings.put("dynamic", searchIndex.dynamicTypeSet());
        }
        mappings.put("dynamic", searchIndex.dynamic());
        if (!searchIndex.dynamic()) {
            mappings.put("fields", new Document() {{
                put(fieldName, new ArrayList<Document>() {{
                    mongoSearchFields.stream().forEachOrdered(searchField -> {
                        add(new MongoPlusDocument() {{
                            SearchFieldType fieldType = searchField.type();
                            put("type", fieldType.name());
                            if (fieldType == SearchFieldType.AUTOCOMPLETE) {
                                if (searchField.analyzer() != null && searchField.analyzer() != Analyzer.DEFAULT) {
                                    put("analyzer", searchField.analyzer().getAnalyzer());
                                }
                                putIsNotNull("maxGrams", searchField.maxGrams());
                                putIsNotNull("minGrams", searchField.minGrams());
                                SearchTokenization tokenization = searchField.tokenization();
                                if (tokenization != null && tokenization != SearchTokenization.NONE) {
                                    put("tokenization", tokenization.getValue());
                                }
                                putIsNotNull("foldDiacritics", searchField.foldDiacritics());
                                SearchSimilarity similarity = searchField.similarity();
                                if (similarity != null && similarity != SearchSimilarity.NONE) {
                                    put("similarity", new Document("type", similarity.getValue()));
                                }
                            }
                            if (fieldType == SearchFieldType.DOCUMENT || fieldType == SearchFieldType.EMBEDDED_DOCUMENTS) {
                                put("dynamic", searchField.dynamic());
                                if (StringUtils.isNotBlank(searchField.dynamicTypeSet())) {
                                    put("dynamic", searchField.dynamicTypeSet());
                                }
                                if (StringUtils.isNotBlank(searchField.fields())) {
                                    put("fields", Document.parse(searchField.fields(), MapCodecCache.getDefaultCodec()));
                                }
                                if (StringUtils.isNotBlank(searchField.storedSource())) {
                                    put("storedSource", searchField.storedSource());
                                }
                            }
                            if (fieldType == SearchFieldType.GEO) {
                                put("indexShapes", searchField.indexShapes());
                            }
                            if (fieldType == SearchFieldType.KNN_VECTOR) {
                                put("dimensions", searchField.dimensions());
                                SearchVectorSimilarity vectorSimilarity = searchField.vectorSimilarity();
                                if (vectorSimilarity == null || vectorSimilarity == SearchVectorSimilarity.DEFAULT) {
                                    throw new MongoPlusIndexException("KNN_VECTOR type must specify vector similarity.");
                                }
                                put("similarity", vectorSimilarity.getValue());
                            }
                            if (fieldType == SearchFieldType.NUMBER || fieldType == SearchFieldType.NUMBER_FACET) {
                                put("representation", searchField.representation().getValue());
                                put("indexIntegers", searchField.indexIntegers());
                                put("indexDoubles", searchField.indexDoubles());
                            }
                            if (fieldType == SearchFieldType.STRING) {
                                if (searchField.analyzer() != null && searchField.analyzer() != Analyzer.DEFAULT) {
                                    put("analyzer", searchField.analyzer().getAnalyzer());
                                }
                                if (searchField.searchAnalyzer() != null && searchField.searchAnalyzer() != Analyzer.DEFAULT) {
                                    put("searchAnalyzer", searchField.searchAnalyzer().getAnalyzer());
                                }
                                put("indexOptions", searchField.indexOptions());
                                put("store", searchField.store());
                                if (searchField.ignoreAbove() > 0) {
                                    put("ignoreAbove", searchField.ignoreAbove());
                                }
                                SearchSimilarity similarity = searchField.similarity();
                                if (similarity != null && similarity != SearchSimilarity.NONE) {
                                    put("similarity", new Document("type", similarity.getValue()));
                                }
                                if (StringUtils.isNotBlank(searchField.multi())) {
                                    put("multi", searchField.multi());
                                }
                                put("norms", searchField.norms());
                            }
                            if (fieldType == SearchFieldType.TOKEN) {
                                put("normalizer", searchField.normalizer());
                            }
                            if (fieldType == SearchFieldType.VECTOR) {
                                put("numDimensions", searchField.numDimensions());
                                put("similarity", searchField.vectorSimilarity());
                                SearchQuantization quantization = searchField.quantization();
                                if (quantization != null && quantization != SearchQuantization.EMPTY) {
                                    put("quantization", quantization.getValue());
                                }
                                SearchIndexingMethod indexingMethod = searchField.indexingMethod();
                                if (indexingMethod != null && indexingMethod != SearchIndexingMethod.NONE) {
                                    put("indexingMethod", indexingMethod.getValue());
                                }
                                if (StringUtils.isNotBlank(searchField.hnswOptions())) {
                                    put("hnswOptions", searchField.hnswOptions());
                                }
                            }
                        }});
                    });
                }});
            }});
        }
        return mappings;
    }

    private static class SearchFieldMapping {

        private final FieldInformation fieldInformation;

        private final List<MongoSearchField> mongoSearchFields;

        public FieldInformation getFieldInformation() {
            return fieldInformation;
        }

        public List<MongoSearchField> getMongoSearchFields() {
            return mongoSearchFields;
        }

        public void addAll(List<MongoSearchField> mongoSearchFields) {
            this.mongoSearchFields.addAll(mongoSearchFields);
        }

        public SearchFieldMapping(FieldInformation fieldInformation, List<MongoSearchField> mongoSearchFields) {
            this.fieldInformation = fieldInformation;
            this.mongoSearchFields = mongoSearchFields;
        }
    }

}
