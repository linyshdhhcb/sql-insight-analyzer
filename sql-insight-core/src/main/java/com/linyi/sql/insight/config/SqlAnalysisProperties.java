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

package com.linyi.sql.insight.config;

/**
 * 核心配置（由 starter 映射注入）。
 *
 * @author linyi
 * @since 1.0.0
 */
public class SqlAnalysisProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 最大SQL长度
     */
    private int maxSqlLength = 20000;

    /**
     * 采样率（0~1）
     */
    private double samplingRate = 1.0;

    /**
     * 是否只检查一次
     */
    private boolean onlyCheckOnce = false;

    /**
     * 检查间隔（毫秒）
     */
    private long checkIntervalMs = 300_000L;

    /**
     * 是否异步输出
     */
    private boolean asyncEnabled = false;

    /**
     * 异步线程池大小
     */
    private int asyncPoolSize = 4;

    /**
     * 允许的 SQL 类型前缀（如：SELECT,UPDATE,DELETE,INSERT），为空表示不限
     */
    private java.util.List<String> allowSqlTypes = new java.util.ArrayList<>();

    /**
     * 禁止的 SQL 类型前缀（优先级高于允许），为空表示不限
     */
    private java.util.List<String> denySqlTypes = new java.util.ArrayList<>();

    /**
     * SQL ID 白名单（contains 匹配），命中则强制分析
     */
    private java.util.List<String> whitelistSqlIdContains = new java.util.ArrayList<>();

    /**
     * SQL ID 黑名单（contains 匹配），命中则跳过分析
     */
    private java.util.List<String> blacklistSqlIdContains = new java.util.ArrayList<>();

    /**
     * 是否启用 SQL 重写
     */
    private boolean rewriteEnabled = false;

    /**
     * LIMIT 收敛的最大值（rewrite 启用时生效，<=0 表示不启用该规则）
     */
    private int rewriteMaxLimit = 0;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxSqlLength() {
        return maxSqlLength;
    }

    public void setMaxSqlLength(int maxSqlLength) {
        this.maxSqlLength = maxSqlLength;
    }

    public double getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(double samplingRate) {
        this.samplingRate = samplingRate;
    }

    public boolean isOnlyCheckOnce() {
        return onlyCheckOnce;
    }

    public void setOnlyCheckOnce(boolean onlyCheckOnce) {
        this.onlyCheckOnce = onlyCheckOnce;
    }

    public long getCheckIntervalMs() {
        return checkIntervalMs;
    }

    public void setCheckIntervalMs(long checkIntervalMs) {
        this.checkIntervalMs = checkIntervalMs;
    }

    public boolean isAsyncEnabled() {
        return asyncEnabled;
    }

    public void setAsyncEnabled(boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
    }

    public int getAsyncPoolSize() {
        return asyncPoolSize;
    }

    public void setAsyncPoolSize(int asyncPoolSize) {
        this.asyncPoolSize = asyncPoolSize;
    }

    public java.util.List<String> getAllowSqlTypes() {
        return allowSqlTypes;
    }

    public void setAllowSqlTypes(java.util.List<String> allowSqlTypes) {
        this.allowSqlTypes = allowSqlTypes;
    }

    public java.util.List<String> getDenySqlTypes() {
        return denySqlTypes;
    }

    public void setDenySqlTypes(java.util.List<String> denySqlTypes) {
        this.denySqlTypes = denySqlTypes;
    }

    public java.util.List<String> getWhitelistSqlIdContains() {
        return whitelistSqlIdContains;
    }

    public void setWhitelistSqlIdContains(java.util.List<String> whitelistSqlIdContains) {
        this.whitelistSqlIdContains = whitelistSqlIdContains;
    }

    public java.util.List<String> getBlacklistSqlIdContains() {
        return blacklistSqlIdContains;
    }

    public void setBlacklistSqlIdContains(java.util.List<String> blacklistSqlIdContains) {
        this.blacklistSqlIdContains = blacklistSqlIdContains;
    }

    public boolean isRewriteEnabled() {
        return rewriteEnabled;
    }

    public void setRewriteEnabled(boolean rewriteEnabled) {
        this.rewriteEnabled = rewriteEnabled;
    }

    public int getRewriteMaxLimit() {
        return rewriteMaxLimit;
    }

    public void setRewriteMaxLimit(int rewriteMaxLimit) {
        this.rewriteMaxLimit = rewriteMaxLimit;
    }
}
