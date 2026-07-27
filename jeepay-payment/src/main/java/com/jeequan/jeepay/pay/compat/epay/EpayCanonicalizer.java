package com.jeequan.jeepay.pay.compat.epay;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class EpayCanonicalizer {

    private EpayCanonicalizer() {
    }

    public static String canonical(Map<String, ?> fields) {
        TreeMap<String, String> sorted = new TreeMap<>();
        if (fields == null) {
            return "";
        }
        for (Map.Entry<String, ?> entry : fields.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || "sign".equals(key) || "sign_type".equals(key) || value == null) {
                continue;
            }
            String text = String.valueOf(value);
            if (text.isEmpty()) {
                continue;
            }
            sorted.put(key, text);
        }
        return sorted.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }
}
