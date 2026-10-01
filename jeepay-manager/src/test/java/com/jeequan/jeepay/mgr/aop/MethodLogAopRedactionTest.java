package com.jeequan.jeepay.mgr.aop;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.beans.RequestKitBean;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysLog;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.service.impl.SysLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** 直接检查异步持久化边界，不启动数据库或应用容器。 */
class MethodLogAopRedactionTest {

    private static final String REQUEST_PASSWORD = "request-password-sentinel-51";
    private static final String BASE64_PASSWORD = Base64.getEncoder().encodeToString(
            "new-password-sentinel-73".getBytes(StandardCharsets.UTF_8));
    private static final String PASSWORD_HASH = "b5020a7e7fd2363e397dcf4ddc79e39c54a5940e51f0df8af2d53cf583064f9b";
    private static final String RESPONSE_PASSWORD = "response-password-sentinel-83";
    private static final String EXCEPTION_PASSWORD = "exception-password-sentinel-97";

    private MethodLogAop aspect;
    private SysLogService logService;
    private RequestKitBean requestKit;
    private ProceedingJoinPoint joinPoint;
    private JSONObject originalRequest;
    private String requestSnapshot;
    private final BlockingQueue<SysLog> savedLogs = new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() {
        aspect = new MethodLogAop();
        logService = mock(SysLogService.class);
        requestKit = new RequestKitBean();
        ReflectionTestUtils.setField(aspect, "sysLogService", logService);
        ReflectionTestUtils.setField(aspect, "requestKitBean", requestKit);
        doAnswer(invocation -> {
            savedLogs.add(invocation.getArgument(0));
            return true;
        }).when(logService).save(any(SysLog.class));

        JSONObject requestBody = new JSONObject();
        requestBody.put("loginName", "audit-test-user");
        requestBody.put("originalPwd", REQUEST_PASSWORD);
        requestBody.put("newPwd", BASE64_PASSWORD);
        requestBody.put("confirmPwd", BASE64_PASSWORD);
        requestBody.put("ifParams", "{\"isvPrivateCertPwd\":\"" + REQUEST_PASSWORD + "\"}");
        JSONObject nested = new JSONObject();
        nested.put("passwordHash", PASSWORD_HASH);
        nested.put("enabled", true);
        JSONArray accounts = new JSONArray();
        accounts.add(nested);
        requestBody.put("accounts", accounts);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/current/modifyPwd");
        request.setContentType("application/json");
        request.setCharacterEncoding("UTF-8");
        request.setContent(requestBody.toJSONString().getBytes(StandardCharsets.UTF_8));
        request.setRemoteAddr("192.0.2.37");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        ReflectionTestUtils.setField(requestKit, "request", request);
        originalRequest = requestKit.getReqParamJSON();
        requestSnapshot = originalRequest.toJSONString();

        SysUser user = new SysUser();
        user.setSysUserId(701L);
        user.setRealname("审计测试用户");
        user.setSysType(CS.SYS_TYPE.MGR);
        JeeUserDetails principal = new JeeUserDetails(user, "authentication-test-only");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));

        AuditTarget target = new AuditTarget();
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getName()).thenReturn("changePassword");
        when(signature.getParameterTypes()).thenReturn(new Class<?>[0]);
        joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getThis()).thenReturn(target);
    }

    @AfterEach
    void clearThreadContexts() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void successRedactsNestedRequestAndResponseBeforeSavingWithoutMutatingThem() throws Throwable {
        JSONObject account = new JSONObject();
        account.put("Password", RESPONSE_PASSWORD);
        account.put("credential", PASSWORD_HASH);
        account.put("passwordBase64", BASE64_PASSWORD);
        account.put("loginName", "audit-test-user");
        JSONArray records = new JSONArray();
        records.add(account);
        JSONObject data = new JSONObject();
        data.put("records", records);
        data.put("updated", true);
        ApiRes<JSONObject> response = ApiRes.ok(data);
        String responseSnapshot = response.toJSONString();
        when(joinPoint.proceed()).thenReturn(response);

        assertThat(aspect.around(joinPoint)).isSameAs(response);
        SysLog saved = awaitSavedLog();

        assertSanitizedRequest(saved);
        JSONObject savedResponse = JSONObject.parseObject(saved.getOptResInfo());
        JSONObject savedAccount = savedResponse.getJSONObject("data").getJSONArray("records").getJSONObject(0);
        assertThat(savedAccount.getString("Password")).isEqualTo("[REDACTED]");
        assertThat(savedAccount.getString("credential")).isEqualTo("[REDACTED]");
        assertThat(savedAccount.getString("passwordBase64")).isEqualTo("[REDACTED]");
        assertThat(savedAccount.getString("loginName")).isEqualTo("audit-test-user");
        assertThat(savedResponse.getInteger("code")).isEqualTo(response.getCode());
        assertThat(savedResponse.getJSONObject("data").getBoolean("updated")).isTrue();
        assertNoSecrets(saved);
        assertMetadata(saved);
        assertUnchangedRequest();
        assertThat(response.toJSONString()).isEqualTo(responseSnapshot);
        assertThat(response.getData().getJSONArray("records").getJSONObject(0)).isSameAs(account);
    }

    @Test
    void businessExceptionKeepsCodeButNeverPersistsUncontrolledMessage() throws Throwable {
        BizException failure = new BizException("密码校验失败: " + EXCEPTION_PASSWORD);
        when(joinPoint.proceed()).thenThrow(failure);
        assertThat(assertThrows(BizException.class, () -> aspect.around(joinPoint))).isSameAs(failure);
        verifyNoInteractions(logService);

        aspect.doException(joinPoint, failure);
        SysLog saved = awaitSavedLog();

        assertSanitizedRequest(saved);
        assertNoSecrets(saved);
        assertThat(saved.getOptResInfo()).contains(String.valueOf(failure.getApiRes().getCode()));
        assertMetadata(saved);
        assertUnchangedRequest();
    }

    @Test
    void unexpectedExceptionStillSavesSanitizedRequestAndGenericOutcome() throws Throwable {
        IllegalStateException failure = new IllegalStateException("internal password=" + EXCEPTION_PASSWORD);
        when(joinPoint.proceed()).thenThrow(failure);
        assertThat(assertThrows(IllegalStateException.class, () -> aspect.around(joinPoint))).isSameAs(failure);
        verifyNoInteractions(logService);

        aspect.doException(joinPoint, failure);
        SysLog saved = awaitSavedLog();

        assertSanitizedRequest(saved);
        assertThat(saved.getOptResInfo()).isEqualTo("请求异常");
        assertNoSecrets(saved);
        assertMetadata(saved);
        assertUnchangedRequest();
    }

    @Test
    void auditFailureNeverLogsSecretOrStackAndLeavesResponseUnchanged() throws Throwable {
        RequestKitBean failingRequestKit = mock(RequestKitBean.class);
        when(failingRequestKit.getReqParamJSON())
                .thenThrow(new IllegalStateException("password=" + EXCEPTION_PASSWORD));
        ReflectionTestUtils.setField(aspect, "requestKitBean", failingRequestKit);
        ApiRes<String> response = ApiRes.ok("业务处理成功");
        String responseSnapshot = response.toJSONString();
        when(joinPoint.proceed()).thenReturn(response);

        Logger logger = (Logger) LoggerFactory.getLogger(MethodLogAop.class);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(logger.getLoggerContext());
        encoder.setCharset(StandardCharsets.UTF_8);
        // 同时渲染异常栈，防止只检查消息时漏掉 Throwable 参数。
        encoder.setPattern("%level %msg%n%ex");
        encoder.start();
        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        appender.setContext(logger.getLoggerContext());
        appender.setEncoder(encoder);
        appender.setOutputStream(output);
        appender.start();
        logger.addAppender(appender);
        try {
            assertThat(aspect.around(joinPoint)).isSameAs(response);
            assertThat(response.toJSONString()).isEqualTo(responseSnapshot);
            verifyNoInteractions(logService);
            assertThat(new String(output.toByteArray(), StandardCharsets.UTF_8))
                    .contains("ERROR", "methodLogError")
                    .doesNotContain(EXCEPTION_PASSWORD, "IllegalStateException", "\tat ");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
            encoder.stop();
        }
    }

    private SysLog awaitSavedLog() throws InterruptedException {
        SysLog saved = savedLogs.poll(5, TimeUnit.SECONDS);
        assertThat(saved).as("异步保存的审计日志").isNotNull();
        return saved;
    }

    private void assertSanitizedRequest(SysLog saved) {
        JSONObject stored = JSONObject.parseObject(saved.getOptReqParam());
        assertThat(stored.getString("originalPwd")).isEqualTo("[REDACTED]");
        assertThat(stored.getString("newPwd")).isEqualTo("[REDACTED]");
        assertThat(stored.getString("confirmPwd")).isEqualTo("[REDACTED]");
        assertThat(stored.getString("ifParams")).isEqualTo("[REDACTED]");
        assertThat(stored.getJSONArray("accounts").getJSONObject(0).getString("passwordHash"))
                .isEqualTo("[REDACTED]");
        assertThat(stored.getString("loginName")).isEqualTo("audit-test-user");
        assertThat(stored.getJSONArray("accounts").getJSONObject(0).getBoolean("enabled")).isTrue();
    }

    private void assertNoSecrets(SysLog saved) {
        assertThat(saved.getOptReqParam() + saved.getOptResInfo()).doesNotContain(
                REQUEST_PASSWORD, BASE64_PASSWORD, PASSWORD_HASH, RESPONSE_PASSWORD, EXCEPTION_PASSWORD);
    }

    private void assertUnchangedRequest() {
        assertThat(requestKit.getReqParamJSON()).isSameAs(originalRequest);
        assertThat(originalRequest.toJSONString()).isEqualTo(requestSnapshot);
    }

    private void assertMetadata(SysLog saved) {
        assertThat(saved.getUserId()).isEqualTo(701L);
        assertThat(saved.getUserName()).isEqualTo("审计测试用户");
        assertThat(saved.getSysType()).isEqualTo(CS.SYS_TYPE.MGR);
        assertThat(saved.getUserIp()).isEqualTo("192.0.2.37");
        assertThat(saved.getReqUrl()).isEqualTo("http://localhost/api/current/modifyPwd");
        assertThat(saved.getMethodName()).isEqualTo(AuditTarget.class.getName() + ".changePassword");
        assertThat(saved.getMethodRemark()).isEqualTo("测试密码修改");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    public static class AuditTarget {
        @MethodLog(remark = "测试密码修改")
        public void changePassword() { }
    }
}
