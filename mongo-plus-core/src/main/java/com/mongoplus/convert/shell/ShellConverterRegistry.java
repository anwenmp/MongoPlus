package com.mongoplus.convert.shell;

import org.bson.BsonDocument;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 按命令名选择内置的 Shell 转换方法。
 *
 * @author anwen
 */
final class ShellConverterRegistry {

    private static final Map<String, BiFunction<String, BsonDocument, String>> CONVERTERS = createConverters();

    private ShellConverterRegistry() {
    }

    /**
     * @param name 小写命令名
     * @return 转换方法，未登记时为 null
     */
    static BiFunction<String, BsonDocument, String> find(String name) {
        return CONVERTERS.get(name);
    }

    /**
     * @return 供并发读取的内置转换方法映射
     */
    private static Map<String, BiFunction<String, BsonDocument, String>> createConverters() {
        Map<String, BiFunction<String, BsonDocument, String>> converters = new HashMap<>();
        converters.put("find", FindShellConverter::convert);
        converters.put("aggregate", AggregateShellConverter::convert);
        converters.put("insert", InsertShellConverter::convert);
        converters.put("update", UpdateShellConverter::convert);
        converters.put("delete", DeleteShellConverter::convert);
        converters.put("distinct", ReadShellConverter::distinct);
        converters.put("count", ReadShellConverter::count);
        converters.put("findandmodify", FindAndModifyShellConverter::convert);
        converters.put("createindexes", IndexShellConverter::create);
        converters.put("listindexes", IndexShellConverter::list);
        converters.put("dropindexes", IndexShellConverter::drop);
        return Collections.unmodifiableMap(converters);
    }
}
