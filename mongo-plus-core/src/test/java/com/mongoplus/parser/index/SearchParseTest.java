package com.mongoplus.parser.index;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.SearchIndexModel;
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.annotation.index.MongoIndex;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchSort;
import com.mongoplus.annotation.index.search.MongoSearchStoredSource;
import com.mongoplus.annotation.index.search.MongoSearchSynonym;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.enums.*;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import org.bson.BsonDocument;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SearchParseTest {

    @Test
    public void unnamedFieldAndSortBelongToTheSingleNamedIndex() {
        List<SearchIndexModel> models = new Resolver().resolve(SingleNamedIndex.class).getSearchIndexModels();
        assertEquals(1, models.size());
        assertEquals("catalog", models.get(0).getName());
        BsonDocument definition = encode(models.get(0));
        assertEquals("number", definition.getDocument("mappings").getDocument("fields")
                .getDocument("sale_price").getString("type").getValue());
        assertEquals(1, definition.getDocument("sort").getInt32("sale_price").getValue());
    }

    @MongoSearchIndex(name = "catalog")
    public static class SingleNamedIndex {
        @CollectionField("sale_price")
        @MongoSearchField(type = SearchFieldType.NUMBER)
        @MongoSearchSort
        private int price;
    }

    @Test
    public void activationAndEmptyEntityAreSafe() {
        SearchParse parser = new SearchParse();
        assertFalse(parser.activate().apply(TypeInformation.of(Empty.class)));
        assertTrue(parser.activate().apply(TypeInformation.of(Dynamic.class)));
        assertTrue(parser.activate().apply(TypeInformation.of(FieldOnly.class)));
        assertTrue(new Resolver().resolve(Empty.class).getSearchIndexModels().isEmpty());
    }

    @Test
    public void dynamicDefinitionKeepsNameOutsideDefinition() {
        SearchIndexModel model = model(Dynamic.class);
        assertEquals("catalog", model.getName());
        assertEquals(BsonDocument.parse("{mappings:{dynamic:true},numPartitions:2}"),
                encode(model));
    }

    @Test
    public void repeatableFieldsUseMappedNamesAndSeparateIndexes() {
        List<SearchIndexModel> models = new Resolver().resolve(FieldOnly.class).getSearchIndexModels();
        assertEquals(2, models.size());
        assertEquals("catalog", models.get(0).getName());
        BsonDocument fields = encode(models.get(0)).getDocument("mappings").getDocument("fields");
        assertEquals("string", fields.getArray("display_name").get(0).asDocument().getString("type").getValue());
        assertEquals("token", fields.getArray("display_name").get(1).asDocument().getString("type").getValue());
        assertEquals("ids", models.get(1).getName());
        assertTrue(encode(models.get(1)).getDocument("mappings").getDocument("fields").containsKey("_id"));
    }

    @Test
    public void sortCombinesArrayOrderAndFieldPriority() {
        BsonDocument sort = encode(model(Sorted.class)).getDocument("sort");
        assertEquals(Arrays.asList("score", "zeta", "alpha", "beta"), new ArrayList<>(sort.keySet()));
        assertEquals(BsonDocument.parse("{order:-1,noData:'lowest'}"), sort.getDocument("zeta"));
    }

    @Test
    public void invalidPartitionFailsWithoutChangingMetadata() {
        IndexMetaObject meta = new IndexMetaObject();
        List<SearchIndexModel> existing = new ArrayList<>();
        existing.add(new SearchIndexModel(new org.bson.Document("mappings",
                new org.bson.Document("dynamic", true))));
        meta.setSearchIndexModels(existing);
        assertThrows(MongoPlusConvertException.class,
                () -> new SearchParse().parse(TypeInformation.of(BadPartition.class), meta));
        assertEquals(1, existing.size());
    }

    @Test
    public void parsesNestedMappingsTypeSetsAndAlternativeAnalyzers() {
        BsonDocument definition = encode(model(Complex.class));
        assertEquals("lucene.english", definition.getString("analyzer").getValue());
        assertEquals("customText", definition.getString("searchAnalyzer").getValue());
        BsonDocument mappings = definition.getDocument("mappings");
        assertEquals(BsonDocument.parse("{typeSet:'strings'}"), mappings.getDocument("dynamic"));
        BsonDocument fields = mappings.getDocument("fields");
        assertTrue(fields.getArray("internal").isEmpty());
        BsonDocument title = fields.getDocument("title");
        assertEquals("customText", title.getString("analyzer").getValue());
        assertEquals("lucene.french", title.getDocument("multi").getDocument("french")
                .getString("analyzer").getValue());
        assertEquals(1, fields.getArray("items").get(1).asDocument()
                .getDocument("storedSource").getArray("include").size());
        assertEquals(-1, definition.getDocument("sort").getInt32("items.price").getValue());
        assertEquals("customText", definition.getArray("synonyms").get(0).asDocument().getString("analyzer").getValue());
        assertFalse(definition.toJson().contains("lucene.default"));
    }

    @Test
    public void supportsAllTypedFieldFamiliesWithoutLeakingUnrelatedDefaults() {
        BsonDocument fields = encode(model(FieldFamilies.class)).getDocument("mappings").getDocument("fields");
        assertEquals(BsonDocument.parse("{type:'boolean'}"), fields.getDocument("active"));
        assertEquals(BsonDocument.parse("{type:'date'}"), fields.getDocument("date"));
        assertEquals(BsonDocument.parse("{type:'objectId'}"), fields.getDocument("id"));
        assertEquals(BsonDocument.parse("{type:'uuid'}"), fields.getDocument("uuid"));
        assertEquals(BsonDocument.parse("{type:'token',normalizer:'lowercase'}"), fields.getDocument("token"));
        assertEquals(BsonDocument.parse("{type:'number',representation:'int64',indexIntegers:true,indexDoubles:false}"),
                fields.getDocument("number"));
        assertEquals(BsonDocument.parse("{type:'geo',indexShapes:true}"), fields.getDocument("geo"));
        BsonDocument autocomplete = fields.getDocument("autocomplete");
        assertEquals("rightEdgeGram", autocomplete.getString("tokenization").getValue());
        assertFalse(autocomplete.containsKey("store"));
        assertFalse(autocomplete.containsKey("searchAnalyzer"));
        BsonDocument text = fields.getDocument("text");
        assertEquals("stableTfl", text.getDocument("similarity").getString("type").getValue());
        assertFalse(text.containsKey("ignoreAbove"));
        assertFalse(text.containsKey("numDimensions"));
        assertFalse(text.containsKey("minGrams"));
    }

    @Test
    public void parsesCurrentAndLegacyVectorFormats() {
        BsonDocument definition = encode(model(Vectors.class));
        BsonDocument fields = definition.getDocument("mappings").getDocument("fields");
        BsonDocument current = fields.getDocument("embedding");
        assertEquals(16, current.getInt32("numDimensions").getValue());
        assertEquals("dotProduct", current.getString("similarity").getValue());
        assertEquals(32, current.getDocument("hnswOptions").getInt32("maxEdges").getValue());
        assertFalse(current.containsKey("dimensions"));
        BsonDocument legacy = fields.getDocument("oldEmbedding");
        assertEquals(BsonDocument.parse("{type:'knnVector',dimensions:8,similarity:'cosine'}"), legacy);
        assertEquals(BsonDocument.parse("{exclude:['embedding']}"), definition.getDocument("storedSource"));
    }

    @Test
    public void storedSourceAndSynonymDefaultsProduceRequiredShapes() {
        BsonDocument definition = encode(model(SourceAndSynonyms.class));
        assertEquals(BsonDocument.parse("{include:['title','awards.wins']}"), definition.getDocument("storedSource"));
        assertEquals(BsonDocument.parse("{name:'terms',source:{collection:'dictionary'},analyzer:'lucene.standard'}"),
                definition.getArray("synonyms").get(0).asDocument());
        assertTrue(encode(model(FullSource.class)).getBoolean("storedSource").getValue());
    }

    @Test
    public void inheritsFieldAnnotationsAndHonorsCamelCaseMapping() {
        boolean previous = com.mongoplus.cache.global.PropertyCache.camelToUnderline;
        try {
            com.mongoplus.cache.global.PropertyCache.camelToUnderline = true;
            BsonDocument fields = encode(model(Inherited.class)).getDocument("mappings").getDocument("fields");
            assertTrue(fields.containsKey("created_at"));
            assertTrue(fields.containsKey("explicit_name"));
        } finally {
            com.mongoplus.cache.global.PropertyCache.camelToUnderline = previous;
        }
    }

    @Test
    public void ordinaryAndSearchIndexesCoexistInResolver() {
        IndexMetaObject meta = new Resolver().resolve(MixedIndexes.class);
        assertEquals(1, meta.getIndexModels().size());
        assertEquals(1, meta.getSearchIndexModels().size());
        assertFalse(encode(meta.getSearchIndexModels().get(0)).containsKey("name"));
    }

    @Test
    public void fieldSortsAreRepeatableAcrossIndexesAndDoNotDependOnReflectionOrder() {
        List<SearchIndexModel> models = new Resolver().resolve(MultipleSorts.class).getSearchIndexModels();
        assertEquals(2, models.size());
        assertEquals(1, encode(models.get(0)).getDocument("sort").getInt32("score").getValue());
        assertEquals(-1, encode(models.get(1)).getDocument("sort").getInt32("score").getValue());
    }

    @Test
    public void secondInvalidIndexCannotAppendTheFirstOne() {
        IndexMetaObject meta = new IndexMetaObject();
        assertThrows(MongoPlusConvertException.class,
                () -> new SearchParse().parse(TypeInformation.of(PartialFailure.class), meta));
        assertNull(meta.getSearchIndexModels());
    }

    @Test
    public void rejectsInvalidAnnotationsAndNestedJsonWithUsefulPaths() {
        Class<?>[] invalid = {
                NoMappings.class, ExclusionConflict.class, MissingFieldName.class, InvalidFieldPath.class,
                DuplicateSort.class, DynamicSort.class, EmbeddedOnlySort.class, UnknownTypeSet.class,
                DuplicateTypeSet.class, InvalidTypeSetType.class, BadJson.class, WrongFieldOption.class,
                BadNumbers.class, BadGrams.class, MissingVectorSimilarity.class, BadBinaryDimensions.class,
                BadHnsw.class, ConflictingSource.class, VectorSource.class, UnknownAnalyzer.class,
                ConflictingAnalyzer.class, DuplicateAnalyzer.class, DuplicateSynonym.class,
                UnsupportedSynonymAnalyzer.class, UnsupportedCustomFilter.class, InheritedAutocompleteAnalyzer.class,
                InvalidNestedNumber.class, NestedVectorSource.class, InvalidSourcePath.class
        };
        for (Class<?> entity : invalid) {
            MongoPlusConvertException exception = assertThrows(entity.getSimpleName(),
                    MongoPlusConvertException.class, () -> model(entity));
            assertTrue(entity.getSimpleName(), exception.getMessage().contains(":"));
        }
    }

    private static SearchIndexModel model(Class<?> entity) {
        return new Resolver().resolve(entity).getSearchIndexModels().get(0);
    }

    private static BsonDocument encode(SearchIndexModel model) {
        return model.getDefinition().toBsonDocument(org.bson.Document.class,
                MongoClientSettings.getDefaultCodecRegistry());
    }

    public static class Empty {
    }

    @MongoSearchIndex(name = "catalog", dynamic = true, numPartitions = 2)
    public static class Dynamic {
    }

    public static class FieldOnly {
        @CollectionField("display_name")
        @MongoSearchField(name = "catalog")
        @MongoSearchField(name = "catalog", type = SearchFieldType.TOKEN)
        private String title;

        @ID
        @MongoSearchField(name = "ids", type = SearchFieldType.OBJECT_ID)
        private String id;
    }

    @MongoSearchIndex(fields = {
            @MongoSearchField(fieldName = "score", type = SearchFieldType.NUMBER),
            @MongoSearchField(fieldName = "zeta", type = SearchFieldType.NUMBER),
            @MongoSearchField(fieldName = "alpha", type = SearchFieldType.NUMBER),
            @MongoSearchField(fieldName = "beta", type = SearchFieldType.NUMBER)
    }, sort = @MongoSearchSort(fieldName = "score"))
    public static class Sorted {
        @MongoSearchSort(priority = -1, order = IndexDirection.DESC, noData = SearchSortNoData.LOWEST)
        private int zeta;
        @MongoSearchSort
        private int beta;
        @MongoSearchSort
        private int alpha;
    }

    @MongoSearchIndex(dynamic = true, numPartitions = 3)
    public static class BadPartition {
    }

    @MongoSearchIndex(analyzer = Analyzer.ENGLISH, customSearchAnalyzer = "customText",
            analyzers = "{\"name\":\"customText\",\"tokenizer\":{\"type\":\"standard\"}}",
            typeSets = "{\"name\":\"strings\",\"types\":[{\"type\":\"string\"}]}",
            dynamicTypeSet = "strings",
            fields = {
                    @MongoSearchField(fieldName = "title", customAnalyzer = "customText",
                            multi = "{\"french\":{\"type\":\"string\",\"analyzer\":\"lucene.french\"}}"),
                    @MongoSearchField(fieldName = "internal", exclude = true),
                    @MongoSearchField(fieldName = "items", type = SearchFieldType.DOCUMENT,
                            fields = "{\"price\":{\"type\":\"number\"}}"),
                    @MongoSearchField(fieldName = "items", type = SearchFieldType.EMBEDDED_DOCUMENTS,
                            fields = "{\"price\":{\"type\":\"number\"}}",
                            storedSource = "{\"include\":[\"price\"]}")
            },
            sort = @MongoSearchSort(fieldName = "items.price", order = IndexDirection.DESC),
            synonyms = @MongoSearchSynonym(name = "words", sourceCollection = "dictionary", customAnalyzer = "customText"))
    public static class Complex {
    }

    @MongoSearchIndex(fields = {
            @MongoSearchField(fieldName = "active", type = SearchFieldType.BOOLEAN),
            @MongoSearchField(fieldName = "date", type = SearchFieldType.DATE),
            @MongoSearchField(fieldName = "id", type = SearchFieldType.OBJECT_ID),
            @MongoSearchField(fieldName = "uuid", type = SearchFieldType.UUID),
            @MongoSearchField(fieldName = "token", type = SearchFieldType.TOKEN, normalizer = SearchNormalizer.LOWERCASE),
            @MongoSearchField(fieldName = "number", type = SearchFieldType.NUMBER,
                    representation = SearchRepresentation.INT64, indexDoubles = false),
            @MongoSearchField(fieldName = "geo", type = SearchFieldType.GEO, indexShapes = true),
            @MongoSearchField(fieldName = "autocomplete", type = SearchFieldType.AUTOCOMPLETE,
                    tokenization = SearchTokenization.RIGHT_EDGE_GRAM, minGrams = 3, maxGrams = 10),
            @MongoSearchField(fieldName = "text", similarity = SearchSimilarity.STABLE_TFL, store = false,
                    indexOptions = SearchIndexOptions.POSITIONS)
    })
    public static class FieldFamilies {
    }

    @SuppressWarnings("deprecation")
    @MongoSearchIndex(fields = {
            @MongoSearchField(fieldName = "embedding", type = SearchFieldType.VECTOR, numDimensions = 16,
                    vectorSimilarity = SearchVectorSimilarity.DOT_PRODUCT, quantization = SearchQuantization.BINARY,
                    hnswOptions = "{\"maxEdges\":32,\"numEdgeCandidates\":200}"),
            @MongoSearchField(fieldName = "oldEmbedding", type = SearchFieldType.KNN_VECTOR, dimensions = 8,
                    vectorSimilarity = SearchVectorSimilarity.COSINE)
    }, storedSource = @MongoSearchStoredSource(exclude = "embedding"))
    public static class Vectors {
    }

    @MongoSearchIndex(dynamic = true, storedSource = @MongoSearchStoredSource(include = {"title", "awards.wins"}),
            synonyms = @MongoSearchSynonym(name = "terms", sourceCollection = "dictionary"))
    public static class SourceAndSynonyms {
    }

    @MongoSearchIndex(dynamic = true, storedSource = @MongoSearchStoredSource(true))
    public static class FullSource {
    }

    public static class BaseFields {
        @MongoSearchField
        private java.util.Date createdAt;
    }

    public static class Inherited extends BaseFields {
        @CollectionField("explicit_name")
        @MongoSearchField
        private String title;
    }

    @MongoSearchIndex(dynamic = true)
    public static class MixedIndexes {
        @MongoIndex
        private int score;
    }

    public static class MultipleSorts {
        @MongoSearchField(name = "ascending", type = SearchFieldType.NUMBER)
        @MongoSearchField(name = "descending", type = SearchFieldType.NUMBER)
        @MongoSearchSort(name = "ascending")
        @MongoSearchSort(name = "descending", order = IndexDirection.DESC)
        private int score;
    }

    public static class PartialFailure {
        @MongoSearchField(name = "valid")
        @MongoSearchField(name = "invalid", type = SearchFieldType.VECTOR)
        private String value;
    }

    @MongoSearchIndex
    public static class NoMappings {}
    @MongoSearchIndex(fields = {@MongoSearchField(fieldName = "title"), @MongoSearchField(fieldName = "title", exclude = true)})
    public static class ExclusionConflict {}
    @MongoSearchIndex(fields = @MongoSearchField)
    public static class MissingFieldName {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "parent.child"))
    public static class InvalidFieldPath {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "score", type = SearchFieldType.NUMBER),
            sort = {@MongoSearchSort(fieldName = "score"), @MongoSearchSort(fieldName = "score")})
    public static class DuplicateSort {}
    @MongoSearchIndex(dynamic = true, sort = @MongoSearchSort(fieldName = "score"))
    public static class DynamicSort {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "items", type = SearchFieldType.EMBEDDED_DOCUMENTS,
            fields = "{\"price\":{\"type\":\"number\"}}"), sort = @MongoSearchSort(fieldName = "items.price"))
    public static class EmbeddedOnlySort {}
    @MongoSearchIndex(dynamicTypeSet = "missing")
    public static class UnknownTypeSet {}
    @MongoSearchIndex(dynamic = true, typeSets = "{\"name\":\"types\",\"types\":[{\"type\":\"number\"},{\"type\":\"number\"}]}")
    public static class DuplicateTypeSet {}
    @MongoSearchIndex(dynamic = true, typeSets = "{\"name\":\"types\",\"types\":[{\"type\":\"document\",\"dynamic\":true}]}")
    public static class InvalidTypeSetType {}
    @MongoSearchIndex(dynamic = true, analyzers = "{bad")
    public static class BadJson {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "score", type = SearchFieldType.NUMBER, analyzer = Analyzer.ENGLISH))
    public static class WrongFieldOption {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "score", type = SearchFieldType.NUMBER,
            indexIntegers = false, indexDoubles = false))
    public static class BadNumbers {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "title", type = SearchFieldType.AUTOCOMPLETE,
            minGrams = 10, maxGrams = 3))
    public static class BadGrams {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "vector", type = SearchFieldType.VECTOR, numDimensions = 8))
    public static class MissingVectorSimilarity {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "vector", type = SearchFieldType.VECTOR, numDimensions = 7,
            vectorSimilarity = SearchVectorSimilarity.COSINE, quantization = SearchQuantization.BINARY))
    public static class BadBinaryDimensions {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "vector", type = SearchFieldType.VECTOR, numDimensions = 8,
            vectorSimilarity = SearchVectorSimilarity.COSINE, indexingMethod = SearchIndexingMethod.FLAT, hnswOptions = "{}"))
    public static class BadHnsw {}
    @MongoSearchIndex(dynamic = true, storedSource = @MongoSearchStoredSource(include = "title", exclude = "price"))
    public static class ConflictingSource {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "vector", type = SearchFieldType.VECTOR, numDimensions = 8,
            vectorSimilarity = SearchVectorSimilarity.COSINE), storedSource = @MongoSearchStoredSource(true))
    public static class VectorSource {}
    @MongoSearchIndex(dynamic = true, customAnalyzer = "missing")
    public static class UnknownAnalyzer {}
    @MongoSearchIndex(dynamic = true, analyzer = Analyzer.ENGLISH, customAnalyzer = "custom")
    public static class ConflictingAnalyzer {}
    @MongoSearchIndex(dynamic = true, analyzers = {
            "{\"name\":\"same\",\"tokenizer\":{\"type\":\"standard\"}}",
            "{\"name\":\"same\",\"tokenizer\":{\"type\":\"standard\"}}"})
    public static class DuplicateAnalyzer {}
    @MongoSearchIndex(dynamic = true, synonyms = {
            @MongoSearchSynonym(name = "same", sourceCollection = "source"),
            @MongoSearchSynonym(name = "same", sourceCollection = "source")})
    public static class DuplicateSynonym {}
    @MongoSearchIndex(dynamic = true, synonyms = @MongoSearchSynonym(name = "cjk", sourceCollection = "source", analyzer = Analyzer.CJK))
    public static class UnsupportedSynonymAnalyzer {}
    @MongoSearchIndex(dynamic = true,
            analyzers = "{\"name\":\"custom\",\"tokenizer\":{\"type\":\"standard\"},\"tokenFilters\":[{\"type\":\"wordDelimiterGraph\"}]}",
            synonyms = @MongoSearchSynonym(name = "words", sourceCollection = "source", customAnalyzer = "custom"))
    public static class UnsupportedCustomFilter {}
    @MongoSearchIndex(analyzer = Analyzer.KUROMOJI,
            fields = @MongoSearchField(fieldName = "title", type = SearchFieldType.AUTOCOMPLETE))
    public static class InheritedAutocompleteAnalyzer {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "nested", type = SearchFieldType.DOCUMENT,
            fields = "{\"price\":{\"type\":\"number\",\"indexIntegers\":false,\"indexDoubles\":false}}"))
    public static class InvalidNestedNumber {}
    @MongoSearchIndex(fields = @MongoSearchField(fieldName = "nested", type = SearchFieldType.EMBEDDED_DOCUMENTS,
            fields = "{\"vector\":{\"type\":\"vector\",\"numDimensions\":8,\"similarity\":\"cosine\"}}", storedSource = "true"))
    public static class NestedVectorSource {}
    @MongoSearchIndex(dynamic = true, storedSource = @MongoSearchStoredSource(include = " "))
    public static class InvalidSourcePath {}
}
