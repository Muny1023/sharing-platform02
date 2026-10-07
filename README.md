# myproject01

这是一个由 Spring Cloud Java 微服务、Vue 前端和独立 Python AI 服务组成的资源分享社区项目。

## 环境要求

- JDK 17（当前 Spring Boot 4.0.8；JDK 24 也可用于本地运行）
- Maven 3.9+
- Node.js 22.18+ 或 24.12+
- MySQL 8、Redis、RabbitMQ
- Nacos 2.x/3.x（默认 `localhost:8848`）
- Sentinel Dashboard（默认 `localhost:8858`）
- Seata Server（默认 `127.0.0.1:8091`，仅使用 Seata 示例时需要）

## 启动顺序

可以使用 `deploy/docker-compose.yml` 启动本地基础设施：

```powershell
cd deploy
docker compose up -d
# 需要 Seata 演示时再执行
docker compose --profile seata up -d
```

也可以使用已有的本机安装。无论采用哪种方式，启动顺序都是：

1. MySQL、Redis、RabbitMQ。
2. Nacos Server。
3. Sentinel Dashboard。
4. Seata Server（需要 Seata 事务演示时启动）。
5. `account-service`、`content-service`、`notification-service`。
6. `cloud-gateway`。
7. `fronted` 前端；`ai-service` 仅在使用 AI 功能时启动。

数据库脚本位于 `docs/sql/`。至少需要执行业务表脚本和 `006_notifications.sql`。默认数据库名为 `test`。

## Java 服务

每个服务都可以在 IDEA 中直接运行对应的 `*Application` 启动类：

| 服务 | 默认端口 | 启动类 |
| --- | ---: | --- |
| Gateway | 8080 | `org.example.cloudgateway.CloudGatewayApplication` |
| Account | 8081 | `org.example.accountservice.AccountServiceApplication` |
| Content | 8082 | `org.example.contentservice.ContentServiceApplication` |
| Notification | 8083 | `org.example.notificationservice.NotificationServiceApplication` |

账号服务的第二个实例使用相同启动类，将端口改为 `8086`。两个实例必须都使用 Nacos 服务名 `account-service`，然后可以运行 Gateway 测试类中的 `LoadBalancerVerificationTest` 验证轮询选择。

后端配置默认连接本机中间件，也可以通过环境变量覆盖，例如 `NACOS_SERVER_ADDR`、`ACCOUNT_DB_URL`、`REDIS_HOST`、`RABBITMQ_HOST` 和 `JWT_KEY`。

## 邮件配置

通知服务的真实邮箱配置使用本地 profile，不提交到 Git：

```text
cloud-backend/notification-service/src/main/resources/application-local.yml
```

模板是 `application-local.yml.example`，真实文件已经加入 `.gitignore`。在 IDEA 的 `NotificationServiceApplication` 运行配置中设置 Active profiles 为 `local`，然后重启服务。不要把邮箱授权码写入主配置或提交到仓库。

账号服务调用 `/api/auth/ask-code` 后，会向 RabbitMQ 的 `mail` 队列发送验证码消息；通知服务消费消息并通过 SMTP 发信。

## Nacos 和 Sentinel

Gateway 从 Nacos 读取自身配置，并从以下 Data ID 动态读取 Sentinel 规则：

- `cloud-gateway-flow`：网关流控规则
- `cloud-gateway-degrade`：网关熔断降级规则

规则说明和示例位于 `docs/sentinel/nacos-rules.md`。Dashboard 地址为 `http://localhost:8858`，Nacos 控制台地址通常为 `http://localhost:8850`。

当前 `content-post` 规则是异常比例熔断：10 秒窗口内至少 20 个请求，异常比例达到 50% 后熔断 10 秒，并返回 HTTP 429 的 `RestBean` 响应。验证时需要让已注册的内容服务实例产生连接异常；仅停止实例会导致负载均衡找不到服务，不能验证 Sentinel 熔断。

## 前端和 AI 服务

```powershell
cd fronted
npm ci
npm run dev
```

前端默认访问 Gateway `http://localhost:8080`。前端测试和构建：

```powershell
npm test
npm run build
```

AI 服务是独立进程：

```powershell
cd ai-service
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

## 验证命令

运行后端测试：

```powershell
mvn -pl cloud-backend -am test
```

只验证 Gateway 配置：

```powershell
mvn -pl cloud-backend/cloud-gateway -Dtest=CloudGatewayApplicationTests test
```

验证 Nacos 负载均衡前，先启动 Nacos 和 8081、8086 两个账号服务实例，再在 IDEA 中直接运行 `LoadBalancerVerificationTest`。

## 常见问题

- **8080 被占用**：使用 `netstat -ano | findstr :8080` 查 PID；确认是 Gateway 后再停止对应进程。
- **邮件提示 no password specified**：检查是否创建了 `application-local.yml`，并确认 IDEA 的 Active profiles 是 `local`，修改后重启通知服务。
- **Nacos 能打开但规则为空**：确认 Data ID、Group、namespace 和 Gateway 的 Nacos 地址一致；规则源是 Nacos，Dashboard 主要用于查看运行状态。
- **Gateway 返回 503**：先在 Nacos 确认目标服务有健康实例，再检查服务名和端口。
- **Sentinel 出现日志目录错误**：创建 `C:\logs\csp`、`C:\logs\nacos` 和 `C:\nacos\config`，或者确认这些错误只来自本地目录不存在而不是服务连接失败。
- **服务间 JWT**：当前由 account、content、notification 各业务服务分别校验 JWT，Gateway 负责路由和 Sentinel 入口治理。

## 目录说明

- `cloud-backend/`：Java Maven 聚合工程
- `fronted/`：Vue 前端
- `ai-service/`：独立 Python 服务
- `config-repo/`：脱敏的 Nacos 配置副本
- `deploy/`：本地基础设施 Docker Compose 配置
- `docs/`：数据库、Sentinel、Seata 和架构文档
