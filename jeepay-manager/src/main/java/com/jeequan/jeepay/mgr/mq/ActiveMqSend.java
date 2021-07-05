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
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Queue;

/**
 *
 * @author pangxiaoyu
 * @site https://www.jeepay.vip
 * @date 2021-06-07 07:15
 */
@Slf4j
@Component
@Profile(CS.MQTYPE.ACTIVE_MQ)
public class ActiveMqSend extends MqCommonService {

    @Autowired private JmsTemplate jmsTemplate;

    @Autowired private SysConfigService sysConfigService;


    @Bean("activeMqSendModifyMchUserRemove")
    public Queue mqQueue4ModifyMchUserRemove(){
        return new ActiveMQQueue(CS.MQ.QUEUE_MODIFY_MCH_USER_REMOVE);
    }

    @Lazy
    @Autowired
    @Qualifier("activeMqSendModifyMchUserRemove")
    private Queue mqQueue4ModifyMchUserRemove;

    @Bean("activeMqSendPayOrderMchNotify")
    public Queue mqQueue4PayOrderMchNotify(){
        return new ActiveMQQueue(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY);
    }

    @Lazy
    @Autowired
    @Qualifier("activeMqSendPayOrderMchNotify")
    private Queue mqQueue4PayOrderMchNotify;

    @Bean("activeMqSendModifyIsvInfo")
    public ActiveMQTopic mqTopic4ModifyIsvInfo(){
        return new ActiveMQTopic(CS.MQ.TOPIC_MODIFY_ISV_INFO);
    }

    @Lazy
    @Autowired
    @Qualifier("activeMqSendModifyIsvInfo")
    private ActiveMQTopic mqTopic4ModifyIsvInfo;

    @Bean("activeMqSendModifyMchApp")
    public ActiveMQTopic mqTopic4ModifyMchApp(){
        return new ActiveMQTopic(CS.MQ.TOPIC_MODIFY_MCH_APP);
    }

    @Lazy
    @Autowired
    @Qualifier("activeMqSendModifyMchApp")
    private ActiveMQTopic mqTopic4ModifyMchApp;


    @Bean("activeMqSendModifyMchInfo")
    public ActiveMQTopic mqTopic4ModifyMchInfo(){
        return new ActiveMQTopic(CS.MQ.TOPIC_MODIFY_MCH_INFO);
    }

    @Lazy
    @Autowired
    @Qualifier("activeMqSendModifyMchInfo")
    private ActiveMQTopic mqTopic4ModifyMchInfo;

    @Bean("activeMqSendModifySysConfig")
    public ActiveMQTopic mqTopic4ModifySysConfig(){
        return new ActiveMQTopic(CS.MQ.TOPIC_MODIFY_SYS_CONFIG);
    }

    @Lazy
    @Autowired
    @Qualifier("activeMqSendModifySysConfig")
    private ActiveMQTopic mqTopic4ModifySysConfig;

    @Override
    public void send(String msg, String sendType) {
        if (sendType.equals(CS.MQ.MQ_TYPE_PAY_ORDER_MCH_NOTIFY)) {   // 商户订单回调
            queuePayOrderMchNotify(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_ISV_INFO)) {  // 服务商信息修改
            topicModifyIsvInfo(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_MCH_APP)) {   // 商户应用修改
            topicModifyMchApp(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_MCH_INFO)) {  // 商户信息修改
            topicModifyMchInfo(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_SYS_CONFIG)) {    // 系统配置修改
            topicModifySysConfig(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MCH_LOGIN_USER_REMOVE)) {    // 商户登录用户清除信息
            queueMchLoginUserRemove(msg);
        }
    }

    @Override
    public void send(String msg, long delay, String sendType) {

    }

    /** 发送商户订单回调消息 **/
    public void queuePayOrderMchNotify(String msg) {
        this.jmsTemplate.convertAndSend(mqQueue4PayOrderMchNotify, msg);
    }

    /** 发送服务商信息修改消息 **/
    public void topicModifyIsvInfo(String msg) {
        this.jmsTemplate.convertAndSend(mqTopic4ModifyIsvInfo, msg);
    }

    /** 发送商户应用修改消息 **/
    public void topicModifyMchApp(String msg) {
        this.jmsTemplate.convertAndSend(mqTopic4ModifyMchApp, msg);
    }

    /** 发送商户信息修改消息 **/
    public void topicModifyMchInfo(String msg) {
        this.jmsTemplate.convertAndSend(mqTopic4ModifyMchInfo, msg);
    }

    /** 发送系统配置修改消息 **/
    public void topicModifySysConfig(String msg) {
        this.jmsTemplate.convertAndSend(mqTopic4ModifySysConfig, msg);
    }

    /** 发送商户登录用户清除信息消息 **/
    public void queueMchLoginUserRemove(String msg) { this.jmsTemplate.convertAndSend(mqQueue4ModifyMchUserRemove, msg); }

    /** 接收 更新系统配置项的消息 **/
    @JmsListener(destination = CS.MQ.TOPIC_MODIFY_SYS_CONFIG, containerFactory = "jmsListenerContainer")
    public void receive(String msg) {

        log.info("成功接收更新系统配置的订阅通知, msg={}", msg);
        sysConfigService.initDBConfig(msg);
        log.info("系统配置静态属性已重置");
    }

}
