package com.jeequan.jeepay.pay.compat.epay;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class EpayCompatCallbackControllerTest {

    @Test
    void notifyEndpointReturnsProtocolSuccessBody() {
        EpayCompatNotifyService notifyService = mock(EpayCompatNotifyService.class);
        when(notifyService.handleCallback(Map.of("trade_status", "TRADE_SUCCESS")))
                .thenReturn("success");
        EpayCompatController controller = controller(notifyService);

        ResponseEntity<String> response = controller.notify(Map.of("trade_status", "TRADE_SUCCESS"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("success");
    }

    @Test
    void returnEndpointReturnsProtocolFailureBody() {
        EpayCompatNotifyService notifyService = mock(EpayCompatNotifyService.class);
        when(notifyService.handleReturn(Map.of("trade_status", "TRADE_CLOSED")))
                .thenReturn("fail");
        EpayCompatController controller = controller(notifyService);

        ResponseEntity<String> response = controller.returnCallback(Map.of("trade_status", "TRADE_CLOSED"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("fail");
        verify(notifyService).handleReturn(Map.of("trade_status", "TRADE_CLOSED"));
    }

    private static EpayCompatController controller(EpayCompatNotifyService notifyService) {
        return new EpayCompatController(
                new EpayCompatProperties(),
                mock(EpayCredentialResolver.class),
                new EpayRequestNormalizer(null, new EpayCompatProperties()),
                mock(EpayCompatOrderService.class),
                notifyService);
    }
}
