package com.mongoplus.enums;

/**
 * 模式匹配选项
 */
public enum RegexOptions {

    /**
     * i - 不区分大小写匹配
     */
    CASE_INSENSITIVE('i'),

    /**
     * m - 多行匹配：^ 和 $ 会匹配每行的开头/结尾
     */
    MULTILINE('m'),

    /**
     * x - 扩展模式：忽略空白字符与 # 注释
     */
    EXTENDED('x'),

    /**
     * s - 点字符匹配所有字符（包括换行符）
     */
    DOT_ALL('s'),

    /**
     * u - Unicode 支持（默认开启，但接受）
     */
    UNICODE('u');

    private final Character flag;

    RegexOptions(Character flag) {
        this.flag = flag;
    }

    public Character getFlag() {
        return flag;
    }
}
