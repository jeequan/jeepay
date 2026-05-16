# Jeepay Railway 完整部署指南

## 目录

- [概述](#概述)
- [准备工作](#准备工作)
- [部署步骤](#部署步骤)
- [配置文件说明](#配置文件说明)
- [一键部署](#一键部署)
- [验证部署](#验证部署)
- [故障排查](#故障排查)

---

## 概述

本指南将帮助您将完整的 Jeepay 支付系统部署到 Railway 平台。

### 部署的服务

| 服务 | 说明 | 端口 | 内存需求 |
|------|------|------|----------|
| MySQL | 数据库 | 3306 | 1GB |
| Redis | 缓存 | 6379 | 512MB |
| RabbitMQ | 消息队列 | 5672/15672 | 1GB |
| jeepay-payment | 支付网关 | 9216 | 1GB |
| jeepay-manager | 运营平台 | 9217 | 512MB |
| jeepay-merchant | 商户平台 | 9218 | 512MB |

### 技术选型

- **消息队列**：使用 RabbitMQ 替代 RocketMQ（Railway 原生支持）
- **数据库**：MySQL 8.0
- **缓存**：Redis 6.2

---

## 准备工作

### 1. 安装必要的工具

```bash
# 安装 Railway CLI
npm install -g @railway/cli

# 安装 Docker
# Ubuntu: sudo apt-get install docker.io
# macOS: brew install docker
# Windows: 下载 Docker Desktop

# 安装 Docker Compose
# 通常 Docker Desktop 已包含
```

### 2. 登录 Railway

```bash
# 登录 Railway
railway login

# 验证登录
railway whoami
```

### 3. Fork 项目（可选）

如果您需要修改代码，建议 Fork 项目：

```bash
# Fork jeepay 后端
https://github.com/jeequan/jeepay/fork

# Fork jeepay-ui 前端
https://github.com/jeequan/jeepay-ui/fork
```

---

## 部署步骤

### 方法一：使用 Railway CLI 部署（推荐）

#### 第一步：创建项目

```bash
# 登录后，创建新项目
railway init

# 输入项目名称：jeepay
? Project Name: jeepay

# 选择部署方式：Empty Project
? Select a template: Empty Project
```

#### 第二步：部署基础设施服务

##### 1. 部署 MySQL

```bash
# 使用 Railway MySQL 插件
railway add mysql

# 或者手动创建
railway run --service=mysql --image=mysql:8.0

# 设置环境变量
railway variables set MYSQL_ROOT_PASSWORD "root123456"
railway variables set MYSQL_DATABASE "jeepaydb"
railway variables set MYSQL_USER "jeepay"
railway variables set MYSQL_PASSWORD "jeepay123"
```

##### 2. 部署 Redis

```bash
# 使用 Railway Redis 插件
railway add redis

# 或者手动创建
railway run --service=redis --image=redis:6.2-alpine
```

##### 3. 部署 RabbitMQ

```bash
# 创建 RabbitMQ 服务
railway run --service=rabbitmq --image=rabbitmq:3-management-alpine

# 设置环境变量
railway variables set RABBITMQ_DEFAULT_USER "admin"
railway variables set RABBITMQ_DEFAULT_PASS "admin123"
railway variables set RABBITMQ_DEFAULT_VHOST "/jeepay"

# 暴露管理界面端口
railway domains set --service=rabbitmq --port=15672
```

#### 第三步：初始化数据库

等待 MySQL 启动完成后，导入初始化 SQL：

```bash
# 进入 MySQL 容器
railway shell mysql

# 执行 SQL
mysql -u root -p
CREATE DATABASE IF NOT EXISTS jeepaydb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jeepaydb;
source /docker-entrypoint-initdb.d/01-init.sql;
source /docker-entrypoint-initdb.d/02-patch.sql;
EXIT;
```

#### 第四步：编译后端项目

```bash
# 在本地编译（需要 JDK 17 和 Maven）
cd jeepay
mvn clean package -DskipTests

# 或者使用 Railway 构建
railway build
```

#### 第五步：部署 Jeepay Payment

```bash
# 进入 payment 目录
cd jeepay-payment

# 部署
railway up --service=payment

# 配置环境变量
railway variables set SPRING_DATASOURCE_URL "jdbc:mysql://mysql:3306/jeepaydb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
railway variables set SPRING_DATASOURCE_USERNAME "root"
railway variables set SPRING_DATASOURCE_PASSWORD "root123456"
railway variables set SPRING_DATA_REDIS_HOST "redis"
railway variables set SPRING_DATA_REDIS_PORT "6379"
railway variables set SPRING_RABBITMQ_HOST "rabbitmq"
railway variables set SPRING_RABBITMQ_PORT "5672"
railway variables set SPRING_RABBITMQ_USERNAME "admin"
railway variables set SPRING_RABBITMQ_PASSWORD "admin123"
railway variables set ISYS_MQ_VENDER "rabbitMQ"

# 暴露端口
railway domains set --service=payment --port=9216

# 返回上级目录
cd ..
```

#### 第六步：部署 Jeepay Manager

```bash
cd jeepay-manager
railway up --service=manager

railway variables set SPRING_DATASOURCE_URL "jdbc:mysql://mysql:3306/jeepaydb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
railway variables set SPRING_DATASOURCE_USERNAME "root"
railway variables set SPRING_DATASOURCE_PASSWORD "root123456"
railway variables set SPRING_DATA_REDIS_HOST "redis"
railway variables set SPRING_DATA_REDIS_PORT "6379"
railway variables set SPRING_RABBITMQ_HOST "rabbitmq"
railway variables set SPRING_RABBITMQ_PORT "5672"
railway variables set SPRING_RABBITMQ_USERNAME "admin"
railway variables set SPRING_RABBITMQ_PASSWORD "admin123"
railway variables set ISYS_MQ_VENDER "rabbitMQ"
railway variables set ISYS_MGR_SITE_URL "https://jeepay-manager.up.railway.app"
railway variables set ISYS_MCH_SITE_URL "https://jeepay-merchant.up.railway.app"
railway variables set ISYS_PAY_SITE_URL "https://jeepay-payment.up.railway.app"

railway domains set --service=manager --port=9217
cd ..
```

#### 第七步：部署 Jeepay Merchant

```bash
cd jeepay-merchant
railway up --service=merchant

railway variables set SPRING_DATASOURCE_URL "jdbc:mysql://mysql:3306/jeepaydb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
railway variables set SPRING_DATASOURCE_USERNAME "root"
railway variables set SPRING_DATASOURCE_PASSWORD "root123456"
railway variables set SPRING_DATA_REDIS_HOST "redis"
railway variables set SPRING_DATA_REDIS_PORT "6379"
railway variables set SPRING_RABBITMQ_HOST "rabbitmq"
railway variables set SPRING_RABBITMQ_PORT "5672"
railway variables set SPRING_RABBITMQ_USERNAME "admin"
railway variables set SPRING_RABBITMQ_PASSWORD "admin123"
railway variables set ISYS_MQ_VENDER "rabbitMQ"

railway domains set --service=merchant --port=9218
cd ..
```

---

### 方法二：使用 Docker Compose 部署（最简单）

#### 第一步：修改 docker-compose.railway.yml

我已经为您准备好了完整的配置文件 [docker-compose.railway.yml](file:///workspace/docker-compose.railway.yml)

#### 第二步：部署

```bash
# 使用 Railway CLI 部署整个项目
railway login
railway init
railway up

# Railway 会自动识别 docker-compose.railway.yml 并部署所有服务
```

---

## 配置文件说明

### 已创建的文件

| 文件路径 | 说明 |
|----------|------|
| [railway.toml](file:///workspace/railway.toml) | Railway 项目配置 |
| [docker-compose.railway.yml](file:///workspace/docker-compose.railway.yml) | 完整的 Docker Compose 配置 |
| [jeepay-payment/Dockerfile.railway](file:///workspace/jeepay-payment/Dockerfile.railway) | Payment 服务 Dockerfile |
| [jeepay-manager/Dockerfile.railway](file:///workspace/jeepay-manager/Dockerfile.railway) | Manager 服务 Dockerfile |
| [jeepay-merchant/Dockerfile.railway](file:///workspace/jeepay-merchant/Dockerfile.railway) | Merchant 服务 Dockerfile |
| [conf/payment/application.railway.yml](file:///workspace/conf/payment/application.railway.yml) | Payment 配置 |
| [conf/manager/application.railway.yml](file:///workspace/conf/manager/application.railway.yml) | Manager 配置 |
| [conf/merchant/application.railway.yml](file:///workspace/conf/merchant/application.railway.yml) | Merchant 配置 |

### 环境变量说明

#### MySQL 配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `MYSQL_ROOT_PASSWORD` | root123456 | MySQL root 密码 |
| `MYSQL_DATABASE` | jeepaydb | 数据库名 |
| `MYSQL_USER` | jeepay | 数据库用户 |
| `MYSQL_PASSWORD` | jeepay123 | 数据库密码 |

#### RabbitMQ 配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `RABBITMQ_DEFAULT_USER` | admin | RabbitMQ 用户名 |
| `RABBITMQ_DEFAULT_PASS` | admin123 | RabbitMQ 密码 |
| `RABBITMQ_DEFAULT_VHOST` | /jeepay | 虚拟主机 |

#### Jeepay 配置

| 变量名 | 说明 |
|--------|------|
| `SPRING_DATASOURCE_URL` | MySQL 连接 URL |
| `SPRING_DATASOURCE_USERNAME` | MySQL 用户名 |
| `SPRING_DATASOURCE_PASSWORD` | MySQL 密码 |
| `SPRING_DATA_REDIS_HOST` | Redis 主机 |
| `SPRING_DATA_REDIS_PORT` | Redis 端口 |
| `SPRING_RABBITMQ_HOST` | RabbitMQ 主机 |
| `SPRING_RABBITMQ_PORT` | RabbitMQ 端口 |
| `SPRING_RABBITMQ_USERNAME` | RabbitMQ 用户名 |
| `SPRING_RABBITMQ_PASSWORD` | RabbitMQ 密码 |
| `ISYS_MQ_VENDER` | MQ 厂商 (rabbitMQ) |

---

## 一键部署

### 使用部署脚本

```bash
# 设置执行权限
chmod +x deploy-to-railway.sh

# 运行脚本
./deploy-to-railway.sh
```

脚本会自动完成：
1. 检查依赖工具
2. 登录 Railway
3. 创建/选择项目
4. 配置环境变量
5. 部署所有服务
6. 显示部署结果

---

## 验证部署

### 检查服务状态

```bash
# 查看所有服务
railway status

# 查看日志
railway logs -f payment
railway logs -f manager
railway logs -f merchant
```

### 访问服务

部署成功后，Railway 会自动分配域名：

| 服务 | 访问地址 |
|------|----------|
| 运营平台 | https://jeepay-manager.up.railway.app |
| 商户平台 | https://jeepay-merchant.up.railway.app |
| 支付网关 | https://jeepay-payment.up.railway.app |
| RabbitMQ | https://jeepay-rabbitmq.up.railway.app:15672 |

### 默认账号

| 平台 | 地址 | 账号 | 密码 |
|------|------|------|------|
| 运营平台 | https://jeepay-manager.up.railway.app | jeepay | jeepay123 |
| 商户平台 | https://jeepay-merchant.up.railway.app | 需在运营平台创建 | jeepay666 |
| RabbitMQ | https://jeepay-rabbitmq.up.railway.app:15672 | admin | admin123 |

---

## 故障排查

### 常见问题

#### 1. 服务启动失败

```bash
# 查看详细日志
railway logs --verbose

# 检查环境变量
railway variables

# 重启服务
railway restart
```

#### 2. 数据库连接失败

```bash
# 检查 MySQL 服务
railway status mysql

# 测试连接
railway shell
mysql -h mysql -u root -p
```

#### 3. RabbitMQ 连接失败

```bash
# 检查 RabbitMQ 服务
railway status rabbitmq

# 访问 Management UI
# https://jeepay-rabbitmq.up.railway.app:15672
```

#### 4. 内存不足

```bash
# 升级服务计划
railway plan upgrade

# 或者调整 JVM 内存
railway variables set JAVA_OPTS "-Xms256m -Xmx512m"
```

### 资源监控

```bash
# 查看资源使用
railway metrics

# 查看账单
railway billing
```

---

## 成本估算

### Railway 定价

| 资源 | 用量 | 成本 |
|------|------|------|
| MySQL | 1GB RAM | ~$5/月 |
| Redis | 512MB RAM | ~$3/月 |
| RabbitMQ | 1GB RAM | ~$5/月 |
| Payment | 1GB RAM | ~$5/月 |
| Manager | 512MB RAM | ~$3/月 |
| Merchant | 512MB RAM | ~$3/月 |
| **总计** | ~4GB RAM | **~$24/月** |

### 优化建议

1. 使用 Starter 计划进行开发和测试（免费）
2. 生产环境选择 Pro 计划
3. 合理设置健康检查，避免无效容器占用资源
4. 使用按量付费，避免资源浪费

---

## 快速检查清单

### 部署前

- [ ] Railway CLI 已安装
- [ ] Docker 已安装
- [ ] 已登录 Railway
- [ ] 代码已准备

### 部署中

- [ ] MySQL 已启动
- [ ] Redis 已启动
- [ ] RabbitMQ 已启动
- [ ] 数据库已初始化
- [ ] Payment 服务已部署
- [ ] Manager 服务已部署
- [ ] Merchant 服务已部署

### 部署后

- [ ] 所有服务状态正常
- [ ] 健康检查通过
- [ ] 可以访问管理后台
- [ ] 可以访问商户后台
- [ ] 支付接口可调用
- [ ] RabbitMQ Management UI 可访问

---

**文档版本**: v1.0
**最后更新**: 2026-05-16
