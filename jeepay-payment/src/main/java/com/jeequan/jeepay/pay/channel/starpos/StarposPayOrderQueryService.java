package com.jeequan.jeepay.pay.channel.starpos;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.starpos.StarposNormalMchParams;
import com.jeequan.jeepay.pay.channel.IPayOrderQueryService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * 星驿付支付订单查询服务。
 *
 * <p>查询结果必须先通过星驿付公钥验签，再参与本地订单状态推进。对无法确认
 * 的结果统一保留本地进行中状态，交由后续查单继续收敛。</p>
 */
@Slf4j
@Service
public class StarposPayOrderQueryService implements IPayOrderQueryService {

    static final String QUERY_ORDER_PATH = "/yyfsevr/order/orderQuery";
    static final String SUCCESS_CODE = "000000";
    static final String WAITING_CODE = "222222";
    static final String NOT_FOUND_CODE = "000002";
    static final String UNKNOWN_CODE = "-80000";
    static final String QUERY_ERROR_CODE = "STARPOS_QUERY_ERROR";
    private static final String ORDER_DATE_FORMAT = "yyyyMMdd";
    private static final String ORDER_TIME_ZONE = "Asia/Shanghai";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.STARPOS;
    }

    @Override
    public ChannelRetMsg query(
            PayOrder payOrder,
            MchAppConfigContext mchAppConfigContext
    ) {
        if (payOrder == null) {
            return waitingResult(QUERY_ERROR_CODE, "星驿付查单订单不能为空", null);
        }

        StarposNormalMchParams params;
        String baseUrl;
        JSONObject request;
        try {
            params = getParams(mchAppConfigContext);
            baseUrl = StarposConfig.resolveBaseUrl(params);
            request = buildQueryRequest(params, payOrder);
        } catch (RuntimeException e) {
            log.warn("星驿付查单配置或签名构造失败，orderNo={}",
                    maskOrderNo(payOrder.getPayOrderId()), e);
            return confirmedFailure(QUERY_ERROR_CODE, "星驿付查单配置无效", null);
        }

        try {
            JSONObject response = createHttpClient(baseUrl)
                    .post(QUERY_ORDER_PATH, request, payOrder.getPayOrderId());
            return mapResponse(response, params, payOrder);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return unknownResult(QUERY_ERROR_CODE, "星驿付查单请求被中断", null);
        } catch (IOException | RuntimeException e) {
            log.warn("星驿付查单请求异常，订单进入可查询状态，orderNo={}",
                    maskOrderNo(payOrder.getPayOrderId()), e);
            return unknownResult(QUERY_ERROR_CODE, "星驿付查单请求异常", null);
        }
    }

    /**
     * 测试使用本地转发客户端覆盖；生产使用固定 HTTPS 环境客户端。
     */
    protected StarposHttpClient createHttpClient(String baseUrl) {
        return new StarposHttpClient(baseUrl);
    }

    private StarposNormalMchParams getParams(MchAppConfigContext context) {
        if (context == null) {
            throw new IllegalArgumentException("星驿付商户配置上下文不能为空");
        }
        StarposNormalMchParams params = context.getNormalMchParamsByIfCode(
                getIfCode(), StarposNormalMchParams.class
        );
        if (params == null) {
            throw new IllegalArgumentException("星驿付商户参数不能为空");
        }
        return params;
    }

    private JSONObject buildQueryRequest(
            StarposNormalMchParams params,
            PayOrder payOrder
    ) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("agetId", params.getAgetId());
        fields.put("custId", params.getCustId());
        fields.put("version", params.getVersion());
        fields.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        fields.put("orderNo", payOrder.getPayOrderId());
        fields.put("orderTime", orderDate(payOrder.getCreatedAt()));
        fields.put("sign", StarposSigner.signRequest(fields, params.getPublicKey()));
        return new JSONObject(fields);
    }

    private String orderDate(Date createdAt) {
        Date value = createdAt == null ? new Date() : createdAt;
        SimpleDateFormat formatter = new SimpleDateFormat(ORDER_DATE_FORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone(ORDER_TIME_ZONE));
        return formatter.format(value);
    }

    private ChannelRetMsg mapResponse(
            JSONObject response,
            StarposNormalMchParams params,
            PayOrder payOrder
    ) {
        if (response == null) {
            return waitingResult(QUERY_ERROR_CODE, "星驿付查单返回为空", null);
        }

        String envelopeCode = firstNonBlank(response, "code", "retCode");
        String message = message(response);
        JSONObject data = response.getJSONObject("data");

        boolean topLevelVerified = verify(response, params);
        boolean nestedVerified = data != null && verify(data, params);
        if (!topLevelVerified && !nestedVerified) {
            return unknownResult(envelopeCode, "星驿付查单响应验签失败",
                    response.toJSONString());
        }

        JSONObject business = data == null ? response : data;
        String signedCode = firstNonBlank(business, "code", "retCode");
        if (data != null && nestedVerified && !topLevelVerified) {
            if (StringUtils.isBlank(signedCode)
                    || !StringUtils.equals(envelopeCode, signedCode)) {
                return unknownResult(envelopeCode,
                        "星驿付查单响应状态码未被签名覆盖", response.toJSONString());
            }
        }
        String code = data != null && nestedVerified && !topLevelVerified
                ? signedCode
                : envelopeCode;
        String status = firstNonBlank(business, "orderStatus", "status");
        String businessMessage = StringUtils.defaultIfBlank(
                firstNonBlank(business, "message", "msg"),
                message
        );
        String origin = originWithExtension(response, business);

        if (NOT_FOUND_CODE.equals(code)) {
            return waitingResult(code, businessMessage, origin);
        }
        if (UNKNOWN_CODE.equals(code)) {
            return unknownResult(code, businessMessage, origin);
        }
        if (WAITING_CODE.equals(code) || "2".equals(status)) {
            return waitingResult(code, businessMessage, origin);
        }
        if ("0".equals(status) || "99".equals(status)) {
            return confirmedFailure(code, businessMessage, origin);
        }
        if (SUCCESS_CODE.equals(code) && "1".equals(status)) {
            String channelOrderId = firstNonBlank(business, "orderNo");
            if (StringUtils.isBlank(channelOrderId)
                    || !amountMatches(business, payOrder.getAmount())) {
                return unknownResult(code, "星驿付查单成功结果校验失败", origin);
            }
            ChannelRetMsg result = ChannelRetMsg.confirmSuccess(channelOrderId);
            result.setChannelOriginResponse(origin);
            return result;
        }

        return unknownResult(code, "星驿付查单返回了未识别状态", origin);
    }

    private boolean verify(JSONObject body, StarposNormalMchParams params) {
        return StarposSigner.verifyNotification(
                new LinkedHashMap<>(body),
                params.getPublicKey()
        );
    }

    private boolean amountMatches(JSONObject business, Long localAmount) {
        String upstreamAmount = firstNonBlank(business, "txamt", "amount");
        if (StringUtils.isBlank(upstreamAmount) || localAmount == null) {
            return false;
        }
        try {
            long parsed = Long.parseLong(upstreamAmount);
            return parsed >= 0 && parsed == localAmount;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String originWithExtension(JSONObject response, JSONObject business) {
        JSONObject origin = JSONObject.parseObject(response.toJSONString());
        JSONObject extension = new JSONObject();
        putIfPresent(extension, "torderNo", firstNonBlank(business, "torderNo"));
        putIfPresent(extension, "threeOrderNo", firstNonBlank(business, "threeOrderNo"));
        if (!extension.isEmpty()) {
            origin.put("starposExtension", extension);
        }
        return origin.toJSONString();
    }

    private void putIfPresent(JSONObject target, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            target.put(key, value);
        }
    }

    private ChannelRetMsg waitingResult(
            String code,
            String message,
            String origin
    ) {
        ChannelRetMsg result = ChannelRetMsg.waiting();
        result.setChannelErrCode(code);
        result.setChannelErrMsg(message);
        if (origin != null) {
            result.setChannelOriginResponse(origin);
        }
        return result;
    }

    private ChannelRetMsg unknownResult(
            String code,
            String message,
            String origin
    ) {
        ChannelRetMsg result = ChannelRetMsg.unknown(message);
        result.setChannelErrCode(StringUtils.defaultIfBlank(code, UNKNOWN_CODE));
        result.setNeedQuery(true);
        if (origin != null) {
            result.setChannelOriginResponse(origin);
        }
        return result;
    }

    private ChannelRetMsg confirmedFailure(
            String code,
            String message,
            String origin
    ) {
        ChannelRetMsg result = ChannelRetMsg.confirmFail(code, message);
        if (origin != null) {
            result.setChannelOriginResponse(origin);
        }
        return result;
    }

    private String firstNonBlank(JSONObject object, String... keys) {
        if (object == null) {
            return null;
        }
        for (String key : keys) {
            String value = object.getString(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String message(JSONObject response) {
        return StringUtils.defaultIfBlank(
                firstNonBlank(response, "message", "msg"),
                "星驿付查单返回异常"
        );
    }

    private String maskOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return "-";
        }
        if (orderNo.length() <= 4) {
            return "*".repeat(orderNo.length());
        }
        return orderNo.substring(0, 2) + "***"
                + orderNo.substring(orderNo.length() - 2);
    }
}
