package com.mongoplus.manager;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.mongoplus.enums.SensitiveType;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.property.SensitiveWordProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author anwen
 */
public class SensitiveWordManager {

    private final Log log = LogFactory.getLog(SensitiveWordManager.class);

    /**
     * 敏感词配置
     */
    private final SensitiveWordProperty sensitiveWordProperty;

    /**
     * 敏感词引导类
     */
    private final SensitiveWordBs sensitiveWordBs;

    public SensitiveWordManager(SensitiveWordProperty sensitiveWordProperty) {
        this.sensitiveWordProperty = sensitiveWordProperty;
        this.sensitiveWordBs = SensitiveWordBs.newInstance().init();
    }

    public SensitiveWordBs sensitiveWordBs() {
        return sensitiveWordBs;
    }

    public SensitiveType getSensitiveType() {
        return sensitiveWordProperty.getSensitiveType();
    }

    /**
     * 添加敏感词
     * @param words 敏感词
     * @author anwen
     */
    public void addWordBlacklist(String... words) {
        addWordBlacklist(Arrays.stream(words).collect(Collectors.toList()));
    }

    /**
     * 添加敏感词
     * @param words 敏感词
     * @author anwen
     */
    public void addWordBlacklist(Collection<String> words) {
        sensitiveWordBs.addWord(words);
    }

    /**
     * 添加敏感词
     * @param wordFile 敏感词文件路径
     * @author anwen
     */
    public void addWordBlackList(Path wordFile) {
        try (Stream<String> stream = Files.lines(wordFile)) {
            addWordBlacklist(stream.collect(Collectors.toList()));
        } catch (IOException e) {
            log.error("Abnormal loading of sensitive word blacklist file", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除敏感词
     * @param words 敏感词
     * @author anwen
     */
    public void removeSensitiveWord(String... words) {
        removeSensitiveWord(Arrays.stream(words).collect(Collectors.toList()));
    }

    /**
     * 删除敏感词
     * @param words 敏感词
     * @author anwen
     */
    public void removeSensitiveWord(Collection<String> words) {
        sensitiveWordBs.removeWord(words);
    }

    /**
     * 删除敏感词
     * @param wordFile 敏感词文件路径
     * @author anwen
     */
    public void removeSensitiveWord(Path wordFile) {
        try (Stream<String> stream = Files.lines(wordFile)) {
            removeSensitiveWord(stream.collect(Collectors.toList()));
        } catch (IOException e) {
            log.error("Abnormal loading of sensitive word blacklist file", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加敏感词白名单
     * @param words 敏感词
     * @author anwen
     */
    public void addSensitiveWordAllow(String... words) {
        addSensitiveWordAllow(Arrays.stream(words).collect(Collectors.toList()));
    }

    /**
     * 添加敏感词白名单
     * @param words 敏感词
     * @author anwen
     */
    public void addSensitiveWordAllow(Collection<String> words) {
        sensitiveWordBs.addWordAllow(words);
    }

    /**
     * 添加敏感词白名单
     * @param wordFile 敏感词文件路径
     * @author anwen
     */
    public void addSensitiveWordAllow(Path wordFile) {
        try (Stream<String> stream = Files.lines(wordFile)) {
            addSensitiveWordAllow(stream.collect(Collectors.toList()));
        } catch (IOException e) {
            log.error("Abnormal loading of sensitive word blacklist file", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除敏感词白名单
     * @param words 敏感词
     * @author anwen
     */
    public void removeSensitiveWordAllow(String... words) {
        removeSensitiveWordAllow(Arrays.stream(words).collect(Collectors.toList()));
    }

    /**
     * 删除敏感词白名单
     * @param words 敏感词
     * @author anwen
     */
    public void removeSensitiveWordAllow(Collection<String> words) {
        sensitiveWordBs.removeWordAllow(words);
    }

    /**
     * 删除敏感词白名单
     * @param wordFile 敏感词文件路径
     * @author anwen
     */
    public void removeSensitiveWordAllow(Path wordFile) {
        try (Stream<String> stream = Files.lines(wordFile)) {
            removeSensitiveWordAllow(stream.collect(Collectors.toList()));
        } catch (IOException e) {
            log.error("Abnormal loading of sensitive word blacklist file", e);
            throw new RuntimeException(e);
        }
    }

}
