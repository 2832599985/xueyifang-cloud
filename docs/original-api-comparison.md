# 原项目前后端接口对照清单

## 对照范围

来源：

| 来源 | 路径 | 说明 |
| --- | --- | --- |
| 原后端 | `D:\_Code\Java\_reference\xueyifang-original\xueyifang-backend` | 以 Controller 映射为准。 |
| 原前端 | `D:\_Code\Java\_reference\xueyifang-original\xueyifang-frontend` | 以 `src/api/*.ts` 的实际请求为准。 |
| 新后端 | 当前 `xueyifang-cloud` | 以各微服务 Controller 和 Gateway 路由为准。 |

口径：

- 旧前端 `src/api/*.ts` 中共有 `80` 个请求调用点。
- 当前路径层面已覆盖 `80/80` 个旧前端调用点，统计和用户导入硬缺口已补齐。
- 已覆盖调用点中，纠纷域有字段和语义兼容注意项：旧 `disputeType` 当前不单独持久化；旧后台“处理但不退款”动作当前按关闭/驳回语义处理。
- 原后端存在但旧前端未直接调用的 `GET /admin/user-import/template` 已随后台导入能力一并迁移。

## 覆盖总览

| 分组 | 旧前端调用点 | 已覆盖 | 未实现 | 部分兼容/注意 |
| --- | ---: | ---: | ---: | --- |
| 认证、用户、发布权限 | 10 | 10 | 0 | 无 |
| 服务市场、收藏、评价 | 17 | 17 | 0 | 无 |
| 订单、钱包 | 14 | 14 | 0 | 无 |
| 纠纷 | 7 | 7 | 0 | `disputeType` 未持久化；后台“处理不退款”语义需产品确认。 |
| 聊天、通知、WebSocket | 7 | 7 | 0 | WebSocket 多实例需要联调压测。 |
| 文件 | 3 | 3 | 0 | 本地存储已覆盖，OSS 未迁移。 |
| 字典、系统配置 | 19 | 19 | 0 | 后台用户导入已覆盖；系统配置主体已覆盖。 |
| 管理后台统计 | 3 | 3 | 0 | 管理员概览、趋势、用户销售统计已覆盖。 |
| 合计 | 80 | 80 | 0 | 旧前端调用点路径层面已覆盖，后续重点转入联调和上线工程化。 |

## 旧前端调用对照

### 认证、用户和权限

| 旧前端 API | 原后端 | 新后端 | 状态 |
| --- | --- | --- | --- |
| `POST /auth/login` | `UserController` | `xueyifang-auth` | 已兼容 |
| `POST /auth/register` | `UserController` | `xueyifang-auth` | 已兼容，读取注册开关 |
| `POST /auth/logout` | `UserController` | `xueyifang-auth` | 已兼容，写 Redis Token 黑名单 |
| `GET /auth/currentUser` | `UserController` | `xueyifang-user` 兼容路径 | 已兼容 |
| `PUT /auth/updateProfile` | `UserController` | `xueyifang-user` 兼容路径 | 已兼容 |
| `PUT /auth/changePassword` | `UserController` | `xueyifang-user` 兼容路径 | 已兼容 |
| `GET /permission/status` | `PermissionController` | `xueyifang-user` | 已兼容 |
| `POST /permission/apply` | `PermissionController` | `xueyifang-user` | 已兼容 |
| `GET /admin/users/pending` | `AdminController` | `xueyifang-user` | 已兼容 |
| `PUT /admin/permission/review` | `AdminController` | `xueyifang-user` | 已兼容，回接消息通知 |

### 服务市场、收藏和评价

| 旧前端 API | 原后端 | 新后端 | 状态 |
| --- | --- | --- | --- |
| `GET /service/list` | `ServiceController` | `xueyifang-service` | 已兼容 |
| `GET /service/{id}` | `ServiceController` | `xueyifang-service` | 已兼容 |
| `GET /service/myServices` | `ServiceController` | `xueyifang-service` | 已兼容 |
| `POST /service/publish` | `ServiceController` | `xueyifang-service` | 已兼容，接入 `REVIEW_MODE` |
| `PUT /service/{id}` | `ServiceController` | `xueyifang-service` | 已兼容 |
| `PUT /service/{id}/online` | `ServiceController` | `xueyifang-service` | 已兼容，按审核配置决定状态 |
| `PUT /service/{id}/offline` | `ServiceController` | `xueyifang-service` | 已兼容 |
| `DELETE /service/{id}` | `ServiceController` | `xueyifang-service` | 已兼容 |
| `GET /service/tags` | `ServiceTagController` | `xueyifang-service` | 已兼容 |
| `POST /favorite/collect` | `ServiceFavoriteController` | `xueyifang-service` | 已兼容 |
| `DELETE /favorite/collect/{serviceId}` | `ServiceFavoriteController` | `xueyifang-service` | 已兼容 |
| `GET /favorite/myCollections` | `ServiceFavoriteController` | `xueyifang-service` | 已兼容 |
| `POST /review/create` | `ServiceReviewController` | `xueyifang-service` | 已兼容，校验订单完成状态 |
| `GET /review/service/{serviceId}` | `ServiceReviewController` | `xueyifang-service` | 已兼容 |
| `GET /review/order/{orderId}/status` | `ServiceReviewController` | `xueyifang-service` | 已兼容 |
| `GET/PUT /admin/services/*` | `AdminServiceController` | `xueyifang-service` | 已兼容，回接消息通知 |

### 订单、钱包和纠纷

| 旧前端 API | 原后端 | 新后端 | 状态 |
| --- | --- | --- | --- |
| `POST /order/create` | `ServiceOrderController` | `xueyifang-trade` | 已兼容 |
| `POST /order/{orderId}/pay` | `ServiceOrderController` | `xueyifang-trade` | 已兼容 |
| `GET /order/{orderId}` | `ServiceOrderController` | `xueyifang-trade` | 已兼容 |
| `GET /order/myOrders` | `ServiceOrderController` | `xueyifang-trade` | 已兼容 |
| `GET /order/mySellingOrders` | `ServiceOrderController` | `xueyifang-trade` | 已兼容 |
| `POST /order/{orderId}/cancel` | `ServiceOrderController` | `xueyifang-trade` | 已兼容 |
| `POST /order/{orderId}/confirm` | `ServiceOrderController` | `xueyifang-trade` | 已兼容 |
| `POST /order/{orderId}/ship` | `ServiceOrderController` | `xueyifang-trade` | 已兼容 |
| `POST /order/{orderId}/handleRefund` | `ServiceOrderController` | `xueyifang-trade` | 已兼容 |
| `POST /order/{orderId}/refund` | `ServiceOrderController` | `xueyifang-trade` | 已兼容 |
| `GET /wallet/balance` | `WalletController` | `xueyifang-trade` | 已兼容 |
| `GET /wallet/transactions` | `WalletController` | `xueyifang-trade` | 已兼容 |
| `POST /wallet/recharge` | `WalletController` | `xueyifang-trade` | 已兼容 |
| `POST /wallet/withdraw` | `WalletController` | `xueyifang-trade` | 已兼容 |
| `POST /dispute/create` | `ServiceDisputeController` | `xueyifang-trade` | 已兼容旧 `description` 字段；`disputeType` 暂不持久化 |
| `GET /dispute/my` | `ServiceDisputeController` | `xueyifang-trade` | 已兼容旧响应字段别名 |
| `GET /dispute/{disputeId}` | `ServiceDisputeController` | `xueyifang-trade` | 已兼容旧响应字段别名 |
| `GET /admin/dispute/list` | `AdminDisputeController` | `xueyifang-trade` | 本轮已补齐旧路径和 `disputeStatus` 参数 |
| `GET /admin/dispute/{disputeId}` | `AdminDisputeController` | `xueyifang-trade` | 本轮已补齐 |
| `GET /admin/dispute/by-order/{orderId}` | `AdminDisputeController` | `xueyifang-trade` | 本轮已补齐，无纠纷返回 `null` |
| `POST /admin/dispute/{disputeId}/handle` | `AdminDisputeController` | `xueyifang-trade` | 本轮已补齐旧字段；“处理不退款”语义需确认 |

### 字典、系统配置和文件

| 旧前端 API | 原后端 | 新后端 | 状态 |
| --- | --- | --- | --- |
| `GET /professional/list` | `ProfessionalController` | `xueyifang-system` | 已兼容 |
| `GET /professional/{id}` | `ProfessionalController` | `xueyifang-system` | 已兼容 |
| `GET /admin/professional/list` | `AdminProfessionalController` | `xueyifang-system` | 已兼容 |
| `POST /admin/professional/add` | `AdminProfessionalController` | `xueyifang-system` | 已兼容 |
| `PUT /admin/professional/update` | `AdminProfessionalController` | `xueyifang-system` | 已兼容 |
| `DELETE /admin/professional/{id}` | `AdminProfessionalController` | `xueyifang-system` | 已兼容 |
| `GET /trade-location/list` | `TradeLocationController` | `xueyifang-system` | 已兼容 |
| `GET /trade-location/list/all` | `TradeLocationController` | `xueyifang-system` | 已兼容 |
| `GET /trade-location/{id}` | `TradeLocationController` | `xueyifang-system` | 已兼容 |
| `GET /admin/trade-location/list` | `AdminTradeLocationController` | `xueyifang-system` | 已兼容 |
| `POST /admin/trade-location/add` | `AdminTradeLocationController` | `xueyifang-system` | 已兼容 |
| `PUT /admin/trade-location/update` | `AdminTradeLocationController` | `xueyifang-system` | 已兼容 |
| `DELETE /admin/trade-location/{id}` | `AdminTradeLocationController` | `xueyifang-system` | 已兼容 |
| `GET /sys-config/register-status` | `SysConfigController` | `xueyifang-system` | 已兼容 |
| `GET /admin/sys-config/list` | `AdminSysConfigController` | `xueyifang-system` | 已兼容 |
| `GET /admin/sys-config/{id}` | `AdminSysConfigController` | `xueyifang-system` | 已兼容 |
| `PUT /admin/sys-config/update` | `AdminSysConfigController` | `xueyifang-system` | 已兼容 |
| `GET /admin/sys-config/key-values` | `AdminSysConfigController` | `xueyifang-system` | 已兼容 |
| `POST /admin/user-import/upload` | `AdminUserImportController` | `xueyifang-user` | 已兼容 |
| `POST /file/upload` | `FileController` | `xueyifang-file` | 已兼容 |
| `POST /file/upload/batch` | `FileController` | `xueyifang-file` | 已兼容 |
| `DELETE /file/delete` | `FileController` | `xueyifang-file` | 已兼容 |

### 消息和通知

| 旧前端 API | 原后端 | 新后端 | 状态 |
| --- | --- | --- | --- |
| `GET /chat/conversations` | `ChatController` | `xueyifang-message` | 已兼容 |
| `GET /chat/messages/{userId}` | `ChatController` | `xueyifang-message` | 已兼容 |
| `POST /chat/send` | `ChatController` | `xueyifang-message` | 已兼容 |
| `GET /notification/my-notifications` | `NotificationController` | `xueyifang-message` | 已兼容 |
| `GET /notification/unreadCount` | `NotificationController` | `xueyifang-message` | 已兼容 |
| `POST /notification/{id}/read` | `NotificationController` | `xueyifang-message` | 已兼容 |
| `POST /notification/readAll` | `NotificationController` | `xueyifang-message` | 已兼容 |
| `WS /api/ws` | `WebSocketConfig` | `xueyifang-message` + Gateway 兼容路由 | 已兼容，支持可选 Redis 广播 |

### 统计

| 旧前端 API | 原后端 | 新后端 | 状态 |
| --- | --- | --- | --- |
| `GET /admin/statistics` | `AdminController` | `xueyifang-system` | 已兼容 |
| `GET /admin/statistics/trend` | `AdminController` | `xueyifang-system` | 已兼容 |
| `GET /statistics/sales` | `StatisticsController` | `xueyifang-trade` | 已兼容 |

## 下一轮建议

优先级按“旧前端可见度”和“实现风险”排序：

| 优先级 | 后续项 | 建议归属 | 说明 |
| --- | --- | --- | --- |
| P0 | 本地网关联通冒烟 | 所有服务 | 旧前端调用点已路径覆盖，下一步需要启动 Nacos/MySQL/Redis 和各服务，经 Gateway 跑关键链路。 |
| P0 | 旧前端统计和用户导入联调 | `xueyifang-system`、`xueyifang-trade`、`xueyifang-user` | 重点确认后台首页图表、销售统计页面、CSV/Excel 导入和模板下载的浏览器行为。 |
| P1 | 纠纷 `disputeType` 持久化和“处理不退款”语义 | `xueyifang-trade` | 当前接口可用，但旧前端类型展示会统一为“其他”；产品上若需要“解决但不退款”，需扩展状态机。 |
| P2 | 本地网关联通冒烟 | 所有服务 | 启动 Nacos/MySQL/Redis 后，跑旧前端关键路径：登录、服务浏览、下单、退款/纠纷、通知、文件上传。 |

结论：迁移代码层面已达到“基本完成”的路径覆盖口径。从旧前端调用点看，`80/80` 个请求调用点已覆盖，旧后端导入模板配套接口也已补齐；完整上线仍需要联通验证、生产配置、安全和部署收尾。
