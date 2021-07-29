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
package com.jeequan.jeepay.pay.mq;

import com.jeequan.jeepay.components.mq.model.PayOrderReissueMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ChannelOrderReissueService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 接收MQ消息
 * 业务： 支付订单补单（一般用于没有回调的接口，比如微信的条码支付）
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/7/27 9:23
 */
@Slf4j
@Component
public class PayOrderReissueMQReceiver implements PayOrderReissueMQ.IMQReceiver {

    @Autowired
    private IMQSender mqSender;
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ChannelOrderReissueService channelOrderReissueService;


    @Override
    public void receive(PayOrderReissueMQ.MsgPayload payload) {
        try {
            String payOrderId = payload.getPayOrderId();
            int currentCount = payload.getCount();
            log.info("接收轮询查单通知MQ, payOrderId={}, count={}", payOrderId, currentCount);
            currentCount++ ;

            PayOrder payOrder = payOrderService.getById(payOrderId);
            if(payOrder == null) {
                log.warn("查询支付订单为空,payOrderId={}", payOrderId);
                return;
            }

            if(payOrder.getState() != PayOrder.STATE_ING) {
                log.warn("订单状态不是支付中,不需查询渠道.payOrderId={}", payOrderId);
                return;
            }

            ChannelRetMsg channelRetMsg = channelOrderReissueService.processPayOrder(payOrder);

            //返回null 可能为接口报错等， 需要再次轮询
            if(channelRetMsg == null || channelRetMsg.getChannelState() == null || channelRetMsg.getChannelState().equals(ChannelRetMsg.ChannelState.WAITING)){

                //最多查询6次
                if(currentCount <= 6){
                    mqSender.send(PayOrderReissueMQ.build(payOrderId, currentCount), 5); //延迟5s再次查询
                }else{

                    //TODO 调用【撤销订单】接口

                }

            }else{ //其他状态， 不需要再次轮询。
            }
        }catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
    }
}
