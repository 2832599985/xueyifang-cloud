# 消息服务接口契约

## 范围

`xueyifang-message` 承接原单体 `/chat/*`、`/notification/*` 和 `/api/ws` 能力。当前支持聊天消息落库、会话列表、聊天记录、通知列表、未读数、标记已读、内部通知创建和单实例 WebSocket 在线推送。

消息服务持有 `user_chat` 和 `notification` 表。用户资料只读取 `user` 表的轻量展示字段，不接管用户状态管理。

内部通知创建接口用于服务间调用，不通过网关公开路由；外部用户仍只能通过 `/notification/**` 查询和标记自己的通知。

## 网关路由

| 外部路径 | 网关处理 | 目标服务 |
| --- | --- | --- |
| `/chat/**` | 直接转发，需要登录 | `lb://xueyifang-message` |
| `/notification/**` | 直接转发，需要登录 | `lb://xueyifang-message` |
| `/api/ws`、`/api/ws/**` | `StripPrefix=1` 后转发为 `/ws`，握手由消息服务校验 `token` 查询参数 | `lb:ws://xueyifang-message` |
| `/ws`、`/ws/**` | 直接转发，握手由消息服务校验 Gateway 用户头或 `token` 查询参数 | `lb:ws://xueyifang-message` |

## 聊天接口

### 发送消息

`POST /chat/send`

请求：

```json
{
  "receiverId": 2,
  "content": "你好，这个服务还可以预约吗？",
  "messageType": 1,
  "relatedServiceId": 10,
  "relatedOrderId": null
}
```

说明：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `receiverId` | 是 | 接收者用户 ID。 |
| `content` | 是 | 消息内容。 |
| `messageType` | 否 | `1` 文本、`2` 图片、`3` 文件，默认 `1`。 |
| `relatedServiceId` | 否 | 关联服务 ID。 |
| `relatedOrderId` | 否 | 关联订单 ID。 |

约束：

- 不能给自己发送消息。
- 接收者必须存在，且账号未禁用。
- 消息保存成功后尝试 WebSocket 推送，推送失败不影响接口结果。

### 获取聊天记录

`GET /chat/messages/{userId}?pageNum=1&pageSize=50`

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [
      {
        "messageId": 1,
        "sender": {
          "userId": 1,
          "realName": "卖家小王",
          "avatar": "/api/file/view/user_avatar/1/a.png"
        },
        "receiver": {
          "userId": 2,
          "realName": "买家小李",
          "avatar": null
        },
        "content": "你好",
        "messageType": 1,
        "isRead": 0,
        "createTime": "2026-06-14T12:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "pageNum": 1,
    "pageSize": 50,
    "pages": 1
  }
}
```

读取会话记录后，系统会把对方发给当前用户的未读消息标记为已读。

### 获取会话列表

`GET /chat/conversations`

返回每个对话用户、最后一条消息、未读数和最后消息时间，按最后消息时间倒序排列。

## 通知接口

### 获取我的通知

`GET /notification/my-notifications?pageNum=1&pageSize=10&notificationType=3`

`notificationType` 可为空。当前类型如下：

| 类型 | 含义 |
| --- | --- |
| `1` | 发布权限审核 |
| `2` | 服务下架 |
| `3` | 订单通知 |
| `4` | 纠纷通知 |
| `5` | 服务审核 |

### 获取未读数

`GET /notification/unreadCount`

返回当前用户未读通知数量。

### 标记单条已读

`POST /notification/{id}/read`

只能标记自己的通知。

### 标记全部已读

`POST /notification/readAll`

把当前用户全部未读通知标记为已读。

## 内部通知生产接口

`POST /internal/notifications`

请求：

```json
{
  "recipientId": 2,
  "notificationType": 3,
  "title": "订单已支付",
  "content": "订单 ORDER20260614120000123 已支付，请及时发货。",
  "relatedId": 100
}
```

说明：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `recipientId` | 是 | 通知接收者用户 ID，用户必须存在。 |
| `notificationType` | 是 | 通知类型，沿用下方类型表。 |
| `title` | 是 | 通知标题。 |
| `content` | 是 | 通知内容。 |
| `relatedId` | 否 | 关联业务 ID，订单通知传订单 ID，纠纷通知传纠纷 ID。 |

创建成功后会写入 `notification` 表，并尝试通过 WebSocket 推送 `NEW_NOTIFICATION`。推送失败不影响通知创建。

## WebSocket

连接地址保持旧前端兼容：

```text
ws://{gateway-host}:8080/api/ws?token={jwt}
```

服务端消息类型：

| type | data |
| --- | --- |
| `CONNECTED` | 当前用户 ID。 |
| `PONG` | `null`，响应客户端 `PING` 心跳。 |
| `NEW_CHAT` | `ChatMessageResponse`。 |
| `NEW_NOTIFICATION` | `NotificationResponse`。 |

当前实现使用消息服务进程内会话表，适合单实例或粘性会话。多实例部署时，需要增加 Redis pub/sub、消息队列广播或网关粘性会话方案。

## 后续回接

交易订单和纠纷流程已通过 HTTP 内部接口调用消息服务创建通知。当前覆盖新订单、支付、取消、发货、完成、退款申请、退款完成、纠纷创建和纠纷处理结果。

权限审核和服务审核流程已在后台审核接口落地后复用同一内部通知接口，分别创建 `notificationType=1` 和 `notificationType=5` 的通知。多实例或高并发场景可再替换为事件/MQ 机制。
