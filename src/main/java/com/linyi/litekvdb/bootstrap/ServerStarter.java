package com.linyi.litekvdb.bootstrap;

import com.linyi.litekvdb.core.network.Reactor;
import com.linyi.litekvdb.store.Database;

import java.io.IOException;

public class ServerStarter {
    public static void main(String[] args) throws IOException {
        System.out.println("LiteKV-DB 正在启动...");
        // 全局唯一存储
        Database storage = new Database();
        // 注入依赖
        new Reactor(storage).start(6381);
    }
}