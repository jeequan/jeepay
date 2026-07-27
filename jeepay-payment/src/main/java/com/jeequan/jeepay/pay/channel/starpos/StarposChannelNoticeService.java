package com.jeequan.jeepay.pay.channel.starpos;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.starpos.StarposNormalMchParams;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.channel.IChannelNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 星驿付支付异步通知服务。
 *
 * <p>通知字段不使用固定白名单参与验签，保证上游新增字段仍然进入
 * canonical JSON。订单关联使用 {@code THREE_ORDER_NO} 查找 JeePay
 * {@code payOrderId}，再校验商户订单、金额和支付状态。</p>
 */
@Slf4j
@Service
public class StarposChannelNoticeService extends AbstractChannelNoticeService {

    private static final String SUCCESS_STATUS = "1";
    private static final String FAILED_STATUS = "0";
    private static final String CLOSED_STATUS = "99";

    @Autowired
    private PayOrderService payOrderService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.STARPOS;
    }

    @Override
    public MutablePair<String, Object> parseParams(
            HttpServletRequest request,
            String urlOrderId,
            NoticeTypeEnum noticeTypeEnum
    ) {
        try {
            JSONObject params = getReqParamJSON();
            String threeOrderNo = firstNonBlank(
                    params, "THREE_ORDER_NO", "threeOrderNo", "three_order_no"
            );
            String orderId = threeOrderNo;
            if (StringUtils.isBlank(orderId)) {
                orderId = firstNonBlank(
                        params, "ORDER_NO", "orderNo", "order_no"
                );
            }
            if (StringUtils.isBlank(orderId)) {
                throw ResponseException.buildText("ERROR");
            }

            if (StringUtils.isNotBlank(threeOrderNo)) {
                PayOrder payOrder = findPayOrderByThreeOrderNo(threeOrderNo);
                if (payOrder != null && StringUtils.isNotBlank(payOrder.getPayOrderId())) {
                    orderId = payOrder.getPayOrderId();
                }
            }
            return MutablePair.of(orderId, params);
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析星驿付支付通知失败", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(
            HttpServletRequest request,
            Object params,
            PayOrder payOrder,
            MchAppConfigContext mchAppConfigContext,
            NoticeTypeEnum noticeTypeEnum
    ) {
        try {
            JSONObject jsonParams = toJsonObject(params);
            StarposNormalMchParams starposParams = getParams(mchAppConfigContext);

            if (!verifyNotification(jsonParams, starposParams)) {
                log.warn("星驿付支付通知验签失败，payOrderId={}",
                        maskOrderNo(payOrder == null ? null : payOrder.getPayOrderId()));
                throw ResponseException.buildText("ERROR");
            }

            validateOrderAndAmount(jsonParams, payOrder);

            String orderNo = firstNonBlank(
                    jsonParams, "ORDER_NO", "orderNo", "order_no"
            );
            String orderStatus = firstNonBlank(
                    jsonParams, "ORDER_STATUS", "orderStatus", "status"
            );

            ChannelRetMsg result = new ChannelRetMsg();
            result.setChannelOrderId(orderNo);
            result.setChannelOriginResponse(originWithExtension(jsonParams).toJSONString());
            result.setResponseEntity(successResponse());

            if (SUCCESS_STATUS.equals(orderStatus)) {
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            } else if (FAILED_STATUS.equals(orderStatus)
                    || CLOSED_STATUS.equals(orderStatus)) {
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                result.setChannelErrCode(orderStatus);
                result.setChannelErrMsg("星驿付支付未成功");
            } else {
                result.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            }
            return result;
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            log.error("处理星驿付支付通知失败，payOrderId={}",
                    maskOrderNo(payOrder == null ? null : payOrder.getPayOrderId()), e);
            throw ResponseException.buildText("ERROR");
        }
    }

    protected PayOrder findPayOrderByThreeOrderNo(String threeOrderNo) {
        if (payOrderService == null) {
            throw new IllegalStateException("支付订单服务未初始化");
        }
        return payOrderService.getById(threeOrderNo);
    }

    /**
     * 仅供同包测试替换 Spring 注入依赖，不参与生产调用。
     */
    protected void setPayOrderServiceForTest(PayOrderService payOrderService) {
        this.payOrderService = payOrderService;
    }

    private StarposNormalMchParams getParams(MchAppConfigContext context) {
        if (context == null) {
            throw new IllegalArgumentException("星驿付商户配置上下文不能为空");
        }
        StarposNormalMchParams params = context.getNormalMchParamsByIfCode(
                getIfCode(), StarposNormalMchParams.class
        );
        if (params == null || StringUtils.isBlank(params.getPublicKey())) {
            throw new IllegalArgumentException("星驿付商户参数不能为空");
        }
        return params;
    }

    private boolean verifyNotification(
            JSONObject params,
            StarposNormalMchParams starposParams
    ) {
        Map<String, Object> fields = new LinkedHashMap<>(params);
        if (!fields.containsKey("sign") && fields.containsKey("SIGN")) {
            fields.put("sign", fields.remove("SIGN"));
        }
        return StarposSigner.verifyNotification(
                fields, starposParams.getPublicKey()
        );
    }

    private void validateOrderAndAmount(JSONObject params, PayOrder payOrder) {
        if (payOrder == null
                || !CS.IF_CODE.STARPOS.equals(payOrder.getIfCode())) {
            throw ResponseException.buildText("ERROR");
        }

        String threeOrderNo = firstNonBlank(
                params, "THREE_ORDER_NO", "threeOrderNo", "three_order_no"
        );
        String orderNo = firstNonBlank(
                params, "ORDER_NO", "orderNo", "order_no"
        );
        if (StringUtils.isBlank(threeOrderNo)
                || !StringUtils.equals(threeOrderNo, payOrder.getPayOrderId())
                || StringUtils.isBlank(orderNo)) {
            log.warn("星驿付支付通知订单关系不匹配，threeOrderNo={}, payOrderId={}",
                    threeOrderNo, maskOrderNo(payOrder.getPayOrderId()));
            throw ResponseException.buildText("ERROR");
        }

        String amount = firstNonBlank(
                params, "TXAMT", "txamt", "amount"
        );
        if (StringUtils.isBlank(amount) || payOrder.getAmount() == null) {
            throw ResponseException.buildText("ERROR");
        }
        try {
            if (Long.parseLong(amount) != payOrder.getAmount()) {
                log.warn("星驿付支付通知金额不匹配，payOrderId={}, upstream={}, local={}",
                        maskOrderNo(payOrder.getPayOrderId()),
                        amount, payOrder.getAmount());
                throw ResponseException.buildText("ERROR");
            }
        } catch (NumberFormatException e) {
            throw ResponseException.buildText("ERROR");
        }
    }

    private JSONObject toJsonObject(Object params) {
        if (params instanceof JSONObject) {
            return (JSONObject) params;
        }
        if (params instanceof Map) {
            return new JSONObject((Map<String, Object>) params);
        }
        throw ResponseException.buildText("ERROR");
    }

    private JSONObject originWithExtension(JSONObject params) {
        JSONObject origin = JSONObject.parseObject(params.toJSONString());
        JSONObject extension = new JSONObject();
        putIfPresent(extension, "tOrderNo",
                firstNonBlank(params, "T_ORDER_NO", "tOrderNo", "t_order_no"));
        putIfPresent(extension, "threeOrderNo",
                firstNonBlank(params, "THREE_ORDER_NO", "threeOrderNo", "three_order_no"));
        if (!extension.isEmpty()) {
            origin.put("starposExtension", extension);
        }
        return origin;
    }

    private void putIfPresent(JSONObject target, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            target.put(key, value);
        }
    }

    private String firstNonBlank(JSONObject params, String... keys) {
        for (String key : keys) {
            String value = params.getString(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private ResponseEntity<String> successResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        return new ResponseEntity<>(
                "{\"rspCod\":\"\",\"rspMsg\":\"success\"}",
                headers,
                HttpStatus.OK
        );
    }

    private String maskOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo) || orderNo.length() <= 4) {
            return orderNo;
        }
        return orderNo.substring(0, 2) + "***"
                + orderNo.substring(orderNo.length() - 2);
    }
}
