package com.mongoplus.indexer.cli;

import com.mongoplus.indexer.MongoPlusIndexer;
import com.mongoplus.indexer.MongoPlusIndexerConfig;
import com.mongoplus.indexer.model.MongoPlusApiIndex;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** MongoPlus API Index 命令行入口。 */
public final class MongoPlusIndexerMain {
    private MongoPlusIndexerMain() {
    }

    public static void main(String[] args) throws Exception {
        Path currentDir = Paths.get(System.getProperty("user.dir"))
                .toAbsolutePath()
                .normalize();

        Path projectRoot;

        // IDEA Working Directory 是 MongoPlus 项目根目录
        if (Files.isDirectory(currentDir.resolve("mongo-plus-core"))
                && Files.isDirectory(currentDir.resolve("mongo-plus-indexer"))) {
            projectRoot = currentDir;
        }
        // IDEA Working Directory 是 mongo-plus-indexer 模块目录
        else if ("mongo-plus-indexer".equals(currentDir.getFileName().toString())) {
            projectRoot = currentDir.getParent();
        } else {
            throw new IllegalStateException(
                    "无法识别 MongoPlus 项目根目录，当前目录: " + currentDir
            );
        }

        Path output = projectRoot
                .resolve("mongo-plus-indexer")
                .resolve("target")
                .resolve("generated-sources")
                .resolve("mongo-plus-api-aggregate-index.json");

        MongoPlusIndexerConfig config = MongoPlusIndexerConfig
                .forMongoPlusProject(projectRoot)
                .output(output)
                .build();

        MongoPlusApiIndex index = new MongoPlusIndexer(config)
                .generateAndWrite();

        System.out.println("MongoPlus API Index: " + output);
    }
}