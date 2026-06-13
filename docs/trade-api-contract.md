# 交易服务接口契约

## 当前范围

本契约覆盖阶段 5 订单最短链路：创建订单、支付订单、取消订单、卖家发货、买家确认完成、买家订单列表、卖家订单列表和订单详情。

退款、纠纷、钱包充值提现、钱包流水查询和订单定时任务暂缓；本批次只落地评价创建所需的订单完成状态、订单归属和钱包冻结/结算闭环。

## 路由与鉴权

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| `POST` | `/order/create` | 创建服务订单，返回订单 ID。 | 登录用户 |
| `POST` | `/order/{orderId}/pay` | 使用钱包余额支付订单，买家余额扣减并转入冻结金额。 | 订单买家 |
| `POST` | `/order/{orderId}/cancel` | 取消待支付订单。 | 订单买家 |
| `POST` | `/order/{orderId}/ship` | 卖家发货，订单进入待收货。 | 订单卖家 |
| `POST` | `/order/{orderId}/confirm` | 买家确认完成，冻结金额结算给卖家。 | 订单买家 |
| `POST` | `/order/{orderId}/sellerConfirm` | 兼容旧路径，等价于卖家发货。 | 订单卖家 |
| `GET` | `/order/myOrders` | 当前登录用户买家订单列表。 | 登录用户 |
| `GET` | `/order/mySellingOrders` | 当前登录用户卖家订单列表。 | 登录用户 |
| `GET` | `/order/{orderId}` | 订单详情。 | 订单买家、卖家或管理员 |

网关已将 `/order/**` 转发到 `xueyifang-trade`。业务服务只信任网关写入的 `X-User-*` 用户上下文。

## 查询参数

`GET /order/myOrders` 和 `GET /order/mySellingOrders` 支持：

| 参数 | 说明 |
| --- | --- |
| `orderStatus` | 可选订单状态。 |
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

## 状态约定

| 字段 | 值 | 说明 |
| --- | --- | --- |
| `orderStatus` | `1` | 待支付 |
| `orderStatus` | `2` | 待发货，已支付并冻结资金 |
| `orderStatus` | `3` | 待收货，卖家已发货 |
| `orderStatus` | `4` | 已完成，买家确认后资金结算给卖家 |
| `orderStatus` | `5` | 已取消 |
| `orderStatus` | `6` | 交易失败，预留给退款/纠纷 |
| `paymentStatus` | `1` | 未支付 |
| `paymentStatus` | `2` | 已支付 |
| `paymentStatus` | `3` | 已退款，后续退款链路接入 |
| `refundStatus` | `0` | 无退款 |
| `refundStatus` | `1` | 退款申请中，后续接入 |

## 数据表

交易服务当前接入 `service_order`、`service_order_log`、`wallet_transaction`，并读写 `user.wallet_balance`、`user.frozen_amount` 和 `service.order_count`。本地初始化脚本见 `deploy/docker/mysql/init/003-trade.sql`。

当前支付采用担保冻结模型：支付时扣减买家可用余额并增加买家冻结金额；买家确认完成时扣减买家冻结金额并增加卖家可用余额，同时记录钱包流水。
