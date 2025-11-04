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

package com.linyi.sql.insight.util;

/**
 * 全局常量汇总。
 *
 * @author linyi
 * @since 1.0.0
 */
public final class AppConstants {
    private AppConstants() {
    }

    public static final String METRIC_SUCCESS = "sql_analysis_success_total";
    public static final String METRIC_FAILURE = "sql_analysis_failure_total";
    public static final String METRIC_DURATION = "sql_analysis_duration_ms";

    /**
     * HTTP 连接超时（毫秒）
     */
    public static final int HTTP_CONNECT_TIMEOUT_MS = 5000;
    /**
     * HTTP 读取超时（毫秒）
     */
    public static final int HTTP_READ_TIMEOUT_MS = 5000;
    /**
     * HTTP 4xx 状态码阈值（>=此值视为客户端错误，不重试）
     */
    public static final int HTTP_STATUS_4XX = 400;
    /**
     * HTTP 5xx 状态码阈值（>=此值视为服务端错误，可重试）
     */
    public static final int HTTP_STATUS_5XX = 500;
    /**
     * 指数退避最大单次睡眠默认上限（毫秒）
     */
    public static final long DEFAULT_RETRY_SLEEP_MAX_MS = 30000L;
}
