package com.mongoplus.conditions.interfaces;

import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.toolkit.StringUtils;

/**
 * 文本检索选项
 *
 * @author anwen
 */
public class TextSearchOptions {

    /**
     * 文本搜索的语言
     */
    private String language;

    /**
     * 启用或禁用区分大小写
     */
    private Boolean caseSensitive;

    /**
     * 文本搜索的变音符号敏感标志
     */
    private Boolean diacriticSensitive;

    @Nullable
    public String getLanguage() {
        return language;
    }

    public TextSearchOptions language(@Nullable final String language) {
        this.language = language;
        return this;
    }

    @Nullable
    public Boolean getCaseSensitive() {
        return caseSensitive;
    }

    public TextSearchOptions caseSensitive(@Nullable final Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    @Nullable
    public Boolean getDiacriticSensitive() {
        return diacriticSensitive;
    }

    public TextSearchOptions diacriticSensitive(@Nullable final Boolean diacriticSensitive) {
        this.diacriticSensitive = diacriticSensitive;
        return this;
    }

    public com.mongodb.client.model.TextSearchOptions to(){
        com.mongodb.client.model.TextSearchOptions textSearchOptions =
                new com.mongodb.client.model.TextSearchOptions();
        if (StringUtils.isNotBlank(language)){
            textSearchOptions.language(language);
        }
        if (caseSensitive != null){
            textSearchOptions.caseSensitive(caseSensitive);
        }
        if (diacriticSensitive != null){
            textSearchOptions.diacriticSensitive(diacriticSensitive);
        }
        return textSearchOptions;
    }

}
