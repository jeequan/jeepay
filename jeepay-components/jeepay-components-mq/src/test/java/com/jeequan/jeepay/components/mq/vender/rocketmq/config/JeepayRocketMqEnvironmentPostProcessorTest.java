package com.jeequan.jeepay.components.mq.vender.rocketmq.config;

import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JeepayRocketMqEnvironmentPostProcessorTest {

    private final JeepayRocketMqEnvironmentPostProcessor processor = new JeepayRocketMqEnvironmentPostProcessor();

    @Test
    void shouldPopulateRocketMqDefaultsWhenVendorMatches() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                MQVenderCS.YML_VENDER_KEY, MQVenderCS.ROCKET_MQ,
                "spring.application.name", "jeepay-payment"
        )));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals("PID_JEEPAY_JEEPAY_PAYMENT", environment.getProperty("rocketmq.producer.group"));
        assertEquals("10000", environment.getProperty("rocketmq.producer.send-message-timeout"));
        assertEquals("2", environment.getProperty("rocketmq.producer.retry-times-when-send-failed"));
        assertEquals("2", environment.getProperty("rocketmq.producer.retry-times-when-send-async-failed"));
    }

    @Test
    void shouldNotOverrideExplicitlyConfiguredProducerGroup() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                MQVenderCS.YML_VENDER_KEY, MQVenderCS.ROCKET_MQ,
                "spring.application.name", "jeepay-payment",
                "rocketmq.producer.group", "MY_CUSTOM_GROUP"
        )));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals("MY_CUSTOM_GROUP", environment.getProperty("rocketmq.producer.group"));
    }

    @Test
    void shouldLeaveEnvironmentUntouchedWhenVendorDoesNotMatch() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                MQVenderCS.YML_VENDER_KEY, MQVenderCS.ACTIVE_MQ,
                "spring.application.name", "jeepay-payment"
        )));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertNull(environment.getProperty("rocketmq.producer.group"));
    }
}
