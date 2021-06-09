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
package com.jeequan.jeepay.pay.mq.queue;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.pay.mq.config.MqThreadExecutor;
import com.jeequan.jeepay.service.impl.MchNotifyRecordService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.jms.Queue;
import javax.jms.TextMessage;

/*
* 商户订单回调MQ通知
* 
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 17:34
*/
@Slf4j
@Component
public class MqQueue4PayOrderMchNotify {

    @Bean("mqQueue4PayOrderMchNotifyInner")
    public Queue mqQueue4PayOrderMchNotifyInner(){
        return new ActiveMQQueue(CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY);
    }

    @Lazy
    @Autowired
    @Qualifier("mqQueue4PayOrderMchNotifyInner")
    private Queue mqQueue4PayOrderMchNotifyInner;
    @Autowired private JmsTemplate jmsTemplate;
    @Autowired private MchNotifyRecordService mchNotifyRecordService;
    @Autowired private PayOrderService payOrderService;

    public MqQueue4PayOrderMchNotify(){
        super();
    }

    /** 发送MQ消息 **/
    public void send(String msg) {
        this.jmsTemplate.convertAndSend(mqQueue4PayOrderMchNotifyInner, msg);
    }

    /** 发送MQ消息 **/
    public void send(String msg, long delay) {
        jmsTemplate.send(mqQueue4PayOrderMchNotifyInner, session -> {
            TextMessage tm = session.createTextMessage(msg);
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1*1000);
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
            return tm;
        });
    }

    /** 接收 更新系统配置项的消息 **/
    @Async(MqThreadExecutor.EXECUTOR_PAYORDER_MCH_NOTIFY)
    @JmsListener(destination = CS.MQ.QUEUE_PAYORDER_MCH_NOTIFY)
    public void receive(String msg) {

        log.info("接收商户通知MQ, msg={}", msg);

        Long notifyId = Long.parseLong(msg);

        MchNotifyRecord record = mchNotifyRecordService.getById(notifyId);

        if(record == null || record.getState() != MchNotifyRecord.STATE_ING){
            log.info("查询通知记录不存在或状态不是通知中");
            return ;
        }
        if( record.getNotifyCount() >= 6 ){
            log.info("已达到最大发送次数");
            return ;
        }

        //1. (发送结果最多6次)
        Integer currentCount = record.getNotifyCount() + 1;

        String notifyUrl = record.getNotifyUrl();
        String res = "";
        try {
            res = HttpUtil.createPost(notifyUrl).timeout(20000).execute().body();
        } catch (HttpException e) {
            log.error("http error", e);
        }

        if(currentCount == 1){ //第一次通知: 更新为已通知
            payOrderService.updateNotifySent(record.getOrderId());
        }

        //通知成功
        if("SUCCESS".equalsIgnoreCase(res)){
            mchNotifyRecordService.updateNotifyResult(notifyId, MchNotifyRecord.STATE_SUCCESS, res);
            return ;
        }

        //响应结果为异常
        if( currentCount >= 6 ){
            mchNotifyRecordService.updateNotifyResult(notifyId, MchNotifyRecord.STATE_FAIL, res);
            return ;
        }

        // 继续发送MQ 延迟发送
        mchNotifyRecordService.updateNotifyResult(notifyId, MchNotifyRecord.STATE_ING, res);

        // 通知延时次数
//        1   2  3  4   5   6
//        0  30 60 90 120 150
        send(msg, currentCount * 30 * 1000);
    }


}
