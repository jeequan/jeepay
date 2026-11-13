package com.jeequan.jeepay.core.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HexFormat;
import static org.junit.jupiter.api.Assertions.*;

class JeepayKitLoggingTest {
    @Test
    void signingPreservesDigestWithoutEmittingSensitiveDiagnostics() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(JeepayKit.class);
        Level previous = logger.getLevel();
        boolean additive = logger.isAdditive();
        ListAppender<ILoggingEvent> capture = new ListAppender<>();
        capture.start();
        logger.addAppender(capture);
        logger.setAdditive(false);
        logger.setLevel(Level.TRACE);
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("z", "synthetic-payload-81");
            params.put("A", 100);
            params.put("empty", "");
            params.put("absent", null);
            Map<String, Object> before = new LinkedHashMap<>(params);
            String key = "synthetic-signing-key-92";
            String expected = HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(
                    ("A=100&z=synthetic-payload-81&key=" + key).getBytes(StandardCharsets.UTF_8))).toUpperCase(java.util.Locale.ROOT);
            assertEquals(expected, JeepayKit.getSign(params, key));
            assertEquals(before, params);
            assertTrue(capture.list.isEmpty(), "Signing must not emit request, credential, or signature diagnostics");
        } finally {
            logger.detachAppender(capture);
            logger.setLevel(previous);
            logger.setAdditive(additive);
            capture.stop();
        }
    }
}
