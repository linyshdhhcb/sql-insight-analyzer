package com.linyi.litekvdb.store.storage.database;

import com.linyi.litekvdb.core.command.DataType;
import com.linyi.litekvdb.store.storage.StringStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: StringDatabase
 * @Version: 1.0
 * @Description:
 */
public class StringDatabase implements StringStorage {

    private final Map<String, String> data = new ConcurrentHashMap<>();
    private final Map<String, DataType> typeMap = new ConcurrentHashMap<>();

    // ---------- String 操作 ----------

    @Override
    public void setString(String key, String value) {
        data.put(key, value);
        typeMap.put(key, DataType.STRING);
    }

    @Override
    public String getString(String key) {
        if (typeMap.get(key) != DataType.STRING){
            return null;
        }
        return (String) data.get(key);
    }

    @Override
    public Boolean delKey(String key) {
        if (data.containsKey(key)) {
            data.remove(key);
            typeMap.remove(key);
            return true;
        }
        return false;
    }


}
