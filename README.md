>XxPay'官方技术交流群：206119456`满2000` 新群:885394559 （加群暗号：xxpay交流）。

>XxPay'官方资源交流群：214196580`满500` 新群:838740906 （加群暗号：xxpay交流）。
***

### 项目介绍
***

- `XxPay聚合支付` XxPay聚合支付使用Java开发，包括spring-cloud、dubbo、spring-boot三个架构版本，已接入微信、支付宝等主流支付渠道，可直接用于生产环境。

- 目前已经接入支付渠道：微信(公众号支付、扫码支付、APP支付、H5支付)、支付宝(电脑网站支付、手机网站支付、APP支付、当面付)；

> [XxPay官网：http://www.xxpay.org](http://www.xxpay.org "xxpay官方网站")

> [XxPay开发社区：http://pub.xxpay.org](http://pub.xxpay.org "xxpay开发社区")

> [XxPay统一扫码支付体验：http://shop.xxpay.org/goods/openQrPay.html](http://shop.xxpay.org/goods/openQrPay.html "xxpay支付体验")

> [XxPay运营平台演示：http://mgr.xxpay.org](http://mgr.xxpay.org "xxpay运营平台")

> [XxPay文档库：http://docs.xxpay.org](http://docs.xxpay.org "xxpay文档库")

### 版本更新
***

版本 |日期 |描述
------- | ------- | -------
V1.0.0 |2018-04-15 |在springboot版本中增加了rabbitMQ的支持，修复其他bug
V1.0.0 |2018-04-09 |已完成spring cloud新版规划
V1.0.0 |2017-11-25 |在dubbo版本增加了转账、退款接口；重构了商户通知；修复了已知Bug
V1.0.0 |2017-10-24 |提交xxpay4spring-boot版本
V1.0.0 |2017-09-10 |完成dubbo架构，增加支付订单查询接口
V1.0.0 |2017-08-25 |微信支付SDK更换为weixin-java-pay，增加微信H5支付，增加docker部署支持
V1.0.0 |2017-08-23 |升级支付宝为最新接口
V1.0.0 |2017-08-20 |升级spring boot为1.5.6，修复通知bug
V1.0.0 |2017-08-11 |完成spring-cloud架构，集成微信、支付宝渠道

接下来的开源版本开发计划：
```html
+ 重点发展spring cloud架构版本；
+ 增加PC，H5支付场景体验；
+ 重构支付核心，便于渠道对接；
+ 持续增加其他支付渠道对接；
```
真正开发未必按上面的顺序，大家如有更强烈的开发需求请反馈作者。

### 项目测试
------------


- 如何获取支付体验账号？关注官方公众号（搜索：XxPay），回复：测试账号。
- 支付宝沙箱测试：[XxPay支付宝沙箱测试](http://docs.xxpay.org/docs/deploy/41 "XxPay支付宝沙箱测试")

![体验XxPay支付流程，手机扫一扫可体验](https://git.oschina.net/uploads/images/2017/1009/112525_df5aac80_430718.png "XxPay支付体验")

【运营平台截图】

![输入图片说明](https://git.oschina.net/uploads/images/2017/0814/015506_5b5871eb_430718.png "Xxpay运营平台")

![输入图片说明](https://git.oschina.net/uploads/images/2017/0814/015531_b34e63aa_430718.png "Xxpay运营平台")

### 项目结构
***
```
xxpay-master
├── xxpay4dubbo -- spring-boot-dubbo架构实现
|    ├── xxpay4dubbo-api -- 接口定义
|    ├── xxpay4dubbo-service -- 服务生产者
|    ├── xxpay4dubbo-web -- 服务消费者
├── xxpay4spring-cloud -- spring-cloud架构实现
|    ├── xxpay-config -- 配置中心
|    ├── xxpay-gateway -- API网关
|    ├── xxpay-server -- 服务注册中心
|    ├── xxpay-service -- 服务生产者
|    └── xxpay-web -- 服务消费者
├── xxpay4spring-mvc -- spring-mvc架构实现
├── xxpay-common -- 公共模块
├── xxpay-dal -- 数据持久层
├── xxpay-mgr -- 运营管理平台
├── xxpay-shop -- 演示商城
```

#### xxpay-master
| 项目  | 端口 | 描述
|---|---|---
|xxpay-common |  | 公共模块(常量、工具类等)，jar发布
|xxpay-dal |  | 支付数据持久层，jar发布
|xxpay-mgr | 8092 | 支付运营平台
|xxpay-shop | 8081 | 支付商城演示系统
|xxpay4spring-cloud |  | 支付中心spring-cloud架构实现
|xxpay4dubbo |  | 支付中心spring-boot-dubbo架构实现
|xxpay4spring-mvc |  | 支付中心spring-mvc架构实现
#### xxpay4spring-cloud
| 项目  | 端口 | 描述
|---|---|---
|xxpay-config | 2020 | 支付服务配置中心
|xxpay-gateway | 3020 | 支付服务API网关
|xxpay-server | 2000 | 支付服务注册中心
|xxpay-service | 3000 | 支付服务生产者
|xxpay-web | 3010 | 支付服务消费者
项目启动顺序：
```
xxpay-server > xxpay-config > xxpay-service > xxpay-web > xxpay-gateway
```
#### xxpay4dubbo
| 项目  | 端口 | 描述
|---|---|---
|xxpay4dubbo-api |  | API接口定义
|xxpay4dubbo-service | 20880 | 支付服务生产者
|xxpay4dubbo-web | 3020 | 支付服务消费者
项目启动顺序：
```
xxpay4dubbo-service > xxpay4dubbo-web
```
### 项目部署
***

项目部署文档：[XxPay项目部署](http://docs.xxpay.org/docs/deploy "xxpay部署")

作者已成功将项目部署在阿里云主机上，服务器配置为：

| CPU  | 内存 | 操作系统
|---|---|---
|1核 | 2 GB | CentOS 6.8 64位

安装的各软件对应的版本为（仅供参考）：

| 软件  | 版本 | 说明
|---|---|---
|JDK | 1.8 | spring boot 对低版支持没有测过
|ActiveMQ|  5.11.1 | 高版本也可以，如：5.14.3
|MySQL | 5.7.17 | 要在5.6以上，否则初始化SQL会报错，除非手动修改建表语句

### 关于我们
***
微信扫描下面二维码，关注官方公众号：XxPay，获取更多精彩内容。

![XxPay聚合支付公众号](http://docs.xxpay.org/uploads/201708/attach_14dc8f1fac0a36a1.jpg "XxPay公众号")