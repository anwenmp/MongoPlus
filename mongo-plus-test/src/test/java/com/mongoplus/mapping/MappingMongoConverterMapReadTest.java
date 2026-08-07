package com.mongoplus.mapping;

import org.bson.Document;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * 验证顶层 Map 结果映射的回归场景。
 */
public class MappingMongoConverterMapReadTest {

    /**
     * 验证顶层 Map TypeReference 读取由 MappingMongoConverter 的 Map 转换分支处理。
     */
    @Test
    public void readMapTypeReferenceShouldConvertDocument() {
        MappingMongoConverter converter = new MappingMongoConverter();
        Document document = new Document("name", "mongo-plus");

        Map<String, Object> result = converter.readInternal(document, new TypeReference<Map<String, Object>>() {
        }, true);

        assertEquals("mongo-plus", result.get("name"));
    }

    private final MappingMongoConverter converter = new MappingMongoConverter();

    /**
     * 验证 Class<Map> 经两参数 readInternal 动态分派到实际 Map 转换逻辑。
     */
    @Test
    public void readClassMapShouldConvertDocument() {
        Map<?, ?> result = converter.read(new Document("name", "mongo-plus"), Map.class);

        assertEquals("mongo-plus", result.get("name"));
    }

    /**
     * 验证参数化 Map 结果类型按 value 泛型完成顶层 Document 转换。
     */
    @Test
    public void readMapTypeReferenceShouldConvertDocument2() {
        Map<String, Object> result = converter.read(new Document("name", "mongo-plus"),
                new TypeReference<Map<String, Object>>() {
                });

        assertEquals("mongo-plus", result.get("name"));
    }

    /**
     * 验证 Document 结果类型直接保留 Driver 返回对象。
     */
    @Test
    public void readDocumentClassShouldReturnOriginalDocument() {
        Document document = new Document("name", "mongo-plus");

        assertSame(document, converter.read(document, Document.class));
    }

}
