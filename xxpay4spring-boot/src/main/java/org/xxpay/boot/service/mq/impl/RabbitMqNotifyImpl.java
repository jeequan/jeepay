package org.xxpay.boot.service.mq.impl;

import static org.xxpay.boot.service.mq.MqConfig.DELAY_NOTIFY_EXCHANGE_NAME;
import static org.xxpay.boot.service.mq.MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME;
import static org.xxpay.boot.service.mq.MqConfig.PAY_NOTIFY_QUEUE_NAME;
import static org.xxpay.boot.service.mq.MqConfig.REFUND_NOTIFY_QUEUE_NAME;

import javax.annotation.PostConstruct;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.xxpay.boot.service.mq.IMqNotify;
import org.xxpay.boot.service.mq.Mq4MchRefundNotify;
import org.xxpay.boot.service.mq.Mq4PayNotify;
import org.xxpay.boot.service.mq.Mq4RefundNotify;
import org.xxpay.boot.service.mq.MqConfig;
import org.xxpay.common.util.MyLog;

/**
 * RabbitMq通知实现类
 * 
 * @author https://github.com/cbwleft
 * @date 2018年5月3日
 */
@Component
@Profile(MqConfig.Impl.RABBIT_MQ)
public class RabbitMqNotifyImpl implements IMqNotify {

	private static final MyLog _log = MyLog.getLog(RabbitMqNotifyImpl.class);

	@Autowired
	private AmqpAdmin amqpAdmin;

	@Autowired
	private AmqpTemplate rabbitTemplate;

	@PostConstruct
	public void init() {
		DirectExchange delayNotifyExchange = new DirectExchange(MqConfig.DELAY_NOTIFY_EXCHANGE_NAME);
		delayNotifyExchange.setDelayed(true);
		amqpAdmin.declareExchange(delayNotifyExchange);

		Queue payNotifyQueue = new Queue(PAY_NOTIFY_QUEUE_NAME);
		Binding payNotifyBinding = BindingBuilder.bind(payNotifyQueue).to(delayNotifyExchange).withQueueName();
		amqpAdmin.declareQueue(payNotifyQueue);
		amqpAdmin.declareBinding(payNotifyBinding);

		Queue refundNotifyQueue = new Queue(REFUND_NOTIFY_QUEUE_NAME);
		Binding refundNotifyBinding = BindingBuilder.bind(refundNotifyQueue).to(delayNotifyExchange).withQueueName();
		amqpAdmin.declareQueue(refundNotifyQueue);
		amqpAdmin.declareBinding(refundNotifyBinding);

		Queue mchRefundQueue = new Queue(MCH_REFUND_NOTIFY_QUEUE_NAME);
		Binding mchRefundBinding = BindingBuilder.bind(mchRefundQueue).to(delayNotifyExchange).withQueueName();
		amqpAdmin.declareQueue(mchRefundQueue);
		amqpAdmin.declareBinding(mchRefundBinding);
	}

	@Override
	public void send(String queueName, String msg) {
		_log.info("发送MQ消息:msg={}", msg);
		rabbitTemplate.convertAndSend(queueName, msg);
	}

	@Override
	public void send(String queueName, String msg, int delay) {
		_log.info("发送MQ延时消息:msg={},delay={}", msg, delay);
		rabbitTemplate.convertAndSend(DELAY_NOTIFY_EXCHANGE_NAME, queueName, msg, new MessagePostProcessor() {
			public Message postProcessMessage(Message message) throws AmqpException {
				message.getMessageProperties().setDelay(delay);
				return message;
			}
		});
	}
	
	@Autowired
	private Mq4PayNotify mq4PayNotify;

	@RabbitListener(queues = PAY_NOTIFY_QUEUE_NAME)
	public void onPayMessage(String msg) {
		mq4PayNotify.receive(msg);
	}
	
	@Autowired
	private Mq4RefundNotify mq4RefundNotify;

	@RabbitListener(queues = REFUND_NOTIFY_QUEUE_NAME)
	public void onRefundMessage(String msg) {
		mq4RefundNotify.receive(msg);
	}

	@Autowired
	private Mq4MchRefundNotify mq4MchRefundNotify;
	
	@RabbitListener(queues = MCH_REFUND_NOTIFY_QUEUE_NAME)
	public void onMchRefundMessage(String msg) {
		mq4MchRefundNotify.receive(msg);
	}

}
