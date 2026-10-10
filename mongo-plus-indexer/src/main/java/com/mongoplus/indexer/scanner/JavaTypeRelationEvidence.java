package com.mongoplus.indexer.scanner;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

/** 从显式声明的 artifact 读取类型关系；赋值事实不授予语义角色或 codec 等价。 */
final class JavaTypeRelationEvidence {
    static final String TAG = "mongoJavaTypeRelation";
    static final String CAPABILITY = "JAVA_TYPE_RELATIONS_V1";

    private JavaTypeRelationEvidence() { }

    static List<Object> inspect(Path repository, String declaration, List<String> declarations) {
        List<Object> result = new ArrayList<>();
        for (String raw : new java.util.TreeSet<>(declarations)) {
            Map<String, String> attrs = new LinkedHashMap<>();
            for (String token : raw.split("\\s+")) {
                String[] pair = token.split("=", -1);
                if (pair.length != 2 || pair[1].isEmpty() || attrs.putIfAbsent(pair[0], pair[1]) != null) {
                    throw new IllegalArgumentException("非法 @" + TAG + ": " + raw);
                }
            }
            if (!attrs.keySet().equals(java.util.Set.of("artifact", "subtype", "supertype"))) {
                throw new IllegalArgumentException("类型关系必须声明 artifact、subtype、supertype: " + raw);
            }
            result.add(inspect(repository, declaration, raw, attrs));
        }
        return result;
    }

    private static Map<String, Object> inspect(Path repository, String declaration, String raw,
                                                Map<String, String> attrs) {
        String artifact = attrs.get("artifact");
        String[] coordinates = artifact.split(":", -1);
        if (repository == null || coordinates.length != 3 || Arrays.stream(coordinates)
                .anyMatch(part -> !part.matches("[A-Za-z0-9_.-]+") || part.contains(".."))) {
            throw new IllegalArgumentException("类型关系需要显式 repository 和完整 Maven 坐标: " + artifact);
        }
        Path jar = repository.resolve(coordinates[0].replace('.', '/')).resolve(coordinates[1])
                .resolve(coordinates[2]).resolve(coordinates[1] + "-" + coordinates[2] + ".jar");
        try (JarFile archive = new JarFile(jar.toFile());
             URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()},
                     ClassLoader.getPlatformClassLoader())) {
            String identitySource;
            java.util.jar.JarEntry metadata = archive.getJarEntry("META-INF/maven/" + coordinates[0]
                    + "/" + coordinates[1] + "/pom.properties");
            if (metadata != null) {
                Properties identity = new Properties();
                try (InputStream stream = archive.getInputStream(metadata)) { identity.load(stream); }
                if (!coordinates[0].equals(identity.getProperty("groupId"))
                        || !coordinates[1].equals(identity.getProperty("artifactId"))
                        || !coordinates[2].equals(identity.getProperty("version"))) {
                    throw new IllegalArgumentException("类型 artifact 身份不匹配: " + artifact);
                }
                identitySource = "MAVEN_POM_PROPERTIES";
            } else {
                java.util.jar.Manifest manifest = archive.getManifest();
                if (manifest == null || Arrays.asList("Implementation-Version", "Build-Version", "Bundle-Version")
                        .stream().noneMatch(key -> coordinates[2].equals(manifest.getMainAttributes().getValue(key)))) {
                    throw new IllegalArgumentException("类型 artifact 版本身份不匹配: " + artifact);
                }
                identitySource = "JAR_MANIFEST_VERSION";
            }
            Class<?> subtype = Class.forName(attrs.get("subtype"), false, loader);
            Class<?> supertype = Class.forName(attrs.get("supertype"), false, loader);
            if (subtype.getClassLoader() != loader || supertype.getClassLoader() != loader
                    && supertype.getClassLoader() != null
                    || !java.lang.reflect.Modifier.isPublic(subtype.getModifiers())
                    || !java.lang.reflect.Modifier.isPublic(supertype.getModifiers())) {
                throw new IllegalArgumentException("类型必须来自指定 artifact 或 JDK bootstrap: " + raw);
            }
            List<String> interfaces = Arrays.stream(subtype.getInterfaces()).map(Class::getName).sorted().toList();
            Map<String, Object> source = object("source", "ARTIFACT_CLASS_HIERARCHY", "artifact", artifact,
                    "artifactIdentity", identitySource,
                    "artifactSha256", HexFormat.of().withUpperCase().formatHex(
                            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(jar))),
                    "subtypeClassSha256", classHash(archive, subtype),
                    "supertypeClassSha256", supertype.getClassLoader() == loader ? classHash(archive, supertype) : "JDK_BOOTSTRAP",
                    "declaration", declaration, "tag", TAG, "value", raw, "verified", true);
            return object("capability", CAPABILITY, "subtype", subtype.getName(), "supertype", supertype.getName(),
                    "assignable", supertype.isAssignableFrom(subtype), "reverseAssignable", subtype.isAssignableFrom(supertype),
                    "directSuperclass", subtype.getSuperclass() == null ? null : subtype.getSuperclass().getName(),
                    "directInterfaces", interfaces, "proofStatus", "ESTABLISHED", "sourceEvidence", source,
                    "applicability", object("sameBinaryTypeDefinitionsRequired", true,
                            "genericArgumentAssignabilityProven", false, "uncheckedCastsAllowed", false),
                    "semanticRoleImplied", false, "runtimeCodecImplied", false, "bsonEquivalenceImplied", false);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception | LinkageError exception) {
            throw new IllegalArgumentException("无法读取类型 artifact: " + artifact, exception);
        }
    }

    private static String classHash(JarFile archive, Class<?> type) throws Exception {
        try (InputStream stream = archive.getInputStream(archive.getJarEntry(type.getName().replace('.', '/') + ".class"))) {
            return HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes()));
        }
    }

    private static Map<String, Object> object(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) { result.put((String) pairs[i], pairs[i + 1]); }
        return result;
    }
}
