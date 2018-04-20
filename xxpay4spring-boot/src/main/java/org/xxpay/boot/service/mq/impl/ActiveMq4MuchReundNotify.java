package org.xxpay.boot.service.mq.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import org.xxpay.boot.service.mq.Mq4MchRefundNotify;
import org.xxpay.boot.service.mq.MqConfig;

@Component
@Profile(MqConfig.Impl.ACTIVE_MQ)
public class ActiveMq4MuchReundNotify extends Mq4MchRefundNotify{
	
	
	@Autowired
    private Queue mchRefundNotifyQueue;

    @Autowired
    private JmsTemplate jmsTemplate;
    

	@Override
	public void send(String msg) {
		_log.info("发送MQ消息:msg={}", msg);
        jmsTemplate.convertAndSend(mchRefundNotifyQueue, msg);
	}

	@Override
	public void send(String msg, long delay) {
		_log.info("发送MQ延时消息:msg={},delay={}", msg, delay);
        jmsTemplate.send(this.mchRefundNotifyQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage tm = session.createTextMessage(msg);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1*1000);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
                return tm;
            }
        });
	}
	
	@JmsListener(destination = MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME)
	public void onMessage(String msg) {
		receive(msg);
	}

}
