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
- 当前状态：阶段 4 认证与用户迁移进行中，阶段 5 服务市场、交易链路、系统字典、文件能力和消息能力已启动；阶段 3 基础设施已完成，已新增 JWT 公共能力、Gateway Bearer Token 校验、Servlet 用户上下文解析、Auth 登录/注册/刷新/登出、注册开关、Redis Token 黑名单、User 当前用户资料、发布权限申请、后台发布权限审核、后台用户导入和通知回接，`xueyifang-service` 已提供服务列表、详情、标签读取、服务发布、我的服务、编辑、上下架、逻辑删除、`REVIEW_MODE` 审核流、后台服务审核、收藏、我的收藏、评价创建、评价列表和订单评价状态，`xueyifang-trade` 已提供订单创建、支付、取消、发货、确认完成、退款申请、卖家处理退款、纠纷发起与处理、旧后台纠纷路径兼容、订单定时任务、买卖家订单列表、详情、钱包余额、钱包流水、充值、提现、用户销售统计和交易/纠纷通知回接，`xueyifang-system` 已提供专业、交易地点、注册开关、后台系统配置维护和后台统计接口，`xueyifang-file` 已提供本地文件上传、批量上传、删除和公开访问接口，`xueyifang-message` 已提供聊天、通知、内部通知创建、单实例 WebSocket 在线推送和可选 Redis pub/sub 多实例广播；按原前端 80 个 HTTP 调用点口径已完成路径覆盖，后续进入本地联调和上线工程化收尾。

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
| `docs/auth-user-api-contract.md` | 文档 | 阶段 4 认证与用户资料接口契约，记录新旧兼容路径、Token 约定和后台发布权限审核接口。 |
| `docs/service-market-api-contract.md` | 文档 | 阶段 5 服务市场接口契约，记录服务浏览、发布者管理、审核流和后台服务审核接口。 |
| `docs/trade-api-contract.md` | 文档 | 阶段 5 交易服务接口契约，记录订单最短链路和资金流状态约定。 |
| `docs/system-api-contract.md` | 文档 | 阶段 5 系统字典与配置接口契约，记录专业、交易地点和系统配置接口。 |
| `docs/file-api-contract.md` | 文档 | 阶段 5 文件服务接口契约，记录上传、批量上传、删除、查看和网关兼容路径。 |
| `docs/message-api-contract.md` | 文档 | 阶段 5 消息服务接口契约，记录聊天、通知、内部通知创建、WebSocket、Redis 广播和通知回接策略。 |
| `docs/original-api-comparison.md` | 文档 | 原前端调用点、原后端接口和新云端接口对照清单，记录 80/80 路径覆盖和后续联调风险。 |
| `deploy/` | 目录 | Docker、Nacos、数据库等部署配置。 |
| `deploy/docker/.env.example` | 配置 | 本地 Docker Compose 和应用环境变量示例，包含 MySQL、Redis、Nacos 与 JWT 配置。 |
| `deploy/docker/docker-compose.yml` | 配置 | 本地 MySQL、Redis、Nacos 基础设施，并挂载 MySQL 初始化脚本。 |
| `deploy/docker/mysql/init/001-user.sql` | SQL | 本地 MySQL 初始化 `user` 表，供认证和用户服务使用，包含发布权限申请和审核痕迹字段。 |
| `deploy/docker/mysql/init/002-service.sql` | SQL | 本地 MySQL 初始化 `service`、`service_image`、`service_tag`、`service_favorite` 和 `service_review` 表，供服务市场使用，包含服务审核痕迹字段。 |
| `deploy/docker/mysql/init/003-trade.sql` | SQL | 本地 MySQL 初始化 `service_order`、`service_order_log`、`service_dispute` 和 `wallet_transaction` 表，供交易服务使用，并包含退款和纠纷状态查询索引。 |
| `deploy/docker/mysql/init/004-system.sql` | SQL | 本地 MySQL 初始化 `professional`、`trade_location` 和 `sys_config` 表，供系统服务使用。 |
| `deploy/docker/mysql/init/005-message.sql` | SQL | 本地 MySQL 初始化 `user_chat` 和 `notification` 表，供消息服务使用。 |
| `scripts/` | 目录 | 后续放本地开发、检查和迁移辅助脚本。 |
| `xueyifang-common/` | 目录 | 计划中的公共模块聚合目录。 |
| `xueyifang-gateway/` | 目录 | 计划中的网关服务。 |
| `xueyifang-auth/` | 目录 | 计划中的认证服务。 |
| `xueyifang-user/` | 目录 | 计划中的用户服务。 |
| `xueyifang-service/` | 目录 | 服务市场模块，承载服务发布、浏览、收藏和评价展示。 |
| `xueyifang-trade/` | 目录 | 交易模块，承载订单、钱包流水、退款、纠纷和订单定时任务。 |
| `xueyifang-system/` | 目录 | 系统模块，承载专业、交易地点和系统配置。 |
| `xueyifang-file/` | 目录 | 文件模块，承载本地文件上传、批量上传、删除和公开访问。 |
| `xueyifang-message/` | 目录 | 消息模块，承载聊天、通知、内部通知创建、单实例 WebSocket 在线推送和可选 Redis pub/sub 多实例广播。 |

## 计划模块索引

| 模块 | 状态 | 功能短评 |
| --- | --- | --- |
| `xueyifang-common-core` | 已创建 | 通用响应、错误码、业务异常、用户上下文和链路常量，避免绑定 Web 技术栈。 |
| `xueyifang-common-web` | 已创建 | Web 层通用能力，供 Servlet 服务使用；自动装配统一异常处理、requestId 过滤器和用户上下文过滤器。 |
| `xueyifang-gateway` | 已创建 | Spring Cloud Gateway 统一入口，当前使用 Nacos 服务发现和 `lb://` 路由，并生成或透传 `X-Request-Id`，校验 Bearer Token、拒绝黑名单 Token 后透传可信用户上下文。 |
| `xueyifang-auth` | 已创建 | 认证服务，当前包含 Spring Boot 启动类、登录、注册、注册开关、Token 刷新、退出登录、Redis Token 黑名单，并按 `user.publish_permission` 签发权限声明。 |
| `xueyifang-user` | 已创建 | 用户服务，当前包含当前用户、资料更新、改密、发布权限申请/状态、后台发布权限审核、后台用户导入、审核通知回接和旧 `/auth/*` 资料路径兼容接口。 |
| `xueyifang-service` | 已创建 | 服务市场，当前包含 Spring Boot 启动类、MySQL/JDBC 接入、服务列表、服务详情、标签读取、发布、我的服务、编辑、上下架、逻辑删除、`REVIEW_MODE` 审核流、后台服务审核、审核通知回接、收藏、我的收藏、评价创建、评价列表和订单评价状态接口。 |
| `xueyifang-trade` | 已创建 | 交易服务，当前包含 Spring Boot 启动类、MySQL/JDBC 接入、订单创建、支付、取消、发货、确认完成、退款申请、卖家处理退款、纠纷发起与处理、旧后台纠纷路径兼容、订单定时任务、买卖家订单列表、详情、钱包余额、钱包流水、充值、提现、用户销售统计接口和交易/纠纷通知回接。 |
| `xueyifang-system` | 已创建 | 系统服务，当前包含 Spring Boot 启动类、MySQL/JDBC 接入、专业字典、交易地点、注册开关、后台系统配置维护和后台统计接口。 |
| `xueyifang-file` | 已创建 | 文件服务，当前包含 Spring Boot 启动类、本地存储配置、单文件上传、批量上传、删除和公开查看接口，并通过网关兼容 `/api/file/**`。 |
| `xueyifang-message` | 已创建 | 消息服务，当前包含 Spring Boot 启动类、MySQL/JDBC 接入、聊天发送、聊天记录、会话列表、通知列表、未读数、标记已读、内部通知创建、`/api/ws` 单实例 WebSocket 兼容入口和可选 Redis pub/sub 多实例广播。 |

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
| `xueyifang-gateway/src/main/resources/application.yml` | 网关端口、服务名、Nacos 接入、JWT、`lb://` 路由、旧 `/auth/*` 用户资料路径兼容路由、用户/服务后台审核路由、系统服务路由、文件服务 `/api/file/**` 兼容路由和消息服务 `/api/ws` 兼容路由配置。 |
| `xueyifang-auth/pom.xml` | 认证服务 POM。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/XueyifangAuthApplication.java` | 认证服务启动类。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/controller/AuthController.java` | 认证入口，提供 `POST /auth/register`、`POST /auth/login` 和 `POST /auth/logout`。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/controller/AuthTokenController.java` | Token 刷新接口，当前提供 `POST /auth/token/refresh`，并拒绝黑名单 Token。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/repository/JdbcAuthUserRepository.java` | 基于 `JdbcTemplate` 的 `user` 表认证数据访问，读取发布权限用于签发 JWT。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/repository/AuthSystemConfigRepository.java` | 认证服务读取系统配置的接口，当前用于注册开关。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/repository/JdbcAuthSystemConfigRepository.java` | 基于 `JdbcTemplate` 的 `sys_config` 读取实现，查询启用配置值。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/service/AuthService.java` | 登录、注册、BCrypt 密码校验和 JWT 签发业务逻辑。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/service/AuthTokenService.java` | Token 刷新和退出登录黑名单业务逻辑。 |
| `xueyifang-auth/src/main/java/com/xueyifang/cloud/auth/token/RedisTokenBlacklistService.java` | Auth 服务 Redis Token 黑名单写入和查询实现。 |
| `xueyifang-auth/src/main/resources/application.yml` | 认证服务端口、服务名、Nacos、MySQL、Redis 和 JWT 配置。 |
| `xueyifang-user/pom.xml` | 用户服务 POM，依赖公共 Web、JDBC、MySQL、BCrypt、Apache POI 和 Commons CSV。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/XueyifangUserApplication.java` | 用户服务启动类。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/controller/UserProfileController.java` | 用户资料新接口，提供 `/users/me`、`/users/me/profile` 和 `/users/me/password`。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/controller/AuthUserCompatibilityController.java` | 旧 `/auth/currentUser`、`/auth/updateProfile` 和 `/auth/changePassword` 兼容入口。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/controller/PermissionController.java` | 发布权限申请和状态查询入口。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/controller/AdminPermissionController.java` | 后台发布权限审核入口，兼容 `/admin/users/pending` 和 `/admin/permission/review`。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/controller/AdminUserImportController.java` | 后台用户导入入口，提供 `/admin/user-import/upload` 和 `/admin/user-import/template`。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/repository/JdbcUserAccountRepository.java` | 基于 `JdbcTemplate` 的 `user` 表资料、发布权限申请、后台审核和导入查重/创建数据访问。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/service/UserProfileService.java` | 当前用户资料、资料更新、改密和发布权限状态业务逻辑。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/service/AdminPermissionService.java` | 后台发布权限待审列表、通过/驳回和审核通知触发业务逻辑。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/service/UserImportService.java` | 后台用户导入业务逻辑，解析 CSV/Excel、校验专业/账号、使用 BCrypt 密码入库并生成模板。 |
| `xueyifang-user/src/main/java/com/xueyifang/cloud/user/notification/HttpUserNotificationPublisher.java` | 用户服务审核通知发布实现，事务提交后调用消息服务内部通知接口。 |
| `xueyifang-user/src/main/resources/application.yml` | 用户服务端口、服务名、Nacos、MySQL 和审核通知配置。 |
| `xueyifang-service/pom.xml` | 服务市场 POM，依赖公共 Web、JDBC 和 MySQL。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/XueyifangServiceApplication.java` | 服务市场启动类。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/controller/ServiceCatalogController.java` | 服务市场入口，提供服务浏览、标签读取和发布者管理接口。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/controller/AdminServiceReviewController.java` | 后台服务审核入口，兼容 `/admin/services/pending` 和 `/admin/services/service/review`。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/controller/ServiceFavoriteController.java` | 服务收藏入口，提供收藏、取消收藏和我的收藏接口。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/controller/ServiceReviewController.java` | 服务评价入口，提供评价创建、公开列表和订单评价状态接口。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/dto/ServiceReviewCreateRequest.java` | 服务评价创建请求，校验订单 ID、评分、评价内容和匿名标记。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/dto/ServicePublishRequest.java` | 服务发布请求，兼容新字段和旧前端 `serviceTitle`/`serviceDescription` 字段。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/dto/ServiceUpdateRequest.java` | 服务编辑请求，兼容图片替换和旧前端字段。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/repository/JdbcServiceCatalogRepository.java` | 基于 `JdbcTemplate` 的 `service`、`service_image` 和 `service_tag` 查询/写入实现，并维护服务收藏数和服务审核状态。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/repository/JdbcServiceReviewModeRepository.java` | 读取 `sys_config.REVIEW_MODE`，决定服务发布或重新上架是否需要审核。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/repository/ReviewableOrder.java` | 评价创建所需的订单归属和状态快照。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/repository/ServiceReviewCreateCommand.java` | 服务评价写入命令。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/repository/JdbcServiceInteractionRepository.java` | 基于 `JdbcTemplate` 的 `service_favorite`、`service_review` 和评价所需订单状态查询实现。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/service/ServiceCatalogService.java` | 服务列表、详情可见性、标签读取、发布、编辑、上下架、删除、`REVIEW_MODE` 审核流、后台审核和详情收藏状态业务逻辑。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/service/ServiceFavoriteService.java` | 服务收藏、取消收藏、我的收藏、幂等和收藏数同步业务逻辑。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/service/ServiceReviewService.java` | 服务评价创建、公开列表、匿名展示和订单评价状态业务逻辑。 |
| `xueyifang-service/src/main/java/com/xueyifang/cloud/service/notification/HttpServiceNotificationPublisher.java` | 服务审核通知发布实现，事务提交后调用消息服务内部通知接口。 |
| `xueyifang-service/src/main/resources/application.yml` | 服务市场端口、服务名、Nacos、MySQL 和审核通知配置。 |
| `xueyifang-trade/pom.xml` | 交易服务 POM，依赖公共 Web、JDBC 和 MySQL。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/XueyifangTradeApplication.java` | 交易服务启动类，启用订单定时任务和任务配置绑定。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/config/TradeClockConfiguration.java` | 交易服务时间源配置，便于任务测试固定时间。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/config/TradeNotificationConfiguration.java` | 交易通知客户端配置，提供负载均衡 RestClient 调用消息服务。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/config/TradeNotificationProperties.java` | 交易通知配置，控制通知开关、消息服务地址和内部创建路径。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/config/TradeOrderTaskProperties.java` | 订单定时任务配置，包含开关、批量大小、超时时间和 cron 表达式。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/controller/OrderController.java` | 订单入口，提供创建、支付、取消、发货、确认完成、退款申请、卖家处理退款、买卖家列表和详情接口。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/controller/DisputeController.java` | 纠纷入口，提供买家发起纠纷、双方查询、管理员列表和管理员处理接口。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/controller/AdminDisputeCompatibilityController.java` | 旧后台纠纷兼容入口，提供 `/admin/dispute/list`、详情、按订单查纠纷和处理纠纷旧路径。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/controller/WalletController.java` | 钱包入口，提供钱包余额、钱包流水、充值和提现接口。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/controller/StatisticsController.java` | 用户销售统计入口，提供旧前端 `/statistics/sales`。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/OrderCreateRequest.java` | 创建订单请求。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/OrderPayRequest.java` | 支付订单请求。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/OrderRefundRequest.java` | 买家退款申请请求。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/SellerHandleRefundRequest.java` | 卖家处理退款申请请求。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/DisputeCreateRequest.java` | 纠纷创建请求，兼容旧前端 `description` 字段。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/DisputeHandleRequest.java` | 纠纷处理请求，兼容新字段 `approveRefund`/`handleRemark` 和旧字段 `actionType`/`adminReply`/`resolution`/`needRefund`。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/OrderListResponse.java` | 订单分页列表响应。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/OrderDetailResponse.java` | 订单详情响应。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/WalletBalanceResponse.java` | 钱包余额响应，包含可用余额、冻结金额和总资产。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/WalletTransactionListResponse.java` | 钱包流水分页响应。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/WalletTransactionResponse.java` | 钱包流水记录响应。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/WalletRechargeRequest.java` | 钱包充值请求。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/dto/WalletWithdrawRequest.java` | 钱包提现请求。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/notification/TradeNotificationPublisher.java` | 交易通知发布端口，隔离交易业务和消息服务实现。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/notification/HttpTradeNotificationPublisher.java` | HTTP 通知发布实现，事务提交后调用消息服务内部通知接口，失败仅记录日志。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/repository/TradeOrderRepository.java` | 订单、用户钱包、服务快照、销售统计、订单日志、钱包流水和订单任务候选数据访问接口。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/repository/JdbcTradeOrderRepository.java` | 基于 `JdbcTemplate` 的交易数据访问实现，包含销售统计和订单任务候选查询。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/repository/TradeDisputeRepository.java` | 纠纷数据访问接口，支持一单一纠纷和待处理纠纷检查。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/repository/JdbcTradeDisputeRepository.java` | 基于 `JdbcTemplate` 的纠纷数据访问实现。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/repository/WalletTransactionItem.java` | 钱包流水查询记录快照。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/repository/WalletTransactionPage.java` | 钱包流水分页快照。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/repository/WalletTransactionQuery.java` | 钱包流水分页查询条件。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/service/TradeOrderService.java` | 订单状态机、权限校验、钱包冻结/结算、退款、系统任务动作、分页查询和订单通知触发业务逻辑。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/service/TradeDisputeService.java` | 纠纷发起、查询、管理员裁决和纠纷通知触发业务逻辑。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/service/TradeOrderTaskService.java` | 订单定时任务批处理服务，扫描超时订单并逐单触发系统动作。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/service/TradeWalletService.java` | 钱包余额、流水查询、充值和提现业务逻辑。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/service/TradeStatisticsService.java` | 当前卖家销售统计业务逻辑，返回销量、收入、平均客单价、最畅销服务和最近订单。 |
| `xueyifang-trade/src/main/java/com/xueyifang/cloud/trade/task/TradeOrderTaskScheduler.java` | 订单定时任务调度入口，触发自动取消、自动确认收货和自动退款。 |
| `xueyifang-trade/src/main/resources/application.yml` | 交易服务端口、服务名、Nacos、MySQL 和订单定时任务配置。 |
| `xueyifang-system/pom.xml` | 系统服务 POM，依赖公共 Web、JDBC、MySQL、Actuator 和 Nacos。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/XueyifangSystemApplication.java` | 系统服务启动类。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/controller/ProfessionalController.java` | 专业字典旧路径入口，提供公开查询和兼容管理员写接口。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/controller/TradeLocationController.java` | 交易地点旧路径入口，提供公开查询和兼容管理员写接口。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/controller/SysConfigController.java` | 系统配置公开入口，提供注册开关状态。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/controller/AdminProfessionalController.java` | 后台专业管理入口。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/controller/AdminTradeLocationController.java` | 后台交易地点管理入口。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/controller/AdminSysConfigController.java` | 后台系统配置管理入口。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/controller/AdminStatisticsController.java` | 后台统计入口，提供 `/admin/statistics` 和 `/admin/statistics/trend`。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/repository/SystemDictionaryRepository.java` | 专业、交易地点和系统配置数据访问接口。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/repository/JdbcSystemDictionaryRepository.java` | 基于 `JdbcTemplate` 的系统字典和配置数据访问实现。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/repository/SystemStatisticsRepository.java` | 后台统计所需的用户、服务、订单、纠纷聚合查询接口。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/repository/JdbcSystemStatisticsRepository.java` | 基于 `JdbcTemplate` 的后台统计聚合查询实现。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/service/SystemDictionaryService.java` | 系统字典公开读取、管理员维护、注册开关和配置查询业务逻辑。 |
| `xueyifang-system/src/main/java/com/xueyifang/cloud/system/service/AdminStatisticsService.java` | 后台系统概览和近 7 天趋势统计业务逻辑。 |
| `xueyifang-system/src/main/resources/application.yml` | 系统服务端口、服务名、Nacos 和 MySQL 配置。 |
| `xueyifang-file/pom.xml` | 文件服务 POM，依赖公共 Web、Spring MVC、Actuator 和 Nacos。 |
| `xueyifang-file/src/main/java/com/xueyifang/cloud/file/XueyifangFileApplication.java` | 文件服务启动类。 |
| `xueyifang-file/src/main/java/com/xueyifang/cloud/file/config/FileStorageProperties.java` | 文件存储配置绑定，包含本地路径、访问前缀、允许类型和业务大小限制。 |
| `xueyifang-file/src/main/java/com/xueyifang/cloud/file/controller/FileController.java` | 文件入口，提供 `/file/upload`、`/file/upload/batch`、`/file/delete` 和 `/file/view/**`。 |
| `xueyifang-file/src/main/java/com/xueyifang/cloud/file/service/FileService.java` | 文件上传业务校验，校验登录用户、业务类型、大小和扩展名。 |
| `xueyifang-file/src/main/java/com/xueyifang/cloud/file/service/FileUploadBizType.java` | 文件上传业务类型枚举，当前支持用户头像和服务图片。 |
| `xueyifang-file/src/main/java/com/xueyifang/cloud/file/storage/FileStorageService.java` | 文件存储策略接口，隔离本地存储和后续 OSS 扩展。 |
| `xueyifang-file/src/main/java/com/xueyifang/cloud/file/storage/LocalFileStorageService.java` | 本地文件存储实现，负责落盘、删除、查看和路径遍历防护。 |
| `xueyifang-file/src/main/resources/application.yml` | 文件服务端口、服务名、Nacos、上传大小和本地存储配置。 |
| `xueyifang-file/src/test/java/com/xueyifang/cloud/file/service/FileServiceTest.java` | 文件服务单元测试，覆盖上传、批量上传、删除、类型/大小校验和路径安全。 |
| `xueyifang-message/pom.xml` | 消息服务 POM，依赖公共 Web、JDBC、Spring MVC、WebSocket、Redis、Actuator 和 Nacos。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/XueyifangMessageApplication.java` | 消息服务启动类。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/config/MessagePushConfiguration.java` | 消息实时推送配置，按开关注册 Redis pub/sub 监听容器。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/config/MessagePushProperties.java` | 消息实时推送配置属性，包含 Redis 广播开关、频道和实例标识。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/controller/ChatController.java` | 聊天入口，提供 `/chat/send`、`/chat/messages/{userId}` 和 `/chat/conversations`。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/controller/NotificationController.java` | 通知入口，提供 `/notification/my-notifications`、`/notification/unreadCount`、`/notification/{id}/read` 和 `/notification/readAll`。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/controller/InternalNotificationController.java` | 内部通知入口，提供服务间 `POST /internal/notifications` 创建通知。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/repository/JdbcMessageRepository.java` | 基于 `JdbcTemplate` 的 `user_chat`、`notification` 和用户轻量信息数据访问实现。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/service/ChatService.java` | 聊天业务逻辑，校验登录用户、接收者、消息类型，读取聊天记录时标记未读消息。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/service/NotificationService.java` | 通知业务逻辑，支持通知创建、查询、未读数和已读状态。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/websocket/MessageWebSocketConfiguration.java` | WebSocket `/ws` 注册，兼容 Gateway 用户头和旧前端 `?token=` 握手。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/websocket/MessageWebSocketSessionManager.java` | 单实例 WebSocket 会话表和在线推送。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/websocket/WebSocketMessagePushService.java` | 默认本机 WebSocket 推送实现，在 Redis 广播关闭时启用。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/websocket/RedisBroadcastMessagePushService.java` | Redis 广播推送实现，先投递本机连接，再发布跨实例实时消息。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/websocket/RedisMessagePushSubscriber.java` | Redis 实时消息订阅者，过滤本实例广播并投递其他实例消息。 |
| `xueyifang-message/src/main/java/com/xueyifang/cloud/message/websocket/RedisRealtimePushMessage.java` | Redis 广播消息载荷，记录来源实例、目标用户和实时消息体。 |
| `xueyifang-message/src/main/resources/application.yml` | 消息服务端口、服务名、Nacos、MySQL、Redis、JWT 和消息推送配置。 |
| `xueyifang-message/src/test/java/com/xueyifang/cloud/message/service/ChatServiceTest.java` | 聊天服务单元测试，覆盖发送、会话未读、禁用接收者、自发消息和登录校验。 |
| `xueyifang-message/src/test/java/com/xueyifang/cloud/message/service/NotificationServiceTest.java` | 通知服务单元测试，覆盖创建推送、未读数、已读权限、通知类型和登录校验。 |
| `xueyifang-message/src/test/java/com/xueyifang/cloud/message/websocket/RedisBroadcastMessagePushServiceTest.java` | Redis 广播推送单元测试，覆盖本机投递、广播发布和 Redis 异常降级。 |
| `xueyifang-message/src/test/java/com/xueyifang/cloud/message/websocket/RedisMessagePushSubscriberTest.java` | Redis 广播订阅单元测试，覆盖跨实例投递和本实例消息过滤。 |

## 部署联调状态

- GitHub 公开仓库：`https://github.com/2832599985/xueyifang-cloud`，当前 `master` 已推到 `origin/master`，最新提交为 `f301722 fix: wire auth token blacklist constructor`。
- 当前旧前端 HTTP 调用点路径覆盖已到 `80/80`；核心缺口提交为 `fa7accf feat: complete legacy frontend api coverage`，包含后台统计、用户销售统计、后台用户导入、旧后台纠纷兼容和 Gateway 路由补齐。
- 服务器 `hk_domain`（香港，约 2G 内存）已尝试部署：Docker、Java 21、MySQL/Redis/Nacos 和 JAR 上传均验证过；全套 8 个 Spring Boot 服务同时运行会明显吃 swap，不适合作为完整后端联调主机。临时应用进程和 `xueyifang` Compose 基础设施已停止，数据卷/部署目录保留在 `/home/ubuntu/xueyifang-cloud-deploy`。
- 香港部署过程中发现并修复了 `xueyifang-auth` Spring 上下文启动问题：`RedisTokenBlacklistService` 的测试构造器导致 Spring 选择无参构造失败，已在 `f301722` 中通过 `@Autowired` 明确生产构造器。
- 香港部署还确认了 Nacos 注意事项：当 Nacos 容器设置 `NACOS_AUTH_ENABLE=false` 时，应用环境变量不要再传 `NACOS_USERNAME=nacos` / `NACOS_PASSWORD=nacos`，否则 Nacos 3 客户端会尝试登录并报 `User nacos not found`。
- 服务器 `aws_43_213_28_91`（台湾/台北 AWS，约 8G 内存）已确认更适合跑完整链路：Docker 和 Compose 可用，`sudo` 可用，防火墙 inactive；当前未安装 Java/Maven。本机已有服务占用宿主机 `3306` 和 `127.0.0.1:8080`，部署时需避开。
- 台湾机建议端口规划：MySQL 暴露 `13306:3306`，Redis 暴露 `16379:6379`，Nacos 暴露 `18848:8848`、`19848:9848`、`19849:9849`；Gateway 使用 `18080`，业务服务继续使用 `8100` 到 `8700`。应用环境需同时设置 `XUEYIFANG_MYSQL_*` 和 `MYSQL_*`，因为不同模块当前读取的变量名不完全一致。
- 下一步从台湾机继续：安装 Java 21，拉取 `origin/master` 的 `f301722`，使用独立部署目录启动基础设施和 8 个服务，等 Nacos 注册稳定后通过 `http://<server>:18080` 做注册、登录、字典、服务列表、统计和用户导入模板等 Gateway 冒烟。
- 本地工作区仍有未纳入提交的 `AIREADME.md`、`.mcp-ssh.lock` 和 `.tmp-mcp-ssh-tests/`，不要清理或回滚这些与本轮部署无关的文件。

## Todo

- 如需完整还原旧纠纷后台体验，补 `service_dispute.dispute_type` 持久化，并确认“处理但不退款”状态机语义。
- 做一次本地全链路启动验证，覆盖 Gateway 到认证、用户、服务、交易、系统、文件和消息服务的健康检查。
- 使用旧前端做冒烟，重点跑登录、服务浏览、下单、退款/纠纷、统计、用户导入、通知和文件上传。
- 资金规则复杂后再评估是否拆出钱包服务。
- 多实例部署 `xueyifang-message` 前，基于已接入 Redis pub/sub 做联调压测，并按可靠性要求评估消息队列、离线补偿或网关粘性会话。
- 明确 Nacos 生产环境鉴权和外置数据库方案。
