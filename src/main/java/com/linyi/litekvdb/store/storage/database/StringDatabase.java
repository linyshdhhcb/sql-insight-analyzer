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

    private final Map<String, LiteKVString> data = new ConcurrentHashMap<>();

    @Override
    public void set(String key, String value) {
        data.put(key, new LiteKVString(value));
    }

    @Override
    public String get(String key) {
        LiteKVString liteKVString = data.get(key);
        return liteKVString != null ? liteKVString.getValue() : null;
    }

    @Override
    public boolean del(String key) {
        return data.remove(key) != null;
    }

    @Override
    public boolean exists(String key) {
        return data.containsKey(key);
    }
}
