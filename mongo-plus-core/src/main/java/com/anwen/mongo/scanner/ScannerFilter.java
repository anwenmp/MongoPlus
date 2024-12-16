package com.anwen.mongo.scanner;

import com.anwen.mongo.scanner.meta.MetadataReader;

/**
 * 扫描过滤器
 * @author anwen
 */
public interface ScannerFilter {

    /**
     * 筛选条件是否和元数据匹配
     * @return {@link boolean}
     * @author anwen
     */
    boolean match(MetadataReader metadataReader);

}
