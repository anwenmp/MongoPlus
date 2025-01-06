package com.mongoplus.bson;

import com.mongoplus.domain.MongoPlusUnsupportedException;
import com.mongoplus.support.SFunction;
import org.bson.Document;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 这是一个不可改变的Document
 * @author anwen
 */
@SuppressWarnings("all")
public class ImmutableDocument extends MongoPlusDocument {

    public ImmutableDocument(Document document) {
        super(document);
    }

    @Override
    public <T, R> void put(SFunction<T, R> key, Object value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public <T, R> void putIsNotNull(String key, Object value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public <T, V> void put(SFunction<T, ?> key, SFunction<V, ?> value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public <T, V> void putOption(SFunction<T, ?> key, SFunction<V, ?> value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public <T, R> void append(SFunction<T, R> key, Object value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public <T, R> Object remove(SFunction<T, R> key) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public <T, R> Object remove(SFunction<T, R> key, Object value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public Object put(String key, Object value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public Object remove(Object key) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public void clear() {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public Document append(String key, Object value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public Object replace(String key, Object value) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        throw new MongoPlusUnsupportedException("This is an immutable document");
    }
}
