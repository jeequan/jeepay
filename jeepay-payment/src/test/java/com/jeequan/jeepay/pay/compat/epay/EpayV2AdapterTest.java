package com.jeequan.jeepay.pay.compat.epay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EpayV2AdapterTest {

    @Test
    void successfulResponseSigningFailureBecomesExplicitFailure() {
        EpayCreateResult result = new EpayCreateResult(
                true, "success", "JPAY-001", "redirect", "https://cashier.example/JPAY-001", null);

        EpayCompatResponse response = new EpayV2Adapter().success(
                result, new EpayCredential(null, null, "not-a-private-key"));
        JSONObject body = JSON.parseObject(response.asJson().getBody());

        assertThat(response.asSubmitResponse().getStatusCode().value()).isEqualTo(200);
        assertThat(body.getInteger("code")).isEqualTo(1);
        assertThat(body.getString("msg")).contains("签名失败");
        assertThat(body.getString("trade_no")).isEmpty();
        assertThat(body.getString("sign")).isEmpty();
    }
}
