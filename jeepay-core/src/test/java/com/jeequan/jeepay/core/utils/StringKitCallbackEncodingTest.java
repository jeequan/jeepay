package com.jeequan.jeepay.core.utils;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** 回调取值完整编码，同时保留现有 hash 路由参数位置。 */
class StringKitCallbackEncodingTest {
    private Map<String, String> decode(String query) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            result.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                URLDecoder.decode(parts.length == 2 ? parts[1] : "", StandardCharsets.UTF_8));
        }
        return result;
    }

    @Test
    void reservedAndUnicodeValuesRoundTripWithoutExtraParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("subject", "套餐 A & B=2 + 优惠/折扣?#%");
        params.put("sign", "ab+cd/ef==");
        String result = StringKit.appendUrlQuery("https://example.test/callback?tenant=one", params);
        Map<String, String> values = decode(URI.create(result).getRawQuery());
        assertEquals(3, values.size());
        assertEquals("one", values.get("tenant"));
        assertEquals(params.get("subject"), values.get("subject"));
        assertEquals(params.get("sign"), values.get("sign"));
        assertNull(URI.create(result).getRawFragment());
    }

    @Test
    void hashRouteKeepsExistingQueryLocationAndRoundTripsValues() {
        String result = StringKit.appendUrlQuery("https://example.test/#/return?tenant=one",
            Map.of("extParam", "a&b+c=中文"));
        URI uri = URI.create(result);
        assertNull(uri.getRawQuery());
        assertTrue(uri.getRawFragment().startsWith("/return?tenant=one&"));
        Map<String, String> values = decode(uri.getRawFragment().split("\\?", 2)[1]);
        assertEquals(2, values.size());
        assertEquals("a&b+c=中文", values.get("extParam"));
    }

    @Test
    void decodedCallbackRetainsOriginalSignature() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("subject", "套餐 A&B + C");
        params.put("amount", 100);
        String expected = JeepayKit.getSign(params, "synthetic-test-key");
        params.put("sign", expected);
        Map<String, String> decoded = decode(URI.create(StringKit.appendUrlQuery(
            "https://example.test/notify", params)).getRawQuery());
        assertEquals(expected, decoded.remove("sign"));
        Map<String, Object> unsigned = new LinkedHashMap<>(decoded);
        assertEquals(expected, JeepayKit.getSign(unsigned, "synthetic-test-key"));
    }

    @Test
    void hashRouteWithoutQueryKeepsLegacyLocation() {
        assertEquals("https://example.test/#/return?code=abc123",
            StringKit.appendUrlQuery("https://example.test/#/return", Map.of("code", "abc123")));
    }

    @Test
    void ordinarySafeValuesAndEmptyInputsRemainCompatible() {
        assertEquals("https://example.test/return?amount=100",
            StringKit.appendUrlQuery("https://example.test/return", Map.of("amount", 100)));
        assertEquals("https://example.test/return", StringKit.appendUrlQuery("https://example.test/return", Map.of()));
        assertEquals("", StringKit.appendUrlQuery("", Map.of("amount", 100)));
        assertNull(StringKit.appendUrlQuery(null, Map.of("amount", 100)));
        assertEquals("https://example.test/return", StringKit.appendUrlQuery("https://example.test/return", null));
    }
}
