# 资源分享社区前端

从源项目 `my-project-frontend` 迁入，保留 Vue 3、TypeScript、Element Plus、Vue Router 和原业务页面。此目录 `fronted` 是当前业务前端入口，原默认 Vue 模板已不再作为启动入口。

## 在 IDEA 启动

1. 使用 Node.js `^22.18.0` 或 `>=24.12.0`。
2. 首次在此目录的终端执行 `npm ci`。
3. 打开本目录 `package.json`，点击 `scripts.dev` 左侧运行按钮。运行配置中的 package.json 路径必须指向 `myproject01/fronted/package.json`。
4. 打开运行日志中的本地地址，通常为 `http://localhost:5173`。端口被占用时 Vite 自动选择下一个端口。

也可以在此目录执行 `npm run dev`。

## 不启动后端时预览界面

在 `fronted/.env.development.local` 中设置：

```dotenv
VITE_OFFLINE_PREVIEW=true
```

重启 `npm run dev` 后，访问 `/` 会直接进入首页。首页和 `/post/1`、`/post/2`、`/post/3` 无需登录，使用本地示例帖子和评论，不会发送后端请求。可以通过首页帖子进入详情页。发布、点赞、收藏、评论和 AI 操作不在离线预览中提供。

需要连接后端调试登录时，将该值改为 `false`，然后重启开发服务器。该本地配置文件不会提交到 Git。

`npm run build` 默认生成生产版本，生产版本始终关闭离线预览，即使将该变量设为 `true` 也不会绕过登录。`npm run preview` 用来查看已构建的生产版本，也需要登录。URL 中的 `preview=1` 不再控制访问权限。

## 后端连接

所有业务请求默认发送至 Gateway `http://localhost:8080`，不直接访问账号、内容或通知服务。登录请求格式、JWT 存储和请求头、RestBean 响应处理均沿用原实现。

需要使用其他 Gateway 地址时，在此目录创建 `.env.local`，设置 `VITE_API_BASE_URL`，然后重启开发服务器。示例见 `.env.example`。不要在 `VITE_*` 配置中放内部服务令牌、AI 服务令牌或其他密钥。

业务联调需要启动本地 MySQL、Redis、RabbitMQ、Nacos，以及 Gateway、account-service、content-service、notification-service。Seata 已默认启用时还需启动 Seata Server。AI 页面仍由 Java 内容服务调用独立 Python 服务；此次迁移不复制或改动 Python 服务。

## 验证

```sh
npm run build
```

构建同时执行 Vue/TypeScript 类型检查。

## 目录说明

- `src/views`：业务页面，`welcome` 子目录存放登录、注册和找回密码页面。
- `src/styles`：全局基础样式和业务页、账号页共用的视觉样式；首页继续保留自己的布局。
- `src/router`、`src/net`：路由权限及后端接口封装。
- `src/config`、`src/preview`：开发环境离线预览开关和示例数据。
- `env.d.ts`、`tsconfig*.json`、`vite.config.ts`、`package-lock.json`：类型声明、构建配置和依赖锁定文件，需要保留。

默认空图标和未使用的 Vue DevTools、Vitest 依赖已移除。`node_modules` 为安装依赖，`dist` 为构建产物，均不提交 Git。
