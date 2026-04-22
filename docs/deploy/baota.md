# 宝塔面板部署

面向在宝塔（BaoTa）面板上把 jeepay 作为 Docker 应用一键安装的场景。宝塔用户不需要 `git clone` 源码，所有配置文件由 compose 里的 `jeepay-configs` init 容器（`alpine/git` 镜像）从 gitee 按 tag 拉下来分发。

## Compose 文件

- 位置：仓库根目录 [`docker-compose.baota.yml`](../../docker-compose.baota.yml)
- 宝塔应用模板引用该文件作为 `docker-compose` 部分即可

## 镜像默认地址

所有业务镜像 + MySQL / Redis / RocketMQ / Nginx 默认走**华为云 SWR 公开仓库**（`swr.cn-south-1.myhuaweicloud.com/jeepay/*`），由计全官方维护，国内直拉不需要 Docker Hub 加速器。`alpine/git`（配置分发 init 容器）仍从 Docker Hub 拉（< 20MB，一次性）。

想改回 Docker Hub 上游镜像，可通过下面"可选环境变量"覆盖。

## 宝塔应用模板要声明的变量

### 必须

| 变量 | 用途 | 建议默认 |
|---|---|---|
| `VERSION` | 业务镜像 tag（`jeepay-manager` 等） | `3.2.0` |
| `CONFIG_REF` | 配置源 git ref（与 `VERSION` 同版本的 tag 或 `master`） | 当前 release tag |
| `UI_RELEASE` | 前端 `html.tar.gz` 的 `jeepay-ui` release tag | `V3.0.0` |
| `APP_PATH` | 宿主部署根目录 | 宝塔默认 |
| `HOST_IP` | 对外绑定 IP | 宝塔默认 |
| `WEB_HTTP_PORT1` / `2` / `3` | 映射到容器 `19216` / `19217` / `19218` | `19216` / `19217` / `19218` |
| `CPUS` / `MEMORY_LIMIT` | nginx 资源限制 | `1` / `512m` |

### 可选（改镜像源时覆盖）

| 变量 | 默认 |
|---|---|
| `MYSQL_IMAGE` | `swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:8.0.25` |
| `REDIS_IMAGE` | `swr.cn-south-1.myhuaweicloud.com/jeepay/redis:6.2.14` |
| `ROCKETMQ_IMAGE` | `swr.cn-south-1.myhuaweicloud.com/jeepay/rocketmq:5.3.1` |
| `NGINX_IMAGE` | `swr.cn-south-1.myhuaweicloud.com/jeepay/nginx:1.18.0` |
| `MANAGER_IMAGE` | `swr.cn-south-1.myhuaweicloud.com/jeepay/jeepay-manager:${VERSION}` |
| `MERCHANT_IMAGE` | `swr.cn-south-1.myhuaweicloud.com/jeepay/jeepay-merchant:${VERSION}` |
| `PAYMENT_IMAGE` | `swr.cn-south-1.myhuaweicloud.com/jeepay/jeepay-payment:${VERSION}` |
| `JEEPAY_CONFIGS_IMAGE` | `alpine/git:v2.45.2`（Docker Hub） |

**切回 Docker Hub 整套示例**：

```
MYSQL_IMAGE=mysql:8.0.25
REDIS_IMAGE=redis:6.2.14
ROCKETMQ_IMAGE=apache/rocketmq:5.3.1
NGINX_IMAGE=nginx:1.18.0
MANAGER_IMAGE=jeepay/jeepay-manager:3.2.0
MERCHANT_IMAGE=jeepay/jeepay-merchant:3.2.0
PAYMENT_IMAGE=jeepay/jeepay-payment:3.2.0
```

## 安装后怎么改配置

配置文件都在 `${APP_PATH}/jeepayhomes/jeepayConfigs/` 下，**直接编辑即可**：

```
jeepayConfigs/
├── db/my.cnf, init.sql
├── redis/redis.conf
├── rocketmq/broker.conf
├── service/{manager,merchant,payment}/application.yml
├── nginx/nginx.conf, conf.d/, html/
└── .latest/        # 最新版模板（只读参考，不要改）
```

改完重启对应服务：

```bash
docker compose restart jeepay-manager
# nginx 改完可热加载
docker exec nginx118 nginx -t && docker exec nginx118 nginx -s reload
```

`jeepay-configs` 用 `cp -n` 铺配置，**已存在的文件永远不覆盖**，机器重启 / compose up 都不会丢失用户编辑。

## 升级版本

1. 在宝塔 UI 改 `VERSION` 和 `CONFIG_REF` 到新 tag（两者建议同值）
2. 重建应用：宝塔 UI 点"重建"，或 `docker compose up -d`
3. `.latest/` 目录会同步刷新到新版模板，用户自己的 yml 不动
4. 看 `diff` 决定是否合并新模板中的新字段：
   ```bash
   diff ${APP_PATH}/jeepayhomes/jeepayConfigs/service/manager/application.yml \
        ${APP_PATH}/jeepayhomes/jeepayConfigs/.latest/conf/manager/application.yml
   ```

## 宝塔场景特有的点

- **不需要 `jeepay/jeepay-configs` 镜像**：从 `alpine/git` 直接 clone，配置永远跟 git tag 一致
- **`jeepay-configs` 容器安装后显示 `Exited (0)`**：正常，一次性 init 容器跑完即退出
- **`baota_net` 是宝塔预建网络**：compose 里 `external: true`，不要自己创建

## 验证

安装完成后检查：

```bash
# 8 个业务容器 + 1 个 init 容器
docker ps -a --format "table {{.Names}}\t{{.Status}}" | grep -E "jeepay|mysql8|redis6|rocketmq|nginx118"

# 三个 API healthy
for c in jeepaymanager jeepaymerchant jeepaypayment; do
  echo -n "$c: "; docker inspect --format '{{.State.Health.Status}}' $c
done

# 验证码 / 登录（浏览器访问宝塔映射的 19217 端口）
```

## 出问题看哪里

- [troubleshooting.md](./troubleshooting.md)：所有部署方式通用的故障排查
- [https.md](./https.md)：配置域名 + HTTPS 反代（宝塔场景建议在外层再套一层 nginx + 证书）
- `${APP_PATH}/jeepayhomes/service/logs/`：业务服务日志
- `docker logs jeepay-configs`：配置分发是否成功
