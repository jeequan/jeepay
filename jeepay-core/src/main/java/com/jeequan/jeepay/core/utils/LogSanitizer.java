package com.jeequan.jeepay.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ValueFilter;

import java.util.Locale;

/** 日志专用序列化；不修改业务对象，也不改变接口响应。 */
public final class LogSanitizer {

    private static final String REDACTED = "[REDACTED]";
    private static final ValueFilter PASSWORD_FILTER = (object, name, value) -> {
        String key = name.toLowerCase(Locale.ROOT);
        // 编码、摘要和嵌套密码字段同样不能进入日志。
        return key.contains("password") || key.contains("passwd")
                || key.contains("pwd") || key.contains("credential")
                // 支付配置是 JSON 字符串，可能含证书密码；整体省略，避免二次解析遗漏。
                || key.equals("ifparams") ? REDACTED : value;
    };

    private LogSanitizer() { }

    public static String toJson(Object value) {
        try {
            return JSON.toJSONString(value, PASSWORD_FILTER);
        } catch (RuntimeException e) {
            // 序列化异常可能包含原始值；失败时不回退到原文或异常详情。
            return "\"[UNAVAILABLE]\"";
        }
    }
}
