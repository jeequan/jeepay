# 部署常见问题排查

排障第一步：先看**完整安装日志**（Shell 部署时脚本在开头与 summary box 里都打印了路径）：

```bash
ls -lt /tmp/jeepay-install-*.log | head -1
less $(ls -t /tmp/jeepay-install-*.log | head -1)
```

## 登录页验证码不出 / 任何 DB 操作报错

99% 是 manager 连不上 MySQL 或 Redis。按下面顺序排查：

```bash
# 1) 三个应用是否 healthy
for c in jeepaymanager jeepaymerchant jeepaypayment; do
  echo -n "$c: "; docker inspect --format '{{.State.Health.Status}}' $c
done

# 2) manager 实际挂载的 application.yml
docker exec jeepaymanager grep -nE "url:|host:|password:" /jeepayhomes/service/app/application.yml

# 3) 从 manager 容器内验证 DB / Redis 连通
docker exec jeepaymanager getent hosts mysql redis
docker exec jeepaymanager sh -c "timeout 3 bash -c '</dev/tcp/mysql/3306' && echo MySQL_OK"
docker exec jeepaymanager sh -c "timeout 3 bash -c '</dev/tcp/redis/6379' && echo Redis_OK"
```

历史坑（V3.2.7 之前的老部署可能遇到）：
- `application.yml` 里 `host: redis` / `mysql`，但容器名是 `redis6` / `mysql8` → 新版 `install.sh` 已给容器加 `--network-alias`。
- `password: rootroot`（Compose 默认）与 `mysql_pwd=jeepaydb123456` 不一致 → 新版 `install.sh` 会自动 `sed` 替换。

## 业务触发 RocketMQ 时报 `connect to x.x.x.x:10911 failed`

两个可能原因：
1. **broker.conf 里 `brokerIP1` 是宿主的某个 Docker bridge 地址**（老版本用 `hostname -I` 挑错了）：
   ```bash
   rootDir=$(docker inspect mysql8 --format '{{range .Mounts}}{{if eq .Destination "/var/lib/mysql"}}{{.Source}}{{end}}{{end}}' | sed 's|/mysql/data$||')
   sed -i 's/^brokerIP1=.*/brokerIP1=rocketmq-broker/' $rootDir/rocketmq/broker/conf/broker.conf
   docker restart rocketmq-broker
   ```
2. **broker 启动好了但 Spring Boot RocketMQ producer 缓存了坏连接**——重启三个业务应用让客户端重新从 nameserver 拉路由：
   ```bash
   docker restart jeepaymanager jeepaymerchant jeepaypayment
   ```

## RocketMQ Broker 启动失败 NullPointerException

Broker 日志出现 `ScheduleMessageService.configFilePath` 相关 NPE，通常是 volume 权限问题。RocketMQ 5.x 镜像以 `rocketmq`（uid=3000）用户运行，而 Docker volume 默认 root 权限：

```bash
docker run --rm -u root -v jeepay_rocketmq_broker_store:/data alpine chown -R 3000:3000 /data
docker run --rm -u root -v jeepay_rocketmq_broker_logs:/data alpine chown -R 3000:3000 /data
```

Compose 已通过 `user: "0:0"` 规避；Shell 部署命中需手工修。

## WebSocket 握手 101 但收不到消息

商户端支付测试页 `ws://.../api/anon/ws/payOrder/...` 订阅订单状态，握手显示 `101 Switching Protocols` 但后续没数据。外层 nginx 必须配：

```nginx
proxy_http_version 1.1;                # 必须，HTTP/1.0 下 Connection: close 隐式生效
proxy_set_header Upgrade   $http_upgrade;
proxy_set_header Connection "upgrade";
proxy_read_timeout  3600s;             # 默认 60s，长连接必断
proxy_send_timeout  3600s;
```

内层 jeepay 内置 nginx 已从 V3.2.7 起全量补齐，外层反代若自己维护需同步修。

## `application.yml` 变成目录而不是文件

Spring Boot 容器起来但拿不到配置。原因：某次 `cp` 失败后 `docker run -v <path>:<container>` 把 host 不存在的 path 自动建成目录：

```bash
docker stop jeepaymanager jeepaymerchant jeepaypayment
rm -rf $rootDir/service/configs
bash uninstall.sh        # 或手工重跑 install.sh 的 [6] 步骤
```

V3.2.8+ 的 `install.sh` 每个 `cp` 都加了返回值 + 文件类型校验，失败会 `exit 1` 明确指引，不再留半成品。

## Apple Silicon（M1/M2/M3）

RocketMQ 官方镜像只发布 `linux/amd64`，在 Apple Silicon 上通过 Rosetta 2 模拟运行，启动略慢属正常。Docker Desktop → Settings → 开启 **Use Rosetta for x86_64/amd64 emulation**。

## 镜像拉取失败（403 / 超时）

默认 SWR 镜像偶尔有地区限制，先确认：

```bash
docker pull swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:8.0.25
```

如持续失败，可临时回 Docker Hub 上游（安装前 export）：

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

## 前端镜像构建失败（Docker Compose 场景）

- `jeepay-ui` 目录是否存在且在 `jeepay` 同级？
- `UI_BASE_DIR` 是否正确？
- npm 源是否可达？必要时在 `jeepay-ui` 的 Dockerfile 里切淘宝源。

## 打包排障现场给维护者

```bash
ts=$(date +%Y%m%d-%H%M%S)
out=/tmp/jeepay-debug-$ts
mkdir -p $out
docker ps -a > $out/docker-ps.txt
for c in mysql8 redis6 rocketmq-namesrv rocketmq-broker jeepaymanager jeepaymerchant jeepaypayment nginx118; do
    docker logs --tail 200 $c > $out/log-$c.txt 2>&1
    docker inspect $c > $out/inspect-$c.json 2>&1
done
docker network inspect jeepay-net > $out/network.json 2>&1
cp $(ls -t /tmp/jeepay-install-*.log | head -1) $out/install.log 2>/dev/null
tar -czf $out.tar.gz -C /tmp "$(basename $out)"
echo "已打包到：$out.tar.gz"
```

把 `.tar.gz` 发维护者，能直接定位。
