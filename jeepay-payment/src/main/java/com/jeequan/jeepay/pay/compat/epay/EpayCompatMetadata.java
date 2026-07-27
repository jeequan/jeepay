package com.jeequan.jeepay.pay.compat.epay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Optional;

public final class EpayCompatMetadata {

    static final String RESERVED_KEY = "__epay_compat";

    private EpayCompatMetadata() {
    }

    public static String encode(EpayMetadataValue value) {
        JSONObject compat = new JSONObject(true);
        compat.put("version", value.version());
        compat.put("pid", value.pid());
        compat.put("app_id", value.appId());
        compat.put("type", value.type());
        compat.put("out_trade_no", value.outTradeNo());
        compat.put("notify_url", value.notifyUrl());
        compat.put("return_url", value.returnUrl());
        compat.put("name", value.name());
        compat.put("money", value.money());
        compat.put("clientip", value.clientIp());
        compat.put("device", value.device());
        compat.put("trade_no", value.tradeNo());

        JSONObject root = new JSONObject(true);
        root.put(RESERVED_KEY, compat);
        return root.toJSONString();
    }

    public static Optional<EpayMetadataValue> decode(String channelExtra) {
        try {
            Object parsed = JSON.parse(channelExtra);
            if (!(parsed instanceof JSONObject root)) {
                return Optional.empty();
            }
            Object compatValue = root.get(RESERVED_KEY);
            if (!(compatValue instanceof JSONObject compat)) {
                return Optional.empty();
            }
            EpayMetadataValue value = new EpayMetadataValue(
                    required(compat, "version"),
                    required(compat, "pid"),
                    compat.getString("app_id"),
                    compat.getString("type"),
                    required(compat, "out_trade_no"),
                    required(compat, "notify_url"),
                    required(compat, "return_url"),
                    compat.getString("name"),
                    required(compat, "money"),
                    compat.getString("clientip"),
                    compat.getString("device"),
                    compat.getString("trade_no"));
            return Optional.of(value);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String required(JSONObject object, String key) {
        String value = object.getString(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }
}

record EpayMetadataValue(
        String version,
        String pid,
        String appId,
        String type,
        String outTradeNo,
        String notifyUrl,
        String returnUrl,
        String name,
        String money,
        String clientIp,
        String device,
        String tradeNo) {
}
