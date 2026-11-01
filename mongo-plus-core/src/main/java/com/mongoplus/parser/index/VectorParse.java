package com.mongoplus.parser.index;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.HnswSearchIndexOptions;
import com.mongodb.client.model.SearchIndexDefinition;
import com.mongodb.client.model.SearchIndexModel;
import com.mongodb.client.model.SearchIndexType;
import com.mongodb.client.model.VectorSearchIndexDefinition;
import com.mongodb.client.model.VectorSearchIndexFields;
import com.mongoplus.annotation.index.vector.MongoVectorField;
import com.mongoplus.annotation.index.vector.MongoVectorHnswOptions;
import com.mongoplus.annotation.index.vector.MongoVectorIndex;
import com.mongoplus.annotation.index.vector.MongoVectorStoredSource;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.enums.SearchIndexingMethod;
import com.mongoplus.enums.SearchQuantization;
import com.mongoplus.enums.SearchVectorSimilarity;
import com.mongoplus.enums.VectorFieldType;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.SimpleFieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mongoplus.parser.index.SearchDefinitionValidator.*;

/**
 * 将独立 Vector Search 注解合并为 Driver 模型，不访问数据库。
 * <p>复用 Search 元数据和 DDL 生命周期；全部配置验证成功后才追加模型。</p>
 */
public class VectorParse extends AbstractParser {
    private static final String DEFAULT_INDEX_NAME = "default";
    private static final int UNSPECIFIED = -1;
    private static final int MAX_DIMENSIONS = 8192;

    @Override
    public Function<TypeInformation, Boolean> activate() {
        return type -> type.getClazz().getAnnotationsByType(MongoVectorIndex.class).length > 0
                || type.getFields().stream().anyMatch(field ->
                field.getField().getAnnotationsByType(MongoVectorField.class).length > 0);
    }

    @Override
    public void parse(TypeInformation type, IndexMetaObject meta) {
        try {
            MongoVectorIndex[] indexes = type.getClazz().getAnnotationsByType(MongoVectorIndex.class);
            Map<String, VectorConfiguration> configurations = new LinkedHashMap<>();
            for (MongoVectorIndex index : indexes) {
                String name = indexName(index.name());
                require(!configurations.containsKey(name), "MongoVectorIndex.name", "duplicate index name: " + name);
                VectorConfiguration configuration = new VectorConfiguration(index, index.name(), type);
                configurations.put(name, configuration);
                for (MongoVectorField field : index.fields()) {
                    require(field.name().isEmpty(), name + ".fields.name", "must be empty inside MongoVectorIndex");
                    configuration.add(field, mappedPath(type, field.fieldName()));
                }
            }
            for (FieldInformation field : type.getFields()) {
                for (MongoVectorField annotation : field.getField().getAnnotationsByType(MongoVectorField.class)) {
                    VectorConfiguration configuration = configuration(configurations, indexes, annotation.name(), type);
                    String path = annotation.fieldName().isEmpty() ? field.getIdOrCamelCaseName()
                            : mappedPath(type, annotation.fieldName());
                    configuration.add(annotation, fieldName(path, true));
                }
            }
            List<SearchIndexModel> models = configurations.values().stream()
                    .map(VectorConfiguration::model).collect(Collectors.toList());
            Set<String> names = new HashSet<>();
            if (meta.getSearchIndexModels() != null) {
                meta.getSearchIndexModels().forEach(model -> names.add(indexName(model.getName())));
            }
            // Search 和 Vector Search 共用名称空间，避免直到远端创建时才发现跨类型重名。
            models.forEach(model -> require(names.add(indexName(model.getName())),
                    "name", "duplicate Search/Vector Search index name: " + indexName(model.getName())));
            if (!models.isEmpty()) {
                if (meta.getSearchIndexModels() == null) {
                    meta.setSearchIndexModels(new ArrayList<>());
                }
                meta.getSearchIndexModels().addAll(models);
            }
        } catch (MongoPlusConvertException exception) {
            throw failure("Invalid MongoDB Vector Search indexes for " + type.getClazz().getName()
                    + ": " + exception.getMessage(), exception);
        }
    }

    private static VectorConfiguration configuration(Map<String, VectorConfiguration> configurations,
                                                     MongoVectorIndex[] indexes, String name, TypeInformation type) {
        indexName(name);
        if (indexes.length == 0) {
            return configurations.computeIfAbsent(indexName(name), key -> new VectorConfiguration(null, name, type));
        }
        require(indexes.length == 1 || !name.isEmpty(), "MongoVectorField.name",
                "must specify an index name when multiple MongoVectorIndex annotations are present");
        String resolved = indexName(name.isEmpty() ? indexes[0].name() : name);
        require(configurations.containsKey(resolved), "MongoVectorField.name", "unknown index name: " + resolved);
        return configurations.get(resolved);
    }

    private static String indexName(String name) {
        if (name == null) {
            return DEFAULT_INDEX_NAME;
        }
        require(name.isEmpty() || !name.trim().isEmpty(), "name", "must not be blank");
        return name.isEmpty() ? DEFAULT_INDEX_NAME : name;
    }

    /** 普通路径保持原样；$属性路径逐段使用实体元数据，不构造嵌套实体实例。 */
    private static String mappedPath(TypeInformation type, String value) {
        required(value, "fieldName");
        if (!value.startsWith("$")) {
            return fieldName(value, true);
        }
        String[] parts = fieldName(value.substring(1), true).split("\\.");
        Class<?> current = type.getClazz();
        List<String> mapped = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            Field field = ClassTypeUtil.getFields(current).stream()
                    .filter(candidate -> candidate.getName().equals(part)).findFirst().orElse(null);
            require(field != null && !java.lang.reflect.Modifier.isStatic(field.getModifiers()),
                    value, "unknown entity property: " + part);
            mapped.add(new SimpleFieldInformation<>(null, field).getIdOrCamelCaseName());
            if (i < parts.length - 1) {
                try {
                    current = ClassTypeUtil.getClassByFieldType(field);
                } catch (RuntimeException exception) {
                    throw failure(value + ": cannot resolve nested entity type for " + part, exception);
                }
            }
        }
        return fieldName(String.join(".", mapped), true);
    }

    private static final class VectorConfiguration {
        private final MongoVectorIndex index;
        private final String name;
        private final TypeInformation type;
        private final Map<String, Bson> fields = new LinkedHashMap<>();
        private final List<String> vectors = new ArrayList<>();

        private VectorConfiguration(MongoVectorIndex index, String name, TypeInformation type) {
            this.index = index;
            this.name = name;
            this.type = type;
        }

        private void add(MongoVectorField field, String path) {
            String location = indexName(name) + ".fields." + path;
            require(!fields.containsKey(path), location, "duplicate field path");
            MongoVectorHnswOptions options = field.hnswOptions();
            boolean hasHnsw = options.maxEdges() != UNSPECIFIED || options.numEdgeCandidates() != UNSPECIFIED;
            if (field.type() == VectorFieldType.FILTER) {
                require(field.numDimensions() == UNSPECIFIED && field.similarity() == SearchVectorSimilarity.DEFAULT
                                && field.quantization() == SearchQuantization.EMPTY
                                && field.indexingMethod() == SearchIndexingMethod.NONE && !hasHnsw,
                        location, "filter fields cannot specify vector options");
                fields.put(path, VectorSearchIndexFields.filterField(path));
                return;
            }
            require(field.numDimensions() > 0 && field.numDimensions() <= MAX_DIMENSIONS,
                    location + ".numDimensions", "required in 1..8192");
            require(field.similarity() != SearchVectorSimilarity.DEFAULT, location + ".similarity", "required for vector fields");
            require(field.quantization() != SearchQuantization.BINARY || field.numDimensions() % 8 == 0,
                    location + ".numDimensions", "binary quantization requires a multiple of 8");
            require(!hasHnsw || field.indexingMethod() != SearchIndexingMethod.FLAT,
                    location + ".hnswOptions", "not supported with flat");
            VectorSearchIndexFields.VectorField vector = VectorSearchIndexFields.vectorField(path)
                    .numDimensions(field.numDimensions()).similarity(field.similarity().getValue());
            if (field.indexingMethod() != SearchIndexingMethod.NONE) {
                vector.indexingMethod(field.indexingMethod().getValue());
            }
            if (hasHnsw) {
                HnswSearchIndexOptions hnsw = new HnswSearchIndexOptions();
                if (options.maxEdges() != UNSPECIFIED) {
                    require(options.maxEdges() >= 16 && options.maxEdges() <= 64,
                            location + ".hnswOptions.maxEdges", "must be in 16..64");
                    hnsw.maxEdges(options.maxEdges());
                }
                if (options.numEdgeCandidates() != UNSPECIFIED) {
                    require(options.numEdgeCandidates() >= 100 && options.numEdgeCandidates() <= 3200,
                            location + ".hnswOptions.numEdgeCandidates", "must be in 100..3200");
                    hnsw.numEdgeCandidates(options.numEdgeCandidates());
                }
                vector.hnswOptions(hnsw);
            }
            Bson definition = vector;
            if (field.quantization() != SearchQuantization.EMPTY) {
                // Driver 5.13 的 VectorField 尚无 quantization setter，只补充这一个官方参数。
                definition = vector.toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry())
                        .append("quantization", new BsonString(field.quantization().getValue()));
            }
            fields.put(path, definition);
            vectors.add(path);
        }

        private SearchIndexModel model() {
            require(!vectors.isEmpty(), indexName(name) + ".fields", "requires at least one vector field");
            VectorSearchIndexDefinition definition = SearchIndexDefinition.vectorSearch(new ArrayList<>(fields.values()));
            if (index != null) {
                MongoVectorStoredSource source = index.storedSource();
                require(source.include().length == 0 || source.exclude().length == 0,
                        indexName(name) + ".storedSource", "include and exclude are mutually exclusive");
                if (source.include().length > 0 || source.exclude().length > 0) {
                    boolean include = source.include().length > 0;
                    List<String> paths = Arrays.stream(include ? source.include() : source.exclude())
                            .map(path -> mappedPath(type, path)).collect(Collectors.toList());
                    definition = definition.storedSource(new Document(include ? "include" : "exclude", paths));
                }
                if (!index.nestedRoot().isEmpty()) {
                    String root = mappedPath(type, index.nestedRoot());
                    vectors.forEach(path -> require(path.startsWith(root + "."), indexName(name) + ".nestedRoot",
                            "vector path must be a child of nestedRoot: " + path));
                    // Driver 5.13 尚无 nestedRoot API，保留官方构建的 fields/storedSource 后补充。
                    BsonDocument bson = definition.toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
                    bson.append("nestedRoot", new BsonString(root));
                    return new SearchIndexModel(name.isEmpty() ? null : name, bson, SearchIndexType.vectorSearch());
                }
            }
            return new SearchIndexModel(name.isEmpty() ? null : name, definition);
        }
    }
}
