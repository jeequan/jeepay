package org.xxpay.shop.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.xxpay.common.util.PayDigestUtil;
import org.xxpay.common.util.XXPayUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dingzhiwei on 16/5/5.
 */
public class PayOrderDemo {

    // 商户ID
    static final String mchId = "20001223";
    // 项目ID
    static final String payItemId = "7000001";
    // 加签key
    static final String reqKey = "M86l522AV6q613Ii4W6u8K48uW8vM1N6bFgyv769220MdYe9u37N4y7rI5mQ";
    // 验签key
    static final String repKey = "Hpcl522AV6q613KIi46u6g6XuW8vM1N8bFgyv769770MdYe9u37M4y7rIpl8";

    static final String baseUrl = "http://localhost:3020/api";

    public static void main(String[] args) {
        String payOrderId = payOrderTest();
//          String payOrderId = "pay2016120514591893319913549";
  //      if(payOrderId != null ) {
    //        queryOrderTest(payOrderId);
//            //iapNotifyTest(payOrderId);
      //  }

        //reissueOrder();
    }

    // 补单
    static String reissueOrder() {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("mchId", "0000010001");                       // 商户ID
        //pay2016110217365533065720596 易宝订单
        //pay2016110710140585712982930 京东PC快捷
        //pay2016110413295519324497135 支付宝wap
        //pay2016110709341803399339691 微信
        paramMap.put("payOrderId", "pay2016111514582547325887888");
        String reqData = XXPayUtil.genUrlParams(paramMap);
        System.out.println("请求支付中心补单接口,请求数据:" + reqData);
        String url = baseUrl + "/pay/reissue_order.htm?";
        String result = XXPayUtil.call4Post(url + reqData);
        System.out.println("请求支付中心补单接口,响应数据:" + result);
        Map retMap = JSON.parseObject(result);
        if("SUCCESS".equals(retMap.get("retCode"))) {
            System.out.println("=========支付中心补单验签成功=========");
        }
        return result;
    }

    // 统一下单
    static String payOrderTest() {
        JSONObject paramMap = new JSONObject();
        paramMap.put("mchId", mchId);                       // 商户ID
        paramMap.put("mchOrderNo", System.currentTimeMillis());               // 商户订单号
        paramMap.put("channelId", "WX_NATIVE");                    // 支付渠道ID, WX_NATIVE,ALIPAY_WAP
        paramMap.put("amount", 1);                      // 支付金额,单位分
        paramMap.put("currency", "cny");                  // 币种, cny-人民币
        paramMap.put("clientIp", "114.112.124.236");                        // 用户地址,IP或手机号
        paramMap.put("device", "WEB");                       // 支付公司标识
        paramMap.put("subject", "XXPAY支付测试");
        paramMap.put("body", "XXPAY支付测试");
        paramMap.put("notifyUrl", "http://baidu.com");         // 回调URL
        //paramMap.put("notifyUrl", "http://xxpay.ngrok.cc/notify/pay/wxPayNotifyRes.htm");
        paramMap.put("param1", "");                         // 扩展参数1
        paramMap.put("param2", "");                         // 扩展参数2

        paramMap.put("extra", "{\"productId\":\"120989823\",\"openId\":\"o2RvowBf7sOVJf8kJksUEMceaDqo\"}");  // 附加参数

        String reqSign = PayDigestUtil.getSign(paramMap, reqKey);
        paramMap.put("sign", reqSign);   // 签名
        String reqData = "params=" + paramMap.toJSONString();
        System.out.println("请求支付中心下单接口,请求数据:" + reqData);
        String url = baseUrl + "/pay/create_order?";
        String result = XXPayUtil.call4Post(url + reqData);
        System.out.println("请求支付中心下单接口,响应数据:" + result);
        Map retMap = JSON.parseObject(result);
        if("SUCCESS".equals(retMap.get("retCode"))) {
            // 验签
            String checkSign = PayDigestUtil.getSign(retMap, repKey, "sign", "payParams");
            String retSign = (String) retMap.get("sign");
            if(checkSign.equals(retSign)) {
                System.out.println("=========支付中心下单验签成功=========");
            }else {
                System.err.println("=========支付中心下单验签失败=========");
                return null;
            }
        }
        return retMap.get("payOrderId")+"";
    }

    static void queryOrderTest(String payOrderId) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("mchId", mchId);                   // 商户ID
        paramMap.put("payItemId", payItemId);               // 业务项目ID
        paramMap.put("rechargeId", "20160505000968");       // 业务系统流水号
        paramMap.put("payOrderId", payOrderId);             // 支付单号

        String reqSign = PayDigestUtil.getSign(paramMap, reqKey);
        paramMap.put("sign", reqSign);   // 签名
        String reqData = XXPayUtil.genUrlParams(paramMap);
        System.out.println("请求支付中心查单接口,请求数据:" + reqData);
        String url = baseUrl + "/pay/query_order.htm?";
        String result = XXPayUtil.call4Post(url + reqData);
        System.out.println("请求支付中心查单接口,响应数据:" + result);
        Map retMap = JSON.parseObject(result);      // fastJson速度更快
        if("SUCCESS".equals(retMap.get("retCode"))) {
            // 验签
            String checkSign = PayDigestUtil.getSign(retMap, repKey, "sign");
            String retSign = (String) retMap.get("sign");
            if(checkSign.equals(retSign)) {
                System.out.println("=========查询支付订单验签成功=========");
            }else {
                System.err.println("=========查询支付订单验签失败=========");
            }
        }
    }
    //
    static void iapNotifyTest(String payOrderId) {
        // "mchId", "payOrderId", "userId", "rechargeId", "receiptData", "transactionId", "sign"
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String receiptData = "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";
        paramMap.put("mchId", mchId);                   // 商户ID
        paramMap.put("payOrderId", payOrderId);         // 支付单号
        paramMap.put("receiptData", receiptData);
        paramMap.put("transactionId", "111");              // 苹果支付ID
        paramMap.put("userId", 1984l);                  // 用户ID

        String reqSign = PayDigestUtil.getSign(paramMap, reqKey);
        paramMap.put("sign", reqSign);   // 签名

        try {
            paramMap.put("receiptData", URLEncoder.encode(receiptData, "UTF-8"));             // 苹果支付凭据
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String reqData = XXPayUtil.genUrlParams(paramMap);
        System.out.println("请求支付中心苹果通知接口,请求数据:" + reqData);
        String url = baseUrl + "/notify/iapPayNotifyRes.htm?";
        String result = XXPayUtil.call4Post(url + reqData);
        System.out.println("请求支付中心苹果通知接口,响应数据:" + result);
        Map retMap = JSON.parseObject(result);      // fastJson速度更快
        if("SUCCESS".equals(retMap.get("retCode"))) {
            // 验签
            String checkSign = PayDigestUtil.getSign(retMap, repKey, "sign");
            String retSign = (String) retMap.get("sign");
            if(checkSign.equals(retSign)) {
                System.out.println("=========苹果支付验证凭据验签成功=========");
            }else {
                System.err.println("=========苹果支付验证凭据验签失败=========");
            }
        }
    }

    static void iapNotifyTest2(String payOrderId) {
        // "mchId", "payOrderId", "userId", "rechargeId", "receiptData", "transactionId", "sign"
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("mchId", mchId);                   // 商户ID
        //paramMap.put("rechargeId", "20160505000968");   // 业务系统流水号
        paramMap.put("payOrderId", payOrderId);         // 支付单号
        try {
            paramMap.put("receiptData", URLEncoder.encode("abc+123", "UTF-8"));             // 苹果支付凭据
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        paramMap.put("transactionId", "");           // 苹果支付ID
        paramMap.put("userId", 1984l);                  // 用户ID

        String reqSign = PayDigestUtil.getSign(paramMap, reqKey, "receiptData");
        paramMap.put("sign", reqSign);   // 签名
        String reqData = XXPayUtil.genUrlParams(paramMap);
        System.out.println("请求支付中心苹果通知接口,请求数据:" + reqData);
        String url = "http://localhost:8088/h5/test.html?";
        String result = XXPayUtil.call4Post(url + reqData);
        System.out.println("请求支付中心苹果通知接口,响应数据:" + result);
        Map retMap = JSON.parseObject(result);      // fastJson速度更快
        if("SUCCESS".equals(retMap.get("retCode"))) {
            // 验签
            String checkSign = PayDigestUtil.getSign(retMap, repKey, "sign");
            String retSign = (String) retMap.get("sign");
            if(checkSign.equals(retSign)) {
                System.out.println("=========苹果支付验证凭据验签成功=========");
            }else {
                System.err.println("=========苹果支付验证凭据验签失败=========");
            }
        }
    }


}
