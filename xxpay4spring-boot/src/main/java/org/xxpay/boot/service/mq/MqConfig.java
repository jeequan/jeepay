package org.xxpay.boot.service.mq;

import javax.jms.Queue;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * @Description:
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@Configuration
public class MqConfig {

	public static final String PAY_NOTIFY_EXCHANGE_NAME = "pay.notify.exchange";
	    
	//public static final String REFUND_NOTIFY_EXCHANGE_NAME = "refund.notify.exchange";
	
    public static final String PAY_NOTIFY_QUEUE_NAME = "pay.notify.queue";
    
    public static final String REFUND_NOTIFY_QUEUE_NAME = "refund.notify.queue";
    
    public static final String MCH_REFUND_NOTIFY_QUEUE_NAME ="mch.refund.notify.queue";
    
	@Bean
	@Profile(MqConfig.Impl.ACTIVE_MQ)
	public Queue refundNotifyQueue() {
        return new ActiveMQQueue(MqConfig.REFUND_NOTIFY_QUEUE_NAME);
    }
	
	@Bean
	@Profile(MqConfig.Impl.ACTIVE_MQ)
	public Queue mchRefundNotifyQueue() {
        return new ActiveMQQueue(MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME);
    }
	
	@Bean
	@Profile(MqConfig.Impl.ACTIVE_MQ)
	public Queue payNotifyQueue() {
        return new ActiveMQQueue(MqConfig.PAY_NOTIFY_QUEUE_NAME);
    }
	
    
    public static class Impl{
    	public static final String ACTIVE_MQ = "activeMQ";
    	public static final String RABBIT_MQ = "rabbitMQ";
    }
    
}
