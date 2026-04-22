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
- [Jeepay 适合谁](#jeepay-适合谁)
- [官方托管服务（计全付）](#官方托管服务计全付)
- [部署方式](#部署方式)
- [系统架构](#系统架构)
- [核心技术栈](#核心技术栈)
- [文档与资源](#文档与资源)
- [贡献与协作](#贡献与协作)
- [更多支持](#更多支持)

---

# 项目简介

Jeepay 是一套面向互联网企业的开源支付系统，支持 **普通商户模式** / **多渠道服务商模式** / **聚合码支付** / **多商户多应用接入**。已对接微信支付、支付宝、云闪付等主流渠道。

后端 `Spring Boot 3.3.7` + `JDK 17` + `Spring Security`，前端 `Ant Design Vue`，MQ 支持 `RocketMQ` / `ActiveMQ` / `RabbitMQ`。

适合自建聚合支付平台、SaaS 支付中台、多商户支付系统以及电商 / 零售 / 本地生活 / 数字内容等业务的支付接入与二次开发。

---

# Jeepay 适合谁

- 想**独立掌控**支付流程、商户体系、渠道配置、回调通知，不想被托管服务绑定
- 需要**服务商模式**管理多家商户、多家渠道的平台方
- 计划长期**二次开发**，希望起点是一套架构清晰、代码分层明确、文档完备的开源系统
- 需要 Docker / Shell 一键部署、分布式、多 MQ 可切换的**可运维**支付底座

由原 `XxPay` 团队持续开发维护，多年支付实战沉淀。

---

# 官方托管服务（计全付）

如果你希望 **不自建部署、直接拿到可用的支付通道与分账能力**，欢迎来 **计全付官方** 申请接入：

- **目标场景**：没有支付牌照 / 没有支付通道资源的中小商户、SaaS 平台、独立开发者
- **提供内容**：微信、支付宝、银联等主流通道聚合接入；**分账能力**；账户体系、结算与对账；售后支撑
- **分账亮点**：支持 **100% 全额分账**，满足平台型业务的分润 / 代收代付诉求
- **优势**：开通即用，免去服务器与运维成本；费率与政策按业务规模定制

联系方式：官网 <https://www.jeequan.com> · 微信客服见文末。

---

# 部署方式

| 方式 | 适用场景 | 跳转 |
|---|---|---|
| **Shell 脚本一键安装** | 干净的 CentOS / Anolis / Ubuntu / Debian 服务器 | [docs/deploy/shell.md](docs/deploy/shell.md) |
| **Docker Compose 部署** | 本地 / 测试环境起完整集群（含前端） | [docs/deploy/compose.md](docs/deploy/compose.md) |
| 宝塔面板一键安装 | 有宝塔面板（≥ 9.2.0），Docker 应用市场一键装 | Compose 文件：[`docker-compose.baota.yml`](docker-compose.baota.yml)，用法：[docs/deploy/baota.md](docs/deploy/baota.md) |
| 自助源码部署 | 对接内部基础设施的团队 | 自备 MySQL / Redis / MQ，按环境调整 `conf/` 后 Maven 打包部署 |

> 默认镜像指向**华为云 SWR 公开仓库**，国内零配置直达，不依赖 Docker Hub。部署后需域名 + HTTPS 见 [docs/deploy/https.md](docs/deploy/https.md)，出问题优先看 [docs/deploy/troubleshooting.md](docs/deploy/troubleshooting.md)。

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

## 本仓库文档

- 部署 — Shell 脚本：[docs/deploy/shell.md](docs/deploy/shell.md)
- 部署 — Docker Compose：[docs/deploy/compose.md](docs/deploy/compose.md)
- 部署 — 宝塔面板：[docs/deploy/baota.md](docs/deploy/baota.md)
- 部署 — 域名 + HTTPS：[docs/deploy/https.md](docs/deploy/https.md)
- 部署 — 常见问题排查：[docs/deploy/troubleshooting.md](docs/deploy/troubleshooting.md)
- 镜像发布（维护者）：[docs/deploy/publish.md](docs/deploy/publish.md)
- 功能与接口市场：[docs/features.md](docs/features.md)
- 项目结构：[docs/project-structure.md](docs/project-structure.md)
- 系统截图：[docs/screenshots.md](docs/screenshots.md)
- 贡献指南：[CONTRIBUTING.md](CONTRIBUTING.md)

## SDK 资源

Jeepay 已提供 Java、Python SDK，以及 PHP 对接 Demo：

- SDK 下载地址：<https://doc.jeequan.com/#/integrate/open/api/116>
- Java SDK 仓库：<https://github.com/jeequan/jeepay-sdk-java>

## 项目地址

- 服务端：[GitHub](https://github.com/jeequan/jeepay) · [Gitee](https://gitee.com/jeequan/jeepay) · [GitCode](https://gitcode.com/jeequantech/jeepay)
- 前端：[GitHub](https://github.com/jeequan/jeepay-ui) · [Gitee](https://gitee.com/jeequan/jeepay-ui) · [GitCode](https://gitcode.com/jeequantech/jeepay-ui)

---

# 贡献与协作

- 分支模型、commit 规范、PR 流程、测试要求：见 [CONTRIBUTING.md](CONTRIBUTING.md)
- 反馈问题 / 提交 PR / 完善渠道对接 / 补充文档，都欢迎
- 在线体验：[支付流程](https://www.jeequan.com/demo/jeepay_cashier.html) · [管理平台演示](https://www.jeequan.com/doc/detail_84.html)

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
