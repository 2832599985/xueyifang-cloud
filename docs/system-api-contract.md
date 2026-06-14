# 系统字典与配置接口契约

本契约覆盖阶段 5 新增的 `xueyifang-system`：专业字典、交易地点字典、系统配置读取和后台统计。公开接口保持旧前端路径，管理员接口保留旧后台路径。

## 接口总览

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| `GET` | `/professional/list` | 查询未删除专业列表。 | 公开 |
| `GET` | `/professional/{id}` | 查询专业详情。 | 公开 |
| `POST` | `/professional/add` | 添加专业，兼容旧路径。 | 管理员 |
| `PUT` | `/professional/update` | 更新专业，兼容旧路径。 | 管理员 |
| `DELETE` | `/professional/{id}` | 逻辑删除专业，兼容旧路径。 | 管理员 |
| `GET` | `/trade-location/list` | 查询可用交易地点。 | 公开 |
| `GET` | `/trade-location/list/all` | 查询全部未删除交易地点。 | 公开 |
| `GET` | `/trade-location/page` | 分页查询交易地点，可按可用状态筛选。 | 公开 |
| `GET` | `/trade-location/{id}` | 查询交易地点详情。 | 公开 |
| `POST` | `/trade-location/add` | 添加交易地点，兼容旧路径。 | 管理员 |
| `PUT` | `/trade-location/update` | 更新交易地点，兼容旧路径。 | 管理员 |
| `DELETE` | `/trade-location/{id}` | 逻辑删除交易地点，兼容旧路径。 | 管理员 |
| `DELETE` | `/trade-location/batch` | 批量逻辑删除交易地点，兼容旧路径。 | 管理员 |
| `GET` | `/sys-config/register-status` | 查询自助注册开关。 | 公开 |
| `GET` | `/admin/professional/list` | 后台分页查询专业。 | 管理员 |
| `POST` | `/admin/professional/add` | 后台添加专业。 | 管理员 |
| `PUT` | `/admin/professional/update` | 后台更新专业。 | 管理员 |
| `DELETE` | `/admin/professional/{id}` | 后台逻辑删除专业。 | 管理员 |
| `GET` | `/admin/trade-location/list` | 后台分页查询交易地点。 | 管理员 |
| `POST` | `/admin/trade-location/add` | 后台添加交易地点。 | 管理员 |
| `PUT` | `/admin/trade-location/update` | 后台更新交易地点。 | 管理员 |
| `DELETE` | `/admin/trade-location/{id}` | 后台逻辑删除交易地点。 | 管理员 |
| `GET` | `/admin/sys-config/list` | 后台分页查询系统配置。 | 管理员 |
| `GET` | `/admin/sys-config/{id}` | 后台查询配置详情。 | 管理员 |
| `GET` | `/admin/sys-config/key-values` | 后台按 key 批量读取启用配置值。 | 管理员 |
| `PUT` | `/admin/sys-config/update` | 后台更新配置值、描述或启用状态。 | 管理员 |
| `GET` | `/admin/statistics` | 后台首页系统统计，聚合用户、服务、订单和待处理纠纷。 | 管理员 |
| `GET` | `/admin/statistics/trend` | 后台近 7 天订单、服务和交易金额趋势。 | 管理员 |

管理员由 Gateway 透传的 `X-User-Role=2` 判定。公开接口已在网关白名单中放行。

## 请求与响应

专业写入请求：

```json
{
  "id": 1,
  "professionalName": "软件工程",
  "description": "软件开发与设计相关专业"
}
```

交易地点写入请求：

```json
{
  "id": 1,
  "locationName": "图书馆一楼大厅",
  "locationDescription": "人多，安全可靠",
  "locationAddress": "图书馆入口",
  "isAvailable": 1
}
```

系统配置更新请求：

```json
{
  "id": 1,
  "configValue": "1",
  "description": "self-service registration switch",
  "isEnabled": 1
}
```

注册开关响应：

```json
{
  "registerEnabled": true
}
```

分页响应统一返回 `records`、`total`、`current`、`pageNum`、`pageSize` 和 `pages`。

后台统计响应保留旧前端字段：`totalUsers`、`activeUsers`、`totalServices`、`totalOrders`、`completedOrders`、`pendingDisputes`、`totalTransactionAmount`、`todayNewUsers`、`todayNewServices` 和 `todayNewOrders`。趋势响应返回 `dates`、`orderCounts`、`serviceCounts` 和 `transactionAmounts`。

## 数据表

本地初始化脚本见 `deploy/docker/mysql/init/004-system.sql`，会创建并初始化：

| 表 | 说明 |
| --- | --- |
| `professional` | 专业字典，使用 `is_delete` 逻辑删除。 |
| `trade_location` | 交易地点字典，使用 `is_available` 控制前台可用性，使用 `is_delete` 逻辑删除。 |
| `sys_config` | 系统配置，当前包含 `REGISTER_ENABLED`、审核模式和订单超时默认配置。 |

`REGISTER_ENABLED` 支持 `1` 和 `true` 表示开启，其他值按关闭处理。认证服务注册接口也会读取该配置；缺少配置时默认允许注册。
