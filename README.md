>XxPay'官方交流群：206119456（加群暗号：xxpay交流）
***

### 项目介绍
***

- `XxpPay聚合支付` 使用Java开发，分为spring-cloud和dubbo分布式架构版本，已接入微信、支付宝等主流支付渠道，可直接用于生产环境。
- 目前已经接入支付渠道：微信(公众号支付、扫码支付、APP支付)、支付宝(电脑网站支付、手机网站支付、APP支付)；

> [XxPay官网：http://www.xxpay.org](http://www.xxpay.org "xxpay官方网站")

> [XxPay统一扫码支付体验：http://shop.xxpay.org/goods/openQrPay.html](http://shop.xxpay.org/goods/openQrPay.html "xxpay支付体验")

> [XxPay运营平台演示：http://mgr.xxpay.org](http://mgr.xxpay.org "xxpay运营平台")

> [XxPay文档库：http://docs.xxpay.org](http://docs.xxpay.org "xxpay文档库")

- 如何获取支付体验账号？关注官方公众号（搜索：XxPay聚合支付），回复：测试账号。

![体验xxpay支付流程，手机扫一扫可体验](https://git.oschina.net/uploads/images/2017/0813/230918_96b80c69_430718.png "xxpay支付体验")

【运营平台截图】

![输入图片说明](https://git.oschina.net/uploads/images/2017/0814/015506_5b5871eb_430718.png "Xxpay运营平台")

![输入图片说明](https://git.oschina.net/uploads/images/2017/0814/015531_b34e63aa_430718.png "Xxpay运营平台")

### 项目结构
***
```
xxpay-master
├── xxpay4spring-boot-dubbo -- spring-boot-dubbo架构实现
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
|xxpay4spring-boot-dubbo |  | 支付中心spring-boot-dubbo架构实现
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

### 版本更新
***

版本 |日期 |描述
------- | ------- | -------
V1.0.0 |2017-08-11 |完成spring-cloud架构，集成微信、支付宝渠道
V1.0.0 |2017-08-20 |升级spring boot为1.5.6，修复通知bug

接下来的版本开发计划：
```html
+ 增加支付中心查询订单、补单等接口；
+ 增加微信转账、红包接口；
+ 增加IAP支付；
+ 增加spring-mvc版本；
+ 增加spirng-boot-dubbo架构版本；
+ 增加与支付渠道测的对账；
+ 增加账户、结算功能；
+ 增加与商户测的对账；
```
真正开发未必按上面的顺序，大家如有更强烈的开发需求请反馈。

### 关于我们
***
微信扫描下面二维码，关注公众号：XxPay聚合支付，获取更多精彩内容。

![XxPay聚合支付公众号](http://docs.xxpay.org/uploads/201708/attach_14dc8f1fac0a36a1.jpg "XxPay聚合支付公众号")