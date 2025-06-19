package com.mongoplus.domain;

import java.util.Collection;

/**
 * 敏感词异常类
 */
public class SensitiveWordException extends MongoPlusException {

    /**
     * 敏感词
     */
    private Collection<String> sensitiveWords;

    public SensitiveWordException(Collection<String> sensitiveWords) {
        super("The content contains sensitive words: "+sensitiveWords.toString());
    }

    /**
     * 返回敏感词
     */
    public Collection<String> getSensitiveWords() {
        return sensitiveWords;
    }

}
