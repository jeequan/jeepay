# 退款接口

## 统一退款

商户业务系统通过统一退款接口发起退款请求，Jeepay支付网关会根据商户发起的支付订单号，找到对应到支付通道发起退款。

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：https://pay.jeepay.vip/api/refund/refundOrder

请求方式：`POST`

请求类型：`application/json` 或 `application/x-www-form-urlencoded`

> 请求参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号   
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
支付订单号 | payOrderId | 是 | String(30) | P20160427210604000490 | 支付中心生成的订单号，与mchOrderNo二者传一即可  
商户订单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 商户生成的支付订单号，与payOrderId二者传一即可  
商户退款单号 | mchRefundNo | 是 | String(30) | M27210632100491 | 商户生成的退款单号  
退款金额 | refundAmount | 是 | int | 100 | 退款金额,单位分   
货币代码 | currency | 是 | String(3) | cny | 三位货币代码,人民币:cny
退款原因 | refundReason | 是 | String(64) | 用户退货 | 退款原因  
客户端IP | clientIp | 否 | String(32) | 210.73.10.148 | 客户端IPV4地址  
异步通知地址 | notifyUrl | 否 | String(128) | https://www.jeequan.com/notify.htm | 退款完成后回调该URL,只有传了该值才会发起回调  
渠道参数 | channelExtra | 否 | String(256 | {"auth_code", "13920933111042"} | 特定渠道发起的额外参数,json格式字符串.详见渠道参数说明  
扩展参数 | extraParam | 否 | String(512) | 134586944573118714 | 商户扩展参数,回调时会原样返回  
请求时间 | reqTime | 是 | long | 1622016572190 | 请求接口时间,13位时间戳   
接口版本 | version | 是 | String(3) | 1.0 | 接口版本号，固定：1.0  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  
签名类型 | signType | 是 | String(32) | MD5 | 签名类型，目前只支持MD5方式  


`请求示例数据`

```json
{
  "payOrderId": "P202106181104177050002",
  "extParam": "",
  "mchOrderNo": "",
  "refundReason": "退款测试",
  "sign": "2762CDB48D5179281DB6C0995E4EEDE0",
  "reqTime": "1624007315",
  "version": "1.0",
  "channelExtra": "",
  "appId": "60cc09bce4b0f1c0b83761c9",
  "mchRefundNo": "mho1624007315478",
  "clientIp": "192.166.1.132",
  "notifyUrl": "https://www.jeequan.com",
  "signType": "MD5",
  "currency": "cny",
  "mchNo": "M1623984572",
  "refundAmount": 4
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
退款订单号 | refundOrderId | 是 | String(30) | R202106181708358940000 | 返回退款订单号  
商户退款单号 | mchRefundNo | 是 | String(30) | mho1624007315478 | 返回商户传入的退款单号  
退款状态 | state | 是 | int | 2 | 退款状态<br>0-订单生成<br>1-退款中<br>2-退款成功<br>3-退款失败<br>4-退款关闭  
渠道退款单号 | channelOrderNo | 否 | String | 20160427210604000490 | 对应渠道的退款单号   
渠道错误码 | errCode | 否 | String | ACQ.PAYMENT_AUTH_CODE_INVALID | 上游渠道返回的错误码
渠道错误描述 | errMsg | 否 | String | Business Failed 失败 | 上游渠道返回的错误描述

`返回示例数据`

```json
{
  "code": 0,
  "data": {
    "channelOrderNo": "2021061822001423031419593035",
    "mchRefundNo": "mho1624007315478",
    "payAmount": 58,
    "refundAmount": 4,
    "refundOrderId": "R202106181708358940000",
    "state": 2
  },
  "msg": "SUCCESS",
  "sign": "2843B811B7A75D56B7D1950362820875"
}
```

## 查询订单

商户通过该接口查询退款订单，支付网关会返回订单最新的数据

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：https://pay.jeepay.vip/api/refund/query

请求方式：`POST`

请求类型：`application/json` 或 `application/x-www-form-urlencoded`

> 请求参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号 
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
退款订单号 | refundOrderId | 是 | String(30) | R20160427210604000490 | 支付中心生成的退款单号，与mchRefundNo二者传一即可  
商户退款单号 | mchRefundNo | 是 | String(30) | 20160427210604000490 | 商户生成的退款单号，与refundOrderId二者传一即可  
请求时间 | reqTime | 是 | long | 1622016572190 | 请求接口时间,13位时间戳  
接口版本 | version | 是 | String(3) | 1.0 | 接口版本号，固定：1.0  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  
签名类型 | signType | 是 | String(32) | MD5 | 签名类型，目前只支持MD5方式 

`请求示例数据`

```json
{
  "refundOrderId": "P202106181105527690009",
  "appId": "60cc09bce4b0f1c0b83761c9",
  "sign": "1484293FCAEAFE11DEC8949DB6B525A9",
  "signType": "MD5",
  "reqTime": "1624008199",
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
退款订单号 | refundOrderId | 是 | String(30) | R20160427210604000490 | 支付中心生成的退款单号  
支付订单号 | payOrderId | 是 | String(30) | P12021022311124442600 | 返回支付系统订单号
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号   
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
商户退款单号 | mchRefundNo | 是 | String(30) | 20160427210604000490 | 商户生成的退款单号
支付金额 | payAmount | 是 | int | 100 | 支付金额,单位分 
退款金额 | refundAmount | 是 | int | 100 | 退款金额,单位分 
货币代码 | currency | 是 | String(3) | cny | 三位货币代码,人民币:cny
退款状态 | state | 是 | int | 2 | 退款状态<br>0-订单生成<br>1-退款中<br>2-退款成功<br>3-退款失败<br>4-退款关闭  
渠道订单号 | channelOrderNo | 否 | String | 20160427210604000490 | 对应渠道的订单号  
渠道错误码 | errCode | 否 | String | 1002 | 渠道返回错误码
渠道错误描述 | errMsg | 否 | String | 134586944573118714 | 渠道返回错误描述
扩展参数 | extraParam | 否 | String(512) | 134586944573118714 | 商户扩展参数,回调时会原样返回  
创建时间 | createdAt | 是 | long | 1622016572190 | 订单创建时间,13位时间戳  
成功时间 | successTime | 否 | long | 1622016572190 | 订单支付成功时间,13位时间戳  

`返回示例数据`

```json
{
  "code": 0,
  "data": {
    "appId": "60cc09bce4b0f1c0b83761c9",
    "channelOrderNo": "2021061822001423031419593035",
    "createdAt": 1623985552769,
    "currency": "cny",
    "extParam": "",
    "mchNo": "M1623984572",
    "mchRefundNo": "mho1623985552430",
    "payAmount": 58,
    "payOrderId": "P202106181104177050002",
    "refundAmount": 4,
    "refundOrderId": "P202106181105527690009",
    "state": 2,
    "successTime": 1623985554000
  },
  "msg": "SUCCESS",
  "sign": "E3F9F008FC5EF84BD782CCC7BE69DC5E"
}
```

## 退款通知

当退款完成时(成功或失败)，支付网关会向商户系统发起回调通知。如果商户系统没有正确返回，支付网关会延迟再次通知。

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：该链接是通过统一退款接口提交的参数notifyUrl设置，如果无法访问链接，商户系统将无法接收到支付中心的通知。

请求方式：`POST`

请求类型：`application/x-www-form-urlencoded`

> 通知参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
退款订单号 | refundOrderId | 是 | String(30) | R20160427210604000490 | 支付中心生成的退款单号  
支付订单号 | payOrderId | 是 | String(30) | P12021022311124442600 | 返回支付系统订单号
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号   
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
商户退款单号 | mchRefundNo | 是 | String(30) | 20160427210604000490 | 商户生成的退款单号
支付金额 | payAmount | 是 | int | 100 | 支付金额,单位分 
退款金额 | refundAmount | 是 | int | 100 | 退款金额,单位分 
货币代码 | currency | 是 | String(3) | cny | 三位货币代码,人民币:cny
退款状态 | state | 是 | int | 2 | 退款状态<br>0-订单生成<br>1-退款中<br>2-退款成功<br>3-退款失败<br>4-退款关闭  
渠道订单号 | channelOrderNo | 否 | String | 20160427210604000490 | 对应渠道的订单号  
渠道错误码 | errCode | 否 | String | 1002 | 渠道返回错误码
渠道错误描述 | errMsg | 否 | String | 134586944573118714 | 渠道返回错误描述
扩展参数 | extraParam | 否 | String(512) | 134586944573118714 | 商户扩展参数,回调时会原样返回  
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
    "appId": "60cc09bce4b0f1c0b83761c9",
    "channelOrderNo": "2021061822001423031419593035",
    "createdAt": 1623985552769,
    "currency": "cny",
    "extParam": "",
    "mchNo": "M1623984572",
    "mchRefundNo": "mho1623985552430",
    "payAmount": 58,
    "payOrderId": "P202106181104177050002",
    "refundAmount": 4,
    "refundOrderId": "P202106181105527690009",
    "state": 2,
    "successTime": 1623985554000
}
```