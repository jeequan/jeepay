package com.jeequan.jeepay.pay.channel.starpos;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.starpos.StarposNormalMchParams;
import com.jeequan.jeepay.pay.channel.starpos.payway.StarposQr;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.StarposQrOrderRS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.crypto.Cipher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarposPaymentServiceTest {

    private static final URI FIXED_BASE_URI = URI.create("https://xyf-server-test.postar.cn");
    private static final KeyPair KEY_PAIR = generateKeyPair();
    private static final String PUBLIC_KEY = Base64.getEncoder()
            .encodeToString(KEY_PAIR.getPublic().getEncoded());

    @Test
    void shouldNotUseNestedPaywayLookupForConcreteQrService() {
        assertNull(new StarposQr().preCheck(new UnifiedOrderRQ(), payOrder(
                "ORDER-PRECHECK", 100L, "标题", "描述"
        )));
    }

    @Test
    void shouldMapPaymentFieldsSignRequestAndOmitDynamicAsyncNotify() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(
                    200,
                    "{\"code\":\"000000\",\"message\":\"成功\","
                            + "\"data\":\"https://xyf-server-test.postar.cn/cashier/ORDER-1001\"}"
            );

            PayOrder payOrder = payOrder("ORDER-1001", 12345L, "商品标题", "这是商品描述");
            StarposQrOrderRS response = pay(service(server, Duration.ofSeconds(5)), payOrder);

            assertEquals(CS.PAY_DATA_TYPE.PAY_URL, response.buildPayDataType());
            assertEquals(
                    "https://xyf-server-test.postar.cn/cashier/ORDER-1001",
                    response.buildPayData()
            );
            assertEquals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS,
                    response.getChannelRetMsg().getChannelState());

            StarposTestServer.RecordedRequest request = server.takeRequest();
            assertEquals("/yyfsevr/order/getCodeUrl", request.getPath());
            JSONObject body = JSONObject.parseObject(request.getJsonBody());
            assertEquals("AGENT-1001", body.getString("agetId"));
            assertEquals("CUST-1001", body.getString("custId"));
            assertEquals("1.0.0", body.getString("version"));
            assertEquals("ORDER-1001", body.getString("orderNo"));
            assertEquals(12345L, body.getLongValue("txamt"));
            assertEquals("商品标题", body.getString("title"));
            assertEquals("这是商品描述", body.getString("remark"));
            assertFalse(body.containsKey("asyncNotify"));
            assertTrue(!body.getString("timeStamp").isBlank());
            assertTrue(!body.getString("sign").isBlank());

            assertEquals(
                    StarposSigner.sha256Hex(StarposSigner.canonicalJson(withoutSign(body))),
                    decryptRequestHash(body.getString("sign"), KEY_PAIR.getPrivate())
            );
        }
    }

    @Test
    void shouldLimitRemarkLengthWithoutChangingOrderMapping() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(
                    200,
                    "{\"code\":\"000000\",\"data\":\"https://xyf-server-test.postar.cn/cashier/ORDER-1002\"}"
            );

            String longBody = "描述".repeat(300);
            PayOrder payOrder = payOrder("ORDER-1002", 1L, "标题", longBody);
            StarposQrOrderRS response = pay(service(server, Duration.ofSeconds(5)), payOrder);

            assertEquals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS,
                    response.getChannelRetMsg().getChannelState());
            JSONObject body = JSONObject.parseObject(server.takeRequest().getJsonBody());
            assertEquals("ORDER-1002", body.getString("orderNo"));
            assertEquals(1L, body.getLongValue("txamt"));
            assertEquals("标题", body.getString("title"));
            assertTrue(body.getString("remark").length() <= 128);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"222222", "555555", "-35200"})
    void shouldMapKnownBusinessErrorsToConfirmedFailure(String code) throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(200, "{\"code\":\"" + code + "\",\"message\":\"上游失败\"}");

            StarposQrOrderRS response = pay(service(server, Duration.ofSeconds(5)),
                    payOrder("ORDER-" + code, 100L, "标题", "描述"));

            ChannelRetMsg channelRetMsg = response.getChannelRetMsg();
            assertEquals(ChannelRetMsg.ChannelState.CONFIRM_FAIL, channelRetMsg.getChannelState());
            assertEquals(code, channelRetMsg.getChannelErrCode());
            assertEquals("上游失败", channelRetMsg.getChannelErrMsg());
        }
    }

    @Test
    void shouldKeepOrderQueryableWhenUpstreamReturnsUnknownCode() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(200, "{\"code\":\"-80000\",\"message\":\"结果未知\"}");

            StarposQrOrderRS response = pay(service(server, Duration.ofSeconds(5)),
                    payOrder("ORDER-UNKNOWN", 100L, "标题", "描述"));

            ChannelRetMsg channelRetMsg = response.getChannelRetMsg();
            assertEquals(ChannelRetMsg.ChannelState.UNKNOWN, channelRetMsg.getChannelState());
            assertEquals("-80000", channelRetMsg.getChannelErrCode());
            assertTrue(channelRetMsg.isNeedQuery());
        }
    }

    @Test
    void shouldKeepOrderQueryableAfterRequestTimeout() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(200, "{\"code\":\"000000\"}", 300L);

            StarposQrOrderRS response = pay(service(server, Duration.ofMillis(50)),
                    payOrder("ORDER-TIMEOUT", 100L, "标题", "描述"));

            assertEquals(ChannelRetMsg.ChannelState.UNKNOWN,
                    response.getChannelRetMsg().getChannelState());
            assertTrue(response.getChannelRetMsg().isNeedQuery());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://xyf-server-test.postar.cn/cashier/ORDER-BAD",
            "https://xyzscxm.postar.cn/cashier/ORDER-BAD"
    })
    void shouldRejectCashierLinkThatIsNotHttpsOrCurrentEnvironmentHost(String cashierUrl)
            throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(200, "{\"code\":\"000000\",\"data\":\"" + cashierUrl + "\"}");

            StarposQrOrderRS response = pay(service(server, Duration.ofSeconds(5)),
                    payOrder("ORDER-BAD-LINK", 100L, "标题", "描述"));

            assertEquals(ChannelRetMsg.ChannelState.CONFIRM_FAIL,
                    response.getChannelRetMsg().getChannelState());
            assertFalse(response.getChannelRetMsg().getChannelErrMsg().isBlank());
            assertEquals("", response.buildPayData());
        }
    }

    @Test
    void shouldRejectCashierLinkWithNonDefaultPort() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(
                    200,
                    "{\"code\":\"000000\","
                            + "\"data\":\"https://xyf-server-test.postar.cn:8443/cashier/ORDER-PORT\"}"
            );

            StarposQrOrderRS response = pay(service(server, Duration.ofSeconds(5)),
                    payOrder("ORDER-PORT", 100L, "标题", "描述"));

            assertEquals(ChannelRetMsg.ChannelState.CONFIRM_FAIL,
                    response.getChannelRetMsg().getChannelState());
            assertEquals("", response.buildPayData());
        }
    }

    private static StarposQrOrderRS pay(StarposQr service, PayOrder payOrder) throws Exception {
        return (StarposQrOrderRS) service.pay(
                new UnifiedOrderRQ(),
                payOrder,
                context()
        );
    }

    private static StarposQr service(StarposTestServer server, Duration requestTimeout) {
        URI localBaseUri = URI.create(server.url("/").toString());
        HttpClient client = new ForwardingHttpClient(
                StarposHttpClient.newHttpClient(),
                localBaseUri
        );
        StarposHttpClient httpClient = new StarposHttpClient(
                FIXED_BASE_URI,
                client,
                requestTimeout
        );
        return new StarposQr() {
            @Override
            protected StarposHttpClient createHttpClient(String baseUrl) {
                return httpClient;
            }
        };
    }

    private static MchAppConfigContext context() {
        StarposNormalMchParams params = new StarposNormalMchParams();
        params.setEnvironment("test");
        params.setAgetId("AGENT-1001");
        params.setCustId("CUST-1001");
        params.setPublicKey(PUBLIC_KEY);
        params.setVersion("1.0.0");

        MchAppConfigContext context = new MchAppConfigContext();
        context.setMchNo("MCH-1001");
        context.setAppId("APP-1001");
        context.getNormalMchParamsMap().put(CS.IF_CODE.STARPOS, params);
        return context;
    }

    private static PayOrder payOrder(String orderNo, long amount, String subject, String body) {
        return new PayOrder()
                .setPayOrderId(orderNo)
                .setMchNo("MCH-1001")
                .setAppId("APP-1001")
                .setIfCode(CS.IF_CODE.STARPOS)
                .setWayCode(CS.PAY_WAY_CODE.STARPOS_QR)
                .setAmount(amount)
                .setSubject(subject)
                .setBody(body);
    }

    private static java.util.Map<String, Object> withoutSign(JSONObject body) {
        java.util.Map<String, Object> fields = new java.util.LinkedHashMap<>(body);
        fields.remove("sign");
        return fields;
    }

    private static String decryptRequestHash(String sign, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(
                    cipher.doFinal(Base64.getDecoder().decode(sign)),
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            throw new AssertionError("请求签名无法用本地 fixture 私钥解密", e);
        }
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new AssertionError("生成 RSA 测试密钥失败", e);
        }
    }

    private static class ForwardingHttpClient extends HttpClient {

        private final HttpClient delegate;
        private final URI localBaseUri;

        private ForwardingHttpClient(HttpClient delegate, URI localBaseUri) {
            this.delegate = delegate;
            this.localBaseUri = localBaseUri;
        }

        @Override
        public <T> HttpResponse<T> send(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) throws IOException, InterruptedException {
            return delegate.send(rewrite(request), responseBodyHandler);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) {
            return delegate.sendAsync(rewrite(request), responseBodyHandler);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            return delegate.sendAsync(
                    rewrite(request),
                    responseBodyHandler,
                    pushPromiseHandler
            );
        }

        private HttpRequest rewrite(HttpRequest request) {
            URI target = localBaseUri.resolve(request.uri().getRawPath()
                    + Optional.ofNullable(request.uri().getRawQuery())
                    .map(query -> "?" + query)
                    .orElse(""));
            HttpRequest.Builder builder = HttpRequest.newBuilder(target)
                    .method(request.method(), request.bodyPublisher()
                            .map(publisher -> publisher)
                            .orElse(HttpRequest.BodyPublishers.noBody()));
            request.headers().map().forEach((name, values) ->
                    values.forEach(value -> builder.header(name, value)));
            request.timeout().ifPresent(builder::timeout);
            return builder.build();
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
    }
}
