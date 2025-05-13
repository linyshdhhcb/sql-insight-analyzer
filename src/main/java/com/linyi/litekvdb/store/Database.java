package com.linyi.litekvdb.store;

import com.linyi.litekvdb.core.command.DataType;
import com.linyi.litekvdb.store.structure.LiteKVObject;
import com.linyi.litekvdb.store.structure.LiteKVString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Database {

    private final Map<String, Object> data = new ConcurrentHashMap<>();
    private final Map<String, DataType> typeMap = new ConcurrentHashMap<>();

    // ---------- String 操作 ----------

    public void setString(String key, String value) {
        data.put(key, value);
        typeMap.put(key, DataType.STRING);
    }

    public String getString(String key) {
        if (typeMap.get(key) != DataType.STRING){
            return null;
        }
        return (String) data.get(key);
    }

    public boolean delKey(String key) {
        if (data.containsKey(key)) {
            data.remove(key);
            typeMap.remove(key);
            return true;
        }
        return false;
    }

    public boolean exists(String key) {
        return data.containsKey(key);
    }

    public DataType getType(String key) {
        return typeMap.get(key);
    }
}