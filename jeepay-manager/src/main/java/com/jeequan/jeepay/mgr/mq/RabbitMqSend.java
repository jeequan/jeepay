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
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author pangxiaoyu
 * @site https://www.jeepay.vip
 * @date 2021-06-07 07:15
 */
@Slf4j
@Component
@Profile(CS.MQTYPE.RABBIT_MQ)
public class RabbitMqSend extends MqCommonService {

    @Autowired private AmqpTemplate rabbitTemplate;

    @Autowired private SysConfigService sysConfigService;

    @Override
    public void send(String msg, String sendType) {
        if (sendType.equals(CS.MQ.MQ_TYPE_PAY_ORDER_MCH_NOTIFY)) {   // 商户订单回调
            payOrderMchNotify(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_ISV_INFO)) {  // 服务商信息修改
            directModifyIsvInfo(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_MCH_APP)) {    // 商户应用修改
            directModifyMchApp(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_MCH_INFO)) {  // 商户信息修改
            directModifyMchInfo(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MODIFY_SYS_CONFIG)) {    // 系统配置修改
            fanoutModifySysConfig(msg);
        }else if (sendType.equals(CS.MQ.MQ_TYPE_MCH_LOGIN_USER_REMOVE)) {    // 商户登录用户清除信息
            directMchLoginUserRemove(msg);
        }
    }

    @Override
    public void send(String msg, long delay, String sendType) {

    }

    /** 发送商户订单回调消息 **/
    public void payOrderMchNotify(String msg) {
        rabbitTemplate.convertAndSend(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY, msg);
    }

    /** 发送服务商信息修改消息 **/
    public void directModifyIsvInfo(String msg) {
        rabbitTemplate.convertAndSend(CS.DIRECT_EXCHANGE, CS.MQ.TOPIC_MODIFY_ISV_INFO, msg);
    }

    /** 发送商户应用修改消息 **/
    public void directModifyMchApp(String msg) {
        rabbitTemplate.convertAndSend(CS.DIRECT_EXCHANGE, CS.MQ.TOPIC_MODIFY_MCH_APP, msg);
    }

    /** 发送商户信息修改消息 **/
    public void directModifyMchInfo(String msg) {
        rabbitTemplate.convertAndSend(CS.DIRECT_EXCHANGE, CS.MQ.TOPIC_MODIFY_MCH_INFO, msg);
    }

    /** 发送系统配置修改消息 **/
    public void fanoutModifySysConfig(String msg) {
        this.rabbitTemplate.convertAndSend(CS.FANOUT_EXCHANGE_SYS_CONFIG, CS.MQ.FANOUT_MODIFY_SYS_CONFIG, msg);
    }

    /** 发送商户登录用户清除信息消息 **/
    public void directMchLoginUserRemove(String msg) {
        this.rabbitTemplate.convertAndSend(CS.DIRECT_EXCHANGE, CS.MQ.QUEUE_MODIFY_MCH_USER_REMOVE, msg);
    }

    /** 接收 更新系统配置项的消息 **/
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(),exchange = @Exchange(name = CS.FANOUT_EXCHANGE_SYS_CONFIG,type = "fanout"))})
    public void receive(String msg) {
        log.info("成功接收更新系统配置的订阅通知, msg={}", msg);
        sysConfigService.initDBConfig(msg);
        log.info("系统配置静态属性已重置");
    }

}
