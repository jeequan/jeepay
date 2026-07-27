package com.jeequan.jeepay.pay.compat.epay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EpayCompatMetadataTest {

    @Test
    void metadataRoundTripDoesNotExposeSecret() {
        String channelExtra = EpayCompatMetadata.encode(valueWithTradeNo("JPAY_ORDER"));

        assertThat(EpayCompatMetadata.decode(channelExtra)).isPresent();
        assertThat(channelExtra).doesNotContain("secret", "private", "publicKey", "merchantKey", "platformPrivateKey");
    }

    @Test
    void encodesOnlyReservedTopLevelNamespace() {
        String channelExtra = EpayCompatMetadata.encode(valueWithTradeNo("JPAY_ORDER"));
        JSONObject root = JSON.parseObject(channelExtra);

        assertThat(root.keySet()).containsExactly("__epay_compat");
        assertThat(root.getJSONObject("__epay_compat").getString("out_trade_no")).isEqualTo("ORDER_TARGET");
        assertThat(root.getJSONObject("__epay_compat").getString("app_id")).isEqualTo("APP_TARGET");
    }

    @Test
    void decodesRequiredFieldsAndPreservesNullableOptionalFields() {
        EpayMetadataValue decoded = EpayCompatMetadata.decode(EpayCompatMetadata.encode(valueWithTradeNo("JPAY_ORDER")))
                .orElseThrow();

        assertThat(decoded.version()).isEqualTo("v1");
        assertThat(decoded.pid()).isEqualTo("MCH_TARGET");
        assertThat(decoded.appId()).isEqualTo("APP_TARGET");
        assertThat(decoded.type()).isEqualTo("alipay");
        assertThat(decoded.outTradeNo()).isEqualTo("ORDER_TARGET");
        assertThat(decoded.notifyUrl()).isEqualTo("https://merchant.example/notify.php");
        assertThat(decoded.returnUrl()).isEqualTo("https://merchant.example/return.php");
        assertThat(decoded.name()).isEqualTo("subject");
        assertThat(decoded.money()).isEqualTo("12.34");
        assertThat(decoded.clientIp()).isEqualTo("203.0.113.9");
        assertThat(decoded.device()).isEqualTo("pc");
        assertThat(decoded.tradeNo()).isEqualTo("JPAY_ORDER");
    }

    @Test
    void returnsEmptyForMalformedJsonWrongShapeAndMissingRequiredFields() {
        assertThat(EpayCompatMetadata.decode("not-json")).isEmpty();
        assertThat(EpayCompatMetadata.decode("[]")).isEmpty();
        assertThat(EpayCompatMetadata.decode("{\"other\":{}}" )).isEmpty();
        assertThat(EpayCompatMetadata.decode("{\"__epay_compat\":\"bad\"}" )).isEmpty();
        assertThat(EpayCompatMetadata.decode("{\"__epay_compat\":{\"version\":\"v1\"}}" )).isEmpty();
    }

    private static EpayMetadataValue valueWithTradeNo(String tradeNo) {
        return new EpayMetadataValue(
                "v1",
                "MCH_TARGET",
                "APP_TARGET",
                "alipay",
                "ORDER_TARGET",
                "https://merchant.example/notify.php",
                "https://merchant.example/return.php",
                "subject",
                "12.34",
                "203.0.113.9",
                "pc",
                tradeNo);
    }
}
