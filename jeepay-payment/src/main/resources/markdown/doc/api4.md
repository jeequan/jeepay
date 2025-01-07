# 转账接口

## 发起转账

商户业务系统通过转账接口发起转账申请，Jeepay支付网关将根据请求数据传入到对应的上游接口。

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：https://pay.jeepay.vip/api/transferOrder

请求方式：`POST`

请求类型：`application/json` 或 `application/x-www-form-urlencoded`

> 请求参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号   
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
商户订单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 商户生成的转账订单号
接口代码 | ifCode | 是 | String(10) | wxpay | wxpay-微信官方接口 ; alipay-支付宝官方接口
入账方式 | entryType | 是 | String(20) | 20160427210604000490 | 入账方式： WX_CASH-微信零钱; ALIPAY_CASH-支付宝转账; BANK_CARD-银行卡
转账金额 | amount | 是 | int | 100 | 转账金额,单位分   
货币代码 | currency | 是 | String(3) | cny | 三位货币代码,人民币:cny
收款账号 | accountNo | 是 | String(64) | o6BcIwvTvIqf1zXZohc61biryWik | wxpay-openID, alipay-登录账号  
收款人姓名 | accountName | 否 | String(64) | 张三 | 填入则验证姓名，否则不验证
收款人开户行名称 | bankName | 否 | String(64) | 中国工商银行 | 当前仅作为记录
客户端IP | clientIp | 否 | String(32) | 210.73.10.148 | 客户端IPV4地址  
转账备注信息 | transferDesc | 否 | String(128) | 测试转账 | 转账备注信息
异步通知地址 | notifyUrl | 否 | String(128) | https://www.jeequan.com/notify.htm | 转账完成后回调该URL,只有传了该值才会发起回调
渠道参数 | channelExtra | 否 | String(256 | {} | 特定渠道发起的额外参数,json格式字符串
扩展参数 | extraParam | 否 | String(512) | 134586944573118714 | 商户扩展参数,回调时会原样返回  
请求时间 | reqTime | 是 | long | 1622016572190 | 请求接口时间,13位时间戳   
接口版本 | version | 是 | String(3) | 1.0 | 接口版本号，固定：1.0  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  
签名类型 | signType | 是 | String(32) | MD5 | 签名类型，目前只支持MD5方式  


`请求示例数据`

```json
{"ifCode":"wxpay",
"entryType":"WX_CASH",
"amount":1,
"accountName":"",
"mchOrderNo":"mho1629106169045",
"sign":"3EB5A3B81E92DB41677E235363E7DDE3",
"transferDesc":"测试转账",
"reqTime":"1629106169",
"version":"1.0",
"appId":"60cc3ba74ee0e6685f57eb1e",
"accountNo":"a6BcIwtTvIqv1zXZohc61biryWok",
"clientIp":"192.166.1.132",
"signType":"MD5",
"currency":"CNY",
"mchNo":"M1623997351"
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
转账订单号 | transferId | 是 | String(30) | T202108161731281310004 | 返回转账订单号  
商户转账单号 | mchOrderNo | 是 | String(30) | mho1624007315478 | 返回商户传入的转账单号  
转账状态 | state | 是 | int | 2 | 转账状态<br>0-订单生成<br>1-转账中<br>2-转账成功<br>3-转账失败<br>4-转账关闭  
渠道转账单号 | channelOrderNo | 否 | String | 20160427210604000490 | 对应渠道的转账单号   
渠道错误码 | errCode | 否 | String | ACQ.PAYMENT_AUTH_CODE_INVALID | 上游渠道返回的错误码
渠道错误描述 | errMsg | 否 | String | Business Failed 失败 | 上游渠道返回的错误描述

`返回示例数据`

```json
{
    "code": 0,
    "data": {
        "accountNo": "1",
        "amount": 11,
        "channelOrderNo": "20210816110070001506260000372216",
        "mchOrderNo": "1629106288",
        "state": 2,
        "transferId": "T202108161731281310004"
    },
    "msg": "SUCCESS",
    "sign": "195BF6F112386F7FC8EA2AA7EECA1D33"
}
```

## 查询订单

商户通过该接口查询转账订单，支付网关会返回订单最新的数据

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：https://pay.jeepay.vip/api/transfer/query

请求方式：`POST`

请求类型：`application/json` 或 `application/x-www-form-urlencoded`

> 请求参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号 
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
转账订单号 | transferId | 是 | String(30) | T20160427210604000490 | 支付中心生成的转账单号，与mchOrderNo二者传一即可  
商户转账单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 商户生成的转账单号，与transferId二者传一即可  
请求时间 | reqTime | 是 | long | 1622016572190 | 请求接口时间,13位时间戳  
接口版本 | version | 是 | String(3) | 1.0 | 接口版本号，固定：1.0  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  
签名类型 | signType | 是 | String(32) | MD5 | 签名类型，目前只支持MD5方式 

`请求示例数据`

```json
{
"appId":"60cc3ba74ee0e6685f57eb1e",
"sign":"D3C0CC231F3FC3D033650699BA099B39",
"signType":"MD5",
"reqTime":"1629106457",
"transferId":"T202108121543441860003",
"mchNo":"M1623997351",
"version":"1.0"
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
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号   
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
商户订单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 商户生成的转账订单号
转账订单号 | transferId | 是 | String(30) | T20160427210604000490 | 支付中心生成的转账单号
转账金额 | amount | 是 | int | 100 | 转账金额,单位分   
货币代码 | currency | 是 | String(3) | cny | 三位货币代码,人民币:cny
接口代码 | ifCode | 是 | String(10) | wxpay | wxpay-微信官方接口 ; alipay-支付宝官方接口
入账方式 | entryType | 是 | String(20) | 20160427210604000490 | 入账方式： WX_CASH-微信零钱; ALIPAY_CASH-支付宝转账; BANK_CARD-银行卡
转账状态 | state | 是 | int | 2 | 转账状态<br>0-订单生成<br>1-转账中<br>2-转账成功<br>3-转账失败<br>4-转账关闭 
收款账号 | accountNo | 是 | String(64) | o6BcIwvTvIqf1zXZohc61biryWik | wxpay-openID, alipay-登录账号  
收款人姓名 | accountName | 否 | String(64) | 张三 | 填入则验证姓名，否则不验证
收款人开户行名称 | bankName | 否 | String(64) | 中国工商银行 | 当前仅作为记录
转账备注信息 | transferDesc | 否 | String(128) | 测试转账 | 转账备注信息
渠道转账单号 | channelOrderNo | 否 | String | 20160427210604000490 | 对应渠道的转账单号 
渠道错误码 | errCode | 否 | String | 1002 | 渠道返回错误码
渠道错误描述 | errMsg | 否 | String | 134586944573118714 | 渠道返回错误描述
扩展参数 | extraParam | 否 | String(512) | 134586944573118714 | 商户扩展参数,回调时会原样返回  
创建时间 | createdAt | 是 | long | 1622016572190 | 订单创建时间,13位时间戳  
成功时间 | successTime | 否 | long | 1622016572190 | 转账成功时间,13位时间戳  


`返回示例数据`

```json
{
    "code": 0,
    "data": {
        "accountNo": "o6BcIwvTvIqf1zXZohc61biryWik",
        "amount": 1,
        "appId": "6113805e42020495c62bd4cb",
        "createdAt": 1628818820011,
        "currency": "CNY",
        "entryType": "WX_CASH",
        "errCode": "OPENID_ERROR",
        "errMsg": "openid与商户appid不匹配【openid与商户appid不匹配】",
        "ifCode": "wxpay",
        "mchNo": "M1623997351",
        "mchOrderNo": "1628818820",
        "state": 3,
        "transferDesc": "测试",
        "transferId": "T202108130940200100001"
    },
    "msg": "SUCCESS",
    "sign": "A262DBD3D6182E8A0AEC90EF820F2A5A"
}
```

## 转账通知

当转账完成时(成功或失败)，支付网关会向商户系统发起回调通知。如果商户系统没有正确返回，支付网关会延迟再次通知。

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：该链接是通过转账申请接口提交的参数notifyUrl设置，如果无法访问链接，商户系统将无法接收到支付中心的通知。

请求方式：`POST`

请求类型：`application/x-www-form-urlencoded`

> 通知参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号   
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
商户订单号 | mchOrderNo | 是 | String(30) | 20160427210604000490 | 商户生成的转账订单号
转账订单号 | transferId | 是 | String(30) | T20160427210604000490 | 支付中心生成的转账单号
转账金额 | amount | 是 | int | 100 | 转账金额,单位分   
货币代码 | currency | 是 | String(3) | cny | 三位货币代码,人民币:cny
接口代码 | ifCode | 是 | String(10) | wxpay | wxpay-微信官方接口 ; alipay-支付宝官方接口
入账方式 | entryType | 是 | String(20) | 20160427210604000490 | 入账方式： WX_CASH-微信零钱; ALIPAY_CASH-支付宝转账; BANK_CARD-银行卡
转账状态 | state | 是 | int | 2 | 转账状态<br>0-订单生成<br>1-转账中<br>2-转账成功<br>3-转账失败<br>4-转账关闭 
收款账号 | accountNo | 是 | String(64) | o6BcIwvTvIqf1zXZohc61biryWik | wxpay-openID, alipay-登录账号  
收款人姓名 | accountName | 否 | String(64) | 张三 | 填入则验证姓名，否则不验证
收款人开户行名称 | bankName | 否 | String(64) | 中国工商银行 | 当前仅作为记录
转账备注信息 | transferDesc | 否 | String(128) | 测试转账 | 转账备注信息
渠道转账单号 | channelOrderNo | 否 | String | 20160427210604000490 | 对应渠道的转账单号 
渠道错误码 | errCode | 否 | String | 1002 | 渠道返回错误码
渠道错误描述 | errMsg | 否 | String | 134586944573118714 | 渠道返回错误描述
扩展参数 | extraParam | 否 | String(512) | 134586944573118714 | 商户扩展参数,回调时会原样返回  
创建时间 | createdAt | 是 | long | 1622016572190 | 订单创建时间,13位时间戳  
成功时间 | successTime | 否 | long | 1622016572190 | 转账成功时间,13位时间戳  

&gt; 返回结果

业务系统处理后同步返回给支付中心，返回字符串 success 则表示成功，返回非success则表示处理失败，支付中心会再次通知业务系统。（通知频率为0/30/60/90/120/150,单位：秒）

`注意：返回的字符串必须是小写，且前后不能有空格和换行符。`

`通知示例数据`

```json
 {
        "accountNo": "o6BcIwvTvIqf1zXZohc61biryWik",
        "amount": 1,
        "appId": "6113805e42020495c62bd4cb",
        "createdAt": 1628818820011,
        "currency": "CNY",
        "entryType": "WX_CASH",
        "errCode": "OPENID_ERROR",
        "errMsg": "openid与商户appid不匹配【openid与商户appid不匹配】",
        "ifCode": "wxpay",
        "mchNo": "M1623997351",
        "mchOrderNo": "1628818820",
        "state": 3,
        "transferDesc": "测试",
        "transferId": "T202108130940200100001"
    }
```