package com.jeequan.jeepay.core.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JeepayKit#genUrlParams 回归测试。
 * <p>
 * 该方法生成的查询串会被直接拼接到渠道方请求地址上（xxpay 的下单/退款/查询接口），
 * 因此生成的查询串必须能被标准 URL 解码还原出原始参数。
 * 原实现仅对包含 "+" 的值编码，包含 & = 空格 等保留字符的值原样输出，
 * 导致参数被截断或凭空产生额外参数。
 */
public class JeepayKitUrlParamsTest {

    /** 按标准 query string 规则解析：以 & 分组、以第一个 = 分割，并对 key/value 做 URL 解码 **/
    private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {

        Map<String, String> result = new LinkedHashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            String key = idx < 0 ? pair : pair.substring(0, idx);
            String value = idx < 0 ? "" : pair.substring(idx + 1);
            result.put(URLDecoder.decode(key, StandardCharsets.UTF_8.name()),
                    URLDecoder.decode(value, StandardCharsets.UTF_8.name()));
        }
        return result;
    }

    @Test
    @DisplayName("参数值含保留字符时，生成的查询串应能原样还原")
    public void reservedCharactersInValueShouldRoundTrip() throws Exception {

        Map<String, Object> params = new TreeMap<>();
        params.put("mchOrderNo", "P1661045100000000001");
        // 商户配置的异步通知地址，自带 query 条件
        params.put("notifyUrl", "https://mch.example.com/notify?token=abc&x=1");
        // 商品标题为商户传入的自由文本，常见包含空格与 & 符号
        params.put("subject", "iPhone 15 & 14 Pro");

        String query = JeepayKit.genUrlParams(params);
        Map<String, String> parsed = parseQuery(query);

        // 参数个数必须与入参一致，不允许因未编码而多出参数
        assertEquals(3, parsed.size(), "生成的查询串参数数量不一致: " + query);
        assertEquals(params.get("mchOrderNo"), parsed.get("mchOrderNo"));
        assertEquals(params.get("notifyUrl"), parsed.get("notifyUrl"));
        assertEquals(params.get("subject"), parsed.get("subject"));
    }

    @Test
    @DisplayName("参数值含加号时，不应被还原成空格")
    public void plusSignShouldNotBeDecodedAsSpace() throws Exception {

        Map<String, Object> params = new TreeMap<>();
        params.put("body", "A+B 商品");

        Map<String, String> parsed = parseQuery(JeepayKit.genUrlParams(params));

        assertEquals(1, parsed.size());
        assertEquals("A+B 商品", parsed.get("body"));
    }

    @Test
    @DisplayName("非 ASCII 参数值应被编码，且不影响其他参数")
    public void nonAsciiValueShouldBeEncoded() throws Exception {

        Map<String, Object> params = new TreeMap<>();
        params.put("extParam", null);
        params.put("subject", "测试商品");

        String query = JeepayKit.genUrlParams(params);

        // 查询串直接拼在请求地址上，非 ASCII 字符必须先百分号编码，否则请求行非法
        for (int i = 0; i < query.length(); i++) {
            assertTrue(query.charAt(i) < 128, "查询串存在未编码字符: " + query);
        }

        Map<String, String> parsed = parseQuery(query);

        assertEquals(2, parsed.size());
        assertEquals("", parsed.get("extParam"));
        assertEquals("测试商品", parsed.get("subject"));
    }
}
