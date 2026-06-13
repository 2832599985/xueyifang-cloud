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
- 当前状态：阶段 4 认证与用户迁移进行中，阶段 5 服务市场最短链路已启动；阶段 3 基础设施已完成，已新增 JWT 公共能力、Gateway Bearer Token 校验、Servlet 用户上下文解析、Auth 登录/注册/刷新/登出、Redis Token 黑名单、User 当前用户资料和发布权限接口，`xueyifang-service` 已提供服务列表、详情、标签读取、服务发布、我的服务、编辑、上下架、逻辑删除、收藏、我的收藏、评价列表和订单评价状态。

## 根目录索引

| 路径 | 类型 | 功能短评 |
| --- | --- | --- |
| `AIREADME.md` | 文档 | 用户提供的协作约定；禁止修改。 |
| `.github/workflows/ci.yml` | CI | GitHub Actions 基础流水线，校验 Docker Compose 配置并执行 Maven 构建。 |
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
| `docs/auth-user-api-contract.md` | 文档 | 阶段 4 认证与用户资料接口契约，记录新旧兼容路径和 Token 约定。 |
| `docs/service-market-api-contract.md` | 文档 | 阶段 5 服务市场接口契约，记录服务浏览和发布者管理接口。 |
| `deploy/` | 目录 | Docker、Nacos、数据库等部署配置。 |
| `deploy/docker/.env.example` | 配置 | 本地 Docker Compose 和应用环境变量示例，包含 MySQL、Redis、Nacos 与 JWT 配置。 |
| `deploy/docker/docker-compose.yml` | 配置 | 本地 MySQL、Redis、Nacos 基础设施，并挂载 MySQL 初始化脚本。 |
| `deploy/docker/mysql/init/001-user.sql` | SQL | 本地 MySQL 初始化 `user` 表，供认证和用户服务使用。 |
| `deploy/docker/mysql/init/002-service.sql` | SQL | 本地 MySQL 初始化 `service`、`service_image`、`service_tag`、`service_favorite` 和 `service_review` 表，供服务市场使用。 |
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
| `xueyifang-common-core` | 已创建 | 通用响应、错误码、业务异常、用户上下文和链路常量，避免绑定 Web 技术栈。 |
| `xueyifang-common-web` | 已创建 | Web 层通用能力，供 Servlet 服务使用；自动装配统一异常处理、requestId 过滤器和用户上下文过滤器。 |
| `xueyifang-gateway` | 已创建 | Spring Cloud Gateway 统一入口，当前使用 Nacos 服务发现和 `lb://` 路由，并生成或透传 `X-Request-Id`，校验 Bearer Token、拒绝黑名单 Token 后透传可信用户上下文。 |
| `xueyifang-auth` | 已创建 | 认证服务，当前包含 Spring Boot 启动类、登录、注册、Token 刷新、退出登录、Redis Token 黑名单，并按 `user.publish_permission` 签发权限声明。 |
| `xueyifang-user` | 已创建 | 用户服务，当前包含当前用户、资料更新、改密、发布权限状态和旧 `/auth/*` 资料路径兼容接口。 |
| `xueyifang-service` | 已创建 | 服务市场，当前包含 Spring Boot 启动类、MySQL/JDBC 接入、服务列表、服务详情、标签读取、发布、我的服务、编辑、上下架、逻辑删除、收藏、我的收藏、评价列表和订单评价状态接口。 |
| `xueyifang-trade` | 已创建 | 交易服务，当前包含 Spring Boot 启动类和基础端口配置。 |

## 关键文件索引

| 路径 | 功能短评 |
| --- | --- |
| `xueyifang-common/pom.xml` | 公共模块聚合 POM。 |
| `xueyifang-common/xueyifang-common-core/pom.xml` | 公共核心模块 POM。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/api/BaseResponse.java` | 通用响应结构，保持 `code/message/data` JSON 契约。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/api/ErrorCode.java` | 原单体错误码迁移。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/api/ResultUtils.java` | 通用成功和失败响应构造工具。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/auth/AuthConstants.java` | 认证请求头、Bearer Token 和下游用户上下文头常量。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/auth/AuthTokenUtils.java` | 从 `Authorization: Bearer` 和兼容 `token` 头中提取 Token。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/auth/JwtTokenService.java` | JWT 签发、解析和刷新基础能力。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/auth/TokenBlacklistKeys.java` | Token 黑名单 Redis key 指纹生成约定。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/auth/TokenBlacklistService.java` | 同步 Token 黑名单服务接口，供认证服务使用。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/context/LoginUserContext.java` | 当前登录用户上下文快照。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/context/UserContextHolder.java` | Servlet 线程内用户上下文持有器。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/context/TraceConstants.java` | `X-Request-Id` 和 MDC key 常量。 |
| `xueyifang-common/xueyifang-common-core/src/main/java/com/xueyifang/cloud/common/core/exception/BusinessException.java` | 通用业务异常。 |
| `xueyifang-common/xueyifang-common-web/pom.xml` | 公共 Web 模块 POM。 |
| `xueyifang-common/xueyifang-common-web/src/main/java/com/xueyifang/cloud/common/web/autoconfigure/CommonWebAutoConfiguration.java` | Spring Boot 3 自动配置入口，仅在 Servlet Web 应用启用。 |
| `xueyifang-common/xueyifang-common-web/src/main/java/com/xueyifang/cloud/common/web/exception/GlobalExceptionHandler.java` | Servlet 服务全局异常处理，输出统一响应结构。 |
| `xueyifang-common/xueyifang-common-web/src/main/java/com/xueyifang/cloud/common/web/filter/RequestIdFilter.java` | Servlet requestId 生成、透传和 MDC 写入。 |
| `xueyifang-common/xueyifang-common-web/src/main/java/com/xueyifang/cloud/common/web/filter/UserContextFilter.java` | Servlet 服务解析 Gateway 透传的 `X-User-*`，写入并清理 `UserContextHolder`。 |
| `xueyifang-common/xueyifang-common-web/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | Spring Boot 3 自动配置声明。 |
| `xueyifang-gateway/pom.xml` | 网关服务 POM，依赖 Spring Cloud Gateway 和 Actuator。 |
| `xueyifang-gateway/src/main/java/com/xueyifang/cloud/gateway/XueyifangGatewayApplication.java` | 网关服务启动类。 |
| `xueyifang-gateway/src/main/java/com/xueyifang/cloud/gateway/config/GatewayAuthProperties.java` | 网关认证白名单和 JWT 配置绑定。 |
| `xueyifang-gateway/src/main/java/com/xueyifang/cloud/gateway/auth/ReactiveTokenBlacklistService.java` | Gateway 响应式 Token 黑名单查询接口。 |
| `xueyifang-gateway/src/main/java/com/xueyifang/cloud/gateway/auth/RedisReactiveTokenBlacklistService.java` | Gateway Redis Token 黑名单查询实现。 |
| `xueyifang-gateway/src/main/java/com/xueyifang/cloud/gateway/filter/GatewayAuthFilter.java` | Gateway Bearer Token 校验、错误响应和 `X-User-*` 用户上下文透传。 |
| `xueyifang-gateway/src/main/java/com/xueyifang/cloud/gateway/filter/GatewayRequestIdFilter.java` | Gateway requestId 生成、透传、响应回写和 MDC 写入。 |
| `xueyifang-gateway/src/main/resources/application.yml` | 网关端口、服务名、Nacos 接入、JWT、`lb://` 路由和旧 `/auth/*` 用户资料路径兼容路由配置。 |
| `xueyifang-auth/pom.xml` | 认证服务 POM。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/XueyifangAuthApplication.java` | 认证服务启动类。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/controller/AuthController.java` | 认证入口，提供 `POST /auth/register`、`POST /auth/login` 和 `POST /auth/logout`。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/controller/AuthTokenController.java` | Token 刷新接口，当前提供 `POST /auth/token/refresh`，并拒绝黑名单 Token。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/repository/JdbcAuthUserRepository.java` | 基于 `JdbcTemplate` 的 `user` 表认证数据访问，读取发布权限用于签发 JWT。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/service/AuthService.java` | 登录、注册、BCrypt 密码校验和 JWT 签发业务逻辑。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/service/AuthTokenService.java` | Token 刷新和退出登录黑名单业务逻辑。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/token/RedisTokenBlacklistService.java` | Auth 服务 Redis Token 黑名单写入和查询实现。 |
| `xueyifang-auth/src/main/resources/application.yml` | 认证服务端口、服务名、Nacos、MySQL、Redis 和 JWT 配置。 |
| `xueyifang-user/pom.xml` | 用户服务 POM，依赖公共 Web、JDBC、MySQL 和 BCrypt。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/XueyifangUserApplication.java` | 用户服务启动类。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/controller/UserProfileController.java` | 用户资料新接口，提供 `/users/me`、`/users/me/profile` 和 `/users/me/password`。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/controller/AuthUserCompatibilityController.java` | 旧 `/auth/currentUser`、`/auth/updateProfile` 和 `/auth/changePassword` 兼容入口。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/controller/PermissionController.java` | 发布权限申请和状态查询入口。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/repository/JdbcUserAccountRepository.java` | 基于 `JdbcTemplate` 的 `user` 表资料和权限数据访问。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/service/UserProfileService.java` | 当前用户资料、资料更新、改密和发布权限状态业务逻辑。 |
| `xueyifang-user/src/main/resources/application.yml` | 用户服务端口、服务名、Nacos 和 MySQL 配置。 |
| `xueyifang-service/pom.xml` | 服务市场 POM，依赖公共 Web、JDBC 和 MySQL。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/XueyifangServiceApplication.java` | 服务市场启动类。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/controller/ServiceCatalogController.java` | 服务市场入口，提供服务浏览、标签读取和发布者管理接口。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/controller/ServiceFavoriteController.java` | 服务收藏入口，提供收藏、取消收藏和我的收藏接口。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/controller/ServiceReviewController.java` | 服务评价入口，提供评价公开列表和订单评价状态接口。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/dto/ServicePublishRequest.java` | 服务发布请求，兼容新字段和旧前端 `serviceTitle`/`serviceDescription` 字段。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/dto/ServiceUpdateRequest.java` | 服务编辑请求，兼容图片替换和旧前端字段。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/repository/JdbcServiceCatalogRepository.java` | 基于 `JdbcTemplate` 的 `service`、`service_image` 和 `service_tag` 查询/写入实现，并维护服务收藏数。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/repository/JdbcServiceInteractionRepository.java` | 基于 `JdbcTemplate` 的 `service_favorite` 和 `service_review` 查询/写入实现。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/service/ServiceCatalogService.java` | 服务列表、详情可见性、标签读取、发布、编辑、上下架、删除和详情收藏状态业务逻辑。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/service/ServiceFavoriteService.java` | 服务收藏、取消收藏、我的收藏、幂等和收藏数同步业务逻辑。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/service/ServiceReviewService.java` | 服务评价公开列表、匿名展示和订单评价状态业务逻辑。 |
| `xueyifang-service/src/main/resources/application.yml` | 服务市场端口、服务名、Nacos 和 MySQL 配置。 |
| `xueyifang-trade/pom.xml` | 交易服务 POM。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/XueyifangTradeApplication.java` | 交易服务启动类。 |
| `xueyifang-trade/src/main/resources/application.yml` | 交易服务端口和服务名配置。 |

## Todo

- 让交易服务在迁移业务接口时消费 `X-User-*` 用户上下文。
- 接入评价创建接口，或先进入订单最短链路后再回接评价创建所需的订单完成状态校验。
- 启动本地 Nacos 后，做一次网关到业务服务的健康检查联通验证。
- 明确 Nacos 生产环境鉴权和外置数据库方案。
