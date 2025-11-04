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

import com.linyi.sql.insight.model.SqlInsightLogDto;
import com.linyi.sql.insight.model.SqlScoreResult;
import com.linyi.sql.insight.model.SqlAnalysisResultList;

/**
 * 结果输出服务接口。
 *
 * @author linyi
 * @since 1.0.0
 */
public interface SqlScoreResultOutService {

    void publish(SqlScoreResult result, String sqlId, String sql, SqlAnalysisResultList plan);

    /**
     * 创建一个异步执行的 SqlScoreResultOutService 实例
     *
     * @param executor 用于执行异步任务的线程池执行器
     * @return 返回一个新的 SqlScoreResultOutService 实例，其 publish 方法将在指定的执行器中异步执行
     */
    default SqlScoreResultOutService async(java.util.concurrent.Executor executor) {
        SqlScoreResultOutService self = this;
        // 使用 CompletableFuture 异步执行 publish 操作
        return (result, sqlId, sql, plan) -> java.util.concurrent.CompletableFuture.runAsync(
                () -> self.publish(result, sqlId, sql, plan), executor);
    }


}
