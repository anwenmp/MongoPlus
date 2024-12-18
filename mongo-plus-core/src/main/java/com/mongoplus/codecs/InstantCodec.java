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

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;

import java.time.Instant;

import static java.lang.String.format;

/**
 * Instant 编解码器
 * 对{@link Instant}对象进行编码和解码。DateTime数据通过 提取 Instant. toEpochMilli() 并存储到毫秒级精度
 * @author anwen
 */
public class InstantCodec extends DateTimeBasedCodec<Instant> {

    @Override
    public Instant decode(final BsonReader reader, final DecoderContext decoderContext) {
        return Instant.ofEpochMilli(validateAndReadDateTime(reader));
    }

    /**
     * {@inheritDoc}
     * @throws CodecConfigurationException 如果 Instant 无法转换为有效的 Bson DateTime。
     */
    @Override
    public void encode(final BsonWriter writer, final Instant value, final EncoderContext encoderContext) {
        try {
            writer.writeDateTime(value.toEpochMilli());
        } catch (ArithmeticException e) {
            throw new CodecConfigurationException(format("Unsupported Instant value '%s' could not be converted to milliseconds: %s",
                    value, e.getMessage()), e);
        }
    }

    @Override
    public Class<Instant> getEncoderClass() {
        return Instant.class;
    }
}
