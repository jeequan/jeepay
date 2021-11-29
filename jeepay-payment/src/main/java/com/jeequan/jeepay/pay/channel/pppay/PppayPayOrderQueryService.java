package com.jeequan.jeepay.pay.channel.pppay;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.channel.IPayOrderQueryService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import org.springframework.stereotype.Service;

/**
 * none.
 *
 * @author 陈泉
 * @package com.jeequan.jeepay.pay.channel.pppay
 * @create 2021/11/15 21:02
 */
@Service
public class PppayPayOrderQueryService implements IPayOrderQueryService {
    @Override
    public String getIfCode() {
        return CS.IF_CODE.PPPAY;
    }

    @Override
    public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        return mchAppConfigContext.getPaypalWrapper().processOrder(null, payOrder);
    }
}
