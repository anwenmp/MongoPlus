package com.mongoplus.indexer.cli;

import com.mongoplus.indexer.MongoPlusIndexer;
import com.mongoplus.indexer.MongoPlusIndexerConfig;
import java.nio.file.Path;
import java.nio.file.Paths;

/** 聚合专用 API Index 命令行入口。 */
public final class MongoPlusPipelineIndexerMain {
    private MongoPlusPipelineIndexerMain() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 0 && (args.length != 2 || !"--project-root".equals(args[0]))) {
            throw new IllegalArgumentException("用法: MongoPlusPipelineIndexerMain [--project-root <目录>]");
        }
        Path root = Paths.get(args.length == 0 ? "." : args[1]).toAbsolutePath().normalize();
        if (args.length == 0 && "mongo-plus-indexer".equals(root.getFileName().toString())) {
            root = root.getParent();
        }
        MongoPlusIndexerConfig config = MongoPlusIndexerConfig.forPipelineProject(root).build();
        new MongoPlusIndexer(config).generateAndWrite();
        System.out.println("MongoPlus Pipeline API Index: " + config.getOutput());
    }
}
