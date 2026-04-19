# Shell 脚本一键安装

> **国内用户直接使用默认镜像即可。** 脚本中 MySQL / Redis / RocketMQ / Nginx 以及 3 个 jeepay 应用镜像已全部默认指向 **华为云 SWR 公开仓库**（`swr.cn-south-1.myhuaweicloud.com/jeepay/*`），由计全官方维护，**公网可直接匿名拉取**，不依赖 Docker Hub，无需登录，也不需要镜像加速器。

> Shell 一键安装脚本默认部署 **RocketMQ**，会自动启动 `rocketmq-namesrv` 与 `rocketmq-broker`；如需改回 ActiveMQ / RabbitMQ，请同步调整脚本与 `conf/*.yml` 配置。

## CentOS

> 推荐系统：Anolis OS 8.8

```bash
yum install -y wget && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && sh install.sh
```

## Ubuntu

> 推荐系统：Ubuntu 22.04 64 位

```bash
apt update && apt-get -y install docker.io && apt-get -y install git && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && sh install.sh
```

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

执行前用环境变量覆盖即可，例如切回 Docker Hub 上游：

```bash
export mysqlImage=mysql:8.0.25
export redisImage=redis:6.2.14
export rocketmqImage=apache/rocketmq:5.3.1
export nginxImage=nginx:1.18.0
export managerImage=jeepay/jeepay-manager:3.2.0
export merchantImage=jeepay/jeepay-merchant:3.2.0
export paymentImage=jeepay/jeepay-payment:3.2.0
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
