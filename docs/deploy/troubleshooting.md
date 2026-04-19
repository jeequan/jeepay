# 部署常见问题排查

## RocketMQ Broker 启动失败（NullPointerException）

如果 Broker 日志中出现 `ScheduleMessageService.configFilePath` 相关 NPE，通常是 Docker named volume 的权限问题。RocketMQ 5.x 镜像以 `rocketmq`（uid=3000）用户运行，而 Docker 创建的 volume 默认 `root` 权限。

`docker-compose.yml` 中已通过 `user: "0:0"` 解决此问题。如果手动部署遇到此问题，可执行：

```bash
docker run --rm -u root -v jeepay_rocketmq_broker_store:/data alpine chown -R 3000:3000 /data
docker run --rm -u root -v jeepay_rocketmq_broker_logs:/data alpine chown -R 3000:3000 /data
```

## Apple Silicon（M1/M2/M3）注意事项

RocketMQ 官方镜像仅提供 `linux/amd64` 版本，在 Apple Silicon 上通过 Rosetta 2 模拟运行，启动较慢属于正常现象。请确保 Docker Desktop 已开启 **Use Rosetta for x86_64/amd64 emulation on Apple Silicon**。

## 前端镜像构建失败

优先检查：

- `jeepay-ui` 目录是否存在且在正确位置；
- Node.js 依赖是否可正常安装（npm 源是否可达）。

## 登录提示"认证服务出现异常"

检查 `conf/*/application.yml` 中的数据库连接配置是否与 `docker-compose.yml` 中的 MySQL 密码一致。Compose 默认 root 密码为 `rootroot`。

## 镜像拉取失败（403 Forbidden）

镜像加速源可能失效，建议配置多个加速源（参见 [Docker Compose 部署 — 配置 Docker 国内镜像加速](./compose.md#1-配置-docker-国内镜像加速强烈建议)），Docker 会自动尝试下一个。
