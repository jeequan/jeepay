package com.jeequan.jeepay.pay.channel.starpos;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarposHttpClientTest {

    private static final URI FIXED_BASE_URI = URI.create("https://yyfsvxm.postar.cn");

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
    void shouldPropagateConnectionTimeoutFromPost() {
        HttpClient timeoutClient = new DelegatingHttpClient(StarposHttpClient.newHttpClient()) {
            @Override
            public <T> HttpResponse<T> send(
                    HttpRequest request,
                    HttpResponse.BodyHandler<T> responseBodyHandler
            ) throws IOException {
                throw new HttpConnectTimeoutException("本地连接超时 fixture");
            }
        };
        StarposHttpClient client = new StarposHttpClient(
                FIXED_BASE_URI,
                timeoutClient,
                Duration.ofSeconds(5)
        );

        HttpConnectTimeoutException exception = assertThrows(
                HttpConnectTimeoutException.class,
                () -> client.post("/gateway/query", new JSONObject(), "LOCAL-ORDER-CONNECT")
        );

        assertEquals("本地连接超时 fixture", exception.getMessage());
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

    @ParameterizedTest
    @ValueSource(strings = {
            "http://127.0.0.1:8080",
            "http://localhost:8080",
            "http://[::1]:8080"
    })
    void shouldRejectNonTlsEndpointThroughInternalConstructor(String baseUrl) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new StarposHttpClient(
                        URI.create(baseUrl),
                        StarposHttpClient.newHttpClient(),
                        Duration.ofSeconds(5)
                )
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
        URI localBaseUri = URI.create(server.url("/").toString());
        HttpClient httpClient = new ForwardingHttpClient(
                StarposHttpClient.newHttpClient(),
                localBaseUri
        );
        return new StarposHttpClient(FIXED_BASE_URI, httpClient, requestTimeout);
    }

    private static class DelegatingHttpClient extends HttpClient {

        private final HttpClient delegate;

        private DelegatingHttpClient(HttpClient delegate) {
            this.delegate = delegate;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return delegate.cookieHandler();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return delegate.connectTimeout();
        }

        @Override
        public Redirect followRedirects() {
            return delegate.followRedirects();
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return delegate.proxy();
        }

        @Override
        public SSLContext sslContext() {
            return delegate.sslContext();
        }

        @Override
        public SSLParameters sslParameters() {
            return delegate.sslParameters();
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return delegate.authenticator();
        }

        @Override
        public Version version() {
            return delegate.version();
        }

        @Override
        public Optional<Executor> executor() {
            return delegate.executor();
        }

        @Override
        public <T> HttpResponse<T> send(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) throws IOException, InterruptedException {
            return delegate.send(request, responseBodyHandler);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) {
            return delegate.sendAsync(request, responseBodyHandler);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            return delegate.sendAsync(request, responseBodyHandler, pushPromiseHandler);
        }
    }

    private static final class ForwardingHttpClient extends DelegatingHttpClient {

        private final URI localBaseUri;

        private ForwardingHttpClient(HttpClient delegate, URI localBaseUri) {
            super(delegate);
            this.localBaseUri = localBaseUri;
        }

        @Override
        public <T> HttpResponse<T> send(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) throws IOException, InterruptedException {
            return super.send(forward(request), responseBodyHandler);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) {
            return super.sendAsync(forward(request), responseBodyHandler);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            return super.sendAsync(forward(request), responseBodyHandler, pushPromiseHandler);
        }

        private HttpRequest forward(HttpRequest request) {
            String pathAndQuery = request.uri().getRawPath();
            if (request.uri().getRawQuery() != null) {
                pathAndQuery += "?" + request.uri().getRawQuery();
            }
            HttpRequest.Builder builder = HttpRequest.newBuilder(localBaseUri.resolve(pathAndQuery));
            request.timeout().ifPresent(builder::timeout);
            request.version().ifPresent(builder::version);
            request.headers().map().forEach(
                    (name, values) -> values.forEach(value -> builder.header(name, value))
            );
            return builder.method(
                    request.method(),
                    request.bodyPublisher().orElse(HttpRequest.BodyPublishers.noBody())
            ).build();
        }
    }
}
