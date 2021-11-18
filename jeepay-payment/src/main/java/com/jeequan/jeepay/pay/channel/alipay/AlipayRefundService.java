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
package com.jeequan.jeepay.pay.channel.alipay;

import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.channel.AbstractRefundService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.refund.RefundOrderRQ;
import org.springframework.stereotype.Service;

/*
* 退款接口： 支付宝官方
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/17 9:38
*/
@Service
public class AlipayRefundService extends AbstractRefundService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(refundOrder.getPayOrderId());
        model.setTradeNo(refundOrder.getChannelPayOrderNo());
        model.setOutRequestNo(refundOrder.getRefundOrderId());
        model.setRefundAmount(AmountUtil.convertCent2Dollar(refundOrder.getRefundAmount().toString()));
        model.setRefundReason(refundOrder.getRefundReason());
        request.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);

        AlipayTradeRefundResponse response = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        channelRetMsg.setChannelAttach(response.getBody());

        // 调用成功
        if(response.isSuccess()){
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        }else{

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode(response.getSubCode());
            channelRetMsg.setChannelErrMsg(response.getSubMsg());
        }
        return channelRetMsg;
    }

    @Override
    public ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception {

        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setTradeNo(refundOrder.getChannelPayOrderNo());
        model.setOutTradeNo(refundOrder.getPayOrderId());
        model.setOutRequestNo(refundOrder.getRefundOrderId());
        request.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);

        AlipayTradeFastpayRefundQueryResponse response = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        channelRetMsg.setChannelAttach(response.getBody());

        // 调用成功 & 金额相等  （传入不存在的outRequestNo支付宝仍然返回响应成功只是数据不存在， 调用isSuccess() 仍是成功, 此处需判断金额是否相等）
        Long channelRefundAmount = response.getRefundAmount() == null ? null : Long.parseLong(AmountUtil.convertDollar2Cent(response.getRefundAmount()));
        if(response.isSuccess() && refundOrder.getRefundAmount().equals(channelRefundAmount)){
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        }else{

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING); //认为是处理中

        }

        return channelRetMsg;
    }


}
