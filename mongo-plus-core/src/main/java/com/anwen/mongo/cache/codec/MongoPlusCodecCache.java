package com.anwen.mongo.cache.codec;

import com.anwen.mongo.codecs.MongoPlusCodec;
import com.anwen.mongo.toolkit.CollUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * MongoPlusCodec 编解码器缓存
 * @author anwen
 */
public class MongoPlusCodecCache {

    private static final List<MongoPlusCodec<?>> codecListCache = new ArrayList<>();

    public static boolean isEmpty(){
        return CollUtil.isEmpty(codecListCache);
    }

    public static void addCodec(MongoPlusCodec<?> codec){
        codecListCache.add(codec);
    }

    public static void addAllCodec(Collection<MongoPlusCodec<?>> codecs){
        codecListCache.addAll(codecs);
    }

    public static List<MongoPlusCodec<?>> getAllCodec(){
        return codecListCache;
    }

    public static void clear(){
        codecListCache.clear();
    }

    public static void remove(MongoPlusCodec<?> codec){
        codecListCache.remove(codec);
    }

}
