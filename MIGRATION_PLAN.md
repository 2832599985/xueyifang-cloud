# xueyifang-cloud 迁移计划

## 工作原则

- 先拆边界，再写代码；每一步都要能独立构建或独立验证。
- 每完成一个阶段，立即更新本文档的进度和 `PROJECT_MEMORY.md`。
- 每完成一个模块、阶段或明确任务，完成必要校验后创建 Git 提交；提交信息要说明变更内容和验证结果。
- 不直接照搬原单体结构；先识别领域边界，再落到服务、模块、表和接口。
- 当前项目从空仓库开始，优先建立可持续演进的基础设施。

## 当前状态

| 项目 | 状态 | 说明 |
| --- | --- | --- |
| 仓库检查 | 已完成 | 根目录原本仅有 `AIREADME.md`，尚未初始化 Git。 |
| 计划文档 | 已完成 | 已建立本迁移计划，后续按阶段更新进度。 |
| 项目记忆 | 已完成 | 已创建 `PROJECT_MEMORY.md`，作为代码索引和 Todo 记录。 |
| Spring Cloud 骨架 | 已完成 | 已写入 Maven 父工程、公共模块、网关和四个业务服务模块。 |
| Git 初始化 | 已完成 | 已创建本地 Git 仓库，并加入基础忽略和换行规则。 |
| 原项目分析 | 已完成 | 已拉取原后端、前端项目到仓库外参考目录，并输出初版盘点文档。 |
| 阶段 3 横切基础 | 已完成 | 已迁移统一响应、错误码、业务异常、Servlet 全局异常处理和 requestId 日志上下文。 |
| 阶段 4 认证与用户基础 | 进行中 | 已新增 JWT 公共能力、Gateway Bearer Token 校验、Auth 登录/注册/登出、Redis Token 黑名单、User 当前用户资料和发布权限接口。 |
| 阶段 5 服务市场与交易最短链路 | 进行中 | `xueyifang-service` 已接入服务浏览、发布者管理和互动链路；`xueyifang-trade` 已接入订单创建、支付、取消、发货、确认完成、退款、钱包和买卖家订单查询。 |

## 阶段计划

### 阶段 0：项目初始化

目标：让空仓库具备可维护的工程基础。

任务：

- [x] 创建 `PROJECT_MEMORY.md`。
- [x] 初始化 Git 仓库。
- [x] 创建 `.gitignore`、`README.md`、基础目录和文档目录。
- [x] 建立 Maven 多模块父工程。
- [x] 完成一次空骨架构建校验。

验收标准：

- `mvn clean verify` 能在无业务代码时通过。
- 根目录有明确的 README、计划文档和项目记忆。
- 后续新增模块有统一命名、包名和依赖管理方式。

### 阶段 1：原单体项目盘点

目标：弄清楚单体项目到底有哪些业务，而不是凭感觉拆服务。

任务：

- [x] 获取原后端项目 `2832599985/xueyifang-backend`。
- [x] 获取原前端项目 `2832599985/xueyifang-frontend`。
- [x] 梳理后端包结构、数据库表、接口、权限模型和外部依赖。
- [x] 梳理前端页面、路由、接口调用和角色入口。
- [x] 输出模块盘点文档。

验收标准：

- 每个业务域都有来源文件路径和功能短评。
- 标出高风险改造点，例如登录鉴权、文件上传、支付、定时任务、跨表事务。
- 明确第一批迁移范围。

### 阶段 2：服务边界设计

目标：确定 Spring Cloud 服务拆分边界，避免拆得过碎。

初始建议边界：

- `xueyifang-gateway`：统一入口、路由、跨域、限流、认证透传。
- `xueyifang-auth`：登录、令牌、权限聚合。
- `xueyifang-user`：用户、角色、资料、账号状态。
- `xueyifang-service`：服务发布、浏览、收藏、评价展示。
- `xueyifang-trade`：订单、钱包流水、退款、纠纷。
- `xueyifang-common`：通用返回结构、异常、工具、Web 约定。

暂缓拆出的候选服务：

- `xueyifang-message`：聊天、通知、WebSocket。
- `xueyifang-system`：专业、交易地点、系统配置、字典。
- `xueyifang-file`：上传、删除、访问、本地或 OSS 存储。

验收标准：

- 每个服务都有清晰职责和不做什么。
- 服务间调用关系画清楚。
- 数据归属和事务边界有初版判断。

进度：

- [x] 输出 `docs/service-boundary-design.md`。
- [x] 将 `xueyifang-content` 校准为 `xueyifang-service`。
- [x] 新增 `xueyifang-trade`。
- [x] 更新网关静态路由。

### 阶段 3：基础设施落地

目标：搭出能跑的微服务开发环境。

任务：

- [x] 接入 Nacos 或 Spring Cloud Config 的配置方案，优先按项目实际部署环境定。
- [x] 配置 Gateway 路由。
- [x] 配置统一异常、统一响应、基础日志。
- [x] 准备本地 Docker Compose，覆盖数据库、Redis、Nacos 等基础依赖。
- [x] 增加基础 CI。

验收标准：

- 本地能启动网关和至少一个业务服务。
- 健康检查可用。
- 通过网关能访问业务服务的测试接口。

### 阶段 4：认证与用户优先迁移

目标：先打通系统入口，再迁移业务。

任务：

- 迁移登录、注册、Token 刷新、退出登录。
- 迁移用户资料和角色权限基础能力。
- 建立前后端接口契约。
- 补充认证链路测试。

进度：

- [x] 在 `common-core` 建立 JWT 签发、解析、刷新和 Bearer Token 解析基础能力。
- [x] 在 Gateway 接入 Bearer Token 校验、公开路径白名单和可信 `X-User-*` 用户上下文透传。
- [x] 在 `common-web` 自动解析 Gateway 透传的 `X-User-*` 用户上下文。
- [x] 在认证服务新增 `POST /auth/token/refresh`。
- [x] 在认证服务接入 `user` 表，新增 `POST /auth/login` 和 `POST /auth/register`。
- [x] 接入 Redis Token 黑名单，新增 `POST /auth/logout`，并让 Gateway 拒绝已登出 Token。
- [x] 在用户服务接入 `user` 表，新增当前用户、资料更新、改密和发布权限状态接口。
- [x] 保留 `/auth/currentUser`、`/auth/updateProfile`、`/auth/changePassword` 兼容路径，并输出认证与用户接口契约。

验收标准：

- 用户能完成登录和鉴权访问。
- 无权限、过期令牌、非法令牌都有明确响应。
- 前端可以切换到新认证接口进行联调。

### 阶段 5：业务模块逐步迁移

目标：按领域和风险拆分迁移业务，而不是一次性重写。

任务：

- 每次只迁移一个清晰业务域。
- 为每个业务域记录来源路径、目标服务、接口变化、数据表归属。
- 给每批迁移补最小可用测试。
- 保留兼容策略，避免前端一次性大改。

进度：

- [x] 在 `xueyifang-service` 接入 `service`、`service_image` 和 `service_tag` 表。
- [x] 新增 `GET /service/list`、`GET /service/{serviceId}` 和 `GET /service/tags`。
- [x] 服务详情按已上架公开可见，发布者或管理员可查看非公开服务。
- [x] 输出服务市场接口契约和本地初始化 SQL。
- [x] 新增 `POST /service/publish`、`GET /service/myServices`、`PUT /service/{serviceId}`、`PUT /service/{serviceId}/online`、`PUT /service/{serviceId}/offline` 和 `DELETE /service/{serviceId}`。
- [x] 服务发布、编辑、上下架和删除按登录用户、发布权限、发布者/管理员权限和状态机校验。
- [x] 新增 `POST /favorite/collect`、`DELETE /favorite/collect/{serviceId}`、`GET /favorite/myCollections`、`GET /review/service/{serviceId}` 和 `GET /review/order/{orderId}/status`。
- [x] 收藏写入保持幂等并同步服务收藏数；评价列表支持匿名展示。
- [x] 在 `xueyifang-trade` 接入 `service_order`、`service_order_log` 和 `wallet_transaction` 表。
- [x] 新增 `POST /order/create`、`POST /order/{orderId}/pay`、`POST /order/{orderId}/cancel`、`POST /order/{orderId}/ship`、`POST /order/{orderId}/confirm`、`GET /order/myOrders`、`GET /order/mySellingOrders` 和 `GET /order/{orderId}`。
- [x] 订单支付按钱包余额扣减和冻结金额流转；买家确认完成后结算给卖家，并记录钱包流水和订单日志。
- [x] 回接 `POST /review/create`，按订单买家、已完成状态和订单唯一评价校验后写入评价并刷新服务评分。
- [x] 新增 `POST /order/{orderId}/refund` 和 `POST /order/{orderId}/handleRefund`，支持待发货直接退款、待收货退款申请、卖家同意退款和卖家拒绝退款。
- [x] 新增 `GET /wallet/balance`、`GET /wallet/transactions`、`POST /wallet/recharge` 和 `POST /wallet/withdraw`，接入钱包余额、冻结金额和钱包流水分页查询。

验收标准：

- 每个迁移批次都能独立验收。
- 旧接口和新接口的差异有记录。
- 数据一致性风险有处理方案。

## 进度日志

| 时间 | 阶段 | 进度 | 说明 |
| --- | --- | --- | --- |
| 2026-06-14 | 阶段 0 | 已完成 | 检查当前目录，确认原始仓库只有 `AIREADME.md`，且尚未初始化 Git。 |
| 2026-06-14 | 阶段 0 | 已完成 | 创建迁移计划文档，确定后续按阶段更新进度。 |
| 2026-06-14 | 阶段 0 | 已完成 | 创建 `PROJECT_MEMORY.md`，记录项目索引、计划模块和 Todo。 |
| 2026-06-14 | 阶段 0 | 已完成 | 创建 `.gitignore`、`README.md`、Maven 父工程和初始 Spring Cloud 多模块骨架。 |
| 2026-06-14 | 阶段 0 | 已完成 | 使用 Java 21 执行 `mvn clean verify`，8 个模块构建通过。 |
| 2026-06-14 | 阶段 0 | 已完成 | 初始化 Git 仓库，确认构建产物未进入待提交列表，并补充 `.gitattributes`。 |
| 2026-06-14 | 阶段 0 | 已完成 | 补充提交约定：每完成一个模块、阶段或明确任务后创建带说明的 Git 提交。 |
| 2026-06-14 | 阶段 1 | 已完成 | 拉取原后端和前端项目到 `D:\_Code\Java\_reference\xueyifang-original`，未将旧源码纳入新仓库。 |
| 2026-06-14 | 阶段 1 | 已完成 | 输出 `docs/original-project-inventory.md`，记录后端业务域、API 分组、数据表、前端页面、横切能力和拆分风险。 |
| 2026-06-14 | 阶段 2 | 已完成 | 输出 `docs/service-boundary-design.md`，确定第一批落地服务为网关、认证、用户、服务市场和交易。 |
| 2026-06-14 | 阶段 2 | 已完成 | 将 `xueyifang-content` 校准为 `xueyifang-service`，新增 `xueyifang-trade`，并更新网关路由。 |
| 2026-06-14 | 阶段 2 | 已完成 | 使用 Java 21 执行 `mvn clean verify`，9 个模块构建通过。 |
| 2026-06-14 | 阶段 3 | 已完成 | 增加 Docker Compose 本地基础设施，覆盖 MySQL、Redis 和 Nacos。 |
| 2026-06-14 | 阶段 3 | 已完成 | 接入 Spring Cloud Alibaba Nacos Discovery 和 Config，并将网关路由改为 `lb://` 服务名。 |
| 2026-06-14 | 阶段 3 | 已完成 | 使用 Java 21 执行 `mvn clean verify`，9 个模块构建通过；执行 `docker compose config --quiet` 通过。 |
| 2026-06-14 | 阶段 3 | 已完成 | 在 `common-core` 迁移 `BaseResponse`、`ErrorCode`、`ResultUtils`、`BusinessException`、用户上下文和链路常量；在 `common-web` 自动装配 Servlet 全局异常处理和 requestId 过滤器；在 Gateway 生成或透传 `X-Request-Id`。 |
| 2026-06-14 | 阶段 3 | 已完成 | 补充通用响应、异常映射、Servlet requestId、Gateway requestId 测试；使用 Java 21 执行 `mvn clean verify`，9 个模块构建通过。 |
| 2026-06-14 | 阶段 3 | 已完成 | 增加 GitHub Actions 基础 CI，执行 Docker Compose 配置校验和 `mvn -B clean verify`。 |
| 2026-06-14 | 阶段 4 | 进行中 | 新增 JWT 公共能力、Gateway 鉴权过滤器和 Auth Token 刷新接口；相关模块测试通过。 |
| 2026-06-14 | 阶段 4 | 进行中 | 在 `common-web` 自动解析 Gateway 透传的 `X-User-*` 用户上下文，并补充 ThreadLocal 清理测试。 |
| 2026-06-14 | 阶段 4 | 进行中 | 在 `xueyifang-auth` 接入 `user` 表登录/注册、BCrypt 密码校验、Redis Token 黑名单和登出接口；Gateway 增加黑名单 Token 拒绝。 |
| 2026-06-14 | 阶段 4 | 进行中 | 在 `xueyifang-user` 接入 `user` 表当前用户、资料更新、改密和发布权限状态接口；Gateway 将旧 `/auth/currentUser` 等资料路径兼容转发到用户服务。 |
| 2026-06-14 | 阶段 5 | 进行中 | 在 `xueyifang-service` 接入服务市场只读链路，新增服务列表、详情、标签接口和 `service`/`service_image`/`service_tag` 初始化脚本。 |
| 2026-06-14 | 阶段 5 | 进行中 | 在 `xueyifang-service` 补服务发布、我的服务、编辑、上下架和逻辑删除接口，并补充图片替换和权限/状态单测。 |
| 2026-06-14 | 阶段 5 | 进行中 | 在 `xueyifang-service` 补收藏、我的收藏、评价公开列表和订单评价状态接口，并补充互动表初始化脚本和单测。 |
| 2026-06-14 | 阶段 5 | 进行中 | 在 `xueyifang-trade` 补订单最短链路，新增下单、支付、取消、发货、确认完成、买卖家列表和详情接口，并补充交易表初始化脚本和资金流单测。 |
| 2026-06-14 | 阶段 5 | 进行中 | 回接 `xueyifang-service` 评价创建接口，基于已完成订单校验买家归属、唯一评价并刷新服务评分。 |
| 2026-06-14 | 阶段 5 | 进行中 | 在 `xueyifang-trade` 补退款和钱包基础链路，新增买家退款申请、卖家处理退款、钱包余额、钱包流水、充值和提现接口，并补充资金流单测。 |

## 待确认事项

- 是否使用 Nacos 作为注册中心和配置中心。按国内 Spring Cloud Alibaba 项目经验，Nacos 是合理默认值。
- 第一批迁移已采用“认证、用户、服务列表、订单最短链路”的顺序；后续优先补纠纷、订单定时任务和系统字典。
- 消息、系统、文件三个候选服务何时拆出，建议在主交易链路跑通后再决定。
- Nacos 生产环境鉴权和外置数据库方案。
