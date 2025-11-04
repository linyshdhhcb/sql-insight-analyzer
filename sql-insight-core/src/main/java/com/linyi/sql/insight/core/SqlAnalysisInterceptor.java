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

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Properties;

import com.linyi.sql.insight.extract.SqlExtractor;
import com.linyi.sql.insight.analysis.ExplainExecutor;
import com.linyi.sql.insight.model.SqlAnalysisResultList;
import com.linyi.sql.insight.model.SqlScoreResult;
import com.linyi.sql.insight.out.LogOutService;
import com.linyi.sql.insight.out.SqlScoreResultOutService;
import com.linyi.sql.insight.score.DefaultScoreService;
import com.linyi.sql.insight.score.ScoreService;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import com.linyi.sql.insight.config.SqlAnalysisProperties;
import com.linyi.sql.insight.model.SqlInsightLogDto;
import com.google.gson.Gson;
import java.sql.ResultSet;
import com.linyi.sql.insight.core.ApplicationContextProvider;
import com.linyi.sql.insight.core.SqlInsightLogEvent;

/**
 * 最小可运行的 MyBatis 拦截器骨架：
 * - 拦截 StatementHandler#prepare，提取最终 SQL 并打印日志
 * - 后续在此处编排提取/EXPLAIN/规则/评分/输出
 *
 * @author linyi
 * @since 1.0.0
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class })
})
public class SqlAnalysisInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SqlAnalysisInterceptor.class);

    private final SqlExtractor sqlExtractor = new SqlExtractor();
    private final ExplainExecutor explainExecutor = new ExplainExecutor();
    private ScoreService scoreService;
    private SqlScoreResultOutService outService = new LogOutService();
    private final SqlAnalysisProperties properties;
    private final FlowControlService flowControlService;
    private MetricsRecorder metricsRecorder;
    private SqlRewriteStrategy sqlRewriteStrategy;

    public SqlAnalysisInterceptor(SqlAnalysisProperties properties) {
        this.properties = properties;
        this.flowControlService = new FlowControlService(properties);
    }

    public void setOutService(SqlScoreResultOutService outService, java.util.concurrent.Executor executor) {
        if (outService == null)
            return;
        if (properties.isAsyncEnabled() && executor != null) {
            this.outService = outService.async(executor);
        } else {
            this.outService = outService;
        }
    }

    public void setMetricsRecorder(MetricsRecorder metricsRecorder) {
        this.metricsRecorder = metricsRecorder;
    }

    public void setScoreService(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    public void setSqlRewriteStrategy(SqlRewriteStrategy sqlRewriteStrategy) {
        this.sqlRewriteStrategy = sqlRewriteStrategy;
    }

    /**
     * 拦截 MyBatis 的 SQL 执行过程，用于分析 SQL 语句并进行性能评估。
     * <p>
     * 主要功能包括：
     * <p>
     * 1. 提取并清洗 SQL 语句；
     * 2. 可选地重写 SQL；
     * 3. 对符合条件的 SELECT/UPDATE/DELETE 语句执行 EXPLAIN 分析；
     * 4. 根据规则评分并将结果发布；
     * 5. 记录指标信息（如耗时、成功与否等）。
     *
     * @param invocation 调用上下文对象，包含目标处理器及参数
     * @return 原始调用的结果
     * @throws Throwable 若在拦截过程中发生异常则抛出
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取目标对象
        Object target = invocation.getTarget();

        // 仅拦截 StatementHandler
        if (target instanceof StatementHandler) {

            // 获取 StatementHandler
            StatementHandler statementHandler = (StatementHandler) target;
            long start = System.nanoTime();
            String sqlId = null;
            // 获取 SQL ID
            BoundSql boundSql = statementHandler.getBoundSql();
            // 获取 SQL
            String sql = sqlExtractor.extractAndSanitize(boundSql, properties.getMaxSqlLength());

            // SQL 重写（若启用）
            if (sql != null && sqlRewriteStrategy != null) {
                try {
                    String newSql = sqlRewriteStrategy.rewrite(null, sql);
                    if (newSql != null && !newSql.equals(sql)) {
                        java.lang.reflect.Field f = BoundSql.class.getDeclaredField("sql");
                        f.setAccessible(true);
                        f.set(boundSql, newSql);
                        sql = newSql;
                    }
                } catch (Throwable ignore) {
                }
            }

            if (sql != null) {
                log.debug("[SqlInsight] intercepted SQL: {}", sql);

                // 获取SQL前缀以判断类型
                String head = sql.length() > 12 ? sql.substring(0, 12) : sql;
                String headTrim = head.trim().toLowerCase();

                // 过滤不需要处理的SQL类型
                if (!passTypeFilter(headTrim)) {
                    return invocation.proceed();
                }

                // 判断是否需要执行EXPLAIN分析
                boolean shouldExplain = headTrim.startsWith("select") || headTrim.startsWith("update")
                        || headTrim.startsWith("delete");

                if (shouldExplain) {
                    sqlId = resolveSqlId(statementHandler);

                    // ID白名单过滤
                    if (!passIdList(sqlId)) {
                        return invocation.proceed();
                    }

                    // 流控检查：决定是否跳过分析
                    if (!flowControlService.shouldAnalyze(sqlId, sql)) {
                        Object ret = invocation.proceed();
                        if (metricsRecorder != null) {
                            metricsRecorder.recordDuration(null, sqlId, System.nanoTime() - start);
                        }
                        return ret;
                    }

                    // 获取数据库连接以便执行EXPLAIN
                    Object[] args = invocation.getArgs();
                    Connection conn = (args != null && args.length > 0 && args[0] instanceof Connection)
                            ? (Connection) args[0]
                            : null;

                    // --- 封装Dto ---
                    SqlInsightLogDto dto = new SqlInsightLogDto();
                    dto.startTime = System.currentTimeMillis();
                    dto.sqlId = sqlId;
                    dto.sql = sql;

                    long bizStart = System.nanoTime();
                    Object ret = null;
                    Throwable bizEx = null;
                    try {
                        ret = invocation.proceed();
                    } catch (Throwable e) {
                        bizEx = e;
                        throw e;
                    } finally {
                        dto.bizCostMs = (System.nanoTime() - bizStart) / 1_000_000L;
                    }
                    long anaStart = System.nanoTime();

                    if (conn != null) {
                        try {
                            java.sql.DatabaseMetaData meta = conn.getMetaData();
                            dto.dbProductName = meta == null ? null : meta.getDatabaseProductName();
                            dto.dbVersion = meta == null ? null : meta.getDatabaseProductVersion();
                        } catch (Throwable e) {
                        }
                        String explainSql = sqlExtractor.extractForExplain(boundSql, properties.getMaxSqlLength());
                        dto.explainSql = explainSql;
                        // 拿EXPLAIN JSON与表格plan
                        String explainJson = null;
                        SqlAnalysisResultList plan = null;
                        try {
                            java.sql.Statement explainStmt = conn.createStatement();
                            try (ResultSet rs = explainStmt.executeQuery("EXPLAIN FORMAT=JSON " + explainSql)) {
                                if (rs.next()) {
                                    explainJson = rs.getString(1);
                                }
                            }
                        } catch (Throwable e) {
                        }
                        if (explainJson != null) {
                            dto.explainJson = explainJson;
                        }
                        plan = explainExecutor.explain(conn, explainSql != null ? explainSql : sql);
                        dto.explainRows = plan != null ? plan.getResults() : null;
                        // 评分
                        SqlScoreResult scoreResult = ensureScoreService().score(plan);
                        dto.scoreResult = scoreResult;
                        // 标准通道原逻辑
                        if (log.isDebugEnabled()) {
                            log.debug("[SqlInsight] explain sql: {}", explainSql);
                            log.debug("[SqlInsight] explain entries: {}", plan == null ? 0 : plan.getResults().size());
                        }

                        // 发往 outService 保证兼容原有通道
                        outService.publish(scoreResult, sqlId, sql, plan);
                        // 新增: 存入UI内存与SSE
                        // 在分析流程末尾仅发布事件，不直接操作store或sse
                        try {
                            ApplicationContextProvider.get().publishEvent(new SqlInsightLogEvent(this, dto));
                        } catch (Throwable ignore) {
                        }
                    }
                    dto.costMs = (System.nanoTime() - start) / 1_000_000L;

                    // --- 输出一份完整json ---
                    log.info("[SqlInsight-FULL] {}", new Gson().toJson(dto));
                    // 通过Spring事件广播全量分析dto
                    return ret;
                }
            }

            // TODO 预留：后续 1) SQL 类型过滤 2) 执行 EXPLAIN 并解析 3) 规则评分与输出
        }

        return invocation.proceed();
    }

    /**
     * 创建代理对象的方法
     *
     * @param target 被代理的目标对象
     * @return 返回包装后的代理对象
     */
    @Override
    public Object plugin(Object target) {
        // 使用Plugin工具类创建代理对象，将目标对象和当前拦截器进行包装
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 预留：可通过 MyBatis 插件属性传递开关等
    }

    /**
     * 解析SQL ID
     *
     * @param statementHandler Statement处理器对象
     * @return 返回解析到的SQL ID，如果解析失败则返回null
     */
    private String resolveSqlId(StatementHandler statementHandler) {
        try {
            // 通过MetaObject反射获取MappedStatement对象
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            Object msObj = metaObject.getValue("delegate.mappedStatement");
            if (msObj instanceof MappedStatement) {
                return ((MappedStatement) msObj).getId();
            }
        } catch (Throwable ignore) {
            // 忽略异常情况，返回null
        }
        return null;
    }

    /**
     * 检查SQL语句类型是否通过过滤器
     *
     * @param headLower SQL语句头部的小写形式
     * @return 如果通过过滤器返回true，否则返回false
     */
    private boolean passTypeFilter(String headLower) {
        // 检查是否在拒绝列表中
        java.util.List<String> deny = properties.getDenySqlTypes();
        if (deny != null) {
            for (String t : deny) {
                if (startsWithIgnoreCase(headLower, t))
                    return false;
            }
        }

        // 检查是否在允许列表中
        java.util.List<String> allow = properties.getAllowSqlTypes();
        if (allow == null || allow.isEmpty())
            return true;
        for (String t : allow) {
            if (startsWithIgnoreCase(headLower, t))
                return true;
        }
        return false;
    }

    /**
     * 检查SQL ID是否通过黑白名单过滤
     *
     * @param sqlId 待检查的SQL ID
     * @return true表示通过过滤，false表示被过滤掉
     */
    private boolean passIdList(String sqlId) {
        if (sqlId == null)
            return true;

        // 检查黑名单过滤
        java.util.List<String> black = properties.getBlacklistSqlIdContains();
        if (black != null) {
            for (String k : black) {
                if (k != null && !k.isEmpty() && sqlId.contains(k))
                    return false;
            }
        }

        // 检查白名单过滤
        java.util.List<String> white = properties.getWhitelistSqlIdContains();
        if (white != null && !white.isEmpty()) {
            for (String k : white) {
                if (k != null && !k.isEmpty() && sqlId.contains(k))
                    return true;
            }
            return false; // 有白名单但未命中
        }
        return true;
    }

    /**
     * 判断字符串是否以指定类型开头（忽略大小写）
     *
     * @param headLower 已经转换为小写的头部字符串
     * @param type      要匹配的类型字符串
     * @return 如果headLower以type开头（忽略大小写）则返回true，否则返回false
     */
    private boolean startsWithIgnoreCase(String headLower, String type) {
        // 检查type参数是否为null
        if (type == null)
            return false;

        // 将type字符串去除空格并转换为小写
        String t = type.trim().toLowerCase();

        // 检查处理后的字符串是否为空
        if (t.isEmpty())
            return false;

        // 判断headLower是否以处理后的字符串开头
        return headLower.startsWith(t);
    }

    /**
     * 确保获取一个可用的评分服务实例
     * <p>
     * 如果当前评分服务实例为空，则创建一个新的默认评分服务实例，
     * 该实例使用规则加载器进行初始化。
     * </p>
     *
     * @return 返回评分服务实例
     */
    private ScoreService ensureScoreService() {
        // 如果评分服务实例尚未初始化，则创建新的默认评分服务实例
        if (this.scoreService == null) {
            this.scoreService = new DefaultScoreService(new com.linyi.sql.insight.rule.RuleLoader());
        }
        return this.scoreService;
    }

}
