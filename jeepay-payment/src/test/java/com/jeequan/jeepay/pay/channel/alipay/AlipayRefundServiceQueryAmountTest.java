package com.jeequan.jeepay.pay.channel.alipay;

import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.pay.model.AlipayClientWrapper;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 主动退款查询的离线回归；SDK 包装器被模拟，不发送支付请求。 */
class AlipayRefundServiceQueryAmountTest {
    private final Locale originalLocale = Locale.getDefault();
    private AlipayRefundService service;
    private AlipayClientWrapper client;
    private MchAppConfigContext context;

    @BeforeEach
    void setUp() {
        service = new AlipayRefundService();
        client = mock(AlipayClientWrapper.class);
        context = mock(MchAppConfigContext.class);
        ConfigContextQueryService configs = mock(ConfigContextQueryService.class);
        when(configs.getAlipayClientWrapper(context)).thenReturn(client);
        ReflectionTestUtils.setField(service, "configContextQueryService", configs);
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    @Test
    void confirmsMatchingAmountUnderCommaDecimalLocale() throws Exception {
        Locale.setDefault(Locale.GERMANY);
        assertEquals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS, query("123.45", "10000", 12345L).getChannelState());
        ArgumentCaptor<AlipayTradeFastpayRefundQueryRequest> request =
                ArgumentCaptor.forClass(AlipayTradeFastpayRefundQueryRequest.class);
        verify(client).execute(request.capture());
        AlipayTradeFastpayRefundQueryModel model = (AlipayTradeFastpayRefundQueryModel) request.getValue().getBizModel();
        assertEquals("test-refund-149", model.getOutRequestNo());
        assertEquals("test-pay-149", model.getOutTradeNo());
        assertEquals("test-channel-149", model.getTradeNo());
    }

    @Test
    void keepsMismatchedAmountWaiting() throws Exception {
        assertEquals(ChannelRetMsg.ChannelState.WAITING, query("123.44", "10000", 12345L).getChannelState());
    }

    @Test
    void keepsMissingAmountWaitingEvenWhenChannelReportsSuccess() throws Exception {
        assertEquals(ChannelRetMsg.ChannelState.WAITING, query(null, "10000", 12345L).getChannelState());
    }

    @Test
    void doesNotConfirmMatchingAmountOnChannelFailure() throws Exception {
        assertEquals(ChannelRetMsg.ChannelState.WAITING, query("123.45", "40004", 12345L).getChannelState());
    }

    @Test
    void documentsUnresolvedLargeAmountPrecisionLimit() throws Exception {
        // 保守补丁只修复 Locale。旧 double 换算仍多出 1 分，不能声称精度问题已解决。
        assertEquals(ChannelRetMsg.ChannelState.WAITING,
                query("90071992547409.93", "10000", 9007199254740993L).getChannelState());
    }

    @Test
    void preservesExistingRoundingDuringQuery() throws Exception {
        assertEquals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS,
                query("1.125", "10000", 112L).getChannelState());
        assertEquals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS,
                query("1.115", "10000", 111L).getChannelState());
        assertEquals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS,
                query("2.675", "10000", 267L).getChannelState());
    }

    private ChannelRetMsg query(String amount, String code, long expectedAmount) throws Exception {
        AlipayTradeFastpayRefundQueryResponse response = new AlipayTradeFastpayRefundQueryResponse();
        response.setCode(code);
        // SDK 的 isSuccess() 依赖 subCode；失败响应同时带业务错误码。
        if (!"10000".equals(code)) {
            response.setSubCode("ACQ.TEST_FAILURE");
        }
        response.setRefundAmount(amount);
        response.setBody("synthetic-query-response");
        when(client.execute(any(AlipayTradeFastpayRefundQueryRequest.class))).thenReturn(response);
        RefundOrder order = new RefundOrder();
        order.setRefundOrderId("test-refund-149");
        order.setPayOrderId("test-pay-149");
        order.setChannelPayOrderNo("test-channel-149");
        order.setRefundAmount(expectedAmount);
        ChannelRetMsg result = service.query(order, context);
        assertEquals("synthetic-query-response", result.getChannelAttach());
        return result;
    }
}
