package com.jeequan.jeepay.pay.channel.starpos;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarposHttpClientTest {

    @Test
    void shouldPostUtf8JsonAndParseSuccessfulResponse() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(200, "{\"code\":\"00\",\"message\":\"成功\"}");
            StarposHttpClient client = localClient(server, Duration.ofSeconds(5));

            JSONObject response = client.post(
                    "/gateway/pay",
                    JSONObject.parseObject("{\"orderNo\":\"本地订单\",\"amount\":100}"),
                    "LOCAL-ORDER-1001"
            );

            assertEquals("00", response.getString("code"));
            assertEquals("成功", response.getString("message"));
            StarposTestServer.RecordedRequest request = server.takeRequest();
            assertEquals("/gateway/pay", request.getPath());
            assertEquals(
                    "application/json; charset=UTF-8",
                    request.getFirstHeader("Content-Type")
            );
            assertEquals(
                    "{\"amount\":100,\"orderNo\":\"本地订单\"}",
                    request.getJsonBody()
            );
        }
    }

    @Test
    void shouldConfigureFiveSecondConnectionTimeout() {
        assertEquals(
                Duration.ofSeconds(5),
                StarposHttpClient.newHttpClient().connectTimeout().orElseThrow()
        );
    }

    @Test
    void shouldApplyRequestTimeout() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(200, "{\"code\":\"00\"}", 300L);
            StarposHttpClient client = localClient(server, Duration.ofMillis(50));

            assertThrows(
                    HttpTimeoutException.class,
                    () -> client.post("/gateway/query", new JSONObject(), "LOCAL-ORDER-1002")
            );
        }
    }

    @Test
    void shouldRejectNon2xxResponse() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(503, "{\"code\":\"SYSTEM_BUSY\"}");
            StarposHttpClient client = localClient(server, Duration.ofSeconds(5));

            IOException exception = assertThrows(
                    IOException.class,
                    () -> client.post("/gateway/pay", new JSONObject(), "LOCAL-ORDER-1003")
            );

            assertEquals("星驿付响应状态异常", exception.getMessage());
        }
    }

    @Test
    void shouldRejectInvalidJsonResponse() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(200, "{invalid-json");
            StarposHttpClient client = localClient(server, Duration.ofSeconds(5));

            IOException exception = assertThrows(
                    IOException.class,
                    () -> client.post("/gateway/query", new JSONObject(), "LOCAL-ORDER-1004")
            );

            assertEquals("星驿付响应不是合法 JSON 对象", exception.getMessage());
        }
    }

    @Test
    void shouldRejectResponseLargerThanOneMib() throws Exception {
        String oversizedResponse = "{\"data\":\"" + "x".repeat(1024 * 1024) + "\"}";
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(200, oversizedResponse);
            StarposHttpClient client = localClient(server, Duration.ofSeconds(5));

            IOException exception = assertThrows(
                    IOException.class,
                    () -> client.post("/gateway/query", new JSONObject(), "LOCAL-ORDER-1005")
            );

            assertEquals("星驿付响应超过 1 MiB", exception.getMessage());
        }
    }

    @Test
    void shouldRejectConfiguredHostOutsideFixedStarposHosts() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new StarposHttpClient("https://starpos.example.com")
        );

        assertEquals("不允许的星驿付服务主机: starpos.example.com", exception.getMessage());
    }

    @Test
    void shouldRejectNonHttpsConfiguredEndpoint() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new StarposHttpClient("http://yyfsvxm.postar.cn")
        );

        assertEquals("星驿付服务地址必须使用 HTTPS", exception.getMessage());
    }

    @Test
    void shouldAcceptFixedStarposEndpoint() {
        new StarposHttpClient("https://yyfsvxm.postar.cn");
    }

    @Test
    void shouldLogOnlyPathElapsedBusinessCodeAndMaskedOrderNo() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(StarposHttpClient.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(200, "{\"code\":\"00\",\"secret\":\"RESPONSE_SECRET\"}");
            StarposHttpClient client = localClient(server, Duration.ofSeconds(5));

            client.post(
                    "/gateway/pay?token=QUERY_SECRET",
                    JSONObject.parseObject("{\"secret\":\"REQUEST_SECRET\"}"),
                    "LOCAL-ORDER-12345678"
            );

            String message = appender.list.get(0).getFormattedMessage();
            assertTrue(message.contains("path=/gateway/pay"));
            assertTrue(message.contains("elapsedMs="));
            assertTrue(message.contains("businessCode=00"));
            assertTrue(message.contains("orderNo=LOCA************5678"));
            assertFalse(message.contains("127.0.0.1"));
            assertFalse(message.contains("QUERY_SECRET"));
            assertFalse(message.contains("REQUEST_SECRET"));
            assertFalse(message.contains("RESPONSE_SECRET"));
            assertFalse(message.contains("LOCAL-ORDER-12345678"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private static StarposHttpClient localClient(
            StarposTestServer server,
            Duration requestTimeout
    ) {
        URI baseUri = URI.create(server.url("/").toString());
        HttpClient httpClient = StarposHttpClient.newHttpClient();
        return new StarposHttpClient(baseUri, httpClient, requestTimeout);
    }
}
