# Nacos 配置副本

这个目录保存不含密码的 Nacos 配置副本，便于新环境按文件恢复配置。文件名对应 Nacos Data ID，目录名对应 Group：

```text
config-repo/nacos/DEFAULT_GROUP/<data-id>
```

当前已纳入版本控制的配置是 Gateway 的 Sentinel 规则：

- `cloud-gateway-flow.json`
- `cloud-gateway-degrade.json`

在 Nacos 控制台中选择 namespace `public` 和 Group `DEFAULT_GROUP`，分别创建同名 Data ID，并粘贴对应文件内容。规则内容不包含环境密码，可以安全提交；Nacos 登录密码、数据库密码、RabbitMQ 密码和邮箱授权码必须通过本机配置或环境变量提供。

Gateway 的路由和基础连接参数仍保留在各服务的 `src/main/resources/application.yml` 中，并通过 `${...}` 环境变量覆盖地址和密钥。后续新增 Nacos 配置时，先导出脱敏版本到本目录，再更新本说明。
