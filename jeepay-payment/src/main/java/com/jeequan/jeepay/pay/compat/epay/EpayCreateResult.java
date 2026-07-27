package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.core.entity.PayOrder;

public record EpayCreateResult(
        boolean success,
        String message,
        String tradeNo,
        String payInfo,
        String payType,
        PayOrder payOrder) {
}
