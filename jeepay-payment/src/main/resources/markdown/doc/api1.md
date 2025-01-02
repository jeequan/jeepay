# 接口规则

## 协议规则
传输方式：采用HTTP传输(生产环境建议HTTPS)   
提交方式：`POST` 或 `GET`   
内容类型：`application/json`   
字符编码：`UTF-8`   
签名算法：`MD5`   

## 参数规范
交易金额：默认为人民币交易，单位为分，参数值不能带小数。   
时间参数：所有涉及时间参数均使用精确到毫秒的13位数值，如：1622016572190。时间戳具体是指从格林尼治时间1970年01月01日00时00分00秒起至现在的毫秒数。

## 签名算法
`签名生成的通用步骤如下`

***第一步：*** 设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），使用URL键值对的格式（即key1=value1&amp;key2=value2…）拼接成字符串stringA。
特别注意以下重要规则：   
◆ 参数名ASCII码从小到大排序（字典序）；   
◆ 如果参数的值为空不参与签名；   
◆ 参数名区分大小写；   
◆ 验证调用返回或支付中心主动通知签名时，传送的sign参数不参与签名，将生成的签名与该sign值作校验。   
◆ 支付中心接口可能增加字段，验证签名时必须支持增加的扩展字段

***第二步：*** 在stringA最后拼接上key`[即 StringA +"&key=" + 私钥 ]` 得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，再将得到的字符串所有字符转换为大写，得到sign值signValue。

如请求支付系统参数如下：
```java
Map signMap = new HashMap<>();
signMap.put("platId", "1000");
signMap.put("mchOrderNo", "P0123456789101");
signMap.put("amount", "10000");
signMap.put("clientIp", "192.168.0.111");
signMap.put("returnUrl", "https://www.baidu.com");
signMap.put("notifyUrl", "https://www.baidu.com");
signMap.put("reqTime", "20190723141000");
signMap.put("version", "1.0");
```
`待签名值`： 
amount=10000&amp;clientIp=192.168.0.111&amp;mchOrderNo=P0123456789101&amp;notifyUrl=https://www.baidu.com&amp;platId=1000&amp;reqTime=20190723141000&amp;returnUrl=https://www.baidu.com&amp;version=1.0&amp;key=EWEFD123RGSRETYDFNGFGFGSHDFGH

`签名结果`：4A5078DABBCE0D9C4E7668DACB96FF7A 

`最终请求支付系统参数`：amount=10000&amp;clientIp=192.168.0.111&amp;mchOrderNo=P0123456789101&amp;notifyUrl=https://www.baidu.com&amp;platId=1000&reqTime=20190723141000&amp;returnUrl=https://www.baidu.com&amp;version=1.0&amp;sign=4A5078DABBCE0D9C4E7668DACB96FF7A

&gt; 运营管理平台可以管理商户的私钥

`提示：`签名以及接口调用，请使用jeepay官方提供的sdk：https://gitee.com/jeequan/jeepay-sdk-java