# LiteKV-DB

> **轻量级、兼容 Redis 的分布式 KV 存储服务**，支持高并发场景下的常用数据类型与命令，具备混合持久化、高可用集群及生产级监控。

---

## 目录

* [特性](#特性)
* [架构概览](#架构概览)
* [快速开始](#快速开始)
* [模块说明](#模块说明)
* [配置参考](#配置参考)
* [开发指南](#开发指南)
* [测试](#测试)
* [部署](#部署)
* [贡献者](#贡献者)
* [许可证](#许可证)

---

## 特性

* **RESP2 & RESP3** 完全兼容，支持字符串、列表、哈希、集合、有序集合等。
* **混合持久化**：RDB 全量快照 + AOF 增量日志，启动加载快速、运行时性能稳定。
* **高可用 & 集群**：主从复制、Gossip 分片、一致性哈希、自动故障转移。
* **生产级监控**：Prometheus 指标暴露，支持 Grafana 仪表盘。
* **安全机制**：AUTH 认证、TLS 加密、ACL 访问控制。
* **可扩展 & 热加载**：动态调整线程池、内存限额及持久化策略。

## 架构概览

```mermaid
flowchart LR
  Client[客户端 (CLI / SDK)] -->|TCP| Network[网络层<br/>(Netty NIO Reactor)]
  Network --> Protocol[协议层<br/>(RESP2/RESP3 解析/序列化)]
  Protocol --> Dispatcher[命令分发层<br/>(线程池执行)]
  Dispatcher --> Storage[内存存储层<br/>(String/List/Hash/...)]
  Storage --> Persistence[持久化层<br/>(RDB + AOF)]
  Dispatcher --> Cluster[集群 & 复制<br/>(Gossip & PSYNC)]
  subgraph Monitoring
    Metrics[监控层<br/>(Prometheus)]
    Metrics --> Network
    Metrics --> Dispatcher
    Metrics --> Storage
  end
```

**图解说明**：

1. 客户端通过 TCP 建立连接并发送 RESP 格式命令。
2. Netty 处理网络 IO，并将字节流交给协议层解析。
3. 协议层生成 `RedisRequest`，命令分发层提交到线程池执行。
4. 内存存储层执行读写操作，持久化层异步写入快照与日志。
5. 集群模块负责主从复制与故障转移。
6. Prometheus 指标采集全链路请求、延迟及资源使用情况。

---

## 快速开始

1. 克隆代码：

   ```bash
   git clone https://github.com/your_org/LiteKV-DB.git
   cd LiteKV-DB
   ```
2. 构建项目：

   ```bash
   mvn clean package -DskipTests
   ```
3. 编辑配置：

   ```bash
   vi src/main/resources/config/production/redis.conf
   ```
4. 启动服务：

   ```bash
   scripts/startup.sh
   ```
5. 连接测试：

   ```bash
   redis-cli -h 127.0.0.1 -p 6379
   ```

---

## 模块说明

| 模块              | 描述                         |
| --------------- | -------------------------- |
| **bootstrap**   | 启动入口、配置加载、环境初始化            |
| **network**     | 基于 Netty 的网络框架与 IO Reactor |
| **protocol**    | RESP2/3 协议解析与序列化           |
| **executor**    | 命令分发与线程池执行                 |
| **store**       | 内存数据结构与过期管理                |
| **persistence** | RDB 快照 & AOF 日志管理          |
| **cluster**     | 主从复制、Gossip 分片与故障转移        |
| **security**    | AUTH 认证、TLS 加密、ACL 管理      |
| **metrics**     | Prometheus 指标暴露与收集         |
| **utils**       | CRC16、ByteUtils 等工具类       |

---

## 配置参考

详见 `docs/Configuration-Reference.md`，包含所有可配置项及其默认值。

---

## 开发指南

请参阅 `docs/Development-Guide.md`，涵盖代码规范、单元测试、集成测试及模块扩展指导。

---

## 测试

```bash
# 单元测试
mvn test

# 性能基准
mvn jmh:benchmark
```

---

## 部署

* **Docker**：提供官方镜像，详情见 `docker/` 目录。
* **Kubernetes**：Helm Chart 存放在 `deploy/helm/`，支持 StatefulSet 部署。

---

## 贡献者

欢迎提 PR 与 Issue！详见 `CONTRIBUTING.md`。

---

## 许可证

本项目遵循 [Apache-2.0](LICENSE) 许可。
