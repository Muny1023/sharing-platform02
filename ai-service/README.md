# ai-service

资源分享社区的独立 Python AI 微服务。它通过 LangChain 调用 DeepSeek，提供社区帖子搜索、正文概括、评论反馈分析和正文修改草稿生成。

## 启动

1. 在 `ai-service` 目录创建并激活 Python 环境（原项目使用过 `langchain1.2`）：`conda activate langchain1.2`
2. 安装依赖：`pip install -r requirements.txt`
3. 复制 `.env.example` 为 `.env` 并填写环境变量。Java 内容服务与 Python 的 `AI_SERVICE_TOKEN` 必须一致，`JAVA_SERVICE_URL` 指向内容服务的内部接口地址。
4. 启动服务：`uvicorn app.main:app --host 127.0.0.1 --port 8000`

也可以在 VS Code 中打开 `run_ai.ipynb`，选择 `Python (langchain1.2)` 内核，运行第一个代码单元启动服务；运行第二个代码单元停止由 Notebook 启动的服务。重复运行启动单元不会重复占用 8000 端口。

健康检查：`GET http://127.0.0.1:8000/health`

聊天请求可选传入 `targetPostId` 触发单篇正文概括，也可以传入 `feedbackPostId` 或 `draftPostId` 处理评论反馈。响应中的 `summary` 为模型生成的概括。正文只通过 Java 内容服务内部接口读取，已删除、空正文或超过 10000 字时返回明确提示。
