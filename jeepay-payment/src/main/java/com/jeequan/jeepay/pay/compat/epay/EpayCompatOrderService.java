package com.jeequan.jeepay.pay.compat.epay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
public final class EpayCompatOrderService {

    private static final String PAY_DATA_TYPE = "payUrl";
    private static final String PAY_INFO_KEY = "pay_info";

    private final PayOrderService payOrderService;

    public EpayCompatOrderService(PayOrderService payOrderService) {
        this.payOrderService = payOrderService;
    }

    public PayOrder findExisting(EpayCreateCommand command) {
        PayOrder existing = payOrderService.queryMchOrder(
                command.merchantId(), null, command.orderNo());
        if (existing == null) {
            return null;
        }
        if (!isCompatible(existing, command)) {
            throw new IllegalStateException("重复订单参数冲突：商户、应用、订单号、金额、支付方式、回调地址或类型不一致");
        }
        return existing;
    }

    public UnifiedOrderRQ toUnifiedOrderRequest(EpayCreateCommand command) {
        UnifiedOrderRQ rq = new UnifiedOrderRQ();
        rq.setMchNo(command.merchantId());
        rq.setAppId(command.appId());
        rq.setMchOrderNo(command.orderNo());
        rq.setWayCode(CS.PAY_WAY_CODE.STARPOS_QR);
        rq.setAmount(command.amountFen());
        rq.setCurrency("CNY");
        rq.setClientIp(command.clientIp());
        rq.setSubject(command.subject());
        rq.setBody(command.subject());
        rq.setNotifyUrl(command.notifyUrl());
        rq.setReturnUrl(command.returnUrl());
        rq.setChannelExtra(channelExtra(command, null, null));
        rq.setExtParam(null);
        return rq;
    }

    public EpayCreateResult fromApiResult(ApiRes apiRes, EpayCreateCommand command) {
        if (apiRes == null || apiRes.getCode() == null || apiRes.getCode() != ApiCodeEnum.SUCCESS.getCode()
                || !(apiRes.getData() instanceof UnifiedOrderRS response)) {
            return new EpayCreateResult(false, messageOf(apiRes), "", "", "", null);
        }

        String tradeNo = response.getPayOrderId();
        PayOrder payOrder = payOrderService.queryMchOrder(command.merchantId(), tradeNo, null);
        String payInfo = response.buildPayData();
        if (payOrder != null) {
            persistMetadata(payOrder, command, tradeNo, payInfo);
        }
        if (!hasText(payInfo)) {
            String message = response.getOrderState() != null && response.getOrderState() == PayOrder.STATE_ING
                    ? "星驿付下单结果未知，可通过交易号查询"
                    : "未取得星驿付收银链接";
            return new EpayCreateResult(false, message, tradeNo, "", "", payOrder);
        }
        return new EpayCreateResult(true, "success", tradeNo, payInfo, "redirect", payOrder);
    }

    EpayCreateResult fromExisting(PayOrder existing, EpayCreateCommand command) {
        String payInfo = payInfo(existing.getChannelExtra());
        if (!hasText(payInfo)) {
            return new EpayCreateResult(
                    false,
                    "原订单没有可用的收银链接，可通过交易号查询",
                    existing.getPayOrderId(), "", "", existing
            );
        }
        return new EpayCreateResult(
                true, "success", existing.getPayOrderId(), payInfo, "redirect", existing
        );
    }

    private boolean isCompatible(PayOrder existing, EpayCreateCommand command) {
        Optional<EpayMetadataValue> metadata = EpayCompatMetadata.decode(existing.getChannelExtra());
        return existing.getAmount() != null
                && existing.getAmount() == command.amountFen()
                && CS.IF_CODE.STARPOS.equals(existing.getIfCode())
                && command.paymentType().equals(existing.getWayCode())
                && Objects.equals(command.merchantId(), existing.getMchNo())
                && Objects.equals(command.appId(), existing.getAppId())
                && Objects.equals(command.orderNo(), existing.getMchOrderNo())
                && Objects.equals(command.notifyUrl(), existing.getNotifyUrl())
                && Objects.equals(command.returnUrl(), existing.getReturnUrl())
                && metadata.map(value ->
                Objects.equals(command.merchantId(), value.pid())
                        && Objects.equals(command.appId(), value.appId())
                        && Objects.equals(command.orderNo(), value.outTradeNo())
                        && Objects.equals(amountYuan(command.amountFen()), value.money())
                        && Objects.equals(command.notifyUrl(), value.notifyUrl())
                        && Objects.equals(command.returnUrl(), value.returnUrl())
                        && Objects.equals(command.epayType(), value.type())
        ).orElse(false);
    }

    private void persistMetadata(PayOrder payOrder, EpayCreateCommand command, String tradeNo, String payInfo) {
        PayOrder patch = new PayOrder()
                .setPayOrderId(payOrder.getPayOrderId())
                .setChannelExtra(channelExtra(command, tradeNo, payInfo));
        payOrderService.updateById(patch);
    }

    private String channelExtra(EpayCreateCommand command, String tradeNo, String payInfo) {
        JSONObject root = JSON.parseObject(EpayCompatMetadata.encode(new EpayMetadataValue(
                command.version(),
                command.merchantId(),
                command.appId(),
                command.epayType(),
                command.orderNo(),
                command.notifyUrl(),
                command.returnUrl(),
                command.subject(),
                amountYuan(command.amountFen()),
                command.clientIp(),
                command.device(),
                tradeNo
        )));
        root.put("payDataType", PAY_DATA_TYPE);
        if (payInfo != null) {
            root.getJSONObject(EpayCompatMetadata.RESERVED_KEY).put(PAY_INFO_KEY, payInfo);
        }
        return root.toJSONString();
    }

    private String payInfo(String channelExtra) {
        try {
            JSONObject root = JSON.parseObject(channelExtra);
            JSONObject metadata = root.getJSONObject(EpayCompatMetadata.RESERVED_KEY);
            return metadata == null ? null : metadata.getString(PAY_INFO_KEY);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String amountYuan(long amountFen) {
        return BigDecimal.valueOf(amountFen, 2).toPlainString();
    }

    private static String messageOf(ApiRes apiRes) {
        if (apiRes == null || !hasText(apiRes.getMsg())) {
            return "统一下单失败";
        }
        return apiRes.getMsg();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
