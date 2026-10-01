# 资源分享社区前端

从源项目 `my-project-frontend` 迁入，保留 Vue 3、TypeScript、Element Plus、Vue Router 和原业务页面。此目录 `fronted` 是当前业务前端入口，原默认 Vue 模板已不再作为启动入口。

## 在 IDEA 启动

1. 使用 Node.js `^22.18.0` 或 `>=24.12.0`。
2. 首次在此目录的终端执行 `npm ci`。
3. 打开本目录 `package.json`，点击 `scripts.dev` 左侧运行按钮。运行配置中的 package.json 路径必须指向 `myproject01/fronted/package.json`。
4. 打开运行日志中的本地地址，通常为 `http://localhost:5173`。端口被占用时 Vite 自动选择下一个端口。

也可以在此目录执行 `npm run dev`。

## 后端连接

所有业务请求默认发送至 Gateway `http://localhost:8080`，不直接访问账号、内容或通知服务。登录请求格式、JWT 存储和请求头、RestBean 响应处理均沿用原实现。

需要使用其他 Gateway 地址时，在此目录创建 `.env.local`，设置 `VITE_API_BASE_URL`，然后重启开发服务器。示例见 `.env.example`。不要在 `VITE_*` 配置中放内部服务令牌、AI 服务令牌或其他密钥。

业务联调需要启动本地 MySQL、Redis、RabbitMQ、Nacos，以及 Gateway、account-service、content-service、notification-service。Seata 已默认启用时还需启动 Seata Server。AI 页面仍由 Java 内容服务调用独立 Python 服务；此次迁移不复制或改动 Python 服务。

## 验证

```sh
npm test
npm run build
```

测试覆盖请求封装的 Gateway 地址、登录表单、JWT 请求头、RestBean 回调和 AI 请求超时；构建同时执行 Vue/TypeScript 类型检查。
