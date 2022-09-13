/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.pay.channel.plspay.payway;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.plspay.PlspayConfig;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.pay.channel.plspay.PlspayKit;
import com.jeequan.jeepay.pay.channel.plspay.PlspayPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AliBarOrderRS;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.WxBarOrderRQ;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 计全付 微信 bar
 *
 * @author yr
 * @site https://www.jeequan.com
 * @date 2022/8/16 18:37
 */
@Service("plspayPaymentByWxBarService") //Service Name需保持全局唯一性
public class WxBar extends PlspayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        WxBarOrderRQ bizRQ = (WxBarOrderRQ) rq;
        if (StringUtils.isEmpty(bizRQ.getAuthCode())) {
            throw new BizException("用户支付条码[authCode]不可为空");
        }
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        WxBarOrderRQ bizRQ = (WxBarOrderRQ) rq;

        // 构造函数响应数据
        AliBarOrderRS res = ApiResBuilder.buildSuccess(AliBarOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        try {
            // 构建请求数据
            PayOrderCreateReqModel model = new PayOrderCreateReqModel();
            // 支付方式
            model.setWayCode(PlspayConfig.WX_BAR);
            // 异步通知地址
            model.setNotifyUrl(getNotifyUrl());
            JSONObject channelExtra = new JSONObject();
            channelExtra.put("authCode", bizRQ.getAuthCode());
            // 用户付款码值
            model.setChannelExtra(channelExtra.toString());

            // 发起统一下单
            PayOrderCreateResponse response = PlspayKit.payRequest(payOrder, mchAppConfigContext, model);
            // 下单返回状态
            Boolean isSuccess = PlspayKit.checkPayResp(response, mchAppConfigContext);

            if (isSuccess) {
                // 下单成功
                if (PlspayConfig.PAY_STATE_SUCCESS.equals(response.getData().getString("orderState"))) {
                    // 支付成功
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else {
                    // 支付中
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                }
                channelRetMsg.setChannelOrderId(response.get().getPayOrderId());
            } else {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(response.getCode()+"");
                channelRetMsg.setChannelErrMsg(response.getMsg());
            }
        } catch (JeepayException e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
        }
        return res;
    }
}
