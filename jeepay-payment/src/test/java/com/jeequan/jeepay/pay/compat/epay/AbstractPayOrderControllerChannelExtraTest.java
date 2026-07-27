package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.beans.RequestKitBean;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.pay.ctrl.payorder.AbstractPayOrderController;
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

    private static PayOrder generate(UnifiedOrderRQ request) throws Exception {
        request.setAmount(1234L);
        request.setClientIp("203.0.113.10");
        EpayCompatProperties properties = new EpayCompatProperties();
        EpayCompatController controller = new EpayCompatController(
                properties,
                mock(EpayCredentialResolver.class),
                new EpayRequestNormalizer(java.time.Clock.systemUTC(), properties),
                new EpayCompatOrderService(mock(PayOrderService.class)));
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
}
