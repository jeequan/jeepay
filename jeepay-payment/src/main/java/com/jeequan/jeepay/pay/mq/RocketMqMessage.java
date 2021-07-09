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
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
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
@Profile(CS.MQTYPE.ROCKET_MQ)
public class RocketMqMessage extends MqCommonService {

    @Autowired private RocketMQTemplate rocketMQTemplate;

    @Lazy
    @Autowired
    private MqReceiveCommon mqReceiveCommon;

    @Override
    public void send(String msg, String sendType) {
        if (sendType.equals(CS.MQ.MQ_TYPE_CHANNEL_ORDER_QUERY)) {
            channelOrderQuery(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_PAY_ORDER_MCH_NOTIFY)) {
            payOrderMchNotify(msg);
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
        rocketMQTemplate.convertAndSend(CS.MQ.QUEUE_CHANNEL_ORDER_QUERY, msg);
    }

    /** 发送订单查询延迟消息 **/
    public void channelOrderQueryFixed(String msg, long delay) {
        rocketMQTemplate.asyncSend(CS.MQ.QUEUE_CHANNEL_ORDER_QUERY, MessageBuilder.withPayload(msg).build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult var1) {
                log.info("async onSucess SendResult :{}", var1);
            }
            @Override
            public void onException(Throwable var1) {
                log.info("async onException Throwable :{}", var1);
            }
        }, 300000, 2);
    }

    /** 发送订单回调消息 **/
    public void payOrderMchNotify(String msg) {
        rocketMQTemplate.convertAndSend(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY, msg);
    }

    /** 发送订单回调延迟消息 **/
    public void payOrderMchNotifyFixed(String msg, long delay) {
        rocketMQTemplate.asyncSend(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY, MessageBuilder.withPayload(msg).build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult var1) {
                log.info("async onSucess SendResult :{}", var1);
            }
            @Override
            public void onException(Throwable var1) {
                log.info("async onException Throwable :{}", var1);
            }
        }, 300000, 4);
    }

    /** 接收 查单消息 **/
    @Service
    @RocketMQMessageListener(topic = CS.MQ.QUEUE_CHANNEL_ORDER_QUERY, consumerGroup = CS.MQ.QUEUE_CHANNEL_ORDER_QUERY)
    class receiveChannelOrderQuery implements RocketMQListener<String> {
        @Override
        public void onMessage(String msg) {
            mqReceiveCommon.channelOrderQuery(msg);
        }
    }


    /** 接收 支付订单商户回调消息 **/
    @Service
    @RocketMQMessageListener(topic = CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY, consumerGroup = CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY)
    class receivePayOrderMchNotify implements RocketMQListener<String> {
        @Override
        public void onMessage(String msg) {
            mqReceiveCommon.payOrderMchNotify(msg);
        }
    }
}
