# xueyifang-cloud

`xueyifang-cloud` 是对原 `xueyifang` 单体项目的 Spring Cloud 重构工程。

当前仓库处于 Spring Cloud 基础设施落地阶段。迁移会按 [MIGRATION_PLAN.md](MIGRATION_PLAN.md) 分阶段推进，每完成一段就更新计划进度和 [PROJECT_MEMORY.md](PROJECT_MEMORY.md)。

## 技术基线

- Java 21
- Maven 3.9+
- Spring Boot 3.5.x
- Spring Cloud 2025.0.x

## 初始模块

| 模块 | 说明 |
| --- | --- |
| `xueyifang-common` | 公共模块聚合目录。 |
| `xueyifang-common-core` | 通用核心约定，避免绑定 Web 技术栈。 |
| `xueyifang-common-web` | Web 层通用能力，供 Servlet 服务使用。 |
| `xueyifang-gateway` | 统一网关入口。 |
| `xueyifang-auth` | 认证服务。 |
| `xueyifang-user` | 用户服务。 |
| `xueyifang-service` | 服务市场，承载服务发布、浏览、收藏和评价展示。 |
| `xueyifang-trade` | 交易服务，承载订单、钱包流水、退款、纠纷和订单定时任务。 |
| `xueyifang-system` | 系统服务，承载专业、交易地点和系统配置读取。 |
| `xueyifang-file` | 文件服务，承载本地文件上传、批量上传、删除和公开访问。 |
| `xueyifang-message` | 消息服务，承载聊天、通知、单实例 WebSocket 在线推送和可选 Redis pub/sub 多实例广播。 |

## 已落地基础能力

- Nacos Discovery 和 Config 接入，网关路由使用 `lb://` 服务名。
- 本地 Docker Compose 覆盖 MySQL、Redis 和 Nacos。
- `xueyifang-common-core` 提供 `BaseResponse`、`ErrorCode`、`ResultUtils`、`BusinessException`、用户上下文和链路常量。
- `xueyifang-common-web` 为 Servlet 服务自动装配统一异常处理、`X-Request-Id` 日志上下文和 `X-User-*` 用户上下文解析。
- Gateway 入口生成或透传 `X-Request-Id`，并写回响应头。
- GitHub Actions 基础 CI 覆盖 Docker Compose 配置校验和 Maven 构建验证。
- `xueyifang-common-core` 提供 JWT 签发、解析、刷新和 Token 黑名单 key 约定；Gateway 校验 Bearer Token、拒绝已登出 Token，并向下游透传可信 `X-User-*` 用户上下文。
- `xueyifang-auth` 已接入 `user` 表登录/注册、BCrypt 密码校验、注册开关、Token 刷新和 Redis 黑名单登出，并按 `user.publish_permission` 签发权限声明。
- `xueyifang-user` 已接入 `user` 表当前用户、资料更新、改密和发布权限状态接口，并兼容旧 `/auth/currentUser`、`/auth/updateProfile`、`/auth/changePassword` 路径。
- `xueyifang-service` 已接入 `service`、`service_image`、`service_tag`、`service_favorite` 和 `service_review` 表，提供服务列表、详情、标签读取、服务发布、我的服务、编辑、上下架、逻辑删除、收藏、我的收藏、评价创建、评价列表和订单评价状态接口。
- `xueyifang-trade` 已接入 `service_order`、`service_order_log`、`service_dispute` 和 `wallet_transaction` 表，提供订单创建、支付、取消、发货、确认完成、退款申请、卖家处理退款、纠纷发起与处理、订单超时任务、买家订单、卖家订单、订单详情、钱包余额、钱包流水、充值和提现能力。
- `xueyifang-system` 已接入 `professional`、`trade_location` 和 `sys_config` 表，提供专业列表、交易地点、注册开关和后台维护接口。
- `xueyifang-file` 已接入本地文件存储，提供单文件上传、批量上传、删除和公开查看接口，并兼容 `/api/file/**` 网关路径。
- `xueyifang-message` 已接入 `user_chat` 和 `notification` 表，提供聊天、通知读取、已读状态、`/api/ws` WebSocket 兼容入口和可选 Redis pub/sub 多实例广播。
- 认证与用户接口契约见 [docs/auth-user-api-contract.md](docs/auth-user-api-contract.md)。
- 服务市场接口契约见 [docs/service-market-api-contract.md](docs/service-market-api-contract.md)。
- 交易服务接口契约见 [docs/trade-api-contract.md](docs/trade-api-contract.md)。
- 系统字典与配置接口契约见 [docs/system-api-contract.md](docs/system-api-contract.md)。
- 文件服务接口契约见 [docs/file-api-contract.md](docs/file-api-contract.md)。
- 消息服务接口契约见 [docs/message-api-contract.md](docs/message-api-contract.md)。

## 本地构建

```powershell
mvn clean verify
```

## 本地基础设施

```powershell
Copy-Item deploy/docker/.env.example deploy/docker/.env
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml up -d
```

更多说明见 [docs/local-infrastructure.md](docs/local-infrastructure.md)。

## 协作约定

- 先读 `PROJECT_MEMORY.md`，再改代码。
- 改完代码后同步更新 `PROJECT_MEMORY.md`。
- 迁移进度写入 `MIGRATION_PLAN.md`。
- 每完成一个模块、阶段或明确任务，先更新进度文档并完成必要校验，再创建 Git 提交；提交信息要说明变更内容和验证结果。
- 不修改 `AIREADME.md`。
