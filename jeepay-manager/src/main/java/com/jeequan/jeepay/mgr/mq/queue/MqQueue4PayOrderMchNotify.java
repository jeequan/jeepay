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
package com.jeequan.jeepay.mgr.mq.queue;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.mgr.mq.service.MqPayOrderNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Queue;

/**
* 商户订单回调MQ通知
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/21 18:03
*/
@Slf4j
@Component
@Profile(CS.MQTYPE.ACTIVE_MQ)
public class MqQueue4PayOrderMchNotify extends MqPayOrderNotifyService {

    @Autowired private JmsTemplate jmsTemplate;

    @Bean("payOrderMchNotify")
    public Queue mqQueue4PayOrderMchNotify(){
        return new ActiveMQQueue(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY);
    }

    @Lazy
    @Autowired
    @Qualifier("payOrderMchNotify")
    private Queue mqQueue4PayOrderMchNotify;


    /** 发送MQ消息 **/
    @Override
    public void send(String msg) {
        this.jmsTemplate.convertAndSend(mqQueue4PayOrderMchNotify, msg);
    }

}
