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

package com.linyi.sql.insight.score;

import com.linyi.sql.insight.model.SqlAnalysisResultList;
import com.linyi.sql.insight.model.SqlScoreResult;
import com.linyi.sql.insight.model.AnalysisLevel;
import com.linyi.sql.insight.model.SqlScoreResultDetail;
import com.linyi.sql.insight.model.SqlScoreRule;
import com.linyi.sql.insight.rule.DefaultRuleEngine;
import com.linyi.sql.insight.rule.RuleEngine;
import com.linyi.sql.insight.rule.RuleLoader;

import java.util.List;

/**
 * 评分服务：通过规则引擎评估细项并聚合总分/等级。
 *
 * @author linyi
 * @since 1.0.0
 */
public class DefaultScoreService implements ScoreService {

    private final RuleEngine ruleEngine = new DefaultRuleEngine();
    private final RuleLoader ruleLoader;

    public DefaultScoreService(RuleLoader ruleLoader) {
        this.ruleLoader = ruleLoader;
    }

    /**
     * 对SQL分析结果进行评分
     *
     * @param plan SQL分析结果列表，包含待评分的SQL执行计划信息
     * @return SqlScoreResult 评分结果对象，包含总分、风险等级和详细的评分项
     */
    @Override
    public SqlScoreResult score(SqlAnalysisResultList plan) {
        // 加载评分规则并执行评估
        List<SqlScoreRule> rules = ruleLoader.loadRules();
        List<SqlScoreResultDetail> details = ruleEngine.evaluate(plan, rules);

        // 初始化评分结果
        SqlScoreResult result = new SqlScoreResult();
        int total = 0;
        AnalysisLevel level = AnalysisLevel.OK;

        // 汇总各项评分结果，计算总分和最高风险等级
        for (SqlScoreResultDetail d : details) {
            if (d == null)
                continue;
            total += d.getScore();
            level = maxLevel(level, d.getLevel());
        }

        // 限制总分范围在0-100之间
        if (total < 0)
            total = 0;
        if (total > 100)
            total = 100;

        // 设置最终评分结果
        result.setScore(total);
        result.setLevel(level);
        result.setDetails(details);
        return result;
    }


    /**
     * 比较两个分析级别，返回级别较高的一个
     *
     * @param a 第一个分析级别
     * @param b 第二个分析级别，如果为null则使用AnalysisLevel.OK作为默认值
     * @return 返回两个分析级别中级别较高者
     */
    private static AnalysisLevel maxLevel(AnalysisLevel a, AnalysisLevel b) {
        // 如果b为null，将其设置为默认级别OK
        if (b == null)
            b = AnalysisLevel.OK;
        // 通过rank方法获取两个级别的权重值进行比较
        int ra = rank(a);
        int rb = rank(b);
        // 返回权重值较大（级别较高）的分析级别
        return ra >= rb ? a : b;
    }


    /**
     * 获取分析级别对应的优先级排名
     *
     * @param l 分析级别枚举值
     * @return 优先级排名数值，CRIT=3，WARN=2，OK=1
     */
    private static int rank(AnalysisLevel l) {
        // 根据分析级别返回对应的优先级排名
        if (l == AnalysisLevel.CRIT)
            return 3;
        if (l == AnalysisLevel.WARN)
            return 2;
        return 1; // OK
    }

}
