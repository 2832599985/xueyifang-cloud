# 服务市场接口契约

## 当前范围

本契约覆盖第一批服务市场迁移的服务浏览和发布者管理最短链路：服务列表、服务详情、服务标签读取、服务发布、我的服务、编辑、上下架和逻辑删除。收藏和评价创建仍待后续迁移。

## 路由与鉴权

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| `GET` | `/service/list` | 服务列表，默认只返回已上架服务。 | 公开；携带管理员 Token 时可按 `status` 查询非公开服务。 |
| `GET` | `/service/myServices` | 当前登录用户发布的服务列表，可按状态筛选。 | 登录用户 |
| `GET` | `/service/{serviceId}` | 服务详情，返回服务主体和图片列表。 | 公开已上架服务；发布者或管理员可查看非公开服务。 |
| `GET` | `/service/tags` | 服务标签列表。 | 公开 |
| `POST` | `/service/publish` | 发布服务，写入服务主体和图片列表。 | 登录用户且具备发布权限；管理员也可发布。 |
| `PUT` | `/service/{serviceId}` | 编辑服务基础信息，可替换图片列表。 | 发布者或管理员；已上架服务需先下架。 |
| `PUT` | `/service/{serviceId}/offline` | 下架服务。 | 发布者或管理员；仅已上架服务可下架。 |
| `PUT` | `/service/{serviceId}/online` | 上架服务。 | 发布者或管理员；仅已下架或已驳回服务可上架。 |
| `DELETE` | `/service/{serviceId}` | 逻辑删除服务，并逻辑删除图片。 | 发布者或管理员；已上架服务需先下架。 |

网关已将 `/service/**` 转发到 `xueyifang-service`，并对公开 GET 请求尝试解析可选 Token。业务服务只信任网关写入的 `X-User-*` 用户上下文。

## 查询参数

`GET /service/list` 支持：

| 参数 | 说明 |
| --- | --- |
| `keyword` | 按标题或描述模糊查询。 |
| `tagId` | 标签 ID。 |
| `categoryId` | 分类 ID。 |
| `professionalId` | 专业 ID。 |
| `publisherId` / `sellerId` | 发布者 ID；`sellerId` 用于兼容旧前端命名。 |
| `status` | 管理员可指定服务状态；匿名和普通用户固定按已上架查询。 |
| `pageNum` / `current` | 页码，兼容两种命名，默认 `1`。 |
| `pageSize` | 每页数量，默认 `10`，最大 `100`。 |

`GET /service/myServices` 支持：

| 参数 | 说明 |
| --- | --- |
| `status` | 可选服务状态。 |
| `pageNum` / `current` | 页码，兼容两种命名，默认 `1`。 |
| `pageSize` | 每页数量，默认 `10`，最大 `100`。 |

## 写入请求

`POST /service/publish` 和 `PUT /service/{serviceId}` 支持当前新字段，也兼容旧前端字段：

| 字段 | 说明 |
| --- | --- |
| `title` / `serviceTitle` | 服务标题；发布时必填，编辑时可选。 |
| `description` / `serviceDescription` | 服务描述；发布时必填，编辑时可选。 |
| `tagId`、`tagName` | 标签 ID 和名称快照。 |
| `categoryId`、`categoryName` | 分类 ID 和名称快照。 |
| `professionalId`、`professionalName` | 专业 ID 和名称快照。 |
| `price` | 服务价格；发布时必须大于 0，编辑时若传入也必须大于 0。 |
| `unit` | 价格单位。 |
| `location` | 服务地点说明。 |
| `coverImage` | 封面图；未传时取图片列表第一张。 |
| `images` | 图片 URL 列表；编辑时传 `null` 表示不改图片，传空数组表示清空图片。 |

当前最短链路暂不接系统审核配置，发布和重新上架会直接落为已上架、审核通过。后续迁移后台审核后，再接入审核流。

## 数据表

服务市场当前接入 `service`、`service_image` 和 `service_tag`。本地初始化脚本见 `deploy/docker/mysql/init/002-service.sql`。

状态约定先按最短链路落地：`service.status = 0` 表示已下架，`1` 表示已上架公开可见，`2` 表示审核中，`3` 表示已驳回；非 `1` 状态仅发布者或管理员可见。`service.review_status = 1` 表示审核通过。
