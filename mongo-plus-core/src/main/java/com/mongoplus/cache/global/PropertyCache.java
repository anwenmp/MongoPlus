package com.mongoplus.cache.global;

/**
 * 配置文件缓存
 *
 * @author JiaChaoYang
 **/
public class PropertyCache {

    /**
     * 驼峰转下划线
     */
    public static Boolean camelToUnderline = false;

    /**
     * 是否忽略空值
    */
    public static Boolean ignoringNull = true;

    /**
     * 是否开启spring事务
    */
    public static Boolean transaction = false;

    /**
     * 格式化执行语句，默认false
    */
    public static Boolean format = false;

    /**
     * 存放自增id的集合
     */
    public static String autoIdCollectionName = "counters";

    /**
     * 小黑子模式
     */
    public static Boolean ikun = false;

    /**
     * 私钥，非对称加密
     */
    public static String privateKey = "";

    /**
     * 公钥，非对称加密
     */
    public static String publicKey = "";

    /**
     * 秘钥，对称加密
     */
    public static String key = "";

    /**
     * 自动转换ObjectId
     */
    public static Boolean autoConvertObjectId = true;

    /**
     * 是否将Id字段的ObjectId转换为字段的类型
     */
    public static Boolean objectIdConvertType = false;

    /**
     * 是否打印日志
     */
    public static Boolean log = false;

}
