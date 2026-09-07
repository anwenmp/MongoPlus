package com.mongoplus.indexer;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MongoPlus API Index 生成配置。
 *
 * <p>源码根目录仅用于按包名精确定位源码。扫描器不会遍历源码根目录中的全部 Java 文件。</p>
 */
public final class MongoPlusIndexerConfig {

    public static final String DEFAULT_PRIMARY_PACKAGE = "com.mongoplus.conditions";
    public static final String DEFAULT_OUTPUT_FILE = "mongo-plus-api-index.json";
    public static final String PIPELINE_ENTRY_TYPE = "com.mongoplus.aggregate.Aggregate";
    public static final String PIPELINE_OUTPUT_FILE = "mongo-plus-pipeline-api-index.json";

    /** 面向用户的表达式工厂；旧包 ConditionOperators 已废弃，不作为独立入口。 */
    public static final List<String> PIPELINE_EXPRESSION_ROOTS = Collections.unmodifiableList(java.util.Arrays.asList(
            "com.mongoplus.aggregate.pipeline.Accumulators",
            "com.mongoplus.aggregate.pipeline.AggregateOperator",
            "com.mongoplus.aggregate.pipeline.Projections",
            "com.mongoplus.aggregate.pipeline.Sorts",
            "com.mongoplus.conditions.operation.ConditionOperators"));

    private final List<Path> sourceRoots;
    private final List<String> primaryPackages;
    private final Path output;
    private final String mongoPlusVersion;
    private final boolean includeGeneratedAt;
    private final boolean pipeline;
    private final List<String> expressionRoots;

    private MongoPlusIndexerConfig(Builder builder) {
        if (builder.sourceRoots.isEmpty()) {
            throw new IllegalArgumentException("至少需要配置一个 sourceRoot");
        }
        if (builder.primaryPackages.isEmpty() && !builder.pipeline) {
            throw new IllegalArgumentException("至少需要配置一个 primaryPackage");
        }
        this.sourceRoots = Collections.unmodifiableList(new ArrayList<Path>(builder.sourceRoots));
        this.primaryPackages = Collections.unmodifiableList(new ArrayList<String>(builder.primaryPackages));
        this.output = builder.output;
        this.mongoPlusVersion = builder.mongoPlusVersion;
        this.includeGeneratedAt = builder.includeGeneratedAt;
        this.pipeline = builder.pipeline;
        this.expressionRoots = Collections.unmodifiableList(
                new ArrayList<String>(new java.util.TreeSet<String>(builder.expressionRoots)));
        if (pipeline && includeGeneratedAt) {
            throw new IllegalArgumentException("Pipeline Index 不允许非确定性的 generatedAt");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 为 MongoPlus 源码工程创建默认配置，并从根 POM 自动读取版本。
     *
     * @param projectRoot MongoPlus 根目录
     * @return 已配置 core/annotation 源码根、conditions 主包和默认输出的构建器
     */
    public static Builder forMongoPlusProject(Path projectRoot) throws IOException {
        Path root = projectRoot.toAbsolutePath().normalize();
        return builder()
                .addSourceRoot(root.resolve("mongo-plus-core/src/main/java"))
                .addSourceRoot(root.resolve("mongo-plus-annotation/src/main/java"))
                .addPrimaryPackage(DEFAULT_PRIMARY_PACKAGE)
                .mongoPlusVersion(readProjectVersion(root.resolve("pom.xml")))
                .output(root.resolve("mongo-plus-indexer/target/generated-resources").resolve(DEFAULT_OUTPUT_FILE));
    }

    private static String readProjectVersion(Path pom) throws IOException {
        if (!Files.isRegularFile(pom)) { throw new IOException("找不到 MongoPlus 根 POM: " + pom); }
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            org.w3c.dom.Document document = factory.newDocumentBuilder().parse(pom.toFile());
            org.w3c.dom.NodeList children = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                org.w3c.dom.Node child = children.item(i);
                if ("version".equals(child.getNodeName())) { return child.getTextContent().trim(); }
            }
            throw new IOException("MongoPlus 根 POM 未声明 project version: " + pom);
        } catch (javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException exception) {
            throw new IOException("读取 MongoPlus 版本失败: " + pom, exception);
        }
    }

    public List<Path> getSourceRoots() { return sourceRoots; }
    public List<String> getPrimaryPackages() { return primaryPackages; }
    public Path getOutput() { return output; }
    public String getMongoPlusVersion() { return mongoPlusVersion; }
    public boolean isIncludeGeneratedAt() { return includeGeneratedAt; }
    public boolean isPipeline() { return pipeline; }
    public List<String> getExpressionRoots() { return expressionRoots; }

    /** 创建聚合专用索引配置，复用项目版本和源码根。 */
    public static Builder forPipelineProject(Path projectRoot) throws IOException {
        Builder builder = forMongoPlusProject(projectRoot).pipeline(true).output(projectRoot.toAbsolutePath().normalize()
                .resolve("mongo-plus-indexer/target/generated-resources").resolve(PIPELINE_OUTPUT_FILE));
        for (String root : PIPELINE_EXPRESSION_ROOTS) { builder.addExpressionRoot(root); }
        return builder;
    }

    /** 配置构建器。 */
    public static final class Builder {
        private final List<Path> sourceRoots = new ArrayList<Path>();
        private final List<String> primaryPackages = new ArrayList<String>();
        private Path output;
        private String mongoPlusVersion;
        private boolean includeGeneratedAt;
        private boolean pipeline;
        private final List<String> expressionRoots = new ArrayList<String>();

        /** 添加精确的表达式 API 类型入口，不按包或标签反向扫描工厂。 */
        public Builder addExpressionRoot(String qualifiedName) {
            if (qualifiedName == null || qualifiedName.trim().isEmpty()) {
                throw new IllegalArgumentException("expressionRoot 不能为空");
            }
            expressionRoots.add(qualifiedName.trim());
            return this;
        }

        public Builder addSourceRoot(Path sourceRoot) {
            if (sourceRoot == null) { throw new IllegalArgumentException("sourceRoot 不能为空"); }
            sourceRoots.add(sourceRoot.toAbsolutePath().normalize());
            return this;
        }

        public Builder addPrimaryPackage(String primaryPackage) {
            if (primaryPackage == null || primaryPackage.trim().isEmpty()) {
                throw new IllegalArgumentException("primaryPackage 不能为空");
            }
            primaryPackages.add(primaryPackage.trim());
            return this;
        }

        public Builder output(Path output) { this.output = output; return this; }
        public Builder mongoPlusVersion(String version) { this.mongoPlusVersion = version; return this; }
        public Builder includeGeneratedAt(boolean include) { this.includeGeneratedAt = include; return this; }
        public Builder pipeline(boolean pipeline) { this.pipeline = pipeline; return this; }
        public MongoPlusIndexerConfig build() { return new MongoPlusIndexerConfig(this); }
    }
}
