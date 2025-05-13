package com.linyi.litekvdb.store.storage;

// 通用接口（适合String）
public interface StringStorage {
//    void set(String key, String value);
//    String get(String key);
//    boolean exists(String key);
//    boolean delete(String key);

     void setString(String key, String value);

    String getString(String key);

    Boolean delKey(String key);
}
