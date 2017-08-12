# xxpay-master
使用spring-cloud开发的分布式聚合支付系统,集成微信,支付宝,易宝支付,京东支付,IAP支付等.

# XxPay项目介绍

## 1. 版本信息
版本 |日期 |描述 |作者   
------- | ------- | ------- | -------
V1.0 |2017-08-11 |创建 |丁志伟 

## 2. 项目结构

- xxpay项目使用java语言开发，jdk版本为1.8（其他版本没有测试过），项目使用maven编译。
- 项目计划使用种架构开发：（1）spring-cloud架构（2）spring-boot-dubbo架构（3）spring-mvc
- 

### 2.1 xxpay-master
| 项目  | server-port | 描述
|---|---|---|---|---|---
|xxpay-common |  | 公共模块(常量、工具类等)，jar发布
|xxpay-dal |  | 支付数据访问层，jar发布
|xxpay-mgr | 8092 | 支付运营平台
|xxpay-shop | 8081 | 支付商城演示系统
|xxpay4spring-cloud |  | 支付中心spring-cloud架构实现
|xxpay4spring-boot-dubbo |  | 支付中心spring-boot-dubbo架构实现
|xxpay4spring-mvc |  | 支付中心spring-mvc架构实现
### 2.2 xxpay4spring-cloud
| 项目  | server-port | 描述
|---|---|---|---|---|---
|xxpay-config | 2020 | 支付服务配置中心
|xxpay-gateway | 3020 | 支付服务API网关
|xxpay-server | 2000 | 支付服务注册中心
|xxpay-service | 3000 | 支付服务端
|xxpay-web | 3010 | 支付客户端

说明:

- 项目启动顺序：xxpay-server > xxpay-config > xxpay-server > xxpay-web > xxpay-gateway 

### 2.3 xxpay4spring-boot-dubbo
| 项目  | server-port | 描述
|---|---|---|---|---|---
|... |  | 
### 2.4 xxpay4spring-mvc
| 项目  | server-port | 描述
|---|---|---|---|---|---
|... |  | 
## 3. 第三方依赖

### 3.1 ActiveMQ服务
在支付中心回调业务系统时，使用了MQ，用到延迟消息处理。
