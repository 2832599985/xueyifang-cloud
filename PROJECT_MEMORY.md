# PROJECT_MEMORY

## 记忆规约

- 本文件只记录项目索引、路径、功能短评和 Todo。
- 不写具体业务代码。
- 每次新增、删除或修改代码后，同步更新本文件。
- 每完成一个模块、阶段或明确任务，完成必要校验后创建 Git 提交，并写清提交说明。

## 项目概况

- 项目名称：`xueyifang-cloud`
- 项目目标：将原 `xueyifang` 单体项目重构为 Spring Cloud 架构。
- 原后端项目：`2832599985/xueyifang-backend`
- 原前端项目：`2832599985/xueyifang-frontend`
- 当前状态：阶段 3 基础设施部分落地；已接入 Nacos 注册配置、本地 Docker Compose，并通过 Maven 和 Compose 配置校验。

## 根目录索引

| 路径 | 类型 | 功能短评 |
| --- | --- | --- |
| `AIREADME.md` | 文档 | 用户提供的协作约定；禁止修改。 |
| `.gitattributes` | 配置 | 统一文本文件换行策略，降低 Windows 和类 Unix 环境差异。 |
| `.gitignore` | 配置 | 忽略 Maven 构建产物、日志、本地环境文件和 IDE 文件。 |
| `README.md` | 文档 | 项目简介、技术基线、模块说明和本地构建命令。 |
| `MIGRATION_PLAN.md` | 文档 | 迁移阶段、拆分策略、进度日志。 |
| `PROJECT_MEMORY.md` | 文档 | 项目索引、功能短评和 Todo。 |
| `pom.xml` | Maven | 根父工程，统一 Java、Spring Boot、Spring Cloud 版本和模块聚合。 |
| `docs/` | 目录 | 架构、模块盘点和接口设计文档。 |
| `docs/original-project-inventory.md` | 文档 | 原后端和前端盘点，包含业务域、API、数据表、前端页面、横切能力和拆分风险。 |
| `docs/service-boundary-design.md` | 文档 | 服务拆分边界、暂缓服务、第一批迁移顺序和网关路由约定。 |
| `docs/local-infrastructure.md` | 文档 | 本地 MySQL、Redis、Nacos 启动方式和应用接入说明。 |
| `deploy/` | 目录 | Docker、Nacos、数据库等部署配置。 |
| `deploy/docker/.env.example` | 配置 | 本地 Docker Compose 环境变量示例。 |
| `deploy/docker/docker-compose.yml` | 配置 | 本地 MySQL、Redis、Nacos 基础设施。 |
| `scripts/` | 目录 | 后续放本地开发、检查和迁移辅助脚本。 |
| `xueyifang-common/` | 目录 | 计划中的公共模块聚合目录。 |
| `xueyifang-gateway/` | 目录 | 计划中的网关服务。 |
| `xueyifang-auth/` | 目录 | 计划中的认证服务。 |
| `xueyifang-user/` | 目录 | 计划中的用户服务。 |
| `xueyifang-service/` | 目录 | 服务市场模块，承载服务发布、浏览、收藏和评价展示。 |
| `xueyifang-trade/` | 目录 | 交易模块，承载订单、钱包流水、退款和纠纷。 |

## 计划模块索引

| 模块 | 状态 | 功能短评 |
| --- | --- | --- |
| `xueyifang-common-core` | 已创建 | 通用模型、错误码、工具和跨端无关约定；当前仅有包占位。 |
| `xueyifang-common-web` | 已创建 | Web 层通用能力，供 Servlet 服务使用；当前仅有包占位。 |
| `xueyifang-gateway` | 已创建 | Spring Cloud Gateway 统一入口，当前配置静态路由到认证、用户、服务市场和交易服务。 |
| `xueyifang-auth` | 已创建 | 认证服务，当前包含 Spring Boot 启动类和基础端口配置。 |
| `xueyifang-user` | 已创建 | 用户服务，当前包含 Spring Boot 启动类和基础端口配置。 |
| `xueyifang-service` | 已创建 | 服务市场，当前包含 Spring Boot 启动类和基础端口配置。 |
| `xueyifang-trade` | 已创建 | 交易服务，当前包含 Spring Boot 启动类和基础端口配置。 |

## 关键文件索引

| 路径 | 功能短评 |
| --- | --- |
| `xueyifang-common/pom.xml` | 公共模块聚合 POM。 |
| `xueyifang-common/xueyifang-common-core/pom.xml` | 公共核心模块 POM。 |
| `xueyifang-common/xueyifang-common-web/pom.xml` | 公共 Web 模块 POM。 |
| `xueyifang-gateway/pom.xml` | 网关服务 POM，依赖 Spring Cloud Gateway 和 Actuator。 |
| `xueyifang-gateway/src/main/java/com/xueyifang/cloud/gateway/XueyifangGatewayApplication.java` | 网关服务启动类。 |
| `xueyifang-gateway/src/main/resources/application.yml` | 网关端口、服务名、Nacos 接入和 `lb://` 路由配置。 |
| `xueyifang-auth/pom.xml` | 认证服务 POM。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/XueyifangAuthApplication.java` | 认证服务启动类。 |
| `xueyifang-auth/src/main/resources/application.yml` | 认证服务端口和服务名配置。 |
| `xueyifang-user/pom.xml` | 用户服务 POM。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/XueyifangUserApplication.java` | 用户服务启动类。 |
| `xueyifang-user/src/main/resources/application.yml` | 用户服务端口和服务名配置。 |
| `xueyifang-service/pom.xml` | 服务市场 POM。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/XueyifangServiceApplication.java` | 服务市场启动类。 |
| `xueyifang-service/src/main/resources/application.yml` | 服务市场端口和服务名配置。 |
| `xueyifang-trade/pom.xml` | 交易服务 POM。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/XueyifangTradeApplication.java` | 交易服务启动类。 |
| `xueyifang-trade/src/main/resources/application.yml` | 交易服务端口和服务名配置。 |

## Todo

- 设计第一批迁移顺序，建议认证、用户、服务列表、订单最短链路。
- 配置统一响应、异常、错误码、用户上下文和基础日志。
- 增加基础 CI。
- 明确 Nacos 生产环境鉴权和外置数据库方案。
