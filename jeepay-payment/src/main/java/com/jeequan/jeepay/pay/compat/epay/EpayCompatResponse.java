package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.pay.channel.starpos.StarposConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class EpayCompatResponse {

    private final boolean success;
    private final String body;
    private final String cashierUrl;

    public EpayCompatResponse(boolean success, String body, String cashierUrl) {
        this.success = success;
        this.body = body;
        this.cashierUrl = cashierUrl;
    }

    public ResponseEntity<String> asJson() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    public ResponseEntity<?> asSubmitResponse() {
        if (success && isValidatedHttpsCashierUrl(cashierUrl)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, cashierUrl)
                    .build();
        }
        return asJson();
    }

    private static boolean isValidatedHttpsCashierUrl(String value) {
        return StarposConfig.isKnownCashierUrl(value);
    }
}
