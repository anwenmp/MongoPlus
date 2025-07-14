package com.mongoplus.meta;

import com.mongodb.client.MongoClient;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.toolkit.StringUtils;

import java.io.File;
import java.net.URI;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * MongoPlus 框架版本信息类
 * <p>用于获取当前 MongoPlus SDK 的版本、声明支持的 MongoDriver 版本、
 * 以及实际运行时 MongoDriver 的版本信息</p>
 */
public final class MongoPlusVersion {

    private MongoPlusVersion() {}

    /**
     * 返回当前 MongoPlus 的完整版本字符串
     * @return MongoPlus版本
     */
    public static String getVersion() {
        return "2.1.9";
    }

    /**
     * 返回当前 MongoPlus 的使用的 MongoDriver 完整版本字符串
     * @return MongoDriver版本
     */
    public static String getMongoDriverVersion() {
        return "5.4.0";
    }

    /**
     * 获取实际使用的 MongoDriver 版本
     * @return MongoDriver版本
     */
    @Nullable
    public static String getActualUseMongoDriverVersion() {
        try {
            URI jarUri = MongoClient.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarFile = new File(jarUri);
            try (JarFile jar = new JarFile(jarFile)) {
                Manifest manifest = jar.getManifest();
                Attributes attrs = manifest.getMainAttributes();
                String version = attrs.getValue("Bundle-Version");
                if (version != null) {
                    return version;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * 判断当前使用的 MongoDriver 是否小于指定版本
     * @param compareVersion 比较版本
     * @return 是否小于
     */
    public static boolean isMongoDriverVersionBefore(String compareVersion) {
        String actual = getActualUseMongoDriverVersion();
        if (StringUtils.isBlank(actual)) return false;
        return StringUtils.compareVersion(actual,compareVersion) < 0;
    }

}
