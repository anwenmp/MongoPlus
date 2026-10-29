package com.mongoplus.property;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import com.mongoplus.enums.SensitiveType;
import com.mongoplus.handler.LoadExtraWord;
import com.mongoplus.handler.SensitiveWordFieldHandler;
import com.mongoplus.handlers.FieldHandlerChain;
import com.mongoplus.interceptor.InterceptorChain;
import com.mongoplus.interceptor.SensitiveWordInterceptor;

/**
 * 敏感词运行时适配及兼容入口；配置字段统一由 core 管理。
 *
 * @author anwen
 */
public class SensitiveWordProperty extends SensitiveWordConfigurationProperty {

    private LoadExtraWord loadExtraWord = new LoadExtraWord() {};

    public SensitiveWordBs sensitiveWordBs() {
        return sensitiveWordBs(this, loadExtraWord);
    }

    /**
     * 使用同一个 core 配置对象构造词库，不复制绑定属性。
     */
    public static SensitiveWordBs sensitiveWordBs(SensitiveWordConfigurationProperty property,
                                                  LoadExtraWord loadExtraWord) {
        return SensitiveWordBs.newInstance()
                .ignoreCase(property.isIgnoreCase())
                .ignoreWidth(property.isIgnoreWidth())
                .ignoreNumStyle(property.isIgnoreNumStyle())
                .ignoreChineseStyle(property.isIgnoreChineseStyle())
                .ignoreEnglishStyle(property.isIgnoreEnglishStyle())
                .ignoreRepeat(property.isIgnoreRepeat())
                .charIgnore(SensitiveWordCharIgnores.specialChars())
                .enableNumCheck(property.isEnableNumCheck())
                .enableEmailCheck(property.isEnableEmailCheck())
                .enableUrlCheck(property.isEnableUrlCheck())
                .enableIpv4Check(property.isEnableIpv4Check())
                .wordDeny(WordDenys.chains(WordDenys.defaults(), loadExtraWord))
                .wordAllow(WordAllows.chains(WordAllows.defaults(), loadExtraWord))
                .init();
    }

    public void setDynamicLoadWord(LoadExtraWord loadExtraWord) {
        this.loadExtraWord = loadExtraWord;
    }

    /**
     * 初始化敏感词校验类型
     */
    public void initSensitiveType() {
        initSensitiveType(this);
    }

    /**
     * 根据 core 配置注册原有处理器。
     */
    public static void initSensitiveType(SensitiveWordConfigurationProperty property) {
        if (property.getSensitiveType() == SensitiveType.GLOBAL) {
            // 如果是全局，需要注册拦截器
            InterceptorChain.addInterceptor(new SensitiveWordInterceptor());
        } else if (property.getSensitiveType() == SensitiveType.LOCAL) {
            // 如果是局部，需要注册处理器
            FieldHandlerChain.getInstance().register(new SensitiveWordFieldHandler());
        }
    }

}
