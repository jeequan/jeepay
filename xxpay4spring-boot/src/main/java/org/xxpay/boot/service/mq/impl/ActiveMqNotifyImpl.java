package org.xxpay.boot.service.mq.impl;

import static org.xxpay.boot.service.mq.MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME;
import static org.xxpay.boot.service.mq.MqConfig.PAY_NOTIFY_QUEUE_NAME;
import static org.xxpay.boot.service.mq.MqConfig.REFUND_NOTIFY_QUEUE_NAME;

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
import org.xxpay.boot.service.mq.IMqNotify;
import org.xxpay.boot.service.mq.Mq4MchRefundNotify;
import org.xxpay.boot.service.mq.Mq4PayNotify;
import org.xxpay.boot.service.mq.Mq4RefundNotify;
import org.xxpay.boot.service.mq.MqConfig;
import org.xxpay.common.util.MyLog;

/**
 * ActiveMq通知实现类
 * 
 * @author https://github.com/cbwleft
 * @date 2018年5月3日
 */
@Component
@Profile(MqConfig.Impl.ACTIVE_MQ)
public class ActiveMqNotifyImpl implements IMqNotify {

	private static final MyLog _log = MyLog.getLog(ActiveMqNotifyImpl.class);
	
	@Bean
	public Queue payNotifyQueue() {
        return new ActiveMQQueue(PAY_NOTIFY_QUEUE_NAME);
    }
	
	@Bean
	public Queue refundNotifyQueue() {
        return new ActiveMQQueue(REFUND_NOTIFY_QUEUE_NAME);
    }
	
	@Bean
	public Queue mchRefundNotifyQueue() {
        return new ActiveMQQueue(MCH_REFUND_NOTIFY_QUEUE_NAME);
    }
	
	@Autowired
    private JmsTemplate jmsTemplate;

	@Override
	public void send(String queueName, String msg) {
		_log.info("发送MQ消息:msg={}", msg);
        jmsTemplate.convertAndSend(queueName, msg);
	}

	@Override
	public void send(String queueName, String msg, int delay) {
		_log.info("发送MQ延时消息:msg={},delay={}", msg, delay);
        jmsTemplate.send(queueName, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage tm = session.createTextMessage(msg);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1*1000);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
                return tm;
            }
        });
	}
	
	@Autowired
	private Mq4PayNotify mq4PayNotify;

	@JmsListener(destination = PAY_NOTIFY_QUEUE_NAME)
	public void onPayMessage(String msg) {
		mq4PayNotify.receive(msg);
	}
	
	@Autowired
	private Mq4RefundNotify mq4RefundNotify;

	@JmsListener(destination = REFUND_NOTIFY_QUEUE_NAME)
	public void onRefundMessage(String msg) {
		mq4RefundNotify.receive(msg);
	}

	@Autowired
	private Mq4MchRefundNotify mq4MchRefundNotify;
	
	@JmsListener(destination = MCH_REFUND_NOTIFY_QUEUE_NAME)
	public void onMchRefundMessage(String msg) {
		mq4MchRefundNotify.receive(msg);
	}

}
