package com.linyi.litekvdb.store.storage;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: StringStorage
 * @Version: 1.0
 * @Description: 字符串存储接口
 */
public interface StringStorage {

    /**
     * 设置字符串
     * @param key 键
     * @param value 值
     */
    void set(String key, String value);

    /**
     * 获取字符串
     * @param key 键
     * @return
     */
    String get(String key);

    /**
     * 删除字符串
     * @param key 键
     * @return
     */
    boolean del(String key);

    /**
     * 判断是否存在
     * @param key 键
     * @return
     */
    boolean exists(String key);
}
