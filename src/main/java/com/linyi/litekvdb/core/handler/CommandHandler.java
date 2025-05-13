package com.linyi.litekvdb.core.handler;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: DataTypeHandler
 * @Version: 1.0
 * @Description: 通用数据类型处理器
 */
public interface CommandHandler {

    /**
     * 执行命令
     * @param command 命令
     * @param key 键
     * @param args 数组参数
     * @return
     */
    String executeCommand(String command, String key, String... args);
}
