# 本地基础设施

## 目标

本地开发先使用 Docker Compose 启动 MySQL、Redis 和 Nacos。应用默认连接 `127.0.0.1:8848` 的 Nacos，并使用可选配置导入；没有启动 Nacos 时，Maven 构建不受影响。运行服务注册、发现和网关 `lb://` 路由时仍需要启动 Nacos。

## 组件

| 组件 | 镜像 | 端口 | 用途 |
| --- | --- | --- | --- |
| MySQL | `mysql:8.0.39` | `3306` | 业务数据库。 |
| Redis | `redis:7.2-alpine` | `6379` | Token 黑名单、缓存、限流和幂等。 |
| Nacos | `nacos/nacos-server:v2.4.3` | `8848`、`9848`、`9849` | 服务注册和配置中心。 |

## 启动

```powershell
Copy-Item deploy/docker/.env.example deploy/docker/.env
docker compose --env-file deploy/docker/.env -f deploy/docker/docker-compose.yml up -d
```

Nacos 控制台地址：

```text
http://localhost:8848/nacos
```

本地 Compose 关闭了 Nacos 鉴权，只用于开发环境。生产环境必须开启鉴权，并改用独立数据库持久化 Nacos 配置。

## 应用接入

每个应用服务都配置了：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `NACOS_SERVER_ADDR` | `127.0.0.1:8848` | Nacos 地址。 |
| `NACOS_USERNAME` | `nacos` | 预留给开启鉴权后的账号。 |
| `NACOS_PASSWORD` | `nacos` | 预留给开启鉴权后的密码。 |
| `NACOS_NAMESPACE` | 空 | 默认 public 命名空间。 |
| `NACOS_GROUP` | `DEFAULT_GROUP` | 默认配置和注册分组。 |

配置导入格式：

```text
optional:nacos:${spring.application.name}.yml?group=${NACOS_GROUP:DEFAULT_GROUP}
```

`optional` 是刻意保留的。这样新服务还没完成配置中心治理时，缺少远端配置不会阻断本地 `application.yml` 加载；完整运行微服务链路时仍应先启动 Nacos。
