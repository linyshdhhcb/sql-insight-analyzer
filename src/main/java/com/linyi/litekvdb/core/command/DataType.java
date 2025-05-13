package com.linyi.litekvdb.core.command;

public enum DataType {
    /**
     * 字符串类型
     */
    STRING,
    /**
     * 列表类型
     */
    LIST,
    /**
     * 集合类型
     */
    SET,
    /**
     * 哈希类型
     */
    HASH,
    /**
     * 有序集合类型
     */
    ZSET;
}
