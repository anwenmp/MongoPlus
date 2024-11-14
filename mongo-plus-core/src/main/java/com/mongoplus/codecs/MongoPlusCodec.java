package com.mongoplus.codecs;

import org.bson.codecs.Codec;

/**
 * 此接口的实现可以对{@code T}类型的值进行编码和解码。
 * @author anwen
 */
public interface MongoPlusCodec<T> extends Codec<T> {
}
