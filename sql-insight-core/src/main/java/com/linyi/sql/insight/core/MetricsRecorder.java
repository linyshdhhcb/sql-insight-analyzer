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

package com.linyi.sql.insight.core;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

import com.linyi.sql.insight.util.AppConstants;

/**
 * Micrometer 指标记录器（可选，无注册表时为 no-op）。
 *
 * @author linyi
 * @since 1.0.0
 */
public class MetricsRecorder {

    // 允许为 null
    private final MeterRegistry registry;

    public MetricsRecorder(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * 记录SQL执行成功的指标数据
     *
     * @param app   应用名称，用于标识哪个应用执行的SQL
     * @param sqlId SQL语句的唯一标识符
     * @param level SQL执行的级别或重要程度分类
     */
    public void recordSuccess(String app, String sqlId, String level) {
        // 如果注册表为空，则直接返回不进行记录
        if (registry == null)
            return;
        // 增加成功计数器指标，包含应用、SQL ID和级别标签
        registry.counter(AppConstants.METRIC_SUCCESS, "app", safe(app), "sqlId", safe(sqlId), "level",
                safe(level)).increment();
    }


    /**
     * 记录失败事件的计数器统计
     *
     * @param errorSimpleName 错误的简单名称，用于标识具体的错误类型
     */
    public void recordFailure(String errorSimpleName) {
        // 如果注册表为空，则直接返回不进行统计
        if (registry == null)
            return;
        // 增加失败计数器，使用错误名称作为标签进行分类统计
        registry.counter(AppConstants.METRIC_FAILURE, "error", safe(errorSimpleName)).increment();
    }


    /**
     * 记录SQL执行耗时指标
     *
     * @param app   应用名称，用于标识不同的应用实例
     * @param sqlId SQL语句的唯一标识符
     * @param nanos 执行耗时，单位为纳秒
     */
    public void recordDuration(String app, String sqlId, long nanos) {
        // 如果监控注册中心未初始化，则直接返回
        if (registry == null)
            return;

        // 构建并注册耗时监控指标，记录SQL执行时间
        Timer.builder(AppConstants.METRIC_DURATION)
                .tags("app", safe(app), "sqlId", safe(sqlId))
                .register(registry)
                .record(nanos, TimeUnit.NANOSECONDS);
    }


    /**
     * 安全地处理字符串，避免空指针异常
     *
     * @param v 待处理的字符串，可能为null
     * @return 如果输入为null则返回空字符串，否则返回原字符串
     */
    private String safe(String v) {
        return v == null ? "" : v;
    }

}
