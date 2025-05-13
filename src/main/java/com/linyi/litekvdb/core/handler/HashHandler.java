package com.linyi.litekvdb.core.handler;

import com.linyi.litekvdb.core.handler.CommandHandler;
import com.linyi.litekvdb.store.Database;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: HashHandler
 * @Version: 1.0
 * @Description:
 */
public class HashHandler implements CommandHandler {

    private final Database database;

    public HashHandler(Database database) {
        this.database = database;
    }

    @Override
    public String executeCommand(String command, String key, String... args) {
        return null;
    }
}
