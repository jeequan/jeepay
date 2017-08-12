package org.xxpay.service.channel.alipay.config;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2012-08-10
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。

 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.MyLog;
import org.xxpay.common.util.PropertiesFileUtil;

import java.util.Date;
import java.util.Map;

@RefreshScope
@Service
public class AlipayConfig {

    private static final MyLog _log = MyLog.getLog(AlipayConfig.class);

    public AlipayConfig init(String configParam) {
        Assert.notNull(configParam, "init alipay config error");
        JSONObject paramObj = JSON.parseObject(configParam);
        this.setAppId(paramObj.getString("appid"));
        this.setPartner(paramObj.getString("partner"));
        this.setAli_account(paramObj.getString("ali_account"));
        this.setPrivate_key(paramObj.getString("private_key"));
        return this;
    }

    //↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    // 合作身份者ID，以2088开头由16位纯数字组成的字符串
    public String partner = "";
    // 商户的私钥
    public String private_key = "";

    // 支付宝的公钥，无需修改该值
    public static String ali_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

    //↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    // 支付宝账号
    public String ali_account = "";

    // 字符编码格式 目前支持 gbk 或 utf-8
    public static String input_charset = "utf-8";

    // 签名方式 不需修改
    public static String sign_type = "RSA";

    // 提供给支付宝的通知地址
    @Value("${ali.notify_url}")
    private String notify_url;

    // 是否验证支付宝的通知地址
    private boolean is_verify_notify_url = true;

    // 支付宝即时到帐网关
    private String gateway = "https://mapi.alipay.com/gateway.do";

    // 支付宝新网关(查询等)
    private String openapiGateway = "https://openapi.alipay.com/gateway.do";

    // appid
    public String appId = "";

    // 配置加载时间
    private Long loadTime = new Date().getTime();

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getPrivate_key() {
        return private_key;
    }

    public void setPrivate_key(String private_key) {
        this.private_key = private_key;
    }

    public String getAli_account() {
        return ali_account;
    }

    public void setAli_account(String ali_account) {
        this.ali_account = ali_account;
    }

    public String getNotify_url() {
        return notify_url;
    }

    public void setNotify_url(String notify_url) {
        this.notify_url = notify_url;
    }

    public boolean is_verify_notify_url() {
        return is_verify_notify_url;
    }

    public void setIs_verify_notify_url(boolean is_verify_notify_url) {
        this.is_verify_notify_url = is_verify_notify_url;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getOpenapiGateway() {
        return openapiGateway;
    }

    public void setOpenapiGateway(String openapiGateway) {
        this.openapiGateway = openapiGateway;
    }

    public Long getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(Long loadTime) {
        this.loadTime = loadTime;
    }

    @Override
    public String toString() {
        return "AlipayConfig{" +
                "partner='" + partner + '\'' +
                ", private_key='" + private_key + '\'' +
                ", ali_account='" + ali_account + '\'' +
                ", notify_url='" + notify_url + '\'' +
                ", is_verify_notify_url='" + is_verify_notify_url + '\'' +
                ", gateway='" + gateway + '\'' +
                ", openapiGateway='" + openapiGateway + '\'' +
                ", appId='" + appId + '\'' +
                ", loadTime=" + loadTime +
                '}';
    }

}