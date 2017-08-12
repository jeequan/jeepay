/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2005 All Rights Reserved.
 */
package org.xxpay.service.channel.alipay.util.httpClient;

/* *
 *类名：HttpResultType
 *功能：表示Http返回的结果字符方式
 *详细：表示Http返回的结果字符方式
 *版本：3.3
 *日期：2012-08-17
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */
public enum HttpResultType {
    /**
     * 字符串方式
     */
    STRING,

    /**
     * 字节数组方式
     */
    BYTES
}
