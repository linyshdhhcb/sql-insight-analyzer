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

package com.linyi.sql.insight.rule;

import com.linyi.sql.insight.model.SqlScoreRule;

import java.util.ArrayList;
import java.util.List;

/**
 * 简易规则加载器：提供内置默认规则，子类可覆盖从文件/HTTP 加载。
 *
 * @author linyi
 * @since 1.0.0
 */
public class RuleLoader {

    public List<SqlScoreRule> loadRules() {
        return loadDefaults();
    }

    /**
     * 加载默认的SQL评分规则列表。
     * <p>
     * 该方法用于初始化并返回一组预定义的SQL分析规则，每条规则包括：
     * - 唯一标识ID
     * - 匹配条件表达式
     * - 扣分分数
     * - 分析级别（如警告或严重）
     * - 规则描述原因
     * - 优先级顺序
     * </p>
     *
     * @return 返回一个包含多个SqlScoreRule对象的列表，每个对象代表一条默认评分规则
     */
    public List<SqlScoreRule> loadDefaults() {
        List<SqlScoreRule> list = new ArrayList<>();

        // 全表扫描规则：当执行计划类型为"ALL"时触发，属于严重问题
        SqlScoreRule fullscan = new SqlScoreRule();
        fullscan.setId("fullscan");
        fullscan.setCondition("type == \"ALL\"");
        fullscan.setScore(50);
        fullscan.setLevel(com.linyi.sql.insight.model.AnalysisLevel.CRIT);
        fullscan.setReason("全表扫描");
        fullscan.setPriority(1);
        list.add(fullscan);

        // 文件排序规则：当执行计划中extra字段包含"filesort"时触发
        SqlScoreRule filesort = new SqlScoreRule();
        filesort.setId("filesort");
        filesort.setCondition("extra contains \"filesort\"");
        filesort.setScore(20);
        filesort.setLevel(com.linyi.sql.insight.model.AnalysisLevel.WARN);
        filesort.setReason("Using filesort");
        filesort.setPriority(2);
        list.add(filesort);

        // 使用临时表规则：当执行计划中extra字段包含"temporary"时触发
        SqlScoreRule temporary = new SqlScoreRule();
        temporary.setId("temporary");
        temporary.setCondition("extra contains \"temporary\"");
        temporary.setScore(10);
        temporary.setLevel(com.linyi.sql.insight.model.AnalysisLevel.WARN);
        temporary.setReason("Using temporary");
        temporary.setPriority(3);
        list.add(temporary);

        // 未使用索引规则：当key为空时表示没有使用索引
        SqlScoreRule noindex = new SqlScoreRule();
        noindex.setId("noindex");
        noindex.setCondition("key == null || key == \"\"");
        noindex.setScore(20);
        noindex.setLevel(com.linyi.sql.insight.model.AnalysisLevel.WARN);
        noindex.setReason("未使用索引");
        noindex.setPriority(4);
        list.add(noindex);

        // 高扫描行数规则：预估扫描行数超过10万行时判定为严重问题
        SqlScoreRule rowsHigh = new SqlScoreRule();
        rowsHigh.setId("rows_high");
        rowsHigh.setCondition("rows != null && rows > 100000");
        rowsHigh.setScore(30);
        rowsHigh.setLevel(com.linyi.sql.insight.model.AnalysisLevel.CRIT);
        rowsHigh.setReason("预估扫描行数过高");
        rowsHigh.setPriority(5);
        list.add(rowsHigh);

        return list;
    }

}
