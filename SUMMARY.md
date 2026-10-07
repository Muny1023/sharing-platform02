# myproject01 项目摘要

## 1. 项目定位

这是一个由 Spring Cloud Java 微服务、Vue 前端和独立 Python AI 服务组成的资源分享社区。后端包含 Gateway、账号、内容和通知四个服务，`cloud-common` 承载跨服务契约；前端目录实际名称是 `fronted`。

目标是参考原资源分享社区项目，逐步建设账号、内容、通知等 Java 微服务，并接入 Nacos、Gateway、OpenFeign、Sentinel、Seata 等组件。原 Python AI 服务如果后续加入，应作为独立进程通过 HTTP 被调用，不放进 Maven 聚合模块。

## 2. 技术栈速览

| 区域 | 当前实际情况 |
|---|---|
| Java 后端 | Spring Boot 4.0.8，Java 17，Maven |
| 后端聚合 | `cloud-backend` 是 Maven `pom` 聚合工程 |
| Java 服务 | Gateway、账号、内容、通知服务均有独立启动类 |
| 公共模块 | `cloud-common` 承载跨服务响应对象、事件和内部服务契约 |
| 前端 | Vue 3 + Vite + TypeScript，实际目录名为 `fronted` |
| 配置中心 | `config-repo` 保存脱敏的 Nacos Sentinel 规则副本 |
| 部署目录 | `deploy/docker-compose.yml` 提供可选的本地基础设施编排 |
| AI 服务 | `ai-service`，独立 Python/FastAPI 服务，复用原 AI Agent 实现 |
| 中间件 | 已接入 Nacos、Sentinel、Seata、OpenFeign、RabbitMQ、Redis 和 MySQL |

## 3. 当前目录结构

```text
myproject01/
├── pom.xml                         最外层 Maven 父工程，当前只聚合 cloud-backend
├── cloud-backend/                   Java 后端聚合工程
│   ├── pom.xml
│   ├── cloud-gateway/               Gateway 服务骨架
│   ├── cloud-common/                公共模块骨架
│   ├── account-service/             账号服务骨架
│   ├── content-service/             内容服务骨架，当前存在但未列入聚合模块
│   ├── notification-service/        通知服务骨架，当前存在但未列入聚合模块
├── fronted/                         Vue 前端模板，目录名暂时拼写为 fronted
├── ai-service/                      独立 Python AI 微服务
├── config-repo/                     脱敏的 Nacos 配置副本
├── deploy/                          可选的 Docker Compose 配置
├── .idea/                           IntelliJ IDEA 项目配置
└── .git/                            Git 仓库元数据
```

## 4. Java 模块职责

### `cloud-backend`

Maven 聚合工程和后端目录边界。自身不应作为业务服务运行，负责聚合各服务模块和统一继承版本配置。

当前 `<modules>` 包含：

- `cloud-gateway`
- `cloud-common`
- `account-service`
- `content-service`
- `notification-service`

### `cloud-gateway`

Gateway 负责统一入口、服务路由、跨域和 Sentinel 入口治理。JWT 当前由各业务服务分别校验，网关没有统一 JWT 过滤器。

### `account-service`

Account 负责注册、登录、账号信息和 JWT 相关业务，也通过 Nacos 注册为 `account-service`。

### `content-service`

Content 承载帖子、评论、点赞、收藏和 AI 内部接口等内容业务。

### `notification-service`

Notification 负责站内通知、邮件发送和 RabbitMQ 消费。

### `cloud-common`

`cloud-common` 提供 `RestBean`、通知事件、内部服务请求头和跨服务 DTO，不作为独立业务服务运行。

## 5. 前端模块

`fronted` 是一个 Vue 3 + Vite + TypeScript 初始模板，目前只有欢迎页组件、图标组件、基础 CSS 和入口文件，没有原社区项目的业务页面、路由或 API 封装。

常用脚本来自 `fronted/package.json`：

- `npm run dev`：开发服务器
- `npm run build`：类型检查并构建
- `npm run type-check`：类型检查
- `npm run preview`：预览构建结果

Node 版本约束为 `^22.18.0 || >=24.12.0`。

## 6. 配置和基础设施现状

- `config-repo/` 当前保存 Gateway 的 Sentinel 流控和熔断规则副本，导入方式见其 README。
- `deploy/` 提供 MySQL、Redis、RabbitMQ、Nacos、Sentinel Dashboard 的可选 Compose 配置，Seata 通过 profile 按需启动。
- 16GB 内存环境下，Nacos、Seata、Sentinel Dashboard 不需要每天全部启动，应按演示场景按需启动。
- Nacos Server、Seata Server、Sentinel Dashboard 是外部运行服务，不属于 Maven 模块；对应 Java 客户端依赖才写入各服务的 `pom.xml`。

## 7. 当前已知问题和下一步

1. 邮箱授权码已经通过本地 profile 隔离，上传前必须确认 `application-local.yml` 未被 Git 跟踪，并轮换曾经暴露过的授权码。
2. `github-clean` 分支只保留 `LoadBalancerVerificationTest`，其余测试保留在 `master`。该手工测试需要本地 Nacos 和两个账号服务实例；CI 仅执行后端编译和前端构建。
3. `ai-service` 是独立 Python/FastAPI 微服务，不加入 Maven `<modules>`；通过 `JAVA_SERVICE_URL` 调用 Java 内容服务内部接口，并使用 `AI_SERVICE_TOKEN` 做服务间鉴权。

## 8. 建议的目标结构

```text
myproject01/
├── cloud-backend/
│   ├── pom.xml
│   ├── cloud-gateway/
│   ├── cloud-common/
│   ├── account-service/
│   ├── content-service/
│   └── notification-service/
├── ai-service/                      独立 Python 微服务
├── fronted/                         现有 Vue 前端
├── config-repo/                     Nacos 配置文件副本
└── deploy/                          Docker/基础设施配置
```

前端只访问 Gateway；Java 服务之间使用服务发现和 OpenFeign；内容服务保留帖子、评论、点赞、收藏的强关联事务；AI 服务独立运行，通过 HTTP 对接。

## 9. 当前启动入口

当前可以直接运行的 Java 启动类：

- `org.example.cloudgateway.CloudGatewayApplication`
- `org.example.accountservice.AccountServiceApplication`
- `org.example.contentservice.ContentServiceApplication`
- `org.example.notificationservice.NotificationServiceApplication`
- `org.example.cloudbackend.CloudBackendApplication`（聚合工程残留启动类，不作为业务服务运行）
- `org.example.cloudcommon.CloudCommonApplication`（公共模块残留启动类，不作为业务服务运行）

当前前端从 `fronted/` 执行 `npm ci` 后，可用 `npm run dev` 启动。AI 服务从 `ai-service/` 安装 `requirements.txt` 后，可用 `uvicorn app.main:app --host 127.0.0.1 --port 8000` 启动。Nacos、数据库、Redis、RabbitMQ、Seata 和 Sentinel 可使用本机安装，或按 `deploy/README.md` 使用可选 Compose 配置。
