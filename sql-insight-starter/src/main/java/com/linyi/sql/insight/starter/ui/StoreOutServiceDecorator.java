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

package com.linyi.sql.insight.starter.ui;

import com.linyi.sql.insight.model.SqlInsightLogDto;
import com.linyi.sql.insight.model.SqlScoreResult;
import com.linyi.sql.insight.model.SqlAnalysisResultList;
import com.linyi.sql.insight.out.SqlScoreResultOutService;

/**
 * 装饰器：在写入原通道前，先将结果写入内存存储用于 UI 展示。
 * 
 * @author linyi
 * @since 1.0.0
 */
public class StoreOutServiceDecorator implements SqlScoreResultOutService {

    private final SqlScoreResultOutService delegate;
    private final InMemoryAnalysisStore store;
    private final SseHub sseHub;

    public StoreOutServiceDecorator(SqlScoreResultOutService delegate, InMemoryAnalysisStore store, SseHub sseHub) {
        this.delegate = delegate;
        this.store = store;
        this.sseHub = sseHub;
    }

    /**
     * 发布SQL分析结果到存储和UI界面，并继续传递给委托服务处理
     * 
     * @param result SQL评分结果
     * @param sqlId SQL语句标识符
     * @param sql 原始SQL语句
     * @param plan SQL分析结果列表，可能为null
     */
    @Override
    public void publish(SqlScoreResult result, String sqlId, String sql, SqlAnalysisResultList plan) {
        SqlInsightLogDto dto = new SqlInsightLogDto();
        dto.sqlId = sqlId;
        dto.sql = sql;
        dto.scoreResult = result;
        dto.explainRows = plan == null ? null : plan.getResults();
        dto.startTime = System.currentTimeMillis();
        
        // 将结果存储到内存存储中，用于UI展示
        if (store != null && dto != null) {
            store.add(dto);
            if (sseHub != null) {
                sseHub.broadcast(new InMemoryAnalysisStore.Record(dto.startTime, dto));
            }
        }
        
        // 继续传递给委托服务处理
        delegate.publish(result, sqlId, sql, plan);
    }
}
