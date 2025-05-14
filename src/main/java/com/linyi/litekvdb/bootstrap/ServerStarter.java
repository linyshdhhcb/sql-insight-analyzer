package com.linyi.litekvdb.bootstrap;

import com.linyi.litekvdb.core.command.CommandExecutor;
import com.linyi.litekvdb.core.network.Reactor;
import com.linyi.litekvdb.store.storage.database.HashDatabase;
import com.linyi.litekvdb.store.storage.database.StringDatabase;
import com.linyi.litekvdb.store.structure.LiteKVString;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: ServerStarter
 * @Version: 1.0
 * @Description: 启动类
 */
public class ServerStarter {
    public static void main(String[] args) throws IOException {
        System.out.println("LiteKV-DB 正在启动...");

        // 全局唯一String存储
        StringDatabase stringDb =StringDatabase.getInstance();
      // 全局唯一Hash存储
        HashDatabase hashDb = new HashDatabase();
//        // 全局唯一List存储
//        Database listStorage = new Database();
//        // 全局唯一Set存储
//        Database setStorage = new Database();
//        // 全局唯一ZSet存储
//        Database zsetStorage = new Database();

        CommandExecutor executor = new CommandExecutor(
                stringDb,
                hashDb
                /*,
                listDb,
                setDb,
                zsetDb
                */
        );
        // 注入依赖
        new Reactor(executor).start(6381);
    }
}