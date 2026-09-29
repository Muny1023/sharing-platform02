# Java 服务之间使用服务发现和 OpenFeign

状态：accepted

Java 服务之间使用 Nacos 服务发现和 OpenFeign，调用同时携带内部服务令牌与用户 JWT。OpenFeign 仅用于 Java 到 Java 的调用；Python AI 服务继续通过 HTTP 访问内容服务的内部接口。批量内部接口优先于逐条远程查询，以避免通知、账号和内容之间形成 N+1 调用。

## 考虑过的方案

- 在每个服务中手写固定地址 HTTP 客户端：本地可用，但失去服务发现和统一调用策略。
- 让 OpenFeign 负责 Python 调用：会把 Java 服务治理约束错误地扩展到独立 Python 进程。

## 后果

核心写操作必须快速失败；通知等非核心读取允许字段降级，并保留后续补偿所需的业务标识。
