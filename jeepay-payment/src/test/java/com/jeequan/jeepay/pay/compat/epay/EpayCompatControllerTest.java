package com.jeequan.jeepay.pay.compat.epay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.StarposQrOrderRS;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EpayCompatControllerTest {

    private static final String STARPOS_CASHIER_BASE = "https://xyf-server-test.postar.cn/cashier/";

    @Test
    void v1MapiReturnsCodeOneAndPayurlOnSuccess() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        TestController controller = controller(payOrderService, new EpayCredential("merchant-key", null, null));
        controller.apiResult = successApi("JPAY-V1", STARPOS_CASHIER_BASE + "JPAY-V1");
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(null);
        when(payOrderService.queryMchOrder("MCH-1001", "JPAY-V1", null))
                .thenReturn(EpayCompatOrderServiceTest.compatibleOrder(
                        EpayCompatOrderServiceTest.command("alipay", 1234L), "JPAY-V1", STARPOS_CASHIER_BASE + "JPAY-V1"));
        Map<String, String> fields = v1Fields();
        fields.put("sign", EpaySigner.signMd5(fields, "merchant-key"));

        ResponseEntity<String> response = controller.mapi(fields);
        JSONObject body = JSON.parseObject(response.getBody());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(body.getInteger("code")).isEqualTo(1);
        assertThat(body.getString("trade_no")).isEqualTo("JPAY-V1");
        assertThat(body.getString("payurl")).isEqualTo(STARPOS_CASHIER_BASE + "JPAY-V1");
        assertThat(EpaySigner.verifyMd5(body, "merchant-key", body.getString("sign"))).isTrue();
        assertThat(controller.unifiedCalls).isEqualTo(1);
        assertThat(controller.capturedRequest.getExtParam()).isNull();
        assertThat(controller.capturedRequest.getChannelExtra()).contains("__epay_compat");
    }

    @Test
    void v2MapiReturnsCodeZeroAndRedirectPayInfoOnSuccess() throws Exception {
        KeyPair merchantKeys = keyPair();
        KeyPair platformKeys = keyPair();
        EpayCredential credential = new EpayCredential(
                null, pem(merchantKeys.getPublic().getEncoded(), "PUBLIC"),
                pem(platformKeys.getPrivate().getEncoded(), "PRIVATE")
        );
        PayOrderService payOrderService = mock(PayOrderService.class);
        TestController controller = controller(payOrderService, credential);
        controller.apiResult = successApi("JPAY-V2", STARPOS_CASHIER_BASE + "JPAY-V2");
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(null);
        when(payOrderService.queryMchOrder("MCH-1001", "JPAY-V2", null))
                .thenReturn(EpayCompatOrderServiceTest.compatibleOrder(
                        EpayCompatOrderServiceTest.command("alipay", 1234L), "JPAY-V2", STARPOS_CASHIER_BASE + "JPAY-V2"));
        Map<String, String> fields = v2Fields();
        fields.put("sign", EpaySigner.signRsa(fields, pem(merchantKeys.getPrivate().getEncoded(), "PRIVATE")));

        ResponseEntity<String> response = controller.mapi(fields);
        JSONObject body = JSON.parseObject(response.getBody());

        assertThat(body.getInteger("code")).isZero();
        assertThat(body.getString("trade_no")).isEqualTo("JPAY-V2");
        assertThat(body.getString("pay_type")).isEqualTo("redirect");
        assertThat(body.getString("pay_info")).isEqualTo(STARPOS_CASHIER_BASE + "JPAY-V2");
        assertThat(EpaySigner.verifyRsa(body, pem(platformKeys.getPublic().getEncoded(), "PUBLIC"), body.getString("sign")))
                .isTrue();
    }

    @Test
    void submitEndpointRedirectsOnlyToValidatedHttpsCashierUrl() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        TestController controller = controller(payOrderService, new EpayCredential("merchant-key", null, null));
        controller.apiResult = successApi("JPAY-SUBMIT", STARPOS_CASHIER_BASE + "JPAY-SUBMIT");
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(null);
        when(payOrderService.queryMchOrder("MCH-1001", "JPAY-SUBMIT", null))
                .thenReturn(EpayCompatOrderServiceTest.compatibleOrder(
                        EpayCompatOrderServiceTest.command("alipay", 1234L), "JPAY-SUBMIT", STARPOS_CASHIER_BASE + "JPAY-SUBMIT"));
        Map<String, String> fields = v1Fields();
        fields.put("sign", EpaySigner.signMd5(fields, "merchant-key"));

        ResponseEntity<?> response = controller.submit(fields);

        assertThat(response.getStatusCode().value()).isEqualTo(302);
        assertThat(response.getHeaders().getLocation()).hasToString(STARPOS_CASHIER_BASE + "JPAY-SUBMIT");

        controller.apiResult = successApi("JPAY-UNTRUSTED", "https://cashier.example/JPAY-UNTRUSTED");
        when(payOrderService.queryMchOrder("MCH-1001", "JPAY-UNTRUSTED", null))
                .thenReturn(EpayCompatOrderServiceTest.compatibleOrder(
                        EpayCompatOrderServiceTest.command("alipay", 1234L), "JPAY-UNTRUSTED", "https://cashier.example/JPAY-UNTRUSTED"));
        ResponseEntity<?> rejected = controller.submit(fields);

        assertThat(rejected.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void invalidSignatureReturnsVersionSpecificFailureJson() throws Exception {
        PayOrderService payOrderService = mock(PayOrderService.class);
        TestController v1 = controller(payOrderService, new EpayCredential("merchant-key", null, null));

        JSONObject v1Body = JSON.parseObject(v1.mapi(v1Fields()).getBody());
        assertThat(v1Body.getInteger("code")).isZero();
        assertThat(v1Body.getString("trade_no")).isEmpty();

        KeyPair merchantKeys = keyPair();
        KeyPair platformKeys = keyPair();
        TestController v2 = controller(payOrderService, new EpayCredential(
                null, pem(merchantKeys.getPublic().getEncoded(), "PUBLIC"),
                pem(platformKeys.getPrivate().getEncoded(), "PRIVATE")
        ));
        JSONObject v2Body = JSON.parseObject(v2.mapi(v2Fields()).getBody());
        assertThat(v2Body.getInteger("code")).isEqualTo(1);
        assertThat(v2Body.getString("trade_no")).isEmpty();
    }

    private static TestController controller(PayOrderService payOrderService, EpayCredential credential) {
        EpayCompatProperties properties = new EpayCompatProperties();
        properties.setDefaultAppId("APP-1001");
        EpayCredentialResolver resolver = mock(EpayCredentialResolver.class);
        when(resolver.resolve("MCH-1001", "APP-1001", EpayProtocolVersion.V1)).thenReturn(credential);
        when(resolver.resolve("MCH-1001", "APP-1001", EpayProtocolVersion.V2)).thenReturn(credential);
        EpayCompatOrderService orderService = new EpayCompatOrderService(payOrderService);
        return new TestController(properties, resolver,
                new EpayRequestNormalizer(Clock.systemUTC(), properties), orderService);
    }

    private static ApiRes successApi(String tradeNo, String payUrl) {
        StarposQrOrderRS rs = new StarposQrOrderRS();
        rs.setPayOrderId(tradeNo);
        rs.setMchOrderNo("ORDER-1001");
        rs.setOrderState(PayOrder.STATE_SUCCESS);
        rs.setPayUrl(payUrl);
        return ApiRes.ok(rs);
    }

    private static Map<String, String> v1Fields() {
        return baseFields();
    }

    private static Map<String, String> v2Fields() {
        Map<String, String> fields = baseFields();
        fields.put("sign_type", "RSA");
        fields.put("timestamp", String.valueOf(java.time.Instant.now().getEpochSecond()));
        return fields;
    }

    private static Map<String, String> baseFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("pid", "MCH-1001");
        fields.put("type", "alipay");
        fields.put("out_trade_no", "ORDER-1001");
        fields.put("money", "12.34");
        fields.put("name", "test subject");
        fields.put("notify_url", "https://merchant.example/notify.php");
        fields.put("return_url", "https://merchant.example/return.php");
        fields.put("clientip", "203.0.113.10");
        return fields;
    }

    private static KeyPair keyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String pem(byte[] encoded, String type) {
        return "-----BEGIN " + type + " KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded)
                + "\n-----END " + type + " KEY-----";
    }

    private static final class TestController extends EpayCompatController {
        private ApiRes apiResult;
        private int unifiedCalls;
        private com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ capturedRequest;

        private TestController(EpayCompatProperties properties,
                               EpayCredentialResolver credentialResolver,
                               EpayRequestNormalizer requestNormalizer,
                               EpayCompatOrderService orderService) {
            super(properties, credentialResolver, requestNormalizer, orderService);
        }

        @Override
        protected ApiRes unifiedOrder(String wayCode, com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ request) {
            unifiedCalls++;
            capturedRequest = request;
            return apiResult;
        }
    }
}
