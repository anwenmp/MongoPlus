package com.mongoplus.indexer.cli;

import com.mongoplus.indexer.MongoPlusIndexer;
import com.mongoplus.indexer.MongoPlusIndexerConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** 聚合专用 API Index 命令行入口。 */
public final class MongoPlusPipelineIndexerMain {
    private MongoPlusPipelineIndexerMain() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 0 && (args.length != 2 || !"--project-root".equals(args[0]))) {
            throw new IllegalArgumentException("用法: MongoPlusPipelineIndexerMain [--project-root <目录>]");
        }
        Path root;
        if (args.length == 2) {
            root = Paths.get(args[1]).toAbsolutePath().normalize();
        } else {
            Path currentDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
            // 与 MongoPlusIndexerMain 一致，支持 IDEA 从项目根目录或 indexer 模块启动。
            if (Files.isDirectory(currentDir.resolve("mongo-plus-core"))
                    && Files.isDirectory(currentDir.resolve("mongo-plus-indexer"))) {
                root = currentDir;
            } else if (currentDir.getFileName() != null
                    && "mongo-plus-indexer".equals(currentDir.getFileName().toString())) {
                root = currentDir.getParent();
            } else {
                throw new IllegalStateException("无法识别 MongoPlus 项目根目录，当前目录: " + currentDir);
            }
        }
        MongoPlusIndexerConfig config = MongoPlusIndexerConfig.forPipelineProject(root).build();
        new MongoPlusIndexer(config).generateAndWrite();
        System.out.println("MongoPlus Pipeline API Index: " + config.getOutput());
    }
}
