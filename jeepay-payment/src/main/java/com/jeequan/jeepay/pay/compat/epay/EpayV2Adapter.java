package com.jeequan.jeepay.pay.compat.epay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public final class EpayV2Adapter {

    public boolean verify(Map<String, String> fields, EpayCredential credential) {
        return credential != null
                && hasText(credential.merchantPublicKey())
                && EpaySigner.verifyRsa(fields, credential.merchantPublicKey(), fields.get("sign"));
    }

    public EpayCompatResponse success(EpayCreateResult result, EpayCredential credential) {
        return response(0, "success", result.tradeNo(), result.payType(), result.payInfo(), credential);
    }

    public EpayCompatResponse failure(String message, EpayCredential credential) {
        return response(1, message, "", "", "", credential);
    }

    private EpayCompatResponse response(int code, String message, String tradeNo, String payType,
                                        String payInfo, EpayCredential credential) {
        JSONObject body = new JSONObject(true);
        body.put("code", code);
        body.put("msg", message);
        body.put("trade_no", tradeNo == null ? "" : tradeNo);
        body.put("pay_type", payType == null ? "" : payType);
        body.put("pay_info", payInfo == null ? "" : payInfo);
        body.put("sign_type", "RSA");
        try {
            body.put("sign", sign(body, credential));
            return new EpayCompatResponse(code == 0, JSON.toJSONString(body), payInfo);
        } catch (IllegalArgumentException e) {
            if (code == 0) {
                JSONObject failure = new JSONObject(true);
                failure.put("code", 1);
                failure.put("msg", "V2 RSA响应签名失败");
                failure.put("trade_no", "");
                failure.put("pay_type", "");
                failure.put("pay_info", "");
                failure.put("sign_type", "RSA");
                failure.put("sign", "");
                return new EpayCompatResponse(false, JSON.toJSONString(failure), "");
            }
            body.put("sign", "");
            return new EpayCompatResponse(false, JSON.toJSONString(body), "");
        }
    }

    private static String sign(JSONObject body, EpayCredential credential) {
        String privateKey = credential == null ? null : credential.platformPrivateKey();
        if (!hasText(privateKey)) {
            throw new IllegalArgumentException("V2 RSA响应私钥未配置");
        }
        return EpaySigner.signRsa(body, privateKey);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
