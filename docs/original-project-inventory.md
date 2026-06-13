# 原项目盘点

## 来源

| 项目 | 参考路径 | 当前提交 | 说明 |
| --- | --- | --- | --- |
| 后端 | `D:\_Code\Java\_reference\xueyifang-original\xueyifang-backend` | `2c1a97d` | Spring Boot 单体后端。 |
| 前端 | `D:\_Code\Java\_reference\xueyifang-original\xueyifang-frontend` | `73bd6fc` | Vue 3 单页应用。 |

旧项目源码保存在仓库外，只作为迁移参考。新仓库只提交分析文档、迁移代码和必要配置。

## 后端基线

| 项目 | 现状 |
| --- | --- |
| 构建 | Maven 单模块，`pom.xml`。 |
| Java | Java 8。 |
| Spring | Spring Boot 2.7.2。 |
| 数据访问 | MyBatis-Plus 3.5.2，XML Mapper。 |
| 数据库 | MySQL，主库名 `xueyifang`。 |
| 缓存 | Redis，用于 Token 黑名单、缓存、限流和幂等。 |
| 认证 | JWT，Header 为 `Authorization: Bearer {token}`。 |
| API 文档 | Knife4j。 |
| 实时能力 | Spring WebSocket，路径 `/api/ws`。 |
| 文件存储 | 本地目录或阿里云 OSS。 |

关键路径：

| 路径 | 功能短评 |
| --- | --- |
| `pom.xml` | Java 8、Spring Boot 2.7.2、MyBatis-Plus、Redis、JWT、Knife4j、OSS、POI 等依赖。 |
| `src/main/resources/application.yml` | 单体服务端口、`/api` 上下文、MySQL、Redis、JWT、文件存储配置。 |
| `src/main/resources/application-dev.yml` | 开发环境数据库和 Redis 覆盖配置。 |
| `script/xueyifang-clean-init.sql` | 推荐初始化脚本，包含完整表结构、字典数据和测试账号。 |
| `src/main/java/com/xueyifang/config/WebMvcConfig.java` | JWT 拦截器注册和公开接口放行规则。 |
| `src/main/java/com/xueyifang/config/WebSocketConfig.java` | WebSocket `/ws` 注册。 |

## 后端业务域

| 业务域 | 主要后端路径 | 数据表 | 微服务归属建议 |
| --- | --- | --- | --- |
| 认证与用户 | `UserController`、`UserServiceImpl`、`PermissionController` | `user` | `xueyifang-auth` 负责令牌，`xueyifang-user` 负责用户资料和权限状态。 |
| 服务市场 | `ServiceController`、`ServiceServiceImpl`、`ServiceTagController`、`ServiceImageController` | `service`、`service_image`、`service_tag`、`service_favorite` | 建议独立为 `xueyifang-service`，当前骨架中的 `xueyifang-content` 应在阶段 2 校准命名。 |
| 订单交易 | `ServiceOrderController`、`ServiceOrderServiceImpl`、`ServiceOrderLogController` | `service_order`、`service_order_log` | 建议独立为 `xueyifang-trade`，订单是跨用户、服务、钱包的核心交易边界。 |
| 钱包资金 | `WalletController`、`WalletServiceImpl`、`WalletTransactionServiceImpl` | `user.balance`、`wallet_transaction` | 可先并入 `xueyifang-trade`，后期资金规则复杂时再拆钱包服务。 |
| 纠纷 | `ServiceDisputeController`、`AdminDisputeController`、`ServiceDisputeServiceImpl` | `service_dispute` | 建议归入 `xueyifang-trade`，与订单状态和退款强相关。 |
| 聊天通知 | `ChatController`、`NotificationController`、`WebSocket*` | `user_chat`、`notification` | 建议独立为 `xueyifang-message`，也可第一阶段先随交易服务迁移。 |
| 字典配置 | `ProfessionalController`、`TradeLocationController`、`SysConfigController` | `professional`、`trade_location`、`sys_config`、`admin_setting` | 建议独立为 `xueyifang-system`，管理后台和公共查询都依赖它。 |
| 后台管理 | `Admin*Controller` | 多表聚合 | 不建议作为单独数据服务；更适合作为各服务的管理接口或 BFF 聚合层。 |
| 文件上传 | `FileController`、`FileServiceImpl`、`FileStorageStrategy*` | 无独立业务表，服务图片落 `service_image` | 建议独立为 `xueyifang-file` 或作为公共基础服务，优先保持接口兼容。 |
| 统计 | `StatisticsController`、`AdminController` | 多表聚合 | 建议后置迁移，初期可按查询所在业务服务提供，后期再做统计读模型。 |

## API 分组

| API 前缀 | 代表接口 | 说明 | 迁移目标 |
| --- | --- | --- | --- |
| `/auth` | 登录、注册、登出、当前用户、修改资料、改密 | 认证和用户资料混在同一 Controller。 | 拆为 `auth` 和 `user` 两层职责。 |
| `/permission` | 申请发布权限、查询权限状态 | 用户权限流。 | `xueyifang-user`。 |
| `/service` | 发布、列表、详情、编辑、上下架、删除 | 核心服务市场。 | `xueyifang-service`。 |
| `/service/tags` | 服务标签列表 | 字典类公共查询。 | `xueyifang-system` 或 `xueyifang-service`，阶段 2 决定。 |
| `/order` | 创建、支付、取消、发货、确认、退款、买卖家订单 | 核心交易流程。 | `xueyifang-trade`。 |
| `/wallet` | 余额、流水、充值、提现 | 钱包和资金流水。 | 初期归 `xueyifang-trade`。 |
| `/dispute` | 发起纠纷、我的纠纷、纠纷详情 | 订单纠纷。 | `xueyifang-trade`。 |
| `/favorite` | 收藏、取消收藏、我的收藏 | 服务和用户之间的关系。 | `xueyifang-service`，需读取用户身份。 |
| `/review` | 创建评价、服务评价、订单评价状态 | 服务评价依赖订单完成状态。 | `xueyifang-service` 或 `xueyifang-trade`，需阶段 2 定边界。 |
| `/chat` | 发送消息、聊天记录、会话列表 | 用户间消息。 | `xueyifang-message`。 |
| `/notification` | 通知列表、未读数、标记已读 | 系统通知。 | `xueyifang-message`。 |
| `/file` | 上传、批量上传、删除、查看 | 文件基础能力。 | `xueyifang-file` 或公共基础服务。 |
| `/professional` | 专业查询和管理 | 字典资源。 | `xueyifang-system`。 |
| `/trade-location` | 地点查询和管理 | 字典资源。 | `xueyifang-system`。 |
| `/sys-config` | 注册开关 | 公共系统配置。 | `xueyifang-system`。 |
| `/admin/*` | 审核、统计、配置、用户导入、资源管理 | 管理后台接口。 | 按业务域拆到对应服务的 admin 接口。 |

## 数据表清单

| 表 | 说明 | 归属建议 |
| --- | --- | --- |
| `user` | 用户、角色、发布权限、账号状态、余额。 | 用户基础信息归 `xueyifang-user`，余额字段迁移时需与钱包归属重新设计。 |
| `service` | 服务主体。 | `xueyifang-service`。 |
| `service_image` | 服务图片。 | `xueyifang-service`，文件实际存储由文件服务负责。 |
| `service_tag` | 服务标签。 | `xueyifang-system` 或 `xueyifang-service`。 |
| `service_favorite` | 用户收藏服务。 | `xueyifang-service`。 |
| `service_order` | 服务订单。 | `xueyifang-trade`。 |
| `service_order_log` | 订单状态和资金变更日志。 | `xueyifang-trade`。 |
| `wallet_transaction` | 钱包流水。 | `xueyifang-trade` 或后续 `xueyifang-wallet`。 |
| `service_dispute` | 订单纠纷。 | `xueyifang-trade`。 |
| `service_review` | 服务评价。 | 倾向 `xueyifang-service`，但创建评价需校验订单。 |
| `user_chat` | 聊天记录。 | `xueyifang-message`。 |
| `notification` | 通知。 | `xueyifang-message`。 |
| `professional` | 专业字典。 | `xueyifang-system`。 |
| `trade_location` | 交易地点字典。 | `xueyifang-system`。 |
| `sys_config` | 系统配置。 | `xueyifang-system`。 |
| `admin_setting` | 管理员设置。 | `xueyifang-system`。 |

## 前端基线

| 项目 | 现状 |
| --- | --- |
| 框架 | Vue 3.3.4 + TypeScript + Vite 4.4.9。 |
| UI | Element Plus。 |
| 状态管理 | Pinia。 |
| HTTP | Axios，`src/api/request.ts` 统一封装，`baseURL = /api`。 |
| 路由 | Vue Router，路由守卫区分普通登录、发布权限和管理员。 |
| WebSocket | `src/composables/useWebSocket.ts`，开发环境连接 `ws://{host}:8080/api/ws`。 |

关键路径：

| 路径 | 功能短评 |
| --- | --- |
| `package.json` | 前端依赖和构建脚本。 |
| `vite.config.ts` | 端口 3000，`/api` 代理到 `http://localhost:8080`。 |
| `src/api/request.ts` | Axios 请求和响应拦截，`code === 0` 返回 `data`。 |
| `src/router/index.ts` | 页面路由和权限守卫。 |
| `src/stores/user.ts` | 当前用户状态、获取用户、退出登录。 |
| `src/composables/useWebSocket.ts` | 全局 WebSocket 连接、重连和消息分发。 |

## 前端页面与 API 模块

| 前端范围 | 页面路径 | API 文件 |
| --- | --- | --- |
| 认证 | `src/views/auth/Login.vue`、`src/views/auth/Register.vue` | `src/api/auth.ts` |
| 首页和服务浏览 | `src/views/home/Home.vue`、`src/views/service/*` | `src/api/service.ts`、`src/api/dict.ts`、`src/api/review.ts` |
| 订单 | `src/views/order/*` | `src/api/order.ts` |
| 钱包 | `src/views/wallet/*` | `src/api/wallet.ts` |
| 收藏 | `src/views/favorite/MyFavorites.vue` | `src/api/favorite.ts` |
| 纠纷 | `src/views/dispute/*` | `src/api/dispute.ts` |
| 聊天 | `src/views/chat/ChatPage.vue` | `src/api/chat.ts`、`src/composables/useWebSocket.ts` |
| 通知 | `src/views/notification/NotificationList.vue` | `src/api/notification.ts` |
| 用户中心 | `src/views/user/UserCenter.vue` | `src/api/auth.ts`、`src/api/permission.ts` |
| 管理后台 | `src/views/admin/*`、`src/layouts/AdminLayout.vue` | `src/api/admin.ts`、`src/api/adminService.ts`、`src/api/sysConfig.ts`、`src/api/professional.ts`、`src/api/tradeLocation.ts`、`src/api/dispute.ts` |

## 横切能力

| 能力 | 原实现 | 迁移建议 |
| --- | --- | --- |
| 统一响应 | `BaseResponse`、`ErrorCode`、`ResultUtils` | 放入 `xueyifang-common-core`，保持 `code/message/data` 兼容。 |
| 全局异常 | `BusinessException`、`GlobalExceptionHandler` | 放入 `xueyifang-common-web`，业务服务复用。 |
| JWT 鉴权 | `JwtAuthInterceptor`、`JwtUtils`、Redis Token 黑名单 | 认证服务签发 Token，网关完成基础校验，业务服务接收可信用户上下文。 |
| 权限注解 | `@AuthCheck` 和 AOP | 可迁入 `common-web`，但管理员权限和发布权限的数据读取应由用户服务提供。 |
| 限流 | `@RateLimit` 和 Redis 滑动窗口 | 优先放到网关，业务内保留关键写接口幂等。 |
| 幂等 | `@IdempotentSubmit` 和 Redis SETNX | 放入 `common-web`，订单支付、权限申请、收藏等写接口继续使用。 |
| 缓存 | `RedisUtils`、各业务手写缓存 Key | 阶段 1 不直接复用，先梳理 Key 归属，再按服务拆分。 |
| 定时任务 | 订单自动取消、自动确认收货、自动退款 | 归入交易服务，后续考虑 ShedLock 或分布式任务调度，避免多实例重复执行。 |
| WebSocket | 单体内存会话表 | 如果拆为消息服务，需要考虑多实例会话、网关转发和离线消息。 |
| 文件 | 本地或 OSS 策略 | 保留策略模式，接口先兼容 `/file/*`。 |

## 拆分风险

| 风险 | 依据 | 处理建议 |
| --- | --- | --- |
| 订单强依赖用户、服务、钱包和通知 | `ServiceOrderServiceImpl` 注入用户、服务、地点、日志、通知、配置、图片、评价、专业、钱包流水等能力。 | 第一轮不要把交易链路拆得太碎。先做交易服务，内部管理订单、钱包流水和纠纷。 |
| 钱包余额在 `user` 表 | `WalletServiceImpl` 直接改用户余额，并写钱包流水。 | 迁移时应把余额从用户域剥离，至少在交易服务中建立钱包账户模型。 |
| 服务评价跨订单和服务 | 评价创建需要订单状态，展示又属于服务详情。 | 可以先由服务服务持有评价表，通过交易服务校验订单，或先与交易同域迁移。 |
| WebSocket 使用单体内存会话 | `WebSocketSessionManager` 用内存 Map 保存用户连接。 | 多实例后要引入消息广播或粘性会话，初期可单实例运行消息服务。 |
| 管理后台是多域聚合 | `/admin/*` 同时审核用户、服务、纠纷、配置、字典和统计。 | 不要建一个“大 admin 服务”接管所有数据。管理接口应跟随数据归属。 |
| 注册和系统配置耦合 | 前端注册路由会调用 `/sys-config/register-status`。 | 认证服务注册前需要读取系统配置，初期可通过系统服务提供轻量接口。 |
| 旧接口路径已被前端固定 | 前端统一 `baseURL=/api`，大量 API 文件直接写旧路径。 | 网关优先兼容旧路径，再逐步调整前端 API。 |

## 阶段 2 服务边界建议

当前骨架已有 `xueyifang-auth`、`xueyifang-user`、`xueyifang-content`。盘点后建议在阶段 2 调整为更贴合业务的边界：

| 建议服务 | 主要职责 | 说明 |
| --- | --- | --- |
| `xueyifang-gateway` | 路由、跨域、基础鉴权、限流。 | 保持。 |
| `xueyifang-auth` | 登录、注册、登出、Token 签发和黑名单。 | 保持，但用户资料不放这里。 |
| `xueyifang-user` | 用户资料、角色、发布权限、账号状态。 | 保持。 |
| `xueyifang-service` | 服务发布、浏览、图片关系、标签、收藏、评价展示。 | 建议替代当前 `xueyifang-content`。 |
| `xueyifang-trade` | 订单、退款、纠纷、钱包流水、订单任务。 | 建议新增，是交易主干。 |
| `xueyifang-message` | 聊天、通知、WebSocket。 | 可第二批拆，第一批可先保守。 |
| `xueyifang-system` | 专业、交易地点、系统配置、字典。 | 可第二批拆，认证和服务会依赖它。 |
| `xueyifang-file` | 上传、删除、访问、本地或 OSS 存储。 | 可后置，先保持接口兼容。 |

第一批落地建议：先重命名或新增 `xueyifang-service` 和 `xueyifang-trade`，再迁移认证、用户、服务列表和订单最短链路。

