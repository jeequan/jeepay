package com.jeequan.jeepay.components.mq.vender.rocketmq.config;

import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ROCKET_MQ)
public class JeepayRocketMqAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JeepayRocketMqAutoConfiguration.class);

    @Bean
    public SmartInitializingSingleton jeepayRocketMqSanityCheck(Environment environment) {
        return () -> {
            String nameServer = environment.getProperty("rocketmq.name-server");
            String applicationName = environment.getProperty("spring.application.name");
            String producerGroup = environment.getProperty("rocketmq.producer.group");

            Assert.hasText(nameServer,
                    "When isys.mq.vender=rocketMQ, property 'rocketmq.name-server' must be configured.");
            Assert.hasText(producerGroup,
                    "When isys.mq.vender=rocketMQ, property 'rocketmq.producer.group' must be configured.");

            if (!StringUtils.hasText(applicationName)) {
                log.warn("spring.application.name is empty, RocketMQ consumer groups will use fallback naming.");
                return;
            }

            log.info("RocketMQ integration enabled. applicationName={}, nameServer={}, producerGroup={}",
                    applicationName, nameServer, producerGroup);
        };
    }
}
