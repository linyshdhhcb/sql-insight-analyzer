package com.linyi.litekvdb.core.command;

import com.linyi.litekvdb.common.DataType;
import com.linyi.litekvdb.core.handler.CommandHandler;
import com.linyi.litekvdb.core.handler.HashHandler;
import com.linyi.litekvdb.core.handler.StringHandler;
import com.linyi.litekvdb.store.storage.HashStorage;
import com.linyi.litekvdb.store.storage.StringStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: CommandExecutor
 * @Version: 1.0
 * @Description: 命令路由执行器
 */
public class CommandExecutor {

    private final Map<DataType, CommandHandler> handlerMap = new HashMap<>();

    public CommandExecutor(StringStorage stringDb, HashStorage hashDb) {
        handlerMap.put(DataType.STRING, new StringHandler(stringDb));
        handlerMap.put(DataType.HASH, new HashHandler(hashDb));
//        handlerMap.put(DataType.LIST, new ListHandler(listDb));
//        handlerMap.put(DataType.SET, new SetHandler(setDb));
//        handlerMap.put(DataType.ZSET, new ZSetHandler(zsetDb));
    }

    /**
     * 执行命令
     * @param command 命令
     * @param key 键
     * @param args 数组参数
     * @return
     */
    public String executeCommand(String command, String key, String... args) {
        // 根据命令类型选择对应的处理器
        DataType type = detectDataType(command);
        // 根据数据类型选择对应的处理器
        CommandHandler handler = handlerMap.get(type);
        if (handler == null) {
            return "-ERR unknown command type\r\n";
        }
        //  执行命令
        return handler.executeCommand(command, key, args);
    }

    /**
     * 路由数据类型
     * @param command 命令
     * @return
     */
    private DataType detectDataType(String command) {
        command = command.toUpperCase();
        if (command.startsWith("SET") || command.startsWith("GET") || command.startsWith("DEL")){
            return DataType.STRING;
        }
        if (command.startsWith("H")){
            return DataType.HASH;
        }
        if (command.startsWith("L")){
            return DataType.LIST;
        }
        if (command.startsWith("S") && !command.startsWith("SET")){
            return DataType.SET;
        }
        if (command.startsWith("Z")){
            return DataType.ZSET;
        }
        return null;
    }
}