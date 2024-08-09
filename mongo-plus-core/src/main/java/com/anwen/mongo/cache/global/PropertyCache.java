package com.anwen.mongo.cache.global;

/**
 * 配置文件缓存
 *
 * @author JiaChaoYang
 **/
public class PropertyCache {

    /**
     * 驼峰转下划线
     * @date 2024/6/27 下午11:56
     */
    public static Boolean camelToUnderline = false;

    /**
     * 是否忽略空值
     * @date 2023/10/25 15:42
    */
    public static Boolean ignoringNull = true;

    /**
     * 是否开启spring事务
     * @date 2023/10/25 15:43
    */
    public static Boolean transaction = false;

    /**
     * 格式化执行语句，默认false
     * @date 2023/11/22 11:03
    */
    public static Boolean format = false;

    /**
     * 存放自增id的集合
     * @date 2024/5/1 下午10:40
     */
    public static String autoIdCollectionName = "counters";

    /**
     * 小黑子模式
     * @date 2024/5/2 上午2:24
     */
    public static Boolean ikun = false;

    /**
     * 私钥，非对称加密
     * @date 2024/6/30 下午1:28
     */
    public static String privateKey = "";

    /**
     * 公钥，非对称加密
     * @date 2024/6/30 下午1:28
     */
    public static String publicKey = "";

    /**
     * 秘钥，对称加密
     * @date 2024/6/30 下午1:28
     */
    public static String key = "";

    /**
     * 自动转换ObjectId
     * @date 2024/7/26 下午5:41
     */
    public static Boolean autoConvertObjectId = true;

    /**
     * 是否将Id字段的ObjectId转换为字段的类型
     * @date 2024/8/9 14:53
     */
    public static Boolean objectIdConvertType = false;

    /**
     * 是否打印日志
     * @date 2024/7/28 下午5:59
     */
    public static Boolean log = false;

}
