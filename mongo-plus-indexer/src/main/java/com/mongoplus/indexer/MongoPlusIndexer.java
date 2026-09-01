package com.mongoplus.indexer;

import com.mongoplus.indexer.json.JsonWriter;
import com.mongoplus.indexer.model.MongoPlusApiIndex;
import com.mongoplus.indexer.scanner.SourceScanner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** MongoPlus API Index 的公共生成入口。 */
public final class MongoPlusIndexer {
    private final MongoPlusIndexerConfig config;

    public MongoPlusIndexer(MongoPlusIndexerConfig config) {
        if (config == null) { throw new IllegalArgumentException("config 不能为空"); }
        this.config = config;
    }

    /** 扫描源码并返回内存索引，不产生文件副作用。 */
    public MongoPlusApiIndex generate() throws IOException {
        MongoPlusApiIndex index = new SourceScanner(config).scan();
        if (config.isIncludeGeneratedAt()) {
            index.asMap().put("generatedAt", java.time.Instant.now().toString());
        }
        return index;
    }

    /** 将索引写入配置的输出路径。 */
    public Path write(MongoPlusApiIndex index) throws IOException {
        if (config.getOutput() == null) { throw new IllegalStateException("未配置输出路径"); }
        return write(index, config.getOutput());
    }

    /** 将索引写入指定路径。 */
    public Path write(MongoPlusApiIndex index, Path output) throws IOException {
        Path normalized = output.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) { Files.createDirectories(parent); }
        try (BufferedWriter writer = Files.newBufferedWriter(normalized, StandardCharsets.UTF_8)) {
            JsonWriter.write(index.asMap(), writer);
        }
        return normalized;
    }

    /** 扫描并写入配置的输出路径。 */
    public MongoPlusApiIndex generateAndWrite() throws IOException {
        MongoPlusApiIndex index = generate();
        write(index);
        return index;
    }
}
