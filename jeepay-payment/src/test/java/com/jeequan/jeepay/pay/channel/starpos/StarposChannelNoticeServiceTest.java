package com.jeequan.jeepay.pay.channel.starpos;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.starpos.StarposNormalMchParams;
import com.jeequan.jeepay.pay.channel.IChannelNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Cipher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StarposChannelNoticeServiceTest {

    private static final KeyPair KEY_PAIR = generateKeyPair();
    private static final String PUBLIC_KEY = Base64.getEncoder()
            .encodeToString(KEY_PAIR.getPublic().getEncoded());

    @Test
    void shouldVerifyDynamicFieldsAndReturnSuccessResponse() {
        JSONObject params = signedNotice(
                "CHANNEL-1001", "PAY-1001", "T-1001", "1", "12345"
        );
        params.put("NEW_PROTOCOL_FIELD", "signed-too");
        params.put("sign", signWithPrivateKey(unsignedFields(params)));

        PayOrder payOrder = payOrder("PAY-1001", 12345L, PayOrder.STATE_ING);
        ChannelRetMsg result = service(params, payOrder)
                .doNotice(null, params, payOrder, context(), noticeType());

        assertEquals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS,
                result.getChannelState());
        assertEquals("CHANNEL-1001", result.getChannelOrderId());
        assertEquals(
                "{\"rspCod\":\"\",\"rspMsg\":\"success\"}",
                result.getResponseEntity().getBody()
        );
        assertEquals(
                "application/json; charset=UTF-8",
                result.getResponseEntity().getHeaders().getFirst("Content-Type")
        );

        JSONObject origin = JSONObject.parseObject(result.getChannelOriginResponse());
        assertEquals("T-1001",
                origin.getJSONObject("starposExtension").getString("tOrderNo"));
        assertEquals("PAY-1001",
                origin.getJSONObject("starposExtension").getString("threeOrderNo"));
    }

    @Test
    void shouldResolvePayOrderIdFromThreeOrderNo() {
        JSONObject params = signedNotice(
                "CHANNEL-1002", "THREE-1002", "T-1002", "1", "100"
        );
        PayOrder resolved = payOrder("PAY-RESOLVED-1002", 100L, PayOrder.STATE_ING);

        PayOrderService payOrderService = mock(PayOrderService.class);
        when(payOrderService.getById("THREE-1002")).thenReturn(resolved);

        TestableNoticeService service = service(params, resolved);
        service.setPayOrderService(payOrderService);

        MutablePair<String, Object> parsed = service.parseParams(
                null, null, noticeType()
        );

        assertEquals("PAY-RESOLVED-1002", parsed.getLeft());
        assertEquals(params, parsed.getRight());
        verify(payOrderService).getById("THREE-1002");
    }

    @Test
    void shouldRejectAmountMismatch() {
        JSONObject params = signedNotice(
                "CHANNEL-1003", "PAY-1003", "T-1003", "1", "999"
        );
        PayOrder payOrder = payOrder("PAY-1003", 1000L, PayOrder.STATE_ING);

        assertThrows(ResponseException.class, () ->
                service(params, payOrder)
                        .doNotice(null, params, payOrder, context(), noticeType())
        );
    }

    @Test
    void shouldKeepSuccessResponseForDuplicateNotification() {
        JSONObject params = signedNotice(
                "CHANNEL-1004", "PAY-1004", "T-1004", "1", "100"
        );
        PayOrder payOrder = payOrder("PAY-1004", 100L, PayOrder.STATE_SUCCESS);

        ChannelRetMsg result = service(params, payOrder)
                .doNotice(null, params, payOrder, context(), noticeType());

        assertEquals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS,
                result.getChannelState());
        assertEquals("CHANNEL-1004", result.getChannelOrderId());
        assertEquals(
                "{\"rspCod\":\"\",\"rspMsg\":\"success\"}",
                result.getResponseEntity().getBody()
        );
        assertEquals(PayOrder.STATE_SUCCESS, payOrder.getState());
    }

    @Test
    void shouldRejectInvalidSignature() {
        JSONObject params = signedNotice(
                "CHANNEL-1005", "PAY-1005", "T-1005", "1", "100"
        );
        params.put("NEW_PROTOCOL_FIELD", "tampered");

        PayOrder payOrder = payOrder("PAY-1005", 100L, PayOrder.STATE_ING);

        assertThrows(ResponseException.class, () ->
                service(params, payOrder)
                        .doNotice(null, params, payOrder, context(), noticeType())
        );
    }

    private static TestableNoticeService service(
            JSONObject params,
            PayOrder payOrder
    ) {
        TestableNoticeService service = new TestableNoticeService(params);
        service.setPayOrderService(mock(PayOrderService.class));
        return service;
    }

    private static JSONObject signedNotice(
            String orderNo,
            String threeOrderNo,
            String tOrderNo,
            String orderStatus,
            String txamt
    ) {
        JSONObject params = new JSONObject(new LinkedHashMap<>());
        params.put("ORDER_NO", orderNo);
        params.put("THREE_ORDER_NO", threeOrderNo);
        params.put("T_ORDER_NO", tOrderNo);
        params.put("ORDER_STATUS", orderStatus);
        params.put("TXAMT", txamt);
        params.put("NEW_PROTOCOL_FIELD", "initial");
        params.put("sign", signWithPrivateKey(params));
        return params;
    }

    private static String signWithPrivateKey(Map<String, Object> fields) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, KEY_PAIR.getPrivate());
            byte[] digest = StarposSigner.sha256Hex(
                    StarposSigner.canonicalJson(fields)
            ).getBytes(StandardCharsets.UTF_8);
            return Base64.getEncoder().encodeToString(cipher.doFinal(digest));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, Object> unsignedFields(JSONObject params) {
        Map<String, Object> fields = new LinkedHashMap<>(params);
        fields.remove("sign");
        return fields;
    }

    private static MchAppConfigContext context() {
        StarposNormalMchParams params = new StarposNormalMchParams();
        params.setEnvironment("test");
        params.setAgetId("AGENT-1001");
        params.setCustId("CUST-1001");
        params.setPublicKey(PUBLIC_KEY);
        params.setVersion("1.0.0");

        MchAppConfigContext context = new MchAppConfigContext();
        context.getNormalMchParamsMap().put(CS.IF_CODE.STARPOS, params);
        return context;
    }

    private static PayOrder payOrder(String payOrderId, long amount, byte state) {
        return new PayOrder()
                .setPayOrderId(payOrderId)
                .setAmount(amount)
                .setState(state)
                .setIfCode(CS.IF_CODE.STARPOS);
    }

    private static IChannelNoticeService.NoticeTypeEnum noticeType() {
        return IChannelNoticeService.NoticeTypeEnum.DO_NOTIFY;
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class TestableNoticeService
            extends StarposChannelNoticeService {

        private final JSONObject params;

        private TestableNoticeService(JSONObject params) {
            this.params = params;
        }

        @Override
        protected JSONObject getReqParamJSON() {
            return params;
        }

        private void setPayOrderService(PayOrderService payOrderService) {
            super.setPayOrderServiceForTest(payOrderService);
        }
    }
}
