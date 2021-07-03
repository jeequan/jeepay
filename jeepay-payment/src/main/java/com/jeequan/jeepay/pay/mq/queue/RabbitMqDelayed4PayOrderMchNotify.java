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
import com.jeequan.jeepay.pay.mq.MqReceiveServiceImpl;
import com.jeequan.jeepay.pay.mq.queue.service.MqPayOrderMchNotifyService;
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
public class RabbitMqDelayed4PayOrderMchNotify extends MqPayOrderMchNotifyService {

    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private MqReceiveServiceImpl mqReceiveServiceImpl;

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


    /** 接收 支付订单商户回调消息 **/
    @RabbitListener(queues = CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY)
    public void receive(String msg) {
        Integer currentCount = mqReceiveServiceImpl.payOrderMchNotify(msg);
        // 通知延时次数
//        1   2  3  4   5   6
//        0  30 60 90 120 150
        send(msg, currentCount * 30 * 1000);
    }


}
