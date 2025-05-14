package com.linyi.litekvdb.store.storage.database;

import com.linyi.litekvdb.store.storage.HashStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: HashDatabase
 * @Version: 1.0
 * @Description: Hash操作实现
 */
public class HashDatabase implements HashStorage {

    /**
     * 哈希数据存储
     */
    private final Map<String, Map<String, String>> hashData = new ConcurrentHashMap<>();

    /**
     * 单例实例
     */
    private static final HashDatabase INSTANCE = new HashDatabase();

    private HashDatabase() {
    }

    /**
     * 获取单例实例
     * @return HashDatabase
     */
    public static HashDatabase getInstance() {
        return INSTANCE;
    }
    /**
     * 设置哈希数据
     * @param key 键
     * @param field 字段
     * @param value 值
     */

    @Override
    public void hset(String key, String field, String value) {
        hashData.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(field, value);
    }

    /**
     * 获取哈希数据
     * @param key 键
     * @param field 字段
     * @return
     */
    @Override
    public String hget(String key, String field) {
        Map<String, String> map = hashData.get(key);
        return map != null ? map.get(field) : null;
    }

    /**
     * 获取所有哈希数据
     * @param key
     * @return
     */
    @Override
    public Map<String, String> hgetAll(String key) {
        return hashData.getOrDefault(key, Map.of());
    }

    /**
     * 删除哈希数据
     * @param key
     * @param field
     * @return
     */
    @Override
    public boolean hdel(String key, String field) {
        Map<String, String> map = hashData.get(key);
        if (map != null) {
            return map.remove(field) != null;
        }
        return false;
    }

    /**
     * 判断哈希数据是否存在
     * @param key
     * @return
     */
    @Override
    public boolean exists(String key) {
        return hashData.containsKey(key);
    }
}
