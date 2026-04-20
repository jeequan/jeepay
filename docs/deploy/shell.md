# Shell 脚本一键安装

## 5 分钟上手

在一台干净的 x86_64 服务器（CentOS / Anolis / Ubuntu / Debian）上，复制粘贴一条命令：

**CentOS / Anolis**
```bash
yum install -y wget curl && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && bash install.sh
```

**Ubuntu / Debian**
```bash
apt update && apt-get -y install wget curl git docker.io && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && bash install.sh
```

脚本会自动识别你的系统、装齐依赖、拉镜像、起 8 个容器、跑部署自检。**默认**：

| 项 | 值 |
|---|---|
| 安装目录 | `/jeepayhomes` |
| 镜像来源 | `swr.cn-south-1.myhuaweicloud.com/jeepay/*`（华为云 SWR 公开仓库，免登录） |
| MQ 方案 | RocketMQ（namesrv + broker） |
| 源码 ref | `V3.2.7`（与业务镜像 `3.2.0` 完全兼容） |

装完后访问：

| 平台 | 地址 | 账号 / 密码 |
|---|---|---|
| 运营平台 | `http://外网IP:19217` | `jeepay` / `jeepay123` |
| 商户系统 | `http://外网IP:19218` | 需在运营平台创建商户用户，默认密码 `jeepay666` |
| 支付网关 / 收银台 | `http://外网IP:19216/cashier/index.html` | — |

**卸载**：

```bash
wget -O uninstall.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/uninstall.sh && bash uninstall.sh
```

---

以下章节讲**默认路径之外**的场景：前置依赖细节、自检、端口冲突、HTTPS 反代、高级覆盖项、排障。

## 前置依赖

脚本需要的四个命令，按优先级装齐：

| 命令 | 用途 | 缺失兜底 |
|---|---|---|
| `wget` | 下载 `install.sh` 自身、`config.sh`、前端静态资源包 | **必须由一键命令预装**（脚本本身靠 wget 启动） |
| `curl` | `install.sh` 结束前的部署自检 `[8]` | 缺失只会让自检报 `WARN`，不影响部署本身 |
| `git` | `git clone` 拉取 jeepay 源码 | `install.sh` 会自动识别 `apt` / `dnf` / `yum` / `apk` 并安装 |
| `docker` | 启动所有容器 | 同上，Docker daemon 也会自动 start / enable |

CentOS / Anolis 的一键命令只装 `wget curl` 是因为 `git` 和 `docker` 交给脚本；Ubuntu / Debian 的一键命令把四个都装了，是因为脚本在 minimal Ubuntu 镜像上可能先卡在 `wget -O install.sh`。

## 部署后自检

`install.sh` 结束前会自动执行（约 30 秒）：

1. 轮询 `jeepaymanager` / `jeepaymerchant` / `jeepaypayment` 的 `docker healthcheck` 状态（最长 180 秒）；
2. 探测宿主机 `19216` / `19217` / `19218` 端口的 HTTP 响应；
3. 调用运营平台 `/api/anon/auth/vercode` 接口（会触发 MySQL + Redis 通路）。

任一步骤不达标会打印 `WARN` 并给排查指引。完整日志落在 `/tmp/jeepay-install-<时间戳>.log`（安装开始时脚本会在第一行打印路径，结束时 summary box 再次列出），失败时先看这个文件。

## 默认开放端口

| 组件 | 端口 | 说明 |
| --- | --- | --- |
| MySQL | `3306` | 被占时脚本自动换到 `13306` 之后 |
| Redis | `6379` | 被占时脚本自动换到 `16379` 之后 |
| RocketMQ NameServer | `9876` | 被占时脚本退出 |
| RocketMQ Broker | `10909` / `10911` / `10912` | 被占时脚本退出 |
| 支付网关 | `19216` | payment 服务 |
| 运营平台 | `19217` | manager 服务 |
| 商户平台 | `19218` | merchant 服务 |

## 宿主端口冲突

`install.sh` 在进入 `[1]` 之前会预检以上端口：

- **MySQL / Redis** 被占时 **自动换端口**（3306 → 13306 → 13307…；6379 → 16379 …）并打印 `INFO` 继续部署，无需重跑。容器内仍是标准端口，jeepay 各服务在 `jeepay-net` 内部仍通过 `mysql:3306` / `redis:6379` 通信。
- **RocketMQ / Nginx** 端口与容器内通信耦合（broker 会广播 `brokerIP1:10911`，nginx 的 `listen` 写进静态配置），**不支持自动换**，命中冲突请先释放占用进程再重跑：

```bash
ss -lntp | grep -E ':9876|:1091[0-2]|:1921[6-8]'
```

如果想指定固定 MySQL / Redis 宿主端口（不靠脚本自动挑），见 [高级覆盖项](#高级覆盖项)。

## 配置域名 + HTTPS

脚本内置的 `docs/install/include/nginx.conf` 已经做了三段反代：

- `19217` → 运营平台静态 + `/api/` 反代到 Spring Boot `9217`；
- `19218` → 商户平台静态 + `/api/` 反代到 Spring Boot `9218`；
- `19216` → 整体反代到 Spring Boot `9216`（收银台静态资源与支付 API 均由 `jeepay-payment` 自己提供，静态页面路径为 `/cashier/index.html`）。

三个 `server` 块都补了 `X-Forwarded-Proto` / `X-Forwarded-Port` / `X-Forwarded-For`，配合 Spring Boot 侧 `server.forward-headers-strategy: framework`（已默认开启）即可保证外层 HTTPS 反代时，收银台 `return_url` / 支付回调 URL / 微信 H5 `redirect_url` 拼出的协议与 host 始终正确。

### 拓扑一：三个子域名

最简单的部署方式。外层 nginx 申请三张证书，分别把请求反代到内层的 19217 / 19218 / 19216：

| 外部 | 用途 | 内部回源 |
|---|---|---|
| `https://admin.example.com` | 运营平台 | `http://127.0.0.1:19217` |
| `https://mch.example.com` | 商户平台 | `http://127.0.0.1:19218` |
| `https://pay.example.com` | 支付网关 + 收银台 | `http://127.0.0.1:19216` |

外层 nginx 示例（HTTPS 终结在外层，内层按内部 HTTP 回源）：

```nginx
server {
    listen 443 ssl http2;
    server_name pay.example.com;
    ssl_certificate     /etc/ssl/jeepay/pay.crt;
    ssl_certificate_key /etc/ssl/jeepay/pay.key;

    location / {
        proxy_pass http://127.0.0.1:19216;
        proxy_http_version 1.1;                           # WebSocket 必须（默认 1.0 会让 WS 提前断开）

        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;       # 必须，否则 Spring Boot 拼 http:// 回调
        proxy_set_header X-Forwarded-Port  $server_port;

        # WebSocket（商户端支付测试 / 收银台订单状态推送用）
        proxy_set_header Upgrade   $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout  3600s;
        proxy_send_timeout  3600s;
    }
}
# admin / mch 域名同构，仅 proxy_pass 换为 19217 / 19218
```

收银台联通性验证：`https://pay.example.com/cashier/index.html` 应返回 HTTP 200。

> 实际交易中，客户看到的收银台 URL 不需要手工构造——jeepay API 返回的 `payUrl` 已经是带参数的完整路径（形如 `/cashier/index.html?token=xxx`）。上面的 URL 仅用于部署联通性验证。

### 拓扑二：一个域名 + 路径前缀

只有一个主域名 `https://www.example.com` 把三套前后端塞到子路径。这种拓扑需要同步前端构建时的 `publicPath`（`jeepay-ui` 的 `vue.config.js`）和 Spring Boot 的 `server.servlet.context-path`，否则静态资源 404 / API 404。适合**有前端构建能力**的团队，不建议新手使用。

### 拓扑三：只对外暴露收银台

电商 / SaaS 侧最常见：只把收银台对客户暴露，运营 / 商户平台留在内网 / VPN。

```
公网域名 https://pay.example.com  →  http://127.0.0.1:19216
运营平台 http://内网 IP:19217                        （不对公网放行）
商户平台 http://内网 IP:19218                        （不对公网放行）
```

对应的防火墙规则只放行 19216。

### 反代注意事项

- 外层反代务必传 `X-Forwarded-Proto $scheme`；否则 Spring Boot 会把回调 URL 拼成 `http://`，支付平台回跳失败。
- **WebSocket 必须开 `proxy_http_version 1.1` + Upgrade/Connection 头 + 长 `proxy_read_timeout`**。商户端支付测试页通过 `ws://mch.../api/anon/ws/payOrder/...` 订阅订单状态；缺任一项会导致握手显示 `101 Switching Protocols` 但后续消息收不到。
- 第三方支付平台后台填的 **异步通知 URL / 回跳 URL** 必须用公网域名（`https://pay.example.com/api/pay/...`）而不是 `http://<内网 IP>:19216`。
- 外层 nginx 若开启 `gzip`，注意排除 SSE / WebSocket（`text/event-stream`）。
- 修改 `nginx.conf` 后：`docker exec nginx118 nginx -s reload`。

## 卸载

**推荐（一条命令）**：

```bash
wget -O uninstall.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/uninstall.sh && bash uninstall.sh
```

脚本自动从跑着的 `mysql8` 容器数据卷反推 `rootDir`，打印确认后再删容器、网络、整个 `rootDir`。

若 jeepay 容器已全部被手工删除、自动识别不到：

```bash
rootDir=/jeepayhomes bash uninstall.sh
```

## 高级覆盖项

绝大多数用户用默认值即可。以下变量按需通过环境变量或 [`config.sh`](../install/config.sh)（取消注释）覆盖：

| 变量 | 默认值 | 何时需要改 |
|---|---|---|
| `rootDir` | `/jeepayhomes` | 想把部署目录放到其他位置（比如 `/data/jeepay`） |
| `mysql_pwd` | `jeepaydb123456` | 想改 MySQL root 密码 |
| `mysqlHostPort` | `3306`（被占时自动换） | 想固定到某个特定端口（不让脚本自动挑） |
| `redisHostPort` | `6379`（被占时自动换） | 同上 |
| `mysqlImage` | `swr.../jeepay/mysql:8.0.25` | 切到内网镜像仓库、固定其他版本，或回 Docker Hub |
| `redisImage` | `swr.../jeepay/redis:6.2.14` | 同上 |
| `rocketmqImage` | `swr.../jeepay/rocketmq:5.3.1` | 同上；若使用自行构建的原生 arm64 镜像请同时改 `rocketmqPlatform` |
| `nginxImage` | `swr.../jeepay/nginx:1.18.0` | 同上 |
| `managerImage` / `merchantImage` / `paymentImage` | `swr.../jeepay/jeepay-*:3.2.0` | 切到自有仓库或其他版本 |
| `rocketmqPlatform` | `linux/amd64` | 使用原生 arm64 镜像时改 `linux/arm64`（RocketMQ 上游仅发布 amd64） |
| `jeepayRef` | `V3.2.7` | 想拉其他 release tag 或 master 分支 |

命令行参数：

| 参数 | 作用 |
|---|---|
| `-y` / `--yes` | 跳过脚本中所有 yes/no 确认，适合 CI / 量产脚本 |
| `-h` / `--help` | 打印帮助并退出 |

### ARM64 / Apple Silicon 注意

RocketMQ 上游**只发布 `linux/amd64`** 镜像。在 ARM64 宿主上部署需要先启用 amd64 仿真层，否则 `[5]` 会失败：

- Linux ARM64（自建机 / 云厂商 arm 实例）：`docker run --privileged --rm tonistiigi/binfmt --install amd64`
- macOS Apple Silicon：Docker Desktop 开启 **Use Rosetta for x86_64/amd64 emulation**

其它镜像（mysql / redis / nginx / temurin）SWR 已同步 amd64 + arm64 多架构 manifest，原生运行。

### 切回 Docker Hub 上游镜像示例

```bash
export mysqlImage=mysql:8.0.25
export redisImage=redis:6.2.14
export rocketmqImage=apache/rocketmq:5.3.1
export nginxImage=nginx:1.18.0
export managerImage=jeepay/jeepay-manager:3.2.0
export merchantImage=jeepay/jeepay-merchant:3.2.0
export paymentImage=jeepay/jeepay-payment:3.2.0
bash install.sh
```

## RocketMQ Broker 启动失败

如果安装卡在 `[5]`，脚本会自动输出最近日志并失败退出。优先检查：

1. 服务器架构是否兼容所用的 `rocketmq:5.3.1` 镜像（ARM64 需 amd64 仿真）；
2. `$rootDir/rocketmq/broker/store` 目录权限是否正常；
3. `$rootDir/rocketmq/broker/conf/broker.conf` 是否成功挂载（模板写入的 `brokerIP1` 是否为当前服务器 IP）；
4. `rocketmq-namesrv` 是否已正常启动。

手动排查：

```bash
docker logs --tail 50 rocketmq-namesrv
docker logs --tail 100 rocketmq-broker
# 完整安装日志（安装开始 / 结束都打印了具体路径）
ls -lt /tmp/jeepay-install-*.log | head -1
```
