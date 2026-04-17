<p align="center">
  <a href="https://www.jeequan.com">
    <img src="https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_logo.svg" alt="Jeepay Logo">
  </a>
</p>

<p align="center">
  <strong>计全支付（Jeepay）- 让支付接入更简单</strong>
</p>

<p align="center">
  一套面向互联网企业的开源支付系统，支持普通商户模式、服务商模式、聚合支付与多应用接入。
</p>

<p align="center">
  👉 <a href="https://www.jeequan.com">官网</a> ·
  <a href="https://doc.jeequan.com/#/integrate/open">项目文档</a> ·
  <a href="https://github.com/jeequan/jeepay">GitHub</a> ·
  <a href="https://gitee.com/jeequan/jeepay">Gitee</a> ·
  <a href="https://github.com/jeequan/jeepay-ui">前端项目</a>
</p>

<p align="center">
  <a target="_blank" href="https://spring.io/projects/spring-boot">
    <img src="https://img.shields.io/badge/Spring%20Boot-3.3.7-yellowgreen" />
  </a>
  <a target="_blank" href="https://www.oracle.com/java/technologies/downloads/#java17">
    <img src="https://img.shields.io/badge/JDK-17-green.svg" />
  </a>
  <a target="_blank" href="http://www.gnu.org/licenses/lgpl.html">
    <img src="https://img.shields.io/badge/license-LGPL--3.0-blue" />
  </a>
  <a href="https://gitee.com/jeequan/jeepay/stargazers" target="_blank">
    <img src="https://gitee.com/jeequan/jeepay/badge/star.svg?theme=gvp" alt="gitee star">
  </a>
  <a target="_blank" href="https://github.com/jeequan/jeepay">
    <img src="https://img.shields.io/github/stars/jeequan/jeepay.svg?style=social" alt="github star"/>
  </a>
  <a target="_blank" href="https://gitcode.com/jeequantech/jeepay">
    <img src="https://gitcode.com/jeequantech/jeepay/star/badge.svg" alt="gitcode star"/>
  </a>
</p>

<p align="center">
  <a href="https://jq.qq.com/?_wv=1027&k=94WnXmdL">
    <img src="https://img.shields.io/badge/QQ%E7%BE%A4%E2%91%A0-635647058-critical" alt="QQ群">
  </a>
</p>

---

# 目录

- [项目简介](#项目简介)
- [为什么选择 Jeepay](#为什么选择-jeepay)
- [适用场景](#适用场景)
- [系统能力概览](#系统能力概览)
- [快速开始](#快速开始)
- [部署方式](#部署方式)
- [项目结构](#项目结构)
- [系统架构](#系统架构)
- [核心技术栈](#核心技术栈)
- [文档与资源](#文档与资源)
- [接口市场](#接口市场)
- [在线体验](#在线体验)
- [功能模块](#功能模块)
- [系统截图](#系统截图)
- [版本与兼容性说明](#版本与兼容性说明)
- [贡献与协作](#贡献与协作)
- [更多支持](#更多支持)

---

# 项目简介

Jeepay 是一套面向互联网企业的开源支付系统，支持：

- **普通商户模式**
- **多渠道服务商模式**
- **聚合码支付**
- **多商户、多应用接入**

当前已对接：

- 微信支付
- 支付宝
- 云闪付

项目采用：

- 后端：`Spring Boot`
- 前端：`Ant Design Vue`
- 权限体系：`Spring Security`

Jeepay 适用于支付能力平台化、商户系统建设、支付中台建设以及聚合支付业务的二次开发。

---

# 为什么选择 Jeepay

- **支付能力完整**：覆盖下单、退款、通知、分账扩展、渠道管理等常见支付能力
- **模式灵活**：同时支持普通商户与服务商模式
- **多渠道兼容**：已具备微信、支付宝、云闪付等主流渠道接入能力
- **架构清晰**：后端分层明确，前后端分离，适合持续迭代和二开
- **接入效率高**：标准化 HTTP 接口 + 多语言 SDK，业务系统接入成本低
- **可运维性好**：支持 Docker、脚本部署、分布式场景和 MQ 通知机制
- **支付经验沉淀**：由原 `XxPay` 团队持续开发维护，具备多年实战经验

---

# 适用场景

Jeepay 适合以下业务场景：

- 自建聚合支付平台
- 多商户支付系统
- SaaS 平台支付中台
- 电商、零售、本地生活、数字内容等业务的支付接入
- 服务商模式下的渠道统一管理与商户统一接入
- 需要独立掌控支付流程、商户管理、渠道配置和回调通知的项目

如果你希望快速搭建一套可控、可扩展、可二开的支付系统，Jeepay 是比较合适的基础底座。

---

# 系统能力概览

## 支付渠道能力

- 微信支付：支持服务商 / 普通商户，兼容 `V2` / `V3`
- 支付宝：支持服务商 / 普通商户，兼容 `RSA` / `RSA2`
- 云闪付：支持服务商接口，可扩展多家机构

## 平台能力

- 多商户管理
- 多应用接入
- 聚合码支付
- 订单管理
- 渠道参数配置
- 商户通知与回调
- 支付结果异步通知
- 权限与账号管理
- 运营平台与商户平台双端支持

## 工程能力

- 前后端分离架构
- 支持分布式部署
- 支持高并发场景
- 支付通知支持多种 MQ
- 支持 Docker 部署与脚本化安装
- 支持二次开发与自定义扩展

---

# 快速开始

## 环境要求

| 组件 | 要求 |
|---|---|
| JDK | 17 |
| Maven | 建议 3.8+ |
| MySQL | 5.7.x / 8.0+ |
| Redis | 3.2.8+ |
| MQ | RocketMQ（默认）/ ActiveMQ / RabbitMQ（按需启用） |
| Node.js | 前端工程按 `jeepay-ui` 要求准备 |

## 代码获取

### 服务端

```bash
git clone https://github.com/jeequan/jeepay.git
```

### 前端

```bash
git clone https://github.com/jeequan/jeepay-ui.git
```

## 首次启动建议流程

### 1. 准备数据库与缓存

- 创建 MySQL 数据库
- 导入初始化 SQL
- 准备 Redis
- 如需异步通知增强能力，可按需准备 MQ

初始化 SQL 位于：

```text
docs/sql
```

### 2. 准备配置文件

项目配置文件位于：

```text
conf/manager/application.yml
conf/merchant/application.yml
conf/payment/application.yml
```

建议优先完成以下配置：

- MySQL 连接信息
- Redis 连接信息
- 服务端口与环境参数
- 支付渠道基础配置
- MQ 配置（如启用）

### 3. 编译项目

在项目根目录执行：

```bash
mvn clean package -DskipTests
```

### 4. 启动核心服务

Jeepay 默认包含以下 3 个核心服务：

| 模块 | 说明 | 默认端口 |
|---|---|---|
| `jeepay-payment` | 支付网关 | `9216` |
| `jeepay-manager` | 运营平台服务端 | `9217` |
| `jeepay-merchant` | 商户系统服务端 | `9218` |

### 5. 启动前端项目

前端项目请参考：

- <https://github.com/jeequan/jeepay-ui>

---

# 部署方式

## 方式一：宝塔面板一键安装

- 安装 **宝塔面板 9.2.0 及以上版本**
- 在 Docker 应用中搜索 `jeepay`
- 按页面指引完成一键安装
- 教程：<https://doc.jeequan.com/#/integrate/open/dev/108>

## 方式二：Shell 脚本一键安装

> Shell 一键安装脚本当前默认部署 **RocketMQ**，会自动启动 `rocketmq-namesrv` 与 `rocketmq-broker`；如需改回 ActiveMQ / RabbitMQ，请同步调整脚本与 `conf/*.yml` 配置。

### CentOS

> 推荐系统：Anolis OS 8.8

```bash
yum install -y wget && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && sh install.sh
```

### Ubuntu

> 推荐系统：Ubuntu 22.04 64 位

```bash
apt update && apt-get -y install docker.io && apt-get -y install git && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && sh install.sh
```

### Shell 脚本默认开放端口

| 组件 | 端口 | 说明 |
| --- | --- | --- |
| MySQL | `3306` | 数据库 |
| Redis | `6379` | 缓存 |
| RocketMQ NameServer | `9876` | RocketMQ 注册中心 |
| RocketMQ Broker | `10909` / `10911` / `10912` | RocketMQ Broker |
| 支付网关 | `19216` | payment 服务 |
| 运营平台 | `19217` | manager 服务 |
| 商户平台 | `19218` | merchant 服务 |
| Nginx | `80` | 前端静态资源与反向代理 |

### 卸载

```bash
cd /your/install/path/sources/jeepay/docs/install && sh uninstall.sh
```

> 卸载脚本会同时删除 `rocketmq-namesrv` 与 `rocketmq-broker` 容器。

## 方式三：自助源码部署

适合需要二次开发、自定义部署架构或接入内部基础设施的团队：

- 自行准备 MySQL / Redis / MQ
- 按环境调整 `conf` 目录配置
- 使用 Maven 编译打包
- 分别部署 `payment / manager / merchant` 服务
- 独立部署前端工程 `jeepay-ui`

## 方式四：Docker Compose 部署

适合希望通过容器快速拉起完整开发 / 测试环境的场景。

### 目录要求

默认约定：

```text
jeepay-open/
├── jeepay/
└── jeepay-ui/
```

如果你的前端目录不在 `jeepay` 同级目录，可在 `jeepay/.env` 中覆盖 `UI_BASE_DIR`。
可参考根目录的 `.env.example`。

### 构建前准备

#### 1. 配置 Docker 国内镜像加速（强烈建议）

国内网络拉取 Docker Hub 镜像较慢或被墙，建议配置多个镜像加速源（单个源随时可能失效，Docker 会自动 fallback）。

Docker daemon 配置示例：

```json
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io",
    "https://docker.xuanyuan.me",
    "https://hub.rat.dev",
    "https://docker.1panel.live"
  ]
}
```

配置位置：

- **Linux**：`/etc/docker/daemon.json`，修改后执行 `sudo systemctl restart docker`
- **Docker Desktop（macOS / Windows）**：Settings -> Docker Engine，写入上述 JSON 后点击 Apply & Restart

验证是否生效：

```bash
docker info | grep -A 5 "Registry Mirrors"
```

#### 2. 编译后端 JAR

在 `jeepay` 根目录执行：

```bash
mvn clean package -DskipTests
```

生成的 JAR 位于：

- `jeepay-payment/target/jeepay-payment.jar`
- `jeepay-manager/target/jeepay-manager.jar`
- `jeepay-merchant/target/jeepay-merchant.jar`

#### 3. 准备前端代码

确保 `jeepay-ui` 仓库已拉取到本地，且目录结构满足上面的要求。

### 启动方式

```bash
# 首次启动（编译镜像 + 启动）
docker compose up -d --build

# 后续启动（跳过编译）
docker compose up -d

# 仅重新编译后端服务
docker compose build payment manager merchant

# 重启某个服务
docker compose restart payment
```

### 启动前校验

```bash
docker compose config
```

### 默认暴露端口

| 组件 | 端口 | 说明 |
|---|---|---|
| MySQL | `13306` | 映射宿主机 13306，避免与本地 MySQL 冲突 |
| Redis | `6380` | |
| RocketMQ NameServer | `9876` | |
| RocketMQ Broker | `10909` / `10911` / `10912` | |
| payment | `9216` | 支付网关 |
| manager | `9217` | 运营平台后端 |
| merchant | `9218` | 商户系统后端 |
| manager UI | `9227` | 运营平台前端 |
| merchant UI | `9228` | 商户系统前端 |
| cashier UI | `9226` | 收银台前端 |

### 默认账号

| 平台 | 地址 | 用户名 | 密码 |
|---|---|---|---|
| 运营平台 | http://localhost:9227 | jeepay | jeepay123 |
| 商户系统 | http://localhost:9228 | jeepay | jeepay123 |

### 核心组件版本

| 组件 | 版本 | 说明 |
|---|---|---|
| RocketMQ Server | `5.3.1` | NameServer + Broker |
| rocketmq-spring-boot-starter | `2.3.5` | Spring Boot 集成，内置 client 5.3.2 |
| MySQL | `8` | |
| Redis | `latest` | |
| Java 基础镜像 | `eclipse-temurin:17-jre` | openjdk 官方镜像已停止维护 |

### 配置说明

- Compose 默认使用 **RocketMQ** 作为消息队列，已配置 `rocketmq-namesrv` 与 `rocketmq-broker`。
- `conf/payment`、`conf/manager`、`conf/merchant` 已默认切换为 `rocketMQ`，MySQL / Redis 主机名配置为容器内网名 `mysql`、`redis`。
- MySQL 映射为 `13306:3306`，避免与宿主机本地 MySQL 冲突；容器内部服务仍通过 `mysql:3306` 通信。
- Java 服务的配置文件通过 volume 挂载，修改 `conf/` 目录下的 yml 后重启对应服务即可生效。
- 如需回退到 ActiveMQ 或切换 RabbitMQ，需同时调整 `docker-compose.yml` 与 `conf/*.yml`。

### 启动后验证

```bash
# 查看所有容器状态
docker compose ps

# 查看核心服务日志
docker compose logs --tail=100 payment manager merchant rocketmq-namesrv rocketmq-broker

# 确认 RocketMQ Broker 启动成功（应看到 "boot success"）
docker logs jeepay-rocketmq-broker 2>&1 | grep "boot success"
```

### 常见问题

#### RocketMQ Broker 启动失败（NullPointerException）

如果 Broker 日志中出现 `ScheduleMessageService.configFilePath` 相关 NPE，通常是 Docker named volume 的权限问题。RocketMQ 5.x 镜像以 `rocketmq`（uid=3000）用户运行，而 Docker 创建的 volume 默认 `root` 权限。

Compose 中已通过 `user: "0:0"` 解决此问题。如果手动部署遇到此问题，可执行：

```bash
docker run --rm -u root -v jeepay_rocketmq_broker_store:/data alpine chown -R 3000:3000 /data
docker run --rm -u root -v jeepay_rocketmq_broker_logs:/data alpine chown -R 3000:3000 /data
```

#### Apple Silicon（M1/M2/M3）注意事项

RocketMQ 官方镜像仅提供 `linux/amd64` 版本，在 Apple Silicon 上通过 Rosetta 2 模拟运行，启动较慢属于正常现象。确保 Docker Desktop 已开启 "Use Rosetta for x86_64/amd64 emulation on Apple Silicon"。

#### 前端镜像构建失败

优先检查：
- `jeepay-ui` 目录是否存在且在正确位置
- Node.js 依赖是否可正常安装（npm 源是否可达）

#### 登录提示"认证服务出现异常"

检查 `conf/*/application.yml` 中的数据库连接配置是否与 `docker-compose.yml` 中的 MySQL 密码一致。Compose 默认 root 密码为 `rootroot`。

#### 镜像拉取失败（403 Forbidden）

镜像加速源可能失效，建议配置多个加速源（参见上方"配置 Docker 国内镜像加速"），Docker 会自动尝试下一个。

---

# 项目结构

```text
jeepay
├── conf                     # 系统部署所需 yml 配置
├── docker                   # Docker 相关文件
├── docs                     # 项目文档
│   ├── install              # 安装脚本
│   ├── script               # 启动脚本
│   └── sql                  # 初始化 SQL 文件
├── jeepay-components        # 公共组件目录
│   ├── jeepay-components-mq # MQ 组件
│   └── jeepay-components-oss# OSS 组件
├── jeepay-core              # 核心依赖模块
├── jeepay-manager           # 运营平台服务端（9217）
├── jeepay-merchant          # 商户系统服务端（9218）
├── jeepay-payment           # 支付网关（9216）
├── jeepay-service           # 业务层代码
└── jeepay-z-codegen         # MyBatis 代码生成模块
```

## 仓库关系

| 仓库 | 作用 |
|---|---|
| `jeepay` | 服务端主仓库，包含支付网关、运营平台、商户平台、核心服务 |
| `jeepay-ui` | 前端项目 |
| `jeepay-sdk-java` | Java SDK，供业务系统对接 Jeepay 接口时使用 |

---

# 系统架构

> Jeepay 计全支付系统架构图

![Jeepay系统架构图](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_framework.png "Jeepay系统架构图")

> Jeepay 聚合码支付流程图

![Jeepay计全支付聚合码支付流程图](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_qr.png "Jeepay计全支付聚合码支付流程图")

---

# 核心技术栈

| 软件名称 | 描述 | 版本 |
|---|---|---|
| JDK | Java 运行环境 | 17 |
| Spring Boot | 后端开发框架 | 3.3.7 |
| Redis | 分布式缓存 | 3.2.8 或更高版本 |
| MySQL | 数据库 | 5.7.x / 8.0+ |
| MQ | 消息中间件 | ActiveMQ / RabbitMQ / RocketMQ |
| Ant Design Vue | 前端 UI 框架 | 4.2.6 |
| MyBatis-Plus | MyBatis 增强工具 | 3.4.2 |
| WxJava | 微信开发 Java SDK | 4.6.0 |
| Hutool | Java 工具类库 | 5.8.26 |

---

# 文档与资源

## 官方文档

- 项目文档：<https://doc.jeequan.com/#/integrate/open>
- 快速上手：<https://doc.jeequan.com/#/integrate/open/dev/109>
- 开发指导：<https://doc.jeequan.com/#/integrate/open/dev/103>
- 通道对接：<https://doc.jeequan.com/#/integrate/open/dev/104>
- 线上部署：<https://doc.jeequan.com/#/integrate/open/dev/111>
- 接口文档：<https://doc.jeequan.com/#/integrate/open/api/81>
- 常见问题：<https://doc.jeequan.com/#/integrate/open/dev/107>

## SDK 资源

Jeepay 已提供 Java、Python SDK，以及 PHP 对接 Demo，方便业务系统快速接入。

- SDK 下载地址：<https://doc.jeequan.com/#/integrate/open/api/116>
- Java SDK 仓库：<https://github.com/jeequan/jeepay-sdk-java>

## 项目地址

### 服务端项目

- GitHub：<https://github.com/jeequan/jeepay>
- Gitee：<https://gitee.com/jeequan/jeepay>
- GitCode：<https://gitcode.com/jeequantech/jeepay>

### 前端项目

- GitHub：<https://github.com/jeequan/jeepay-ui>
- Gitee：<https://gitee.com/jeequan/jeepay-ui>
- GitCode：<https://gitcode.com/jeequantech/jeepay-ui>

---

# 接口市场

计全官方团队基于开源版代码，持续开发了多家第三方支付机构、银行以及扩展支付接口的对接代码，帮助用户更快完成支付能力接入。

- 接口市场：<https://www.jeequan.com/ifstore/list.html>
- 插件安装说明：<https://doc.jeequan.com/#/integrate/open/dev/113>

## 已发布接口示例

### 三方支付

汇付 Adapay、斗拱支付、支付宝直付通、微信收付通、银盛支付、银联条码前置、银联支付、联动优势、国通星驿付、丰付支付、盛付通、乐刷、杉德支付、瑞银信、拉卡拉、汇聚支付、新生支付、河马支付、海科融通、富友支付、易生支付、支付宝云支付、通联支付。

### 银行

工行支付、浦发银行、建行龙支付、交行支付。

### 四方支付

付呗支付、米花支付。

---

# 在线体验

## 支付体验

- Jeepay 支付流程体验：<https://www.jeequan.com/demo/jeepay_cashier.html>

## 管理平台体验

- Jeepay 运营平台 / 商户系统演示：<https://www.jeequan.com/doc/detail_84.html>

---

# 功能模块

> Jeepay 运营平台功能

![Jeepay运营平台功能](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_mgr.png "Jeepay运营平台功能")

> Jeepay 商户系统功能

![Jeepay商户系统功能](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_mch.png "Jeepay商户系统功能")

---

# 系统截图

> 以下截图来源于已实现功能界面，截图时间：2021-07-06 08:59

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/001.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/023.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/002.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/005.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/006.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/009.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/010.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/011.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/012.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/013.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/014.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/015.png "Jeepay演示界面")

![Jeepay演示界面](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/yanshi/022.png "Jeepay演示界面")

---

# 版本与兼容性说明

- 当前项目采用 `Spring Boot 3.3.7`
- 当前项目要求 `JDK 17`
- 数据库建议使用 `MySQL 5.7.x` 或 `8.0+`
- Redis 建议使用 `3.2.8+`
- MQ 为可选增强组件，可根据实际业务场景选择启用
- 如需对接前端，请同步使用 `jeepay-ui` 对应版本
- 如需通过 SDK 对接业务系统，请优先使用官方 SDK 或示例代码

---

# 贡献与协作

欢迎通过以下方式参与项目共建：

- 提交 Issue 反馈问题
- 提交 Pull Request 改进功能或文档
- 完善渠道对接能力
- 补充部署文档、二开文档和示例代码

## 协作建议

- 提交前请确保核心功能可运行
- 涉及接口变更时请同步补充文档
- 涉及配置项变更时请补充默认值说明
- 涉及数据库变更时请同步提供 SQL 脚本
- 建议提交聚焦单一主题，便于评审和合并

---

# 更多支持

欢迎关注官方渠道，获取更多产品动态、部署指导与社区支持。

## 官方公众号

微信扫描下方二维码，关注 **计全科技** 公众号：

![计全科技公众号](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jee-qrcode.jpg "计全科技公众号")

## 官方微信交流群

微信扫描下方二维码加入官方交流群。若项目对你有帮助，欢迎先点一个 Star 支持。

![Jeepay微信交流群](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_open_kf.png "Jeepay微信交流群")

## 微信客服

- 微信客服咨询：<https://work.weixin.qq.com/kfid/kfc6de0edce151ee062>
