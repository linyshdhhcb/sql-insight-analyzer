# SQL Insight Analyzer
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
![Java Version](https://img.shields.io/badge/java-8%2B-blue.svg)

一个轻量级、高性能的 MyBatis / MyBatis-Plus SQL 性能分析工具，专为 MySQL 设计。通过实时拦截 SQL 执行并结合 EXPLAIN 分析，提供直观的风险评分与可视化反馈。

目标**: 帮助开发者及时发现潜在性能瓶颈，提升数据库查询质量。

## 特性

- **无侵入监控** - 作为 MyBatis 插件无缝接入，无需修改业务代码
- **版本智能适配** - 自动识别 MySQL 5.7/8.x 并切换最佳 EXPLAIN 模式
-  **规则驱动评分** - 内置 DSL 表达式引擎，支持灵活定制分析规则
-  **多通道输出** - 支持 LOG / HTTP / MQ / 自定义(SPI) 输出方式
-  **指标监控** - 集成 Micrometer，提供成功/失败/耗时指标
-  **可视化界面** - 内置 Web UI，支持实时查看分析结果
-  **SQL 重写** - 可选的 LIMIT 上限收敛策略，防止意外大查询

##  兼容性

- **JDK**: 8+（已在 8/17 下验证）
- **数据库**: MySQL 5.7、8.x（MariaDB 多数场景可兼容）
- **框架**: MyBatis 3.5+、MyBatis-Plus 3.5+

##  安装

Maven 项目中引入依赖：

```xml
<dependency>
  <groupId>com.linyi.sql</groupId>
  <artifactId>sql-insight-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

##  配置

### 最小配置

```yaml
sql:
  analysis:
    enabled: true
```

### 推荐配置

```yaml
sql:
  analysis:
    enabled: true
    # 基础配置
    max-sql-length: 20000
    sampling-rate: 0.5 # 50% 采样
    only-check-once: true
    check-interval-ms: 300000
    # EXPLAIN 与评分输出
    async-enabled: true
    async-pool-size: 4
    output-model: LOG # LOG | HTTP | MQ | CUSTOM
    # output-endpoint: http://localhost:8081/sql-insight   # HTTP/MQ 参数
    # output-class: com.example.CustomOutService           # 自定义实现
    # 规则配置
    rule-loader: FILE # DEFAULT | FILE | HTTP
    rule-file: classpath:sql-analysis-rules.properties
    # rule-http-url: http://host/rules.properties
    # UI（可选）
    ui-enabled: true # 开启页面 /sql-analyzer 与 SSE 实时推送
    # SQL 重写（可选）
    rewrite-enabled: true
    rewrite-max-limit: 50 # 将 "limit N" 超过 50 的查询收敛为 50
    # 过滤（可选）
    allow-sql-types: [SELECT, UPDATE, DELETE]
    deny-sql-types: []
    whitelist-sql-id-contains: []
    blacklist-sql-id-contains: []
```

##  快速开始

### 1. 准备测试数据库

```
CREATE DATABASE IF NOT EXISTS `test` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `test`;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `age` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `users` (`name`, `age`) VALUES
('Alice', 23),
('Bob', 31),
('Charlie', 27),
('Diana', 29),
('Eve', 26);
```

### 2. 运行示例项目

#### MyBatis 示例

```bash
mvn -pl sql-analysis-demo-mybatis -am -DskipTests package
java -jar sql-analysis-demo-mybatis/target/sql-analysis-demo-mybatis-1.0.0.jar
```

#### MyBatis-Plus 示例

```bash
mvn -pl sql-analysis-demo-mybatisplus -am -DskipTests package
java -jar sql-analysis-demo-mybatisplus/target/sql-analysis-demo-mybatisplus-1.0.0.jar
```

### 3. 测试接口

- MyBatis：GET `/u/{id}`、`/u/search?q=kw`、`/u/incr/{id}`
- MyBatis-Plus：GET `/mp/u/{id}`、`/mp/u/search?q=kw`

启动后触发任意 MyBatis 查询，即可在日志中看到评分输出。

##  规则定义

通过 properties 文件自定义评分规则：

```
# 全表扫描
rule.fullscan.condition=type == "ALL"
rule.fullscan.priority=1
rule.fullscan.score=50
rule.fullscan.level=CRIT
rule.fullscan.reason=全表扫描

# 文件排序
rule.filesort.condition=extra contains "filesort"
rule.filesort.priority=2
rule.filesort.score=20
rule.filesort.level=WARN
rule.filesort.reason=Using filesort
```

##  指标监控 (Micrometer)

- `sql_analysis_success_total{app,sqlId,level}` - 成功分析计数
- `sql_analysis_failure_total{error}` - 失败分析计数
- `sql_analysis_duration_ms{app,sqlId}` - 分析耗时分布

##  Web UI 界面

开启 UI 功能后，可通过 `/sql-analyzer` 访问可视化界面：

```yaml
sql:
  analysis:
    ui-enabled: true
```

UI 特性：
- 实时查看 SQL 分析结果
- 支持筛选分页和导出 CSV
- 通过 SSE 实时推送新结果

##  MyBatis-Plus 集成

当项目中存在 `com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor` 时，Starter 会自动通过 `MybatisPlusSqlInsightConfiguration` 将 `SqlAnalysisInterceptor` 排在 MP 插件之后（如分页、租户、动态表名等），以确保被分析的是最终 SQL。

无需手动调整顺序。如需自定义，可参考 `com.linyi.sql.insight.mp.MpPluginOrderHelper` 进行顺序重排。

##  项目结构

```
sql-insight-analyzer/
├── sql-insight-core/                  # 核心库（拦截、EXPLAIN、规则、评分、输出基类等）
│   └── src/main/java/com/linyi/sql/insight/
│       ├── core/                      # 拦截器、流程编排、指标
│       ├── analysis/                  # ExplainExecutor/Parser/VersionDetector
│       ├── rule/                      # 规则装载与引擎（默认/文件/HTTP）
│       ├── score/                     # 评分服务
│       ├── out/                       # 输出通道（LOG/HTTP/MQ/SPI）
│       ├── model/                     # 统一模型（EXPLAIN 结果、评分结果/规则）
│       ├── util/                      # 工具/表达式
│       └── mp/                        # MyBatis-Plus 顺序助手
├── sql-insight-starter/               # Spring Boot Starter（自动装配、UI、属性映射）
│   ├── src/main/java/com/linyi/sql/insight/starter/
│   │   ├── SqlAnalysisAutoConfiguration.java
│   │   └── MybatisPlusSqlInsightConfiguration.java
│   └── src/main/resources/
│       ├── META-INF/spring.factories  # 自动装配入口
│       ├── templates/sql-analyzer.html# 简易 UI
│       └── sql-analysis-rules.properties # 示例规则
├── sql-analysis-demo-mybatis/         # MyBatis 示例（自动建库表&样例数据）
│   └── src/main/resources/{application.yml,schema.sql,data.sql}
├── sql-analysis-demo-mybatisplus/     # MyBatis-Plus 示例（自动建库表&样例数据）
│   └── src/main/resources/{application.yml,schema.sql,data.sql}
└── README.md                          # 文档
```

##  对比与定位

| 项目                           |           在线拦截 |         EXPLAIN 解析 |              规则评分 |       索引建议 |   维护活跃 | 技术栈 |
| ------------------------------ | -----------------: | -------------------: | --------------------: | -------------: | ---------: | ------ |
| 本项目（SQL Insight Analyzer） |                 ✅ | ✅（JSON/表格+回退） |      ✅（可扩展 DSL） |   ❌（规划中） |         ✅ | Java   |
| Alibaba Druid                  |     ❌（监控为主） |                   ❌ |        ✅（静态统计） |             ❌ |         ✅ | Java   |
| Meituan SQLAdvisor             | ✅（离线分析为主） |                   ✅ |                    ❌ | ✅（索引建议） | ✅（停更） | C++    |
| Apache ShardingSphere          |                 ❌ |                   ❌ | ✅（间接：路由/审计） |             ❌ |         ✅ | Java   |
| MyBatis-Mate                   |                 ❌ |                   ❌ |  ✅（间接：审计扩展） |             ❌ |         ✅ | Java   |
| JSqlParser                     |                 ❌ |       ⚠️（仅语法层） |          ⚠️（需自研） |             ❌ |         ✅ | Java   |

说明：
- 本项目定位在"运行期拦截 + EXPLAIN 驱动的风险评分"，区别于纯 SQL 解析或数据源监控。
- 相较 SQLAdvisor 的"索引建议"，本项目先聚焦"可落地的评分与观测闭环"，后续可引入可选的建议模块。

##  常见问题

**Q: 日志一直 level=OK、score=0？**
A: 可能查询已命中有效索引；可尝试 `LIKE '%xx%'` 或去掉索引观察规则命中

**Q: UI 没数据？**
A: 需先有 SQL 被拦截与分析；确保 `ui-enabled=true` 且接口被调用

**Q: MySQL 版本不同？**
A: 已内置版本探测与能力回退；8 优先 JSON，失败回退表格；非 8 表格优先

**Q: MyBatis-Plus 拦截顺序？**
A: 已自动重排至 MP 插件之后，无需手工调整

## 目标路线图

- 短期（S1）
  - 增强默认规则覆盖（例如: 高频 filesort/temporary 的组合权重）
  - 新增 HTTP 输出的签名/鉴权与批量上报
  - UI 增加过滤、下载与最近 N 分钟视图
- 中期（S2）
  - 引入"建议器"模块（索引/改写建议，参考 SQLAdvisor 的思路，以插件化方式集成，可选启用）
  - 规则市场化（远程规则订阅、分环境灰度）
- 长期（S3）
  - 跨库支持与厂商适配（MariaDB/Polar/MySQL 变体）
  - 与 APM/链路系统集成（traceId 透传、慢链路联动）

##  贡献

欢迎提交 Issue 和 Pull Request！

## 👨‍💻 作者

- 作者：linyi
- 邮箱：jingshuihuayue@qq.com

项目不足之处还请多多见谅。

## 📄 许可证

本项目采用 MIT 许可证，详情请见 [LICENSE](LICENSE) 文件。

##  参考资料

- [MyBatis 核心与插件 SPI](https://github.com/mybatis/mybatis-3)
- [MyBatis-Spring-Boot Starter](https://github.com/mybatis/spring-boot-starter)
- [MyBatis-Plus](https://github.com/baomidou/mybatis-plus)
- [p6spy（JDBC SQL 日志拦截）](https://github.com/p6spy/p6spy)
- [Alibaba Druid（SQL 统计与监控）](https://github.com/alibaba/druid)
- [MySQL EXPLAIN 输出说明](https://dev.mysql.com/doc/refman/8.0/en/explain-output.html)
- [Micrometer](https://github.com/micrometer-metrics/micrometer)
- [Spring Boot Auto-Configure](https://github.com/spring-projects/spring-boot/tree/2.7.x/spring-boot-project/spring-boot-autoconfigure)
- [Resilience4j（重试/退避思路参考）](https://github.com/resilience4j/resilience4j)
- [MVEL 表达式](https://github.com/mvel/mvel)
