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
package com.jeequan.jeepay.mgr.mq.topic;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.mgr.mq.service.MqModifyMchInfoService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * RabbitMq
 * 商户信息修改推送
 * @author xiaoyu
 * @site https://www.jeepay.vip
 * @date 2021/6/25 17:10
 */
@Component
@Profile(CS.MQTYPE.RABBIT_MQ)
public class RabbitMqTopic4ModifyMchInfo extends MqModifyMchInfoService {

    @Autowired private RabbitTemplate rabbitTemplate;

    /** 推送消息到各个节点 **/
    @Override
    public void send(String mchNo) {
        rabbitTemplate.convertAndSend(CS.TOPIC_EXCHANGE, CS.MQ.TOPIC_MODIFY_MCH_INFO, mchNo);
    }

}
