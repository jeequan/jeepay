package com.jeequan.jeepay.pay.channel.pppay.payway;

import cn.hutool.json.JSONUtil;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.pay.channel.pppay.PppayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.model.PaypalWrapper;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.PPPcOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.PPPcOrderRS;
import com.paypal.http.HttpResponse;
import com.paypal.http.serializer.Json;
import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * none.
 *
 * @author 陈泉
 * @package com.jeequan.jeepay.pay.channel.pppay.payway
 * @create 2021/11/15 18:59
 */
@Slf4j
@Service("pppayPaymentByPPPCService")
public class PpPc extends PppayPaymentService {
    @Override
    public String preCheck(UnifiedOrderRQ bizRQ, PayOrder payOrder) {
        PPPcOrderRQ rq = (PPPcOrderRQ) bizRQ;
        if (StringUtils.isEmpty(rq.getCancelUrl())) {
            throw new BizException("用户取消支付回调[cancelUrl]不可为空");
        }
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        PPPcOrderRQ bizRQ = (PPPcOrderRQ) rq;

        OrderRequest orderRequest = new OrderRequest();

        ApplicationContext applicationContext = new ApplicationContext()
                .brandName(mchAppConfigContext.getMchApp().getAppName())
                .landingPage("NO_PREFERENCE")
                .cancelUrl(bizRQ.getCancelUrl())
                .returnUrl(getReturnUrl(payOrder.getPayOrderId()))
                .userAction("PAY_NOW")
                .shippingPreference("NO_SHIPPING");

        orderRequest.applicationContext(applicationContext);
        orderRequest.checkoutPaymentIntent("CAPTURE");

        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();

        long amount = (payOrder.getAmount() / 100);
        String amountStr = Long.toString(amount, 10);
        String currency = payOrder.getCurrency().toUpperCase();

        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .customId(payOrder.getPayOrderId())
                .invoiceId(payOrder.getPayOrderId())
                .amountWithBreakdown(new AmountWithBreakdown()
                        .currencyCode(currency)
                        .value(amountStr)
                        .amountBreakdown(
                                new AmountBreakdown().itemTotal(new Money().currencyCode(currency).value(amountStr))
                        )
                )
                .items(new ArrayList<Item>() {
                    {
                        add(
                                new Item()
                                        .name(payOrder.getSubject())
                                        .description(payOrder.getBody())
                                        .sku(payOrder.getPayOrderId())
                                        .unitAmount(new Money().currencyCode(currency).value(amountStr))
                                        .quantity("1")
                        );
                    }
                });

        purchaseUnitRequests.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnitRequests);

        PaypalWrapper palApiConfig = mchAppConfigContext.getPaypalWrapper();

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("prefer", "return=representation");
        request.requestBody(orderRequest);
        HttpResponse<Order> response = palApiConfig.getClient().execute(request);

        PPPcOrderRS res = new PPPcOrderRS();
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();

        if (response.statusCode() == 201) {
            Order order = response.result();
            String status = response.result().status();
            String tradeNo = response.result().id();

            LinkDescription paypalLink = order.links().stream().reduce(null, (result, curr) -> {
                if (curr.rel().equalsIgnoreCase("approve") && curr.method().equalsIgnoreCase("get")) {
                    result = curr;
                }
                return result;
            });

            channelRetMsg.setChannelAttach(JSONUtil.toJsonStr(new Json().serialize(order)));
            channelRetMsg.setChannelOrderId(tradeNo + "," + "null");

            if (status.equalsIgnoreCase("SAVED")) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            } else if (status.equalsIgnoreCase("APPROVED")) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            } else if (status.equalsIgnoreCase("VOIDED")) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            } else if (status.equalsIgnoreCase("COMPLETED")) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            } else if (status.equalsIgnoreCase("PAYER_ACTION_REQUIRED")) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            } else if (status.equalsIgnoreCase("CREATED")) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            }

            res.setPayUrl(paypalLink.href());
        } else {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode("201");
            channelRetMsg.setChannelErrMsg("请求失败，Paypal 响应非 201");
        }

        res.setChannelRetMsg(channelRetMsg);
        return res;
    }
}
