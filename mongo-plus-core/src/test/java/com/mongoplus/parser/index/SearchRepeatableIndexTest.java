package com.mongoplus.parser.index;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.SearchIndexModel;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchIndexes;
import com.mongoplus.annotation.index.search.MongoSearchSort;
import com.mongoplus.annotation.index.search.MongoSearchStoredSource;
import com.mongoplus.annotation.index.search.MongoSearchSynonym;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.enums.IndexDirection;
import com.mongoplus.enums.SearchFieldType;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 多索引声明、字段归属及失败时元数据完整性的本地回归，不访问 MongoDB。
 */
public class SearchRepeatableIndexTest {

    @Test
    public void repeatedAndExplicitContainerDeclarationsActivateWithoutFields() {
        Arrays.asList(RepeatedDynamic.class, ContainerDynamic.class).forEach(entity -> {
            assertTrue(new SearchParse().activate().apply(TypeInformation.of(entity)));
            List<SearchIndexModel> models = resolve(entity);
            assertEquals(2, models.size());
            assertEquals("first", models.get(0).getName());
            assertEquals("second", models.get(1).getName());
        });
    }

    @Test
    public void fieldsSortsAndNestedOptionsStayWithinTheirOwnIndex() {
        List<SearchIndexModel> models = resolve(Isolated.class);
        assertEquals(2, models.size());
        BsonDocument first = encode(models.get(0));
        BsonDocument second = encode(models.get(1));
        assertEquals("first", models.get(0).getName());
        assertEquals("second", models.get(1).getName());
        assertEquals(1, first.getInt32("numPartitions").getValue());
        assertEquals(2, second.getInt32("numPartitions").getValue());
        assertTrue(first.getBoolean("storedSource").getValue());
        assertEquals(BsonDocument.parse("{include:['price']}"), second.getDocument("storedSource"));
        assertEquals("dictionaryA", first.getArray("synonyms").get(0).asDocument()
                .getDocument("source").getString("collection").getValue());
        assertEquals("dictionaryB", second.getArray("synonyms").get(0).asDocument()
                .getDocument("source").getString("collection").getValue());
        assertEquals("standard", first.getArray("analyzers").get(0).asDocument()
                .getDocument("tokenizer").getString("type").getValue());
        assertEquals("keyword", second.getArray("analyzers").get(0).asDocument()
                .getDocument("tokenizer").getString("type").getValue());
        assertTrue(first.getDocument("mappings").getDocument("fields").containsKey("title"));
        assertFalse(second.getDocument("mappings").getDocument("fields").containsKey("title"));
        assertEquals(BsonDocument.parse("{price:1}"), first.getDocument("sort"));
        assertEquals(BsonDocument.parse("{price:-1}"), second.getDocument("sort"));
    }

    @Test
    public void aSingleDefaultOrUnnamedIndexReceivesUnnamedAndExplicitDefaultFields() {
        assertEquals("default", resolve(SingleDefault.class).get(0).getName());
        assertNull(resolve(SingleUnnamed.class).get(0).getName());
        Arrays.asList(SingleDefault.class, SingleUnnamed.class).forEach(entity -> {
            List<SearchIndexModel> models = resolve(entity);
            assertEquals(1, models.size());
            BsonDocument definition = encode(models.get(0));
            assertEquals(2, definition.getDocument("mappings").getDocument("fields").size());
            assertEquals(BsonDocument.parse("{price:1}"), definition.getDocument("sort"));
        });
    }

    @Test
    public void anUnnamedIndexInMultipleDeclarationsCanBeReferencedAsDefault() {
        List<SearchIndexModel> models = resolve(UnnamedAndNamed.class);
        assertEquals(2, models.size());
        assertNull(models.get(0).getName());
        assertEquals(BsonDocument.parse("{price:1}"), encode(models.get(0)).getDocument("sort"));
    }

    @Test
    public void multipleIndexesRequireNamesForBothFieldAndSortAnnotations() {
        assertInvalid(MissingFieldOwner.class, "title.MongoSearchField.name");
        assertInvalid(MissingSortOwner.class, "price.MongoSearchSort.name");
        assertInvalid(InheritedMissingOwner.class, "price.MongoSearchField.name");
    }

    @Test
    public void unknownOrBlankNamesCannotCreateAccidentalIndexes() {
        assertInvalid(UnknownFieldOwner.class, "unknown index name: typo");
        assertInvalid(UnknownSortOwner.class, "unknown index name: typo");
        assertInvalid(MultipleUnknownOwner.class, "unknown index name: typo");
        assertInvalid(BlankOwner.class, "must not be blank");
        assertInvalid(BlankIndex.class, "must not be blank");
    }

    @Test
    public void duplicateNamesIncludeTheImplicitServerDefault() {
        assertInvalid(DuplicateNames.class, "duplicate index name: first");
        assertInvalid(DuplicateDefaults.class, "duplicate index name: default");
    }

    @Test
    public void nestedAnnotationsCannotOverrideTheirEnclosingIndex() {
        assertInvalid(NestedFieldOwner.class, "first.fields.name");
        assertInvalid(NestedSortOwner.class, "first.sort.name");
    }

    @Test
    public void aLaterInvalidIndexLeavesExistingMetadataUntouched() {
        assertInvalid(SecondInvalid.class, "index second: numPartitions");
        assertInvalid(CrossIndexAnalyzer.class, "index second: analyzer");
        assertInvalid(CrossIndexTypeSet.class, "typeSet");
    }

    private static void assertInvalid(Class<?> entity, String detail) {
        IndexMetaObject meta = new IndexMetaObject();
        SearchIndexModel existing = new SearchIndexModel("existing", new Document("mappings",
                new Document("dynamic", true)));
        List<SearchIndexModel> models = new ArrayList<>();
        models.add(existing);
        meta.setSearchIndexModels(models);
        MongoPlusConvertException exception = assertThrows(MongoPlusConvertException.class,
                () -> new SearchParse().parse(TypeInformation.of(entity), meta));
        assertTrue(exception.getMessage(), exception.getMessage().contains(entity.getName()));
        assertTrue(exception.getMessage(), exception.getMessage().contains(detail));
        assertSame(models, meta.getSearchIndexModels());
        assertEquals(1, models.size());
        assertSame(existing, models.get(0));
    }

    private static List<SearchIndexModel> resolve(Class<?> entity) {
        return new Resolver().resolve(entity).getSearchIndexModels();
    }

    private static BsonDocument encode(SearchIndexModel model) {
        return model.getDefinition().toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
    }

    @MongoSearchIndex(name = "first", dynamic = true)
    @MongoSearchIndex(name = "second", dynamic = true)
    public static class RepeatedDynamic {}

    @MongoSearchIndexes({@MongoSearchIndex(name = "first", dynamic = true),
            @MongoSearchIndex(name = "second", dynamic = true)})
    public static class ContainerDynamic {}

    @MongoSearchIndex(name = "first", customAnalyzer = "text",
            analyzers = "{\"name\":\"text\",\"tokenizer\":{\"type\":\"standard\"}}",
            fields = @MongoSearchField(fieldName = "title"),
            storedSource = @MongoSearchStoredSource(true),
            synonyms = @MongoSearchSynonym(name = "terms", sourceCollection = "dictionaryA", customAnalyzer = "text"))
    @MongoSearchIndex(name = "second", numPartitions = 2, customAnalyzer = "text",
            analyzers = "{\"name\":\"text\",\"tokenizer\":{\"type\":\"keyword\"}}",
            storedSource = @MongoSearchStoredSource(include = "price"),
            synonyms = @MongoSearchSynonym(name = "terms", sourceCollection = "dictionaryB", customAnalyzer = "text"))
    public static class Isolated {
        @MongoSearchField(name = "first", type = SearchFieldType.NUMBER)
        @MongoSearchField(name = "second", type = SearchFieldType.NUMBER)
        @MongoSearchSort(name = "first")
        @MongoSearchSort(name = "second", order = IndexDirection.DESC)
        private int price;
    }

    public static class ImplicitFields {
        @MongoSearchField(type = SearchFieldType.NUMBER)
        @MongoSearchSort
        private int price;
        @MongoSearchField(name = "default")
        private String title;
    }

    @MongoSearchIndex
    public static class SingleDefault extends ImplicitFields {}
    @MongoSearchIndex(name = "")
    public static class SingleUnnamed extends ImplicitFields {}

    @MongoSearchIndex(name = "")
    @MongoSearchIndex(name = "second", dynamic = true)
    public static class UnnamedAndNamed {
        @MongoSearchField(name = "default", type = SearchFieldType.NUMBER)
        @MongoSearchSort(name = "default")
        private int price;
    }

    @MongoSearchIndex(name = "first", dynamic = true)
    @MongoSearchIndex(name = "second", dynamic = true)
    public static class MissingFieldOwner {
        @MongoSearchField
        private String title;
    }

    @MongoSearchIndex(name = "first", dynamic = true)
    @MongoSearchIndex(name = "second", dynamic = true)
    public static class MissingSortOwner {
        @MongoSearchSort
        private int price;
    }

    @MongoSearchIndex(name = "first", dynamic = true)
    @MongoSearchIndex(name = "second", dynamic = true)
    public static class InheritedMissingOwner extends ImplicitFields {}

    @MongoSearchIndex(name = "first", dynamic = true)
    public static class UnknownFieldOwner {
        @MongoSearchField(name = "typo")
        private String title;
    }

    @MongoSearchIndex(name = "first", dynamic = true)
    public static class UnknownSortOwner {
        @MongoSearchSort(name = "typo")
        private int price;
    }

    @MongoSearchIndex(name = "first", dynamic = true)
    @MongoSearchIndex(name = "second", dynamic = true)
    public static class MultipleUnknownOwner extends UnknownFieldOwner {}

    @MongoSearchIndex(name = "first", dynamic = true)
    public static class BlankOwner {
        @MongoSearchField(name = " ")
        private String title;
    }

    @MongoSearchIndex(name = " ", dynamic = true)
    public static class BlankIndex {}
    @MongoSearchIndex(name = "first", dynamic = true)
    @MongoSearchIndex(name = "first", dynamic = true)
    public static class DuplicateNames {}
    @MongoSearchIndex(name = "", dynamic = true)
    @MongoSearchIndex(dynamic = true)
    public static class DuplicateDefaults {}

    @MongoSearchIndex(name = "first", fields = @MongoSearchField(name = "second", fieldName = "title"))
    @MongoSearchIndex(name = "second", dynamic = true)
    public static class NestedFieldOwner {}
    @MongoSearchIndex(name = "first", dynamic = true,
            sort = @MongoSearchSort(name = "second", fieldName = "price"))
    @MongoSearchIndex(name = "second", dynamic = true)
    public static class NestedSortOwner {}

    @MongoSearchIndex(name = "first", dynamic = true)
    @MongoSearchIndex(name = "second", dynamic = true, numPartitions = 3)
    public static class SecondInvalid {}
    @MongoSearchIndex(name = "first", dynamic = true,
            analyzers = "{\"name\":\"text\",\"tokenizer\":{\"type\":\"standard\"}}")
    @MongoSearchIndex(name = "second", dynamic = true, customAnalyzer = "text")
    public static class CrossIndexAnalyzer {}
    @MongoSearchIndex(name = "first", dynamic = true,
            typeSets = "{\"name\":\"types\",\"types\":[{\"type\":\"string\"}]}")
    @MongoSearchIndex(name = "second", dynamicTypeSet = "types")
    public static class CrossIndexTypeSet {}
}
