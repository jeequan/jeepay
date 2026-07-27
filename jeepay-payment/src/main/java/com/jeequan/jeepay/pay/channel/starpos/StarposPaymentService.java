package com.jeequan.jeepay.pay.channel.starpos;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.starpos.StarposNormalMchParams;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.util.PaywayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 星驿付支付通道公共服务。
 *
 * <p>首期只负责固定环境、请求字段和结果边界；通知地址不动态下发，避免
 * 在正式协议字段未完全固化前把本地地址暴露给上游。</p>
 */
@Slf4j
@Service
public class StarposPaymentService extends AbstractPaymentService {

    static final String CREATE_ORDER_PATH = "/yyfsevr/order/getCodeUrl";
    static final String SUCCESS_CODE = "000000";
    static final String UNKNOWN_CODE = "-80000";
    static final String CONFIG_ERROR_CODE = "STARPOS_CONFIG_ERROR";
    static final int MAX_REMARK_LENGTH = 128;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.STARPOS;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return CS.PAY_WAY_CODE.STARPOS_QR.equals(wayCode);
    }

    @Override
    public String preCheck(UnifiedOrderRQ bizRQ, PayOrder payOrder) {
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode())
                .preCheck(bizRQ, payOrder);
    }

    @Override
    public AbstractRS pay(
            UnifiedOrderRQ bizRQ,
            PayOrder payOrder,
            MchAppConfigContext mchAppConfigContext
    ) throws Exception {
        return PaywayUtil.getRealPaywayService(this, payOrder.getWayCode())
                .pay(bizRQ, payOrder, mchAppConfigContext);
    }

    /**
     * 执行星驿付二维码下单。
     */
    protected StarposPayResult createQrOrder(
            PayOrder payOrder,
            MchAppConfigContext mchAppConfigContext
    ) {
        StarposNormalMchParams params;
        String baseUrl;
        JSONObject request;
        try {
            params = getParams(mchAppConfigContext);
            baseUrl = StarposConfig.resolveBaseUrl(params);
            request = buildCreateOrderRequest(params, payOrder);
        } catch (RuntimeException e) {
            log.error("星驿付下单配置或签名构造失败，订单标记为失败，orderNo={}",
                    maskOrderNo(payOrder == null ? null : payOrder.getPayOrderId()), e);
            return configurationFailure();
        }

        try {
            JSONObject response = createHttpClient(baseUrl)
                    .post(CREATE_ORDER_PATH, request, payOrder.getPayOrderId());
            return mapResponse(response, params, payOrder);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return unknownResult("星驿付下单请求被中断", e);
        } catch (IOException | RuntimeException e) {
            log.warn("星驿付下单请求异常，订单进入可查询状态，orderNo={}",
                    maskOrderNo(payOrder.getPayOrderId()), e);
            return unknownResult("星驿付下单结果未知", e);
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

    private JSONObject buildCreateOrderRequest(
            StarposNormalMchParams params,
            PayOrder payOrder
    ) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("agetId", params.getAgetId());
        fields.put("custId", params.getCustId());
        fields.put("version", params.getVersion());
        fields.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        fields.put("orderNo", payOrder.getPayOrderId());
        fields.put("txamt", payOrder.getAmount());
        fields.put("title", payOrder.getSubject());
        fields.put("remark", controlledRemark(payOrder));

        fields.put("sign", StarposSigner.signRequest(fields, params.getPublicKey()));
        return new JSONObject(fields);
    }

    private String controlledRemark(PayOrder payOrder) {
        String remark = StringUtils.defaultIfBlank(payOrder.getBody(), payOrder.getSubject());
        if (remark == null) {
            return "";
        }
        return remark.length() <= MAX_REMARK_LENGTH
                ? remark
                : remark.substring(0, MAX_REMARK_LENGTH);
    }

    private StarposPayResult mapResponse(
            JSONObject response,
            StarposNormalMchParams params,
            PayOrder payOrder
    ) {
        if (response == null) {
            return unknownResult("星驿付返回为空", null);
        }

        String code = response.getString("code");
        String message = StringUtils.defaultIfBlank(
                response.getString("message"),
                response.getString("msg")
        );
        ChannelRetMsg retMsg = new ChannelRetMsg();
        retMsg.setChannelErrCode(code);
        retMsg.setChannelErrMsg(message);
        retMsg.setChannelOriginResponse(response.toJSONString());

        if (SUCCESS_CODE.equals(code)) {
            String cashierUrl = response.getString("data");
            if (!isCashierUrlForEnvironment(cashierUrl, params)) {
                retMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                retMsg.setChannelErrMsg("星驿付收银链接不是当前环境的 HTTPS 地址");
                return new StarposPayResult(retMsg, null);
            }
            retMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            retMsg.setChannelOrderId(payOrder.getPayOrderId());
            return new StarposPayResult(retMsg, cashierUrl);
        }

        if (UNKNOWN_CODE.equals(code)) {
            retMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN);
            retMsg.setNeedQuery(true);
            return new StarposPayResult(retMsg, null);
        }

        retMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
        return new StarposPayResult(retMsg, null);
    }

    private StarposPayResult configurationFailure() {
        ChannelRetMsg retMsg = ChannelRetMsg.confirmFail(
                CONFIG_ERROR_CODE,
                "星驿付下单配置无效"
        );
        return new StarposPayResult(retMsg, null);
    }

    private StarposPayResult unknownResult(String message, Exception cause) {
        ChannelRetMsg retMsg = ChannelRetMsg.unknown(message);
        retMsg.setChannelErrCode(UNKNOWN_CODE);
        retMsg.setNeedQuery(true);
        if (cause != null) {
            log.debug("星驿付下单结果未知：{}", message, cause);
        }
        return new StarposPayResult(retMsg, null);
    }

    private boolean isCashierUrlForEnvironment(
            String cashierUrl,
            StarposNormalMchParams params
    ) {
        if (StringUtils.isBlank(cashierUrl)) {
            return false;
        }
        try {
            URI cashierUri = URI.create(cashierUrl);
            URI baseUri = URI.create(StarposConfig.resolveBaseUrl(params));
            return "https".equalsIgnoreCase(cashierUri.getScheme())
                    && cashierUri.getUserInfo() == null
                    && cashierUri.getPort() == -1
                    && cashierUri.getQuery() == null
                    && cashierUri.getFragment() == null
                    && baseUri.getHost().equalsIgnoreCase(cashierUri.getHost());
        } catch (IllegalArgumentException e) {
            return false;
        }
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

    protected static final class StarposPayResult {

        private final ChannelRetMsg channelRetMsg;
        private final String cashierUrl;

        private StarposPayResult(ChannelRetMsg channelRetMsg, String cashierUrl) {
            this.channelRetMsg = channelRetMsg;
            this.cashierUrl = cashierUrl;
        }

        public ChannelRetMsg getChannelRetMsg() {
            return channelRetMsg;
        }

        public String getCashierUrl() {
            return cashierUrl;
        }
    }
}
