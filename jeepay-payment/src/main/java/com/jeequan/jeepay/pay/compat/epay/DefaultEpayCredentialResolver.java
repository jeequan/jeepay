package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultEpayCredentialResolver implements EpayCredentialResolver {

    private final ConfigContextQueryService configContextQueryService;
    private final EpayCompatProperties properties;

    public DefaultEpayCredentialResolver(ConfigContextQueryService configContextQueryService,
                                         EpayCompatProperties properties) {
        this.configContextQueryService = configContextQueryService;
        this.properties = properties;
    }

    @Override
    public EpayCredential resolve(String merchantId, String appId, EpayProtocolVersion version) {
        if (version == EpayProtocolVersion.V1) {
            MchApp mchApp = configContextQueryService.queryMchApp(merchantId, appId);
            if (mchApp == null || !hasText(mchApp.getAppSecret())) {
                throw new IllegalStateException("商户应用 V1 密钥未配置");
            }
            return new EpayCredential(mchApp.getAppSecret(), null, null);
        }

        EpayCompatProperties.CredentialProperties credential = credentialProperties(merchantId, appId);
        if (!hasText(credential.getMerchantPublicKey()) || !hasText(credential.getPlatformPrivateKey())) {
            throw new IllegalStateException("商户应用 V2 RSA 凭据未配置");
        }
        EpaySigner.validateRsaPrivateKey(credential.getPlatformPrivateKey());
        return new EpayCredential(
                null,
                credential.getMerchantPublicKey(),
                credential.getPlatformPrivateKey()
        );
    }

    private EpayCompatProperties.CredentialProperties credentialProperties(String merchantId, String appId) {
        Map<String, Map<String, EpayCompatProperties.CredentialProperties>> all = properties.getCredentials();
        Map<String, EpayCompatProperties.CredentialProperties> byApp = all.get(merchantId);
        if (byApp == null || byApp.get(appId) == null) {
            throw new IllegalStateException("商户应用 V2 RSA 凭据未配置");
        }
        return byApp.get(appId);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
