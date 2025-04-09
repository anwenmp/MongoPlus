package com.mongoplus.cache.codec;

import com.mongodb.client.model.geojson.codecs.GeoJsonCodecProvider;
import com.mongoplus.bson.OverridableUuidRepresentationCodecProvider;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.Jsr310CodecProvider;

import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

/**
 * 存储MapCodecProvider中已有的解码器，以及后续添加的解码器，作为缓存
 *
 * @author JiaChaoYang
 **/
public class MapCodecCache {

    /**
     * 默认编解码器
     * 默认不加载：
     * {@link org.bson.codecs.CollectionCodecProvider},
     * {@link org.bson.codecs.EnumCodecProvider},
     * 因为有些版本并不支持，如需使用，请自行添加
     */
    private static final List<CodecProvider> codecProviderList = new ArrayList<CodecProvider>() {{
        add(new ValueCodecProvider());
        add(new BsonValueCodecProvider());
        add(new DocumentCodecProvider());
        add(new IterableCodecProvider());
        add(new MapCodecProvider());
        add(new Jsr310CodecProvider());
        add(new JsonObjectCodecProvider());
        add(new BsonCodecProvider());
        add(new GeoJsonCodecProvider());
    }};

    /**
     * 获取所有默认编解码器
     * @author anwen
     */
    public static List<CodecProvider> getAllCodecProvider(){
        return codecProviderList;
    }

    /**
     * 设置默认编解码器
     * @author anwen
     */
    public static void addCodecProvider(CodecProvider codecProvider){
        codecProviderList.add(codecProvider);
    }

    private static CodecRegistry DEFAULT_CODEC_REGISTRY;

    private static Codec<Document> DEFAULT_CODEC;

    /**
     * 获取默认编解码器
     * @author anwen
     */
    public static CodecRegistry getDefaultCodecRegistry() {
        if (DEFAULT_CODEC_REGISTRY == null) {
            DEFAULT_CODEC_REGISTRY = fromProviders(codecProviderList);
        }
        return DEFAULT_CODEC_REGISTRY;
    }

    /**
     * 获取Document默认编解码器
     * @author anwen
     */
    public static Codec<Document> getDefaultCodec() {
        if (DEFAULT_CODEC == null) {
            DEFAULT_CODEC = withUuidRepresentation(fromProviders(codecProviderList), UuidRepresentation.STANDARD)
                    .get(Document.class);
        }
        return DEFAULT_CODEC;
    }

    /**
     * 将给定的 {@link UuidRepresentation} 应用到给定的 {@link CodecRegistry}.
     * @param codecRegistry 代码注册表
     * @param uuidRepresentation UUID表示
     * @return {@link org.bson.codecs.configuration.CodecRegistry} 一个 {@code CodecRegistry}，其中给定的
     * {@code UuidRepresentation} 应用于给定的 {@code CodecRegistry}
     * @author anwen
     */
    public static CodecRegistry withUuidRepresentation(final CodecRegistry codecRegistry, final UuidRepresentation uuidRepresentation) {
        return fromProviders(new OverridableUuidRepresentationCodecProvider(codecRegistry, uuidRepresentation));
    }

}