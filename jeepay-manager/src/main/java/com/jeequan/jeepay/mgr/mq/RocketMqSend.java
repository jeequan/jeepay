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
package com.jeequan.jeepay.mgr.mq;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.mq.MqCommonService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @author pangxiaoyu
 * @site https://www.jeepay.vip
 * @date 2021-06-07 07:15
 */
@Slf4j
@Component
@Profile(CS.MQTYPE.ROCKET_MQ)
public class RocketMqSend extends MqCommonService {

    @Autowired private RocketMQTemplate rocketMQTemplate;

    @Autowired private SysConfigService sysConfigService;

    @Override
    public void send(String msg, String sendType) {
        if (sendType.equals(CS.MQ.MQ_TYPE_PAY_ORDER_MCH_NOTIFY)) {   // 商户订单回调
            payOrderMchNotify(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_ISV_INFO)) {  // 服务商信息修改
            modifyIsvInfo(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_MCH_APP)) {    // 商户应用修改
            modifyMchApp(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_MCH_INFO)) {  // 商户信息修改
            modifyMchInfo(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_SYS_CONFIG)) {    // 系统配置修改
            modifySysConfig(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MCH_LOGIN_USER_REMOVE)) {    // 商户登录用户清除信息
            mchLoginUserRemove(msg);
        }
    }

    @Override
    public void send(String msg, long delay, String sendType) {

    }

    /** 发送商户订单回调消息 **/
    public void payOrderMchNotify(String msg) {
        sendMsg(msg, CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY);
    }

    /** 发送服务商信息修改消息 **/
    public void modifyIsvInfo(String msg) {
        sendMsg(msg, CS.MQ.TOPIC_MODIFY_ISV_INFO);
    }

    /** 发送商户应用修改消息 **/
    public void modifyMchApp(String msg) {
        sendMsg(msg, CS.MQ.TOPIC_MODIFY_MCH_APP);
    }

    /** 发送商户信息修改消息 **/
    public void modifyMchInfo(String msg) {
        sendMsg(msg, CS.MQ.TOPIC_MODIFY_MCH_INFO);
    }

    /** 发送系统配置修改消息 **/
    public void modifySysConfig(String msg) {
        sendMsg(msg, CS.MQ.TOPIC_MODIFY_SYS_CONFIG);
    }

    /** 发送商户登录用户清除信息消息 **/
    public void mchLoginUserRemove(String msg) {
        sendMsg(msg, CS.MQ.QUEUE_MODIFY_MCH_USER_REMOVE);
    }

    public void sendMsg(String msg, String group) {
        // 这里的分组和消息名称未做区分
        rocketMQTemplate.getProducer().setProducerGroup(group);
        this.rocketMQTemplate.convertAndSend(group, msg);
    }

    /** 接收 更新系统配置项的消息 **/
    @Service
    @RocketMQMessageListener(topic = CS.MQ.TOPIC_MODIFY_SYS_CONFIG, consumerGroup = CS.MQ.TOPIC_MODIFY_SYS_CONFIG, messageModel = MessageModel.BROADCASTING)
    class RocketMqReceive implements RocketMQListener<String> {
        @Override
        public void onMessage(String msg) {
            log.info("成功接收更新系统配置的订阅通知, msg={}", msg);
            sysConfigService.initDBConfig(msg);
            log.info("系统配置静态属性已重置");
        }
    }

}
