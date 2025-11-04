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

import com.linyi.sql.insight.core.SqlAnalysisInterceptor;
import com.linyi.sql.insight.config.SqlAnalysisProperties;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.linyi.sql.insight.out.LogOutService;
import com.linyi.sql.insight.out.MqOutService;
import com.linyi.sql.insight.out.HttpOutService;
import com.linyi.sql.insight.out.CustomSpiOutService;
import com.linyi.sql.insight.out.SqlScoreResultOutService;
import com.linyi.sql.insight.score.DefaultScoreService;
import com.linyi.sql.insight.score.ScoreService;
import com.linyi.sql.insight.core.MetricsRecorder;
import org.springframework.beans.factory.ObjectProvider;
import io.micrometer.core.instrument.MeterRegistry;
import com.linyi.sql.insight.starter.ui.InMemoryAnalysisStore;
import com.linyi.sql.insight.starter.ui.SqlAnalyzerController;
import com.linyi.sql.insight.starter.ui.SseHub;
import com.linyi.sql.insight.rule.RuleLoader;
import com.linyi.sql.insight.rule.FileRuleLoader;
import com.linyi.sql.insight.rule.HttpRuleLoader;

/**
 * Spring Boot 自动装配：注册拦截器与配置。
 * 
 * @author linyi
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(com.linyi.sql.insight.starter.SqlAnalysisProperties.class)
@ConditionalOnClass({ Interceptor.class, DataSource.class })
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class SqlAnalysisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "sql.analysis", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Interceptor sqlAnalysisInterceptor(SqlAnalysisProperties coreProps, SqlScoreResultOutService outService,
            Executor sqlInsightExecutor, ObjectProvider<MeterRegistry> registryProvider,
            ObjectProvider<InMemoryAnalysisStore> storeProvider,
            ObjectProvider<SseHub> sseHubProvider,
            com.linyi.sql.insight.starter.SqlAnalysisProperties props,
            ScoreService scoreService) {
        SqlAnalysisInterceptor interceptor = new SqlAnalysisInterceptor(coreProps);
        InMemoryAnalysisStore store = storeProvider.getIfAvailable();
        SseHub sseHub = sseHubProvider.getIfAvailable();
        if (store != null) {
            outService = new com.linyi.sql.insight.starter.ui.StoreOutServiceDecorator(outService, store, sseHub);
        }
        // 包装重试/退避与日志收敛（含最大总耗时与指数退避）
        outService = new com.linyi.sql.insight.starter.ui.RetryOutServiceDecorator(outService,
                Math.max(0, props.getOutputRetryMax()), Math.max(0, props.getOutputRetryBackoffMs()),
                Math.max(0, props.getOutputLogSuppressMs()), Math.max(0, props.getOutputRetryMaxTotalMs()),
                props.isOutputRetryExponential(), Math.max(0, props.getOutputRetrySleepMaxMs()));
        interceptor.setOutService(outService, sqlInsightExecutor);
        interceptor.setScoreService(scoreService);
        MeterRegistry registry = registryProvider.getIfAvailable();
        if (registry != null) {
            interceptor.setMetricsRecorder(new MetricsRecorder(registry));
        }
        // SQL 重写策略（可选）
        if (coreProps.isRewriteEnabled() && coreProps.getRewriteMaxLimit() > 0) {
            interceptor.setSqlRewriteStrategy(
                    new com.linyi.sql.insight.core.LimitCapSqlRewriteStrategy(coreProps.getRewriteMaxLimit()));
        }
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlAnalysisProperties coreSqlAnalysisProperties(
            com.linyi.sql.insight.starter.SqlAnalysisProperties mappedProps) {
        SqlAnalysisProperties core = new SqlAnalysisProperties();
        core.setEnabled(mappedProps.isEnabled());
        core.setMaxSqlLength(mappedProps.getMaxSqlLength());
        core.setSamplingRate(mappedProps.getSamplingRate());
        core.setOnlyCheckOnce(mappedProps.isOnlyCheckOnce());
        core.setCheckIntervalMs(mappedProps.getCheckIntervalMs());
        core.setAsyncEnabled(mappedProps.isAsyncEnabled());
        core.setAsyncPoolSize(mappedProps.getAsyncPoolSize());
        core.setAllowSqlTypes(mappedProps.getAllowSqlTypes());
        core.setDenySqlTypes(mappedProps.getDenySqlTypes());
        core.setWhitelistSqlIdContains(mappedProps.getWhitelistSqlIdContains());
        core.setBlacklistSqlIdContains(mappedProps.getBlacklistSqlIdContains());
        core.setRewriteEnabled(mappedProps.isRewriteEnabled());
        core.setRewriteMaxLimit(mappedProps.getRewriteMaxLimit());
        return core;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlScoreResultOutService sqlInsightOutService(com.linyi.sql.insight.starter.SqlAnalysisProperties props) {
        String model = props.getOutputModel() == null ? "LOG" : props.getOutputModel().toUpperCase();
        switch (model) {
            case "HTTP":
                return new HttpOutService(props.getOutputEndpoint());
            case "MQ":
                return new MqOutService(props.getOutputEndpoint());
            case "CUSTOM":
                return new CustomSpiOutService(props.getOutputClass());
            case "LOG":
            default:
                return new LogOutService();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ScoreService sqlInsightScoreService(RuleLoader ruleLoader) {
        return new DefaultScoreService(ruleLoader);
    }

    @Bean
    @ConditionalOnMissingBean(name = "sqlInsightExecutor")
    public Executor sqlInsightExecutor(SqlAnalysisProperties coreProps) {
        int n = Math.max(1, coreProps.getAsyncPoolSize());
        return new ThreadPoolExecutor(n, n, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000));
    }

    @Bean
    @ConditionalOnMissingBean
    public RuleLoader sqlInsightRuleLoader(com.linyi.sql.insight.starter.SqlAnalysisProperties props) {
        String mode = props.getRuleLoader() == null ? "DEFAULT" : props.getRuleLoader().toUpperCase();
        switch (mode) {
            case "FILE":
                return new FileRuleLoader(props.getRuleFile());
            case "HTTP":
                return new HttpRuleLoader(props.getRuleHttpUrl());
            case "DEFAULT":
            default:
                return new RuleLoader();
        }
    }

    // UI（可选）
    @Bean
    @ConditionalOnProperty(prefix = "sql.analysis", name = "ui-enabled", havingValue = "true")
    public InMemoryAnalysisStore sqlInsightStore() {
        return new InMemoryAnalysisStore(500);
    }

    @Bean
    @ConditionalOnProperty(prefix = "sql.analysis", name = "ui-enabled", havingValue = "true")
    public SqlAnalyzerController sqlAnalyzerController(InMemoryAnalysisStore store) {
        return new SqlAnalyzerController(store);
    }

    @Bean
    @ConditionalOnProperty(prefix = "sql.analysis", name = "ui-enabled", havingValue = "true")
    public SqlAnalyzerController.RecentApi sqlAnalyzerRecentApi(InMemoryAnalysisStore store) {
        return new SqlAnalyzerController.RecentApi(store, sseHub());
    }

    @Bean
    @ConditionalOnProperty(prefix = "sql.analysis", name = "ui-enabled", havingValue = "true")
    public SseHub sseHub() {
        return new SseHub();
    }
}
