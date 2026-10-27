package com.jeequan.jeepay.pay.channel.xxpay;

import cn.hutool.core.net.url.UrlQuery;
import com.jeequan.jeepay.core.utils.JeepayKit;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 离线签名/传输契约：不是 xxpay 服务端或真实渠道验收。 */
class XxpayUrlSignatureTest {
    @Test
    void signedRawValuesSurviveQueryTransportForAllChannelPaths() {
        String key = "synthetic-test-key-not-a-credential";
        Map<String, Object> original = new TreeMap<>();
        original.put("mchOrderNo", "synthetic-order-148");
        original.put("subject", "测试 A B & C=1 % #?");
        original.put("body", "保留 A+B 字面加号");
        original.put("notifyUrl", "https://merchant.example.invalid/notice?a=1&b=two words#片段");
        original.put("amount", 123L);
        original.put("optional", null);
        String signature = XxpayKit.getSign(original, key);
        Map<String, Object> signed = new TreeMap<>(original);
        signed.put("sign", signature);
        String base = "https://channel.example.invalid/";
        String[] endpoints = {XxpayKit.getPaymentUrl(base), XxpayKit.getRefundUrl(base),
                XxpayKit.getQueryPayOrderUrl(base), XxpayKit.getQueryRefundOrderUrl(base)};
        for (String endpoint : endpoints) {
            URI wireUrl = URI.create(endpoint + "?" + JeepayKit.genUrlParams(signed));
            // 显式使用表单查询编码契约：+ 表示空格，%2B 表示字面加号。
            // 此处验证标准解码后签名一致，不代表已验证外部 xxpay 的解码设置。
            Map<CharSequence, CharSequence> decoded = UrlQuery.of(wireUrl.getRawQuery(), StandardCharsets.UTF_8, false, true).getQueryMap();
            assertEquals(signed.size(), decoded.size());
            assertEquals(original.get("subject"), decoded.get("subject"));
            assertEquals(original.get("body"), decoded.get("body"));
            assertEquals(original.get("notifyUrl"), decoded.get("notifyUrl"));
            assertEquals("", decoded.get("optional"));
            Map<String, Object> unsigned = new TreeMap<>();
            decoded.forEach((name, value) -> unsigned.put(name.toString(), value == null ? null : value.toString()));
            assertEquals(signature, unsigned.remove("sign"));
            assertEquals(signature, XxpayKit.getSign(unsigned, key));
        }
    }
}
