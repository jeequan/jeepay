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
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ChannelOrderReissueService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.TextMessage;

/*
 * 上游渠道订单轮询查单
 * 如：微信的条码支付，没有回调接口， 需要轮询查单完成交易结果通知。
 *
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 17:30
*/
@Slf4j
@Component
public class MqQueue4ChannelOrderQuery extends ActiveMQQueue{

    @Autowired private JmsTemplate jmsTemplate;
    @Autowired private PayOrderService payOrderService;
    @Autowired private ChannelOrderReissueService channelOrderReissueService;

    public static final String buildMsg(String payOrderId, int count){
        return payOrderId + "," + count;
    }

    /** 构造函数 */
    public MqQueue4ChannelOrderQuery(){
        super(CS.MQ.QUEUE_CHANNEL_ORDER_QUERY);
    }

    /** 发送MQ消息 **/
    public void send(String msg) {
        this.jmsTemplate.convertAndSend(this, msg);
    }

    /** 发送MQ消息 **/
    public void send(String msg, long delay) {
        jmsTemplate.send(this, session -> {
            TextMessage tm = session.createTextMessage(msg);
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1*1000);
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
            return tm;
        });
    }


    /** 接收 更新系统配置项的消息 **/
    @JmsListener(destination = CS.MQ.QUEUE_CHANNEL_ORDER_QUERY)
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
