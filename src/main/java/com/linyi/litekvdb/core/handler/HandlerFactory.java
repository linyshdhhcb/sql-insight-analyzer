package com.linyi.litekvdb.core.handler;

import com.linyi.litekvdb.core.command.DataType;
import com.linyi.litekvdb.store.Database;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: HandlerFactory
 * @Version: 1.0
 * @Description: 处理器工厂
 */
public class HandlerFactory {

    private final Database database;

    public HandlerFactory(Database database) {
        this.database = database;
    }
    public CommandHandler getHandler(DataType dataType) {
        switch (dataType) {
            case STRING:
                return new StringHandler(database);
            // 可以在这里添加更多类型的处理器
            default:
                throw new IllegalArgumentException("Unknown data type: " + dataType);
        }
    }
}
