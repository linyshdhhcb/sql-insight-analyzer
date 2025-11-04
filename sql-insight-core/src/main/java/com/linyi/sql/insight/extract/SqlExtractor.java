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

import com.linyi.sql.insight.util.SqlLengthTruncatorUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/**
 * 从 BoundSql 提取最终 SQL，并进行一行化、脱敏与长度截断。
 *
 * @author linyi
 * @since 1.0.0
 */
public class SqlExtractor {

    private final SensitiveFieldMasker masker = new SensitiveFieldMasker();

    /**
     * 提取并清理SQL语句
     *
     * @param boundSql  包含SQL语句的对象，如果为null则返回null
     * @param maxLength 返回SQL语句的最大长度限制
     * @return 清理后的SQL语句，如果输入为空则返回null，否则返回处理后的SQL字符串
     */
    public String extractAndSanitize(BoundSql boundSql, int maxLength) {
        if (boundSql == null) {
            return null;
        }
        String sql = boundSql.getSql();
        if (sql == null) {
            return null;
        }
        // 将SQL语句中的多个空白字符替换为单个空格，并去除首尾空格
        String oneLine = sql.replaceAll("\\s+", " ").trim();
        // 对SQL语句进行掩码处理
        String masked = masker.mask(oneLine);
        // 根据最大长度截断SQL语句
        return SqlLengthTruncatorUtils.truncate(masked, maxLength);
    }

    /**
     * 为 EXPLAIN 场景准备 SQL：在能力范围内用参数对象填充占位符（简单类型）。
     * 仅处理常见的 String/Number 参数；其它类型回退为原 SQL。
     */
    public String extractForExplain(BoundSql boundSql, int maxLength) {
        if (boundSql == null)
            return null;
        String sql = boundSql.getSql();
        if (sql == null)
            return null;
        String oneLine = sql.replaceAll("\\s+", " ").trim();
        String filled = oneLine;
        try {
            java.util.List<ParameterMapping> mappings = boundSql.getParameterMappings();
            if (mappings != null && !mappings.isEmpty() && oneLine.contains("?")) {
                Object rootParam = boundSql.getParameterObject();
                MetaObject meta = rootParam == null ? null : SystemMetaObject.forObject(rootParam);
                for (ParameterMapping pm : mappings) {
                    String prop = pm.getProperty();
                    Object value;
                    if (boundSql.hasAdditionalParameter(prop)) {
                        value = boundSql.getAdditionalParameter(prop);
                    } else if (meta != null && meta.hasGetter(prop)) {
                        value = meta.getValue(prop);
                    } else {
                        value = rootParam;
                    }
                    String lit = toSqlLiteral(value);
                    if (lit == null)
                        lit = "NULL";
                    int q = filled.indexOf('?');
                    if (q < 0)
                        break;
                    filled = filled.substring(0, q) + lit + filled.substring(q + 1);
                }
            }
        } catch (Throwable ignore) {
        }
        return SqlLengthTruncatorUtils.truncate(filled, maxLength);
    }

    private String toSqlLiteral(Object param) {
        try {
            if (param instanceof Number) {
                return String.valueOf(param);
            }
            if (param instanceof CharSequence) {
                String s = param.toString().replace("'", "''");
                return "'" + s + "'";
            }
            if (param instanceof Boolean) {
                return (Boolean) param ? "1" : "0";
            }
        } catch (Throwable ignore) {
        }
        return null;
    }
}
