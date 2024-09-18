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
package com.jeequan.jeepay.components.mq.vender.aliyunrocketmq;

import com.aliyun.openservices.ons.api.*;
import com.jeequan.jeepay.components.mq.constant.MQSendTypeEnum;
import com.jeequan.jeepay.components.mq.vender.IMQMsgReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public abstract class AbstractAliYunRocketMQReceiver implements IMQMsgReceiver, InitializingBean {

    @Autowired
    private AliYunRocketMQFactory aliYunRocketMQFactory;

    /**
     * 获取topic名称
     *
     * @return
     */
    public abstract String getMQName();

    /**
     * 获取业务名称
     *
     * @return
     */
    public abstract String getConsumerName();

    /**
     * 发送类型
     *
     * @return
     */
    public MQSendTypeEnum getMQType() {
        // QUEUE - 点对点 （只有1个消费者可消费。 ActiveMQ的queue模式 ）
        return MQSendTypeEnum.QUEUE;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        Consumer aliyunConsumer = getMQType().equals(MQSendTypeEnum.BROADCAST) ?
                aliYunRocketMQFactory.getAliyunRocketMQClientBroadcastConsumer() : aliYunRocketMQFactory.getAliyunRocketMQConsumer();

        aliyunConsumer.subscribe(this.getMQName(), AliYunRocketMQFactory.defaultTag, (message, context) -> {
            try {
                receiveMsg(new String(message.getBody()));
                log.debug("【{}】MQ消息消费成功topic:{}, messageId:{}", getConsumerName(), message.getTopic(), message.getMsgID());
                return Action.CommitMessage;
            } catch (Exception e) {
                log.error("【{}】MQ消息消费失败topic:{}, messageId:{}", getConsumerName(), message.getTopic(), message.getMsgID(), e);
            }
            return Action.ReconsumeLater;
        });
        aliyunConsumer.start();
        log.info("初始化[{}]消费者topic: {},tag: {}成功", getConsumerName(), this.getMQName(), AliYunRocketMQFactory.defaultTag);
    }

}
