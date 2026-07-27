package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class EpayCompatNotifyService {

    private final PayOrderService payOrderService;
    private final EpayCompatProperties properties;
    private final EpayCredentialResolver credentialResolver;

    public EpayCompatNotifyService(PayOrderService payOrderService,
                                   EpayCompatProperties properties,
                                   EpayCredentialResolver credentialResolver) {
        this.payOrderService = payOrderService;
        this.properties = properties;
        this.credentialResolver = credentialResolver;
    }

    public String createNotifyUrl(PayOrder payOrder) {
        return createCallbackUrl(payOrder, true);
    }

    public String createReturnUrl(PayOrder payOrder) {
        return createCallbackUrl(payOrder, false);
    }

    public String handleCallback(Map<String, String> fields) {
        try {
            if (fields == null) {
                return "fail";
            }
            String pid = required(fields, "pid");
            String outTradeNo = required(fields, "out_trade_no");
            EpayProtocolVersion version = protocolVersion(fields.get("sign_type"));
            String appId = properties.resolveAppId(pid);
            EpayCredential credential = credentialResolver.resolve(pid, appId, version);
            if (!verify(fields, credential, version)) {
                return "fail";
            }

            PayOrder payOrder = payOrderService.queryMchOrder(pid, null, outTradeNo);
            if (payOrder == null) {
                return "fail";
            }
            Optional<EpayMetadataValue> metadata = EpayCompatMetadata.decode(payOrder.getChannelExtra());
            if (metadata.isEmpty() || !matchesIdentity(fields, metadata.get(), payOrder)) {
                return "fail";
            }
            if (!matchesTradeNo(fields.get("trade_no"), metadata.get(), payOrder)
                    || !matchesMoney(fields.get("money"), payOrder)
                    || !safeEquals(fields.get("type"), metadata.get().type())) {
                return "fail";
            }

            String status = fields.get("trade_status");
            if ("TRADE_SUCCESS".equalsIgnoreCase(status)) {
                return confirmSuccess(payOrder, fields);
            }
            if ("TRADE_CLOSED".equalsIgnoreCase(status)) {
                return confirmClosed(payOrder, fields);
            }
            return "success";
        } catch (RuntimeException e) {
            log.warn("EPAY兼容回调处理失败", e);
            return "fail";
        }
    }

    private String createCallbackUrl(PayOrder payOrder, boolean notify) {
        if (payOrder == null) {
            return "";
        }
        Optional<EpayMetadataValue> metadata = EpayCompatMetadata.decode(payOrder.getChannelExtra());
        if (metadata.isEmpty() || !isTerminal(payOrder.getState())) {
            return "";
        }

        EpayMetadataValue value = metadata.get();
        EpayProtocolVersion version = protocolVersion(value.version());
        EpayCredential credential;
        try {
            String appId = value.appId();
            credential = credentialResolver.resolve(value.pid(), appId, version);
            if (version == EpayProtocolVersion.V2
                    && !hasText(credential == null ? null : credential.platformPrivateKey())) {
                log.warn("EPAY V2通知未生成：平台私钥配置缺失，pid={}, appId={}", value.pid(), appId);
                return "";
            }
        } catch (RuntimeException e) {
            log.warn("EPAY兼容通知凭据不可用，pid={}, appId={}", value.pid(), value.appId(), e);
            return "";
        }

        Map<String, Object> fields = callbackFields(value, payOrder);
        if (version == EpayProtocolVersion.V1) {
            fields.put("sign_type", "MD5");
            fields.put("sign", EpaySigner.signMd5(fields, credential.merchantKey()));
        } else {
            fields.put("sign_type", "RSA");
            fields.put("sign", EpaySigner.signRsa(fields, credential.platformPrivateKey()));
        }
        String callbackUrl = notify ? value.notifyUrl() : value.returnUrl();
        return StringKit.appendUrlQuery(callbackUrl, fields);
    }

    private String confirmSuccess(PayOrder payOrder, Map<String, String> fields) {
        if (payOrder.getState() == PayOrder.STATE_SUCCESS) {
            return "success";
        }
        if (payOrder.getState() != PayOrder.STATE_ING) {
            return "fail";
        }
        return payOrderService.updateIng2Success(
                payOrder.getPayOrderId(), fields.get("trade_no"), null) ? "success" : "fail";
    }

    private String confirmClosed(PayOrder payOrder, Map<String, String> fields) {
        if (payOrder.getState() == PayOrder.STATE_FAIL || payOrder.getState() == PayOrder.STATE_CLOSED) {
            return "success";
        }
        if (payOrder.getState() != PayOrder.STATE_ING) {
            return "fail";
        }
        return payOrderService.updateIng2Fail(
                payOrder.getPayOrderId(), fields.get("trade_no"), null,
                "TRADE_CLOSED", "EPAY交易关闭") ? "success" : "fail";
    }

    private static Map<String, Object> callbackFields(EpayMetadataValue metadata, PayOrder payOrder) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("pid", metadata.pid());
        fields.put("trade_no", tradeNo(metadata, payOrder));
        fields.put("out_trade_no", metadata.outTradeNo());
        fields.put("type", metadata.type());
        fields.put("name", metadata.name());
        fields.put("money", formatAmount(payOrder.getAmount()));
        fields.put("trade_status", payOrder.getState() == PayOrder.STATE_SUCCESS
                ? "TRADE_SUCCESS" : "TRADE_CLOSED");
        return fields;
    }

    private static boolean matchesIdentity(Map<String, String> fields,
                                           EpayMetadataValue metadata,
                                           PayOrder payOrder) {
        return safeEquals(fields.get("pid"), metadata.pid())
                && safeEquals(fields.get("out_trade_no"), metadata.outTradeNo())
                && safeEquals(payOrder.getMchNo(), metadata.pid())
                && safeEquals(payOrder.getMchOrderNo(), metadata.outTradeNo());
    }

    private static boolean matchesTradeNo(String actual, EpayMetadataValue metadata, PayOrder payOrder) {
        return hasText(actual) && safeEquals(actual, tradeNo(metadata, payOrder));
    }

    private static boolean matchesMoney(String actual, PayOrder payOrder) {
        if (!hasText(actual) || payOrder.getAmount() == null) {
            return false;
        }
        try {
            BigDecimal expected = BigDecimal.valueOf(payOrder.getAmount(), 2);
            BigDecimal received = new BigDecimal(actual).setScale(2, RoundingMode.UNNECESSARY);
            return expected.compareTo(received) == 0;
        } catch (ArithmeticException | NumberFormatException e) {
            return false;
        }
    }

    private static String tradeNo(EpayMetadataValue metadata, PayOrder payOrder) {
        if (hasText(metadata.tradeNo())) {
            return metadata.tradeNo();
        }
        if (hasText(payOrder.getChannelOrderNo())) {
            return payOrder.getChannelOrderNo();
        }
        return payOrder.getPayOrderId();
    }

    private static boolean isTerminal(Byte state) {
        return state != null && (state == PayOrder.STATE_SUCCESS
                || state == PayOrder.STATE_FAIL
                || state == PayOrder.STATE_CLOSED);
    }

    private static EpayProtocolVersion protocolVersion(String value) {
        if ("RSA".equalsIgnoreCase(value) || "v2".equalsIgnoreCase(value)) {
            return EpayProtocolVersion.V2;
        }
        return EpayProtocolVersion.V1;
    }

    private static boolean verify(Map<String, String> fields,
                                  EpayCredential credential,
                                  EpayProtocolVersion version) {
        return version == EpayProtocolVersion.V2
                ? EpaySigner.verifyRsa(fields, credential, fields.get("sign"))
                : EpaySigner.verifyMd5(fields, credential.merchantKey(), fields.get("sign"));
    }

    private static String required(Map<String, String> fields, String key) {
        String value = fields.get(key);
        if (!hasText(value)) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value.trim();
    }

    private static String formatAmount(Long amount) {
        if (amount == null) {
            return "";
        }
        return BigDecimal.valueOf(amount, 2).setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    private static boolean safeEquals(String left, String right) {
        return left != null && left.equals(right);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
