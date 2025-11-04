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

import com.linyi.sql.insight.model.SqlAnalysisResultList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责执行 EXPLAIN（后续支持 JSON/TABLE 模式与回退）。
 *
 * @author linyi
 * @since 1.0.0
 */
public class ExplainExecutor {
    private final VersionDetector versionDetector = new VersionDetector();
    private static final Logger log = LoggerFactory.getLogger(ExplainExecutor.class);

    public SqlAnalysisResultList explain(Connection connection, String sql) {
        // 基于版本的优先策略 + 能力回退
        if (connection == null || sql == null || sql.isEmpty()) {
            return new SqlAnalysisResultList();
        }
        boolean preferJson = versionDetector.isMySQL8OrAbove(connection);
        if (preferJson) {
            // JSON 优先
            try (Statement stmt = connection.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("EXPLAIN FORMAT=JSON " + sql)) {
                    if (rs.next()) {
                        String json = rs.getString(1);
                        SqlAnalysisResultList list = new ExplainParser().parseJson(json);
                        if (list != null && !list.getResults().isEmpty()) {
                            return list;
                        }
                        // JSON 为空则回退一次表格
                        try (Statement stmt2 = connection.createStatement();
                                ResultSet rs2 = stmt2
                                        .executeQuery("EXPLAIN " + sql)) {
                            return new ExplainParser().parseTable(rs2);
                        }
                    }
                }
            } catch (SQLException ignore) {
                // 回退到表格
                log.warn("[SqlInsight] EXPLAIN FORMAT=JSON failed: {} sql= {}", ignore.toString(), sql);
            }
        }
        // 表格（默认或回退）
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("EXPLAIN " + sql)) {
            return new ExplainParser().parseTable(rs);
        } catch (SQLException e) {
            if (!preferJson) {
                // 对于非 8，也尝试一次 JSON（某些兼容层可能支持）
                try (Statement stmt = connection.createStatement()) {
                    try (ResultSet rs = stmt.executeQuery("EXPLAIN FORMAT=JSON " + sql)) {
                        if (rs.next()) {
                            String json = rs.getString(1);
                            return new ExplainParser().parseJson(json);
                        }
                    }
                } catch (SQLException ignore) {
                    log.warn("[SqlInsight] EXPLAIN fallback JSON failed: {} sql= {}", ignore.toString(), sql);
                }
            }
            log.warn("[SqlInsight] EXPLAIN failed: {} sql= {}", e.toString(), sql);
            return new SqlAnalysisResultList();
        }

        // 删除 PreparedStatement 绑定方式，改用外部生成的可执行 SQL
    }
}
