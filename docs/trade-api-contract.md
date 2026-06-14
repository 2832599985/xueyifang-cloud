# 交易服务接口契约

## 当前范围

本契约覆盖阶段 5 交易链路：创建订单、支付订单、取消订单、卖家发货、买家确认完成、订单查询、退款申请、卖家处理退款、纠纷发起、纠纷查询、管理员纠纷处理、钱包余额、钱包流水、充值和提现。

订单定时任务暂缓；退款超时自动处理后续接入任务调度方案。

## 路由与鉴权

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| `POST` | `/order/create` | 创建服务订单，返回订单 ID。 | 登录用户 |
| `POST` | `/order/{orderId}/pay` | 使用钱包余额支付订单，买家余额扣减并转入冻结金额。 | 订单买家 |
| `POST` | `/order/{orderId}/cancel` | 取消待支付订单。 | 订单买家 |
| `POST` | `/order/{orderId}/ship` | 卖家发货，订单进入待收货。 | 订单卖家 |
| `POST` | `/order/{orderId}/confirm` | 买家确认完成，冻结金额结算给卖家。 | 订单买家 |
| `POST` | `/order/{orderId}/refund` | 买家申请退款；待发货订单直接退款，待收货订单进入卖家处理。 | 订单买家 |
| `POST` | `/order/{orderId}/handleRefund` | 卖家同意或拒绝待处理退款申请。 | 订单卖家 |
| `POST` | `/order/{orderId}/sellerConfirm` | 兼容旧路径，等价于卖家发货。 | 订单卖家 |
| `GET` | `/order/myOrders` | 当前登录用户买家订单列表。 | 登录用户 |
| `GET` | `/order/mySellingOrders` | 当前登录用户卖家订单列表。 | 登录用户 |
| `GET` | `/order/{orderId}` | 订单详情。 | 订单买家、卖家或管理员 |
| `GET` | `/wallet/balance` | 当前登录用户钱包余额、冻结金额和总资产。 | 登录用户 |
| `GET` | `/wallet/transactions` | 当前登录用户钱包流水分页查询。 | 登录用户 |
| `POST` | `/wallet/recharge` | 钱包充值，当前为本地余额模拟入账。 | 登录用户 |
| `POST` | `/wallet/withdraw` | 钱包提现，当前为本地余额模拟出账。 | 登录用户 |
| `POST` | `/dispute`、`/dispute/create` | 买家在卖家拒绝退款后发起订单纠纷，返回纠纷 ID。 | 订单买家 |
| `GET` | `/dispute/my`、`/dispute/myDisputes`、`/dispute/list` | 当前登录用户相关纠纷分页列表。 | 订单买家或卖家 |
| `GET` | `/dispute/{disputeId}` | 纠纷详情。 | 纠纷双方或管理员 |
| `GET` | `/dispute/admin/list` | 管理员纠纷分页列表。 | 管理员 |
| `POST` | `/dispute/{disputeId}/handle`、`/dispute/admin/{disputeId}/handle` | 管理员处理纠纷；支持退款或驳回关闭。 | 管理员 |

网关已将 `/order/**`、`/wallet/**` 和 `/dispute/**` 转发到 `xueyifang-trade`。业务服务只信任网关写入的 `X-User-*` 用户上下文。

## 查询参数

`GET /order/myOrders` 和 `GET /order/mySellingOrders` 支持：

| 参数 | 说明 |
| --- | --- |
| `orderStatus` | 可选订单状态。 |
| `pageNum` / `current` | 页码，兼容两种命名，默认 `1`。 |
| `pageSize` | 每页数量，默认 `10`，最大 `100`。 |

`GET /wallet/transactions` 支持：

| 参数 | 说明 |
| --- | --- |
| `transactionType` | 可选流水类型：`1` 充值、`2` 提现、`3` 支付、`4` 退款、`5` 收入、`6` 冻结、`7` 解冻。 |
| `startTime` / `endTime` | 可选 ISO 时间范围。 |
| `pageNum` / `current` | 页码，兼容两种命名，默认 `1`。 |
| `pageSize` | 每页数量，默认 `10`，最大 `100`。 |

`GET /dispute/my`、`GET /dispute/myDisputes`、`GET /dispute/list` 和 `GET /dispute/admin/list` 支持：

| 参数 | 说明 |
| --- | --- |
| `status` | 可选纠纷状态。 |
| `pageNum` / `current` | 页码，兼容两种命名，默认 `1`。 |
| `pageSize` | 每页数量，默认 `10`，最大 `100`。 |

## 写入请求

`POST /order/create`：

| 字段 | 说明 |
| --- | --- |
| `serviceId` | 服务 ID，必填。 |
| `quantity` | 购买数量，必填且大于 `0`。 |
| `tradeType` | 交易类型：`1` 线下，`2` 线上。 |
| `tradeLocationId` | 线下交易必填；当前只保存 ID，地点字典待系统服务迁移。 |
| `remark` | 订单备注，可选。 |

`POST /order/{orderId}/pay`：

| 字段 | 说明 |
| --- | --- |
| `paymentMethod` | 支付方式；当前仅支持 `1` 钱包余额。未传时默认 `1`。 |

`POST /order/{orderId}/refund`：

| 字段 | 说明 |
| --- | --- |
| `reason` | 退款原因，必填，最长 `500` 字。 |
| `remark` | 备注，可选，当前仅兼容接收。 |

`POST /order/{orderId}/handleRefund`：

| 字段 | 说明 |
| --- | --- |
| `approve` | 是否同意退款，必填。 |
| `rejectReason` | 拒绝原因；`approve=false` 时必填。 |

`POST /wallet/recharge`：

| 字段 | 说明 |
| --- | --- |
| `amount` | 充值金额，必填且大于 `0`。 |
| `paymentMethod` | 充值方式，可选，当前仅兼容接收。 |

`POST /wallet/withdraw`：

| 字段 | 说明 |
| --- | --- |
| `amount` | 提现金额，必填且大于 `0`，不得超过可用余额。 |
| `accountNumber` | 提现账号，可选，当前仅兼容接收。 |
| `accountName` | 提现账户名，可选，当前仅兼容接收。 |

`POST /dispute` 或 `POST /dispute/create`：

| 字段 | 说明 |
| --- | --- |
| `orderId` | 订单 ID，必填。当前仅允许订单买家在 `refundStatus=3` 卖家拒绝退款后发起。 |
| `reason` | 纠纷原因，必填，最长 `500` 字。 |
| `evidence` | 举证材料，可选，最长 `1000` 字；当前按字符串保存，可放图片 URL 列表的 JSON 或逗号分隔值。 |

`POST /dispute/{disputeId}/handle` 或 `POST /dispute/admin/{disputeId}/handle`：

| 字段 | 说明 |
| --- | --- |
| `approveRefund` | 是否支持买家并退款，必填。`true` 时复用管理员强制退款链路，资金退回买家。 |
| `handleRemark` | 处理备注；`approveRefund=false` 时必填，最长 `500` 字。 |

## 状态约定

| 字段 | 值 | 说明 |
| --- | --- | --- |
| `orderStatus` | `1` | 待支付 |
| `orderStatus` | `2` | 待发货，已支付并冻结资金 |
| `orderStatus` | `3` | 待收货，卖家已发货 |
| `orderStatus` | `4` | 已完成，买家确认后资金结算给卖家 |
| `orderStatus` | `5` | 已取消 |
| `orderStatus` | `6` | 交易失败，退款完成后进入该状态 |
| `paymentStatus` | `1` | 未支付 |
| `paymentStatus` | `2` | 已支付 |
| `paymentStatus` | `3` | 已退款 |
| `refundStatus` | `0` | 无退款 |
| `refundStatus` | `1` | 退款申请中，等待卖家处理 |
| `refundStatus` | `2` | 卖家同意退款，当前同意后立即执行退款，状态不长期停留 |
| `refundStatus` | `3` | 卖家拒绝退款，买家可再次申请或后续发起纠纷 |
| `refundStatus` | `4` | 已执行退款，资金已退回买家可用余额 |
| `dispute.status` | `1` | 待管理员处理 |
| `dispute.status` | `2` | 管理员支持买家，已执行退款并关闭 |
| `dispute.status` | `3` | 管理员驳回，纠纷关闭 |

## 钱包流水

| 类型 | 值 | 说明 |
| --- | --- | --- |
| `transactionType` | `1` | 充值，可用余额增加 |
| `transactionType` | `2` | 提现，可用余额减少 |
| `transactionType` | `3` | 支付，可用余额减少 |
| `transactionType` | `4` | 退款，可用余额增加 |
| `transactionType` | `5` | 收入，卖家可用余额增加 |
| `transactionType` | `6` | 冻结，买家冻结金额增加 |
| `transactionType` | `7` | 解冻，买家冻结金额减少 |

## 数据表

交易服务当前接入 `service_order`、`service_order_log`、`service_dispute`、`wallet_transaction`，并读写 `user.wallet_balance`、`user.frozen_amount` 和 `service.order_count`。`service_order` 已为退款状态和申请时间预留索引；`service_dispute` 通过 `order_id` 保证一单一纠纷，并为纠纷双方、状态和处理人预留索引。本地初始化脚本见 `deploy/docker/mysql/init/003-trade.sql`。

当前支付采用担保冻结模型：支付时扣减买家可用余额并增加买家冻结金额；买家确认完成时扣减买家冻结金额并增加卖家可用余额；退款时扣减买家冻结金额并退回买家可用余额。资金流转均记录钱包流水。

买家发起纠纷后，订单存在待处理纠纷时会阻止买家确认收货、再次申请退款和卖家继续处理退款，直到管理员关闭纠纷。管理员支持买家时会复用订单管理员退款能力，订单进入交易失败且支付状态变为已退款；管理员驳回后，纠纷关闭，订单仍保持原交易状态。
