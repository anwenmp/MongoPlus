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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * LocalTime 编解码器。
 * 对对象进行编码和解码 LocalTime DateTime数据存储精度为毫秒级。
 * 在 的 EpochDay 中将值与 EpochDay ZoneOffset.UTC相互转换LocalTime
 * @author anwen
 */
public class LocalTimeCodec extends DateTimeBasedCodec<LocalTime> {

    @Override
    public LocalTime decode(final BsonReader reader, final DecoderContext decoderContext) {
        return Instant.ofEpochMilli(validateAndReadDateTime(reader)).atOffset(ZoneOffset.UTC).toLocalTime();
    }

    /**
     * 将 type 参数 T 的实例编码为 BSON 值。
     * 通过 LocalTime.atDate(LocalDate) 和 java.time.LocalDateTime.toInstant(ZoneOffset)
     * 在 EpochDay 将 LocalTime 转换为 ZoneOffset.UTC 。
     */
    @Override
    public void encode(final BsonWriter writer, final LocalTime value, final EncoderContext encoderContext) {
        writer.writeDateTime(value.atDate(LocalDate.ofEpochDay(0L)).toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    @Override
    public Class<LocalTime> getEncoderClass() {
        return LocalTime.class;
    }
}
