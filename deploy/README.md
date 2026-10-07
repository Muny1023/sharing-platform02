# 本地基础设施

`docker-compose.yml` 提供本项目本地开发需要的 MySQL、Redis、RabbitMQ、Nacos 和 Sentinel Dashboard。Seata 使用可选 profile，不需要 Seata 演示时不会启动。

在 `deploy` 目录执行：

```powershell
docker compose up -d
docker compose --profile seata up -d
```

如果系统只有独立的 `docker-compose` 命令，将上面命令中的 `docker compose` 替换为 `docker-compose`。首次启动后仍需按 `docs/sql/` 执行数据库脚本，并按 `config-repo/README.md` 导入 Nacos 配置。

默认账号与密码只用于本地开发，可在 `deploy/.env` 覆盖。不要将含真实密码的 `.env` 提交到 Git。
