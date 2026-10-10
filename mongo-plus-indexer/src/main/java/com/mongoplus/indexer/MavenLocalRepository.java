package com.mongoplus.indexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** 只读取 Maven 本地仓库位置，不解析依赖或访问远程仓库。 */
final class MavenLocalRepository {
    private static final Pattern PROPERTY_REFERENCE = Pattern.compile("\\$\\{([^}]+)}");

    private MavenLocalRepository() { }

    /** 显式 JVM 属性优先，用户 settings 覆盖 Maven 安装目录的全局 settings。 */
    static Path resolve() {
        String explicit = nonBlank(System.getProperty("maven.repo.local"));
        if (explicit != null) {
            return Paths.get(explicit).toAbsolutePath().normalize();
        }
        Path userMaven = Paths.get(System.getProperty("user.home"), ".m2");
        Path repository = fromSettings(userMaven.resolve("settings.xml"));
        if (repository != null) {
            return repository;
        }
        String mavenHome = nonBlank(System.getProperty("maven.home"));
        if (mavenHome == null) {
            mavenHome = nonBlank(System.getenv("MAVEN_HOME"));
        }
        if (mavenHome == null) {
            mavenHome = nonBlank(System.getenv("M2_HOME"));
        }
        if (mavenHome != null) {
            repository = fromSettings(Paths.get(mavenHome, "conf", "settings.xml"));
            if (repository != null) {
                return repository;
            }
        }
        return userMaven.resolve("repository").toAbsolutePath().normalize();
    }

    /** 文件不存在或没有非空 localRepository 时返回 null，允许继续使用低优先级配置。 */
    private static Path fromSettings(Path settings) {
        if (!Files.exists(settings)) {
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder parser = factory.newDocumentBuilder();
            // 只向调用方报告配置文件位置，避免解析器把 settings 内容输出到 stderr。
            parser.setErrorHandler(new DefaultHandler() {
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });
            Element root = parser.parse(settings.toFile()).getDocumentElement();
            if (!"settings".equals(root.getLocalName())) {
                throw new IllegalArgumentException("Maven settings 根元素必须为 settings: " + settings
                        + "；可用 -Dmaven.repo.local=<仓库目录> 显式指定");
            }
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && "localRepository".equals(child.getLocalName())) {
                    String value = nonBlank(child.getTextContent());
                    return value == null ? null : interpolate(value, settings);
                }
            }
            return null;
        } catch (IOException | ParserConfigurationException | SAXException exception) {
            throw new IllegalArgumentException("无法读取 Maven 本地仓库配置: " + settings
                    + "；可用 -Dmaven.repo.local=<仓库目录> 显式指定", exception);
        }
    }

    /** settings 仅支持系统属性和 env.* 插值，不使用 profile 内的属性推断路径。 */
    private static Path interpolate(String value, Path settings) {
        Matcher matcher = PROPERTY_REFERENCE.matcher(value);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = key.startsWith("env.") ? System.getenv(key.substring("env.".length()))
                    : System.getProperty(key);
            if (replacement == null) {
                throw unresolved(settings);
            }
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        if (resolved.indexOf("${") >= 0) {
            throw unresolved(settings);
        }
        return Paths.get(resolved.toString()).toAbsolutePath().normalize();
    }

    private static IllegalArgumentException unresolved(Path settings) {
        return new IllegalArgumentException("Maven localRepository 包含无法解析的属性: " + settings
                + "；可用 -Dmaven.repo.local=<仓库目录> 显式指定");
    }

    private static String nonBlank(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
