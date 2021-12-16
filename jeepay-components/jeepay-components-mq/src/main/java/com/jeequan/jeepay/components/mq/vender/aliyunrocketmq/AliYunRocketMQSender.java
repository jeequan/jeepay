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

    private static final List<Integer> DELAY_TIME_LEVEL = new ArrayList<>();

    static {
        // 预设值的延迟时间间隔为：1s、 5s、 10s、 30s、 1m、 2m、 3m、 4m、 5m、 6m、 7m、 8m、 9m、 10m、 20m、 30m、 1h、 2h
        DELAY_TIME_LEVEL.add(1);
        DELAY_TIME_LEVEL.add(5);
        DELAY_TIME_LEVEL.add(10);
        DELAY_TIME_LEVEL.add(30);
        DELAY_TIME_LEVEL.add(60 * 1);
        DELAY_TIME_LEVEL.add(60 * 2);
        DELAY_TIME_LEVEL.add(60 * 3);
        DELAY_TIME_LEVEL.add(60 * 4);
        DELAY_TIME_LEVEL.add(60 * 5);
        DELAY_TIME_LEVEL.add(60 * 6);
        DELAY_TIME_LEVEL.add(60 * 7);
        DELAY_TIME_LEVEL.add(60 * 8);
        DELAY_TIME_LEVEL.add(60 * 9);
        DELAY_TIME_LEVEL.add(60 * 10);
        DELAY_TIME_LEVEL.add(60 * 20);
        DELAY_TIME_LEVEL.add(60 * 30);
        DELAY_TIME_LEVEL.add(60 * 60 * 1);
        DELAY_TIME_LEVEL.add(60 * 60 * 2);
    }

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
            long delayTime = System.currentTimeMillis() + getNearDelayLevel(delaySeconds) * 1000;
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
     * 获取最接近的节点值
     **/
    private int getNearDelayLevel(int delay) {
        // 如果包含则直接返回
        if (DELAY_TIME_LEVEL.contains(delay)) {
            return DELAY_TIME_LEVEL.indexOf(delay) + 1;
        }
        //两个时间的绝对值 - 位置
        TreeMap<Integer, Integer> resultMap = new TreeMap<>();
        DELAY_TIME_LEVEL.stream().forEach(time -> resultMap.put(Math.abs(delay - time), DELAY_TIME_LEVEL.indexOf(time) + 1));
        return resultMap.firstEntry().getValue();
    }


}
