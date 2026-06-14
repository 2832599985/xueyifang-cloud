# 认证与用户接口契约

本文记录阶段 4 已落地的认证和用户资料接口。所有接口仍通过网关暴露；网关负责校验 Bearer Token，并向下游透传可信 `X-User-*` 用户上下文。

## 通用约定

| 项目 | 约定 |
| --- | --- |
| 响应结构 | `code`、`message`、`data` |
| Token 请求头 | `Authorization: Bearer {token}` |
| 兼容 Token 请求头 | `token: {token}` |
| 当前用户来源 | 下游服务只信任网关写入的 `X-User-Id`、`X-User-Role`、`X-User-Publish-Permission` |
| 已登出 Token | Auth 写入 Redis 黑名单，Gateway 和刷新接口都会拒绝 |

## Auth 服务

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | 注册用户，写入 `user` 表并使用 BCrypt 加密密码；会读取启用的 `sys_config.REGISTER_ENABLED`，值为 `0` 时拒绝注册。 | 公开 |
| `POST` | `/auth/login` | 校验用户名和密码，签发 JWT。JWT 的 `publishPermission` 来自 `user.publish_permission`，缺失时按角色兜底。 | 公开 |
| `POST` | `/auth/token/refresh` | 刷新有效 Token。 | 公开，但会拒绝无效、过期或黑名单 Token |
| `POST` | `/auth/logout` | 将当前 Token 加入 Redis 黑名单。 | 登录 |

## User 服务

| 方法 | 新路径 | 兼容旧路径 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/users/me` | `/auth/currentUser` | 返回当前用户资料、角色、发布权限和账户状态。 |
| `PUT` | `/users/me/profile` | `/auth/updateProfile` | 更新当前用户资料。空字段不会覆盖旧值。 |
| `PUT` | `/users/me/password` | `/auth/changePassword` | 校验旧密码后更新 BCrypt 密码。 |
| `GET` | `/permission/status` | 无 | 查询当前用户发布权限状态。 |
| `POST` | `/permission/apply` | 无 | 提交发布权限申请。已拥有权限时直接返回 `approved`。 |
| `GET` | `/admin/users/pending` | 无 | 管理员分页查询待审核发布权限申请。 |
| `PUT` | `/admin/permission/review` | `{ "userId": 1, "approved": true, "reason": "" }` | 管理员审核发布权限申请，通过后授予发布权限，拒绝后记录原因。 |
| `POST` | `/admin/user-import/upload` | 无 | 管理员批量导入用户，支持 CSV、XLS 和 XLSX。导入密码使用 BCrypt 入库。 |
| `GET` | `/admin/user-import/template` | 无 | 管理员下载用户导入模板，默认 XLSX，`format=csv` 返回 CSV。 |

网关把 `/auth/currentUser`、`/auth/updateProfile`、`/auth/changePassword`、`/users/**`、`/permission/**`、`/admin/users/**`、`/admin/permission/**` 和 `/admin/user-import/**` 路由到 `xueyifang-user`，其余 `/auth/**` 仍路由到 `xueyifang-auth`。

用户导入返回 `totalCount`、`successCount`、`skippedCount`、`failedCount` 和 `failedRows`。每行按学号/用户名查重，专业 ID 必须存在于未删除专业表；管理员导入的用户默认拥有发布权限。

## 用户资料字段

`user` 表当前同时保留新认证字段和旧单体资料字段：`username`、`student_id`、`real_name`、`nickname`、`phone`、`email`、`dormitory`、`grade`、`professional_id`、`avatar`、`bio`、`role`、`publish_permission`、`permission_review_status`、`permission_apply_reason`、`permission_review_reason`、`permission_reviewed_by`、`permission_reviewed_at`、`wallet_balance`、`frozen_amount`、`status` 和 `account_status`。

发布权限审核完成后，用户服务会通过消息服务内部 `POST /internal/notifications` 创建 `notificationType=1` 的通知；通知失败只记录日志，不回滚审核结果。钱包余额仍保留在表中以兼容旧结构，交易迁移时再收口资金边界。
