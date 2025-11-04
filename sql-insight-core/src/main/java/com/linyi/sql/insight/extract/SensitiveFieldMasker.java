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

package com.linyi.sql.insight.extract;

import java.util.regex.Pattern;

/**
 * 基础敏感字段脱敏。采用启发式正则：将 \n(password|token|mobile|id_card|email)\s*=\s*(值)\n 替换为
 * ****。
 * 简单实现，不追求 100% 场景覆盖，但保证安全优先。
 *
 * @author linyi
 * @since 1.0.0
 */
public class SensitiveFieldMasker {

    private static final Pattern KV_PATTERN = Pattern.compile(
            "(?i)(password|token|mobile|id_card|email)\\s*=\\s*('.*?'|\\d+|[A-Za-z0-9_@.-]+)");

    /**
     * 对SQL语句中的敏感信息进行掩码处理
     *
     * @param sql 需要进行掩码处理的SQL语句
     * @return 掩码处理后的SQL语句，将匹配到的敏感信息替换为'****'
     */
    public String mask(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }
        // 使用预定义的正则表达式模式匹配SQL中的键值对
        java.util.regex.Matcher matcher = KV_PATTERN.matcher(sql);
        StringBuffer sb = new StringBuffer();
        // 遍历所有匹配项，将值部分替换为掩码
        while (matcher.find()) {
            String group1 = matcher.group(1);
            String replacement = group1 + "='****'";
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        // 将剩余未匹配的部分添加到结果中
        matcher.appendTail(sb);
        return sb.toString();
    }

}
