package org.xxpay.shop.demo;

import com.alibaba.fastjson.JSONObject;
import org.xxpay.common.util.XXPayUtil;

/**
 * Created by dingzhiwei on 16/5/5.
 */
public class MgrDemo {

    // 商户ID

    static final String baseUrl = "http://localhost:3000";

    public static void main(String[] args) {
        //addMchInfo();
       addPayChannel();
    }

    // 添加商户
    static void addMchInfo() {
        JSONObject params = new JSONObject();
        params.put("mchId", "20001226");
        params.put("name", "百年树丁");
        params.put("type", "1");
        params.put("reqKey", "298332323231231313");
        params.put("resKey", "883435353534543534");
        String reqData = "params=" + params.toJSONString();
        System.out.println("请求支付中心添加商户接口,请求数据:" + reqData);
        String url = baseUrl + "/mch/add?";
        String result = XXPayUtil.call4Post(url + reqData);
        System.out.println("请求支付中心添加商户接口,响应数据:" + result);

    }

    // 添加渠道
    static void addPayChannel() {
        JSONObject params = new JSONObject();
        params.put("channelId", "WX_NATIVE");//WX_NATIVE
        params.put("channelName", "WX");//WX
        params.put("channelMchId", "1481721182");
        params.put("mchId", "20001223");
        params.put("param","{\"mchId\":\"1481721182\", \"appId\":\"wx077cb62e341f8a5c\", \"key\":\"***\", \"certLocalPath\":\"wx/1481721182_cert.p12\", \"certPassword\":\"1481721182\", \"desc\":\"xxpay_shop-native(xxpay扫码支付)\"}");
        params.put("remark", "微信扫码支付");
        String reqData = "params=" + params.toJSONString();
        System.out.println("请求支付中心添加渠道接口,请求数据:" + reqData);
        String url = baseUrl + "/channel/add?";
        String result = XXPayUtil.call4Post(url + reqData);
        System.out.println("请求支付中心添加渠道接口,响应数据:" + result);
    }

}
