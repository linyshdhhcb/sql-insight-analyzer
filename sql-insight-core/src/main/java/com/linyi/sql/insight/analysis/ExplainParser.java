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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.linyi.sql.insight.model.SqlAnalysisResult;
import com.linyi.sql.insight.model.SqlAnalysisResultList;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Locale;

/**
 * 解析 EXPLAIN 的 JSON/TABLE 结果为统一结构。
 *
 * @author linyi
 * @since 1.0.0
 */
public class ExplainParser {

    /**
     * 解析SQL分析结果的JSON字符串，将其转换为SqlAnalysisResultList对象
     *
     * @param json 包含SQL分析结果的JSON字符串，可能为null或空字符串
     * @return SqlAnalysisResultList对象，包含解析出的SQL分析结果；如果输入为空则返回空列表
     */
    public SqlAnalysisResultList parseJson(String json) {

        SqlAnalysisResultList list = new SqlAnalysisResultList();
        // 如果输入JSON为空，则直接返回空列表
        if (json == null || json.isEmpty()) {
            return list;
        }
        try {
            // 解析JSON根对象并根据不同的结构进行处理
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("query_block")) {
                // 处理query_block类型的SQL分析结果
                traverseQueryBlock(root.getAsJsonObject("query_block"), list, null);
            } else if (root.has("table")) {
                // 处理table类型的SQL分析结果
                extractFromTableObject(root.getAsJsonObject("table"), list, null);
            }
        } catch (Throwable ignore) {
            // 忽略解析过程中的所有异常
        }
        return list;
    }


    /**
     * 遍历查询块对象，提取SQL分析结果
     *
     * @param qb       查询块的JsonObject对象，包含查询的结构化信息
     * @param list     SQL分析结果列表，用于存储提取到的分析结果
     * @param parentId 父级查询块的标识符，用于关联嵌套查询的关系
     */
    private void traverseQueryBlock(JsonObject qb, SqlAnalysisResultList list, String parentId) {
        if (qb == null)
            return;

        // 处理直接包含table的情况
        if (qb.has("table")) {
            JsonObject t = qb.getAsJsonObject("table");
            extractFromTableObject(t, list, parentId);
        }

        // 处理嵌套循环连接的情况
        if (qb.has("nested_loop")) {
            for (JsonElement e : qb.getAsJsonArray("nested_loop")) {
                if (e.isJsonObject()) {
                    JsonObject o = e.getAsJsonObject();
                    if (o.has("table")) {
                        extractFromTableObject(o.getAsJsonObject("table"), list, parentId);
                    }
                    // 处理附加的子查询
                    if (o.has("attached_subqueries")) {
                        for (JsonElement sub : o.getAsJsonArray("attached_subqueries")) {
                            if (sub.isJsonObject() && sub.getAsJsonObject().has("query_block")) {
                                traverseQueryBlock(sub.getAsJsonObject().getAsJsonObject("query_block"), list,
                                        parentId);
                            }
                        }
                    }
                }
            }
        }

        // 处理排序操作的情况
        if (qb.has("ordering_operation")) {
            JsonObject ord = qb.getAsJsonObject("ordering_operation");
            if (ord.has("using_filesort") && ord.get("using_filesort").getAsBoolean()) {
                SqlAnalysisResult r = new SqlAnalysisResult();
                r.setExtra("Using filesort");
                list.add(r);
            }
        }
    }


    /**
     * 从JSON对象中提取表访问信息并添加到分析结果列表中
     *
     * @param t        包含表访问信息的JSON对象
     * @param list     用于存储SQL分析结果的列表
     * @param parentId 父节点ID（在当前实现中未使用）
     */
    private void extractFromTableObject(JsonObject t, SqlAnalysisResultList list, String parentId) {
        if (t == null)
            return;
        SqlAnalysisResult r = new SqlAnalysisResult();

        // 提取表基本信息
        if (t.has("table_name"))
            r.setTable(getAsString(t, "table_name"));
        if (t.has("access_type"))
            r.setType(getAsString(t, "access_type"));
        if (t.has("key"))
            r.setKey(getAsString(t, "key"));
        if (t.has("key_length"))
            r.setKeyLen(getAsString(t, "key_length"));
        if (t.has("rows_examined_per_scan"))
            trySetRows(r, getAsString(t, "rows_examined_per_scan"));

        // 构建额外信息字符串
        StringBuilder extra = new StringBuilder();
        if (t.has("using_temporary") && t.get("using_temporary").getAsBoolean()) {
            appendExtra(extra, "Using temporary");
        }
        if (t.has("using_filesort") && t.get("using_filesort").getAsBoolean()) {
            appendExtra(extra, "Using filesort");
        }
        if (extra.length() > 0)
            r.setExtra(extra.toString());

        list.add(r);
    }


    /**
     * 向StringBuilder中追加额外的token，如果StringBuilder中已有内容，则先添加逗号分隔符
     *
     * @param sb    用于构建字符串的StringBuilder对象
     * @param token 需要追加的字符串令牌
     */
    private static void appendExtra(StringBuilder sb, String token) {
        // 如果StringBuilder中已有内容，先添加逗号和空格作为分隔符
        if (sb.length() > 0)
            sb.append(", ");
        sb.append(token);
    }


    /**
     * 从JsonObject中获取指定键的字符串值
     *
     * @param o   JsonObject对象
     * @param key 要获取值的键名
     * @return 如果键存在且能转换为字符串则返回对应值，否则返回null
     */
    private static String getAsString(JsonObject o, String key) {
        try {
            // 尝试获取指定键的值并转换为字符串
            return o.get(key).getAsString();
        } catch (Throwable ignore) {
            // 发生异常时返回null
            return null;
        }
    }


    /**
     * 尝试设置SQL分析结果的行数
     *
     * @param r SQL分析结果对象
     * @param v 行数字符串值
     */
    private static void trySetRows(SqlAnalysisResult r, String v) {
        // 解析并设置行数
        try {
            if (v == null)
                return;
            long rows = (long) Double.parseDouble(v);
            r.setRows(rows);
        } catch (Throwable ignore) {
        }
    }


    /**
     * 解析ResultSet中的表分析结果，将其转换为SqlAnalysisResultList对象列表
     *
     * @param rs 包含SQL分析结果的ResultSet对象
     * @return SqlAnalysisResultList 包含解析后的SQL分析结果列表，如果rs为null则返回空列表
     */
    public SqlAnalysisResultList parseTable(ResultSet rs) {
        SqlAnalysisResultList list = new SqlAnalysisResultList();
        if (rs == null)
            return list;
        try {
            // 获取结果集的元数据，用于确定列数和列名
            ResultSetMetaData md = rs.getMetaData();
            int colCount = md.getColumnCount();
            // 遍历结果集中的每一行数据，解析并封装成SqlAnalysisResult对象
            while (rs.next()) {
                SqlAnalysisResult r = new SqlAnalysisResult();
                r.setId(get(rs, colCount, "id", "select_id"));
                r.setTable(get(rs, colCount, "table"));
                r.setType(get(rs, colCount, "type"));
                r.setKey(get(rs, colCount, "key"));
                r.setKeyLen(get(rs, colCount, "key_len"));
                r.setRef(get(rs, colCount, "ref"));
                r.setFiltered(get(rs, colCount, "filtered"));
                r.setExtra(get(rs, colCount, "Extra", "extra"));
                String rowsStr = get(rs, colCount, "rows");
                trySetRows(r, rowsStr);
                list.add(r);
            }
        } catch (SQLException ignore) {
        }
        return list;
    }


    /**
     * 从ResultSet中根据候选列名查找并返回对应的字符串值
     *
     * @param rs         ResultSet对象，用于获取查询结果
     * @param colCount   列的数量，限制搜索范围
     * @param candidates 候选列名数组，按顺序进行匹配查找
     * @return 找到的第一个匹配列的字符串值，如果未找到则返回null
     * @throws SQLException 当访问ResultSet元数据或获取字符串值时发生数据库异常
     */
    private String get(ResultSet rs, int colCount, String... candidates) throws SQLException {
        // 遍历所有候选列名
        for (String c : candidates) {
            // 在指定列数范围内查找匹配的列
            for (int i = 1; i <= colCount; i++) {
                String label = rs.getMetaData().getColumnLabel(i);
                if (label == null)
                    label = rs.getMetaData().getColumnName(i);
                // 比较列标签或列名是否与候选名称匹配（忽略大小写）
                if (label != null && label.toLowerCase(Locale.ROOT).equals(c.toLowerCase(Locale.ROOT))) {
                    return rs.getString(i);
                }
            }
        }
        return null;
    }

}
