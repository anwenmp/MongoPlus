package com.mongoplus.scanner;

import com.mongoplus.scanner.meta.MetadataReader;
import com.mongoplus.scanner.meta.MetadataReaderFactory;
import com.mongoplus.toolkit.CollUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 包扫描
 *
 * @author anwen
 */
public class PackageScanner {

    /**
     * 扫描过滤器
     */
    private final List<ScannerFilter> scannerFilters = new ArrayList<>();

    /**
     * 添加过滤器
     * @param scannerFilter 过滤器
     * @author anwen
     */
    public void addScannerFilter(ScannerFilter scannerFilter) {
        scannerFilters.add(scannerFilter);
    }

    public List<Class<?>> scanPackage(String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(packagePath);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File file = new File(resource.getFile());
            scanClasses(file, packageName, classes);
        }
        return classes;
    }

    private void scanClasses(File directory, String packageName, List<Class<?>> classes) throws ClassNotFoundException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        String subPackageName = packageName + "." + file.getName();
                        scanClasses(file, subPackageName, classes);
                    } else if (file.getName().endsWith(".class")) {
                        String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(className);
                        if (isCandidateComponent(clazz)) {
                            classes.add(clazz);
                        }
                    }
                }
            }
        }
    }

    /**
     * 和筛选器是否匹配
     * @param clazz class
     * @return {@link boolean}
     * @author anwen
     */
    boolean isCandidateComponent(Class<?> clazz) {
        if (CollUtil.isNotEmpty(scannerFilters)) {
            MetadataReader metadataReader = new MetadataReaderFactory(clazz).getMetadataReader();
            for (ScannerFilter scannerFilter : scannerFilters) {
                boolean match = scannerFilter.match(metadataReader);
                if (!match) {
                    return false;
                }
            }
        }
        return true;
    }

}
