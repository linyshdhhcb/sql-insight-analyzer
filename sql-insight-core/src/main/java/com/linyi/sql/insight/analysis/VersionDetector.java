/*
 * Copyright (c) 2025 Lin Yi (linyi)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.linyi.sql.insight.analysis;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MySQL 版本探测器。
 *
 * @author linyi
 * @since 1.0.0
 */
public class VersionDetector {
    private static final Map<String, Boolean> CACHE_IS_MYSQL8 = new ConcurrentHashMap<>();

    /**
     * 检查MySQL数据库版本是否为8.0或更高版本
     *
     * @param connection 数据库连接对象，用于获取数据库版本信息
     * @return 如果是MySQL 8.0或更高版本返回true，否则返回false
     */
    public boolean isMySQL8OrAbove(Connection connection) {
        // 从缓存中获取数据库版本检查结果
        String key = cacheKey(connection);
        Boolean cached = key == null ? null : CACHE_IS_MYSQL8.get(key);
        if (cached != null)
            return cached;

        // 缓存未命中时，实际探测数据库版本
        boolean result = probeMySQL8OrAbove(connection);

        // 将探测结果存入缓存
        if (key != null) {
            CACHE_IS_MYSQL8.put(key, result);
        }
        return result;
    }


    /**
     * 检测MySQL数据库版本是否为8.0或更高版本
     *
     * @param connection 数据库连接对象
     * @return 如果是MySQL 8.0及以上版本返回true，否则返回false
     */
    private boolean probeMySQL8OrAbove(Connection connection) {
        if (connection == null)
            return false;
        try {
            DatabaseMetaData md = connection.getMetaData();
            String product = md.getDatabaseProductName();
            String version = md.getDatabaseProductVersion();
            // 仅对 MySQL/MariaDB 做判断，其它默认按非 8 处理
            if (product == null)
                return false;
            String p = product.toLowerCase();
            if (!(p.contains("mysql") || p.contains("mariadb")))
                return false;
            int[] vv = parseVersion(version);
            // MariaDB 的 JSON EXPLAIN 支持较晚，这里统一按 10.1+ 仍保守回退
            // 只要 MySQL 主版本 >= 8 即认为支持 FORMAT=JSON
            return vv[0] >= 8 && p.contains("mysql");
        } catch (SQLException ignore) {
            return false;
        }
    }


    /**
     * 生成数据库连接的缓存键值
     *
     * @param connection 数据库连接对象
     * @return 返回由数据库URL和用户名组成的缓存键值，格式为"url|user"，如果发生异常则返回null
     */
    private String cacheKey(Connection connection) {
        try {
            // 获取数据库元数据信息
            DatabaseMetaData md = connection.getMetaData();
            String url = md.getURL();
            String user = md.getUserName();
            // 将URL和用户名组合成缓存键值
            return url + "|" + user;
        } catch (Throwable ignore) {
            return null;
        }
    }


    /**
     * 解析版本字符串，提取主版本号、次版本号和修订号
     *
     * @param version 版本字符串，例如: "8.0.36", "8.0.23-commercial", "10.6.14-MariaDB-1:10.6.14+maria~ubu2004"
     * @return 包含三个整数的数组，分别表示主版本号、次版本号和修订号，如果解析失败则对应位置为0
     */
    private int[] parseVersion(String version) {
        int[] r = new int[]{0, 0, 0};
        if (version == null)
            return r;
        // 使用正则表达式分割版本字符串，只保留数字部分
        String[] parts = version.split("[^0-9]+");
        int idx = 0;
        // 遍历分割后的数字部分，最多取前三个数字作为版本号
        for (String p : parts) {
            if (p.isEmpty())
                continue;
            try {
                r[idx++] = Integer.parseInt(p);
                if (idx >= 3)
                    break;
            } catch (Throwable ignore) {
            }
        }
        return r;
    }

}
