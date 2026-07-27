package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.PayMchNotifyService;
import com.jeequan.jeepay.service.impl.MchNotifyRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PayMchNotifyServiceTest {

    @Test
    void missingCompatNotifyUrlDoesNotCreateRecordOrSendMessage() {
        MchNotifyRecordService recordService = mock(MchNotifyRecordService.class);
        ConfigContextQueryService configService = mock(ConfigContextQueryService.class);
        IMQSender mqSender = mock(IMQSender.class);
        EpayCompatNotifyService compatNotifyService = mock(EpayCompatNotifyService.class);
        when(recordService.findByPayOrder("JPAY-1001")).thenReturn(null);
        when(configService.queryMchApp("MCH-1001", "APP-1001"))
                .thenReturn(new MchApp().setAppSecret("native-secret"));
        when(compatNotifyService.createNotifyUrl(org.mockito.ArgumentMatchers.any(PayOrder.class)))
                .thenReturn("");

        PayMchNotifyService service = new PayMchNotifyService();
        ReflectionTestUtils.setField(service, "mchNotifyRecordService", recordService);
        ReflectionTestUtils.setField(service, "configContextQueryService", configService);
        ReflectionTestUtils.setField(service, "mqSender", mqSender);
        ReflectionTestUtils.setField(service, "epayCompatNotifyService", compatNotifyService);

        PayOrder order = new PayOrder()
                .setPayOrderId("JPAY-1001")
                .setMchNo("MCH-1001")
                .setAppId("APP-1001")
                .setMchOrderNo("ORDER-1001")
                .setNotifyUrl("https://merchant.example/notify.php")
                .setChannelExtra(EpayCompatMetadata.encode(new EpayMetadataValue(
                        "v2", "MCH-1001", "APP-1001", "alipay", "ORDER-1001",
                        "https://merchant.example/notify.php", "https://merchant.example/return.php",
                        "Demo", "12.34", null, null, "JPAY-1001")));

        service.payOrderNotify(order);

        verify(recordService, never()).save(org.mockito.ArgumentMatchers.any());
        verify(mqSender, never()).send(org.mockito.ArgumentMatchers.any());
    }
}
