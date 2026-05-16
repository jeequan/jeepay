# Jeepay 计全支付系统 - Code Wiki

## 目录

- [项目概述](#项目概述)
- [技术架构](#技术架构)
- [项目结构](#项目结构)
- [模块详解](#模块详解)
- [核心功能](#核心功能)
- [数据库设计](#数据库设计)
- [API接口](#api接口)
- [部署指南](#部署指南)
- [Railway部署方案](#railway部署方案)
- [运维指南](#运维指南)
- [二次开发](#二次开发)

---

## 项目概述

### 项目简介

Jeepay（计全支付）是一套面向互联网企业的**开源支付系统**，支持多种支付场景和接入模式。

### 核心特性

| 特性 | 说明 |
|------|------|
| **商户模式** | 普通商户模式、服务商模式 |
| **聚合支付** | 支持微信、支付宝、云闪付等多种支付渠道 |
| **多应用接入** | 支持多商户多应用管理 |
| **分账能力** | 支持平台型业务的自动/手动分账 |
| **开源免费** | 基于LGPL-3.0开源协议 |

### 应用场景

- 自建聚合支付平台
- SaaS 支付中台
- 多商户支付系统
- 电商/零售/本地生活/数字内容等业务支付接入

---

## 技术架构

### 技术栈

| 软件名称 | 描述 | 版本 |
|---------|------|------|
| JDK | Java运行环境 | 17 |
| Spring Boot | 后端开发框架 | 3.3.7 |
| MySQL | 数据库 | 5.7.x / 8.0+ |
| Redis | 分布式缓存 | 3.2.8+ |
| RocketMQ | 消息中间件 | 5.3.1 |
| Ant Design Vue | 前端UI框架 | 4.2.6 |
| MyBatis-Plus | ORM框架 | 3.5.7 |

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         前端层                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ 运营平台  │  │ 商户平台  │  │ 收银台   │  │  API    │    │
│  │  (Vue)   │  │  (Vue)   │  │  (Vue)   │  │ (SDK)   │    │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘    │
└───────┼─────────────┼─────────────┼─────────────┼──────────┘
        │             │             │             │
┌───────┴─────────────┴─────────────┴─────────────┴──────────┐
│                       网关层                                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │ Payment │  │ Manager  │  │ Merchant │                  │
│  │ :9216   │  │ :9217    │  │ :9218    │                  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘                  │
└───────┼─────────────┼─────────────┼─────────────────────────┘
        │             │             │
┌───────┴─────────────┴─────────────┴─────────────────────────┐
│                       服务层                                │
│  ┌─────────────────────────────────────────────────┐       │
│  │              Jeepay Service Layer                │       │
│  └─────────────────────────────────────────────────┘       │
│  ┌─────────────────────────────────────────────────┐       │
│  │              Jeepay Core Layer                   │       │
│  └─────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
        │
┌───────┴───────────────────────────────────────────────────┐
│                       数据层                                │
│  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐          │
│  │ MySQL  │  │ Redis  │  │RocketMQ│  │  OSS   │          │
│  └────────┘  └────────┘  └────────┘  └────────┘          │
└───────────────────────────────────────────────────────────┘
```

### 支付流程图

```
┌─────────┐    ┌──────────┐    ┌───────────┐    ┌──────────┐
│  商户   │───▶│ Jeepay   │───▶│ 第三方    │───▶│  支付   │
│  系统   │    │ Payment  │    │ 支付渠道  │    │  成功   │
└─────────┘    └────┬─────┘    └───────────┘    └────┬─────┘
                   │                                 │
                   │    ┌───────────┐                │
                   └───▶│ RocketMQ  │◀───────────────┘
                        └───────────┘
                              │
                              ▼
                        ┌───────────┐
                        │ 异步通知  │
                        │ 商户系统  │
                        └───────────┘
```

---

## 项目结构

### 目录结构

```
jeepay/
├── conf/                     # 配置文件目录
│   └── devCommons/          # 通用配置
│       └── config/
│           └── application.yml
├── docker/                   # Docker相关文件
│   ├── activemq/
│   ├── rabbitmq/
│   ├── rocketmq/
│   └── nginx.sh
├── docs/                     # 文档目录
│   ├── deploy/              # 部署文档
│   ├── install/              # 安装脚本
│   ├── script/               # 启动脚本
│   └── sql/                  # 数据库脚本
├── jeepay-components/        # 组件模块
│   ├── jeepay-components-mq/ # MQ组件
│   └── jeepay-components-oss/ # OSS组件
├── jeepay-core/              # 核心模块
├── jeepay-service/           # 业务服务层
├── jeepay-manager/           # 运营平台 (9217)
├── jeepay-merchant/          # 商户平台 (9218)
├── jeepay-payment/           # 支付网关 (9216)
├── jeepay-z-codegen/         # 代码生成器
└── docker-compose.yml        # Docker编排文件
```

### 模块说明

| 模块 | 说明 | 端口 | 技术栈 |
|------|------|------|--------|
| `jeepay-payment` | 支付网关，统一处理支付请求 | 9216 | Spring Boot |
| `jeepay-manager` | 运营平台后台管理系统 | 9217 | Spring Boot |
| `jeepay-merchant` | 商户系统管理端 | 9218 | Spring Boot |
| `jeepay-core` | 核心基础模块，包含工具类和通用组件 | - | - |
| `jeepay-service` | 业务服务层，数据库操作和业务逻辑 | - | - |
| `jeepay-components` | 公共组件（MQ消息队列、OSS存储） | - | - |
| `jeepay-z-codegen` | MyBatis代码生成器 | - | - |

---

## 模块详解

### 1. jeepay-payment（支付网关）

**职责**：统一处理所有支付请求，对接第三方支付渠道。

**核心类结构**：
```
jeepay-payment/
├── bootstrap/                # 启动类
│   ├── JeepayPayApplication.java
│   └── InitRunner.java       # 初始化任务
├── channel/                  # 支付渠道抽象
│   ├── AbstractChannelNoticeService.java
│   ├── AbstractPaymentService.java
│   └── IChannelNoticeService.java
├── ctrl/                     # 控制器
│   └── ApiController.java    # API入口
├── service/                  # 业务服务
│   ├── PayOrderProcessService.java
│   └── RefundOrderProcessService.java
├── mq/                       # 消息队列
│   └── PayOrderMchNotifyMQReceiver.java
└── task/                     # 定时任务
    └── PayOrderExpiredTask.java
```

**核心服务**：

| 服务类 | 职责 |
|--------|------|
| `PayOrderProcessService` | 支付订单全流程处理 |
| `RefundOrderProcessService` | 退款订单全流程处理 |
| `PayMchNotifyService` | 商户通知发送 |
| `PayOrderDivisionProcessService` | 分账处理 |

### 2. jeepay-manager（运营平台）

**职责**：系统运营管理，包括商户管理、服务商管理、支付配置等。

**核心功能**：
- 商户信息管理
- 服务商信息管理
- 应用管理
- 支付接口配置
- 用户角色权限管理
- 系统配置管理
- 订单查询与退款

### 3. jeepay-merchant（商户平台）

**职责**：商户自助服务，包括应用管理、支付测试、订单查询等。

**核心功能**：
- 商户应用管理
- 支付通道配置
- 支付测试
- 订单管理
- 转账功能
- 分账管理

### 4. jeepay-core（核心模块）

**核心类**：

| 类 | 说明 |
|----|------|
| `ApiRes` / `ApiPageRes` | 统一API响应封装 |
| `BizException` | 业务异常 |
| `JWTUtils` | JWT令牌工具 |
| `RedisUtil` | Redis工具类 |
| `AmountUtil` | 金额计算工具 |
| `SeqKit` | 序列号生成工具 |

**实体类**（Entity）：
- `PayOrder` - 支付订单
- `RefundOrder` - 退款订单
- `TransferOrder` - 转账订单
- `MchInfo` - 商户信息
- `MchApp` - 商户应用
- `IsvInfo` - 服务商信息
- `SysUser` - 系统用户

### 5. jeepay-service（服务层）

提供数据库访问和业务逻辑处理，主要包含：

| 服务 | 说明 |
|------|------|
| `PayOrderService` | 支付订单服务 |
| `RefundOrderService` | 退款订单服务 |
| `MchInfoService` | 商户服务 |
| `MchAppService` | 应用服务 |
| `SysUserService` | 用户服务 |
| `SysConfigService` | 系统配置服务 |

---

## 核心功能

### 支付模式

#### 1. 普通商户模式
```
商户系统 → Jeepay Payment → 微信/支付宝
```

#### 2. 服务商模式（ISV）
```
子商户 → 服务商系统 → Jeepay Payment → 微信/支付宝
```

### 支持的支付方式

| 渠道 | 支付方式代码 | 说明 |
|------|-------------|------|
| **支付宝** | ALI_BAR, ALI_JSAPI, ALI_APP, ALI_WAP, ALI_PC, ALI_QR, ALI_LITE, ALI_OC | 条码、JSAPI、APP、H5、PC网站、二维码、小程序、订单码 |
| **微信支付** | WX_BAR, WX_JSAPI, WX_APP, WX_H5, WX_NATIVE, WX_LITE | 条码、公众号、APP、H5、扫码、小程序 |
| **云闪付** | YSF_BAR, YSF_JSAPI | 条码、JSAPI |
| **银联** | UP_APP, UP_WAP, UP_QR, UP_BAR, UP_B2B, UP_PC, UP_JSAPI | App、手机网站、二维码、条码、企业网银、网关、JS |
| **PayPal** | PP_PC | PC网站支付 |

### 分账功能

支持**自动分账**和**手动分账**两种模式：

- **自动分账**：支付成功后根据配置自动完成分账
- **手动分账**：商户在系统中手动触发分账操作
- **支持100%全额分账**

---

## 数据库设计

### 核心表结构

#### 1. 系统管理表

| 表名 | 说明 |
|------|------|
| `t_sys_user` | 系统用户表 |
| `t_sys_user_auth` | 用户认证表 |
| `t_sys_role` | 角色表 |
| `t_sys_entitlement` | 权限表 |
| `t_sys_role_ent_rela` | 角色权限关联表 |
| `t_sys_user_role_rela` | 用户角色关联表 |
| `t_sys_config` | 系统配置表 |
| `t_sys_log` | 操作日志表 |

#### 2. 商户相关表

| 表名 | 说明 |
|------|------|
| `t_mch_info` | 商户信息表 |
| `t_mch_app` | 商户应用表 |
| `t_isv_info` | 服务商信息表 |

#### 3. 支付配置表

| 表名 | 说明 |
|------|------|
| `t_pay_way` | 支付方式表 |
| `t_pay_interface_define` | 支付接口定义表 |
| `t_pay_interface_config` | 支付接口配置表 |
| `t_mch_pay_passage` | 商户支付通道表 |

#### 4. 订单表

| 表名 | 说明 |
|------|------|
| `t_pay_order` | 支付订单表 |
| `t_refund_order` | 退款订单表 |
| `t_transfer_order` | 转账订单表 |
| `t_mch_notify_record` | 商户通知记录表 |
| `t_order_snapshot` | 订单数据快照表 |

#### 5. 分账表

| 表名 | 说明 |
|------|------|
| `t_mch_division_receiver_group` | 分账账号组表 |
| `t_mch_division_receiver` | 分账接收者表 |
| `t_pay_order_division_record` | 分账记录表 |

### RBAC权限模型

```
用户 (t_sys_user)
    ↓ N:N
角色 (t_sys_role)
    ↓ N:N
权限 (t_sys_entitlement)
```

---

## API接口

### 接口列表

| 接口 | 路径 | 说明 |
|------|------|------|
| 支付下单 | `/api/pay/unifiedOrder` | 统一支付接口 |
| 支付回调 | `/api/pay/notify/{ifCode}` | 支付结果通知 |
| 支付查询 | `/api/pay/query` | 订单查询 |
| 退款 | `/api/refund/unifiedRefund` | 统一退款接口 |
| 退款查询 | `/api/refund/query` | 退款查询 |
| 转账 | `/api/transfer/unifiedTransfer` | 转账接口 |
| 分账 | `/api/division/receiverBind` | 分账账号绑定 |
| 认证 | `/api/anon/auth/login` | 用户登录 |

### 接口特点

- **统一响应格式**：所有接口返回 `ApiRes<T>` 结构
- **签名验证**：使用RSA/MD5签名保证安全
- **幂等性**：通过 `mchOrderNo` 商户订单号保证
- **异步通知**：支付结果通过MQ异步通知商户

---

## 部署指南

### 部署方式

| 方式 | 适用场景 | 文档 |
|------|---------|------|
| **Shell脚本一键安装** | 干净的CentOS/Anolis/Ubuntu/Debian服务器 | [deploy/shell.md](docs/deploy/shell.md) |
| **Docker Compose** | 本地/测试环境 | [deploy/compose.md](docs/deploy/compose.md) |
| **宝塔面板** | 有宝塔面板的环境 | [deploy/baota.md](docs/deploy/baota.md) |
| **源码部署** | 对接内部基础设施 | 自备MySQL/Redis/MQ |

### Docker Compose快速启动

```bash
# 1. 编译后端
mvn clean package -DskipTests

# 2. 启动所有服务
docker compose up -d --build

# 3. 查看服务状态
docker compose ps
```

### 默认端口

| 组件 | 端口 |
|------|------|
| MySQL | 13306 |
| Redis | 6380 |
| RocketMQ Nameserver | 9876 |
| RocketMQ Broker | 10909/10911/10912 |
| Payment | 9216 |
| Manager | 9217 |
| Merchant | 9218 |
| 前端(Manager) | 9227 |
| 前端(Merchant) | 9228 |
| 前端(Cashier) | 9226 |

### 默认账号

| 平台 | 地址 | 账号/密码 |
|------|------|----------|
| 运营平台 | `http://localhost:9227` | `jeepay` / `jeepay123` |
| 商户系统 | `http://localhost:9228` | 需在运营平台创建 |

---

## Railway部署方案

### Railway平台特点

Railway是一个现代化的云部署平台，支持：
- 基于Git的自动部署
- 多环境管理
- 内置PostgreSQL/MySQL数据库
- 内置Redis（通过插件）
- Docker容器支持
- 官方Nixpacks/Buildpacks支持

### 部署挑战分析

Jeepay是一套**重量级的Java微服务系统**，部署到Railway存在以下挑战：

| 挑战项 | 说明 | 解决方案 |
|--------|------|----------|
| **多服务依赖** | 需要MySQL+Redis+RocketMQ+3个Java服务 | 使用Railway插件+外部服务 |
| **RocketMQ** | Railway不原生支持RocketMQ | 使用云MQ服务或替代方案 |
| **内存要求** | Java应用内存需求较高 | 选择适当的服务计划 |
| **存储持久化** | 需要持久化存储日志和上传文件 | 配置Volume挂载 |

### 推荐部署架构

#### 方案一：全云服务架构（推荐生产环境）

```
Railway部署:
├── jeepay-payment (Java微服务)
├── jeepay-manager (Java微服务)
└── jeepay-merchant (Java微服务)

外部云服务:
├── MySQL → Railway MySQL插件 或 云数据库
├── Redis → Railway Redis插件 或 云Redis
└── RocketMQ → 阿里云RocketMQ / 腾讯云CMQ / 自建
```

#### 方案二：开发/测试环境简化架构

```
Railway部署:
├── jeepay-payment
├── jeepay-manager
├── jeepay-merchant
└── jeepay-nginx (前端反向代理)

外部服务:
├── MySQL → Railway MySQL插件
├── Redis → Railway Redis插件
└── RocketMQ → 使用RabbitMQ替代（Railway支持Docker）
```

### 详细部署步骤

#### 第一步：准备Railway项目

1. 登录 [Railway](https://railway.app) 并创建新项目
2. 添加MySQL插件
3. 添加Redis插件（可选）
4. 配置环境变量

#### 第二步：配置环境变量

在Railway项目中配置以下环境变量：

```bash
# 数据库配置
DB_HOST={{MySQL.HOSTNAME}}
DB_PORT={{MySQL.PORT}}
DB_NAME=jeepaydb
DB_USERNAME={{MySQL.USERNAME}}
DB_PASSWORD={{MySQL.PASSWORD}}

# Redis配置
REDIS_HOST={{Redis.HOSTNAME}}
REDIS_PORT={{Redis.PORT}}
REDIS_PASSWORD={{Redis.PASSWORD}}

# RocketMQ配置（使用阿里云或其他云服务）
ROCKETMQ_NAMESRV_ADDR=rmq-cn-xxx.aliyuncs.com:8080
ROCKETMQ_PRODUCER_GROUP=PID_JEEPAY

# 系统配置
APP_ROOT_PATH=/jeepayhomes
SITE_URL=https://your-domain.railway.app
```

#### 第三步：修改数据库连接配置

创建 `conf/payment/application.yml` 等配置文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

rocketmq:
  name-server: ${ROCKETMQ_NAMESRV_ADDR}
  producer:
    group: ${ROCKETMQ_PRODUCER_GROUP}
```

#### 第四步：构建Docker镜像

创建 `Dockerfile.railway` 文件：

```dockerfile
# jeepay-payment/Dockerfile.railway
FROM eclipse-temurin:17-jre

ENV LANG=C.UTF-8
ENV TZ=Asia/Shanghai

WORKDIR /jeepayhomes/service/app

COPY target/jeepay-payment.jar /jeepayhomes/service/app/

EXPOSE 9216

CMD ["java", "-jar", "jeepay-payment.jar"]
```

#### 第五步：部署到Railway

1. **连接GitHub仓库**
   - 在Railway中连接GitHub仓库
   - 选择对应的服务目录

2. **配置构建命令**
   ```bash
   # Build Command
   cd jeepay-payment && mvn clean package -DskipTests
   ```

3. **配置启动命令**
   ```bash
   # Start Command
   java -jar jeepay-payment/target/jeepay-payment.jar
   ```

4. **配置健康检查**
   ```yaml
   healthcheck:
     path: /
     port: 9216
   ```

#### 第六步：配置数据库

1. 在Railway MySQL插件中获取连接信息
2. 导入初始化SQL
   ```bash
   mysql -h ${HOST} -P ${PORT} -u ${USERNAME} -p ${PASSWORD} jeepaydb < init.sql
   ```

#### 第七步：配置域名和SSL

1. 在Railway中为每个服务配置自定义域名
2. Railway自动提供SSL证书
3. 配置Nginx反向代理（可选）

### 使用Docker Compose on Railway

创建 `railway.yml` 配置文件：

```yaml
services:
  payment:
    dockerfile: jeepay-payment/Dockerfile
    build:
      context: .
      dockerfile: jeepay-payment/Dockerfile
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jeepaydb
      - SPRING_REDIS_HOST=redis
      - ROCKETMQ_NAMESRV_ADDR=rocketmq:9876
    depends_on:
      - mysql
      - redis
      - rocketmq

  manager:
    dockerfile: jeepay-manager/Dockerfile
    build:
      context: .
      dockerfile: jeepay-manager/Dockerfile
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jeepaydb
      - SPRING_REDIS_HOST=redis
      - ROCKETMQ_NAMESRV_ADDR=rocketmq:9876
    depends_on:
      - mysql
      - redis
      - rocketmq

  merchant:
    dockerfile: jeepay-merchant/Dockerfile
    build:
      context: .
      dockerfile: jeepay-merchant/Dockerfile
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jeepaydb
      - SPRING_REDIS_HOST=redis
      - ROCKETMQ_NAMESRV_ADDR=rocketmq:9876
    depends_on:
      - mysql
      - redis
      - rocketmq

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=rootpassword
      - MYSQL_DATABASE=jeepaydb
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:6.2

  rocketmq:
    image: apache/rocketmq:5.3.1
    command: sh mqnamesrv
    environment:
      - JAVA_OPT_EXT=-Xms256m -Xmx256m

volumes:
  mysql_data:
```

### Railway部署注意事项

#### 资源规划

| 服务 | 内存建议 | CPU建议 |
|------|---------|--------|
| jeepay-payment | 1GB+ | 1 Core |
| jeepay-manager | 512MB+ | 0.5 Core |
| jeepay-merchant | 512MB+ | 0.5 Core |
| MySQL | 1GB+ | 1 Core |
| Redis | 256MB+ | 0.25 Core |

#### 成本优化建议

1. **使用Starter计划**进行开发和测试
2. **生产环境**选择Production计划
3. 利用Railway的**自动扩缩容**功能
4. 合理使用**数据库连接池**

#### 常见问题

| 问题 | 解决方案 |
|------|---------|
| 内存溢出 | 增加服务计划的内存配额 |
| 启动超时 | 调整健康检查超时时间 |
| 端口冲突 | Railway自动分配外部端口 |
| 日志查看 | 使用 `railway logs -f` 命令 |

---

## 运维指南

### 日志管理

```bash
# 查看支付服务日志
docker logs -f jeepay-payment --tail 100

# 查看运营平台日志
docker logs -f jeepay-manager --tail 100
```

### 常用运维命令

```bash
# 重启服务
docker restart jeepay-payment jeepay-manager jeepay-merchant

# 查看资源使用
docker stats

# 数据库连接
docker exec -it jeepay-mysql mysql -uroot -p'rootroot' jeepaydb

# Redis连接
docker exec -it jeepay-redis redis-cli
```

### 性能优化

1. **JVM参数调优**
   ```bash
   JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
   ```

2. **数据库连接池**
   ```yaml
   spring:
     datasource:
       druid:
         max-active: 50
         initial-size: 10
   ```

3. **Redis缓存优化**
   - 合理设置缓存过期时间
   - 使用Redis集群提高可用性

---

## 二次开发

### 添加新的支付渠道

1. **创建渠道实现类**
   ```java
   @Component
   public class NewPayChannelService extends AbstractPaymentService {
       @Override
       public OriginalRes pay(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
           // 实现支付逻辑
       }
   }
   ```

2. **注册渠道**
   - 在数据库 `t_pay_interface_define` 表添加记录
   - 配置渠道参数模板

3. **实现回调处理**
   ```java
   @Component
   public class NewPayChannelNoticeService extends AbstractChannelNoticeService {
       @Override
       public String notify(HttpServletRequest request, Map<String, Object> params) {
           // 处理回调通知
       }
   }
   ```

### 自定义功能开发

1. **新增业务服务**
   ```java
   @Service
   public class CustomBusinessService {
       @Autowired
       private PayOrderService payOrderService;
       
       // 业务方法
   }
   ```

2. **新增API接口**
   ```java
   @RestController
   @RequestMapping("/api/custom")
   public class CustomApiController {
       @PostMapping("/business")
       public ApiRes<?> customBusiness(@RequestBody ReqDTO request) {
           // 业务逻辑
       }
   }
   ```

### 代码生成器使用

```bash
cd jeepay-z-codegen
# 修改 MainGen.java 配置
mvn clean compile
java -cp target/classes com.gen.MainGen
```

---

## 相关资源

- [官方文档](https://doc.jeequan.com/#/integrate/open)
- [GitHub仓库](https://github.com/jeequan/jeepay)
- [Gitee仓库](https://gitee.com/jeequan/jeepay)
- [前端项目](https://github.com/jeequan/jeepay-ui)
- [Java SDK](https://github.com/jeequan/jeepay-sdk-java)

---

*本文档由Code Wiki自动生成，最后更新于 2026-05-16*
