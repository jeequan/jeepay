XxPay官方交流群：206119456（加群暗号：xxpay交流）
***

### 项目简介
***

- xxpay是一个使用spring-cloud开发的分布式聚合支付系统,可直接用于生产环境.目前已经集成了微信(公众号支付、扫码支付、APP支付),支付宝(电脑网站支付、手机网站支付、APP支付),正在集成开发中的包括:易宝支付,京东支付,IAP支付等;
- xxpay是一个聚合支付的角色,业务系统只需对接几个接口,就可完成所有与第三方支付渠道交互的后端逻辑处理;
- xxpay也是一个标准的分布式系统开发脚手架,后期会陆续完成spring-mvc,及dubbo版本的开发;

[XxPay官网：http://www.xxpay.org](http://www.xxpay.org "xxpay官方网站")

[支付体验&捐赠我：http://shop.xxpay.org/goods/openQrPay.html](http://shop.xxpay.org/goods/openQrPay.html "xxpay支付体验")

[XxPay运营平台演示：http://mgr.xxpay.org](http://mgr.xxpay.org "xxpay运营平台")

可以用微信或支付宝客户端扫描下面二维码，完成支付流程体验。体验的同时也是捐赠我哦！！！
支付的订单数据可以到[XxPay运营平台](http://mgr.xxpay.org "xxpay运营平台")中查看。
![体验xxpay支付流程，手机扫一扫可体验](https://git.oschina.net/uploads/images/2017/0813/230918_96b80c69_430718.png "xxpay支付体验")

【运营平台截图】

![输入图片说明](https://git.oschina.net/uploads/images/2017/0814/015506_5b5871eb_430718.png "Xxpay运营平台")

![输入图片说明](https://git.oschina.net/uploads/images/2017/0814/015531_b34e63aa_430718.png "Xxpay运营平台")

### 项目部署
***

[xxpay表结构](https://gitee.com/jmdhappy/xxpay-master/wikis/xxpay表结构 "xxpay表结构")

[xxpay部署步骤](https://gitee.com/jmdhappy/xxpay-master/wikis/xxpay部署步骤 "xxpay部署步骤")

### 项目结构
***

- xxpay项目使用java语言开发，jdk版本为1.8，使用maven编译。
- 项目计划使用种架构开发：
（1）spring-cloud架构
（2）spring-boot-dubbo架构
（3）spring-mvc

#### xxpay-master
| 项目  | server-port | 描述
|---|---|---
|xxpay-common |  | 公共模块(常量、工具类等)，jar发布
|xxpay-dal |  | 支付数据访问层，jar发布
|xxpay-mgr | 8092 | 支付运营平台
|xxpay-shop | 8081 | 支付商城演示系统
|xxpay4spring-cloud |  | 支付中心spring-cloud架构实现
|xxpay4spring-boot-dubbo |  | 支付中心spring-boot-dubbo架构实现
|xxpay4spring-mvc |  | 支付中心spring-mvc架构实现
#### xxpay4spring-cloud
| 项目  | server-port | 描述
|---|---|---
|xxpay-config | 2020 | 支付服务配置中心
|xxpay-gateway | 3020 | 支付服务API网关
|xxpay-server | 2000 | 支付服务注册中心
|xxpay-service | 3000 | 支付服务端
|xxpay-web | 3010 | 支付客户端

项目启动顺序：
```
xxpay-server > xxpay-config > xxpay-service > xxpay-web > xxpay-gateway
```

#### xxpay4spring-boot-dubbo
| 项目  | server-port | 描述
|---|---|---
|... |  |
#### xxpay4spring-mvc
| 项目  | server-port | 描述
|---|---|---
|... |  |


### 版本更新
***

版本 |日期 |描述 |作者
------- | ------- | ------- | -------
V1.0.0 |2017-08-11 |创建 |丁志伟

### 关于作者
***
QQ：29093576
Email：jmdhappy@126.com