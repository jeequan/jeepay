# Docker Compose 部署

适合本地 / 测试环境拉起完整集群（含前端）。

## 目录约定

```
jeepay-open/
├── jeepay/
└── jeepay-ui/
```

前端不在 jeepay 同级时，可在 `jeepay/.env` 覆盖 `UI_BASE_DIR`（参考 `.env.example`）。

## 启动

```bash
cd jeepay
mvn clean package -DskipTests          # 编译后端 JAR

docker compose up -d --build           # 首次：编译镜像 + 启动
docker compose up -d                   # 后续：跳过编译
docker compose restart payment         # 重启某个服务
```

**默认镜像**指向华为云 SWR 公开仓库（与 Shell 部署一致），国内零配置直达。

## 暴露端口

| 组件 | 端口 |
|---|---|
| MySQL | `13306`（避开宿主 3306） |
| Redis | `6380` |
| RocketMQ | `9876` / `10909` / `10911` / `10912` |
| payment / manager / merchant | `9216` / `9217` / `9218` |
| 前端 | manager `9227` / merchant `9228` / cashier `9226` |

## 默认账号

- 运营平台 `http://localhost:9227`：`jeepay` / `jeepay123`
- 商户系统 `http://localhost:9228`：需运营平台创建商户用户（默认密码 `jeepay666`）

## 常用命令

```bash
docker compose ps
docker compose logs -f --tail=100 payment manager merchant
docker compose config                  # 启动前变量 / 语法校验
docker compose down                    # 停止 + 清网络（卷保留）
docker compose down -v                 # 连同卷一起删
```

## 配置说明

- 默认 MQ = RocketMQ；`conf/payment|manager|merchant/application.yml` 已切到 `rocketMQ`。
- 容器内 MySQL / Redis 主机名 = `mysql` / `redis`（compose 已配 network alias）。
- 改 `conf/*.yml` 后 `docker compose restart <service>` 即可。
- 要回到 ActiveMQ / RabbitMQ，需同时改 `docker-compose.yml` 和 `conf/*.yml`。

---

- 出问题排查：[troubleshooting.md](./troubleshooting.md)
- 推镜像到 Docker Hub / 华为云 SWR（维护者流程）：[publish.md](./publish.md)
