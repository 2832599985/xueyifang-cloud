# 服务市场接口契约

## 当前范围

本契约覆盖第一批服务市场迁移的最短浏览链路：服务列表、服务详情和服务标签读取。发布、编辑、上下架、收藏和评价创建仍待后续迁移。

## 路由与鉴权

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| `GET` | `/service/list` | 服务列表，默认只返回已上架服务。 | 公开；携带管理员 Token 时可按 `status` 查询非公开服务。 |
| `GET` | `/service/{serviceId}` | 服务详情，返回服务主体和图片列表。 | 公开已上架服务；发布者或管理员可查看非公开服务。 |
| `GET` | `/service/tags` | 服务标签列表。 | 公开 |

网关已将 `/service/**` 转发到 `xueyifang-service`，并对公开 GET 请求尝试解析可选 Token。业务服务只信任网关写入的 `X-User-*` 用户上下文。

## 查询参数

`GET /service/list` 支持：

| 参数 | 说明 |
| --- | --- |
| `keyword` | 按标题或描述模糊查询。 |
| `tagId` | 标签 ID。 |
| `categoryId` | 分类 ID。 |
| `professionalId` | 专业 ID。 |
| `status` | 管理员可指定服务状态；匿名和普通用户固定按已上架查询。 |
| `pageNum` / `current` | 页码，兼容两种命名，默认 `1`。 |
| `pageSize` | 每页数量，默认 `10`，最大 `100`。 |

## 数据表

服务市场当前接入 `service`、`service_image` 和 `service_tag`。本地初始化脚本见 `deploy/docker/mysql/init/002-service.sql`。

状态约定先按最短链路落地：`service.status = 1` 表示已上架公开可见，其他状态仅发布者或管理员可见。
