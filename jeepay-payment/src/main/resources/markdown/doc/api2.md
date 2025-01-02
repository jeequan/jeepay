# 支付接口

## 统一下单

商户业务系统通过统一下单接口发起支付收款订单，Jeepay支付网关会根据商户配置的支付通道路由支付通道完成支付下单。支付网关根据不同的支付方式返回对应的支付参数，业务系统使用支付参数发起收款。

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：https://pay.jeepay.vip/api/pay/unifiedOrder

请求方式：`POST`

请求类型：`application/json` 或 `application/x-www-form-urlencoded`

> 请求参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号   
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
商户订单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 商户生成的订单号  
支付方式 | wayCode | 是 | String(30) | WX_LITE | 支付方式,如微信小程序WX_LITE
支付金额 | amount | 是 | int | 100 | 支付金额,单位分  
货币代码 | currency | 是 | String(3) | cny | 三位货币代码,人民币:cny  
客户端IP | clientIp | 否 | String(32) | 210.73.10.148 | 客户端IPV4地址  
商品标题 | subject | 是 | String(64) | Jeepay商品标题测试 | 商品标题  
商品描述 | body | 是 | String(256) | Jeepay商品描述测试 | 商品描述  
异步通知地址 | notifyUrl | 否 | String(128) | https://www.jeequan.com/notify.htm | 支付结果异步回调URL,只有传了该值才会发起回调  
跳转通知地址 | returnUrl | 否 | String(128) | https://www.jeequan.com/return.htm | 支付结果同步跳转通知URL
失效时间 | expiredTime | 否 | int | 3600 | 订单失效时间,单位秒,默认2小时.订单在(创建时间+失效时间)后失效   
渠道参数 | channelExtra | 否 | String(256 | {"auth_code", "13920933111042"} | 特定渠道发起的额外参数,json格式字符串.详见渠道参数说明  
分账模式 | divisionMode | 否 | int | 0 | 分账模式： 0-该笔订单不允许分账[默认], 1-支付成功按配置自动完成分账, 2-商户手动分账(解冻商户金额)  
扩展参数 | extParam | 否 | String(512) | 134586944573118714 | 商户扩展参数,回调时会原样返回  
请求时间 | reqTime | 是 | long | 1622016572190 | 请求接口时间,13位时间戳   
接口版本 | version | 是 | String(3) | 1.0 | 接口版本号，固定：1.0  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  
签名类型 | signType | 是 | String(32) | MD5 | 签名类型，目前只支持MD5方式  


`请求示例数据`

```json
{
  "amount": 8,
  "extParam": "",
  "mchOrderNo": "mho1624005107281",
  "subject": "商品标题",
  "wayCode": "ALI_BAR",
  "sign": "84F606FA25A6EC4783BECC08D4FDC681",
  "reqTime": "1624005107",
  "body": "商品描述",
  "version": "1.0",
  "channelExtra": "{\"authCode\":\"280812820366966512\"}",
  "appId": "60cc09bce4b0f1c0b83761c9",
  "clientIp": "192.166.1.132",
  "notifyUrl": "https://www.jeequan.com",
  "signType": "MD5",
  "currency": "cny",
  "returnUrl": "",
  "mchNo": "M1623984572",
  "divisionMode": 1
}
```

> 返回参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
返回状态 | code | 是 | int | 0 | 0-处理成功，其他-处理有误，详见错误码  
返回信息 | msg | 否 | String(128) | 签名失败 | 具体错误原因，例如：签名失败、参数格式校验错误  
签名信息 | sign | 否 | String(32) | CCD9083A6DAD9A2DA9F668C3D4517A84 | 对data内数据签名,如data为空则不返回
返回数据 | data | 否 | String(512) | {} | 返回下单数据,json格式数据  

`data数据格式`

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
支付订单号 | payOrderId | 是 | String(30) | U12021022311124442600 | 返回支付系统订单号  
商户订单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 返回商户传入的订单号  
订单状态 | orderState | 是 | int | 2 | 支付订单状态<br>0-订单生成<br>1-支付中<br>2-支付成功<br>3-支付失败<br>4-已撤销<br>5-已退款<br>6-订单关闭  
支付数据类型 | payDataType | 是 | String | payUrl | 支付参数类型<br>payUrl-跳转链接的方式<br>form-表单方式<br>wxapp-微信支付参数(微信公众号,小程序,app支付时)<br>aliapp-支付宝app支付参数<br>ysfapp-云闪付app支付参数<br>codeUrl-二维码地址<br>codeImgUrl-二维码图片地址<br>none-空支付参数 
支付数据 | payData | 否 | String | http://www.jeequan.com/pay.html | 发起支付用到的支付参数，如果微信公众号支付等 
渠道错误码 | errCode | 否 | String | ACQ.PAYMENT_AUTH_CODE_INVALID | 上游渠道返回的错误码
渠道错误描述 | errMsg | 否 | String | Business Failed 失败 | 上游渠道返回的错误描述

`返回示例数据`

```json
{
  "code": 0,
  "data": {
    "errCode": "ACQ.PAYMENT_AUTH_CODE_INVALID",
    "errMsg": "Business Failed【支付失败，获取顾客账户信息失败，请顾客刷新付款码后重新收款，如再次收款失败，请联系管理员处理。[SOUNDWAVE_PARSER_FAIL]】",
    "mchOrderNo": "mho1624005752661",
    "orderState": 3,
    "payOrderId": "P202106181642329900002"
  },
  "msg": "SUCCESS",
  "sign": "F4DA202C516D1F33A12F1E547C5004FD"
}
```

> 支付方式

WayCode | 支付方式 
------- | ------- 
QR_CASHIER | 聚合扫码(用户扫商家) 
AUTO_BAR | 聚合条码(商家扫用户) 
ALI_BAR | 支付宝条码 
ALI_JSAPI | 支付宝生活号
ALI_APP | 支付宝APP 
ALI_WAP | 支付宝WAP 
ALI_PC | 支付宝PC网站 
ALI_QR | 支付宝二维码 
WX_BAR | 微信条码
WX_JSAPI | 微信公众号 
WX_LITE | 微信小程序 
WX_APP | 微信APP 
WX_H5 | 微信H5 
WX_NATIVE | 微信扫码
YSF_BAR | 云闪付条码 
YSF_JSAPI | 云闪付jsapi 

> channelExtra参数说明

当 `wayCode=AUTO_BAR` 或 `wayCode=ALI_BAR` 或  `wayCode=WX_BAR` 或  `wayCode=YSF_BAR` 时，channelExtra必须传auth_code，为用户的付款码值，channelExtra示例数据如：
```json
{"auth_code": "13920933111042"}
```

当 `wayCode=ALI_JSAPI` 时，channelExtra必须传buyerUserId，为支付宝用户ID，channelExtra示例数据如：
```json
{"buyerUserId": "2088702585070844"}
```

当 `wayCode=WX_JSAPI` 或 `wayCode=WX_LITE` 时，channelExtra必须传openid，channelExtra示例数据如：
```json
{"openid": "o6BcIwvSiRpfS8e_UyfQNrYuk2LI"}
```

当 `wayCode=QR_CASHIER` 或 `wayCode=ALI_QR` 或  `wayCode=WX_NATIVE` 时，channelExtra可以传payDataType设置返回支付数据支付类型。此时payDataType可以为：codeUrl-二维码地址,codeImgUrl-二维码图片地址，不传payDataType默认返回codeUrl类型, channelExtra示例数据如：
```json
{"payDataType": "codeImgUrl"}
```

当 `wayCode=ALI_WAP` 时，channelExtra可以传payDataType设置返回支付数据支付类型。此时payDataType可以为：form-返回自动跳转的支付表单,codeImgUrl-返回一个二维码图片URL,payUrl-返回支付链接，不传payDataType默认返回payUrl类型, channelExtra示例数据如：
```json
{"payDataType": "form"}
```

当 `wayCode=ALI_PC` 时，channelExtra可以传payDataType设置返回支付数据支付类型。此时payDataType可以为：form-返回自动跳转的支付表单,payUrl-返回支付链接，不传payDataType默认返回payUrl类型, channelExtra示例数据如：
```json
{"payDataType": "form"}
```


## 查询订单

商户通过该接口查询订单，支付网关会返回订单最新的数据

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：https://pay.jeepay.vip/api/pay/query

请求方式：`POST`

请求类型：`application/json` 或 `application/x-www-form-urlencoded`

> 请求参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号 
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
支付订单号 | payOrderId | 是 | String(30) | P20160427210604000490 | 支付中心生成的订单号，与mchOrderNo二者传一即可  
商户订单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 商户生成的订单号，与payOrderId二者传一即可  
请求时间 | reqTime | 是 | long | 1622016572190 | 请求接口时间,13位时间戳  
接口版本 | version | 是 | String(3) | 1.0 | 接口版本号，固定：1.0  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  
签名类型 | signType | 是 | String(32) | MD5 | 签名类型，目前只支持MD5方式 

`请求示例数据`

```json
{
  "payOrderId": "P202106181104177050002",
  "appId": "60cc09bce4b0f1c0b83761c9",
  "sign": "46940C58B2F3AE426B77A297ABF4D31E",
  "signType": "MD5",
  "reqTime": "1624006009",
  "mchNo": "M1623984572",
  "version": "1.0"
}
```

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
返回状态 | code | 是 | int | 0 | 0-处理成功，其他-处理有误，详见错误码  
返回信息 | msg | 否 | String(128) | 签名失败 | 具体错误原因，例如：签名失败、参数格式校验错误  
签名信息 | sign | 否 | String(32) | CCD9083A6DAD9A2DA9F668C3D4517A84 | 对data内数据签名,如data为空则不返回
返回数据 | data | 否 | String(512) | {} | 返回下单数据,json格式数据  

`data数据格式`

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
支付订单号 | payOrderId | 是 | String(30) | P12021022311124442600 | 返回支付系统订单号
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号   
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
商户订单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 返回商户传入的订单号  
支付接口 | ifCode | 是 | String(30) | wxpay | 支付接口编码   
支付方式 | wayCode | 是 | String(30) | WX_LITE | 支付方式,如微信小程序WX_LITE   
支付金额 | amount | 是 | int | 100 | 支付金额,单位分  
货币代码 | currency | 是 | String(3) | cny | 三位货币代码,人民币:cny  
订单状态 | state | 是 | int | 2 | 支付订单状态<br>0-订单生成<br>1-支付中<br>2-支付成功<br>3-支付失败<br>4-已撤销<br>5-已退款<br>6-订单关闭  
客户端IP | clientIp | 否 | String(32) | 210.73.10.148 | 客户端IPV4地址  
商品标题 | subject | 是 | String(64) | Jeepay商品标题测试 | 商品标题  
商品描述 | body | 是 | String(256) | Jeepay商品描述测试 | 商品描述  
渠道订单号 | channelOrderNo | 否 | String | 20160427210604000490 | 对应渠道的订单号  
渠道错误码 | errCode | 否 | String | 1002 | 渠道下单返回错误码
渠道错误描述 | errMsg | 否 | String | 业务异常错误 | 渠道下单返回错误描述
扩展参数 | extParam | 否 | String(512) | 134586944573118714 | 商户扩展参数,回调时会原样返回  
创建时间 | createdAt | 是 | long | 1622016572190 | 订单创建时间,13位时间戳  
成功时间 | successTime | 否 | long | 1622016572190 | 订单支付成功时间,13位时间戳  

`返回示例数据`

```json
{
  "code": 0,
  "data": {
    "amount": 58,
    "appId": "60cc09bce4b0f1c0b83761c9",
    "body": "商品描述",
    "channelOrderNo": "2021061822001423031419593035",
    "clientIp": "192.166.1.132",
    "createdAt": 1623985457705,
    "currency": "cny",
    "extParam": "",
    "ifCode": "alipay",
    "mchNo": "M1623984572",
    "mchOrderNo": "mho1623985457320",
    "payOrderId": "P202106181104177050002",
    "state": 2,
    "subject": "商品标题",
    "successTime": 1623985459000,
    "wayCode": "ALI_BAR"
  },
  "msg": "SUCCESS",
  "sign": "9548145EA12D0CD8C1628BCF44E19E0D"
}
```

## 关闭订单

商户通过该接口关闭订单，支付网关会对订单完成关闭处理。

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：https://pay.jeepay.vip/api/pay/close

请求方式：`POST`

请求类型：`application/json` 或 `application/x-www-form-urlencoded`

> 请求参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号 
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
支付订单号 | payOrderId | 是 | String(30) | P20160427210604000490 | 支付中心生成的订单号，与mchOrderNo二者传一即可  
商户订单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 商户生成的订单号，与payOrderId二者传一即可  
请求时间 | reqTime | 是 | long | 1622016572190 | 请求接口时间,13位时间戳  
接口版本 | version | 是 | String(3) | 1.0 | 接口版本号，固定：1.0  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  
签名类型 | signType | 是 | String(32) | MD5 | 签名类型，目前只支持MD5方式 

`请求示例数据`

```json
{
  "payOrderId": "P202106181104177050002",
  "appId": "60cc09bce4b0f1c0b83761c9",
  "sign": "46940C58B2F3AE426B77A297ABF4D31E",
  "signType": "MD5",
  "reqTime": "1624006009",
  "mchNo": "M1623984572",
  "version": "1.0"
}
```

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
返回状态 | code | 是 | int | 0 | 0-处理成功，其他-处理有误，详见错误码  
返回信息 | msg | 否 | String(128) | 签名失败 | 具体错误原因，例如：签名失败、参数格式校验错误  
签名信息 | sign | 否 | String(32) | CCD9083A6DAD9A2DA9F668C3D4517A84 | 对data内数据签名,如data为空则不返回
返回数据 | data | 否 | String(512) | {} | 返回下单数据,json格式数据  

`data数据格式`

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
渠道错误码 | errCode | 否 | String | 1002 | 渠道关单返回错误码
渠道错误描述 | errMsg | 否 | String | 关闭异常 | 渠道关单返回错误描述

`返回示例数据`

```json
{
  "code": 0,
  "data": {
    "errCode": '',
    "errMsg": ''
  },
  "msg": "SUCCESS",
  "sign": "9548145EA12D0CD8C1628BCF44E19E0D"
}
```

## 支付通知

当订单支付成功时，支付网关会向商户系统发起回调通知。如果商户系统没有正确返回，支付网关会延迟再次通知。

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：该链接是通过统一下单接口提交的参数notifyUrl设置，如果无法访问链接，商户系统将无法接收到支付中心的通知。

请求方式：`POST`

请求类型：`application/x-www-form-urlencoded`

> 通知参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
支付订单号 | payOrderId | 是 | String(30) | P12021022311124442600 | 返回支付系统订单号
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号   
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
商户订单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 返回商户传入的订单号  
支付接口 | ifCode | 是 | String(30) | wxpay | 支付接口编码   
支付方式 | wayCode | 是 | String(30) | WX_LITE | 支付方式,微信小程序WX_LITE   
支付金额 | amount | 是 | int | 100 | 支付金额,单位分  
货币代码 | currency | 是 | String(3) | cny | 三位货币代码,人民币:cny  
订单状态 | state | 是 | int | 2 | 支付订单状态<br>0-订单生成<br>1-支付中<br>2-支付成功<br>3-支付失败<br>4-已撤销<br>5-已退款<br>6-订单关闭  
客户端IP | clientIp | 否 | String(32) | 210.73.10.148 | 客户端IPV4地址  
商品标题 | subject | 是 | String(64) | Jeepay商品标题测试 | 商品标题  
商品描述 | body | 是 | String(256) | Jeepay商品描述测试 | 商品描述  
渠道订单号 | channelOrderNo | 否 | String | 20160427210604000490 | 对应渠道的订单号  
渠道错误码 | errCode | 否 | String | 1002 | 渠道下单返回错误码
渠道错误描述 | errMsg | 否 | String | 134586944573118714 | 渠道下单返回错误描述
扩展参数 | extParam | 否 | String(512) | 134586944573118714 | 商户扩展参数
创建时间 | createdAt | 是 | long | 1622016572190 | 订单创建时间,13位时间戳  
成功时间 | successTime | 否 | long | 1622016572190 | 订单支付成功时间,13位时间戳  
通知请求时间 | reqTime | 是 | String(30) | 1622016572190 | 通知请求时间，,13位时间戳  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  

&gt; 返回结果

业务系统处理后同步返回给支付中心，返回字符串 success 则表示成功，返回非success则表示处理失败，支付中心会再次通知业务系统。（通知频率为0/30/60/90/120/150,单位：秒）

`注意：返回的字符串必须是小写，且前后不能有空格和换行符。`

`通知示例数据`

```json
{
    "amount": 5,
    "body": "商品描述",
    "clientIp": "192.166.1.132",
    "createdAt": "1622016572190",
    "currency": "cny",
    "extParam": "",
    "ifCode": "wxpay",
    "mchNo": "M1621873433953",
    "appId": "60cc09bce4b0f1c0b83761c9",
    "mchOrderNo": "mho1621934803068",
    "payOrderId": "20210525172643357010",
    "state": 3,
    "subject": "商品标题",
    "wayCode": "WX_BAR",
    "sign": "C380BEC2BFD727A4B6845133519F3AD6"
}
```

## 获取渠道用户ID

商户通过该接口获取渠道的用户ID，如微信的openID，支付宝的userId。该接口通过跳转获取到用户ID后，会跳转到商户上传的redirectUrl地址，并传递用户ID参数

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：https://pay.jeepay.vip/api/channelUserId/jump

请求方式：`GET`

请求类型：`application/json` 或 `application/x-www-form-urlencoded`

> 请求参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号 
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
支付接口 | ifCode | 是 | String(30) | AUTO | 目前只支持传 AUTO   
跳转地址 | redirectUrl | 是 | String | https://www.jeequan.com | 获取到用户ID后，会携带用户ID参数跳转到该地址  
请求时间 | reqTime | 是 | long | 1622016572190 | 请求接口时间,13位时间戳  
接口版本 | version | 是 | String(3) | 1.0 | 接口版本号，固定：1.0  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  
签名类型 | signType | 是 | String(32) | MD5 | 签名类型，目前只支持MD5方式 

`请求示例数据`

```json
{
  "mchNo": "M1621873433953",
  "appId": "60cc09bce4b0f1c0b83761c9",
  "ifCode": "wxpay",
  "redirectUrl": "https://www.jeequan.com",
  "sign": "A5C93D50743126ED91AA6ED96CDEEEF8",
  "signType": "MD5",
  "reqTime": "1622011236571",
  "version": "1.0"
}
```

`当获取到渠道用户ID后，会301重定向到跳转地址，传递参数如下`

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
渠道用户ID | channelUserId | 是 | String | o6BcIwvSiRpfS8e_UyfQNrYuk2LI | 渠道用户ID，微信openId或支付宝userId

`完整跳转URL示例`

```html
https://www.jeequan.com/toU?channelUserId=o6BcIwvSiRpfS8e_UyfQNrYuk2LI
```

## 返回码

code | 描述 
------- | -------
0 | 成功 
9999 | 异常，具体错误详见msg字段