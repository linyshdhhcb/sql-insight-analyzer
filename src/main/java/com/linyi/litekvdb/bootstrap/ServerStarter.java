package com.linyi.litekvdb.bootstrap;

import com.linyi.litekvdb.core.command.CommandExecutor;
import com.linyi.litekvdb.core.network.Reactor;
import com.linyi.litekvdb.store.Database;

import java.io.IOException;

public class ServerStarter {
    public static void main(String[] args) throws IOException {
        System.out.println("LiteKV-DB 正在启动...");
        // 全局唯一String存储
        Database stringStorage = new Database();
//        // 全局唯一Hash存储
        Database hashStorage = new Database();
//        // 全局唯一List存储
//        Database listStorage = new Database();
//        // 全局唯一Set存储
//        Database setStorage = new Database();
//        // 全局唯一ZSet存储
//        Database zsetStorage = new Database();

        CommandExecutor executor = new CommandExecutor(
                stringStorage,
                hashStorage
                /*,
                listStorage,
                setStorage,
                zsetStorage*/
        );
        // 注入依赖
        new Reactor(executor).start(6381);
    }
}