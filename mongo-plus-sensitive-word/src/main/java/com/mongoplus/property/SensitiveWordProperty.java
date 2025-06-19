package com.mongoplus.property;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.mongoplus.enums.ResultHandler;
import com.mongoplus.enums.SensitiveType;

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
     * 敏感词处理，默认抛出异常
     * <p>暂只支持抛出异常</p>
     */
    private ResultHandler resultHandler = ResultHandler.REJECT;

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

    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                .ignoreCase(ignoreCase)
                .ignoreWidth(ignoreWidth)
                .ignoreNumStyle(ignoreNumStyle)
                .ignoreChineseStyle(ignoreChineseStyle)
                .ignoreEnglishStyle(ignoreEnglishStyle)
                .ignoreRepeat(ignoreRepeat)
                .enableNumCheck(enableNumCheck)
                .enableEmailCheck(enableEmailCheck)
                .enableUrlCheck(enableUrlCheck)
                .enableIpv4Check(enableIpv4Check)
                .init();
    }

    public ResultHandler getResultHandler() {
        return resultHandler;
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    public void setSensitiveType(SensitiveType sensitiveType) {
        this.sensitiveType = sensitiveType;
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
