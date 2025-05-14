package com.linyi.litekvdb.store.storage;

import java.util.Map;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: HashStorage
 * @Version: 1.0
 * @Description: 哈希存储接口
 */
public interface HashStorage {
    void hset(String key, String field, String value);
    String hget(String key, String field);
    Map<String, String> hgetAll(String key);
    boolean hdel(String key, String field);
    boolean exists(String key);
}
