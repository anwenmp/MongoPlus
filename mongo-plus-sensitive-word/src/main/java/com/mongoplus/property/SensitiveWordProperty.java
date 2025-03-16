package com.mongoplus.property;

import com.mongoplus.enums.SensitiveType;

/**
 * 敏感词属性
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


    /**
     * 检测数字时的长度
     */
    private int numCheckLen = 8;

    public SensitiveType getSensitiveType() {
        return sensitiveType;
    }
}
