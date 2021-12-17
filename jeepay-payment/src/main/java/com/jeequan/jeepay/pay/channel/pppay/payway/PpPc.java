package com.jeequan.jeepay.pay.channel.pppay.payway;

import cn.hutool.json.JSONUtil;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.channel.pppay.PppayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.model.PaypalWrapper;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.PPPcOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.PPPcOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
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
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws
            Exception {
        PPPcOrderRQ bizRQ = (PPPcOrderRQ) rq;

        OrderRequest orderRequest = new OrderRequest();

        // 配置 Paypal ApplicationContext 也就是支付页面信息
        ApplicationContext applicationContext = new ApplicationContext()
                .brandName(mchAppConfigContext.getMchApp().getAppName())
                .landingPage("NO_PREFERENCE")
                .returnUrl(getReturnUrl(payOrder.getPayOrderId()))
                .userAction("PAY_NOW")
                .shippingPreference("NO_SHIPPING");

        if(StringUtils.isNotBlank(bizRQ.getCancelUrl())) {
            applicationContext.cancelUrl(bizRQ.getCancelUrl());
        }

        orderRequest.applicationContext(applicationContext);
        orderRequest.checkoutPaymentIntent("CAPTURE");

        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();

        // 金额换算
        String amountStr = AmountUtil.convertCent2Dollar(payOrder.getAmount().toString());
        String currency = payOrder.getCurrency().toUpperCase();

        // 由于 Paypal 是支持订单多商品的，这里值添加一个
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                // 绑定 订单 ID 否则回调和异步较难处理
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

        // 从缓存获取 Paypal 操作工具
        PaypalWrapper paypalWrapper = configContextQueryService.getPaypalWrapper(mchAppConfigContext);

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("prefer", "return=representation");
        request.requestBody(orderRequest);

        // 构造函数响应数据
        PPPcOrderRS res = ApiResBuilder.buildSuccess(PPPcOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();

        HttpResponse<Order> response;
        try{
            response = paypalWrapper.getClient().execute(request);
        }catch (HttpException e) {
            String message = e.getMessage();
            cn.hutool.json.JSONObject messageObj = JSONUtil.parseObj(message);
            String issue = messageObj.getByPath("details[0].issue", String.class);
            String description = messageObj.getByPath("details[0].description", String.class);
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode(issue);
            channelRetMsg.setChannelErrMsg(description);
            res.setChannelRetMsg(channelRetMsg);
            return res;
        }

        // 标准返回 HttpPost 需要为 201
        if (response.statusCode() == 201) {
            Order order = response.result();
            String status = response.result().status();
            String tradeNo = response.result().id();

            // 从返回数据里读取出支付链接
            LinkDescription paypalLink = order.links().stream().reduce(null, (result, curr) -> {
                if (curr.rel().equalsIgnoreCase("approve") && curr.method().equalsIgnoreCase("get")) {
                    result = curr;
                }
                return result;
            });

            // 设置返回实体
            channelRetMsg.setChannelAttach(JSONUtil.toJsonStr(new Json().serialize(order)));
            channelRetMsg.setChannelOrderId(tradeNo + "," + "null"); // 拼接订单ID
            channelRetMsg = paypalWrapper.dispatchCode(status, channelRetMsg); // 处理状态码

            // 设置支付链接
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
