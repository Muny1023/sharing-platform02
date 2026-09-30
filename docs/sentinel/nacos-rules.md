# Sentinel 网关规则

Gateway 已接入 Sentinel 1.8.9，规则从 Nacos 动态读取。服务启动前可以在 Nacos 控制台创建下面两条配置，配置组使用 `DEFAULT_GROUP`。

## 流控规则

Data ID：`cloud-gateway-flow`

```json
[
  {
    "resource": "account-auth",
    "resourceMode": 0,
    "grade": 1,
    "count": 10,
    "intervalSec": 3,
    "controlBehavior": 0,
    "burst": 0,
    "strategy": 0
  },
  {
    "resource": "api",
    "resourceMode": 0,
    "grade": 1,
    "count": 100,
    "intervalSec": 1,
    "controlBehavior": 0,
    "burst": 0,
    "strategy": 0
  }
]
```

`account-auth` 是认证路由的保护线；`api` 是统一 `/api/**` 入口的初始保护线。账号服务中的原有 `FlowLimitFilter` 仍然保留，负责认证接口的 IP 维度限制。

## 熔断规则

Data ID：`cloud-gateway-degrade`

```json
[
  {
    "resource": "content-post",
    "grade": 1,
    "count": 0.5,
    "timeWindow": 10,
    "minRequestAmount": 20,
    "statIntervalMs": 10000,
    "slowRatioThreshold": 1.0
  }
]
```

网关被 Sentinel 限流或熔断时返回 `RestBean` 结构，HTTP 状态为 `429`。规则可以在 Nacos 控制台修改，客户端会动态刷新。
