package com.linyi.litekvdb.core.handler;

import com.linyi.litekvdb.store.Database;
import com.linyi.litekvdb.store.structure.LiteKVString;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: StringHandler
 * @Version: 1.0
 * @Description: 字符串处理器
 */
public class StringHandler implements CommandHandler {

    private final Database database;

    public StringHandler(Database database) {
        this.database = database;
    }

    @Override
    public String executeCommand(String command, String key, String... args) {
        switch (command.toUpperCase()) {
            case "SET":
                return set(key, args);
            case "GET":
                return get(key);
            case "DEL":
                return del(key);
            case "EXISTS":
                return exists(key);
            default:
                return "-ERR unknown command '" + command + "'\r\n";
        }
    }

    private String set(String key, String... args) {
        if (args.length < 1) {
            return "-ERR wrong number of arguments for 'SET'\r\n";
        }
        database.setString(key, args[1]);
        return "+OK\r\n";
    }

    private String get(String key) {
        String value = database.getString(key);
        if (value == null) {
            return "$-1\r\n";
        }
        return "$" + value.length() + "\r\n" + value + "\r\n";
    }

    private String del(String key) {
        boolean removed = database.delKey(key);
        return ":" + (removed ? 1 : 0) + "\r\n";
    }

    private String exists(String key) {
        boolean exists = database.exists(key);
        return ":" + (exists ? 1 : 0) + "\r\n";
    }
}
