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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static java.lang.String.format;

/**
 * LocalDate 编解码器。
 *
 * <p>编码和解码 {@code LocalDate} 对象到 和 从 {@code DateTime}.</p>
 * <p>将 {@code LocalDate} 值转换为 . 或从 {@link ZoneOffset#UTC}.</p>
 * @author anwen
 */
public class LocalDateCodec extends DateTimeBasedCodec<LocalDate> {

    @Override
    public LocalDate decode(final BsonReader reader, final DecoderContext decoderContext) {
        return Instant.ofEpochMilli(validateAndReadDateTime(reader)).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * {@inheritDoc}
     * <p>通过 {@code LocalDate} to {@link ZoneOffset#UTC} 转换为 {@link LocalDate#atStartOfDay(ZoneId)}.</p>
     * @throws CodecConfigurationException 如果 LocalDate 无法转换为有效的 Bson DateTime。
     */
    @Override
    public void encode(final BsonWriter writer, final LocalDate value, final EncoderContext encoderContext) {
        try {
            writer.writeDateTime(value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } catch (ArithmeticException e) {
            throw new CodecConfigurationException(format("Unsupported LocalDate '%s' could not be converted to milliseconds: %s",
                    value, e.getMessage()), e);
        }
    }

    @Override
    public Class<LocalDate> getEncoderClass() {
        return LocalDate.class;
    }
}
