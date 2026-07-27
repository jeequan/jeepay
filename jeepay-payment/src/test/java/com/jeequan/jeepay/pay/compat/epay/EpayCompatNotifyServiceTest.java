package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.service.PayOrderProcessService;
import com.jeequan.jeepay.pay.rqrs.payorder.QueryPayOrderRS;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EpayCompatNotifyServiceTest {

    @Test
    void createsSignedV1NotifyUrlWithTradeSuccess() {
        PayOrder order = compatOrder("v1", PayOrder.STATE_SUCCESS);
        EpayCompatNotifyService service = service(new EpayCredential("merchant-key", null, null));

        String notifyUrl = service.createNotifyUrl(order);
        Map<String, String> fields = query(notifyUrl);

        assertThat(fields).containsEntry("pid", "MCH-1001")
                .containsEntry("trade_no", "JPAY-1001")
                .containsEntry("out_trade_no", "ORDER-1001")
                .containsEntry("type", "alipay")
                .containsEntry("money", "12.34")
                .containsEntry("trade_status", "TRADE_SUCCESS")
                .containsEntry("sign_type", "MD5");
        assertThat(EpaySigner.verifyMd5(fields, "merchant-key", fields.get("sign"))).isTrue();
    }

    @Test
    void createsSignedV2NotifyUrlWithPlatformPrivateKey() throws Exception {
        KeyPair merchantKeys = keyPair();
        KeyPair platformKeys = keyPair();
        EpayCredential credential = new EpayCredential(
                null,
                pem(merchantKeys.getPublic().getEncoded(), "PUBLIC"),
                pem(platformKeys.getPrivate().getEncoded(), "PRIVATE"));
        EpayCompatNotifyService service = service(credential);
        PayOrder order = compatOrder("v2", PayOrder.STATE_SUCCESS);

        Map<String, String> fields = query(service.createNotifyUrl(order));

        assertThat(fields).containsEntry("sign_type", "RSA")
                .containsEntry("trade_status", "TRADE_SUCCESS");
        assertThat(EpaySigner.verifyRsa(
                fields,
                pem(platformKeys.getPublic().getEncoded(), "PUBLIC"),
                fields.get("sign"))).isTrue();
    }

    @Test
    void v2NotifyUrlIsEmptyWithoutPlatformPrivateKey() {
        EpayCompatNotifyService service = service(
                new EpayCredential(null, "merchant-public", null));

        assertThat(service.createNotifyUrl(compatOrder("v2", PayOrder.STATE_SUCCESS))).isEmpty();
    }

    @Test
    void v2CallbackVerifiesWithMerchantPublicKeyWhenPlatformPrivateKeyIsAbsent() throws Exception {
        KeyPair merchantKeys = keyPair();
        EpayCredential credential = new EpayCredential(
                null,
                pem(merchantKeys.getPublic().getEncoded(), "PUBLIC"),
                null);
        PayOrderService payOrderService = mock(PayOrderService.class);
        PayOrder order = compatOrder("v2", PayOrder.STATE_ING);
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(order);
        when(payOrderService.updateIng2Success("JPAY-1001", "JPAY-1001", null)).thenReturn(true);
        EpayCompatProperties properties = properties();
        properties.setDefaultAppId("WRONG-APP");
        EpayCompatNotifyService service = new EpayCompatNotifyService(
                payOrderService,
                properties,
                (pid, appId, version) -> {
                    if (!"APP-1001".equals(appId)) {
                        throw new IllegalStateException("metadata app id was not used");
                    }
                    return credential;
                });

        Map<String, String> callback = callbackFields("12.34", order);
        callback.put("sign_type", "RSA");
        callback.put("sign", EpaySigner.signRsa(
                callback, pem(merchantKeys.getPrivate().getEncoded(), "PRIVATE")));

        assertThat(service.handleCallback(callback)).isEqualTo("success");
        verify(payOrderService).updateIng2Success("JPAY-1001", "JPAY-1001", null);
    }

    @Test
    void returnUrlIsEmptyWhenCompatMetadataHasNoReturnUrl() {
        PayOrder order = compatOrder("v1", PayOrder.STATE_SUCCESS)
                .setChannelExtra(EpayCompatMetadata.encode(new EpayMetadataValue(
                        "v1", "MCH-1001", "APP-1001", "alipay", "ORDER-1001",
                        "https://merchant.example/notify.php", "", "Demo", "12.34",
                        null, null, "JPAY-1001")));
        EpayCompatNotifyService service = service(new EpayCredential("merchant-key", null, null));

        assertThat(service.createReturnUrl(order)).isEmpty();
    }

    @Test
    void intermediateOrderDoesNotCreateCompatSuccessNotification() {
        PayOrder order = compatOrder("v1", PayOrder.STATE_ING);
        EpayCompatNotifyService service = service(new EpayCredential("merchant-key", null, null));

        assertThat(service.createNotifyUrl(order)).isEmpty();
        assertThat(service.createReturnUrl(order)).isEmpty();
    }

    @Test
    void duplicateSuccessfulCallbackReturnsSuccessWithoutSecondStateTransition() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        PayOrder order = compatOrder("v1", PayOrder.STATE_SUCCESS);
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(order);
        EpayCompatNotifyService service = service(payOrderService, new EpayCredential("merchant-key", null, null));
        Map<String, String> callback = callbackFields("12.34", order);
        callback.put("sign", EpaySigner.signMd5(callback, "merchant-key"));

        assertThat(service.handleCallback(callback)).isEqualTo("success");
        verify(payOrderService, never()).updateIng2Success(
                "JPAY-1001", "JPAY-1001", null);
    }

    @Test
    void validReturnCallbackDoesNotTransitionOrderState() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        PayOrder order = compatOrder("v1", PayOrder.STATE_ING);
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(order);
        EpayCompatNotifyService service = service(payOrderService,
                new EpayCredential("merchant-key", null, null));
        Map<String, String> callback = callbackFields("12.34", order);
        callback.put("sign", EpaySigner.signMd5(callback, "merchant-key"));

        assertThat(service.handleReturn(callback)).isEqualTo("success");
        verify(payOrderService, never()).updateIng2Success(
                "JPAY-1001", "JPAY-1001", null);
        verify(payOrderService, never()).updateIng2Fail(
                "JPAY-1001", "JPAY-1001", null, "TRADE_CLOSED", "EPAY交易关闭");
    }

    @Test
    void successfulCallbackRunsCommonSuccessPostProcessingOnce() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        PayOrderProcessService processService = mock(PayOrderProcessService.class);
        PayOrder order = compatOrder("v1", PayOrder.STATE_ING);
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(order);
        when(payOrderService.updateIng2Success("JPAY-1001", "JPAY-1001", null)).thenReturn(true);
        EpayCompatNotifyService service = service(payOrderService,
                new EpayCredential("merchant-key", null, null), processService);
        Map<String, String> callback = callbackFields("12.34", order);
        callback.put("sign", EpaySigner.signMd5(callback, "merchant-key"));

        assertThat(service.handleCallback(callback)).isEqualTo("success");
        verify(payOrderService).updateIng2Success("JPAY-1001", "JPAY-1001", null);
        verify(processService).confirmSuccess(order);
    }

    @Test
    void concurrentSuccessfulCallbackReturnsSuccessAfterStateRefresh() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        PayOrder order = compatOrder("v1", PayOrder.STATE_ING);
        PayOrder refreshed = compatOrder("v1", PayOrder.STATE_SUCCESS);
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001"))
                .thenReturn(order, refreshed);
        when(payOrderService.updateIng2Success("JPAY-1001", "JPAY-1001", null)).thenReturn(false);
        EpayCompatNotifyService service = service(payOrderService,
                new EpayCredential("merchant-key", null, null));
        Map<String, String> callback = callbackFields("12.34", order);
        callback.put("sign", EpaySigner.signMd5(callback, "merchant-key"));

        assertThat(service.handleCallback(callback)).isEqualTo("success");
        verify(payOrderService).updateIng2Success("JPAY-1001", "JPAY-1001", null);
    }

    @Test
    void callbackAmountMismatchReturnsFail() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        PayOrder order = compatOrder("v1", PayOrder.STATE_ING);
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(order);
        EpayCompatNotifyService service = service(payOrderService, new EpayCredential("merchant-key", null, null));
        Map<String, String> callback = callbackFields("12.35", order);
        callback.put("sign", EpaySigner.signMd5(callback, "merchant-key"));

        assertThat(service.handleCallback(callback)).isEqualTo("fail");
        verify(payOrderService, never()).updateIng2Success(
                "JPAY-1001", "JPAY-1001", null);
    }

    @Test
    void v2OrderRejectsMd5CallbackEvenWhenMerchantKeyIsAvailable() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        PayOrder order = compatOrder("v2", PayOrder.STATE_ING);
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(order);
        EpayCompatNotifyService service = service(payOrderService,
                new EpayCredential("merchant-key", "merchant-public", null));
        Map<String, String> callback = callbackFields("12.34", order);
        callback.put("sign_type", "MD5");
        callback.put("sign", EpaySigner.signMd5(callback, "merchant-key"));

        assertThat(service.handleCallback(callback)).isEqualTo("fail");
        verify(payOrderService, never()).updateIng2Success(
                "JPAY-1001", "JPAY-1001", null);
    }

    @Test
    void v1OrderRejectsRsaCallbackEvenWhenMerchantKeyIsAvailable() throws Exception {
        KeyPair merchantKeys = keyPair();
        PayOrderService payOrderService = mock(PayOrderService.class);
        PayOrder order = compatOrder("v1", PayOrder.STATE_ING);
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(order);
        EpayCompatNotifyService service = service(payOrderService,
                new EpayCredential("merchant-key",
                        pem(merchantKeys.getPublic().getEncoded(), "PUBLIC"), null));
        Map<String, String> callback = callbackFields("12.34", order);
        callback.put("sign_type", "RSA");
        callback.put("sign", EpaySigner.signRsa(
                callback, pem(merchantKeys.getPrivate().getEncoded(), "PRIVATE")));

        assertThat(service.handleCallback(callback)).isEqualTo("fail");
        verify(payOrderService, never()).updateIng2Success(
                "JPAY-1001", "JPAY-1001", null);
    }

    @Test
    void nativePayOrderStillUsesExistingJeepayNotifyShape() {
        PayOrder order = new PayOrder()
                .setPayOrderId("JPAY-NATIVE")
                .setMchNo("MCH-1001")
                .setMchOrderNo("ORDER-NATIVE")
                .setAmount(1234L)
                .setState(PayOrder.STATE_SUCCESS)
                .setNotifyUrl("https://merchant.example/native");

        String notifyUrl = new com.jeequan.jeepay.pay.service.PayMchNotifyService()
                .createNotifyUrl(order, "merchant-key");
        assertThat(QueryPayOrderRS.buildByPayOrder(order).getPayOrderId()).isEqualTo("JPAY-NATIVE");
        assertThat(notifyUrl).contains("payOrderId=JPAY-NATIVE")
                .contains("mchOrderNo=ORDER-NATIVE")
                .doesNotContain("trade_status=TRADE_SUCCESS");
    }

    private static EpayCompatNotifyService service(EpayCredential credential) {
        return service(mock(PayOrderService.class), credential);
    }

    private static EpayCompatNotifyService service(PayOrderService payOrderService, EpayCredential credential) {
        return new EpayCompatNotifyService(payOrderService, properties(), (pid, appId, version) -> credential);
    }

    private static EpayCompatNotifyService service(PayOrderService payOrderService,
                                                   EpayCredential credential,
                                                   PayOrderProcessService processService) {
        return new EpayCompatNotifyService(
                payOrderService, properties(), (pid, appId, version) -> credential, processService);
    }

    private static EpayCompatProperties properties() {
        EpayCompatProperties properties = new EpayCompatProperties();
        properties.setDefaultAppId("APP-1001");
        return properties;
    }

    private static PayOrder compatOrder(String version, byte state) {
        return new PayOrder()
                .setPayOrderId("JPAY-1001")
                .setMchNo("MCH-1001")
                .setAppId("APP-1001")
                .setMchOrderNo("ORDER-1001")
                .setAmount(1234L)
                .setState(state)
                .setChannelOrderNo("JPAY-1001")
                .setWayCode("STARPOS_QR")
                .setNotifyUrl("https://merchant.example/notify.php")
                .setReturnUrl("https://merchant.example/return.php")
                .setChannelExtra(EpayCompatMetadata.encode(new EpayMetadataValue(
                        version, "MCH-1001", "APP-1001", "alipay", "ORDER-1001",
                        "https://merchant.example/notify.php", "https://merchant.example/return.php",
                        "Demo", "12.34", null, null, "JPAY-1001")));
    }

    private static Map<String, String> callbackFields(String money, PayOrder order) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("pid", "MCH-1001");
        fields.put("trade_no", order.getPayOrderId());
        fields.put("out_trade_no", order.getMchOrderNo());
        fields.put("type", "alipay");
        fields.put("name", "Demo");
        fields.put("money", money);
        fields.put("trade_status", "TRADE_SUCCESS");
        fields.put("sign_type", "MD5");
        return fields;
    }

    private static Map<String, String> query(String url) {
        Map<String, String> result = new LinkedHashMap<>();
        String query = url.substring(url.indexOf('?') + 1);
        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);
            result.put(pair[0], pair.length == 1 ? "" : pair[1]);
        }
        return result;
    }

    private static KeyPair keyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String pem(byte[] encoded, String type) {
        return "-----BEGIN " + type + " KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encoded)
                + "\n-----END " + type + " KEY-----";
    }
}
