package com.mongoplus.aggregate.pipeline;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;


/**
 * @author anwen
 */
public class SimpleExpression<TExpression> implements Bson {
    private final String name;
    private final TExpression expression;

    public SimpleExpression(final String name, final TExpression expression) {
        this.name = name;
        this.expression = expression;
    }

    @Override
    public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
        BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());

        writer.writeStartDocument();
        writer.writeName(name);
        encodeValue(writer, expression, codecRegistry);
        writer.writeEndDocument();

        return writer.getDocument();
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    static <TItem> void encodeValue(final BsonDocumentWriter writer, final TItem value, final CodecRegistry codecRegistry) {
        if (value == null) {
            writer.writeNull();
        } else if (value instanceof Bson) {
            codecRegistry.get(BsonDocument.class).encode(writer,
                    ((Bson) value).toBsonDocument(BsonDocument.class, codecRegistry),
                    EncoderContext.builder().build());
        } else {
            ((Encoder) codecRegistry.get(value.getClass())).encode(writer, value, EncoderContext.builder().build());
        }
    }

}
