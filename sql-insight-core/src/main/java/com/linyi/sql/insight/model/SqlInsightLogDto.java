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

import java.util.List;

/**
 * SQL 洞察日志
 *
 * @author linyi
 * @since 1.0.0
 */
public class SqlInsightLogDto {

    /**
     * 数据库产品名称
     */
    public String dbProductName;

    /**
     * 数据库产品版本
     */
    public String dbVersion;

    /**
     * 开始时间
     */
    public long startTime;

    /**
     * 执行耗时
     */
    public long costMs;

    /**
     * 业务执行耗时
     */
    public long bizCostMs;

    /**
     * SQL ID
     */
    public String sqlId;

    /**
     *  SQL
     */
    public String sql;

    /**
     * EXPLAIN SQL
     */
    public String explainSql;

    /**
     * EXPLAIN 结果
     */
    public String explainJson;

    /**
     * SQL 执行计划解析结果
     */
    public List<SqlAnalysisResult> explainRows;

    /**
     * 分析结果
     */
    public SqlScoreResult scoreResult;

    public SqlInsightLogDto() {
    }

    public String getDbProductName() {
        return dbProductName;
    }

    public void setDbProductName(String dbProductName) {
        this.dbProductName = dbProductName;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCostMs() {
        return costMs;
    }

    public void setCostMs(long costMs) {
        this.costMs = costMs;
    }

    public long getBizCostMs() {
        return bizCostMs;
    }

    public void setBizCostMs(long bizCostMs) {
        this.bizCostMs = bizCostMs;
    }

    public String getSqlId() {
        return sqlId;
    }

    public void setSqlId(String sqlId) {
        this.sqlId = sqlId;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getExplainSql() {
        return explainSql;
    }

    public void setExplainSql(String explainSql) {
        this.explainSql = explainSql;
    }

    public String getExplainJson() {
        return explainJson;
    }

    public void setExplainJson(String explainJson) {
        this.explainJson = explainJson;
    }

    public List<SqlAnalysisResult> getExplainRows() {
        return explainRows;
    }

    public void setExplainRows(List<SqlAnalysisResult> explainRows) {
        this.explainRows = explainRows;
    }

    public SqlScoreResult getScoreResult() {
        return scoreResult;
    }

    public void setScoreResult(SqlScoreResult scoreResult) {
        this.scoreResult = scoreResult;
    }
}
