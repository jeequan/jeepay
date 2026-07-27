package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.utils.StringKit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

public final class EpayRequestNormalizer {

    private final Clock clock;
    private final EpayCompatProperties properties;

    public EpayRequestNormalizer(Clock clock, EpayCompatProperties properties) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.properties = properties == null ? new EpayCompatProperties() : properties;
    }

    public EpayCreateCommand normalize(Map<String, String> fields,
                                       String appId,
                                       EpayCredential credential,
                                       EpayProtocolVersion version) {
        if (fields == null) {
            throw new IllegalArgumentException("fields is required");
        }
        String pid = required(fields, "pid");
        String orderNo = required(fields, "out_trade_no");
        String epayType = required(fields, "type").toLowerCase();
        if (!"alipay".equals(epayType) && !"wxpay".equals(epayType)) {
            throw new IllegalArgumentException("type must be alipay or wxpay");
        }

        long amountFen = amountFen(required(fields, "money"));
        String notifyUrl = httpsUrl(fields, "notify_url");
        String returnUrl = httpsUrl(fields, "return_url");
        String subject = trimToNull(fields.get("name"));
        if (subject == null) {
            subject = orderNo;
        }
        EpayProtocolVersion effectiveVersion = version == null ? EpayProtocolVersion.V1 : version;
        if (effectiveVersion == EpayProtocolVersion.V2) {
            validateV2Timestamp(fields.get("timestamp"));
        }

        return new EpayCreateCommand(
                pid,
                appId,
                orderNo,
                CS.PAY_WAY_CODE.STARPOS_QR,
                subject,
                amountFen,
                notifyUrl,
                returnUrl,
                trimToNull(fields.get("clientip")),
                trimToNull(fields.get("device")),
                effectiveVersion == EpayProtocolVersion.V1 ? "v1" : "v2",
                trimToNull(fields.get("method")),
                epayType,
                credential);
    }

    private long amountFen(String money) {
        try {
            BigDecimal yuan = new BigDecimal(money).setScale(2, RoundingMode.UNNECESSARY);
            if (yuan.signum() <= 0) {
                throw new IllegalArgumentException("money must be positive");
            }
            return yuan.movePointRight(2).longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException("money must be positive with at most two decimal places", e);
        }
    }

    private void validateV2Timestamp(String timestamp) {
        if (trimToNull(timestamp) == null) {
            throw new IllegalArgumentException("timestamp is required");
        }
        long epochSeconds;
        try {
            epochSeconds = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("timestamp must be Unix seconds", e);
        }
        long now = Instant.now(clock).getEpochSecond();
        if (Math.abs(now - epochSeconds) > properties.getClockSkewSeconds()) {
            throw new IllegalArgumentException("timestamp is outside allowed clock skew");
        }
    }

    private static String httpsUrl(Map<String, String> fields, String key) {
        String value = required(fields, key);
        if (!StringKit.isAvailableUrl(value) || !value.startsWith("https://")) {
            throw new IllegalArgumentException(key + " must be a valid HTTPS URL");
        }
        return value;
    }

    private static String required(Map<String, String> fields, String key) {
        String value = trimToNull(fields.get(key));
        if (value == null) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
