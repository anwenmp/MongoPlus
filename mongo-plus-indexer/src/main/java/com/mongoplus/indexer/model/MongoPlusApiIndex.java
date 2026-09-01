package com.mongoplus.indexer.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** MongoPlus 条件构造 API 的稳定、机器可读索引。 */
public final class MongoPlusApiIndex {
    private final Map<String, Object> data = new LinkedHashMap<String, Object>();

    public MongoPlusApiIndex(String mongoPlusVersion) {
        data.put("schemaVersion", "1.1");
        data.put("project", "MongoPlus");
        data.put("mongoPlusVersion", mongoPlusVersion);
        data.put("primaryScanModule", "mongo-plus-core");
        data.put("primaryPackages", new ArrayList<Object>());
        data.put("scanStatistics", new LinkedHashMap<String, Object>());
        data.put("wrappers", new ArrayList<Object>());
        data.put("methodFamilies", new ArrayList<Object>());
        data.put("types", new ArrayList<Object>());
        data.put("specialTypes", new ArrayList<Object>());
        data.put("concepts", new ArrayList<Object>());
    }

    public Map<String, Object> asMap() { return data; }

    @SuppressWarnings("unchecked")
    public List<Object> list(String name) { return (List<Object>) data.get(name); }

    @SuppressWarnings("unchecked")
    public Map<String, Object> object(String name) { return (Map<String, Object>) data.get(name); }

    public List<Object> getMethodFamilies() { return list("methodFamilies"); }
}
