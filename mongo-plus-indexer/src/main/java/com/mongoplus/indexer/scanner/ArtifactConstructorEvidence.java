package com.mongoplus.indexer.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/** 从声明指定的真实 Maven artifact 校验公开 entry 构造器，不用类名推断语义。 */
final class ArtifactConstructorEvidence {
    private ArtifactConstructorEvidence() { }

    static Map<String, Object> inspect(Path repository, String artifact, String className,
                                       int keyIndex, int valueIndex) {
        String[] coordinates = artifact.split(":", -1);
        if (coordinates.length != 3 || Arrays.stream(coordinates)
                .anyMatch(part -> !part.matches("[A-Za-z0-9_.-]+") || part.contains(".."))) {
            throw new IllegalArgumentException("construction artifact 必须为完整 group:artifact:version");
        }
        Path jar = repository.resolve(coordinates[0].replace('.', '/')).resolve(coordinates[1])
                .resolve(coordinates[2]).resolve(coordinates[1] + "-" + coordinates[2] + ".jar");
        if (!Files.isRegularFile(jar)) {
            throw new IllegalArgumentException("缺少 construction artifact: " + artifact + "，位置: " + jar);
        }
        try (JarFile archive = new JarFile(jar.toFile());
             URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()},
                     ClassLoader.getPlatformClassLoader())) {
            String metadata = "META-INF/maven/" + coordinates[0] + "/" + coordinates[1] + "/pom.properties";
            JarEntry entry = archive.getJarEntry(metadata);
            String identitySource;
            if (entry != null) {
                Properties identity = new Properties();
                try (InputStream stream = archive.getInputStream(entry)) { identity.load(stream); }
                if (!coordinates[0].equals(identity.getProperty("groupId"))
                        || !coordinates[1].equals(identity.getProperty("artifactId"))
                        || !coordinates[2].equals(identity.getProperty("version"))) {
                    throw new IllegalArgumentException("construction artifact 身份不匹配: " + artifact);
                }
                identitySource = "MAVEN_POM_PROPERTIES";
            } else {
                java.util.jar.Manifest manifest = archive.getManifest();
                if (manifest == null || Arrays.asList("Implementation-Version", "Build-Version", "Bundle-Version")
                        .stream().noneMatch(key -> coordinates[2].equals(manifest.getMainAttributes().getValue(key)))) {
                    throw new IllegalArgumentException("construction artifact 缺少匹配的版本身份: " + artifact);
                }
                identitySource = "JAR_MANIFEST_VERSION";
            }
            Class<?> type = Class.forName(className, false, loader);
            if (type.getClassLoader() != loader || !java.lang.reflect.Modifier.isPublic(type.getModifiers())
                    || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
                throw new IllegalArgumentException("entry 类型必须为 artifact 自身的公开具体类型: " + className);
            }
            TypeVariable<?>[] variables = type.getTypeParameters();
            if (variables.length != 1 || !Arrays.equals(variables[0].getBounds(), new Type[]{Object.class})) {
                throw new IllegalArgumentException("entry 当前仅支持一个 Object 上界的显式类型参数: " + className);
            }
            List<Constructor<?>> candidates = Arrays.stream(type.getConstructors()).filter(constructor -> {
                Type[] parameters = constructor.getGenericParameterTypes();
                return !constructor.isVarArgs() && constructor.getTypeParameters().length == 0
                        && parameters.length == 2 && String.class.equals(parameters[keyIndex])
                        && variables[0].equals(parameters[valueIndex]);
            }).collect(Collectors.toList());
            if (candidates.size() != 1) {
                throw new IllegalArgumentException("entry 缺少唯一公开 (String, 类型参数) 构造器: " + className);
            }
            Constructor<?> constructor = candidates.get(0);
            List<String> parameters = Arrays.stream(constructor.getGenericParameterTypes())
                    .map(Type::getTypeName).collect(Collectors.toList());
            String signature = type.getSimpleName() + "(" + String.join(", ", parameters) + ")";
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("declaredIn", className);
            result.put("name", type.getSimpleName());
            result.put("signature", signature);
            result.put("apiRef", className + "#" + signature);
            result.put("visibility", "public");
            result.put("typeParameters", Arrays.asList(variables[0].getName()));
            result.put("typeParameterBounds", Arrays.asList(Object.class.getName()));
            result.put("parameterTypes", parameters);
            Map<String, Object> source = new LinkedHashMap<String, Object>();
            source.put("source", "ARTIFACT_GENERIC_SIGNATURE");
            source.put("artifact", artifact);
            source.put("symbols", className + "#" + signature);
            source.put("artifactIdentity", identitySource);
            source.put("verified", true);
            result.put("sourceEvidence", source);
            return result;
        } catch (IOException | ReflectiveOperationException | LinkageError exception) {
            throw new IllegalArgumentException("无法校验 construction artifact: " + artifact, exception);
        }
    }
}
