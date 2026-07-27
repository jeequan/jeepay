package com.jeequan.jeepay.pay.channel.starpos.payway;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.channel.starpos.StarposPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.StarposQrOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import org.springframework.stereotype.Service;

/**
 * 星驿付聚合收银台二维码下单。
 */
@Service("starposPaymentByStarposQrService")
public class StarposQr extends StarposPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ bizRQ, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(
            UnifiedOrderRQ bizRQ,
            PayOrder payOrder,
            MchAppConfigContext mchAppConfigContext
    ) {
        StarposPayResult result = createQrOrder(payOrder, mchAppConfigContext);
        StarposQrOrderRS response = ApiResBuilder.buildSuccess(StarposQrOrderRS.class);
        response.setChannelRetMsg(result.getChannelRetMsg());
        if (result.getChannelRetMsg().getChannelState()
                == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
            response.setPayUrl(result.getCashierUrl());
        }
        return response;
    }
}
