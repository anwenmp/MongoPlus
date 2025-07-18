package com.mongoplus.domain;

import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.SensitiveWordManager;
import com.mongoplus.mapping.FieldInformation;

import java.util.Collection;

/**
 * 敏感词异常类
 */
public class SensitiveWordException extends MongoPlusException {

    private static final Log log = LogFactory.getLog(SensitiveWordManager.class);

    /**
     * 敏感词
     */
    private final Collection<String> sensitiveWords;

    private final FieldInformation fieldInformation;

    /**
     * 字段名
     */
    private final String fieldName;

    public SensitiveWordException(@Nullable FieldInformation fieldInformation,
                                  @Nullable String fieldName,
                                  Collection<String> sensitiveWords) {
        super("The content contains sensitive words: "+sensitiveWords.toString());
        this.fieldInformation = fieldInformation;
        this.fieldName = fieldName;
        this.sensitiveWords = sensitiveWords;
        log.error("The content contains sensitive words: "+sensitiveWords+" field: "+fieldName);
    }

    public SensitiveWordException(@Nullable String fieldName, Collection<String> sensitiveWords) {
        this(null,fieldName,sensitiveWords);
    }

    public SensitiveWordException(Collection<String> sensitiveWords) {
        this(null,null,sensitiveWords);
    }

    /**
     * 返回敏感词
     */
    public Collection<String> getSensitiveWords() {
        return sensitiveWords;
    }

    /**
     * 获取字段名
     */
    @Nullable
    public String getFieldName() {
        return fieldName;
    }

    /**
     * 获取字段信息
     */
    @Nullable
    public FieldInformation getFieldInformation() {
        return fieldInformation;
    }
}
