# Docker Compose 部署

适合希望通过容器快速拉起完整开发 / 测试环境的场景。

> **国内用户零配置直达。** `docker-compose.yml` 中 MySQL / Redis / RocketMQ 默认镜像、以及 3 个 jeepay 应用 Dockerfile 的 `ARG BASE_IMAGE`（`eclipse-temurin:17-jre`）都已指向 **华为云 SWR 公开仓库**（`swr.cn-south-1.myhuaweicloud.com/jeepay/*`），由计全官方维护，**公网匿名可拉**，不依赖 Docker Hub，无需加速器。下述"国内镜像加速"章节仅在你主动把镜像改回 Docker Hub 或使用其他第三方镜像时才需要。

## 目录要求

默认约定：

```text
jeepay-open/
├── jeepay/
└── jeepay-ui/
```

如果你的前端目录不在 `jeepay` 同级目录，可在 `jeepay/.env` 中覆盖 `UI_BASE_DIR`；可参考根目录的 `.env.example`。

## 构建前准备

### 1. 配置 Docker 国内镜像加速（仅在不走 SWR 时需要）

> 默认镜像都走华为云 SWR 公开仓库，**直接拉即可跳过本节**。只有在你覆盖为 Docker Hub 上游镜像，或者 Compose 文件里某些非 jeepay 维护的镜像仍然来自 Docker Hub 时，才需要配置下面的加速源。

国内网络拉取 Docker Hub 镜像较慢或被墙，建议配置多个镜像加速源（单个源随时可能失效，Docker 会自动 fallback）。

Docker daemon 配置示例：

```json
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io",
    "https://docker.xuanyuan.me",
    "https://hub.rat.dev",
    "https://docker.1panel.live"
  ]
}
```

配置位置：

- **Linux**：`/etc/docker/daemon.json`，修改后执行 `sudo systemctl restart docker`
- **Docker Desktop（macOS / Windows）**：Settings → Docker Engine，写入上述 JSON 后点击 Apply & Restart

验证是否生效：

```bash
docker info | grep -A 5 "Registry Mirrors"
```

### 2. 编译后端 JAR

在 `jeepay` 根目录执行：

```bash
mvn clean package -DskipTests
```

生成的 JAR 位于：

- `jeepay-payment/target/jeepay-payment.jar`
- `jeepay-manager/target/jeepay-manager.jar`
- `jeepay-merchant/target/jeepay-merchant.jar`

### 3. 准备前端代码

确保 `jeepay-ui` 仓库已拉取到本地，且目录结构满足上面的要求。

## 启动方式

```bash
# 首次启动（编译镜像 + 启动）
docker compose up -d --build

# 后续启动（跳过编译）
docker compose up -d

# 仅重新编译后端服务
docker compose build payment manager merchant

# 重启某个服务
docker compose restart payment
```

## 启动前校验

```bash
docker compose config
```

## 默认暴露端口

| 组件 | 端口 | 说明 |
|---|---|---|
| MySQL | `13306` | 映射宿主机 13306，避免与本地 MySQL 冲突 |
| Redis | `6380` | |
| RocketMQ NameServer | `9876` | |
| RocketMQ Broker | `10909` / `10911` / `10912` | |
| payment | `9216` | 支付网关 |
| manager | `9217` | 运营平台后端 |
| merchant | `9218` | 商户系统后端 |
| manager UI | `9227` | 运营平台前端 |
| merchant UI | `9228` | 商户系统前端 |
| cashier UI | `9226` | 收银台前端 |

## 默认账号

| 平台 | 地址 | 用户名 | 密码 | 来源 |
|---|---|---|---|---|
| 运营平台 | http://localhost:9227 | `jeepay` | `jeepay123` | `docs/sql/init.sql` 初始化的超管账号 |
| 商户系统 | http://localhost:9228 | 运营平台创建的商户用户 | `jeepay666` | 运营平台新建商户时的默认密码（`CS.DEFAULT_PWD`），首次登录后建议立即修改 |

> 商户系统没有内置账号，需先用超管登录运营平台，创建商户及其登录用户，再用这个商户用户登录 9228 的商户平台。

## 核心组件版本

| 组件 | 版本 | 说明 |
|---|---|---|
| RocketMQ Server | `5.3.1` | NameServer + Broker |
| rocketmq-spring-boot-starter | `2.3.5` | Spring Boot 集成，内置 client 5.3.2 |
| MySQL | `8` | |
| Redis | `6.2.14` | |
| Java 基础镜像 | `eclipse-temurin:17-jre` | openjdk 官方镜像已停止维护 |

## 配置说明

- Compose 默认使用 **RocketMQ** 作为消息队列，已配置 `rocketmq-namesrv` 与 `rocketmq-broker`。
- `conf/payment`、`conf/manager`、`conf/merchant` 已默认切换为 `rocketMQ`，MySQL / Redis 主机名配置为容器内网名 `mysql`、`redis`。
- MySQL 映射为 `13306:3306`，避免与宿主机本地 MySQL 冲突；容器内部服务仍通过 `mysql:3306` 通信。
- Java 服务的配置文件通过 volume 挂载，修改 `conf/` 目录下的 yml 后重启对应服务即可生效。
- `ui-payment` / `ui-manager` / `ui-merchant` 依赖后端 `payment` / `manager` / `merchant` 的健康检查结果；如单独启动 UI，Compose 会先等待对应 Java 服务进入 `healthy`。
- 如需回退到 ActiveMQ 或切换 RabbitMQ，需同时调整 `docker-compose.yml` 与 `conf/*.yml`。

## 启动后验证

```bash
# 查看所有容器状态
docker compose ps

# 查看核心服务日志
docker compose logs --tail=100 payment manager merchant rocketmq-namesrv rocketmq-broker

# 确认 RocketMQ Broker 启动成功（应看到 "boot success"）
docker logs jeepay-rocketmq-broker 2>&1 | grep "boot success"
```

## 镜像发布（维护者流程，终端用户跳过）

> 以下章节仅面向 **维护者 / 二开团队**，用于向自己的镜像仓库推送构建好的 jeepay 镜像。**终端用户请忽略本节**，直接用仓库默认的华为云 SWR 公开镜像即可。

### 发布到 Docker Hub

先执行一次 `docker login`，然后在 `jeepay` 根目录运行：

```bash
DOCKERHUB_NAMESPACE=<你的 Docker Hub 用户名或组织> \
IMAGE_TAG=3.2.0 \
PUSH_LATEST=true \
sh docker/publish-dockerhub.sh
```

参数：

- `DOCKERHUB_NAMESPACE`：必填，推送目标命名空间。
- `IMAGE_TAG`：镜像版本号，默认 `latest`。
- `PUSH_LATEST=true`：当 `IMAGE_TAG` 不是 `latest` 时，额外推送一份 `latest` 标签。
- `SKIP_MAVEN=true`：如 JAR 已提前打包，可跳过 `mvn clean package -DskipTests`。

脚本会依次构建并推送：

- `<namespace>/jeepay-manager:<tag>`
- `<namespace>/jeepay-merchant:<tag>`
- `<namespace>/jeepay-payment:<tag>`

### 发布到华为云 SWR（统一 tag 自动适配 amd64 / arm64）

先执行一次 `docker login swr.cn-south-1.myhuaweicloud.com`，然后：

```bash
SWR_NAMESPACE=jeepay \
IMAGE_TAG=3.2.0 \
PUSH_LATEST=true \
sh docker/publish-swr.sh
```

说明：

- `publish-swr.sh` 会分别构建并推送 `amd64`、`arm64` 临时镜像，再创建统一的多架构 tag。
- 最终用户只需使用 `swr.cn-south-1.myhuaweicloud.com/<namespace>/jeepay-manager:3.2.0`（及 `latest` / 其他两个服务同理）。
- 如已提前完成 Maven 打包，可加 `SKIP_MAVEN=true`。
- 如需改区域，可覆盖 `SWR_REGISTRY`，例如 `SWR_REGISTRY=swr.cn-north-4.myhuaweicloud.com`。

### 同步第三方基础镜像到华为云 SWR

为避免国内部署依赖 Docker Hub，可先把 MySQL / Redis / RocketMQ / Nginx / Java 基础镜像同步到 SWR：

```bash
SWR_NAMESPACE=jeepay \
sh docker/sync-swr-thirdparty.sh
```

默认会同步以下镜像：

- `mysql:8`
- `mysql:8.0.25`
- `redis:6.2.14`
- `apache/rocketmq:5.3.1`
- `nginx:1.18.0`
- `eclipse-temurin:17-jre`

同步完成后，仓库内默认部署配置会优先使用：

- `swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:*`
- `swr.cn-south-1.myhuaweicloud.com/jeepay/redis:*`
- `swr.cn-south-1.myhuaweicloud.com/jeepay/rocketmq:*`
- `swr.cn-south-1.myhuaweicloud.com/jeepay/nginx:*`
- `swr.cn-south-1.myhuaweicloud.com/jeepay/eclipse-temurin:17-jre`
