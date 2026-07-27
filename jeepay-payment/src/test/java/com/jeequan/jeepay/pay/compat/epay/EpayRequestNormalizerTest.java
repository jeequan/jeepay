package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.core.constants.CS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EpayRequestNormalizerTest {

    private static final Instant NOW = Instant.parse("2026-07-27T12:00:00Z");

    private EpayRequestNormalizer normalizer;

    @BeforeEach
    void setUp() {
        EpayCompatProperties properties = new EpayCompatProperties();
        properties.setClockSkewSeconds(300);
        normalizer = new EpayRequestNormalizer(Clock.fixed(NOW, ZoneOffset.UTC), properties);
    }

    @Test
    void normalizesV1YuanAmountAndAlipayToStarposQr() {
        Map<String, String> fields = signedFields(
                "pid", "MCH_TARGET",
                "type", "alipay",
                "out_trade_no", "ORDER_TARGET",
                "money", "12.34",
                "name", "subject",
                "notify_url", "https://merchant.example/notify.php",
                "return_url", "https://merchant.example/return.php");

        EpayCreateCommand command = normalizer.normalize(
                fields, "APP_TARGET", credential("secret"), EpayProtocolVersion.V1);

        assertThat(command.merchantId()).isEqualTo("MCH_TARGET");
        assertThat(command.appId()).isEqualTo("APP_TARGET");
        assertThat(command.paymentType()).isEqualTo(CS.PAY_WAY_CODE.STARPOS_QR);
        assertThat(command.amountFen()).isEqualTo(1234L);
        assertThat(command.orderNo()).isEqualTo("ORDER_TARGET");
        assertThat(command.subject()).isEqualTo("subject");
        assertThat(command.notifyUrl()).isEqualTo("https://merchant.example/notify.php");
        assertThat(command.returnUrl()).isEqualTo("https://merchant.example/return.php");
        assertThat(command.version()).isEqualTo("v1");
        assertThat(command.credential()).isEqualTo(credential("secret"));
    }

    @Test
    void normalizesWxpayToStarposQrAndUsesOrderNoWhenSubjectBlank() {
        EpayCreateCommand command = normalizer.normalize(fields(
                        "pid", "MCH_TARGET",
                        "type", "wxpay",
                        "out_trade_no", "ORDER_TARGET",
                        "money", "1",
                        "name", " ",
                        "notify_url", "https://merchant.example/notify.php",
                        "return_url", "https://merchant.example/return.php"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V1);

        assertThat(command.paymentType()).isEqualTo(CS.PAY_WAY_CODE.STARPOS_QR);
        assertThat(command.amountFen()).isEqualTo(100L);
        assertThat(command.subject()).isEqualTo("ORDER_TARGET");
    }

    @Test
    void rejectsMoreThanTwoDecimalPlacesAndUnknownType() {
        assertThatThrownBy(() -> normalizer.normalize(
                validFields("money", "1.001", "type", "alipay"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V1))
                .hasMessageContaining("money");
        assertThatThrownBy(() -> normalizer.normalize(
                validFields("money", "1.00", "type", "bank"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V1))
                .hasMessageContaining("type");
    }

    @Test
    void rejectsNonPositiveMoneyAndMissingRequiredFields() {
        assertThatThrownBy(() -> normalizer.normalize(
                fields("pid", "MCH_TARGET", "type", "alipay", "out_trade_no", "ORDER_TARGET", "money", "0.00",
                        "notify_url", "https://merchant.example/notify.php", "return_url", "https://merchant.example/return.php"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V1))
                .hasMessageContaining("money");
        assertThatThrownBy(() -> normalizer.normalize(
                fields("type", "alipay", "out_trade_no", "ORDER_TARGET", "money", "1.00",
                        "notify_url", "https://merchant.example/notify.php", "return_url", "https://merchant.example/return.php"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V1))
                .hasMessageContaining("pid");
    }

    @Test
    void acceptsHttpAndHttpsNotifyAndReturnUrlsButRejectsOtherSchemes() {
        EpayCreateCommand httpCommand = normalizer.normalize(fields(
                        "pid", "MCH_TARGET", "type", "alipay", "out_trade_no", "ORDER_TARGET", "money", "1.00",
                        "notify_url", "http://merchant.example/notify.php", "return_url", "http://merchant.example/return.php"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V1);
        assertThat(httpCommand.notifyUrl()).isEqualTo("http://merchant.example/notify.php");
        assertThat(httpCommand.returnUrl()).isEqualTo("http://merchant.example/return.php");

        assertThatThrownBy(() -> normalizer.normalize(fields(
                        "pid", "MCH_TARGET", "type", "alipay", "out_trade_no", "ORDER_TARGET", "money", "1.00",
                        "notify_url", "ftp://merchant.example/notify.php", "return_url", "https://merchant.example/return.php"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V1))
                .hasMessageContaining("notify_url");
        assertThatThrownBy(() -> normalizer.normalize(fields(
                        "pid", "MCH_TARGET", "type", "alipay", "out_trade_no", "ORDER_TARGET", "money", "1.00",
                        "notify_url", "https://merchant.example/notify.php", "return_url", "ftp://merchant.example/return.php"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V1))
                .hasMessageContaining("return_url");
    }

    @Test
    void rejectsHttpUrlsWithoutAnAuthority() {
        assertThatThrownBy(() -> normalizer.normalize(fields(
                        "pid", "MCH_TARGET", "type", "alipay", "out_trade_no", "ORDER_TARGET", "money", "1.00",
                        "notify_url", "https://", "return_url", "https://merchant.example/return.php"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V1))
                .hasMessageContaining("notify_url");
    }

    @Test
    void enforcesVersionSpecificSignType() {
        assertThatThrownBy(() -> normalizer.normalize(validFields("sign_type", "RSA"),
                        "APP_TARGET", credential("secret"), EpayProtocolVersion.V1))
                .hasMessageContaining("sign_type");

        assertThatThrownBy(() -> normalizer.normalize(validFields(
                                "timestamp", String.valueOf(NOW.getEpochSecond()), "sign_type", "MD5"),
                        "APP_TARGET", credential("secret"), EpayProtocolVersion.V2))
                .hasMessageContaining("sign_type");

        assertThatThrownBy(() -> normalizer.normalize(validFields(
                                "timestamp", String.valueOf(NOW.getEpochSecond())),
                        "APP_TARGET", credential("secret"), EpayProtocolVersion.V2))
                .hasMessageContaining("sign_type");
    }

    @Test
    void validatesV2TimestampAsUnixSecondsWithinConfiguredSkew() {
        EpayCreateCommand command = normalizer.normalize(fields(
                        "pid", "MCH_TARGET",
                        "type", "alipay",
                        "out_trade_no", "ORDER_TARGET",
                        "money", "1.00",
                        "notify_url", "https://merchant.example/notify.php",
                        "return_url", "https://merchant.example/return.php",
                        "timestamp", String.valueOf(NOW.getEpochSecond()),
                        "sign_type", "RSA",
                        "method", "web"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V2);

        assertThat(command.version()).isEqualTo("v2");
        assertThat(command.method()).isEqualTo("web");

        assertThatThrownBy(() -> normalizer.normalize(fields(
                        "pid", "MCH_TARGET", "type", "alipay", "out_trade_no", "ORDER_TARGET", "money", "1.00",
                        "notify_url", "https://merchant.example/notify.php", "return_url", "https://merchant.example/return.php",
                        "timestamp", String.valueOf(NOW.minusSeconds(301).getEpochSecond()), "sign_type", "RSA"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V2))
                .hasMessageContaining("timestamp");
        assertThatThrownBy(() -> normalizer.normalize(fields(
                        "pid", "MCH_TARGET", "type", "alipay", "out_trade_no", "ORDER_TARGET", "money", "1.00",
                        "notify_url", "https://merchant.example/notify.php", "return_url", "https://merchant.example/return.php",
                        "timestamp", "2026-07-27T12:00:00Z", "sign_type", "RSA"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V2))
                .hasMessageContaining("timestamp");
    }

    @Test
    void rejectsTimestampArithmeticOverflow() {
        assertThatThrownBy(() -> normalizer.normalize(fields(
                        "pid", "MCH_TARGET",
                        "type", "alipay",
                        "out_trade_no", "ORDER_TARGET",
                        "money", "1.00",
                        "notify_url", "https://merchant.example/notify.php",
                        "return_url", "https://merchant.example/return.php",
                        "timestamp", String.valueOf(Long.MIN_VALUE),
                        "sign_type", "RSA"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V2))
                .hasMessageContaining("timestamp");
        assertThatThrownBy(() -> normalizer.normalize(fields(
                        "pid", "MCH_TARGET",
                        "type", "alipay",
                        "out_trade_no", "ORDER_TARGET",
                        "money", "1.00",
                        "notify_url", "https://merchant.example/notify.php",
                        "return_url", "https://merchant.example/return.php",
                        "timestamp", String.valueOf(Long.MAX_VALUE),
                        "sign_type", "RSA"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V2))
                .hasMessageContaining("timestamp");
    }

    @Test
    void ignoresUnknownFieldsButKeepsParamClientIpAndDevice() {
        EpayCreateCommand command = normalizer.normalize(fields(
                        "pid", "MCH_TARGET",
                        "type", "alipay",
                        "out_trade_no", "ORDER_TARGET",
                        "money", "1.00",
                        "notify_url", "https://merchant.example/notify.php",
                        "return_url", "https://merchant.example/return.php",
                        "clientip", "203.0.113.9",
                        "device", "mobile",
                        "param", "opaque=1",
                        "extra", "ignored"),
                "APP_TARGET", credential("secret"), EpayProtocolVersion.V1);

        assertThat(command.clientIp()).isEqualTo("203.0.113.9");
        assertThat(command.device()).isEqualTo("mobile");
    }

    @Test
    void compatPropertiesIsSpringScannableForExternalBinding() {
        assertThat(EpayCompatProperties.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    void resolvesConfiguredAppIdWithoutGuessingFromRequestFields() {
        EpayCompatProperties properties = new EpayCompatProperties();
        properties.setDefaultAppId("APP_DEFAULT");
        properties.getAppIds().put("MCH_TARGET", "APP_MAPPED");

        assertThat(properties.resolveAppId("MCH_TARGET")).isEqualTo("APP_MAPPED");
        assertThat(properties.resolveAppId("OTHER_MCH")).isEqualTo("APP_DEFAULT");

        EpayCompatProperties empty = new EpayCompatProperties();
        assertThatThrownBy(() -> empty.resolveAppId("MCH_TARGET"))
                .hasMessageContaining("epay.compat.default-app-id");
    }

    @Test
    void rejectsNegativeClockSkewConfiguration() {
        EpayCompatProperties properties = new EpayCompatProperties();

        assertThatThrownBy(() -> properties.setClockSkewSeconds(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("clockSkewSeconds");
    }

    private static Map<String, String> signedFields(String... values) {
        Map<String, String> fields = fields(values);
        fields.put("sign", "SIGN_TARGET");
        fields.put("sign_type", "MD5");
        return fields;
    }

    private static Map<String, String> validFields(String... overrides) {
        Map<String, String> fields = fields(
                "pid", "MCH_TARGET",
                "type", "alipay",
                "out_trade_no", "ORDER_TARGET",
                "money", "1.00",
                "notify_url", "https://merchant.example/notify.php",
                "return_url", "https://merchant.example/return.php");
        fields.putAll(fields(overrides));
        return fields;
    }

    private static Map<String, String> fields(String... values) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            fields.put(values[i], values[i + 1]);
        }
        return fields;
    }

    private static EpayCredential credential(String secret) {
        return new EpayCredential(secret, null, null);
    }
}
