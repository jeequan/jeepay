# Jeepay Railway 部署指南

## 目录

- [概述](#概述)
- [部署架构](#部署架构)
- [前期准备](#前期准备)
- [部署步骤](#部署步骤)
- [配置详解](#配置详解)
- [数据库初始化](#数据库初始化)
- [前端部署](#前端部署)
- [HTTPS配置](#https配置)
- [运维监控](#运维监控)
- [故障排查](#故障排查)

---

## 概述

### Railway平台简介

Railway是一个现代化的云部署平台，提供以下优势：
- Git集成自动部署
- 原生支持Docker
- 内置数据库插件（MySQL/PostgreSQL/Redis）
- 自动SSL证书管理
- 多环境支持
- 按使用量计费

### Jeepay部署挑战

Jeepay是一套**微服务架构的Java支付系统**，包含：

| 服务 | 说明 | 资源需求 |
|------|------|---------|
| jeepay-payment | 支付网关 | 1GB+ RAM, 1 Core |
| jeepay-manager | 运营平台 | 512MB+ RAM |
| jeepay-merchant | 商户平台 | 512MB+ RAM |
| MySQL | 数据库 | 1GB+ RAM |
| Redis | 缓存 | 256MB+ RAM |
| RocketMQ | 消息队列 | 512MB+ RAM |

**主要挑战**：
1. Railway不原生支持RocketMQ
2. 多服务需要协调部署
3. 内存资源需要合理规划

---

## 部署架构

### 推荐架构方案

#### 方案一：全云服务架构（生产环境推荐）

```
┌─────────────────────────────────────────────────────────────┐
│                        Railway                               │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐         │
│  │   Payment   │ │   Manager   │ │   Merchant  │         │
│  │   (9216)   │ │   (9217)    │ │   (9218)    │         │
│  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘         │
│         │                │                │                 │
│         └────────────────┼────────────────┘                 │
│                          │                                   │
│                    ┌─────▼─────┐                            │
│                    │   Nginx   │                            │
│                    │  (反向代理) │                            │
│                    └─────┬─────┘                            │
└──────────────────────────┼──────────────────────────────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
    ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼─────┐
    │   MySQL   │   │   Redis   │   │  RocketMQ │
    │ (插件/云) │   │ (插件/云)  │   │ (阿里云)  │
    └───────────┘   └───────────┘   └───────────┘
```

#### 方案二：简化架构（开发测试环境）

使用RabbitMQ替代RocketMQ：

```
┌─────────────────────────────────────────────────────────────┐
│                        Railway                               │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐         │
│  │   Payment   │ │   Manager   │ │   Merchant  │         │
│  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘         │
│         │                │                │                 │
│         └────────────────┼────────────────┘                 │
│                          │                                   │
│                    ┌─────▼─────┐                            │
│                    │   Nginx   │                            │
│                    └─────┬─────┘                            │
│         ┌────────────────┼────────────────┐                │
│         │                │                │                │
│   ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼─────┐         │
│   │   MySQL   │   │   Redis   │   │  RabbitMQ │         │
│   │  (插件)   │   │  (插件)   │   │  (Docker)  │         │
│   └───────────┘   └───────────┘   └───────────┘         │
└─────────────────────────────────────────────────────────────┘
```

---

## 前期准备

### 1. 账号与工具

- [ ] Railway账号（支持GitHub登录）
- [ ] GitHub账号
- [ ] Git客户端
- [ ] Docker Desktop（本地测试用）

### 2. 创建Railway项目

1. 登录 [Railway](https://railway.app)
2. 点击 "New Project"
3. 选择 "Deploy from GitHub repo"
4. 授权GitHub并选择 `jeepay` 仓库
5. 项目创建完成

### 3. 准备外部服务

#### 方案A：使用Railway插件（推荐）

在Railway项目中添加插件：

```bash
# 通过Railway CLI添加
railway add mysql
railway add redis

# 或通过Dashboard添加
# Project Settings → Add Plugin → MySQL
# Project Settings → Add Plugin → Redis
```

#### 方案B：使用云服务

**阿里云RocketMQ**：
1. 开通阿里云RocketMQ服务
2. 创建Topic和Consumer Group
3. 获取SDK接入点

---

## 部署步骤

### 第一步：Fork项目

```bash
# 在GitHub上fork jeepay仓库
# https://github.com/jeequan/jeepay/fork
```

### 第二步：连接Railway

1. 登录Railway
2. 创建新项目
3. 选择 "Deploy from GitHub repo"
4. 选择fork后的仓库

### 第三步：配置环境变量

在Railway Dashboard中配置以下变量：

#### 数据库配置

| 变量名 | 说明 | 示例值 |
|--------|------|-------|
| `DB_HOST` | MySQL主机 | 从Railway插件获取 |
| `DB_PORT` | MySQL端口 | 3306 |
| `DB_NAME` | 数据库名 | jeepaydb |
| `DB_USERNAME` | 用户名 | root |
| `DB_PASSWORD` | 密码 | ********** |

#### Redis配置

| 变量名 | 说明 | 示例值 |
|--------|------|-------|
| `REDIS_HOST` | Redis主机 | 从Railway插件获取 |
| `REDIS_PORT` | Redis端口 | 6379 |
| `REDIS_PASSWORD` | 密码（可选） | |

#### RocketMQ配置（云服务方案）

| 变量名 | 说明 | 示例值 |
|--------|------|-------|
| `ROCKETMQ_NAMESRV_ADDR` | RocketMQ地址 | rmq-cn-xxx.aliyuncs.com:8080 |
| `ROCKETMQ_PRODUCER_GROUP` | 生产者组 | PID_JEEPAY |

#### 系统配置

| 变量名 | 说明 | 示例值 |
|--------|------|-------|
| `APP_ROOT_PATH` | 应用根目录 | /jeepayhomes |
| `SITE_URL` | 站点URL | https://jeepay.xxx.railway.app |

### 第四步：创建应用配置

在项目中创建 `conf/railway/` 目录和配置文件：

#### jeepay-payment/railway.yml

```yaml
spring:
  application:
    name: jeepay-payment
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD:}
      database: 3

server:
  port: 9216

rocketmq:
  name-server: ${ROCKETMQ_NAMESRV_ADDR}
  producer:
    group: ${ROCKETMQ_PRODUCER_GROUP}
    send-message-timeout: 10000
    retry-times-when-send-failed: 2

isys:
  cache-config: true
  oss:
    file-root-path: /jeepayhomes/uploads
    service-type: local
  mq:
    vender: rocketMQ

logging:
  level:
    root: info
  file:
    path: /jeepayhomes/logs
```

### 第五步：修改Dockerfile

创建专门的Railway Dockerfile：

#### jeepay-payment/Dockerfile.railway

```dockerfile
FROM eclipse-temurin:17-jre

LABEL maintainer="Jeepay"

ENV LANG=C.UTF-8
ENV TZ=Asia/Shanghai

WORKDIR /jeepayhomes/service/app

RUN mkdir -p /jeepayhomes/uploads /jeepayhomes/logs

COPY target/jeepay-payment.jar /jeepayhomes/service/app/jeepay-payment.jar
COPY conf/railway/payment-application.yml /jeepayhomes/service/app/application.yml

EXPOSE 9216

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'echo > /dev/tcp/localhost/9216'

CMD ["java", "-jar", "jeepay-payment.jar"]
```

#### jeepay-manager/Dockerfile.railway

```dockerfile
FROM eclipse-temurin:17-jre

LABEL maintainer="Jeepay"

ENV LANG=C.UTF-8
ENV TZ=Asia/Shanghai

WORKDIR /jeepayhomes/service/app

RUN mkdir -p /jeepayhomes/uploads /jeepayhomes/logs

COPY target/jeepay-manager.jar /jeepayhomes/service/app/jeepay-manager.jar
COPY conf/railway/manager-application.yml /jeepayhomes/service/app/application.yml

EXPOSE 9217

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'echo > /dev/tcp/localhost/9217'

CMD ["java", "-jar", "jeepay-manager.jar"]
```

#### jeepay-merchant/Dockerfile.railway

```dockerfile
FROM eclipse-temurin:17-jre

LABEL maintainer="Jeepay"

ENV LANG=C.UTF-8
ENV TZ=Asia/Shanghai

WORKDIR /jeepayhomes/service/app

RUN mkdir -p /jeepayhomes/uploads /jeepayhomes/logs

COPY target/jeepay-merchant.jar /jeepayhomes/service/app/jeepay-merchant.jar
COPY conf/railway/merchant-application.yml /jeepayhomes/service/app/application.yml

EXPOSE 9218

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'echo > /dev/tcp/localhost/9218'

CMD ["java", "-jar", "jeepay-merchant.jar"]
```

### 第六步：配置构建设置

在Railway Dashboard中为每个服务配置：

#### Payment服务

```yaml
# Build Command
cd jeepay-payment && mvn clean package -DskipTests

# Start Command
java -jar jeepay-payment/target/jeepay-payment.jar

# Dockerfile Path
jeepay-payment/Dockerfile.railway
```

#### Manager服务

```yaml
# Build Command
cd jeepay-manager && mvn clean package -DskipTests

# Start Command
java -jar jeepay-manager/target/jeepay-manager.jar

# Dockerfile Path
jeepay-manager/Dockerfile.railway
```

#### Merchant服务

```yaml
# Build Command
cd jeepay-merchant && mvn clean package -DskipTests

# Start Command
java -jar jeepay-merchant/target/jeepay-merchant.jar

# Dockerfile Path
jeepay-merchant/Dockerfile.railway
```

### 第七步：部署触发

1. Railway自动检测代码变更
2. 或者手动点击 "Deploy" 按钮
3. 查看部署日志确保成功

---

## 配置详解

### 环境变量配置示例

#### 本地开发配置

```bash
# .env.local
DB_HOST=localhost
DB_PORT=3306
DB_NAME=jeepaydb
DB_USERNAME=root
DB_PASSWORD=root123

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

ROCKETMQ_NAMESRV_ADDR=localhost:9876
ROCKETMQ_PRODUCER_GROUP=PID_JEEPAY

APP_ROOT_PATH=/jeepayhomes
SITE_URL=http://localhost:8080
```

#### 生产环境配置

```bash
# .env.production
# MySQL - Railway插件自动注入
MYSQL_HOST={{MySQL.HOSTNAME}}
MYSQL_PORT={{MySQL.PORT}}
MYSQL_DATABASE={{MySQL.DATABASE}}
MYSQL_USERNAME={{MySQL.USERNAME}}
MYSQL_PASSWORD={{MySQL.PASSWORD}}

# Redis - Railway插件自动注入
REDIS_HOST={{Redis.HOSTNAME}}
REDIS_PORT={{Redis.PORT}}
REDIS_PASSWORD={{Redis.PASSWORD}}

# RocketMQ - 阿里云
ROCKETMQ_NAMESRV_ADDR=rmq-cn-xxxx.aliyuncs.com:8080
ROCKETMQ_PRODUCER_GROUP=GID_JEEPAY

APP_ROOT_PATH=/jeepayhomes
SITE_URL=https://jeepay.yourdomain.com
```

### 多环境配置

使用 `application-{profile}.yml` 支持多环境：

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true
```

---

## 数据库初始化

### 1. 获取数据库连接

从Railway Dashboard获取MySQL连接信息：

```bash
# 连接信息
Host: mysql.railway.internal
Port: 3306
Database: railway
Username: root
Password: (在Variables中查看)
```

### 2. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS jeepaydb 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
```

### 3. 导入初始化SQL

```bash
# 方法一：使用MySQL命令行
mysql -h $DB_HOST -P $DB_PORT -u $DB_USERNAME -p$DB_PASSWORD jeepaydb < docs/sql/init.sql
mysql -h $DB_HOST -P $DB_PORT -u $DB_USERNAME -p$DB_PASSWORD jeepaydb < docs/sql/patch.sql

# 方法二：使用Railway CLI
railway run mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD jeepaydb < docs/sql/init.sql
```

### 4. 验证初始化

```sql
-- 检查表是否创建成功
USE jeepaydb;
SHOW TABLES;

-- 检查初始用户
SELECT * FROM t_sys_user;
SELECT * FROM t_sys_user_auth;
```

---

## 前端部署

### 方案一：静态托管（推荐）

Jeepay前端项目 `jeepay-ui` 需要单独部署：

1. Fork `jeepay-ui` 仓库
2. 在Railway中创建静态站点项目
3. 配置构建命令：
   ```bash
   npm install
   npm run build
   ```
4. 设置环境变量：
   ```bash
   VITE_API_BASE_URL=https://jeepay-payment.yourdomain.com
   ```

### 方案二：Nginx容器部署

创建统一的Nginx容器处理前后端：

```dockerfile
# nginx.Dockerfile
FROM nginx:alpine

# 复制构建好的前端文件
COPY dist/ /usr/share/nginx/html/

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
```

### 前端nginx配置

```nginx
server {
    listen 80;
    server_name jeepay.yourdomain.com;
    
    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
        index index.html;
    }
    
    # 运营平台API代理
    location /api/mgr/ {
        proxy_pass http://jeepay-manager:9217/api/mgr/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # 商户平台API代理
    location /api/mch/ {
        proxy_pass http://jeepay-merchant:9218/api/mch/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## HTTPS配置

### Railway自动HTTPS

Railway为所有自定义域名自动提供Let's Encrypt证书：

1. 在Railway Dashboard中添加域名：
   - `jeepay-admin.yourdomain.com` → Manager服务
   - `jeepay-mch.yourdomain.com` → Merchant服务
   - `jeepay-pay.yourdomain.com` → Payment服务

2. 添加CNAME记录到Railway

3. Railway自动申请SSL证书

### 支付渠道回调配置

第三方支付平台需要配置公网HTTPS回调地址：

| 渠道 | 回调URL |
|------|--------|
| 微信支付 | `https://jeepay-pay.yourdomain.com/api/pay/notify/wxpay` |
| 支付宝 | `https://jeepay-pay.yourdomain.com/api/pay/notify/alipay` |
| 云闪付 | `https://jeepay-pay.yourdomain.com/api/pay/notify/ysfpay` |

---

## 运维监控

### 日志查看

```bash
# 使用Railway CLI
railway logs -f jeepay-payment
railway logs -f jeepay-manager
railway logs -f jeepay-merchant

# 指定时间范围
railway logs --since=1h jeepay-payment

# 查看错误日志
railway logs jeepay-payment | grep ERROR
```

### 健康检查

Railway自动进行健康检查，配置：

```yaml
# 健康检查配置
healthCheck:
  path: /
  port: 9216
  interval: 30s
  timeout: 5s
  retries: 3
```

### 性能监控

Railway提供基础监控，可集成：

- **New Relic** - 应用性能监控
- **Datadog** - 云监控
- **Prometheus** - 指标收集

### 自动扩缩容

```yaml
# railway.toml
[deployment]
  autoScale = true
  minReplicas = 1
  maxReplicas = 3
  cpuThreshold = 70
  memoryThreshold = 80
```

---

## 故障排查

### 常见问题

#### 1. 服务启动失败

**症状**：部署状态显示 "Failed"

**排查步骤**：
```bash
# 查看详细日志
railway logs jeepay-payment --verbose

# 检查环境变量
railway variables

# 检查端口占用
railway status
```

**常见原因**：
- 环境变量未配置
- 数据库连接失败
- 端口被占用
- 内存不足

#### 2. 数据库连接失败

**排查命令**：
```bash
# 测试数据库连接
railway run mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p -e "SELECT 1"

# 检查连接数
railway run mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p -e "SHOW PROCESSLIST"
```

**解决方案**：
- 确认MySQL插件已启动
- 检查防火墙设置
- 验证用户名密码正确

#### 3. Redis连接失败

**排查命令**：
```bash
# 测试Redis连接
railway run redis-cli -h $REDIS_HOST -p $REDIS_PORT ping
```

#### 4. RocketMQ消息不消费

**排查步骤**：
1. 确认RocketMQ服务可用
2. 检查Topic和Consumer Group配置
3. 查看消费日志

**替代方案**：使用RabbitMQ

```yaml
# 配置切换到RabbitMQ
isys:
  mq:
    vender: rabbitMQ

spring:
  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: 5672
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
```

### 资源优化

#### 内存问题

```bash
# 调整JVM堆内存
java -Xms512m -Xmx1024m -jar jeepay-payment.jar
```

#### 数据库连接池

```yaml
spring:
  datasource:
    druid:
      max-active: 20
      initial-size: 5
      min-idle: 5
```

---

## 成本优化

### Railway定价

| 计划 | 价格 | 资源 |
|------|------|------|
| Starter | 免费 | 有限资源 |
| Pro | 按量计费 | CPU/RAM/Disk |
| Team | 按量计费 | 团队协作 |

### 优化建议

1. **开发环境**：使用Starter计划
2. **生产环境**：选择Pro计划，按需扩展
3. **使用Nixpacks**：减少镜像构建时间
4. **合理配置健康检查**：避免无效容器占用资源
5. **及时清理日志**：避免存储费用增长

### 成本估算（参考）

| 服务 | 内存 | 预估成本/月 |
|------|------|-----------|
| Payment | 1GB | $5-10 |
| Manager | 512MB | $3-5 |
| Merchant | 512MB | $3-5 |
| MySQL | 1GB | $5-10 |
| Redis | 256MB | $2-3 |
| **总计** | **~3.5GB** | **$20-35** |

---

## 快速检查清单

### 部署前检查

- [ ] Railway账号已创建
- [ ] GitHub仓库已Fork
- [ ] MySQL插件已添加
- [ ] Redis插件已添加（如需要）
- [ ] 外部RocketMQ已配置（如不使用RabbitMQ）
- [ ] 环境变量已配置
- [ ] 数据库初始化SQL已导入

### 部署后检查

- [ ] 所有服务状态为 "Running"
- [ ] 健康检查通过
- [ ] 数据库连接正常
- [ ] Redis连接正常
- [ ] MQ消息正常消费
- [ ] 前端可正常访问
- [ ] 支付接口可正常调用

### 生产环境检查

- [ ] HTTPS已配置
- [ ] 域名已解析
- [ ] 支付渠道回调已配置
- [ ] 日志监控已设置
- [ ] 备份策略已配置
- [ ] 告警机制已设置

---

*本文档详细说明了Jeepay在Railway平台上的部署方案。如有问题，请参考官方文档或提交Issue。*
