package com.jeequan.jeepay.components.mq.vender.rocketmq.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JeepayRocketMqClasspathCompatibilityTest {

    @Test
    void shouldProvideLegacyJavaxAnnotationApiForRocketMqStarter() {
        assertDoesNotThrow(() -> Class.forName("javax.annotation.Resource"));
    }
}
