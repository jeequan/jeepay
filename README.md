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
- [官方托管服务（计全付）](#官方托管服务计全付)
- [系统能力概览](#系统能力概览)
- [快速开始](#快速开始)
- [部署方式](#部署方式)
- [系统架构](#系统架构)
- [核心技术栈](#核心技术栈)
- [文档与资源](#文档与资源)
- [在线体验](#在线体验)
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

当前已对接微信支付、支付宝、云闪付等主流渠道；后端 `Spring Boot 3.3.7` + `JDK 17`，前端 `Ant Design Vue`，权限体系 `Spring Security`。

适用于支付能力平台化、商户系统建设、支付中台建设以及聚合支付业务的二次开发。

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

- 自建聚合支付平台
- 多商户支付系统
- SaaS 平台支付中台
- 电商、零售、本地生活、数字内容等业务的支付接入
- 服务商模式下的渠道统一管理与商户统一接入
- 需要独立掌控支付流程、商户管理、渠道配置和回调通知的项目

如果你希望快速搭建一套可控、可扩展、可二开的支付系统，Jeepay 是比较合适的基础底座。

---

# 官方托管服务（计全付）

如果你希望 **不自建部署、直接拿到可用的支付通道与分账能力**，欢迎来 **计全付官方** 申请接入：

- **目标场景**：没有支付牌照 / 没有支付通道资源的中小商户、SaaS 平台、独立开发者
- **提供内容**：微信、支付宝、银联等主流通道聚合接入；**分账能力**；账户体系、结算与对账；售后支撑
- **分账亮点**：支持 **100% 全额分账**，满足平台型业务的分润 / 代收代付诉求
- **优势**：开通即用，免去服务器与运维成本；费率与政策按业务规模定制

联系方式：官网 <https://www.jeequan.com> · 微信客服见文末。

> 自托管请继续往下看。

---

# 系统能力概览

- **支付渠道**：微信支付（`V2`/`V3`、服务商 / 普通商户）、支付宝（`RSA`/`RSA2`、服务商 / 普通商户）、云闪付
- **平台能力**：多商户管理、多应用接入、聚合码支付、订单管理、渠道参数配置、商户通知与回调、支付异步通知、权限与账号管理、运营平台与商户平台双端
- **工程能力**：前后端分离、分布式部署、MQ 通知（RocketMQ / ActiveMQ / RabbitMQ）、Docker 部署与脚本化安装、可二次开发

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

```bash
git clone https://github.com/jeequan/jeepay.git
git clone https://github.com/jeequan/jeepay-ui.git
```

## 首次启动流程

1. **准备数据库与缓存**：创建 MySQL 数据库并导入 `docs/sql/init.sql`；准备 Redis（异步通知增强可按需启用 MQ）。
2. **准备配置文件**：修改 `conf/manager/application.yml`、`conf/merchant/application.yml`、`conf/payment/application.yml`，填写 MySQL / Redis / 服务端口 / 支付渠道基础参数。
3. **编译后端**：`mvn clean package -DskipTests`
4. **启动核心服务**：
   | 模块 | 说明 | 默认端口 |
   |---|---|---|
   | `jeepay-payment` | 支付网关 | `9216` |
   | `jeepay-manager` | 运营平台服务端 | `9217` |
   | `jeepay-merchant` | 商户系统服务端 | `9218` |
5. **启动前端**：参考 <https://github.com/jeequan/jeepay-ui>

---

# 部署方式

| 方式 | 适用场景 | 详细说明 |
|---|---|---|
| 宝塔面板一键安装 | 有宝塔面板（≥ 9.2.0），追求图形化操作 | 面板 Docker 应用内搜索 `jeepay`，或看 [教程](https://doc.jeequan.com/#/integrate/open/dev/108) |
| Shell 脚本一键安装 | 干净的 CentOS / Anolis / Ubuntu 服务器，希望一条命令拉起 | [docs/deploy/shell.md](docs/deploy/shell.md) |
| 自助源码部署 | 需要二次开发 / 接入内部基础设施的团队 | 自行准备 MySQL / Redis / MQ，按环境调整 `conf/` 后 Maven 打包部署 |
| Docker Compose 部署 | 本地或测试环境快速起完整集群（含前端） | [docs/deploy/compose.md](docs/deploy/compose.md) |

> **国内零配置直达**：Shell 脚本与 Docker Compose 的默认镜像都指向 **华为云 SWR 公开仓库**（`swr.cn-south-1.myhuaweicloud.com/jeepay/*`），由计全官方维护，公网匿名可拉，不依赖 Docker Hub，**无需登录也不需要配置加速器**。
>
> 部署过程中碰到问题，优先看 [docs/deploy/troubleshooting.md](docs/deploy/troubleshooting.md)。

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
| Redis | 分布式缓存 | 3.2.8+ |
| MySQL | 数据库 | 5.7.x / 8.0+ |
| MQ | 消息中间件 | RocketMQ / ActiveMQ / RabbitMQ |
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

## 本仓库拆分文档

- 部署详解（Shell 脚本）：[docs/deploy/shell.md](docs/deploy/shell.md)
- 部署详解（Docker Compose）：[docs/deploy/compose.md](docs/deploy/compose.md)
- 部署常见问题：[docs/deploy/troubleshooting.md](docs/deploy/troubleshooting.md)
- 功能与接口市场：[docs/features.md](docs/features.md)
- 项目结构与仓库关系：[docs/project-structure.md](docs/project-structure.md)
- 系统截图：[docs/screenshots.md](docs/screenshots.md)

## SDK 资源

Jeepay 已提供 Java、Python SDK，以及 PHP 对接 Demo：

- SDK 下载地址：<https://doc.jeequan.com/#/integrate/open/api/116>
- Java SDK 仓库：<https://github.com/jeequan/jeepay-sdk-java>

## 项目地址

- 服务端：[GitHub](https://github.com/jeequan/jeepay) · [Gitee](https://gitee.com/jeequan/jeepay) · [GitCode](https://gitcode.com/jeequantech/jeepay)
- 前端：[GitHub](https://github.com/jeequan/jeepay-ui) · [Gitee](https://gitee.com/jeequan/jeepay-ui) · [GitCode](https://gitcode.com/jeequantech/jeepay-ui)

---

# 在线体验

- 支付流程体验：<https://www.jeequan.com/demo/jeepay_cashier.html>
- 管理平台 / 商户系统演示：<https://www.jeequan.com/doc/detail_84.html>

---

# 版本与兼容性说明

- 当前项目采用 `Spring Boot 3.3.7`，要求 `JDK 17`
- 数据库建议使用 `MySQL 5.7.x` 或 `8.0+`，Redis 建议 `3.2.8+`
- MQ 为可选增强组件，按业务场景选择启用
- 前端请同步使用对应版本的 `jeepay-ui`
- SDK 对接优先使用官方 SDK 或示例代码

---

# 贡献与协作

欢迎通过以下方式参与项目共建：

- 提交 Issue 反馈问题
- 提交 Pull Request 改进功能或文档
- 完善渠道对接能力
- 补充部署文档、二开文档和示例代码

协作建议：

- 提交前确保核心功能可运行
- 接口 / 配置项 / 数据库变更请同步更新文档与 SQL 脚本
- 建议聚焦单一主题，便于评审与合并

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
