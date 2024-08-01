package com.anwen.mongo.cache.codec;

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
     * @author anwen
     * @date 2024/7/25 下午4:10
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
    }};

    /**
     * 获取所有默认编解码器
     * @author anwen
     * @date 2024/7/25 下午4:13
     */
    public static List<CodecProvider> getAllCodecProvider(){
        return codecProviderList;
    }

    /**
     * 设置默认编解码器
     * @author anwen
     * @date 2024/7/25 下午4:14
     */
    public static void addCodecProvider(CodecProvider codecProvider){
        codecProviderList.add(codecProvider);
    }

    private static CodecRegistry DEFAULT_CODEC_REGISTRY;

    /**
     * 获取默认编解码器
     * @author anwen
     * @date 2024/7/25 下午4:15
     */
    public static CodecRegistry getDefaultCodecRegistry() {
        if (DEFAULT_CODEC_REGISTRY == null) {
            DEFAULT_CODEC_REGISTRY = fromProviders(codecProviderList);
        }
        return DEFAULT_CODEC_REGISTRY;
    }

}
