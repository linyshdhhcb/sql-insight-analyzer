package com.linyi.litekvdb.store.storage.database;

import com.linyi.litekvdb.store.storage.StringStorage;
import com.linyi.litekvdb.store.structure.LiteKVString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: StringDatabase
 * @Version: 1.0
 * @Description: 字符串操作
 */
public class StringDatabase implements StringStorage {

    /**
     * 字符串数据存储
     */
    private static final Map<String, LiteKVString> stringData = new ConcurrentHashMap<>();


    /**
     * 单例实例
     */
    private static final StringDatabase INSTANCE = new StringDatabase();


    private StringDatabase() {}

    /**
     * 获取单例实例
     * @return
     */
    public static StringDatabase getInstance() {
        return INSTANCE;
    }


    @Override
    public void set(String key, String value) {
        stringData.put(key, new LiteKVString(value));
    }

    @Override
    public String get(String key) {
        LiteKVString liteKVString = stringData.get(key);
        return liteKVString != null ? liteKVString.getValue() : null;
    }

    @Override
    public boolean del(String key) {
        return stringData.remove(key) != null;
    }

    @Override
    public boolean exists(String key) {
        return stringData.containsKey(key);
    }
}
