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

import com.linyi.sql.insight.model.SqlScoreResult;
import com.linyi.sql.insight.model.SqlAnalysisResultList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQ 输出占位：当前仅记录日志，后续接入具体 MQ 客户端。
 *
 * @author linyi
 * @since 1.0.0
 */
public class MqOutService implements SqlScoreResultOutService {

    private static final Logger log = LoggerFactory.getLogger(MqOutService.class);
    private final String topic;

    public MqOutService(String topic) {
        this.topic = topic;
    }

    /**
     * 发布SQL评分结果到消息队列
     *
     * @param result SQL评分结果对象，包含评分等级和分数等信息
     * @param sqlId  SQL语句的唯一标识符
     * @param sql    完整的SQL语句内容
     */
    @Override
    public void publish(SqlScoreResult result, String sqlId, String sql, SqlAnalysisResultList plan) {
        // 记录SQL评分结果日志，包含消息队列主题、评分等级、分数和SQLID等关键信息
        int planSize = plan == null || plan.getResults() == null ? 0 : plan.getResults().size();
        log.info("[SqlInsight][MQ:{}] level={}, score={}, planSize={}, sqlId={}", topic,
                result != null ? result.getLevel() : null,
                result != null ? result.getScore() : null,
                planSize,
                sqlId);
    }

}
