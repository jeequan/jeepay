# 分账接口

## 分账业务

> 业务介绍：商户分账主要用于商户将交易成功的资金，按照一定的周期，分账给其他方，可以是合作伙伴、员工、用户或者其他分润方。

参考微信文档： 
![](/uploads/jeepay/images/m_59fa8b27a5c30a556210fc3d3bd1ad14_r.png)


微信分账：https://pay.weixin.qq.com/wiki/doc/api/allocation.php?chapter=26_1
支付宝分账： https://opendocs.alipay.com/open/20190308105425129272/intro


接口目录： 

## 绑定分账用户

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：https://pay.jeepay.vip/api/division/receiver/bind

请求方式：`POST`

请求类型：`application/json` 或 `application/x-www-form-urlencoded`

> 请求参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号   
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
接口代码 | ifCode | 是 | String(10) | wxpay | wxpay-微信官方接口 ; alipay-支付宝官方接口
接收者账号别名 | receiverAlias | 是 | String(64) | 张三 | 接收者账号别名
组ID | receiverGroupId | 是 | long | 10001 | 需先登录商户系统查找待加入的组ID
分账接收账号类型 | accType | 是 | int | 1 | 分账接收账号类型: 0-个人(对私) 1-商户(对公)
分账接收账号 | accNo | 是 | String(10) | 1231312@qq.com | 分账接收账号, 微信个人是openid, 支付宝可以是userId或登录名
分账接收账号名称 | accName | 否 | String(30) | 张三 | 微信选填（当填入则验证），支付宝账号必填
分账关系类型 | relationType | 是 | String(30) | wxpay | 分账关系类型：<br/>SERVICE_PROVIDER：服务商 <br/>STORE：门店<br/>STAFF：员工<br/>STORE_OWNER：店主<br/>PARTNER：合作伙伴<br/>HEADQUARTER：总部<br/>BRAND：品牌方<br/>DISTRIBUTOR：分销商<br/>USER：用户<br/>SUPPLIER：供应商<br/>CUSTOM：自定义
分账关系类型名称 | relationTypeName | 否 | String(30) | wxpay | 当relationType=CUSTOM 必填
渠道特殊信息 | channelExtInfo | 否 | String(256) | wxpay | 渠道特殊信息
默认分账比例 | divisionProfit | 是 | String(10) | wxpay | 若分账30% 则填入 0.3
请求时间 | reqTime | 是 | long | 1622016572190 | 请求接口时间,13位时间戳   
接口版本 | version | 是 | String(3) | 1.0 | 接口版本号，固定：1.0  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  
签名类型 | signType | 是 | String(32) | MD5 | 签名类型，目前只支持MD5方式  


`请求示例数据`

```json
{
 'version': '1.0',
'reqTime': '1622016572190',
'signType': 'MD5',
'sign': 'MD5MD5MD5MD5MD5MD5MD5MD5MD5MD5MD5MD5',
'mchNo': 'M1623997000',
'appId': '60cc3ba74ee0e6685f57e000',
'ifCode': 'wxpay',
'receiverAlias': '我的第一个账号',
'receiverGroupId': '100001',
'accType': '0',
'accNo': 'sfsfsd@qq.com',
'accName': '张三',
'relationType': 'OTHERS',
'relationTypeName': '我的员工',
'divisionProfit': '0.3' 
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
绑定账号ID | receiverId | 是 | long | 10001 | 绑定账号ID, 订单分账将使用该ID
接收者账号别名 | receiverAlias | 是 | String(64) | 张三 | 接收者账号别名
组ID | receiverGroupId | 是 | long | 10001 | 组ID
分账接收账号类型 | accType | 是 | int | 1 | 分账接收账号类型: 0-个人(对私) 1-商户(对公)
分账接收账号 | accNo | 是 | String(10) | 1231312@qq.com | 分账接收账号
分账接收账号名称 | accName | 否 | String(30) | 张三 | 分账接收账号名称
分账关系类型 | relationType | 是 | String(30) | wxpay | 分账关系类型
渠道特殊信息 | channelExtInfo | 否 | String(256) | wxpay | 渠道特殊信息
默认分账比例 | divisionProfit | 是 | String(10) | wxpay | 默认分账比例
绑定成功时间 | bindSuccessTime | 是 | Long | 1622016572190 | 绑定成功时间
绑定状态 | bindState | 是 | int | 1 | 绑定状态 1-绑定成功, 0-绑定异常 
渠道错误码 | errCode | 否 | String | ACQ.PAYMENT_AUTH_CODE_INVALID | 上游渠道返回的错误码
渠道错误描述 | errMsg | 否 | String | Business Failed 失败 | 上游渠道返回的错误描述

`返回示例数据`

```json
{
    "code": 0,
    "data": {
        "accName": "张三",
        "accNo": "sfsfsd@qq.com",
        "accType": 0,
        "appId": "60cc3ba74ee0e6685f57eb1e",
        "bindState": 0,
        "divisionProfit": 0.3,
        "errCode": "NOAUTH",
        "errMsg": "无分账权限",
        "ifCode": "wxpay",
        "mchNo": "M1623997351",
        "receiverAlias": "我的第一个账号",
        "receiverGroupId": 100001,
        "relationType": "OTHERS",
        "relationTypeName": "我的员工"
    },
    "msg": "SUCCESS",
    "sign": "552CB91FA1E1DB378A534B377E4E9403"
}
```

## 发起订单分账

当订单下单时传入的分账模式  divisionMode = 2商户手动分账(解冻商户金额)，支持商户手动发起订单分账。<br>注意：需要在订单支付完成后（建议1分钟后）调用分账接口。 

> 接口说明

适用对象：`普通商户` `特约商户`

请求URL：https://pay.jeepay.vip/api/division/exec

请求方式：`POST`

请求类型：`application/json` 或 `application/x-www-form-urlencoded`

> 请求参数

字段名 | 变量名 | 必填 | 类型 | 示例值 | 描述
------- | -------| -------| -------| -------| -------
商户号 | mchNo | 是 | String(30) | M1621873433953 | 商户号 
应用ID | appId | 是 | String(24) | 60cc09bce4b0f1c0b83761c9 | 应用ID
支付订单号 | payOrderId | 否 | String(30) | P20160427210604000490 | 支付中心生成的支付订单号，与mchOrderNo二者传一即可  
商户单号 | mchOrderNo | 否 | String(30) | 20160427210604000490 | 商户生成的支付单号，与payOrderId二者传一即可  
是否使用系统配置的自动分账组 | useSysAutoDivisionReceivers | 是 | int | 1 | 是否使用系统配置的自动分账组： 0-否 1-是
分账接收者账号列表 | receivers | 否 | String(512) | [] |  接收者账号列表（JSONArray 转换为字符串类型）<br/>仅当useSysAutoDivisionReceivers=0 时该字段值有效。<br/>参考：<br/>方式1： 按账号纬度<br/>[{<br/>receiverId: 800001,<br/>divisionProfit: 0.1 (若不填入则使用系统默认配置值)<br/>}]<br/>方式2： 按组纬度<br/>[{<br/>receiverGroupId: 100001, (该组所有 当前订单的渠道账号并且可用状态的全部参与分账)<br/>divisionProfit: 0.1 (每个账号的分账比例， 若不填入则使用系统默认配置值， 建议不填写)<br/>}]
请求时间 | reqTime | 是 | long | 1622016572190 | 请求接口时间,13位时间戳  
接口版本 | version | 是 | String(3) | 1.0 | 接口版本号，固定：1.0  
签名 | sign | 是 | String(32) | C380BEC2BFD727A4B6845133519F3AD6 | 签名值，详见签名算法  
签名类型 | signType | 是 | String(32) | MD5 | 签名类型，目前只支持MD5方式 

`请求示例数据`

```json
{
 'version': '1.0',
'reqTime': '1622016572190',
'signType': 'MD5',
'sign': '1',
'mchNo': 'M1623997351',
'appId': '60cc3ba74ee0e6685f57eb1e',
'payOrderId': 'P202108271011463510002',
'useSysAutoDivisionReceivers': '0',
'receivers': '[{"receiverGroupId":"","receiverId":"800029","divisionProfit":"0.0001"},{"receiverGroupId":"","receiverId":"800028","divisionProfit":"0.0002"}]' 
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
分账状态 | state | 是 | int | 2 | 分账状态 1-分账成功, 2-分账失败
上游分账批次号 | channelBatchOrderId | 否 | String(30) | T20160427210604000490 | 上游分账批次号
渠道错误码 | errCode | 否 | String | 1002 | 渠道返回错误码
渠道错误描述 | errMsg | 否 | String | ERROR | 渠道返回错误描述


`返回示例数据`

```json
{
    "code": 0,
    "data": {
        "errCode": "unknown-sub-code",
        "errMsg": "Business Failed【未知的错误码ACQ.ROYALTY_ACCOUNT_NOT_EXIST】",
        "state": 2
    },
    "msg": "SUCCESS",
    "sign": "56836E18015DD7E4FAFE45380C0AD098"
}
```