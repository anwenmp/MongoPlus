package com.mongoplus.bson;

import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.OverridableUuidRepresentationCodec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import static org.bson.assertions.Assertions.notNull;

/**
 * @author anwen
 */
public class OverridableUuidRepresentationCodecProvider implements CodecProvider {

    private final CodecProvider wrapped;
    private final UuidRepresentation uuidRepresentation;

    public OverridableUuidRepresentationCodecProvider(final CodecProvider wrapped, final UuidRepresentation uuidRepresentation) {
        this.uuidRepresentation = notNull("uuidRepresentation", uuidRepresentation);
        this.wrapped = notNull("wrapped", wrapped);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        Codec<T> codec = wrapped.get(clazz, registry);
        if (codec instanceof OverridableUuidRepresentationCodec) {
            return ((OverridableUuidRepresentationCodec<T>) codec).withUuidRepresentation(uuidRepresentation);
        }
        return codec;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OverridableUuidRepresentationCodecProvider that = (OverridableUuidRepresentationCodecProvider) o;

        if (!wrapped.equals(that.wrapped)) {
            return false;
        }
        return uuidRepresentation == that.uuidRepresentation;
    }

    @Override
    public int hashCode() {
        int result = wrapped.hashCode();
        result = 31 * result + uuidRepresentation.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "OverridableUuidRepresentationCodecRegistry{"
                + "wrapped=" + wrapped
                + ", uuidRepresentation=" + uuidRepresentation
                + '}';
    }

}
