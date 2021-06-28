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
package com.jeequan.jeepay.pay.mq.topic;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/*
* 更改ISV信息
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 17:31
*/
@Slf4j
@Component
@Profile(CS.MQTYPE.ACTIVE_MQ)
public class MqTopic4ModifyIsvInfo{

    @Autowired private ConfigContextService configContextService;

    /** 接收 更新系统配置项的消息 **/
    @JmsListener(destination = CS.MQ.TOPIC_MODIFY_ISV_INFO, containerFactory = "jmsListenerContainer")
    public void receive(String isvNo) {

        log.info("重置ISV信息, msg={}", isvNo);
        configContextService.initIsvConfigContext(isvNo);
    }



}
