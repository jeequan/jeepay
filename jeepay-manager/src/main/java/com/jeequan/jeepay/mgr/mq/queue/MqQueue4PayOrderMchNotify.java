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

import com.jeequan.jeepay.core.constants.CS;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
* 商户订单回调MQ通知
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/21 18:03
*/
@Slf4j
@Component
public class MqQueue4PayOrderMchNotify extends ActiveMQQueue{

    @Autowired private JmsTemplate jmsTemplate;

    public MqQueue4PayOrderMchNotify(){
        super(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY);
    }

    /** 发送MQ消息 **/
    public void send(Long notifyId) {
        this.jmsTemplate.convertAndSend(this, notifyId + "");
    }

}
