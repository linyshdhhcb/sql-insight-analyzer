package com.linyi.litekvdb.core.handler;

import com.linyi.litekvdb.store.storage.StringStorage;

import static com.linyi.litekvdb.common.Constant.*;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: StringHandler
 * @Version: 1.0
 * @Description: 字符串命令处理器
 */
public class StringHandler implements CommandHandler {

    private final StringStorage storage;

    public StringHandler(StringStorage storage) {
        this.storage = storage;
    }

    @Override
    public String executeCommand(String command, String key, String... args) {
        switch (command.toUpperCase()) {
            case SET:
                return set(key, args);
            case GET:
                return get(key);
            case DEL:
                return del(key);
            case EXISTS:
                return exists(key);
            default:
                return "-ERR unknown command '" + command + "'\r\n";
        }
    }

    /**
     * 添加值
     * @param key 键
     * @param args 数组参数
     * @return 添加结果
     */
    private String set(String key, String... args) {
        if (args.length < 1) {
            return "-ERR wrong number of arguments for 'SET'\r\n";
        }
        storage.set(key, args[2]);
        return "$OK\r\n";
    }

    /**
     * 获取值
     * @param key 键
     * @return 值
     */
    private String get(String key) {
        String value = storage.get(key);
        if (value == null) {
            return "$-1\r\n";
        }
        return "$" + value.length() + "\r\n" + value + "\r\n";
    }

    /**
     * 删除键
     * @param key 键
     * @return 删除结果
     */
    private String del(String key) {
        boolean removed = storage.del(key);
        return ":" + (removed ? 1 : 0) + "\r\n";
    }

    /**
     * 判断键是否存在
     * @param key 键
     * @return 存在结果
     */
    private String exists(String key) {
        boolean exists = storage.exists(key);
        return ":" + (exists ? 1 : 0) + "\r\n";
    }
}
