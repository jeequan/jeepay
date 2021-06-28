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

import com.alibaba.fastjson.JSONArray;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.mgr.mq.service.MqMchUserRemoveService;
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
import java.util.Collection;

/**
 * 商户用户信息清除
 *
 * @author pangxiaoyu
 * @site https://www.jeepay.vip
 * @date 2021-06-07 07:15
 */
@Slf4j
@Component
@Profile(CS.MQTYPE.ACTIVE_MQ)
public class MqQueue4ModifyMchUserRemove extends MqMchUserRemoveService {

    @Autowired private JmsTemplate jmsTemplate;

    @Bean("modifyMchUserRemove")
    public Queue mqQueue4ModifyMchUserRemove(){
        return new ActiveMQQueue(CS.MQ.QUEUE_MODIFY_MCH_USER_REMOVE);
    }

    @Lazy
    @Autowired
    @Qualifier("modifyMchUserRemove")
    private Queue mqQueue4ModifyMchUserRemove;

    @Override
    public void send(Collection<Long> userIdList) {
        if(userIdList == null || userIdList.isEmpty()){
            return ;
        }
        this.jmsTemplate.convertAndSend(mqQueue4ModifyMchUserRemove, JSONArray.toJSONString(userIdList));
    }

}
