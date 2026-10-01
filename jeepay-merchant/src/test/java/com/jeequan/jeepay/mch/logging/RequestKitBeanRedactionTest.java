package com.jeequan.jeepay.mch.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.jeequan.jeepay.core.beans.RequestKitBean;
import com.jeequan.jeepay.core.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 检查实际格式化输出，避免只过滤消息模板却仍通过异常栈泄漏请求。 */
class RequestKitBeanRedactionTest {

    private static final String BODY_SECRET = "malformed-body-password-sentinel-21";
    private static final String ERROR_SECRET = "reader-exception-password-sentinel-42";

    private Logger logger;
    private PatternLayoutEncoder encoder;
    private OutputStreamAppender<ILoggingEvent> appender;
    private ByteArrayOutputStream output;
    private RequestKitBean requestKit;

    @BeforeEach
    void captureRenderedLogs() {
        requestKit = new RequestKitBean();
        logger = (Logger) LoggerFactory.getLogger(RequestKitBean.class);
        LoggerContext context = logger.getLoggerContext();
        output = new ByteArrayOutputStream();
        encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.setPattern("%level %msg%n%ex");
        encoder.start();
        appender = new OutputStreamAppender<>();
        appender.setContext(context);
        appender.setEncoder(encoder);
        appender.setOutputStream(output);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachCapture() {
        logger.detachAppender(appender);
        appender.stop();
        encoder.stop();
    }

    @Test
    void malformedJsonNeverWritesRawBodyOrParserException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/current/modifyPwd");
        request.setContentType("application/json");
        request.setCharacterEncoding("UTF-8");
        request.setContent(("{\"currentPwd\":\"" + BODY_SECRET + "\",\"newPwd\":")
                .getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(requestKit, "request", request);

        assertThrows(BizException.class, () -> requestKit.reqParam2JSON());

        assertSafeDiagnostic();
    }

    @Test
    void partialBodyReadNeverWritesAlreadyReadPasswordOrReaderException() throws IOException {
        BufferedReader reader = mock(BufferedReader.class);
        when(reader.readLine()).thenReturn("{\"password\":\"" + BODY_SECRET + "\"}")
                .thenThrow(new IOException(ERROR_SECRET));
        HttpServletRequest request = jsonRequest();
        when(request.getReader()).thenReturn(reader);
        ReflectionTestUtils.setField(requestKit, "request", request);

        assertThrows(BizException.class, () -> requestKit.getReqParamFromBody());

        assertSafeDiagnostic();
    }

    @Test
    void jsonReaderFailureNeverWritesExceptionMessage() throws IOException {
        HttpServletRequest request = jsonRequest();
        when(request.getReader()).thenThrow(new IOException(ERROR_SECRET));
        ReflectionTestUtils.setField(requestKit, "request", request);

        assertThrows(BizException.class, () -> requestKit.reqParam2JSON());

        assertSafeDiagnostic();
    }

    private HttpServletRequest jsonRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("application/json");
        when(request.getMethod()).thenReturn("POST");
        return request;
    }

    private void assertSafeDiagnostic() {
        String rendered = new String(output.toByteArray(), StandardCharsets.UTF_8);
        assertThat(rendered).contains("ERROR", "请求参数转换异常");
        assertThat(rendered).doesNotContain(BODY_SECRET, ERROR_SECRET,
                "currentPwd", "newPwd", "password", "JSONException", "IOException");
    }
}
