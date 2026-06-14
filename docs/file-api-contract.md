# 文件服务接口契约

## 范围

`xueyifang-file` 承接原单体 `/file/*` 文件能力，当前优先支持本地存储，用于用户头像和服务图片上传、批量上传、删除和公开访问。

文件服务只负责文件存储和访问 URL 生成，不直接写入 `user.avatar` 或 `service_image`。头像落库仍通过用户资料接口完成；服务图片关系仍由服务市场发布或编辑接口保存。

## 网关路由

| 外部路径 | 网关处理 | 目标服务 |
| --- | --- | --- |
| `/api/file/**` | `StripPrefix=1` 后转发为 `/file/**` | `lb://xueyifang-file` |
| `/file/**` | 直接转发 | `lb://xueyifang-file` |

`GET /api/file/view/**` 和 `GET /file/view/**` 为公开访问；上传、批量上传和删除需要登录。

## 业务类型

| biz | 用途 | 默认大小限制 |
| --- | --- | --- |
| `user_avatar` | 用户头像 | 1MB |
| `service_image` | 服务图片 | 5MB |

默认允许扩展名：`jpg`、`jpeg`、`png`、`gif`、`webp`。

## 接口

### 上传单文件

`POST /file/upload`

请求类型：`multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | file | 是 | 上传文件。 |
| `biz` | string | 是 | `user_avatar` 或 `service_image`。 |

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": "/api/file/view/service_image/10/202606/abc-image.png"
}
```

### 批量上传

`POST /file/upload/batch`

请求类型：`multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `files` | file[] | 是 | 上传文件列表。 |
| `biz` | string | 是 | `user_avatar` 或 `service_image`。 |

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": [
    "/api/file/view/service_image/10/202606/abc-cover.png",
    "/api/file/view/service_image/10/202606/def-detail.png"
  ]
}
```

### 删除文件

`DELETE /file/delete?url={fileUrl}`

需要登录。`url` 可传上传接口返回的完整 URL、`/api/file/view/...`、`/file/view/...` 或相对路径。

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": true
}
```

### 查看文件

`GET /file/view/{biz}/{userId}/{yyyyMM}/{filename}`

本地存储模式下直接返回文件内容，图片使用 `inline` 响应头，支持浏览器直接展示。

## 配置

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | `8600` | 文件服务端口。 |
| `xueyifang.file.storage.type` | `local` | 当前支持本地存储。 |
| `xueyifang.file.storage.local.upload-path` | `uploads` | 上传目录，支持绝对路径或相对路径。 |
| `xueyifang.file.storage.local.url-prefix` | `/api/file/view` | 上传成功后返回的访问 URL 前缀。 |
| `xueyifang.file.storage.local.enable-date-path` | `true` | 是否按年月生成目录。 |
| `xueyifang.file.storage.max-size.user_avatar` | `1MB` | 头像大小限制。 |
| `xueyifang.file.storage.max-size.service_image` | `5MB` | 服务图片大小限制。 |

## 安全约束

- 写接口必须由 Gateway 透传 `X-User-*` 用户上下文。
- 文件扩展名按白名单校验。
- 文件查看和删除都会进行路径归一化，拒绝路径遍历。
- 当前没有文件元数据表，因此删除接口只能按路径删除，不做业务归属校验。
