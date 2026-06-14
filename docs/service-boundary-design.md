# 服务边界设计

## 结论

阶段 2 采用“少拆一点，但边界要准”的策略。当前先保留网关、认证、用户、服务市场、交易和系统六类核心服务；消息和文件服务暂不落空模块，等第一批业务跑通后再拆。

## 当前落地服务

| 服务 | 端口 | 职责 | 旧接口前缀 |
| --- | --- | --- | --- |
| `xueyifang-gateway` | `8080` | 统一入口、路径路由、后续承接跨域、限流和基础鉴权。 | `/api/**` |
| `xueyifang-auth` | `8100` | 登录、注册、登出、Token 签发和失效。 | `/auth/login`、`/auth/register`、`/auth/logout`、`/auth/token/refresh` |
| `xueyifang-user` | `8200` | 用户资料、角色、发布权限、账号状态。 | `/users/**`、`/permission/**`，兼容 `/auth/currentUser`、`/auth/updateProfile`、`/auth/changePassword` |
| `xueyifang-service` | `8300` | 服务发布、浏览、服务图片关系、收藏、评价展示。 | `/service/**`、`/service-image/**`、`/favorite/**`、`/review/**` |
| `xueyifang-trade` | `8400` | 订单、支付状态、退款、纠纷、钱包流水。 | `/order/**`、`/wallet/**`、`/dispute/**` |
| `xueyifang-system` | `8500` | 专业、交易地点和系统配置读取。 | `/professional/**`、`/trade-location/**`、`/sys-config/**`、`/admin/professional/**`、`/admin/trade-location/**`、`/admin/sys-config/**` |

## 暂缓落地服务

| 候选服务 | 暂缓原因 | 后续触发条件 |
| --- | --- | --- |
| `xueyifang-message` | 聊天和通知涉及 WebSocket 多实例、离线消息和推送一致性，过早拆会增加联调成本。 | 交易链路稳定后，迁移聊天和通知。 |
| `xueyifang-file` | 文件接口需要兼容本地和 OSS，服务图片表仍归服务市场。 | 服务发布和图片上传联调前拆出或落入公共基础能力。 |

## 边界理由

| 决策 | 原因 |
| --- | --- |
| `xueyifang-content` 改为 `xueyifang-service` | 原系统不是内容社区，而是校园技能服务与交易平台。核心对象是 `service` 表和服务发布、浏览、审核流程。 |
| 新增 `xueyifang-trade` | 订单、钱包、纠纷之间事务和状态耦合很强。先放到一个交易服务，比分散到订单、钱包、纠纷三个服务更稳。 |
| 用户余额暂不放用户服务 | 原 `user.balance` 和钱包流水强耦合。迁移时应把余额从用户资料中剥离，避免用户服务承担资金事务。 |
| 管理后台不单独成服务 | `/admin/*` 是多业务域入口。数据归属应跟业务走，后台可以由网关或前端聚合多个服务接口。 |
| 先兼容旧路径 | 前端大量 API 固定在 `/api` 下。网关先保持旧路径，减少前端一次性改造。 |

## 第一批迁移顺序

1. 通用响应、异常、错误码、用户上下文。
2. 认证登录和 Token 校验。
3. 用户资料和发布权限。
4. 服务列表、服务详情、标签和基础字典读取。
5. 订单创建、支付、买家订单、卖家订单的最短链路。

这个顺序先打通用户进入系统、浏览服务、下单交易的主流程，再迁移聊天、通知、后台统计和文件存储。

## 路由约定

当前网关使用 Nacos 服务发现和 `lb://` 路由：

| 路由 ID | 目标地址 | Path |
| --- | --- | --- |
| `xueyifang-user-auth-compat` | `lb://xueyifang-user` | `/auth/currentUser`、`/auth/updateProfile`、`/auth/changePassword` |
| `xueyifang-auth` | `lb://xueyifang-auth` | `/auth/**` |
| `xueyifang-user` | `lb://xueyifang-user` | `/users/**`、`/permission/**` |
| `xueyifang-service` | `lb://xueyifang-service` | `/service/**`、`/service-image/**`、`/favorite/**`、`/review/**` |
| `xueyifang-trade` | `lb://xueyifang-trade` | `/order/**`、`/wallet/**`、`/dispute/**` |
| `xueyifang-system` | `lb://xueyifang-system` | `/professional/**`、`/trade-location/**`、`/sys-config/**`、`/admin/professional/**`、`/admin/trade-location/**`、`/admin/sys-config/**` |

旧前端仍可调用已迁移的 `/auth/currentUser` 等路径；新增前端代码优先使用 `/users/me` 系列路径。
