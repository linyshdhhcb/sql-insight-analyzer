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

package com.linyi.sql.insight.model;

/**
 * SQL 执行计划解析结果的统一模型，兼容 MySQL 5.7（表格）与 8.x（JSON）格式。
 *
 * @author linyi
 * @since 1.0.0
 */
public final class SqlAnalysisResult {

    /**
     * 步骤 ID（对应 EXPLAIN 的 id 列）
     */
    private String id;

    /**
     * 表名或派生表标识（如 <derived2>）
     */
    private String table;

    /**
     * 访问类型（ALL / index / range / ref / eq_ref / const / system）
     */
    private String type;

    /**
     * 实际使用的索引名称
     */
    private String key;

    /**
     * 使用的索引长度（字节数）
     */
    private String keyLen;

    /**
     * 与索引比较的列或常量
     */
    private String ref;

    /**
     * 预估扫描行数
     */
    private Long rows;

    /**
     * 行过滤百分比（MySQL 5.7+，如 "10.00" 表示 10%）
     */
    private String filtered;

    /**
     * 额外信息（如 "Using filesort", "Using temporary"）
     */
    private String extra;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKeyLen() {
        return keyLen;
    }

    public void setKeyLen(String keyLen) {
        this.keyLen = keyLen;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Long getRows() {
        return rows;
    }

    public void setRows(Long rows) {
        this.rows = rows;
    }

    public String getFiltered() {
        return filtered;
    }

    public void setFiltered(String filtered) {
        this.filtered = filtered;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
