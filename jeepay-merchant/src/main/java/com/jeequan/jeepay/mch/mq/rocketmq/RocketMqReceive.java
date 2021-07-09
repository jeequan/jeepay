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
package com.jeequan.jeepay.mch.mq.rocketmq;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.mch.mq.receive.MqReceiveCommon;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 消息接收
 * @author pangxiaoyu
 * @site https://www.jeepay.vip
 * @date 2021-04-27 15:50
 */
@Slf4j
@Component
@Profile(CS.MQTYPE.ROCKET_MQ)
public class RocketMqReceive {

    @Autowired private MqReceiveCommon mqReceiveCommon;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:17
     * @describe: 接收 商户用户登录信息清除消息
     */
    @Service
    @RocketMQMessageListener(topic = CS.MQ.QUEUE_MODIFY_MCH_USER_REMOVE, consumerGroup = CS.MQ.QUEUE_MODIFY_MCH_USER_REMOVE)
    class receiveRemoveMchUser implements RocketMQListener<String> {
        @Override
        public void onMessage(String userIdStr) {
            mqReceiveCommon.removeMchUser(userIdStr);
        }
    }

    /** 接收 更新系统配置项的消息 **/
    @Service
    @RocketMQMessageListener(topic = CS.MQ.TOPIC_MODIFY_SYS_CONFIG, consumerGroup = CS.MQ.TOPIC_MODIFY_SYS_CONFIG, messageModel = MessageModel.BROADCASTING)
    class receiveInitDbConfig implements RocketMQListener<String> {
        @Override
        public void onMessage(String msg) {
            mqReceiveCommon.initDbConfig(msg);
        }
    }
}
