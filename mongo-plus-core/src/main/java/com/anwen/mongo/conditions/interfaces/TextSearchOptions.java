package com.anwen.mongo.conditions.interfaces;

import com.anwen.mongo.toolkit.StringUtils;
import com.mongodb.lang.Nullable;

/**
 * 文本检索选项
 *
 * @author anwen
 */
public class TextSearchOptions {

    /**
     * 文本搜索的语言
     * @date 2024/8/26 18:53
     */
    private String language;

    /**
     * 启用或禁用区分大小写
     * @date 2024/8/26 18:53
     */
    private Boolean caseSensitive;

    /**
     * 文本搜索的变音符号敏感标志
     * @date 2024/8/26 18:53
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
