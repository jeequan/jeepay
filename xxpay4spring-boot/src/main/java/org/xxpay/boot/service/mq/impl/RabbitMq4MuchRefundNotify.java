package org.xxpay.boot.service.mq.impl;

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
import org.xxpay.boot.service.mq.Mq4MchRefundNotify;
import org.xxpay.boot.service.mq.MqConfig;

@Component
@Profile(MqConfig.Impl.RABBIT_MQ)
public class RabbitMq4MuchRefundNotify extends Mq4MchRefundNotify {

	@Autowired
	private AmqpAdmin amqpAdmin;

	@PostConstruct
	public void init() {
		DirectExchange exchange = new DirectExchange(MqConfig.PAY_NOTIFY_EXCHANGE_NAME);
		exchange.setDelayed(true);
		Queue queue = new Queue(MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME);
		Binding binding = BindingBuilder.bind(queue).to(exchange).withQueueName();
		amqpAdmin.declareExchange(exchange);
		amqpAdmin.declareQueue(queue);
		amqpAdmin.declareBinding(binding);
	}

	@Autowired
	private AmqpTemplate rabbitTemplate;

	@Override
	public void send(String msg) {
		_log.info("发送MQ消息:msg={}", msg);
		rabbitTemplate.convertAndSend(MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME, msg);
	}

	@Override
	public void send(String msg, long delay) {
		_log.info("发送MQ延时消息:msg={},delay={}", msg, delay);
		rabbitTemplate.convertAndSend(MqConfig.PAY_NOTIFY_EXCHANGE_NAME, MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME, msg, new MessagePostProcessor() {
			public Message postProcessMessage(Message message) throws AmqpException {
				message.getMessageProperties().setDelay((int) delay);
				return message;
			}
		});
	}

	@RabbitListener(queues = MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME)
	public void onMessage(String msg) {
		receive(msg);
	}

}
