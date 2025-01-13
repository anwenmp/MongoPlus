package com.mongoplus.codecs;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeCodec implements MongoPlusCodec<LocalDateTime> {
    @Override
    public LocalDateTime decode(BsonReader reader, DecoderContext decoderContext) {
        Instant instant = Instant.ofEpochMilli(reader.readDateTime());
        return instant.atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    @Override
    public void encode(BsonWriter writer, LocalDateTime value, EncoderContext encoderContext) {
        writer.writeDateTime(
                value.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
        );
    }

    @Override
    public Class<LocalDateTime> getEncoderClass() {
        return LocalDateTime.class;
    }
}
