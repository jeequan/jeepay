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
        body.put("sign", hasText(credential == null ? null : credential.platformPrivateKey())
                ? EpaySigner.signRsa(body, credential.platformPrivateKey()) : "");
        return new EpayCompatResponse(code == 0, JSON.toJSONString(body), payInfo);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
