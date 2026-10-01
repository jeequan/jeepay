package com.jeequan.jeepay.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogSanitizerTest {

    @Test
    void redactsNestedMapsArraysBeansAndEncodedCredentialsWithoutMutation() {
        Map<String, Object> nested = new LinkedHashMap<>();
        String[] names = {"originalPwd", "confirmPwd", "newPassword", "PASSWORD_HASH",
                "passwordBase64", "passwd", "credential", "dbPassword"};
        for (String name : names) nested.put(name, "synthetic-secret-" + name);
        nested.put("recordId", 17);
        Map<String, Object> source = Map.of("items", List.of(nested), "array", new Object[]{nested},
                "user", new PasswordBean());
        String before = JSON.toJSONString(source);

        String sanitized = LogSanitizer.toJson(source);

        assertFalse(sanitized.contains("synthetic-secret"));
        JSONObject result = JSON.parseObject(sanitized);
        JSONObject item = result.getJSONArray("items").getJSONObject(0);
        for (String name : names) assertEquals("[REDACTED]", item.getString(name));
        assertEquals(17, item.getIntValue("recordId"));
        assertEquals("[REDACTED]", result.getJSONObject("user").getString("password"));
        assertEquals(before, JSON.toJSONString(source));
    }

    @Test
    void omitsOpaquePaymentConfigurationEvenWhenMalformed() {
        for (String config : List.of("{\"isvPrivateCertPwd\":\"synthetic-secret-cert\"}",
                "{malformed-synthetic-secret-cert")) {
            JSONObject logged = JSON.parseObject(LogSanitizer.toJson(Map.of(
                    "ifCode", "synthetic-channel", "ifParams", config)));
            assertEquals("[REDACTED]", logged.getString("ifParams"));
            assertEquals("synthetic-channel", logged.getString("ifCode"));
        }
    }

    @Test
    void handlesNullAndSharedReferences() {
        assertEquals("null", LogSanitizer.toJson(null));
        Map<String, Object> shared = Map.of("pwd", "synthetic-secret-shared");
        assertFalse(LogSanitizer.toJson(List.of(shared, shared)).contains("synthetic-secret"));
    }

    @Test
    void failsClosedWhenSerializationFails() {
        assertEquals("\"[UNAVAILABLE]\"", LogSanitizer.toJson(new BrokenBean()));
    }

    public static class PasswordBean {
        public String getPassword() { return "synthetic-secret-bean"; }
        public String getName() { return "synthetic-user"; }
    }

    public static class BrokenBean {
        public String getValue() { throw new IllegalStateException("synthetic-secret-exception"); }
    }
}
