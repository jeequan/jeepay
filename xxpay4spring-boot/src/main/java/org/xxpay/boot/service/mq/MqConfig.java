package org.xxpay.boot.service.mq;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.jms.Queue;

/**
 * @Description:
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@Configuration
public class MqConfig {

    public static final String PAY_NOTIFY_QUEUE_NAME = "pay.notify.queue";
    
    public static class Impl{
    	public static final String ACTIVE_MQ = "activeMQ";
    	public static final String RABBIT_MQ = "rabbitMQ";
    }
    
}
