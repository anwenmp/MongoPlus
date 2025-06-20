package com.mongoplus.property;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.enums.SensitiveType;
import com.mongoplus.handler.DynamicLoadWord;
import com.mongoplus.handler.SensitiveWordFieldHandler;
import com.mongoplus.interceptor.InterceptorChain;
import com.mongoplus.interceptor.SensitiveWordInterceptor;

/**
 * 敏感词属性
 *
 * @author anwen
 */
public class SensitiveWordProperty {

    /**
     * 敏感词校验类型,默认为全局
     */
    private SensitiveType sensitiveType = SensitiveType.GLOBAL;

    /**
     * 是否忽略大小写
     */
    private boolean ignoreCase = true;

    /**
     * 是否忽略全角、半角
     */
    private boolean ignoreWidth = true;

    /**
     * 是否忽略数字样式
     */
    private boolean ignoreNumStyle = true;

    /**
     * 是否忽略中文样式
     */
    private boolean ignoreChineseStyle = true;

    /**
     * 是否忽略英文样式
     */
    private boolean ignoreEnglishStyle = true;

    /**
     * 是否忽略重复
     */
    private boolean ignoreRepeat = false;

    /**
     * 是否忽略特殊字符,如'傻!@#$帽',将会忽略掉其中的特殊字符
     */
    private boolean ignoreChar = false;

    // 开启校验
    /**
     * 启用连续数字检测
     */
    private boolean enableNumCheck = false;

    /**
     * 启用邮箱检测
     * 邮箱等个人信息
     */
    private boolean enableEmailCheck = false;

    /**
     * 启用 URL 检测
     * 用于过滤常见的网址信息
     */
    private boolean enableUrlCheck = false;

    /**
     * 是否启用 ipv4 校验
     * 避免用户通过 ip 绕过网址检测等
     */
    private boolean enableIpv4Check = false;

    // ******** 不可配置项 ********* //
    private DynamicLoadWord dynamicLoadWord = new DynamicLoadWord() {};

    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                .ignoreCase(ignoreCase)
                .ignoreWidth(ignoreWidth)
                .ignoreNumStyle(ignoreNumStyle)
                .ignoreChineseStyle(ignoreChineseStyle)
                .ignoreEnglishStyle(ignoreEnglishStyle)
                .ignoreRepeat(ignoreRepeat)
                .charIgnore(SensitiveWordCharIgnores.specialChars())
                .enableNumCheck(enableNumCheck)
                .enableEmailCheck(enableEmailCheck)
                .enableUrlCheck(enableUrlCheck)
                .enableIpv4Check(enableIpv4Check)
                .wordDeny(dynamicLoadWord)
                .wordAllow(dynamicLoadWord)
                .init();
    }

    /**
     * 设置敏感词校验类型
     * @param sensitiveType 类型
     */
    public void setSensitiveType(SensitiveType sensitiveType) {
        this.sensitiveType = sensitiveType;
    }

    public void setDynamicLoadWord(DynamicLoadWord dynamicLoadWord) {
        this.dynamicLoadWord = dynamicLoadWord;
    }

    /**
     * 初始化敏感词校验类型
     */
    public void initSensitiveType() {
        if (sensitiveType == SensitiveType.GLOBAL) {
            // 如果是全局，需要注册拦截器
            InterceptorChain.addInterceptor(new SensitiveWordInterceptor());
        } else if (sensitiveType == SensitiveType.LOCAL) {
            // 如果是局部，需要注册处理器
            HandlerCache.fieldHandlers.add(new SensitiveWordFieldHandler());
        }
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean isIgnoreWidth() {
        return ignoreWidth;
    }

    public boolean isIgnoreChar() {
        return ignoreChar;
    }

    public void setIgnoreChar(boolean ignoreChar) {
        this.ignoreChar = ignoreChar;
    }

    public void setIgnoreWidth(boolean ignoreWidth) {
        this.ignoreWidth = ignoreWidth;
    }

    public boolean isIgnoreNumStyle() {
        return ignoreNumStyle;
    }

    public void setIgnoreNumStyle(boolean ignoreNumStyle) {
        this.ignoreNumStyle = ignoreNumStyle;
    }

    public boolean isIgnoreChineseStyle() {
        return ignoreChineseStyle;
    }

    public void setIgnoreChineseStyle(boolean ignoreChineseStyle) {
        this.ignoreChineseStyle = ignoreChineseStyle;
    }

    public boolean isIgnoreEnglishStyle() {
        return ignoreEnglishStyle;
    }

    public void setIgnoreEnglishStyle(boolean ignoreEnglishStyle) {
        this.ignoreEnglishStyle = ignoreEnglishStyle;
    }

    public boolean isIgnoreRepeat() {
        return ignoreRepeat;
    }

    public void setIgnoreRepeat(boolean ignoreRepeat) {
        this.ignoreRepeat = ignoreRepeat;
    }

    public boolean isEnableNumCheck() {
        return enableNumCheck;
    }

    public void setEnableNumCheck(boolean enableNumCheck) {
        this.enableNumCheck = enableNumCheck;
    }

    public boolean isEnableEmailCheck() {
        return enableEmailCheck;
    }

    public void setEnableEmailCheck(boolean enableEmailCheck) {
        this.enableEmailCheck = enableEmailCheck;
    }

    public boolean isEnableUrlCheck() {
        return enableUrlCheck;
    }

    public void setEnableUrlCheck(boolean enableUrlCheck) {
        this.enableUrlCheck = enableUrlCheck;
    }

    public boolean isEnableIpv4Check() {
        return enableIpv4Check;
    }

    public void setEnableIpv4Check(boolean enableIpv4Check) {
        this.enableIpv4Check = enableIpv4Check;
    }

    public SensitiveType getSensitiveType() {
        return sensitiveType;
    }
}
