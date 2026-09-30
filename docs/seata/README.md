# Seata AT 演示

先在共用的 `test` 数据库执行 `schema.sql`，再启动 Seata Server（默认 `8091`）、Nacos、账号服务、内容服务和 Gateway。

Gateway 入口为 `POST /api/seata/demo`，请求体示例：

```json
{
  "demoKey": "demo-success-001",
  "accountNote": "账号分支",
  "contentNote": "内容分支"
}
```

携带登录后的 `Authorization` 请求头。成功请求会在两张 `seata_demo_*` 表各写入一行。将 URL 改为 `/api/seata/demo?rollback=true` 会在两个分支完成后人为抛错，Seata 应回滚两张表。

演示数据只使用专用表，不修改真实账号、帖子或通知数据。由于两张表的 `demo_key` 唯一，重复执行同一个 key 会失败；请为每次成功或回滚演示使用新的 key。
