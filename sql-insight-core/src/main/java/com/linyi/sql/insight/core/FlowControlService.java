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

import com.linyi.sql.insight.config.SqlAnalysisProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基础流控：采样 + onlyCheckOnce 去重（时间窗口）。
 *
 * @author linyi
 * @since 1.0.0
 */
public class FlowControlService {

    private final SqlAnalysisProperties properties;
    private final Map<String, Long> lastCheckTimeByKey = new ConcurrentHashMap<>();

    public FlowControlService(SqlAnalysisProperties properties) {
        this.properties = properties;
    }

    /**
     * 判断是否应该分析指定的SQL
     *
     * @param sqlId SQL的唯一标识符
     * @param sql   SQL语句内容
     * @return true表示应该分析该SQL，false表示不应该分析
     */
    public boolean shouldAnalyze(String sqlId, String sql) {
        // 检查采样是否通过
        if (!samplingPass()) {
            return false;
        }

        // 如果配置为只检查一次，则检查在指定时间间隔内是否重复
        if (properties.isOnlyCheckOnce()) {
            return notDuplicateWithinInterval(sqlId, sql, properties.getCheckIntervalMs());
        }

        return true;
    }


    /**
     * 执行采样判断，根据配置的采样率决定是否通过采样
     *
     * @return true表示通过采样，false表示未通过采样
     */
    private boolean samplingPass() {
        // get采样率
        double rate = properties.getSamplingRate();
        // 采样率大于等于1时， always pass（总是通过）
        if (rate >= 1.0)
            return true;
        // 采样率小于等于0时，always fail （总是失败）
        if (rate <= 0.0)
            return false;
        // 按照配置的采样率进行随机采样
        return ThreadLocalRandom.current().nextDouble() < rate;
    }


    /**
     * 检查SQL在指定时间间隔内是否为非重复执行
     *
     * @param sqlId      SQL标识符，用于区分不同的SQL语句
     * @param sql        SQL语句内容
     * @param intervalMs 时间间隔（毫秒），用于判断是否为重复执行
     * @return 如果在指定时间间隔内没有重复执行则返回true，否则返回false
     */
    private boolean notDuplicateWithinInterval(String sqlId, String sql, long intervalMs) {
        // TODO 2025/10/30  内存泄漏风险 无清理机制 内存无限增长（改进建议：添加LRU淘汰机制 定期清理过期数据 使用WeakHashMap）
        // 构造唯一键值：sqlId和sql内容的哈希值组合
        String key = (sqlId == null ? "" : sqlId) + "#" + safeHash(sql);
        long now = System.currentTimeMillis();
        // 记录当前时间并获取上次检查时间
        Long last = lastCheckTimeByKey.put(key, now);
        if (last == null)
            return true;
        // 判断距离上次执行的时间间隔是否超过指定阈值
        return now - last >= intervalMs;
    }


    /**
     * 安全地计算字符串的哈希值并返回十六进制表示
     *
     * @param s 输入的字符串，可以为null
     * @return 字符串哈希值的十六进制表示，如果输入为null则返回"0"
     */
    private String safeHash(String s) {
        // 处理空值情况，避免空指针异常
        if (s == null)
            return "0";
        // 将字符串的哈希码转换为十六进制字符串
        return Integer.toHexString(s.hashCode());
    }

}
