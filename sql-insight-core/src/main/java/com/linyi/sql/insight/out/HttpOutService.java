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
import com.linyi.sql.insight.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

/**
 * 简单 HTTP POST 输出实现（application/json）。
 *
 * @author linyi
 * @since 1.0.0
 */
public class HttpOutService implements SqlScoreResultOutService {

    private static final Logger log = LoggerFactory.getLogger(HttpOutService.class);

    private final String endpoint;
    private final Gson gson = new Gson();

    public HttpOutService(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 发布SQL评分结果到指定的HTTP端点
     *
     * @param result SQL评分结果对象，包含评分详情
     * @param sqlId  SQL语句的唯一标识符
     * @param sql    实际的SQL语句内容
     */
    @Override
    public void publish(SqlScoreResult result, String sqlId, String sql, SqlAnalysisResultList plan) {
        if (endpoint == null || endpoint.isEmpty() || result == null)
            return;
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(AppConstants.HTTP_CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(AppConstants.HTTP_READ_TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // 构造请求体并发送POST请求
            String body = gson.toJson(new Payload(sqlId, sql, result, plan));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            // 处理HTTP响应状态码
            int code = conn.getResponseCode();
            if (code >= AppConstants.HTTP_STATUS_5XX) {
                // 仅对 5xx 抛出异常，交给外层重试装饰器；支持 Retry-After
                long suggested = retryAfterMillis(conn);
                throw new OutputRetryException("HTTP 5xx when posting insight: code=" + code, suggested);
            } else if (code >= AppConstants.HTTP_STATUS_4XX) {
                // 4xx 不重试，记录一次
                log.warn("[SqlInsight][HTTP] non-retriable status: code={} endpoint={}", code, endpoint);
            }
            conn.disconnect();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            // I/O 等网络异常：交给外层重试
            throw new OutputRetryException(ex, 0);
        }
    }

    /**
     * 从HTTP响应头中解析Retry-After字段，返回需要等待的毫秒数
     *
     * @param conn HTTP连接对象，用于获取响应头信息
     * @return 等待的毫秒数，如果解析失败或字段不存在则返回0
     */
    private long retryAfterMillis(HttpURLConnection conn) {
        try {
            String ra = conn.getHeaderField("Retry-After");
            if (ra == null || ra.isEmpty())
                return 0;
            String trimmed = ra.trim();
            // 优先解析数字秒
            try {
                long seconds = Long.parseLong(trimmed);
                return Math.max(0, seconds * 1000);
            } catch (NumberFormatException ignore) {
            }
            // 解析 HTTP-date：RFC 1123 (Thu, 01 Jan 1970 00:00:00 GMT) 及其变体
            long now = System.currentTimeMillis();
            DateTimeFormatter[] formatters = {
                    DateTimeFormatter.RFC_1123_DATE_TIME,
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("EEE, dd MMM yyyy HH:mm:ss")
                            .optionalStart().appendPattern(" z").optionalEnd()
                            .toFormatter(Locale.ENGLISH),
                    new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("EEE, dd-MMM-yyyy HH:mm:ss")
                            .optionalStart().appendPattern(" z").optionalEnd()
                            .toFormatter(Locale.ENGLISH)
            };
            for (DateTimeFormatter fmt : formatters) {
                try {
                    ZonedDateTime zdt = ZonedDateTime.parse(trimmed, fmt);
                    long diff = zdt.toInstant().toEpochMilli() - now;
                    return Math.max(0, diff);
                } catch (DateTimeParseException ignored) {
                }
            }
            return 0;
        } catch (Exception ignore) {
            return 0;
        }
    }

    /**
     * 封装的请求体对象
     */
    static class Payload {
        final String sqlId;
        final String sql;
        final SqlScoreResult result;
        final SqlAnalysisResultList plan;

        Payload(String sqlId, String sql, SqlScoreResult result, SqlAnalysisResultList plan) {
            this.sqlId = sqlId;
            this.sql = sql;
            this.result = result;
            this.plan = plan;
        }
    }
}
