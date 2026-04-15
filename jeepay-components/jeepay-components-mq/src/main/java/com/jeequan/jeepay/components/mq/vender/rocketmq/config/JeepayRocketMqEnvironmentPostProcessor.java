package com.jeequan.jeepay.components.mq.vender.rocketmq.config;

import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Populate production-safe RocketMQ defaults before RocketMQAutoConfiguration runs.
 */
public class JeepayRocketMqEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "jeepayRocketMqDefaults";
    private static final String APPLICATION_NAME_KEY = "spring.application.name";
    private static final String PRODUCER_GROUP_KEY = "rocketmq.producer.group";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!MQVenderCS.ROCKET_MQ.equals(environment.getProperty(MQVenderCS.YML_VENDER_KEY))) {
            return;
        }

        Map<String, Object> defaults = new LinkedHashMap<>();
        String applicationName = environment.getProperty(APPLICATION_NAME_KEY);

        if (!StringUtils.hasText(applicationName)) {
            applicationName = "jeepay";
            defaults.put(APPLICATION_NAME_KEY, applicationName);
        }

        if (!StringUtils.hasText(environment.getProperty(PRODUCER_GROUP_KEY))) {
            defaults.put(PRODUCER_GROUP_KEY, buildProducerGroup(applicationName));
        }

        defaults.putIfAbsent("rocketmq.producer.send-message-timeout", "10000");
        defaults.putIfAbsent("rocketmq.producer.retry-times-when-send-failed", "2");
        defaults.putIfAbsent("rocketmq.producer.retry-times-when-send-async-failed", "2");

        if (!defaults.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
        }
    }

    static String buildProducerGroup(String applicationName) {
        String normalized = applicationName.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
        return "PID_JEEPAY_" + normalized;
    }

    @Override
    public int getOrder() {
        // 必须在 ConfigDataEnvironmentPostProcessor（HIGHEST_PRECEDENCE + 10）之后执行，
        // 否则 application.yml 尚未加载，isys.mq.vender 始终为 null，默认值不会生效
        return Ordered.LOWEST_PRECEDENCE;
    }
}
