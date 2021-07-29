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
package com.jeequan.jeepay.components.mq.vender.rabbitmq.receive;

import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQMsgReceiver;
import com.jeequan.jeepay.components.mq.vender.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
* rabbitMQ消息接收器：仅在vender=rabbitMQ时 && 项目实现IMQReceiver接口时 进行实例化
* 业务：  更新服务商/商户/商户应用配置信息
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/22 17:06
*/
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.RABBIT_MQ)
@ConditionalOnBean(ResetIsvMchAppInfoConfigMQ.IMQReceiver.class)
public class ResetIsvMchAppInfoRabbitMQReceiver implements IMQMsgReceiver {

    @Autowired
    private ResetIsvMchAppInfoConfigMQ.IMQReceiver mqReceiver;

    /** 接收 【 MQSendTypeEnum.BROADCAST  】 广播类型的消息
     *
     * 注意：
     *   RabbitMQ的广播模式（fanout）交换机 --》全部的Queue
     *   如果queue包含多个消费者， 【例如，manager和payment的监听器是名称相同的queue下的消费者（Consumers） 】， 两个消费者是工作模式且存在竞争关系， 导致只能一个来消费。
     * 解决：
     *   每个topic的QUEUE都声明一个FANOUT交换机， 消费者声明一个系统产生的【随机队列】绑定到这个交换机上，然后往交换机发消息，只要绑定到这个交换机上都能收到消息。
     *   参考： https://bbs.csdn.net/topics/392509262?list=70088931
     *
     * **/
    @Override
    @RabbitListener(
            bindings = {@QueueBinding(value = @Queue(), // 注意这里不要定义队列名称,系统会随机产生
            exchange = @Exchange(name = RabbitMQConfig.FANOUT_EXCHANGE_NAME_PREFIX + ResetIsvMchAppInfoConfigMQ.MQ_NAME,
            type = ExchangeTypes.FANOUT ))} )
    public void receiveMsg(String msg){
        mqReceiver.receive(ResetIsvMchAppInfoConfigMQ.parse(msg));
    }

}
