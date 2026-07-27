package com.jeequan.jeepay.pay.channel.starpos;

import com.jeequan.jeepay.core.model.params.starpos.StarposNormalMchParams;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Map;

/**
 * 星驿付固定环境配置。
 */
public final class StarposConfig {

    private static final Map<String, String> BASE_URLS = Map.of(
            "test", "https://xyf-server-test.postar.cn",
            "uat", "https://xyzscxm.postar.cn",
            "prod", "https://yyfsvxm.postar.cn"
    );

    private StarposConfig() {
    }

    /**
     * 校验商户参数并返回对应环境的服务地址。
     */
    public static String resolveBaseUrl(StarposNormalMchParams params) {
        if (params == null) {
            throw new IllegalArgumentException("星驿付商户参数不能为空");
        }

        requireNotBlank(params.getEnvironment(), "environment");
        requireNotBlank(params.getAgetId(), "agetId");
        requireNotBlank(params.getCustId(), "custId");
        requireNotBlank(params.getPublicKey(), "publicKey");
        requireNotBlank(params.getVersion(), "version");

        String baseUrl = BASE_URLS.get(params.getEnvironment());
        if (baseUrl == null) {
            throw new IllegalArgumentException("不支持的星驿付环境: " + params.getEnvironment());
        }
        return baseUrl;
    }

    /**
     * 校验收银台地址是否满足任一已知星驿付环境的地址约束。
     */
    public static boolean isKnownCashierUrl(String cashierUrl) {
        if (StringUtils.isBlank(cashierUrl)) {
            return false;
        }
        try {
            URI cashierUri = URI.create(cashierUrl);
            return "https".equalsIgnoreCase(cashierUri.getScheme())
                    && cashierUri.getHost() != null
                    && cashierUri.getUserInfo() == null
                    && cashierUri.getPort() == -1
                    && cashierUri.getQuery() == null
                    && cashierUri.getFragment() == null
                    && BASE_URLS.values().stream()
                    .map(URI::create)
                    .anyMatch(baseUri -> baseUri.getHost().equalsIgnoreCase(cashierUri.getHost()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void requireNotBlank(String value, String field) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(field + " 不能为空");
        }
    }
}
