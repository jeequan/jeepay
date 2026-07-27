package com.jeequan.jeepay.pay.compat.epay;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "epay.compat")
public class EpayCompatProperties {

    private String defaultAppId = "";
    private Map<String, String> appIds = new LinkedHashMap<>();
    private Map<String, Map<String, CredentialProperties>> credentials = new LinkedHashMap<>();
    private long clockSkewSeconds = 300;

    public String resolveAppId(String pid) {
        String mapped = appIds.get(pid);
        if (hasText(mapped)) {
            return mapped.trim();
        }
        if (hasText(defaultAppId)) {
            return defaultAppId.trim();
        }
        throw new IllegalStateException("Missing epay.compat.default-app-id or epay.compat.app-ids mapping for pid " + pid);
    }

    public String getDefaultAppId() {
        return defaultAppId;
    }

    public void setDefaultAppId(String defaultAppId) {
        this.defaultAppId = defaultAppId;
    }

    public Map<String, String> getAppIds() {
        return appIds;
    }

    public void setAppIds(Map<String, String> appIds) {
        this.appIds = appIds == null ? new LinkedHashMap<>() : appIds;
    }

    public Map<String, Map<String, CredentialProperties>> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, Map<String, CredentialProperties>> credentials) {
        this.credentials = credentials == null ? new LinkedHashMap<>() : credentials;
    }

    public long getClockSkewSeconds() {
        return clockSkewSeconds;
    }

    public void setClockSkewSeconds(long clockSkewSeconds) {
        if (clockSkewSeconds < 0) {
            throw new IllegalArgumentException("clockSkewSeconds must be non-negative");
        }
        this.clockSkewSeconds = clockSkewSeconds;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static class CredentialProperties {
        private String merchantPublicKey;
        private String platformPrivateKey;

        public String getMerchantPublicKey() {
            return merchantPublicKey;
        }

        public void setMerchantPublicKey(String merchantPublicKey) {
            this.merchantPublicKey = merchantPublicKey;
        }

        public String getPlatformPrivateKey() {
            return platformPrivateKey;
        }

        public void setPlatformPrivateKey(String platformPrivateKey) {
            this.platformPrivateKey = platformPrivateKey;
        }
    }
}
