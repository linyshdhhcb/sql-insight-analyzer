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
import com.linyi.sql.insight.util.LogLimiterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 输出重试 + 退避 + 日志收敛装饰器。
 *
 * @author linyi
 * @since 1.0.0
 */
public class RetryOutServiceDecorator implements SqlScoreResultOutService {

    private static final Logger log = LoggerFactory.getLogger(RetryOutServiceDecorator.class);

    private final SqlScoreResultOutService delegate;
    private final int retryMax;
    private final long backoffMs;
    private final LogLimiterUtils limiter;
    private final long maxTotalMs;
    private final boolean exponential;
    private final long maxSingleSleepMs;

    public RetryOutServiceDecorator(SqlScoreResultOutService delegate, int retryMax, long backoffMs, long suppressMs) {
        this(delegate, retryMax, backoffMs, suppressMs, 0, false);
    }

    public RetryOutServiceDecorator(SqlScoreResultOutService delegate, int retryMax, long backoffMs, long suppressMs,
            long maxTotalMs, boolean exponential) {
        this.delegate = delegate;
        this.retryMax = Math.max(0, retryMax);
        this.backoffMs = Math.max(0, backoffMs);
        this.limiter = new LogLimiterUtils(Math.max(0, suppressMs));
        this.maxTotalMs = Math.max(0, maxTotalMs);
        this.exponential = exponential;
        this.maxSingleSleepMs = 0;
    }

    public RetryOutServiceDecorator(SqlScoreResultOutService delegate, int retryMax, long backoffMs, long suppressMs,
            long maxTotalMs, boolean exponential, long maxSingleSleepMs) {
        this.delegate = delegate;
        this.retryMax = Math.max(0, retryMax);
        this.backoffMs = Math.max(0, backoffMs);
        this.limiter = new LogLimiterUtils(Math.max(0, suppressMs));
        this.maxTotalMs = Math.max(0, maxTotalMs);
        this.exponential = exponential;
        this.maxSingleSleepMs = Math.max(0, maxSingleSleepMs);
    }

    @Override
    public void publish(SqlScoreResult result, String sqlId, String sql, SqlAnalysisResultList plan) {
        int attempt = 0;
        long started = System.currentTimeMillis();
        while (true) {
            try {
                delegate.publish(result, sqlId, sql, plan);
                return;
            } catch (Throwable ex) {
                attempt++;
                String key = ex.getClass().getName();
                if (limiter.allow(key)) {
                    log.warn("[SqlInsight] outService failed: attempt={}, ex={}", attempt, ex.toString());
                }
                if (attempt > retryMax) {
                    return;
                }
                if (maxTotalMs > 0 && System.currentTimeMillis() - started >= maxTotalMs) {
                    return;
                }
                long sleep = backoffMs;
                if (ex instanceof com.linyi.sql.insight.out.OutputRetryException) {
                    long suggested = ((com.linyi.sql.insight.out.OutputRetryException) ex).getSuggestedBackoffMs();
                    if (suggested > 0)
                        sleep = suggested;
                }
                if (exponential) {
                    sleep = backoffMs * (1L << Math.max(0, attempt - 1));
                }
                // 随机抖动 ±10%
                long jitter = (long) (sleep * 0.1);
                if (jitter > 0) {
                    long delta = (long) (Math.random() * jitter);
                    sleep = sleep - jitter / 2 + delta;
                }
                try {
                    if (maxSingleSleepMs > 0 && sleep > maxSingleSleepMs)
                        sleep = maxSingleSleepMs;
                    Thread.sleep(sleep);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
