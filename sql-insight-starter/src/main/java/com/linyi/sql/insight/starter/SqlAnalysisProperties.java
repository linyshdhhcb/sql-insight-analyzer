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

package com.linyi.sql.insight.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Starter 侧的外部化配置入口。
 *
 * @author linyi
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "sql.analysis")
public class SqlAnalysisProperties {

    /**
     * 是否启用 SQL 分析
     */
    private boolean enabled = true;

    /**
     * 最大SQL长度（截断）
     */
    private int maxSqlLength = 20000;

    /**
     * 采样率 0~1
     */
    private double samplingRate = 1.0;

    /**
     * 仅在窗口内检查一次
     */
    private boolean onlyCheckOnce = false;

    /**
     * 去重窗口（毫秒）
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
     * 输出模式：LOG/MQ/HTTP/CUSTOM
     */
    private String outputModel = "LOG";

    /**
     * HTTP 端点或 MQ 主题等参数
     */
    private String outputEndpoint = "";

    /**
     * 自定义实现类（当 outputModel=CUSTOM 时）
     */
    private String outputClass = "";

    /**
     * 输出重试次数
     */
    private int outputRetryMax = 2;

    /**
     * 输出重试退避（毫秒）
     */
    private long outputRetryBackoffMs = 500;

    /**
     * 输出错误日志收敛窗口（毫秒）
     */
    private long outputLogSuppressMs = 5000;

    /**
     * 输出重试最大总耗时（毫秒），<=0 表示不限制
     */
    private long outputRetryMaxTotalMs = 0;

    /**
     * 输出重试是否使用指数退避（true：backoff*2^attempt）
     */
    private boolean outputRetryExponential = false;

    /**
     * 输出重试单次最大睡眠（毫秒），<=0 不限制
     */
    private long outputRetrySleepMaxMs = com.linyi.sql.insight.util.AppConstants.DEFAULT_RETRY_SLEEP_MAX_MS;

    /**
     * UI 开关
     */
    private boolean uiEnabled = false;

    /**
     * 规则加载：DEFAULT/FILE/HTTP
     */
    private String ruleLoader = "DEFAULT";

    /**
     * 规则文件位置：classpath: 或 文件路径
     */
    private String ruleFile = "classpath:sql-analysis-rules.properties";

    /**
     * 规则 HTTP 端点
     */
    private String ruleHttpUrl = "";

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
     * LIMIT 收敛的最大值，<=0 不启用该规则
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

    public String getOutputModel() {
        return outputModel;
    }

    public void setOutputModel(String outputModel) {
        this.outputModel = outputModel;
    }

    public String getOutputEndpoint() {
        return outputEndpoint;
    }

    public void setOutputEndpoint(String outputEndpoint) {
        this.outputEndpoint = outputEndpoint;
    }

    public String getOutputClass() {
        return outputClass;
    }

    public void setOutputClass(String outputClass) {
        this.outputClass = outputClass;
    }

    public int getOutputRetryMax() {
        return outputRetryMax;
    }

    public void setOutputRetryMax(int outputRetryMax) {
        this.outputRetryMax = outputRetryMax;
    }

    public long getOutputRetryBackoffMs() {
        return outputRetryBackoffMs;
    }

    public void setOutputRetryBackoffMs(long outputRetryBackoffMs) {
        this.outputRetryBackoffMs = outputRetryBackoffMs;
    }

    public long getOutputLogSuppressMs() {
        return outputLogSuppressMs;
    }

    public void setOutputLogSuppressMs(long outputLogSuppressMs) {
        this.outputLogSuppressMs = outputLogSuppressMs;
    }

    public long getOutputRetryMaxTotalMs() {
        return outputRetryMaxTotalMs;
    }

    public void setOutputRetryMaxTotalMs(long outputRetryMaxTotalMs) {
        this.outputRetryMaxTotalMs = outputRetryMaxTotalMs;
    }

    public boolean isOutputRetryExponential() {
        return outputRetryExponential;
    }

    public void setOutputRetryExponential(boolean outputRetryExponential) {
        this.outputRetryExponential = outputRetryExponential;
    }

    public long getOutputRetrySleepMaxMs() {
        return outputRetrySleepMaxMs;
    }

    public void setOutputRetrySleepMaxMs(long outputRetrySleepMaxMs) {
        this.outputRetrySleepMaxMs = outputRetrySleepMaxMs;
    }

    public boolean isUiEnabled() {
        return uiEnabled;
    }

    public void setUiEnabled(boolean uiEnabled) {
        this.uiEnabled = uiEnabled;
    }

    public String getRuleLoader() {
        return ruleLoader;
    }

    public void setRuleLoader(String ruleLoader) {
        this.ruleLoader = ruleLoader;
    }

    public String getRuleFile() {
        return ruleFile;
    }

    public void setRuleFile(String ruleFile) {
        this.ruleFile = ruleFile;
    }

    public String getRuleHttpUrl() {
        return ruleHttpUrl;
    }

    public void setRuleHttpUrl(String ruleHttpUrl) {
        this.ruleHttpUrl = ruleHttpUrl;
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
