package com.mongoplus.indexer.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/** 不依赖第三方库的 JSON 序列化器。 */
public final class JsonWriter {
    private JsonWriter() {}

    public static void write(Object value, Writer writer) throws IOException {
        writeValue(value, writer, 0);
        writer.write('\n');
    }

    private static void writeValue(Object value, Writer writer, int depth) throws IOException {
        if (value == null) { writer.write("null"); return; }
        if (value instanceof String || value instanceof Character || value instanceof Enum) {
            writeString(String.valueOf(value), writer); return;
        }
        if (value instanceof Boolean) { writer.write(String.valueOf(value)); return; }
        if (value instanceof Number) {
            if (value instanceof Double && !Double.isFinite(((Double) value).doubleValue())) {
                throw new IllegalArgumentException("JSON 不支持非有限浮点数");
            }
            if (value instanceof Float && !Float.isFinite(((Float) value).floatValue())) {
                throw new IllegalArgumentException("JSON 不支持非有限浮点数");
            }
            writer.write(String.valueOf(value)); return;
        }
        if (value instanceof Map) { writeObject((Map<?, ?>) value, writer, depth); return; }
        if (value instanceof Iterable) { writeArray((Iterable<?>) value, writer, depth); return; }
        throw new IllegalArgumentException("不支持的 JSON 类型: " + value.getClass().getName());
    }

    private static void writeObject(Map<?, ?> value, Writer writer, int depth) throws IOException {
        writer.write('{');
        Iterator<? extends Map.Entry<?, ?>> iterator = value.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<?, ?> entry = iterator.next();
            writer.write('\n'); indent(writer, depth + 1);
            writeString(String.valueOf(entry.getKey()), writer); writer.write(": ");
            writeValue(entry.getValue(), writer, depth + 1);
            if (iterator.hasNext()) { writer.write(','); }
        }
        if (!value.isEmpty()) { writer.write('\n'); indent(writer, depth); }
        writer.write('}');
    }

    private static void writeArray(Iterable<?> value, Writer writer, int depth) throws IOException {
        writer.write('[');
        Iterator<?> iterator = value.iterator();
        while (iterator.hasNext()) {
            writer.write('\n'); indent(writer, depth + 1);
            writeValue(iterator.next(), writer, depth + 1);
            if (iterator.hasNext()) { writer.write(','); }
        }
        if (((Iterable<?>) value).iterator().hasNext()) { writer.write('\n'); indent(writer, depth); }
        writer.write(']');
    }

    private static void writeString(String value, Writer writer) throws IOException {
        writer.write('"');
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '"': writer.write("\\\""); break;
                case '\\': writer.write("\\\\"); break;
                case '\b': writer.write("\\b"); break;
                case '\f': writer.write("\\f"); break;
                case '\n': writer.write("\\n"); break;
                case '\r': writer.write("\\r"); break;
                case '\t': writer.write("\\t"); break;
                default:
                    if (current < 0x20) { writer.write(String.format("\\u%04x", (int) current)); }
                    else { writer.write(current); }
            }
        }
        writer.write('"');
    }

    private static void indent(Writer writer, int depth) throws IOException {
        for (int i = 0; i < depth; i++) { writer.write("  "); }
    }
}
