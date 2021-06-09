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
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/*
* 更改系统配置参数
* 
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 17:13
*/
@Slf4j
@Component
public class MqTopic4ModifySysConfig extends ActiveMQTopic{

    @Autowired private JmsTemplate jmsTemplate;
    @Autowired private SysConfigService sysConfigService;

    public MqTopic4ModifySysConfig(){
        super(CS.MQ.TOPIC_MODIFY_SYS_CONFIG);
    }

    /** 接收 更新系统配置项的消息 **/
    @JmsListener(destination = CS.MQ.TOPIC_MODIFY_SYS_CONFIG, containerFactory = "jmsListenerContainer")
    public void receive(String msg) {

        log.info("成功接收更新系统配置的订阅通知, msg={}", msg);
        sysConfigService.initDBConfig(msg);
        log.info("系统配置静态属性已重置");
    }

    /** 推送消息到各个节点 **/
    public void push(String msg) {
        this.jmsTemplate.convertAndSend(this, msg);
    }


}
