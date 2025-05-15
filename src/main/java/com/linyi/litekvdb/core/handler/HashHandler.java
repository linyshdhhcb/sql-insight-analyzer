package com.linyi.litekvdb.core.handler;

import com.linyi.litekvdb.store.storage.HashStorage;

import java.util.Map;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: HashHandler
 * @Version: 1.0
 * @Description: 哈希命令处理器
 */
public class HashHandler implements CommandHandler {

    private final HashStorage storage;

    public HashHandler(HashStorage storage) {
        this.storage = storage;
    }

    @Override
    public String executeCommand(String command, String key, String... args) {
        switch (command.toUpperCase()) {
            case "HSET":
                return hset(key, args);
            case "HGET":
                return hget(key, args);
            case "HGETALL":
                return hgetAll(key);
            case "HDEL":
                return hdel(key, args);
            case "HEXISTS":
                return hExists(key, args);
            default:
                return "-ERROR unknown hash command '" + command + "'\r\n";
        }
    }

    private String hExists(String key, String[] args) {
        if (args.length < 1){
            return "-ERROR wrong number of arguments for 'HEXISTS'\r\n";
        }
        return ":" + (storage.hget(key, args[0]) != null ? 1 : 0) + "\r\n";
    }

    private String hdel(String key, String... args) {
        if (args.length < 1){
            return "-ERROR wrong number of arguments for 'HDEL'\r\n";
        }
        return ":" + (storage.hdel(key, args[0]) ? 1 : 0) + "\r\n";
    }

    private String hgetAll(String key) {
        Map<String, String> map = storage.hgetAll(key);
        StringBuilder sb = new StringBuilder();
        sb.append("*" + (map.size() * 2) + "\r\n");
        map.forEach((field, v) -> {
            sb.append("$" + field.length() + "\r\n" + field + "\r\n");
            sb.append("$" + v.length() + "\r\n" + v + "\r\n");
        });
        return sb.toString();
    }

    private String hget(String key, String... args) {
        if (args.length < 2){
            return "-ERR wrong number of arguments for 'HGET'\r\n";
        }
        String val = storage.hget(key, args[2]);
        return val == null ? "$-1\r\n" : "$" + val.length() + "\r\n" + val + "\r\n";
    }

    private String hset(String key, String... args) {
        if (args.length < 4){
            return "-ERROR wrong number of arguments for 'HSET'\r\n";
        }
        storage.hset(key, args[2], args[3]);
        return "$OK\r\n";

    }
}
