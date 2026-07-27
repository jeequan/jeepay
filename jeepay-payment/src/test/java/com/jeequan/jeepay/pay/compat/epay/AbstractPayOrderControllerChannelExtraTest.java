package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.beans.RequestKitBean;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.ctrl.payorder.AbstractPayOrderController;
import com.jeequan.jeepay.pay.ctrl.payorder.UnifiedOrderController;
import com.jeequan.jeepay.pay.rqrs.AbstractRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractPayOrderControllerChannelExtraTest {

    @Test
    void copiesRequestChannelExtraIntoInitialPayOrder() throws Exception {
        UnifiedOrderRQ request = new UnifiedOrderRQ();
        request.setChannelExtra(EpayCompatMetadata.encode(new EpayMetadataValue(
                "v1", "PID-001", null, "alipay", "ORDER-001",
                "https://merchant.example/notify", "https://merchant.example/return",
                "subject", "12.34", "203.0.113.10", null, null)));

        PayOrder payOrder = generate(request);

        assertThat(payOrder.getChannelExtra()).isEqualTo(request.getChannelExtra());
    }

    @Test
    void keepsNativeOrderChannelExtraEmptyWhenRequestHasNativeChannelExtra() throws Exception {
        UnifiedOrderRQ request = new UnifiedOrderRQ();
        request.setChannelExtra("{\"payDataType\":\"payUrl\"}");

        PayOrder payOrder = generate(request);

        assertThat(payOrder.getChannelExtra()).isNull();
    }

    @Test
    void nativeUnifiedOrderControllerDoesNotPersistFullEpayMetadata() throws Exception {
        UnifiedOrderRQ request = new UnifiedOrderRQ();
        request.setChannelExtra(EpayCompatMetadata.encode(new EpayMetadataValue(
                "v1", "PID-001", null, "alipay", "ORDER-001",
                "https://merchant.example/notify", "https://merchant.example/return",
                "subject", "12.34", "203.0.113.10", null, null)));

        NativeEndpointController controller = new NativeEndpointController(request);

        ApiRes response = controller.unifiedOrder();

        assertThat(response.getData()).isNull();
        assertThat(controller.generatedPayOrder.getChannelExtra()).isNull();
    }

    private static PayOrder generate(UnifiedOrderRQ request) throws Exception {
        return generate(new EpayCompatController(
                new EpayCompatProperties(),
                mock(EpayCredentialResolver.class),
                new EpayRequestNormalizer(java.time.Clock.systemUTC(), new EpayCompatProperties()),
                new EpayCompatOrderService(mock(PayOrderService.class))), request);
    }

    private static PayOrder generate(AbstractPayOrderController controller, UnifiedOrderRQ request) throws Exception {
        request.setAmount(1234L);
        request.setClientIp("203.0.113.10");
        RequestKitBean requestKitBean = mock(RequestKitBean.class);
        when(requestKitBean.getClientIp()).thenReturn("203.0.113.10");
        Field requestKitBeanField = AbstractCtrl.class.getDeclaredField("requestKitBean");
        requestKitBeanField.setAccessible(true);
        requestKitBeanField.set(controller, requestKitBean);
        Method method = AbstractPayOrderController.class.getDeclaredMethod(
                "genPayOrder", UnifiedOrderRQ.class, MchInfo.class, MchApp.class, String.class, MchPayPassage.class);
        method.setAccessible(true);
        return (PayOrder) method.invoke(
                controller,
                request,
                new MchInfo().setMchNo("MCH-1001"),
                new MchApp().setAppId("APP-1001"),
                "STARPOS",
                null);
    }

    private static final class NativeEndpointController extends UnifiedOrderController {
        private final UnifiedOrderRQ request;
        private PayOrder generatedPayOrder;

        private NativeEndpointController(UnifiedOrderRQ request) {
            this.request = request;
            request.setMchNo("MCH-1001");
            request.setAppId("APP-1001");
            request.setMchOrderNo("ORDER-001");
            request.setWayCode(com.jeequan.jeepay.core.constants.CS.PAY_WAY_CODE.QR_CASHIER);
            request.setAmount(1234L);
            request.setCurrency("CNY");
            request.setClientIp("203.0.113.10");
            request.setSubject("subject");
            request.setBody("subject");
        }

        @Override
        protected <T extends AbstractRQ> T getRQByWithMchSign(Class<T> cls) {
            return cls.cast(request);
        }

        @Override
        protected ApiRes unifiedOrder(String wayCode, UnifiedOrderRQ request) {
            try {
                generatedPayOrder = generate(this, request);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
            return ApiRes.customFail("test stop");
        }
    }
}
