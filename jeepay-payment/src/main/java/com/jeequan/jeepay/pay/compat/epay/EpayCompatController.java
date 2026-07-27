package com.jeequan.jeepay.pay.compat.epay;

import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.ctrl.payorder.AbstractPayOrderController;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.util.Map;

@RestController
public class EpayCompatController extends AbstractPayOrderController {

    private final EpayCompatProperties properties;
    private final EpayCredentialResolver credentialResolver;
    private final EpayRequestNormalizer requestNormalizer;
    private final EpayCompatOrderService orderService;
    private EpayCompatNotifyService notifyService;
    private final EpayV1Adapter v1Adapter = new EpayV1Adapter();
    private final EpayV2Adapter v2Adapter = new EpayV2Adapter();

    public EpayCompatController(EpayCompatProperties properties,
                                EpayCredentialResolver credentialResolver,
                                EpayCompatOrderService orderService,
                                EpayCompatNotifyService notifyService) {
        this(properties, credentialResolver, new EpayRequestNormalizer(Clock.systemUTC(), properties),
                orderService, notifyService);
    }

    @Autowired
    public EpayCompatController(EpayCompatProperties properties,
                                EpayCredentialResolver credentialResolver,
                                EpayCompatOrderService orderService) {
        this(properties, credentialResolver, orderService, null);
    }

    public EpayCompatController(EpayCompatProperties properties,
                                EpayCredentialResolver credentialResolver,
                                EpayRequestNormalizer requestNormalizer,
                                EpayCompatOrderService orderService,
                                EpayCompatNotifyService notifyService) {
        this.properties = properties;
        this.credentialResolver = credentialResolver;
        this.requestNormalizer = requestNormalizer;
        this.orderService = orderService;
        this.notifyService = notifyService;
    }

    @Autowired
    private void setNotifyService(EpayCompatNotifyService notifyService) {
        this.notifyService = notifyService;
    }

    public EpayCompatController(EpayCompatProperties properties,
                                EpayCredentialResolver credentialResolver,
                                EpayRequestNormalizer requestNormalizer,
                                EpayCompatOrderService orderService) {
        this(properties, credentialResolver, requestNormalizer, orderService, null);
    }

    @Override
    protected boolean shouldPersistChannelExtra(UnifiedOrderRQ rq) {
        return EpayCompatMetadata.decode(rq.getChannelExtra()).isPresent();
    }

    @PostMapping("/compat/epay/mapi.php")
    public ResponseEntity<String> mapi(@RequestParam Map<String, String> fields) {
        return process(fields).asJson();
    }

    @RequestMapping(value = "/compat/epay/submit.php", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> submit(@RequestParam Map<String, String> fields) {
        return process(fields).asSubmitResponse();
    }

    @RequestMapping(value = "/compat/epay/notify.php", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> notify(@RequestParam Map<String, String> fields) {
        return ResponseEntity.ok(notifyService == null ? "fail" : notifyService.handleCallback(fields));
    }

    @RequestMapping(value = "/compat/epay/return.php", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> returnCallback(@RequestParam Map<String, String> fields) {
        return ResponseEntity.ok(notifyService == null ? "fail" : notifyService.handleReturn(fields));
    }

    private EpayCompatResponse process(Map<String, String> fields) {
        EpayProtocolVersion version = protocolVersion(fields);
        EpayCredential credential = null;
        try {
            String merchantId = required(fields, "pid");
            String appId = properties.resolveAppId(merchantId);
            credential = credentialResolver.resolve(merchantId, appId, version);
            validateResponseCredential(version, credential);
            if (!verify(fields, credential, version)) {
                return failure(version, "验签失败", credential);
            }

            EpayCreateCommand command = requestNormalizer.normalize(fields, appId, credential, version);
            var existing = orderService.findExisting(command);
            EpayCreateResult result;
            if (existing != null) {
                result = orderService.fromExisting(existing, command);
            } else {
                UnifiedOrderRQ request = orderService.toUnifiedOrderRequest(command);
                ApiRes apiRes = unifiedOrder(request.getWayCode(), request);
                result = orderService.fromApiResult(apiRes, command);
                if (!result.success() && isDuplicateOrderFailure(apiRes, command)) {
                    PayOrder raced = orderService.findExisting(command);
                    if (raced != null) {
                        result = orderService.fromExisting(raced, command);
                    }
                }
            }
            return result.success()
                    ? success(version, result, credential)
                    : failure(version, result.message(), credential);
        } catch (RuntimeException e) {
            return failure(version, e.getMessage(), credential);
        }
    }

    private static boolean isDuplicateOrderFailure(ApiRes apiRes, EpayCreateCommand command) {
        if (apiRes == null || apiRes.getCode() == null
                || apiRes.getCode() == ApiCodeEnum.SUCCESS.getCode()
                || apiRes.getMsg() == null) {
            return false;
        }
        return apiRes.getMsg().contains("商户订单[" + command.orderNo() + "]已存在");
    }

    private EpayProtocolVersion protocolVersion(Map<String, String> fields) {
        return "RSA".equalsIgnoreCase(fields == null ? null : fields.get("sign_type"))
                ? EpayProtocolVersion.V2 : EpayProtocolVersion.V1;
    }

    private boolean verify(Map<String, String> fields, EpayCredential credential, EpayProtocolVersion version) {
        return version == EpayProtocolVersion.V2
                ? v2Adapter.verify(fields, credential)
                : v1Adapter.verify(fields, credential);
    }

    private static void validateResponseCredential(EpayProtocolVersion version, EpayCredential credential) {
        if (version == EpayProtocolVersion.V2) {
            if (credential == null || credential.platformPrivateKey() == null
                    || credential.platformPrivateKey().trim().isEmpty()) {
                throw new IllegalStateException("商户应用 V2 RSA 凭据未配置");
            }
            EpaySigner.validateRsaPrivateKey(credential.platformPrivateKey());
        }
    }

    private EpayCompatResponse success(EpayProtocolVersion version, EpayCreateResult result,
                                       EpayCredential credential) {
        return version == EpayProtocolVersion.V2
                ? v2Adapter.success(result, credential)
                : v1Adapter.success(result, credential);
    }

    private EpayCompatResponse failure(EpayProtocolVersion version, String message,
                                       EpayCredential credential) {
        String safeMessage = message == null || message.trim().isEmpty() ? "请求处理失败" : message;
        return version == EpayProtocolVersion.V2
                ? v2Adapter.failure(safeMessage, credential)
                : v1Adapter.failure(safeMessage, credential);
    }

    private static String required(Map<String, String> fields, String key) {
        String value = fields == null ? null : fields.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value.trim();
    }
}
