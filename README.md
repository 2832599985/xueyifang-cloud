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
| `xueyifang-trade` | 交易服务，承载订单、钱包流水、退款和纠纷。 |

## 已落地基础能力

- Nacos Discovery 和 Config 接入，网关路由使用 `lb://` 服务名。
- 本地 Docker Compose 覆盖 MySQL、Redis 和 Nacos。
- `xueyifang-common-core` 提供 `BaseResponse`、`ErrorCode`、`ResultUtils`、`BusinessException`、用户上下文和链路常量。
- `xueyifang-common-web` 为 Servlet 服务自动装配统一异常处理和 `X-Request-Id` 日志上下文。
- Gateway 入口生成或透传 `X-Request-Id`，并写回响应头。
- GitHub Actions 基础 CI 覆盖 Docker Compose 配置校验和 Maven 构建验证。
- `xueyifang-common-core` 提供 JWT 签发、解析和刷新基础能力；Gateway 校验 Bearer Token 并向下游透传可信 `X-User-*` 用户上下文。

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
