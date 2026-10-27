package com.jeequan.jeepay.core.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link StringKit#appendUrlQuery(String, Map)} 回归测试。
 *
 * <p>该方法用于拼装两类对外地址：
 * <ul>
 *   <li>发给商户的异步通知 / 同步跳转地址（{@code PayMchNotifyService#createNotifyUrl}、
 *       {@code createReturnUrl}，商户在 {@code notifyUrl}/{@code returnUrl} 里配置自己的回调地址）；</li>
 *   <li>收银台跳转地址（{@code ChannelUserIdController}、{@code MchTransferController}）。</li>
 * </ul>
 *
 * <p>原实现用 {@code URLUtil.encodeQuery()} 编码**单个参数值**，但该 API 的语义是编码
 * **整条 query string**：它刻意保留 {@code & = + ? /} 等分隔符。于是参数值里的 {@code &}
 * 会被当成参数分隔符，把一个参数拆成两个；{@code =} 会把值截断；{@code +} 在服务端按
 * {@code application/x-www-form-urlencoded} 解码成空格。
 *
 * <p>商户商品标题（{@code subject}）只受 {@code @NotBlank} 约束，``"Salt & Pepper Shaker"``
 * 这类值完全合法，且该标题会被原样带进异步通知报文，因此缺陷可被真实触发。
 */
public class StringKitAppendUrlQueryTest {

    /** 按标准 query string 规则解析：以 & 分组、以第一个 = 分割，并对 key/value 做 URL 解码 */
    private Map<String, String> parseQuery(String url) throws UnsupportedEncodingException {

        Map<String, String> result = new LinkedHashMap<>();
        int q = url.indexOf('?');
        if (q < 0 || q == url.length() - 1) {
            return result;
        }
        String query = url.substring(q + 1);
        for (String pair : query.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int idx = pair.indexOf('=');
            String key = idx < 0 ? pair : pair.substring(0, idx);
            String value = idx < 0 ? "" : pair.substring(idx + 1);
            result.put(URLDecoder.decode(key, StandardCharsets.UTF_8.name()),
                    URLDecoder.decode(value, StandardCharsets.UTF_8.name()));
        }
        return result;
    }

    @Test
    @DisplayName("参数值含 & 时不得被拆成两个参数")
    void ampersandInValueMustNotSplitParameters() throws Exception {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("subject", "Salt & Pepper Shaker");
        params.put("amount", "100");

        String url = StringKit.appendUrlQuery("https://mch.example.com/notify", params);
        Map<String, String> parsed = parseQuery(url);

        assertEquals(2, parsed.size(), "参数个数被改变，实际 URL: " + url);
        assertEquals("Salt & Pepper Shaker", parsed.get("subject"),
                "subject 未能原样还原，实际 URL: " + url);
        assertEquals("100", parsed.get("amount"), "amount 未能原样还原，实际 URL: " + url);
    }

    @Test
    @DisplayName("参数值含 = 时不得截断取值")
    void equalsInValueMustNotTruncate() throws Exception {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("extParam", "a=b");

        String url = StringKit.appendUrlQuery("https://mch.example.com/notify", params);
        Map<String, String> parsed = parseQuery(url);

        assertEquals("a=b", parsed.get("extParam"), "extParam 被截断，实际 URL: " + url);
    }

    @Test
    @DisplayName("参数值含 + 时不得被服务端解码成空格")
    void plusInValueMustBeEncoded() throws Exception {

        Map<String, Object> params = new LinkedHashMap<>();
        // 渠道侧签名、base64 过的扩展参数都可能是这种形态
        params.put("sign", "ab+cd==");

        String url = StringKit.appendUrlQuery("https://mch.example.com/notify", params);
        Map<String, String> parsed = parseQuery(url);

        assertEquals("ab+cd==", parsed.get("sign"),
                "sign 中的 + 未编码，服务端会解码成空格，实际 URL: " + url);
    }

    @Test
    @DisplayName("回调地址已带查询参数时，新参数仍追加在其后")
    void existingQueryStringIsPreserved() throws Exception {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("payOrderId", "P123");

        String url = StringKit.appendUrlQuery("https://mch.example.com/notify?tenant=t1", params);
        Map<String, String> parsed = parseQuery(url);

        assertEquals("t1", parsed.get("tenant"), "原有参数丢失，实际 URL: " + url);
        assertEquals("P123", parsed.get("payOrderId"), "新参数缺失，实际 URL: " + url);
    }

    @Test
    @DisplayName("空 url 或空参数时原样返回")
    void emptyInputsAreReturnedUnchanged() {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("a", "1");

        assertEquals(null, StringKit.appendUrlQuery(null, params));
        assertEquals("", StringKit.appendUrlQuery("", params));
        assertEquals("https://mch.example.com/notify",
                StringKit.appendUrlQuery("https://mch.example.com/notify", new LinkedHashMap<>()));
    }

    @Test
    @DisplayName("回调地址带 fragment 时，query 必须拼在 # 之前")
    void queryMustBePlacedBeforeFragment() throws Exception {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("payOrderId", "P123");

        // SPA 常见的 hash 路由回调地址
        String url = StringKit.appendUrlQuery("https://mch.example.com/cb#/result", params);

        int q = url.indexOf('?');
        int hash = url.indexOf('#');
        assertEquals(true, q >= 0 && hash > q,
                "query 必须出现在 fragment 之前，否则服务端收不到参数，实际 URL: " + url);

        // fragment 必须原样保留在末尾
        assertEquals(true, url.endsWith("#/result"),
                "fragment 被破坏，实际 URL: " + url);

        // 用去掉 fragment 的部分按标准规则反解，应当能还原参数
        Map<String, String> parsed = parseQuery(url.substring(0, hash));
        assertEquals("P123", parsed.get("payOrderId"), "参数未能还原，实际 URL: " + url);
    }

    @Test
    @DisplayName("回调地址同时带 query 与 fragment 时，两者都保留")
    void existingQueryAndFragmentAreBothPreserved() throws Exception {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("payOrderId", "P123");

        String url = StringKit.appendUrlQuery(
                "https://mch.example.com/cb?tenant=t1#/result", params);

        int hash = url.indexOf('#');
        assertEquals(true, url.endsWith("#/result"), "fragment 被破坏，实际 URL: " + url);

        Map<String, String> parsed = parseQuery(url.substring(0, hash));
        assertEquals("t1", parsed.get("tenant"), "原有参数丢失，实际 URL: " + url);
        assertEquals("P123", parsed.get("payOrderId"), "新参数缺失，实际 URL: " + url);
    }
}
