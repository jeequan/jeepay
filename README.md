<p align="center">
  <a href="https://www.jeequan.com">
    <img src="https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_logo.svg" alt="Jeepay Logo">
  </a>
</p>

<p align="center">
  <strong>计全支付（Jeepay）- 让支付接入更简单</strong>
</p>

<p align="center">
  👉 <a href="https://www.jeequan.com">https://www.jeequan.com</a> 👈
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

# Jeepay 是什么

Jeepay 是一套面向互联网企业的开源支付系统，支持 **普通商户模式** 与 **多渠道服务商模式**，已对接：

- 微信支付
- 支付宝
- 云闪付

并支持 **聚合码支付** 等常见支付场景。

项目采用：

- 后端：`Spring Boot`
- 前端：`Ant Design Vue`
- 权限体系：`Spring Security`

适合用于：

- 聚合支付平台搭建
- 商户支付能力接入
- 支付中台建设
- 二次开发与支付业务扩展

---

# 项目亮点

- 支持多渠道对接、多种支付产品形态
- 支持微信服务商 / 普通商户接口，兼容 `V2` 与 `V3`
- 支持支付宝服务商 / 普通商户接口，兼容 `RSA` / `RSA2`
- 支持云闪付服务商接口，可接入多家支付机构
- 提供标准化 HTTP 接口，便于业务系统快速接入
- 提供多语言 SDK，降低接入成本
- 请求与响应数据采用签名机制，保障交易安全
- 支持分布式部署与高并发业务场景
- 支持多商户、多应用接入模式
- 后台界面完整，便于运营、商户和支付管理
- 支付通知通过 MQ 投递，保障消息可达性与系统高可用
- 支付渠道参数配置支持界面化、自动化生成
- 前后端分离架构，便于维护与二次开发
- 支持 Docker 部署，并提供一键安装脚本
- 由原 `XxPay` 团队持续开发维护，具备多年支付系统实战经验

---

# 名称由来

`Jeepay = Jee + Pay`

- `Jee`：代表计全科技
- `Pay`：代表支付能力

中文名称为 **计全支付**，寓意：**计出万全、支付安全，让支付接入更加方便高效**。

---

# 快速入口

## 在线体验

### 支付体验
- Jeepay 支付流程体验：<https://www.jeequan.com/demo/jeepay_cashier.html>

### 管理平台体验
- Jeepay 运营平台 / 商户系统演示：<https://www.jeequan.com/doc/detail_84.html>

## 项目文档

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

---

# 项目地址

## 服务端项目

- GitHub：<https://github.com/jeequan/jeepay>
- Gitee：<https://gitee.com/jeequan/jeepay>
- GitCode：<https://gitcode.com/jeequantech/jeepay>

## 前端项目

- GitHub：<https://github.com/jeequan/jeepay-ui>
- Gitee：<https://gitee.com/jeequan/jeepay-ui>
- GitCode：<https://gitcode.com/jeequantech/jeepay-ui>

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

# 项目结构

```text
jeepay-ui

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

---

# 如何使用

## 对接方式

Jeepay 可以作为独立支付模块部署，对外提供统一支付接入能力。业务系统通过 HTTP 接口完成支付、查询、退款、回调等能力接入。

适用于：

- 自研业务系统统一接入支付能力
- 多商户支付能力聚合
- 服务商模式支付中台建设

## SDK 对接

推荐优先使用官方 SDK 或示例代码进行接入，以减少签名、验签、参数组装等重复开发工作。

---

# 部署安装

## 方式一：宝塔面板一键安装

- 安装 **宝塔面板 9.2.0 及以上版本**
- 在 Docker 应用中搜索 `jeepay`，即可一键安装
- 安装教程：<https://doc.jeequan.com/#/integrate/open/dev/108>

## 方式二：Shell 脚本一键安装

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

### 视频教程

- Jeepay 开源聚合支付系统一键部署和测试教程：<https://www.bilibili.com/video/BV17C411Y7EZ/?share_source=copy_web&vd_source=e48f1c20ae2c74b29a0b959a168914f2>

---

# 接口市场

计全官方团队基于开源版代码，持续开发了多家第三方支付机构、银行以及扩展支付接口的对接代码。为了帮助用户更快完成支付能力接入，已将相关对接能力发布到官方接口市场，并持续更新。

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
