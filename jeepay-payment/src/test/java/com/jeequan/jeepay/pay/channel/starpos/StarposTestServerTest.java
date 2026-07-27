package com.jeequan.jeepay.pay.channel.starpos;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarposTestServerTest {

    @Test
    void shouldBeDiscoveredBySurefire() {
    }

    @Test
    void shouldRecordRequestPathJsonBodyAndHeaders() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(201, "{\"code\":\"00\"}");

            HttpURLConnection connection = postJson(server.url("/gateway/pay?merchantNo=M100"), "{\"amount\":100}",
                    "X-Starpos-Sign", "SIGNATURE");

            assertEquals(201, connection.getResponseCode());
            assertEquals("{\"code\":\"00\"}", readBody(connection));

            StarposTestServer.RecordedRequest request = server.takeRequest();
            assertEquals("/gateway/pay?merchantNo=M100", request.getPath());
            assertEquals("{\"amount\":100}", request.getJsonBody());
            assertEquals("SIGNATURE", request.getFirstHeader("X-Starpos-Sign"));
            assertTrue(request.getHeaders().containsKey("Content-Type"));
        }
    }

    @Test
    void shouldReturnConfiguredDelayAndLargeJsonResponse() throws Exception {
        String largeResponse = "{\"data\":\"" + String.join("", Collections.nCopies(8192, "x")) + "\"}";

        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(202, largeResponse, 25L);

            long startedAt = System.currentTimeMillis();
            HttpURLConnection connection = postJson(server.url("/gateway/query"), "{\"orderNo\":\"P100\"}");

            assertEquals(202, connection.getResponseCode());
            assertEquals(largeResponse, readBody(connection));
            assertTrue(System.currentTimeMillis() - startedAt >= 20L);
            assertFalse(server.getRequests().isEmpty());
        }
    }

    private HttpURLConnection postJson(URL url, String body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        return postJson(connection, body);
    }

    private HttpURLConnection postJson(URL url, String body, String headerName, String headerValue) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setRequestProperty(headerName, headerValue);
        return postJson(connection, body);
    }

    private HttpURLConnection postJson(HttpURLConnection connection, String body) throws Exception {
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return connection;
    }

    private String readBody(HttpURLConnection connection) throws Exception {
        InputStream inputStream = connection.getResponseCode() >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
        try (InputStream body = inputStream) {
            return new String(body.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
