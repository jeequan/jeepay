package com.jeequan.jeepay.pay.compat.epay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public final class EpayV1Adapter {

    public boolean verify(Map<String, String> fields, EpayCredential credential) {
        return credential != null
                && hasText(credential.merchantKey())
                && EpaySigner.verifyMd5(fields, credential.merchantKey(), fields.get("sign"));
    }

    public EpayCompatResponse success(EpayCreateResult result, EpayCredential credential) {
        return response(1, "success", result.tradeNo(), result.payInfo(), credential);
    }

    public EpayCompatResponse failure(String message, EpayCredential credential) {
        return response(0, message, "", "", credential);
    }

    private EpayCompatResponse response(int code, String message, String tradeNo, String payInfo,
                                        EpayCredential credential) {
        JSONObject body = new JSONObject(true);
        body.put("code", code);
        body.put("msg", message);
        body.put("trade_no", tradeNo == null ? "" : tradeNo);
        body.put("payurl", payInfo == null ? "" : payInfo);
        body.put("qrcode", "");
        body.put("urlscheme", "");
        body.put("sign_type", "MD5");
        body.put("sign", hasText(credential == null ? null : credential.merchantKey())
                ? EpaySigner.signMd5(body, credential.merchantKey()) : "");
        return new EpayCompatResponse(code == 1, JSON.toJSONString(body), payInfo);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
