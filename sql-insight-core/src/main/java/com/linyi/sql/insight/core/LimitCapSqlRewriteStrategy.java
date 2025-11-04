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

package com.linyi.sql.insight.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将 SQL 中的 LIMIT 上限强制收敛至配置的最大值，避免意外大查询。
 * 仅对简单形式的 "... limit N" 生效，不处理 offset 形式。
 *
 * @author linyi
 * @since 1.0.0
 */
public class LimitCapSqlRewriteStrategy implements SqlRewriteStrategy {

    // limit 匹配
    private static final Pattern LIMIT_NUM = Pattern.compile("(?i)\\blimit\\s+(\\d+)\\b");

    private final int maxLimit;

    public LimitCapSqlRewriteStrategy(int maxLimit) {
        this.maxLimit = Math.max(1, maxLimit);
    }

    /**
     * 重写SQL语句，将超出最大限制的LIMIT值替换为最大限制值
     *
     * @param sqlId SQL语句的唯一标识符
     * @param sql   需要重写的SQL语句
     * @return 重写后的SQL语句，如果SQL语句中的LIMIT值超过最大限制则会被替换，否则返回原SQL语句
     */
    @Override
    public String rewrite(String sqlId, String sql) {
        if (sql == null)
            return null;
        Matcher m = LIMIT_NUM.matcher(sql);
        StringBuffer sb = new StringBuffer();
        boolean changed = false;

        // 查找并替换超出最大限制的LIMIT值
        while (m.find()) {
            try {
                int v = Integer.parseInt(m.group(1));
                if (v > maxLimit) {
                    changed = true;
                    m.appendReplacement(sb, m.group(0).replaceFirst("\\d+", String.valueOf(maxLimit)));
                }
            } catch (Exception ignore) {
            }
        }

        // 如果有修改，则返回修改后的SQL语句
        if (changed) {
            m.appendTail(sb);
            return sb.toString();
        }
        return sql;
    }

}
