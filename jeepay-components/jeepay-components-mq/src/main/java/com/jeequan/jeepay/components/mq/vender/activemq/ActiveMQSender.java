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
package com.jeequan.jeepay.components.mq.vender.activemq;

import com.jeequan.jeepay.components.mq.model.AbstractMQ;
import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import jakarta.jms.TextMessage;

/**
*  activeMQ 消息发送器的实现
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/23 16:52
*/
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ACTIVE_MQ)
public class ActiveMQSender implements IMQSender {

    @Autowired
    private ActiveMQConfig activeMQConfig;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void send(AbstractMQ mqModel) {
        jmsTemplate.convertAndSend(activeMQConfig.getDestination(mqModel), mqModel.toMessage());
    }

    @Override
    public void send(AbstractMQ mqModel, int delay) {
        jmsTemplate.send(activeMQConfig.getDestination(mqModel), session -> {
            TextMessage tm = session.createTextMessage(mqModel.toMessage());
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay * 1000);
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1*1000);
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
            return tm;
        });
    }

}
