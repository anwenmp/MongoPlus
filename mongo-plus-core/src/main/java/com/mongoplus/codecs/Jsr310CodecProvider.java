/*
 * Copyright 2008-present MongoDB, Inc.
 * Copyright 2018 Cezary Bartosiak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongoplus.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于 JSR-310 日期和时间 API 类的 CodecProvider。
 * <p>
 * 提供以下基于 JSR-310 的编解码器
 * <ul>
 * <li>{@link InstantCodec}
 * <li>{@link LocalDateCodec}
 * <li>{@link LocalDateTimeCodec}
 * <li>{@link LocalTimeCodec}
 * </ul>
 * @author anwen
 */
public class Jsr310CodecProvider implements CodecProvider {
    private static final Map<Class<?>, Codec<?>> JSR310_CODEC_MAP = new HashMap<>();
    static {
        putCodec(new InstantCodec());
        putCodec(new LocalDateCodec());
        putCodec(new LocalDateTimeCodec());
        putCodec(new LocalTimeCodec());
    }

    private static void putCodec(final Codec<?> codec) {
        JSR310_CODEC_MAP.put(codec.getEncoderClass(), codec);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        return (Codec<T>) JSR310_CODEC_MAP.get(clazz);
    }

    @Override
    public String toString() {
        return "Jsr310CodecProvider{}";
    }
}
