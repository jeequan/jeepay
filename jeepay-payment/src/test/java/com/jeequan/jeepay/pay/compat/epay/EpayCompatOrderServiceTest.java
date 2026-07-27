package com.jeequan.jeepay.pay.compat.epay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.StarposQrOrderRS;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EpayCompatOrderServiceTest {

    @Test
    void duplicateSameOrderReturnsOriginalTradeNoWithoutSecondStarposCall() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        EpayCompatOrderService service = new EpayCompatOrderService(payOrderService);
        EpayCreateCommand command = command("alipay", 1234L);
        PayOrder existing = compatibleOrder(command, "JPAY-EXISTING", "https://cashier.example/JPAY-EXISTING");
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001"))
                .thenReturn(existing);

        PayOrder result = service.findExisting(command);

        assertThat(result).isSameAs(existing);
        assertThat(result.getPayOrderId()).isEqualTo("JPAY-EXISTING");
        verify(payOrderService).queryMchOrder("MCH-1001", null, "ORDER-1001");
    }

    @Test
    void duplicateAmountMismatchReturnsConflict() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        EpayCompatOrderService service = new EpayCompatOrderService(payOrderService);
        EpayCreateCommand command = command("alipay", 1234L);
        PayOrder existing = compatibleOrder(command, "JPAY-EXISTING", "https://cashier.example/JPAY-EXISTING");
        existing.setAmount(999L);
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001"))
                .thenReturn(existing);

        assertThatThrownBy(() -> service.findExisting(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("金额");
    }

    @Test
    void starposTimeoutReturnsUnknownAndKeepsOrderQueryable() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        EpayCompatOrderService service = new EpayCompatOrderService(payOrderService);
        EpayCreateCommand command = command("alipay", 1234L);
        PayOrder queryable = compatibleOrder(command, "JPAY-TIMEOUT", null)
                .setState(PayOrder.STATE_ING);
        UnifiedOrderRS rs = new UnifiedOrderRS();
        rs.setPayOrderId("JPAY-TIMEOUT");
        rs.setMchOrderNo(command.orderNo());
        rs.setOrderState(PayOrder.STATE_ING);
        when(payOrderService.queryMchOrder("MCH-1001", "JPAY-TIMEOUT", null))
                .thenReturn(queryable);

        EpayCreateResult result = service.fromApiResult(ApiRes.ok(rs), command);

        assertThat(result.success()).isFalse();
        assertThat(result.tradeNo()).isEqualTo("JPAY-TIMEOUT");
        assertThat(result.payOrder()).isSameAs(queryable);
        assertThat(result.message()).contains("未知");
        verify(payOrderService).queryMchOrder("MCH-1001", "JPAY-TIMEOUT", null);
    }

    @Test
    void unifiedRequestPersistsEpayMetadataInChannelExtraWithoutExtParam() {
        EpayCompatOrderService service = new EpayCompatOrderService(mock(PayOrderService.class));
        EpayCreateCommand command = command("wxpay", 1234L);

        var rq = service.toUnifiedOrderRequest(command);
        JSONObject channelExtra = JSON.parseObject(rq.getChannelExtra());

        assertThat(rq.getWayCode()).isEqualTo(CS.PAY_WAY_CODE.STARPOS_QR);
        assertThat(channelExtra.getString("payDataType")).isEqualTo("payUrl");
        assertThat(EpayCompatMetadata.decode(rq.getChannelExtra())).isPresent();
        assertThat(rq.getExtParam()).isNull();
        assertThat(rq.getChannelExtra()).contains("__epay_compat");
    }

    @Test
    void initialMetadataLeavesTradeNoEmptyAndKeepsInitOrderIdempotent() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        EpayCompatOrderService service = new EpayCompatOrderService(payOrderService);
        EpayCreateCommand command = command("wxpay", 1234L);
        PayOrder initialOrder = compatibleOrder(command, null, null)
                .setPayOrderId("JPAY-INIT")
                .setState(PayOrder.STATE_INIT);
        when(payOrderService.queryMchOrder("MCH-1001", null, "ORDER-1001")).thenReturn(initialOrder);

        var request = service.toUnifiedOrderRequest(command);
        EpayMetadataValue initialMetadata = EpayCompatMetadata.decode(request.getChannelExtra()).orElseThrow();
        PayOrder existing = service.findExisting(command);
        EpayCreateResult result = service.fromExisting(existing, command);

        assertThat(initialMetadata.tradeNo()).isNull();
        assertThat(existing).isSameAs(initialOrder);
        assertThat(result.success()).isFalse();
        assertThat(result.tradeNo()).isEqualTo("JPAY-INIT");
        assertThat(result.message()).contains("可通过交易号查询");
    }

    @Test
    void apiResultPersistsEpayMetadataInPayOrderChannelExtraWithoutExtParam() {
        PayOrderService payOrderService = mock(PayOrderService.class);
        EpayCompatOrderService service = new EpayCompatOrderService(payOrderService);
        EpayCreateCommand command = command("wxpay", 1234L);
        PayOrder savedOrder = new PayOrder()
                .setPayOrderId("JPAY-1001")
                .setExtParam(null);
        StarposQrOrderRS rs = new StarposQrOrderRS();
        rs.setPayOrderId("JPAY-1001");
        rs.setMchOrderNo(command.orderNo());
        rs.setOrderState(PayOrder.STATE_SUCCESS);
        rs.setPayUrl("https://cashier.example/JPAY-1001");
        when(payOrderService.queryMchOrder("MCH-1001", "JPAY-1001", null)).thenReturn(savedOrder);

        EpayCreateResult result = service.fromApiResult(ApiRes.ok(rs), command);

        ArgumentCaptor<PayOrder> orderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderService).updateById(orderCaptor.capture());
        PayOrder persisted = orderCaptor.getValue();
        JSONObject channelExtra = JSON.parseObject(persisted.getChannelExtra());
        assertThat(result.success()).isTrue();
        assertThat(persisted.getPayOrderId()).isEqualTo("JPAY-1001");
        assertThat(persisted.getState()).isNull();
        assertThat(persisted.getChannelOrderNo()).isNull();
        assertThat(persisted.getAmount()).isNull();
        assertThat(EpayCompatMetadata.decode(persisted.getChannelExtra())).isPresent();
        assertThat(channelExtra.getString("payDataType")).isEqualTo("payUrl");
        assertThat(channelExtra.getJSONObject("__epay_compat").getString("pay_info"))
                .isEqualTo("https://cashier.example/JPAY-1001");
        assertThat(persisted.getExtParam()).isNull();
    }

    @Test
    void compatTypesExposeUnambiguousSpringWiring() throws Exception {
        assertThat(EpayCompatOrderService.class.isAnnotationPresent(Service.class)).isTrue();
        assertThat(EpayCompatController.class
                .getConstructor(EpayCompatProperties.class, EpayCredentialResolver.class, EpayCompatOrderService.class)
                .isAnnotationPresent(Autowired.class)).isTrue();
    }

    @Test
    void credentialResolverUsesAppSecretForV1AndExternalRsaKeysForV2() throws Exception {
        MchApp mchApp = new MchApp().setAppSecret("v1-merchant-key");
        EpayCompatProperties properties = new EpayCompatProperties();
        EpayCompatProperties.CredentialProperties rsa = new EpayCompatProperties.CredentialProperties();
        KeyPair platformKeys = keyPair();
        rsa.setMerchantPublicKey("merchant-public");
        rsa.setPlatformPrivateKey(pem(platformKeys.getPrivate().getEncoded(), "PRIVATE"));
        properties.setCredentials(Map.of("MCH-1001", Map.of("APP-1001", rsa)));
        var queryService = mock(com.jeequan.jeepay.pay.service.ConfigContextQueryService.class);
        when(queryService.queryMchApp("MCH-1001", "APP-1001")).thenReturn(mchApp);
        DefaultEpayCredentialResolver resolver = new DefaultEpayCredentialResolver(queryService, properties);

        assertThat(resolver.resolve("MCH-1001", "APP-1001", EpayProtocolVersion.V1))
                .isEqualTo(new EpayCredential("v1-merchant-key", null, null));
        assertThat(resolver.resolve("MCH-1001", "APP-1001", EpayProtocolVersion.V2))
                .isEqualTo(new EpayCredential(null, "merchant-public", rsa.getPlatformPrivateKey()));
    }

    @Test
    void credentialResolverRejectsMalformedV2PlatformPrivateKey() {
        EpayCompatProperties properties = new EpayCompatProperties();
        EpayCompatProperties.CredentialProperties rsa = new EpayCompatProperties.CredentialProperties();
        rsa.setMerchantPublicKey("merchant-public");
        rsa.setPlatformPrivateKey("not-a-private-key");
        properties.setCredentials(Map.of("MCH-1001", Map.of("APP-1001", rsa)));
        var queryService = mock(com.jeequan.jeepay.pay.service.ConfigContextQueryService.class);
        DefaultEpayCredentialResolver resolver = new DefaultEpayCredentialResolver(queryService, properties);

        assertThatThrownBy(() -> resolver.resolve("MCH-1001", "APP-1001", EpayProtocolVersion.V2))
                .hasMessageContaining("私钥");
    }

    static EpayCreateCommand command(String type, long amountFen) {
        return new EpayCreateCommand(
                "MCH-1001", "APP-1001", "ORDER-1001", CS.PAY_WAY_CODE.STARPOS_QR,
                "test subject", amountFen,
                "https://merchant.example/notify.php", "https://merchant.example/return.php",
                "203.0.113.10", "pc", "v1", "mapi", type,
                new EpayCredential("merchant-key", null, null)
        );
    }

    static PayOrder compatibleOrder(EpayCreateCommand command, String tradeNo, String payInfo) {
        JSONObject channelExtra = JSON.parseObject(EpayCompatMetadata.encode(new EpayMetadataValue(
                command.version(), command.merchantId(), command.appId(), command.epayType(), command.orderNo(),
                command.notifyUrl(), command.returnUrl(), command.subject(), "12.34", command.clientIp(),
                command.device(), tradeNo
        )));
        channelExtra.put("payDataType", "payUrl");
        channelExtra.getJSONObject("__epay_compat").put("pay_info", payInfo);
        return new PayOrder()
                .setPayOrderId(tradeNo)
                .setMchNo(command.merchantId())
                .setAppId(command.appId())
                .setMchOrderNo(command.orderNo())
                .setAmount(command.amountFen())
                .setIfCode(CS.IF_CODE.STARPOS)
                .setWayCode(CS.PAY_WAY_CODE.STARPOS_QR)
                .setNotifyUrl(command.notifyUrl())
                .setReturnUrl(command.returnUrl())
                .setChannelExtra(channelExtra.toJSONString());
    }

    private static KeyPair keyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        return generator.generateKeyPair();
    }

    private static String pem(byte[] encoded, String type) {
        return "-----BEGIN " + type + " KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded)
                + "\n-----END " + type + " KEY-----";
    }
}
