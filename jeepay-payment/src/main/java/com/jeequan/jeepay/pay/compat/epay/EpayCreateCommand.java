package com.jeequan.jeepay.pay.compat.epay;

public record EpayCreateCommand(
        String merchantId,
        String appId,
        String orderNo,
        String paymentType,
        String subject,
        long amountFen,
        String notifyUrl,
        String returnUrl,
        String clientIp,
        String device,
        String version,
        String method,
        EpayCredential credential) {
}
