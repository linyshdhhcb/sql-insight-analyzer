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

package com.linyi.sql.insight.out;

import com.google.gson.Gson;
import com.linyi.sql.insight.model.SqlScoreResult;
import com.linyi.sql.insight.model.SqlAnalysisResultList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志通道输出实现。
 *
 * @author linyi
 * @since 1.0.0
 */
public class LogOutService implements SqlScoreResultOutService {

    private static final Logger log = LoggerFactory.getLogger(LogOutService.class);
    private static final Gson gson = new Gson();

    /**
     * 发布SQL评分结果到日志系统
     *
     * @param result SQL评分结果对象，包含评分等级、分数和详细信息
     * @param sqlId  SQL语句的唯一标识符
     * @param sql    原始SQL语句内容
     */
    @Override
    public void publish(SqlScoreResult result, String sqlId, String sql, SqlAnalysisResultList plan) {
        if (result == null) {
            return;
        }
        // 简单结构化日志，后续可切换为 JSON
        int planSize = plan == null || plan.getResults() == null ? 0 : plan.getResults().size();
        String resultJson = gson.toJson(result);
        log.info("[SqlInsight] sqlId={}, result={}, planSize={}, sql={}",
                sqlId,
                resultJson,
                planSize,
                sql);
    }

}
