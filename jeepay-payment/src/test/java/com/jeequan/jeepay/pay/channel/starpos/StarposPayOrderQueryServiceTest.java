package com.jeequan.jeepay.pay.channel.starpos;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.starpos.StarposNormalMchParams;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarposPayOrderQueryServiceTest {

    private static final URI FIXED_BASE_URI = URI.create("https://xyf-server-test.postar.cn");
    private static final KeyPair KEY_PAIR = generateKeyPair();
    private static final String PUBLIC_KEY = Base64.getEncoder()
            .encodeToString(KEY_PAIR.getPublic().getEncoded());

    @Test
    void shouldQuerySuccessWithDateAndSignedRequest() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            JSONObject data = new JSONObject();
            data.put("orderNo", "STARPOS-PLATFORM-1001");
            data.put("threeOrderNo", "ORDER-QUERY-1001");
            data.put("torderNo", "T-ORDER-1001");
            data.put("orderStatus", "1");
            data.put("txamt", "12345");

            server.enqueueJson(200, signedDataResponse("000000", "成功", data));

            PayOrder payOrder = payOrder("ORDER-QUERY-1001", 12345L);
            payOrder.setCreatedAt(new Date(1782396000000L));

            ChannelRetMsg result = query(service(server), payOrder);

            assertEquals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS,
                    result.getChannelState());
            assertEquals("STARPOS-PLATFORM-1001", result.getChannelOrderId());

            JSONObject origin = JSONObject.parseObject(result.getChannelOriginResponse());
            assertEquals("T-ORDER-1001",
                    origin.getJSONObject("starposExtension").getString("torderNo"));
            assertEquals("ORDER-QUERY-1001",
                    origin.getJSONObject("starposExtension").getString("threeOrderNo"));

            StarposTestServer.RecordedRequest request = server.takeRequest();
            assertEquals("/yyfsevr/order/orderQuery", request.getPath());
            JSONObject requestBody = JSONObject.parseObject(request.getJsonBody());
            assertEquals("ORDER-QUERY-1001", requestBody.getString("orderNo"));
            assertEquals(
                    new SimpleDateFormat("yyyyMMdd").format(payOrder.getCreatedAt()),
                    requestBody.getString("orderTime")
            );
            assertEquals("AGENT-1001", requestBody.getString("agetId"));
            assertEquals("CUST-1001", requestBody.getString("custId"));
            assertFalse(requestBody.getString("sign").isBlank());
            assertEquals(
                    StarposSigner.sha256Hex(StarposSigner.canonicalJson(withoutSign(requestBody))),
                    decryptRequestHash(requestBody.getString("sign"), KEY_PAIR.getPrivate())
            );
        }
    }

    @ParameterizedTest
    @MethodSource("waitingResponses")
    void shouldKeepOrderInProgressForPendingResponses(
            String code,
            String orderStatus
    ) throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            JSONObject data = new JSONObject();
            data.put("orderNo", "STARPOS-PENDING-1001");
            data.put("orderStatus", orderStatus);
            server.enqueueJson(200, signedDataResponse(code, "处理中", data));

            ChannelRetMsg result = query(
                    service(server),
                    payOrder("ORDER-PENDING-1001", 100L)
            );

            assertEquals(ChannelRetMsg.ChannelState.WAITING,
                    result.getChannelState());
            assertEquals(code, result.getChannelErrCode());
            assertFalse(result.isNeedQuery());
        }
    }

    private static Stream<Arguments> waitingResponses() {
        return Stream.of(
                Arguments.of("222222", "2"),
                Arguments.of("000000", "2")
        );
    }

    @ParameterizedTest
    @MethodSource("failedStatuses")
    void shouldMapTerminalFailureStatusesToConfirmedFailure(String orderStatus) throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            JSONObject data = new JSONObject();
            data.put("orderNo", "STARPOS-FAILED-1001");
            data.put("orderStatus", orderStatus);
            server.enqueueJson(200, signedDataResponse("000000", "订单失败", data));

            ChannelRetMsg result = query(
                    service(server),
                    payOrder("ORDER-FAILED-1001", 100L)
            );

            assertEquals(ChannelRetMsg.ChannelState.CONFIRM_FAIL,
                    result.getChannelState());
            assertEquals("000000", result.getChannelErrCode());
            assertFalse(result.isNeedQuery());
        }
    }

    private static Stream<String> failedStatuses() {
        return Stream.of("0", "99");
    }

    @Test
    void shouldPreserveLocalStateWhenUpstreamOrderIsNotFound() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            JSONObject response = new JSONObject();
            response.put("code", "000002");
            response.put("message", "订单不存在");
            response.put("orderNo", "ORDER-NOT-FOUND-1001");
            response.put("sign", privateEncryptHash(
                    withoutSign(response),
                    KEY_PAIR.getPrivate()
            ));
            server.enqueueJson(200, response.toJSONString());

            ChannelRetMsg result = query(
                    service(server),
                    payOrder("ORDER-NOT-FOUND-1001", 100L)
            );

            assertEquals(ChannelRetMsg.ChannelState.WAITING,
                    result.getChannelState());
            assertEquals("000002", result.getChannelErrCode());
            assertFalse(result.isNeedQuery());
        }
    }

    @Test
    void shouldReturnUnknownForUpstreamUnknownResult() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            JSONObject response = new JSONObject();
            response.put("code", "-80000");
            response.put("message", "结果未知");
            response.put("sign", privateEncryptHash(
                    withoutSign(response),
                    KEY_PAIR.getPrivate()
            ));
            server.enqueueJson(200, response.toJSONString());

            ChannelRetMsg result = query(
                    service(server),
                    payOrder("ORDER-UNKNOWN-1001", 100L)
            );

            assertEquals(ChannelRetMsg.ChannelState.UNKNOWN,
                    result.getChannelState());
            assertEquals("-80000", result.getChannelErrCode());
            assertTrue(result.isNeedQuery());
        }
    }

    @Test
    void shouldRejectTopLevelCodeTamperingWhenOnlyNestedDataIsSigned() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            JSONObject response = JSONObject.parseObject(signedDataResponse(
                    "000000",
                    "成功",
                    signedSuccessData("STARPOS-TAMPER-1001", 100L)
            ));
            response.put("code", "000002");
            server.enqueueJson(200, response.toJSONString());

            ChannelRetMsg result = query(
                    service(server),
                    payOrder("ORDER-TAMPER-1001", 100L)
            );

            assertEquals(ChannelRetMsg.ChannelState.UNKNOWN,
                    result.getChannelState());
            assertTrue(result.isNeedQuery());
        }
    }

    @Test
    void shouldConfirmFailureForInvalidConfiguration() {
        MchAppConfigContext context = context();
        StarposNormalMchParams params = context.getNormalMchParamsByIfCode(
                CS.IF_CODE.STARPOS,
                StarposNormalMchParams.class
        );
        params.setEnvironment("invalid");

        ChannelRetMsg result = new StarposPayOrderQueryService()
                .query(payOrder("ORDER-CONFIG-1001", 100L), context);

        assertEquals(ChannelRetMsg.ChannelState.CONFIRM_FAIL,
                result.getChannelState());
        assertEquals("STARPOS_QUERY_ERROR", result.getChannelErrCode());
        assertFalse(result.isNeedQuery());
    }

    @Test
    void shouldReturnUnknownForHttpErrorAndAllowFurtherQuery() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            server.enqueueJson(502, "{\"code\":\"GATEWAY_ERROR\"}");

            ChannelRetMsg result = query(
                    service(server),
                    payOrder("ORDER-HTTP-ERROR-1001", 100L)
            );

            assertEquals(ChannelRetMsg.ChannelState.UNKNOWN,
                    result.getChannelState());
            assertEquals("STARPOS_QUERY_ERROR", result.getChannelErrCode());
            assertTrue(result.isNeedQuery());
        }
    }

    @Test
    void shouldNotConfirmSuccessWhenReturnedAmountDoesNotMatch() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            JSONObject data = new JSONObject();
            data.put("orderNo", "STARPOS-AMOUNT-1001");
            data.put("threeOrderNo", "ORDER-AMOUNT-1001");
            data.put("orderStatus", "1");
            data.put("txamt", "101");
            server.enqueueJson(200, signedDataResponse("000000", "成功", data));

            ChannelRetMsg result = query(
                    service(server),
                    payOrder("ORDER-AMOUNT-1001", 100L)
            );

            assertEquals(ChannelRetMsg.ChannelState.UNKNOWN,
                    result.getChannelState());
            assertTrue(result.isNeedQuery());
            assertNotNull(result.getChannelErrMsg());
        }
    }

    @Test
    void shouldRejectInvalidResponseSignatureWithoutChangingLocalState() throws Exception {
        try (StarposTestServer server = StarposTestServer.start()) {
            JSONObject data = new JSONObject();
            data.put("orderNo", "STARPOS-SIGN-1001");
            data.put("threeOrderNo", "ORDER-SIGN-1001");
            data.put("orderStatus", "1");
            data.put("txamt", "100");
            data.put("sign", "invalid-signature");
            JSONObject response = new JSONObject();
            response.put("code", "000000");
            response.put("data", data);
            server.enqueueJson(200, response.toJSONString());

            ChannelRetMsg result = query(
                    service(server),
                    payOrder("ORDER-SIGN-1001", 100L)
            );

            assertEquals(ChannelRetMsg.ChannelState.UNKNOWN,
                    result.getChannelState());
            assertTrue(result.isNeedQuery());
        }
    }

    private static ChannelRetMsg query(
            StarposPayOrderQueryService service,
            PayOrder payOrder
    ) throws Exception {
        return service.query(payOrder, context());
    }

    private static StarposPayOrderQueryService service(StarposTestServer server) {
        URI localBaseUri = URI.create(server.url("/").toString());
        HttpClient client = new ForwardingHttpClient(
                StarposHttpClient.newHttpClient(),
                localBaseUri
        );
        StarposHttpClient httpClient = new StarposHttpClient(
                FIXED_BASE_URI,
                client,
                Duration.ofSeconds(5)
        );
        return new StarposPayOrderQueryService() {
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

    private static PayOrder payOrder(String orderNo, long amount) {
        return new PayOrder()
                .setPayOrderId(orderNo)
                .setMchNo("MCH-1001")
                .setAppId("APP-1001")
                .setIfCode(CS.IF_CODE.STARPOS)
                .setWayCode(CS.PAY_WAY_CODE.STARPOS_QR)
                .setAmount(amount)
                .setSubject("标题")
                .setBody("描述")
                .setCreatedAt(new Date());
    }

    private static String signedDataResponse(
            String code,
            String message,
            JSONObject data
    ) {
        data.put("code", code);
        data.put("message", message);
        data.put("sign", privateEncryptHash(withoutSign(data), KEY_PAIR.getPrivate()));
        JSONObject response = new JSONObject();
        response.put("code", code);
        response.put("message", message);
        response.put("data", data);
        return response.toJSONString();
    }

    private static JSONObject signedSuccessData(String platformOrderNo, long amount) {
        JSONObject data = new JSONObject();
        data.put("orderNo", platformOrderNo);
        data.put("threeOrderNo", "ORDER-TAMPER-1001");
        data.put("torderNo", "T-ORDER-TAMPER-1001");
        data.put("orderStatus", "1");
        data.put("txamt", String.valueOf(amount));
        return data;
    }

    private static Map<String, Object> withoutSign(JSONObject body) {
        Map<String, Object> fields = new LinkedHashMap<>(body);
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

    private static String privateEncryptHash(
            Map<String, Object> fields,
            PrivateKey privateKey
    ) {
        try {
            Cipher encryptor = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            encryptor.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] hash = StarposSigner.sha256Hex(StarposSigner.canonicalJson(fields))
                    .getBytes(StandardCharsets.UTF_8);
            return Base64.getEncoder().encodeToString(encryptor.doFinal(hash));
        } catch (Exception e) {
            throw new IllegalStateException("failed to create local private-key response fixture", e);
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
