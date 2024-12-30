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

import com.jeequan.jeepay.components.mq.constant.MQSendTypeEnum;
import com.jeequan.jeepay.components.mq.model.AbstractMQ;
import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.stereotype.Component;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
* activeMQ的配置项
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/23 16:51
*/
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ACTIVE_MQ)
public class ActiveMQConfig {

    Map<String, Destination> map = new ConcurrentHashMap<>();

    public Destination getDestination(AbstractMQ mqModel){

        if(map.get(mqModel.getMQName()) == null){
            this.init(mqModel.getMQName(), mqModel.getMQType());
        }
        return map.get(mqModel.getMQName());
    }

    private synchronized void init(String mqName, MQSendTypeEnum mqSendTypeEnum){

        if(mqSendTypeEnum == MQSendTypeEnum.QUEUE){
            map.put(mqName, new ActiveMQQueue(mqName) );
        }else{
            map.put(mqName, new ActiveMQTopic(mqName) );
        }
    }


    public static final String TOPIC_LISTENER_CONTAINER = "jmsTopicListenerContainer";

    /** 新增jmsListenerContainer, 用于接收topic类型的消息 **/
    @Bean
    public JmsListenerContainerFactory<?> jmsTopicListenerContainer(ConnectionFactory factory){
        DefaultJmsListenerContainerFactory bean = new DefaultJmsListenerContainerFactory();
        bean.setPubSubDomain(true);
        bean.setConnectionFactory(factory);
        return bean;
    }

}
