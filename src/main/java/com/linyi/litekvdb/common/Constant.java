package com.linyi.litekvdb.common;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: Constant
 * @Version: 1.0
 * @Description: 常量
 */
public class Constant {

    //分割成行
    public static final String SPLIT = "\r\n ";

    /**
     * 缓存对象类型
     */
    public static final byte TYPE_STRING = 'S';
    public static final byte TYPE_HASH = 'H';

    /**
     * String命令
     */
    public static final String SET = "SET";

    public static final String GET = "GET";

    public static final String DEL = "DEL";

    public static final String EXISTS = "EXISTS";
}
