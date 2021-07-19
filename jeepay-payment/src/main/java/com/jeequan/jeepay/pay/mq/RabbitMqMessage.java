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

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.mq.MqCommonService;
import com.jeequan.jeepay.pay.mq.receive.MqReceiveCommon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
public class RabbitMqMessage extends MqCommonService {

    @Autowired private RabbitTemplate rabbitTemplate;

    @Lazy
    @Autowired
    private MqReceiveCommon mqReceiveCommon;

    @Override
    public void send(String msg, String sendType) {
        if (sendType.equals(CS.MQ.MQ_TYPE_CHANNEL_ORDER_QUERY)) {
            channelOrderQuery(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_PAY_ORDER_MCH_NOTIFY)) {
            payOrderMchNotify(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_MCH_APP)) {   // 商户应用修改
            directModifyMchApp(msg);
        }
    }

    @Override
    public void send(String msg, long delay, String sendType) {
        if (sendType.equals(CS.MQ.MQ_TYPE_CHANNEL_ORDER_QUERY)) {
            channelOrderQueryFixed(msg, delay);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_PAY_ORDER_MCH_NOTIFY)) {
            payOrderMchNotifyFixed(msg, delay);
        }
    }

    /** 发送订单查询消息 **/
    public void channelOrderQuery(String msg) {
        rabbitTemplate.convertAndSend(CS.MQ.QUEUE_CHANNEL_ORDER_QUERY, msg);
    }

    /** 发送订单查询延迟消息 **/
    public void channelOrderQueryFixed(String msg, long delay) {
        rabbitTemplate.convertAndSend(CS.DELAYED_EXCHANGE, CS.MQ.QUEUE_CHANNEL_ORDER_QUERY, msg, a ->{
            a.getMessageProperties().setDelay(Math.toIntExact(delay));
            return a;
        });
    }

    /** 发送订单回调消息 **/
    public void payOrderMchNotify(String msg) {
        rabbitTemplate.convertAndSend(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY, msg);
    }

    /** 发送订单回调延迟消息 **/
    public void payOrderMchNotifyFixed(String msg, long delay) {
        rabbitTemplate.convertAndSend(CS.DELAYED_EXCHANGE, CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY, msg, a ->{
            a.getMessageProperties().setDelay(Math.toIntExact(delay));
            return a;
        });
    }

    /** 发送商户应用修改消息 **/
    public void directModifyMchApp(String msg) {
        rabbitTemplate.convertAndSend(CS.DIRECT_EXCHANGE, CS.MQ.TOPIC_MODIFY_MCH_APP, msg);
    }


    /** 接收 查单消息 **/
    @RabbitListener(queues = CS.MQ.QUEUE_CHANNEL_ORDER_QUERY)
    public void receiveChannelOrderQuery(String msg) {
        mqReceiveCommon.channelOrderQuery(msg);
    }

    /** 接收 支付订单商户回调消息 **/
    @RabbitListener(queues = CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY)
    public void receivePayOrderMchNotify(String msg) {
        mqReceiveCommon.payOrderMchNotify(msg);
    }
}
