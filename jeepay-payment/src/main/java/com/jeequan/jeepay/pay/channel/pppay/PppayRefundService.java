package com.jeequan.jeepay.pay.channel.pppay;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.channel.AbstractRefundService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.model.PaypalWrapper;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.refund.RefundOrderRQ;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.http.serializer.Json;
import com.paypal.payments.*;
import org.springframework.stereotype.Service;

/**
 * none.
 *
 * @author 陈泉
 * @package com.jeequan.jeepay.pay.channel.pppay
 * @create 2021/11/16 20:20
 */
@Service
public class PppayRefundService extends AbstractRefundService {
    @Override
    public String getIfCode() {
        return CS.IF_CODE.PPPAY;
    }

    @Override
    public String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder,
                                MchAppConfigContext mchAppConfigContext) throws Exception {
        if (payOrder.getChannelOrderNo() == null) {
            return ChannelRetMsg.confirmFail();
        }

        PaypalWrapper paypalWrapper = mchAppConfigContext.getPaypalWrapper();

        // 因为退款需要商户 Token 而同步支付回调不会保存订单信息
        String ppOrderId = paypalWrapper.processOrder(payOrder.getChannelOrderNo()).get(0);
        String ppCatptId = paypalWrapper.processOrder(payOrder.getChannelOrderNo()).get(1);

        if (ppOrderId == null || ppCatptId == null) {
            return ChannelRetMsg.confirmFail();
        }

        PayPalHttpClient client = paypalWrapper.getClient();

        // 处理金额
        String amountStr = AmountUtil.convertCent2Dollar(refundOrder.getRefundAmount().toString());
        String currency = payOrder.getCurrency().toUpperCase();

        RefundRequest refundRequest = new RefundRequest();
        Money money = new Money();
        money.currencyCode(currency);
        money.value(amountStr);

        refundRequest.invoiceId(refundOrder.getRefundOrderId());
        refundRequest.amount(money);
        refundRequest.noteToPayer(bizRQ.getRefundReason());

        CapturesRefundRequest request = new CapturesRefundRequest(ppCatptId);
        request.prefer("return=representation");
        request.requestBody(refundRequest);

        ChannelRetMsg channelRetMsg = ChannelRetMsg.waiting();
        channelRetMsg.setResponseEntity(paypalWrapper.textResp("ERROR"));
        HttpResponse<Refund> response;
        try{
            response = client.execute(request);
        }catch (HttpException e) {
            String message = e.getMessage();
            cn.hutool.json.JSONObject messageObj = JSONUtil.parseObj(message);
            String issue = messageObj.getByPath("details[0].issue", String.class);
            String description = messageObj.getByPath("details[0].description", String.class);
            return ChannelRetMsg.confirmFail(issue, description);
        }

        if (response.statusCode() == 201) {
            String responseJson = new Json().serialize(response.result());
            channelRetMsg = paypalWrapper.dispatchCode(response.result().status(), channelRetMsg);
            channelRetMsg.setChannelAttach(responseJson);
            channelRetMsg.setChannelOrderId(response.result().id());
            channelRetMsg.setResponseEntity(paypalWrapper.textResp("SUCCESS"));
        } else {
            return ChannelRetMsg.confirmFail("201", "请求退款失败，Paypal 响应非 201");
        }

        return channelRetMsg;
    }

    @Override
    public ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        if (refundOrder.getChannelOrderNo() == null) {
            return ChannelRetMsg.confirmFail();
        }

        PaypalWrapper wrapper = mchAppConfigContext.getPaypalWrapper();
        PayPalHttpClient client = wrapper.getClient();

        RefundsGetRequest refundRequest = new RefundsGetRequest(refundOrder.getPayOrderId());
        HttpResponse<Refund> response = client.execute(refundRequest);

        ChannelRetMsg channelRetMsg = ChannelRetMsg.waiting();
        channelRetMsg.setResponseEntity(wrapper.textResp("ERROR"));

        if (response.statusCode() == 201) {
            String responseJson = new Json().serialize(response.result());
            channelRetMsg = wrapper.dispatchCode(response.result().status(), channelRetMsg);
            channelRetMsg.setChannelAttach(responseJson);
            channelRetMsg.setChannelOrderId(response.result().id());
            channelRetMsg.setResponseEntity(wrapper.textResp("SUCCESS"));
        } else {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode("201");
            channelRetMsg.setChannelErrMsg("请求退款详情失败，Paypal 响应非 200");
        }

        return channelRetMsg;
    }
}
