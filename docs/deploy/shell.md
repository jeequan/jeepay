# Shell 脚本一键安装

在一台干净的 x86_64 服务器（CentOS / Anolis / Ubuntu / Debian）上，复制粘贴一条命令：

**CentOS / Anolis**
```bash
yum install -y wget curl && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && bash install.sh
```

**Ubuntu / Debian**
```bash
apt update && apt-get -y install wget curl git docker.io && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && bash install.sh
```

脚本自动：识别发行版、装齐依赖（`wget` / `curl` / `git` / `docker`）、拉镜像、起 8 个容器、跑部署自检。**默认**：

| 项 | 值 |
|---|---|
| 安装目录 | `/jeepayhomes` |
| 镜像来源 | `swr.cn-south-1.myhuaweicloud.com/jeepay/*`（华为云 SWR 公开仓库，免登录） |
| MQ | RocketMQ（namesrv + broker） |
| 源码 ref | 最新 release tag（与业务镜像完全兼容） |

装完访问：

| 平台 | 地址 | 账号 / 密码 |
|---|---|---|
| 运营平台 | `http://<IP>:19217` | `jeepay` / `jeepay123` |
| 商户系统 | `http://<IP>:19218` | 需在运营平台创建商户用户，默认密码 `jeepay666` |
| 支付网关 / 收银台 | `http://<IP>:19216/cashier/index.html` | — |

## 常用命令

```bash
# 容器 / 日志 / 资源
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
docker logs -f jeepaymanager --tail 100
docker stats --no-stream

# 改完 yml / nginx.conf 后重启或重载
docker restart jeepaymanager jeepaymerchant jeepaypayment
docker exec nginx118 nginx -t && docker exec nginx118 nginx -s reload

# 完整安装日志（每步输出 + summary box）
ls -lt /tmp/jeepay-install-*.log | head -1

# MySQL / Redis 直连
docker exec -it mysql8 mysql -uroot -p'jeepaydb123456' jeepaydb
docker exec -it redis6 redis-cli
```

## 卸载

```bash
wget -O uninstall.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/uninstall.sh && bash uninstall.sh
```

脚本自动识别 `rootDir`（从 `mysql8` 容器卷反推），打印确认后删容器 / 网络 / 整个 `rootDir`。若容器已手工清掉，可显式：`rootDir=/jeepayhomes bash uninstall.sh`。

## 高级覆盖项（可选）

安装前 `export xxx=yyy`，或在 `docs/install/config.sh` 取消对应注释行即可。

| 变量 | 默认 | 场景 |
|---|---|---|
| `rootDir` | `/jeepayhomes` | 改部署目录 |
| `mysql_pwd` | `jeepaydb123456` | 改 MySQL root 密码 |
| `mysqlHostPort` / `redisHostPort` | `3306` / `6379`（被占自动换到 `13306` / `16379`） | 固定到某个端口 |
| `mysqlImage` / `redisImage` / `rocketmqImage` / `nginxImage` | SWR 官方 | 切内网仓库 / 回 Docker Hub |
| `managerImage` / `merchantImage` / `paymentImage` | SWR `jeepay-*:3.2.0` | 换自有镜像 / 版本 |
| `rocketmqPlatform` | `linux/amd64` | 自行构建的 arm64 RocketMQ 镜像时改 `linux/arm64`（上游 rocketmq 不发 arm64） |
| `brokerIP1` | `rocketmq-broker` | 有外部 RocketMQ 客户端时改真实 IP |
| `jeepayRef` | 最新 release tag | 临时拉 master 或其他 tag |

---

- 出问题排查：[troubleshooting.md](./troubleshooting.md)
- 域名 + HTTPS 反代：[https.md](./https.md)
- Docker Compose 部署：[compose.md](./compose.md)
