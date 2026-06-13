# xueyifang-cloud

`xueyifang-cloud` 是对原 `xueyifang` 单体项目的 Spring Cloud 重构工程。

当前仓库处于初始化阶段。迁移会按 [MIGRATION_PLAN.md](MIGRATION_PLAN.md) 分阶段推进，每完成一段就更新计划进度和 [PROJECT_MEMORY.md](PROJECT_MEMORY.md)。

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
