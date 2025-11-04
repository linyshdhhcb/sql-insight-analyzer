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

package com.linyi.sql.insight.util;

/**
 * SQL 长度截断工具，避免日志与 EXPLAIN 因超长 SQL 产生过高开销。
 *
 * @author linyi
 * @since 1.0.0
 */
public class SqlLengthTruncatorUtils {

    private static final String TRUNCATED_SUFFIX = " ... [truncated]";

    private SqlLengthTruncatorUtils() {
    }

    /**
     * 截断SQL字符串到指定长度
     *
     * @param sql       需要截断的SQL字符串
     * @param maxLength 最大长度限制
     * @return 截断后的字符串，如果原字符串长度超过限制则在末尾添加截断后缀
     */
    public static String truncate(String sql, int maxLength) {
        // 处理空字符串情况
        if (sql == null) {
            return null;
        }
        // 当最大长度无效时，返回原字符串
        if (maxLength <= 0) {
            return sql;
        }
        // 当字符串长度未超过限制时，直接返回
        if (sql.length() <= maxLength) {
            return sql;
        }
        // 计算截断位置，确保能容纳截断后缀
        int end = Math.max(0, maxLength - TRUNCATED_SUFFIX.length());
        // 执行截断并添加后缀
        return sql.substring(0, end) + TRUNCATED_SUFFIX;
    }

}
