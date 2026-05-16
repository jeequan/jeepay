# Jeepay 完整部署流程指南

## 目录

- [项目概述](#项目概述)
- [部署前准备](#部署前准备)
- [一、数据库部署](#一数据库部署)
- [二、主程序部署](#二主程序部署)
- [三、UI前端部署](#三ui前端部署)
- [四、完整部署验证](#四完整部署验证)
- [五、常见问题](#五常见问题)

---

## 项目概述

### 系统架构

Jeepay 是一套完整的开源支付系统，包含以下核心组件：

| 组件 | 说明 | 端口 | 源码位置 |
|------|------|------|----------|
| jeepay-payment | 支付网关服务 | 9216 | [jeepay-payment](file:///workspace/jeepay-payment) |
| jeepay-manager | 运营平台服务 | 9217 | [jeepay-manager](file:///workspace/jeepay-manager) |
| jeepay-merchant | 商户平台服务 | 9218 | [jeepay-merchant](file:///workspace/jeepay-merchant) |
| MySQL | 数据库 | 3306 | 需单独部署 |
| Redis | 缓存服务 | 6379 | 需单独部署 |
| MQ (RocketMQ/RabbitMQ) | 消息队列 | - | 需单独部署 |

### 部署方式选择

| 方式 | 适用场景 | 推荐指数 |
|------|----------|----------|
| Docker Compose | 本地开发/测试环境 | ⭐⭐⭐⭐⭐ |
| Docker 独立容器 | 生产环境/容器化部署 | ⭐⭐⭐⭐ |
| 传统 JAR 部署 | 传统服务器部署 | ⭐⭐⭐ |

---

## 部署前准备

### 硬件要求

| 环境 | 配置 | 内存 | 磁盘 |
|------|------|------|------|
| 开发环境 | 2C4G | 4GB | 50GB |
| 测试环境 | 2C8G | 8GB | 100GB |
| 生产环境 | 4C16G+ | 16GB+ | 200GB+ |

### 软件要求

- **JDK**: 17+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **MySQL**: 8.0+
- **Redis**: 6.2+
- **Git**: 最新版

### 获取代码

```bash
# 克隆后端项目
git clone https://github.com/jeequan/jeepay.git
cd jeepay

# 克隆前端项目（与后端同级）
git clone https://github.com/jeequan/jeepay-ui.git
```

---

## 一、数据库部署

### 1.1 MySQL 部署（Docker 方式）

#### 方式一：Docker Compose 方式（推荐）

在 [docker-compose.yml](file:///workspace/docker-compose.yml#L10-L36) 中已经配置好 MySQL 服务：

```yaml
services:
  mysql:
    hostname: mysql
    container_name: jeepay-mysql
    restart: always
    image: swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:8
    environment:
      LANG: C.UTF-8
      MYSQL_ROOT_PASSWORD: "rootroot"
      MYSQL_DATABASE: "jeepaydb"
      MYSQL_USER: "jeepay"
      MYSQL_PASSWORD: "jeepay"
    ports:
      - "13306:3306"
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -uroot -p$$MYSQL_ROOT_PASSWORD --silent"]
      interval: 10s
      timeout: 5s
      retries: 12
      start_period: 20s
    volumes:
      - mysql:/var/lib/mysql
      - ./docs/sql/init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro
      - ./docs/sql/patch.sql:/docker-entrypoint-initdb.d/02-patch.sql:ro
    networks:
      jeepay:
        ipv4_address: 172.20.0.10
```

**启动 MySQL**：
```bash
docker compose up -d mysql
```

#### 方式二：独立 Docker 容器

```bash
docker run -d \
  --name jeepay-mysql \
  --hostname mysql \
  --restart always \
  -p 13306:3306 \
  -e MYSQL_ROOT_PASSWORD=rootroot \
  -e MYSQL_DATABASE=jeepaydb \
  -e MYSQL_USER=jeepay \
  -e MYSQL_PASSWORD=jeepay \
  -v jeepay-mysql-data:/var/lib/mysql \
  -v /path/to/jeepay/docs/sql/init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro \
  -v /path/to/jeepay/docs/sql/patch.sql:/docker-entrypoint-initdb.d/02-patch.sql:ro \
  swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:8
```

### 1.2 数据库初始化

#### SQL 文件位置

- 初始化 SQL：[docs/sql/init.sql](file:///workspace/docs/sql/init.sql)
- 补丁 SQL：[docs/sql/patch.sql](file:///workspace/docs/sql/patch.sql)

#### 手动初始化（如未自动执行）

```bash
# 进入 MySQL 容器
docker exec -it jeepay-mysql bash

# 连接数据库
mysql -uroot -prootroot

# 创建数据库（如果未创建）
CREATE DATABASE IF NOT EXISTS jeepaydb 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

# 导入数据
USE jeepaydb;
source /docker-entrypoint-initdb.d/01-init.sql;
source /docker-entrypoint-initdb.d/02-patch.sql;
```

#### 验证数据库

```bash
# 检查表
mysql -h 127.0.0.1 -P 13306 -uroot -prootroot jeepaydb -e "SHOW TABLES;"

# 检查初始用户
mysql -h 127.0.0.1 -P 13306 -uroot -prootroot jeepaydb -e "SELECT * FROM t_sys_user;"
```

### 1.3 Redis 部署

#### Docker Compose 方式

在 [docker-compose.yml](file:///workspace/docker-compose.yml#L142-L160) 中已配置：

```yaml
redis:
  platform: linux/amd64
  hostname: redis
  container_name: jeepay-redis
  restart: always
  image: swr.cn-south-1.myhuaweicloud.com/jeepay/redis:6.2.14
  ports:
    - "6380:6379"
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    timeout: 5s
    retries: 12
    start_period: 10s
  volumes:
    - redis:/data
  networks:
    jeepay:
      ipv4_address: 172.20.0.12
```

**启动 Redis**：
```bash
docker compose up -d redis
```

#### 验证 Redis

```bash
# 测试连接
redis-cli -h 127.0.0.1 -p 6380 ping

# 应该返回：PONG
```

### 1.4 消息队列部署（二选一）

#### 方案 A：RocketMQ（推荐，默认方案）

在 [docker-compose.yml](file:///workspace/docker-compose.yml#L38-L97) 中已配置：

```bash
# 启动 RocketMQ
docker compose up -d rocketmq-namesrv rocketmq-broker
```

#### 方案 B：RabbitMQ

```yaml
rabbitmq:
  hostname: rabbitmq
  container_name: jeepay-rabbitmq
  image: rabbitmq:3-management-alpine
  restart: always
  ports:
    - "5672:5672"
    - "15672:15672"
  environment:
    RABBITMQ_DEFAULT_USER: "admin"
    RABBITMQ_DEFAULT_PASS: "admin"
    RABBITMQ_DEFAULT_VHOST: "jeepay"
  volumes:
    - rabbitmq:/var/lib/rabbitmq
  networks:
    jeepay:
      ipv4_address: 172.20.0.14
```

---

## 二、主程序部署

### 2.1 项目编译

#### 方式一：Maven 编译（推荐）

```bash
# 进入项目根目录
cd /path/to/jeepay

# 编译（跳过测试）
mvn clean package -DskipTests

# 编译成功后，各模块的 jar 包在 target/ 目录下
# jeepay-payment/target/jeepay-payment.jar
# jeepay-manager/target/jeepay-manager.jar
# jeepay-merchant/target/jeepay-merchant.jar
```

#### 方式二：Docker 编译（无需本地 Maven）

```bash
# 使用 Docker 进行编译
# 注意：项目已配置好 Dockerfile 用于运行，编译仍需 Maven
```

### 2.2 配置文件准备

三个服务的配置文件分别在：

| 服务 | 配置文件位置 |
|------|--------------|
| jeepay-payment | [conf/payment/application.yml](file:///workspace/conf/payment/application.yml) |
| jeepay-manager | [conf/manager/application.yml](file:///workspace/conf/manager/application.yml) |
| jeepay-merchant | [conf/merchant/application.yml](file:///workspace/conf/merchant/application.yml) |

#### 修改配置关键点

以 [jeepay-payment](file:///workspace/conf/payment/application.yml) 为例：

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/jeepaydb?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: rootroot
  data:
    redis:
      host: redis
      port: 6379
      database: 3
isys:
  mq:
    vender: rocketMQ  # 或 rabbitMQ
rocketmq:
  name-server: rocketmq-namesrv:9876
```

### 2.3 Docker Compose 一键部署（推荐）

使用 [docker-compose.yml](file:///workspace/docker-compose.yml) 一键部署所有服务：

```bash
# 首次部署（编译并启动）
docker compose up -d --build

# 查看启动状态
docker compose ps

# 查看日志
docker compose logs -f payment
docker compose logs -f manager
docker compose logs -f merchant
```

### 2.4 各服务单独部署

#### jeepay-payment 部署

Dockerfile：[jeepay-payment/Dockerfile](file:///workspace/jeepay-payment/Dockerfile)

```bash
# 构建镜像
cd jeepay-payment
docker build -t jeepay-payment:latest .

# 运行容器
docker run -d \
  --name jeepay-payment \
  --hostname payment \
  -p 9216:9216 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jeepaydb \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=rootroot \
  -v /path/to/conf/payment/application.yml:/jeepayhomes/service/app/application.yml \
  -v /path/to/logs/payment:/jeepayhomes/service/logs \
  jeepay-payment:latest
```

#### jeepay-manager 部署

Dockerfile：[jeepay-manager/Dockerfile](file:///workspace/jeepay-manager/Dockerfile)

```bash
# 构建镜像
cd jeepay-manager
docker build -t jeepay-manager:latest .

# 运行容器
docker run -d \
  --name jeepay-manager \
  --hostname manager \
  -p 9217:9217 \
  -v /path/to/conf/manager/application.yml:/jeepayhomes/service/app/application.yml \
  -v /path/to/logs/manager:/jeepayhomes/service/logs \
  jeepay-manager:latest
```

#### jeepay-merchant 部署

Dockerfile：[jeepay-merchant/Dockerfile](file:///workspace/jeepay-merchant/Dockerfile)

```bash
# 构建镜像
cd jeepay-merchant
docker build -t jeepay-merchant:latest .

# 运行容器
docker run -d \
  --name jeepay-merchant \
  --hostname merchant \
  -p 9218:9218 \
  -v /path/to/conf/merchant/application.yml:/jeepayhomes/service/app/application.yml \
  -v /path/to/logs/merchant:/jeepayhomes/service/logs \
  jeepay-merchant:latest
```

### 2.5 传统 JAR 方式部署

#### 准备环境

```bash
# 1. 上传编译好的 JAR 包
# jeepay-payment.jar
# jeepay-manager.jar
# jeepay-merchant.jar

# 2. 创建目录
mkdir -p /jeepayhomes/service/logs
mkdir -p /jeepayhomes/service/app

# 3. 上传配置文件
# /jeepayhomes/service/app/application.yml
```

#### 启动服务

```bash
# jeepay-payment
java -jar jeepay-payment.jar \
  --spring.config.additional-location=file:/jeepayhomes/service/app/application.yml

# jeepay-manager
java -jar jeepay-manager.jar \
  --spring.config.additional-location=file:/jeepayhomes/service/app/application.yml

# jeepay-merchant
java -jar jeepay-merchant.jar \
  --spring.config.additional-location=file:/jeepayhomes/service/app/application.yml
```

#### 注册为 Systemd 服务（推荐）

创建 `/etc/systemd/system/jeepay-payment.service`：

```ini
[Unit]
Description=Jeepay Payment Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=jeepay
WorkingDirectory=/jeepayhomes/service/app
ExecStart=/usr/bin/java -jar jeepay-payment.jar --spring.config.additional-location=file:/jeepayhomes/service/app/application.yml
Restart=always
RestartSec=10
StandardOutput=append:/jeepayhomes/service/logs/payment.out
StandardError=append:/jeepayhomes/service/logs/payment.err

[Install]
WantedBy=multi-user.target
```

启动服务：
```bash
systemctl daemon-reload
systemctl enable jeepay-payment
systemctl start jeepay-payment
systemctl status jeepay-payment
```

---

## 三、UI前端部署

### 3.1 前端项目准备

```bash
# 确保 jeepay-ui 项目与 jeepay 同级
jeepay-open/
├── jeepay/
└── jeepay-ui/
```

### 3.2 Docker Compose 方式部署（推荐）

[docker-compose.yml](file:///workspace/docker-compose.yml#L252-L313) 中已配置前端服务：

```yaml
ui-payment:
  build:
    context: ${UI_BASE_DIR:-..}/jeepay-ui
    dockerfile: Dockerfile
    args:
      PLATFORM: cashier
  image: jeepay-ui-payment:latest
  hostname: payment-ui
  container_name: jeepay-ui-payment
  restart: always
  environment:
    - BACKEND_HOST=172.20.0.21:9216
  ports:
    - "9226:80"
  depends_on:
    payment:
      condition: service_healthy
  networks:
    jeepay:
      ipv4_address: 172.20.0.31

ui-manager:
  build:
    context: ${UI_BASE_DIR:-..}/jeepay-ui
    dockerfile: Dockerfile
    args:
      PLATFORM: manager
  image: jeepay-ui-manager:latest
  hostname: manager-ui
  container_name: jeepay-ui-manager
  restart: always
  environment:
    - BACKEND_HOST=172.20.0.22:9217
  ports:
    - "9227:80"
  depends_on:
    manager:
      condition: service_healthy
  networks:
    jeepay:
      ipv4_address: 172.20.0.32

ui-merchant:
  build:
    context: ${UI_BASE_DIR:-..}/jeepay-ui
    dockerfile: Dockerfile
    args:
      PLATFORM: merchant
  image: jeepay-ui-merchant:latest
  hostname: merchant-ui
  container_name: jeepay-ui-merchant
  restart: always
  environment:
    - BACKEND_HOST=172.20.0.23:9218
  ports:
    - "9228:80"
  depends_on:
    merchant:
      condition: service_healthy
  networks:
    jeepay:
      ipv4_address: 172.20.0.33
```

**启动前端服务**：
```bash
docker compose up -d ui-payment ui-manager ui-merchant
```

### 3.3 前端独立部署

#### 方式一：Nginx 静态部署

```bash
# 1. 进入前端项目目录
cd jeepay-ui

# 2. 安装依赖
npm install

# 3. 构建各平台
# 运营平台
npm run build:manager

# 商户平台
npm run build:merchant

# 收银台
npm run build:cashier

# 4. 配置 Nginx
```

Nginx 配置示例：

```nginx
server {
    listen 80;
    server_name jeepay.example.com;

    # 运营平台
    location /manager/ {
        alias /path/to/jeepay-ui/dist/manager/;
        try_files $uri $uri/ /manager/index.html;
    }

    # 商户平台
    location /merchant/ {
        alias /path/to/jeepay-ui/dist/merchant/;
        try_files $uri $uri/ /merchant/index.html;
    }

    # 收银台
    location /cashier/ {
        alias /path/to/jeepay-ui/dist/cashier/;
        try_files $uri $uri/ /cashier/index.html;
    }

    # 后端接口代理
    location /api/ {
        proxy_pass http://backend-host:9216;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 方式二：Docker 容器方式

创建前端 Dockerfile：

```dockerfile
# jeepay-ui/Dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
ARG PLATFORM=manager
RUN npm run build:${PLATFORM}

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

构建并运行：

```bash
# 构建运营平台
docker build --build-arg PLATFORM=manager -t jeepay-ui-manager:latest .

# 运行
docker run -d \
  --name jeepay-ui-manager \
  -p 9227:80 \
  -e BACKEND_HOST=172.20.0.22:9217 \
  jeepay-ui-manager:latest
```

---

## 四、完整部署验证

### 4.1 服务端口对照表

| 服务 | 容器端口 | 外部端口 | 访问地址 |
|------|----------|----------|----------|
| jeepay-payment | 9216 | 9216 | http://localhost:9216 |
| jeepay-manager | 9217 | 9217 | http://localhost:9217 |
| jeepay-merchant | 9218 | 9218 | http://localhost:9218 |
| ui-payment (收银台) | 80 | 9226 | http://localhost:9226 |
| ui-manager (运营平台) | 80 | 9227 | http://localhost:9227 |
| ui-merchant (商户平台) | 80 | 9228 | http://localhost:9228 |
| MySQL | 3306 | 13306 | - |
| Redis | 6379 | 6380 | - |

### 4.2 部署检查清单

- [ ] MySQL 已启动并初始化完成
- [ ] Redis 已启动并可用
- [ ] 消息队列已启动
- [ ] jeepay-payment 已启动并健康检查通过
- [ ] jeepay-manager 已启动并健康检查通过
- [ ] jeepay-merchant 已启动并健康检查通过
- [ ] ui-payment 已启动
- [ ] ui-manager 已启动
- [ ] ui-merchant 已启动

### 4.3 访问验证

#### 1. 检查健康检查

```bash
# 各服务健康检查
curl -I http://localhost:9216
curl -I http://localhost:9217
curl -I http://localhost:9218
```

#### 2. 访问运营平台

- 地址：http://localhost:9227
- 默认账号：jeepay
- 默认密码：jeepay123

#### 3. 访问商户平台

- 地址：http://localhost:9228
- 需要在运营平台创建商户账号
- 默认商户密码：jeepay666

#### 4. 访问收银台

- 地址：http://localhost:9226/cashier

### 4.4 日志查看

```bash
# 查看所有服务日志
docker compose logs -f

# 查看单个服务日志
docker compose logs -f payment
docker compose logs -f manager
docker compose logs -f merchant

# 查看最近 100 行日志
docker compose logs --tail=100 payment
```

---

## 五、常见问题

### 5.1 数据库问题

#### 问题：无法连接 MySQL

**排查**：
```bash
# 检查 MySQL 容器状态
docker ps | grep mysql

# 查看 MySQL 日志
docker logs jeepay-mysql

# 测试连接
mysql -h 127.0.0.1 -P 13306 -uroot -prootroot
```

### 5.2 Redis 问题

#### 问题：Redis 连接失败

**排查**：
```bash
# 检查 Redis 容器
docker ps | grep redis

# 测试 Redis
redis-cli -h 127.0.0.1 -p 6380 ping
```

### 5.3 MQ 问题

#### 问题：RocketMQ 启动失败

**排查**：
```bash
# 检查容器状态
docker compose ps

# 查看日志
docker compose logs -f rocketmq-namesrv
docker compose logs -f rocketmq-broker
```

### 5.4 应用启动问题

#### 问题：应用健康检查失败

**排查**：
```bash
# 查看应用日志
docker compose logs payment

# 检查配置文件
docker exec jeepay-payment cat /jeepayhomes/service/app/application.yml

# 测试端口连通性
docker exec jeepay-payment telnet mysql 3306
docker exec jeepay-payment telnet redis 6379
```

---

## 附录

### A. 常用命令速查

```bash
# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose down

# 重启特定服务
docker compose restart payment manager

# 重新编译并启动
docker compose up -d --build

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f

# 进入容器
docker exec -it jeepay-payment bash

# 查看资源使用
docker stats
```

### B. 环境变量配置

创建 `.env` 文件：

```bash
# 镜像配置
MYSQL_IMAGE=swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:8
REDIS_IMAGE=swr.cn-south-1.myhuaweicloud.com/jeepay/redis:6.2.14
ROCKETMQ_IMAGE=swr.cn-south-1.myhuaweicloud.com/jeepay/rocketmq:5.3.1

# 前端项目位置
UI_BASE_DIR=..

# 数据库密码
MYSQL_ROOT_PASSWORD=rootroot
MYSQL_PASSWORD=jeepay
```

### C. 快速一键部署脚本

```bash
#!/bin/bash

echo "=== Jeepay 一键部署 ==="

# 1. 编译项目
echo "1. 编译项目..."
mvn clean package -DskipTests

# 2. 启动所有服务
echo "2. 启动所有服务..."
docker compose up -d --build

# 3. 等待服务启动
echo "3. 等待服务启动（30秒）..."
sleep 30

# 4. 检查服务状态
echo "4. 检查服务状态..."
docker compose ps

echo ""
echo "=== 部署完成 ==="
echo "运营平台: http://localhost:9227"
echo "商户平台: http://localhost:9228"
echo "收银台: http://localhost:9226"
echo ""
echo "默认账号: jeepay / jeepay123"
```

---

**文档版本**: v1.0
**最后更新**: 2026-05-16
**维护团队**: Jeepay 团队
