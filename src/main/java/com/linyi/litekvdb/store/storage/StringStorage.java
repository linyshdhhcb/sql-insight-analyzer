package com.linyi.litekvdb.store.storage;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: StringStorage
 * @Version: 1.0
 * @Description: 字符串存储接口
 */
public interface StringStorage {

    void set(String key, String value);

    String get(String key);

    boolean del(String key);

    boolean exists(String key);
}
