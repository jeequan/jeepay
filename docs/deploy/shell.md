# Shell 脚本一键安装

> **国内镜像来源**：MySQL / Redis / RocketMQ / Nginx 以及 3 个 jeepay 应用镜像已全部默认指向 **华为云 SWR 公开仓库**（`swr.cn-south-1.myhuaweicloud.com/jeepay/*`），由计全官方维护，**公网可直接匿名拉取**，不依赖 Docker Hub，无需登录，也不需要镜像加速器。

> Shell 一键安装脚本默认部署 **RocketMQ**，会自动启动 `rocketmq-namesrv` 与 `rocketmq-broker`；如需改回 ActiveMQ / RabbitMQ，请同步调整脚本与 `conf/*.yml` 配置。

## 架构前提

| 宿主架构 | MySQL / Redis / Nginx | RocketMQ (namesrv + broker) | 是否一条命令直达 |
|---|---|---|---|
| `x86_64` / `amd64` | 原生运行 | 原生运行 | ✅ 直接跑脚本 |
| `arm64` / Apple Silicon | 原生运行（SWR 已同步 amd64 + arm64 多架构 manifest） | 需 qemu/binfmt 或 Rosetta 2 仿真 | ⚠️ 必须先启用 amd64 仿真 |

说明：

- **RocketMQ 上游只发布 `linux/amd64` 镜像**，ARM64 宿主必须提前注册仿真层才能拉起 `rocketmq-namesrv` / `rocketmq-broker`；否则脚本会在第 5 步直接失败退出。
- **Linux ARM64 宿主**（自建机 / 云厂商 arm 实例）：`docker run --privileged --rm tonistiigi/binfmt --install amd64` 一次即可。
- **macOS Apple Silicon**：Docker Desktop 开启 **Use Rosetta for x86_64/amd64 emulation on Apple Silicon**。
- 如果你自行构建了原生 arm64 的 RocketMQ 镜像，可同时覆盖 `rocketmqImage` 和 `rocketmqPlatform`：

```bash
export rocketmqImage=<你的 arm64 RocketMQ 镜像>
export rocketmqPlatform=linux/arm64
sh install.sh
```

## 前置依赖

脚本运行过程中会使用以下命令；下表说明各自由谁负责，以避免 "minimal 镜像" 上一键命令跑到一半挂掉。

| 命令 | 用途 | 负责方 |
|---|---|---|
| `wget` | 下载 `config.sh` 与前端静态资源包 `html.tar.gz` | **一键命令预装（必须）** |
| `curl` | `install.sh` 结束前的部署自检 `[8]`；缺失只会让自检报 `WARN`，不影响部署本身 | 一键命令预装（建议） |
| `git` | `git clone` 拉取 jeepay 源码 | CentOS / Anolis 上 `install.sh` 检测缺失会 `yum install -y git`；Ubuntu 路径需一键命令预装 |
| `docker` | 启动所有容器 | CentOS / Anolis 上 `install.sh` 检测缺失会 `yum install -y docker-ce`；Ubuntu 路径需一键命令预装 |

## CentOS / Anolis

> 推荐系统：Anolis OS 8.8

```bash
yum install -y wget curl && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && sh install.sh
```

`install.sh` 内部检测到缺失会自动 `yum install` `git` 与 `docker-ce`，因此只需要预先装好 `wget` 和 `curl`。

## Ubuntu

> 推荐系统：Ubuntu 22.04 64 位

```bash
apt update && apt-get -y install wget curl git docker.io && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && sh install.sh
```

`install.sh` 对 apt 系统没有安装兜底，因此 `docker.io` / `git` / `wget` / `curl` 必须在一键命令里装齐。

## 部署后自检

`install.sh` 结束前会自动执行：

1. 轮询 `jeepaymanager` / `jeepaymerchant` / `jeepaypayment` 的 `docker healthcheck` 状态，最长 180 秒；
2. 探测宿主机 `19216` / `19217` / `19218` 端口的 HTTP 响应。

若某个端口未响应或容器未进入 `healthy`，脚本会打印 `WARN` 提示，方便第一时间排查。

## 默认开放端口

| 组件 | 端口 | 说明 |
| --- | --- | --- |
| MySQL | `3306` | 数据库 |
| Redis | `6379` | 缓存 |
| RocketMQ NameServer | `9876` | RocketMQ 注册中心 |
| RocketMQ Broker | `10909` / `10911` / `10912` | RocketMQ Broker |
| 支付网关 | `19216` | payment 服务 |
| 运营平台 | `19217` | manager 服务 |
| 商户平台 | `19218` | merchant 服务 |
| Nginx | `80` | 前端静态资源与反向代理 |

## 卸载

```bash
cd /your/install/path/sources/jeepay/docs/install && sh uninstall.sh
```

卸载脚本会同时删除 `rocketmq-namesrv` 与 `rocketmq-broker` 容器；脚本已做成幂等，缺失容器或网络会自动跳过。

## 自定义镜像源（高级）

绝大多数用户直接用脚本默认镜像（华为云 SWR 公开仓库）即可。只有以下场景需要覆盖：

- 公司 / 集群有内部镜像仓库，想走内部网络；
- 想固定到特定版本或自行构建的镜像；
- 一定要回到 Docker Hub 上游镜像（一般不推荐，国内拉取慢且依赖加速器）。

同样的覆盖项已在 [`config.sh`](../install/config.sh) 中以注释形式列出，取消注释并修改即可；或在执行前直接用环境变量覆盖：

```bash
export mysqlImage=mysql:8.0.25
export redisImage=redis:6.2.14
export rocketmqImage=apache/rocketmq:5.3.1
export nginxImage=nginx:1.18.0
export managerImage=jeepay/jeepay-manager:3.2.0
export merchantImage=jeepay/jeepay-merchant:3.2.0
export paymentImage=jeepay/jeepay-payment:3.2.0
# 若使用原生 arm64 的 RocketMQ 镜像，可同时切换平台
# export rocketmqPlatform=linux/arm64
sh install.sh
```

## RocketMQ Broker 启动失败

如果安装过程卡在 RocketMQ 启动阶段，脚本会自动输出最近日志并直接失败退出。优先检查：

1. 服务器架构是否兼容所用的 `rocketmq:5.3.1` 镜像；
2. `$rootDir/rocketmq/broker/store` 目录权限是否正常；
3. `$rootDir/rocketmq/broker/conf/broker.conf` 是否成功挂载（模板写入的 `brokerIP1` 是否为当前服务器 IP）；
4. `rocketmq-namesrv` 是否已正常启动。

手动排查：

```bash
docker logs --tail 50 rocketmq-namesrv
docker logs --tail 100 rocketmq-broker
```
