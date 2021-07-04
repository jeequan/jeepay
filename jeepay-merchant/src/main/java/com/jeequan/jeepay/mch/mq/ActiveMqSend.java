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
package com.jeequan.jeepay.mch.mq;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.mq.MqCommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/*
* 更改商户应用信息
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 17:10
*/
@Slf4j
@Component
@Profile(CS.MQTYPE.ACTIVE_MQ)
public class ActiveMqSend extends MqCommonService {

    @Autowired private JmsTemplate jmsTemplate;

    @Bean("activeMqSendModifyMchApp")
    public ActiveMQTopic mqTopic4ModifyMchApp(){
        return new ActiveMQTopic(CS.MQ.TOPIC_MODIFY_MCH_APP);
    }

    @Lazy
    @Autowired
    @Qualifier("activeMqSendModifyMchApp")
    private ActiveMQTopic mqTopic4ModifyMchApp;

    /** 推送消息到各个节点 **/
    @Override
    public void send(String msg, String sendType) {
        if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_MCH_APP)) { // 商户应用修改
            topicModifyMchApp(msg);
        }
    }

    @Override
    public void send(String msg, long delay, String sendType) {

    }

    /** 发送商户应用修改信息 **/
    public void topicModifyMchApp(String msg) {
        this.jmsTemplate.convertAndSend(mqTopic4ModifyMchApp, msg);
    }

}
