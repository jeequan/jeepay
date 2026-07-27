package com.jeequan.jeepay.pay.compat.epay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EpayContractFixtureTest {

    @Test
    void v1FixtureCanonicalAndSignatureAreStable() throws IOException {
        JSONObject request = fixture("epay/fixtures/v1-request.json");
        Map<String, Object> fields = fields(request.getJSONObject("fields"));

        assertThat(EpayCanonicalizer.canonical(fields))
                .isEqualTo(request.getString("canonical"));
        String signature = EpaySigner.signMd5(fields, request.getString("merchant_key"));
        assertThat(signature).isEqualTo(request.getString("signature"));
        assertThat(EpaySigner.verifyMd5(fields, request.getString("merchant_key"), signature)).isTrue();

        JSONObject notify = fixture("epay/fixtures/v1-notify.json");
        assertThat(notify.getString("success_body")).isEqualTo("success");
        assertThat(notify.getString("trade_status")).isEqualTo("TRADE_SUCCESS");
    }

    @Test
    void v2FixtureCanonicalAndSignatureAreStable() throws IOException {
        JSONObject request = fixture("epay/fixtures/v2-request.json");
        Map<String, Object> fields = fields(request.getJSONObject("fields"));

        assertThat(EpayCanonicalizer.canonical(fields))
                .isEqualTo(request.getString("canonical"));
        String signature = EpaySigner.signRsa(fields, request.getString("fixture_private_key"));
        assertThat(signature).isEqualTo(request.getString("signature"));
        assertThat(EpaySigner.verifyRsa(
                fields, request.getString("fixture_public_key"), signature)).isTrue();

        JSONObject notify = fixture("epay/fixtures/v2-notify.json");
        assertThat(notify.getString("sign_type")).isEqualTo("RSA");
        assertThat(notify.getString("trade_status")).isEqualTo("TRADE_SUCCESS");
    }

    @Test
    void responseFixturesContainRequiredVersionFields() throws IOException {
        JSONObject v1 = fixture("epay/fixtures/v1-response.json");
        assertThat(v1.getJSONObject("fields"))
                .containsKeys("code", "msg", "trade_no", "payurl", "sign_type", "sign");
        assertThat(v1.getJSONObject("fields").getInteger("code")).isEqualTo(1);
        assertThat(v1.getJSONObject("fields").getString("sign_type")).isEqualTo("MD5");

        JSONObject v2 = fixture("epay/fixtures/v2-response.json");
        assertThat(v2.getJSONObject("fields"))
                .containsKeys("code", "msg", "trade_no", "pay_type", "pay_info", "sign_type", "sign");
        assertThat(v2.getJSONObject("fields").getInteger("code")).isEqualTo(0);
        assertThat(v2.getJSONObject("fields").getString("sign_type")).isEqualTo("RSA");
    }

    private static JSONObject fixture(String path) throws IOException {
        try (InputStream stream = EpayContractFixtureTest.class.getClassLoader().getResourceAsStream(path)) {
            assertThat(stream).as("fixture %s", path).isNotNull();
            return JSON.parseObject(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static Map<String, Object> fields(JSONObject object) {
        return new LinkedHashMap<>(object.getInnerMap());
    }
}
