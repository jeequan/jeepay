<p align="center">
	<a href="https://www.jeequan.com"><img src="https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_logo.svg"></a>
</p>
<p align="center">
	<strong>适合互联网企业使用的开源支付系统</strong>
</p>
<p align="center">
	👉 <a href="https://www.jeequan.com">https://www.jeequan.com</a> 👈
</p>

<p align="center">
	<a target="_blank" href="https://spring.io/projects/spring-boot">
		<img src="https://img.shields.io/badge/spring%20boot-2.4.5-yellowgreen" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8-green.svg" />
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
</p>

<br/>
<p align="center">
	<a href="https://jq.qq.com/?_wv=1027&k=94WnXmdL">
        <img src="https://img.shields.io/badge/qq%E7%BE%A4%E2%91%A0-635647058-critical"/>
    </a>
</p>

-------------------------------------------------------------------------------

## 📚 项目介绍

Jeepay是一套适合互联网企业使用的开源支付系统，支持多渠道服务商和普通商户模式。已对接`微信支付`，`支付宝`，`云闪付`官方接口，支持聚合码支付。

Jeepay使用`Spring Boot`和`Ant Design Vue`开发，集成`Spring Security`实现权限管理功能，是一套非常实用的web开发框架。

### 🎁 名称的由来

Jeepay = Jee + pay，是由原XxPay支付系统作者带领团队开发，“Jee”是公司计全科技名称的表示，pay表示支付。中文名称为计全支付，释为：计出万全、支付安全，让支付更加方便安全。


### 🍟 项目体验

- Jeepay支付流程体验：[https://www.jeequan.com/demo/jeepay_cashier.html](https://www.jeequan.com/demo/jeepay_cashier.html "Jeepay支付体验")
- Jeepay运营平台和商户系统演体验：[https://www.jeequan.com/doc/detail_84.html](https://www.jeequan.com/doc/detail_84.html "Jeepay支付系统体验")
- Jeepay项目文档：[https://docs.jeequan.com/docs/jeepay](https://docs.jeequan.com/docs/jeepay "Jeepay项目文档")
- Jeepay快速使用：[https://docs.jeequan.com/docs/jeepay/jeepay-1dbdn8bqgo270](https://docs.jeequan.com/docs/jeepay/jeepay-1dbdn8bqgo270 "Jeepay快速使用")

### 🍎 项目特点

* 支持多渠道对接，支付网关自动路由
* 已对接`微信`服务商和普通商户接口，支持`V2`和`V3`接口
* 已对接`支付宝`服务商和普通商户接口，支持RSA和RSA2签名
* 已对接`云闪付`服务商接口，可选择多家支付机构
* 提供http形式接口，提供各语言的`sdk`实现，方便对接
* 接口请求和响应数据采用签名机制，保证交易安全可靠
* 系统安全，支持`分布式`部署，`高并发`
* 管理端包括`运营平台`和`商户系统`
* 管理平台操作界面简洁、易用
* 支付平台到商户系统的订单通知使用MQ实现，保证了高可用，消息可达
* 支付渠道的接口参数配置界面自动化生成
* 使用`spring security`实现权限管理
* 前后端分离架构，方便二次开发
* 由原`XxPay`团队开发，有着多年支付系统开发经验

## 🥞 系统架构

> Jeepay计全支付系统架构图

![Jeepay系统架构图](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_framework.png "Jeepay系统架构图")

> Jeepay计全支付聚合码支付流程图

![Jeepay计全支付聚合码支付流程图](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_qr.png "Jeepay计全支付聚合码支付流程图")


> 核心技术栈

| 软件名称  | 描述 | 版本
|---|---|---
|Jdk | Java环境 | 1.8
|Spring Boot | 开发框架 | 2.4.5
|Redis | 分布式缓存 | 3.2.8 或 高版本
|MySQL | 数据库 | 5.7.X 或 8.0 高版本
|MQ | 消息中间件 | ActiveMQ 或 RabbitMQ 或 RocketMQ
|[Ant Design Vue](https://www.antdv.com/docs/vue/introduce-cn/) | Ant Design的Vue实现，前端开发使用 | 2.1.2
|[MyBatis-Plus](https://mp.baomidou.com/) | MyBatis增强工具 | 3.4.2
|[WxJava](https://gitee.com/binary/weixin-java-tools) | 微信开发Java SDK | 4.1.0
|[Hutool](https://www.hutool.cn/) | Java工具类库 | 5.6.6

> 项目结构

```lua
jeepay-ui  -- https://gitee.com/jeequan/jeepay-ui

jeepay
├── conf -- 存放系统部署使用的.yml文件
└── docs -- 存放项目相关文档说明
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

> 开发部署

- 系统开发：[https://docs.jeequan.com/docs/jeepay/dev_serv](https://docs.jeequan.com/docs/jeepay/dev_serv)
- 通道对接：[https://docs.jeequan.com/docs/jeepay/dev_channel](https://docs.jeequan.com/docs/jeepay/dev_channel)
- 线上部署：[https://docs.jeequan.com/docs/jeepay/deploy](https://docs.jeequan.com/docs/jeepay/deploy)
- 接口文档：[https://docs.jeequan.com/docs/jeepay/payment_api](https://docs.jeequan.com/docs/jeepay/payment_api)
- 常见问题：[https://docs.jeequan.com/docs/jeepay/jeepay-1d99ciatu11h5](https://docs.jeequan.com/docs/jeepay/jeepay-1d99ciatu11h5)


## 🍿 功能模块

> Jeepay运营平台功能

![Jeepay运营平台功能](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_mgr.png "Jeepay运营平台功能")

> Jeepay商户系统功能

![Jeepay商户系统功能](https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jeepay_mch.png "Jeepay商户系统功能")

## 🍯 系统截图

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

## 🥪 关于我们
***
微信扫描下面二维码，关注官方公众号：计全科技，获取更多精彩内容。

![计全科技公众号](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/jee-qrcode.jpg "计全科技公众号")

微信扫描下方二维码，邀请进官方微信交流群（加好友备注：邀请进群或jeepay咨询），开源不易，进群前请先点Star给与支持。

![Jeepay微信交流群](http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/wx_my.png "Jeepay微信交流群")
