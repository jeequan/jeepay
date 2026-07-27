package com.jeequan.jeepay.pay.compat.epay;

public interface EpayCredentialResolver {
    EpayCredential resolve(String merchantId, String appId, EpayProtocolVersion version);
}
