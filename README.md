<p align="center">
	<a href="https://www.jeequan.com"><img src="https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_logo.svg"></a>
</p>
<p align="center">
	<strong>计全支付 - 让支付接入更简单</strong>
</p>
<p align="center">
	👉 <a href="https://www.jeequan.com">https://www.jeequan.com</a> 👈
</p>

<p align="center">
	<a target="_blank" href="https://spring.io/projects/spring-boot">
		<img src="https://img.shields.io/badge/spring%20boot-3.3.7-yellowgreen" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/downloads/#java17">
		<img src="https://img.shields.io/badge/JDK-17-green.svg" />
	</a>
	<a target="_blank" href="http://www.gnu.org/licenses/lgpl.html">
		<img src="https://img.shields.io/badge/license-LGPL--3.0-blue" />
	</a>
	<a href='https://gitee.com/jeequan/jeepay/stargazers' target="_blank">
        <img src='https://gitee.com/jeequan/jeepay/badge/star.svg?theme=gvp' alt='star'></img>
    </a>
	<a target="_blank" href='https://github.com/jeequan/jeepay'>
		<img src="https://img.shields.io/github/stars/jeequan/jeepay.svg?style=social" alt="github star"/>
	</a>
	<a target="_blank" href='https://gitcode.com/jeequantech/jeepay'>
		<img src="https://gitcode.com/jeequantech/jeepay/star/badge.svg" alt="gitcode star"/>
	</a>
</p>

<br/>
<p align="center">
	<a href="https://jq.qq.com/?_wv=1027&k=94WnXmdL">
        <img src="https://img.shields.io/badge/qq%E7%BE%A4%E2%91%A0-635647058-critical"/>
    </a>
</p>

-------------------------------------------------------------------------------

# 项目介绍

Jeepay是一套适合互联网企业使用的开源支付系统，支持多渠道服务商和普通商户模式。已对接`微信支付`，`支付宝`，`云闪付`官方接口，支持聚合码支付。

Jeepay使用`Spring Boot`和`Ant Design Vue`开发，集成`Spring Security`实现权限管理功能，是一套非常实用的web开发框架。

## 名称的由来

Jeepay = Jee + pay，是由原XxPay支付系统作者带领团队开发，“Jee”是公司计全科技名称的表示，pay表示支付。中文名称为计全支付，释为：计出万全、支付安全，让支付更加方便安全。

## 项目特点
* 支持多渠道对接，多种支付产品
* 已对接`微信`服务商和普通商户接口，支持`V2`和`V3`接口
* 已对接`支付宝`服务商和普通商户接口，支持RSA和RSA2签名
* 已对接`云闪付`服务商接口，可选择多家支付机构
* 提供http形式接口，提供各语言的`sdk`实现，方便对接
* 接口请求和响应数据采用签名机制，保证交易安全可靠
* 系统安全，支持`分布式`部署，`高并发`
* 支持`多商户模式`，商户多应用接入
* 管理平台操作界面简洁、易用
* 支付平台到商户系统的订单通知使用MQ实现，保证了高可用，消息可达，支持多个产品MQ
* 支付渠道的接口参数配置界面自动化生成
* 使用`spring security`实现权限管理
* 前后端分离架构，方便二次开发
* 由原`XxPay`团队开发，有着多年支付系统开发经验
* 支持docker部署，官方发布一键部署脚本，10分钟部署完成

## 接口市场

计全官方团队基于开源版代码，开发了对接各家三方支付和银行的对接代码。为了让用户能够快速接入支付，目前已将对接好的代码发布到官方接口市场，并不断更新。
如有需要，可前去接口市场购买。

接口市场：https://www.jeequan.com/ifstore/list.html

安装说明：https://docs.jeequan.com/docs/jeepay/jeepay-1ejdnsuhveb16

目前发布接口包括

三方：汇付Adapay、斗拱支付、支付宝直付通、微信收付通、银盛支付、银联条码前置、银联支付、联动优势、国通星驿付、丰付支付、盛付通、乐刷、杉德支付、瑞银信、拉拉卡、汇聚支付、新生支付、河马支付、海科融通、富友支付、易生支付、支付宝云支付、通联支付

银行：工行支付、浦发银行、建行龙支付、交行支付、

四方：付呗支付、米花支付

# 系统架构

> Jeepay计全支付系统架构图

![Jeepay系统架构图](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_framework.png "Jeepay系统架构图")

> Jeepay计全支付聚合码支付流程图

![Jeepay计全支付聚合码支付流程图](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_qr.png "Jeepay计全支付聚合码支付流程图")

## 核心技术栈

| 软件名称  | 描述 | 版本
|---|---|---
|Jdk | Java环境 | 17
|Spring Boot | 开发框架 | 3.3.7
|Redis | 分布式缓存 | 3.2.8 或 高版本
|MySQL | 数据库 | 5.7.X 或 8.0 高版本
|MQ | 消息中间件 | ActiveMQ 或 RabbitMQ 或 RocketMQ
|[Ant Design Vue](https://www.antdv.com/docs/vue/introduce-cn/) | Ant Design的Vue实现，前端开发使用 | 4.2.6
|[MyBatis-Plus](https://mp.baomidou.com/) | MyBatis增强工具 | 3.4.2
|[WxJava](https://gitee.com/binary/weixin-java-tools) | 微信开发Java SDK | 4.6.0
|[Hutool](https://www.hutool.cn/) | Java工具类库 | 5.8.26

## 项目结构

```lua
jeepay-ui  -- https://gitee.com/jeequan/jeepay-ui

jeepay
├── conf -- 存放系统部署使用的.yml文件
├── docker -- 存放docker相关文件
└── docs -- 存放项目相关文档说明
     ├── intsll -- 项目部署shell脚本
     ├── script -- 项目启动shell脚本
     └── sql -- 初始化sql文件
└── jeepay-components -- 公共组件目录
     ├── jeepay-components-mq -- mq组件
     └── jeepay-components-oss -- oss组件
├── jeepay-core -- 核心依赖包
├── jeepay-manager -- 运营平台服务端[9217]
├── jeepay-merchant -- 商户系统服务端[9218]
├── jeepay-payment -- 支付网关[9216]
├── jeepay-service -- 业务层代码
└── jeepay-z-codegen -- mybatis代码生成
```

# 项目体验

## 支付体验
- Jeepay支付流程体验：[https://www.jeequan.com/demo/jeepay_cashier.html](https://www.jeequan.com/demo/jeepay_cashier.html "Jeepay支付体验")

## 管理平台
- Jeepay运营平台和商户系统演体验：[https://www.jeequan.com/doc/detail_84.html](https://www.jeequan.com/doc/detail_84.html "Jeepay支付系统体验")



# 如何使用

## 最佳实践

将Jeepay作为一个支付模块部署，对外提供支付接入能力，有支付需求的业务都可以接入使用。

#### 1、宝塔面板安装：
-  安装宝塔面板9.2.0及以上版本， Docker搜索 jeepay 一键安装
- 【宝塔安装教程】: https://docs.jeequan.com/docs/jeepay/jeepay-1g4h1figddgfm

#### 2、shell脚本一键安装：

CentOS 安装脚本 （推荐Anolis OS 8.8）：
```lua
yum install -y wget && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && sh install.sh
```
Ubuntu 安装脚本 （推荐 Ubuntu 22.04 64位）：
```lua
apt update && apt-get -y install docker.io && apt-get -y install git && wget -O install.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/install.sh && sh install.sh
```
视频教程：
- 【jeepay开源聚合支付系统一键部署和测试教程】: [https://www.bilibili.com/video/BV17C411Y7EZ/?share_source=copy_web&vd_source=e48f1c20ae2c74b29a0b959a168914f2](https://www.bilibili.com/video/BV17C411Y7EZ/?share_source=copy_web&vd_source=e48f1c20ae2c74b29a0b959a168914f2"教程") 



## 项目文档

- 项目文档：[https://docs.jeequan.com/docs/jeepay](https://docs.jeequan.com/docs/jeepay "Jeepay项目文档")
- 开发指导：[https://docs.jeequan.com/docs/jeepay/dev_serv](https://docs.jeequan.com/docs/jeepay/dev_serv)
- 通道对接：[https://docs.jeequan.com/docs/jeepay/dev_channel](https://docs.jeequan.com/docs/jeepay/dev_channel)
- 线上部署：[https://docs.jeequan.com/docs/jeepay/deploy](https://docs.jeequan.com/docs/jeepay/deploy)
- 接口文档：[https://docs.jeequan.com/docs/jeepay/payment_api](https://docs.jeequan.com/docs/jeepay/payment_api)
- 常见问题：[https://docs.jeequan.com/docs/jeepay/jeepay-1d99ciatu11h5](https://docs.jeequan.com/docs/jeepay/jeepay-1d99ciatu11h5)
- 快速上手：[https://docs.jeequan.com/docs/jeepay/jeepay-1dbdn8bqgo270](https://docs.jeequan.com/docs/jeepay/jeepay-1dbdn8bqgo270 "Jeepay快速使用")

## SDK对接

Jeepay已经开发了java和python的sdk，方便接入方开发对接。

- Java SDK：[https://gitee.com/jeequan/jeepay-sdk-java](https://gitee.com/jeequan/jeepay-sdk-java "java sdk")    
  或 https://gitcode.com/jeequantech/jeepay-sdk-java
- Python SDK：[https://gitee.com/jeequan/jeepay-sdk-python](https://gitee.com/jeequan/jeepay-sdk-python "python sdk")    
  或 https://gitcode.com/jeequantech/jeepay-sdk-python

# 功能模块

> Jeepay运营平台功能

![Jeepay运营平台功能](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_mgr.png "Jeepay运营平台功能")

> Jeepay商户系统功能

![Jeepay商户系统功能](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_mch.png "Jeepay商户系统功能")

# 系统截图

`以下截图是从实际已完成功能界面截取,截图时间为：2021-07-06 08:59`

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

# 更多支持
***
微信扫描下面二维码，关注官方公众号：计全科技，获取更多精彩内容。

![计全科技公众号](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jee-qrcode.jpg "计全科技公众号")

微信扫描下方二维码，邀请进官方微信交流群。开源不易，进群前请先点Star给与支持。

![Jeepay微信交流群](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_open_kf.png "Jeepay微信交流群")

微信客服咨询：[https://work.weixin.qq.com/kfid/kfc6de0edce151ee062](https://work.weixin.qq.com/kfid/kfc6de0edce151ee062 "jeepay微信客服咨询")
