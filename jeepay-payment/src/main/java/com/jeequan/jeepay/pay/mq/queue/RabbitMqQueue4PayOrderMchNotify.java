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
package com.jeequan.jeepay.pay.mq.queue;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.mq.queue.service.MqChannelOrderQueryService;
import com.jeequan.jeepay.pay.mq.queue.service.MqPayOrderMchNotifyService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ChannelOrderReissueService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
* RabbitMQ
* 上游渠道订单轮询查单
* 如：微信的条码支付，没有回调接口， 需要轮询查单完成交易结果通知。
*
*
* @author xiaoyu
* @site https://www.jeepay.vip
* @date 2021/6/25 17:10
*/
@Slf4j
@Component
@Profile(CS.MQTYPE.RABBIT_MQ)
public class RabbitMqQueue4PayOrderMchNotify extends MqPayOrderMchNotifyService {

    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private PayOrderService payOrderService;
    @Autowired private ChannelOrderReissueService channelOrderReissueService;

    public static final String buildMsg(String payOrderId, int count){
        return payOrderId + "," + count;
    }

    /** 发送MQ消息 **/
    @Override
    public void send(String msg) {
        rabbitTemplate.convertAndSend(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY, msg);
    }

    /** 发送MQ消息 **/
    @Override
    public void send(String msg, long delay) {
        rabbitTemplate.convertAndSend(CS.DELAYED_EXCHANGE, CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY, msg, a ->{
            a.getMessageProperties().setDelay(Math.toIntExact(delay));
            return a;
        });
    }


    /** 接收 更新系统配置项的消息 **/
    @RabbitListener(queues = CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY)
    public void receive(String msg) {

        String [] arr = msg.split(",");
        String payOrderId = arr[0];
        int currentCount = Integer.parseInt(arr[1]);
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
                send(buildMsg(payOrderId, currentCount), 5 * 1000); //延迟5s再次查询
            }else{

                //TODO 调用【撤销订单】接口

            }

        }else{ //其他状态， 不需要再次轮询。
        }

        return;
    }


}
