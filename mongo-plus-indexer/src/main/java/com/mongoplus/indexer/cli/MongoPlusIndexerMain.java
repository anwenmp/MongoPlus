package com.mongoplus.indexer.cli;

import com.mongoplus.indexer.MongoPlusIndexer;
import com.mongoplus.indexer.MongoPlusIndexerConfig;
import com.mongoplus.indexer.model.MongoPlusApiIndex;

import java.nio.file.Path;
import java.nio.file.Paths;

/** MongoPlus API Index 命令行入口。 */
public final class MongoPlusIndexerMain {
    private MongoPlusIndexerMain() {}

    public static void main(String[] args) throws Exception {
        if (args.length == 2 && "--project-root".equals(args[0])) {
            MongoPlusIndexerConfig config = MongoPlusIndexerConfig.forMongoPlusProject(Paths.get(args[1])).build();
            MongoPlusApiIndex index = new MongoPlusIndexer(config).generateAndWrite();
            System.out.println("MongoPlus API Index: " + config.getOutput().toAbsolutePath().normalize());
            System.out.println("Method Families: " + index.getMethodFamilies().size());
            return;
        }
        if (args.length < 3) {
            System.err.println("用法: MongoPlusIndexerMain --project-root <root>");
            System.err.println("   或: MongoPlusIndexerMain <version> <output> <sourceRoot> [sourceRoot...]");
            System.exit(2);
        }
        MongoPlusIndexerConfig.Builder builder = MongoPlusIndexerConfig.builder()
                .mongoPlusVersion(args[0])
                .output(Paths.get(args[1]))
                .addPrimaryPackage(MongoPlusIndexerConfig.DEFAULT_PRIMARY_PACKAGE);
        for (int i = 2; i < args.length; i++) { builder.addSourceRoot(Paths.get(args[i])); }
        MongoPlusIndexerConfig builtConfig = builder.build();
        MongoPlusIndexer indexer = new MongoPlusIndexer(builtConfig);
        MongoPlusApiIndex index = indexer.generateAndWrite();
        Path output = builtConfig.getOutput().toAbsolutePath().normalize();
        System.out.println("MongoPlus API Index: " + output);
        System.out.println("Method Families: " + index.getMethodFamilies().size());
    }
}
