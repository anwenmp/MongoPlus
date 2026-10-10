package com.mongoplus.indexer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Maven 本地仓库优先级、配置插值和 XML 安全边界的独立可执行回归。 */
public final class MavenLocalRepositorySelfTest {
    private static final String REPOSITORY_PROPERTY = "maven.repo.local";
    private static final String MAVEN_HOME_PROPERTY = "maven.home";
    private static final String USER_HOME_PROPERTY = "user.home";
    private static final String INTERPOLATION_PROPERTY = "mongoplus.indexer.test.repository";
    private static final String ENVIRONMENT_VARIABLE = "MONGOPLUS_INDEXER_TEST_REPOSITORY";
    private static final String UNKNOWN_VARIABLE = "MONGOPLUS_INDEXER_TEST_UNDEFINED_7C590A4E";
    private static final String ENVIRONMENT_PROBE = "--environment-probe";

    private MavenLocalRepositorySelfTest() {}

    public static void main(String[] args) throws Exception {
        if (args.length == 4 && ENVIRONMENT_PROBE.equals(args[0])) {
            System.clearProperty(REPOSITORY_PROPERTY);
            System.setProperty(USER_HOME_PROPERTY, args[1]);
            System.setProperty(MAVEN_HOME_PROPERTY, args[2]);
            assertRepository(Paths.get(args[3]), MavenLocalRepository.resolve(), "环境变量插值");
            return;
        }
        if (args.length != 0) {
            throw new IllegalArgumentException("用法: MavenLocalRepositorySelfTest");
        }
        int cases = 0;
        run("user settings overrides global settings", MavenLocalRepositorySelfTest::userSettingsWins);
        cases++;
        run("global settings selects configured repository", MavenLocalRepositorySelfTest::globalSettings);
        cases++;
        run("explicit JVM property bypasses malformed settings", MavenLocalRepositorySelfTest::explicitProperty);
        cases++;
        run("explicit builder bypasses malformed settings", MavenLocalRepositorySelfTest::explicitBuilder);
        cases++;
        run("explicit builder overrides JVM property", MavenLocalRepositorySelfTest::builderWinsProperty);
        cases++;
        run("ordinary index ignores malformed Maven settings", MavenLocalRepositorySelfTest::ordinaryIndex);
        cases++;
        run("builder resolves defaults when build is called", MavenLocalRepositorySelfTest::resolveAtBuild);
        cases++;
        run("missing settings uses default repository", MavenLocalRepositorySelfTest::defaultRepository);
        cases++;
        run("empty user repository falls back to global", MavenLocalRepositorySelfTest::emptyUserRepository);
        cases++;
        run("empty repositories fall back to default", MavenLocalRepositorySelfTest::emptyRepositories);
        cases++;
        run("user.home interpolation", MavenLocalRepositorySelfTest::userHomeInterpolation);
        cases++;
        run("custom system property interpolation", MavenLocalRepositorySelfTest::propertyInterpolation);
        cases++;
        run("environment variable interpolation", MavenLocalRepositorySelfTest::environmentInterpolation);
        cases++;
        run("namespaced settings", MavenLocalRepositorySelfTest::namespacedSettings);
        cases++;
        run("nested repository is ignored", MavenLocalRepositorySelfTest::nestedRepository);
        cases++;
        run("unknown system property is rejected", MavenLocalRepositorySelfTest::unknownProperty);
        cases++;
        run("unknown environment variable is rejected", MavenLocalRepositorySelfTest::unknownEnvironment);
        cases++;
        run("malformed XML is rejected", MavenLocalRepositorySelfTest::malformedXml);
        cases++;
        run("DOCTYPE and external entity are rejected", MavenLocalRepositorySelfTest::externalEntity);
        cases++;
        System.out.println("MavenLocalRepositorySelfTest PASSED: " + cases + " cases");
    }

    private static void userSettingsWins(Fixture fixture) throws Exception {
        Path expected = fixture.directory.resolve("user-repository");
        fixture.writeUserSettings(settings(expected.toString()));
        fixture.writeGlobalSettings("<settings>");
        assertRepository(expected, MavenLocalRepository.resolve(), "用户配置必须先于全局配置");
    }

    private static void globalSettings(Fixture fixture) throws Exception {
        Path expected = fixture.directory.resolve("configured-repository");
        fixture.writeGlobalSettings(settings(expected.toString()));
        assertRepository(expected, MavenLocalRepository.resolve(), "须读取 Maven home 下的 conf/settings.xml");
        assertRepository(expected, fixture.builder().build().getConstructionArtifactRepository(),
                "默认 Indexer 配置必须使用 Maven 已配置的仓库");
    }

    private static void explicitProperty(Fixture fixture) throws Exception {
        Path expected = fixture.directory.resolve("explicit-repository");
        fixture.writeUserSettings("<settings>");
        fixture.writeGlobalSettings("<settings>");
        System.setProperty(REPOSITORY_PROPERTY, expected.toString());
        assertRepository(expected, MavenLocalRepository.resolve(), "显式 JVM 属性必须跳过配置解析");
    }

    private static void explicitBuilder(Fixture fixture) throws Exception {
        Path expected = fixture.directory.resolve("builder-repository");
        fixture.writeUserSettings("<settings>");
        fixture.writeGlobalSettings("<settings>");
        assertRepository(expected, fixture.builder().constructionArtifactRepository(expected)
                .build().getConstructionArtifactRepository(), "显式 builder 必须跳过默认仓库解析");
    }

    private static void builderWinsProperty(Fixture fixture) throws Exception {
        Path expected = fixture.directory.resolve("builder-repository");
        System.setProperty(REPOSITORY_PROPERTY, fixture.directory.resolve("jvm-repository").toString());
        assertRepository(expected, fixture.builder().constructionArtifactRepository(expected)
                .build().getConstructionArtifactRepository(), "builder 必须先于 JVM 属性");
    }

    private static void resolveAtBuild(Fixture fixture) throws Exception {
        MongoPlusIndexerConfig.Builder builder = fixture.builder();
        Path expected = fixture.directory.resolve("late-repository");
        System.setProperty(REPOSITORY_PROPERTY, expected.toString());
        assertRepository(expected, builder.build().getConstructionArtifactRepository(),
                "创建 builder 后改变显式属性，build 必须读取当前值");
    }

    private static void ordinaryIndex(Fixture fixture) throws Exception {
        fixture.writeUserSettings("<settings>");
        fixture.writeGlobalSettings("<settings>");
        MongoPlusIndexerConfig config = MongoPlusIndexerConfig.builder()
                .addSourceRoot(fixture.directory.resolve("src")).addPrimaryPackage("example")
                .mongoPlusVersion("test").build();
        require(!config.isPipeline(), "普通 Index 夹具不能启用 Pipeline");
        assertRepository(fixture.userHome.resolve(".m2/repository"), config.getConstructionArtifactRepository(),
                "普通 Index 不使用 construction artifact，不能新增 Maven settings 解析要求");
    }

    private static void defaultRepository(Fixture fixture) {
        assertRepository(fixture.userHome.resolve(".m2/repository"), MavenLocalRepository.resolve(),
                "无配置时须保留用户 .m2/repository 默认值");
    }

    private static void emptyUserRepository(Fixture fixture) throws Exception {
        Path expected = fixture.directory.resolve("global-repository");
        fixture.writeUserSettings(settings("  \n  "));
        fixture.writeGlobalSettings(settings(expected.toString()));
        assertRepository(expected, MavenLocalRepository.resolve(), "空用户配置必须继续读取全局配置");
    }

    private static void emptyRepositories(Fixture fixture) throws Exception {
        fixture.writeUserSettings(settings(""));
        fixture.writeGlobalSettings("<settings/>");
        defaultRepository(fixture);
    }

    private static void userHomeInterpolation(Fixture fixture) throws Exception {
        fixture.writeUserSettings(settings("${user.home}/custom-repository"));
        assertRepository(fixture.userHome.resolve("custom-repository"), MavenLocalRepository.resolve(),
                "必须展开 user.home");
    }

    private static void propertyInterpolation(Fixture fixture) throws Exception {
        Path expected = fixture.directory.resolve("property-repository");
        System.setProperty(INTERPOLATION_PROPERTY, expected.toString());
        fixture.writeGlobalSettings(settings("${" + INTERPOLATION_PROPERTY + "}/subdirectory"));
        assertRepository(expected.resolve("subdirectory"), MavenLocalRepository.resolve(), "必须展开系统属性");
    }

    private static void environmentInterpolation(Fixture fixture) throws Exception {
        Path expected = fixture.directory.resolve("environment-repository");
        fixture.writeUserSettings(settings("${env." + ENVIRONMENT_VARIABLE + "}/subdirectory"));
        Path javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java.exe");
        if (!Files.isRegularFile(javaExecutable)) {
            javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java");
        }
        Path output = fixture.directory.resolve("environment-probe.log");
        ProcessBuilder processBuilder = new ProcessBuilder(javaExecutable.toString(), "-cp",
                System.getProperty("java.class.path"), MavenLocalRepositorySelfTest.class.getName(),
                ENVIRONMENT_PROBE, fixture.userHome.toString(), fixture.mavenHome.toString(),
                expected.resolve("subdirectory").toString());
        // 子进程显式设置唯一变量，既覆盖 env 插值又不依赖开发者已有环境。
        processBuilder.environment().put(ENVIRONMENT_VARIABLE, expected.toString());
        processBuilder.redirectErrorStream(true).redirectOutput(output.toFile());
        Process process = processBuilder.start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new AssertionError("环境变量插值子进程超时");
        }
        require(process.exitValue() == 0, "环境变量插值失败: "
                + new String(Files.readAllBytes(output), StandardCharsets.UTF_8));
    }

    private static void namespacedSettings(Fixture fixture) throws Exception {
        Path expected = fixture.directory.resolve("namespaced-repository");
        fixture.writeUserSettings("<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\">"
                + "<localRepository>" + xml(expected.toString()) + "</localRepository></settings>");
        assertRepository(expected, MavenLocalRepository.resolve(), "Maven settings 默认命名空间必须兼容");
    }

    private static void nestedRepository(Fixture fixture) throws Exception {
        Path expected = fixture.directory.resolve("global-repository");
        fixture.writeUserSettings("<settings><profiles><profile><localRepository>"
                + xml(fixture.directory.resolve("nested-repository").toString())
                + "</localRepository></profile></profiles></settings>");
        fixture.writeGlobalSettings(settings(expected.toString()));
        assertRepository(expected, MavenLocalRepository.resolve(), "只读取顶层 localRepository");
    }

    private static void unknownProperty(Fixture fixture) throws Exception {
        String property = "mongoplus.indexer.undefined.7c590a4e";
        require(System.getProperty(property) == null, "未知属性测试夹具被外部环境占用");
        fixture.writeUserSettings(settings("${" + property + "}/repository"));
        assertRejected(fixture.userSettings(), "未解析系统属性");
    }

    private static void unknownEnvironment(Fixture fixture) throws Exception {
        require(System.getenv(UNKNOWN_VARIABLE) == null, "未知环境变量测试夹具被外部环境占用");
        fixture.writeUserSettings(settings("${env." + UNKNOWN_VARIABLE + "}/repository"));
        assertRejected(fixture.userSettings(), "未解析环境变量");
    }

    private static void malformedXml(Fixture fixture) throws Exception {
        fixture.writeUserSettings("<settings><localRepository>broken</settings>");
        assertRejected(fixture.userSettings(), "损坏 XML");
    }

    private static void externalEntity(Fixture fixture) throws Exception {
        Path entity = fixture.directory.resolve("external-entity.txt");
        Files.write(entity, "EXTERNAL_ENTITY_MUST_NOT_BE_READ".getBytes(StandardCharsets.UTF_8));
        fixture.writeUserSettings("<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE settings [<!ENTITY repo SYSTEM \"" + entity.toUri() + "\">]>\n"
                + "<settings><localRepository>&repo;</localRepository></settings>");
        assertRejected(fixture.userSettings(), "DTD/外部实体");
    }

    private static void assertRejected(Path settingsFile, String scenario) throws Exception {
        try {
            MavenLocalRepository.resolve();
        } catch (Exception exception) {
            String message = String.valueOf(exception.getMessage());
            require(message.contains("-Dmaven.repo.local"), scenario + " 缺少显式仓库修复提示: " + message);
            require(message.contains(settingsFile.getFileName().toString()), scenario + " 缺少配置文件位置");
            require(!message.contains("EXTERNAL_ENTITY_MUST_NOT_BE_READ"), "禁止在错误中泄露外部实体内容");
            return;
        }
        throw new AssertionError(scenario + " 必须拒绝，不能静默回退或保留未解析变量");
    }

    private static void run(String scenario, TestCase testCase) throws Exception {
        try (Fixture fixture = new Fixture()) {
            testCase.run(fixture);
        } catch (Exception exception) {
            throw new AssertionError("仓库解析回归失败: " + scenario, exception);
        }
    }

    private static String settings(String repository) {
        return "<settings><localRepository>" + xml(repository) + "</localRepository></settings>";
    }

    private static String xml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void assertRepository(Path expected, Path actual, String message) {
        require(expected.toAbsolutePath().normalize().equals(actual),
                message + ", expected=" + expected.toAbsolutePath().normalize() + ", actual=" + actual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface TestCase {
        void run(Fixture fixture) throws Exception;
    }

    /** 每个场景固定 Maven home，屏蔽 MAVEN_HOME/M2_HOME，并恢复全部被修改的系统属性。 */
    private static final class Fixture implements AutoCloseable {
        private final Path directory = Files.createTempDirectory("mongo-plus-maven-repository-");
        private final Path userHome = directory.resolve("user-home");
        private final Path mavenHome = directory.resolve("maven-home");
        private final Map<String, String> originalProperties = new LinkedHashMap<String, String>();

        private Fixture() throws IOException {
            Files.createDirectories(userHome);
            Files.createDirectories(mavenHome);
            for (String property : new String[] {REPOSITORY_PROPERTY, MAVEN_HOME_PROPERTY,
                    USER_HOME_PROPERTY, INTERPOLATION_PROPERTY}) {
                originalProperties.put(property, System.getProperty(property));
            }
            System.clearProperty(REPOSITORY_PROPERTY);
            System.clearProperty(INTERPOLATION_PROPERTY);
            System.setProperty(USER_HOME_PROPERTY, userHome.toString());
            System.setProperty(MAVEN_HOME_PROPERTY, mavenHome.toString());
        }

        private MongoPlusIndexerConfig.Builder builder() {
            return MongoPlusIndexerConfig.builder().addSourceRoot(directory.resolve("src"))
                    .addPrimaryPackage("example").mongoPlusVersion("test").pipeline(true);
        }

        private Path userSettings() {
            return userHome.resolve(".m2/settings.xml");
        }

        private void writeUserSettings(String content) throws IOException {
            write(userSettings(), content);
        }

        private void writeGlobalSettings(String content) throws IOException {
            write(mavenHome.resolve("conf/settings.xml"), content);
        }

        private static void write(Path path, String content) throws IOException {
            Files.createDirectories(path.getParent());
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void close() throws IOException {
            for (Map.Entry<String, String> property : originalProperties.entrySet()) {
                if (property.getValue() == null) {
                    System.clearProperty(property.getKey());
                } else {
                    System.setProperty(property.getKey(), property.getValue());
                }
            }
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
                    if (exception != null) {
                        throw exception;
                    }
                    Files.delete(path);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
