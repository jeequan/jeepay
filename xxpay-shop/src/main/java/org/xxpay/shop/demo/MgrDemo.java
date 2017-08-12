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

    // 统一下单
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

    // 统一下单
    static void addPayChannel() {
        JSONObject params = new JSONObject();
        params.put("channelId", "WX_NATIVE");//WX_NATIVE
        params.put("channelName", "WX");//WX
        params.put("channelMchId", "1481721182");
        params.put("mchId", "20001223");
       // params.put("param", "{\"appid\": \"2016102302295125\", \"partner\": \"2088521108562983\", \"ali_account\": \"pay_vvlive@mail.51vv.com\", \"private_key\": \"MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKOuhY6LYZBMiRrvRw2s71CjPW7OPCvapjZdJJNPt5x471E3kDCb9A7kQTyqFIIVcUNKDlKRNztKBhhlxAiI7d95UkN5pAMK+XUItjyA9nj9cqK/ajHjwC4AlIRUZhlsPvj6lt1Oj1Kf1sNDJMM/NZL9IR8EXr7HlIsCjJNVHFPvAgMBAAECgYBY1S7G3f5lQiRm6dW2JlT7fpyotmURp+jtOD/Rc0JDOZ8ohO9McldSfa6qLeRTdS+zRU3goc9H7jTAqPprZ2UxNTUwJ4uMh+2bCtXkvUPwoWF4fb095xGtEUdbKMFkv+yKpCQASrjDhqzVq5xD/uc796wd7HOHwr8xPNOrKKSGAQJBANfgcaiIyFeo8KK4vIUWtqSiqLgG6gp7ABx2WpMWX3wsjbiCBQGVbJbnFcCkB+bofCuKYj7BGLjEEqc3c6y+Ph8CQQDCGprovroKw09dOzqFFPpkMrZvkOpO2e+RhDhhLYq2e5lRLVePtB/ZX2iy2yKQEp/7VWbNFzobqYR6KPXEH5AxAkBx3oD1XhkXLBSqMHm4Ve/HTcljMLp5BsJbQQ6rsUxyimnC3kpXuILL4l61+4/ze8Qrj1YdNeudYkdYjsZkYwEPAkBoWedIEylvmdqz86CdZU7LyVu9FPpyk8WwxJWO4O3+9unQ84BseFjbAukFprupGuo5M4uF3OPXdUYMarLd0l4xAkAMjec0KXp15a93I5y/vfIEAMQ+CQj/LwOxyAM6tTSPVGJHu70pXFQVWtwY+ycMuzbxTdSLuQYxITstHnV3mu76\", \"public_key\": \"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCjroWOi2GQTIka70cNrO9Qoz1uzjwr2qY2XSSTT7eceO9RN5Awm/QO5EE8qhSCFXFDSg5SkTc7SgYYZcQIiO3feVJDeaQDCvl1CLY8gPZ4/XKiv2ox48AuAJSEVGYZbD74pbdTo9Sn9bDQyTDPzWS/SEfBF6x5SLAoyTVRxT7wIDAQAB\"}");
        //params.put("param", "{\"appid\": \"2016102302295125\", \"partner\": \"2088521108562983\", \"ali_account\": \"pay_vvlive@mail.51vv.com\", \"private_key\": \"MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKOuhY6LYZBMiRrvRw2s71CjPW7OPCvapjZdJJNPt5x471E3kDCb9A7kQTyqFIIVcUNKDlKRNztKBhhlxAiI7d95UkN5pAMK+XUItjyA9nj9cqK/ajHjwC4AlIRUZhlsPvj6lt1Oj1Kf1sNDJMM/NZL9IR8EXr7HlIsCjJNVHFPvAgMBAAECgYBY1S7G3f5lQiRm6dW2JlT7fpyotmURp+jtOD/Rc0JDOZ8ohO9McldSfa6qLeRTdS+zRU3goc9H7jTAqPprZ2UxNTUwJ4uMh+2bCtXkvUPwoWF4fb095xGtEUdbKMFkv+yKpCQASrjDhqzVq5xD/uc796wd7HOHwr8xPNOrKKSGAQJBANfgcaiIyFeo8KK4vIUWtqSiqLgG6gp7ABx2WpMWX3wsjbiCBQGVbJbnFcCkB+bofCuKYj7BGLjEEqc3c6y+Ph8CQQDCGprovroKw09dOzqFFPpkMrZvkOpO2e+RhDhhLYq2e5lRLVePtB/ZX2iy2yKQEp/7VWbNFzobqYR6KPXEH5AxAkBx3oD1XhkXLBSqMHm4Ve/HTcljMLp5BsJbQQ6rsUxyimnC3kpXuILL4l61+4/ze8Qrj1YdNeudYkdYjsZkYwEPAkBoWedIEylvmdqz86CdZU7LyVu9FPpyk8WwxJWO4O3+9unQ84BseFjbAukFprupGuo5M4uF3OPXdUYMarLd0l4xAkAMjec0KXp15a93I5y/vfIEAMQ+CQj/LwOxyAM6tTSPVGJHu70pXFQVWtwY+ycMuzbxTdSLuQYxITstHnV3mu76\", \"public_key\": \"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCjroWOi2GQTIka70cNrO9Qoz1uzjwr2qY2XSSTT7eceO9RN5Awm/QO5EE8qhSCFXFDSg5SkTc7SgYYZcQIiO3feVJDeaQDCvl1CLY8gPZ4/XKiv2ox48AuAJSEVGYZbD74+pbdTo9Sn9bDQyTDPzWS/SEfBF6+x5SLAoyTVRxT7wIDAQAB\"}");
        params.put("param","{\"mchId\":\"1481721182\", \"appId\":\"wx077cb62e341f8a5c\", \"key\":\"50EBq9MJX5DNa39MZd8bc65TeFUyt6rM\", \"certLocalPath\":\"wx/1481721182_cert.p12\", \"certPassword\":\"1481721182\", \"desc\":\"xxpay_shop-native(xxpay扫码支付)\"}");

        //params.put("param", "{\"mchId\":\"1366201502\", \"appId\":\"wx0ab67caf7f591834\", \"key\":\"zEBq9MJX5DNa39MZd8bc65TeFUyt6rkS\", \"certLocalPath\":\"wx/1366201502_cert.p12\", \"certPassword\":\"1366201502\", \"desc\":\"vvlive-jsapi(vv直播公众号)\"}");
        params.put("remark", "微信扫码支付");
        String reqData = "params=" + params.toJSONString();
        System.out.println("请求支付中心添加渠道接口,请求数据:" + reqData);
        String url = baseUrl + "/channel/add?";
        String result = XXPayUtil.call4Post(url + reqData);
        System.out.println("请求支付中心添加渠道接口,响应数据:" + result);
    }

}
