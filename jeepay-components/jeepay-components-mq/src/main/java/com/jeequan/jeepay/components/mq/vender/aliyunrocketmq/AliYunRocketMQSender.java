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

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendResult;
import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.components.mq.model.AbstractMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * 阿里云rocketMQ 消息发送器的实现
 */
@Slf4j
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ALIYUN_ROCKET_MQ)
public class AliYunRocketMQSender implements IMQSender {

    /** 最大延迟24小时 */
    private static final int MAX_DELAY_TIME = 60 * 60 * 24;
    /** 最小延迟1秒 */
    private static final int MIN_DELAY_TIME = 1;

    @Autowired
    private AliYunRocketMQFactory aliYunRocketMQFactory;
    private Producer producerClient;

    @Override
    public void send(AbstractMQ mqModel) {
        Message message = new Message(mqModel.getMQName(), AliYunRocketMQFactory.defaultTag, mqModel.toMessage().getBytes());
        sendMessage(message);
    }

    @Override
    public void send(AbstractMQ mqModel, int delaySeconds) {
        Message message = new Message(mqModel.getMQName(), AliYunRocketMQFactory.defaultTag, mqModel.toMessage().getBytes());
        if (delaySeconds > 0) {
            long delayTime = System.currentTimeMillis() + delayTimeCorrector(delaySeconds) * 1000;
            // 设置消息需要被投递的时间。
            message.setStartDeliverTime(delayTime);
        }
        sendMessage(message);
    }

    private void sendMessage(Message message) {
        if (producerClient == null) {
            producerClient = aliYunRocketMQFactory.producerClient();
        }
        producerClient.start();
        SendResult sendResult = producerClient.send(message);
        log.info("消息队列推送返回结果：{}", JSONObject.toJSONString(sendResult));
    }

    /**
     * 检查延迟时间的有效性并返回校正后的延迟时间
     **/
    private int delayTimeCorrector(int delay) {
        if (delay < MIN_DELAY_TIME) {
            return MIN_DELAY_TIME;
        }
        if (delay > MAX_DELAY_TIME) {
            return MAX_DELAY_TIME;
        }
        return delay;
    }


}
