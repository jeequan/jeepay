package com.jeequan.jeepay.mgr.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.OutputStreamAppender;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** 从实际配置读取日志级别，在独立上下文验证；不改动测试进程的全局日志配置。 */
class AuthMapperLoggingConfigTest {

    private static final String AUTH_MAPPER = "com.jeequan.jeepay.service.mapper.SysUserAuthMapper";
    private static final String OTHER_MAPPER = "com.jeequan.jeepay.service.mapper.SysUserMapper.updateById";

    @Test
    void hidesCredentialParametersAndRowsWithoutSuppressingSafeDiagnostics() throws Exception {
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new LogbackMDCAdapter());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        try {
            // 即使项目级别开放 TRACE，认证 SQL 也不能输出参数或查询结果行。
            context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.TRACE);
            loadLoggerLevelsFromApplicationResource(context);
            encoder.setContext(context);
            encoder.setCharset(StandardCharsets.UTF_8);
            encoder.setPattern("%level %logger %msg%n%ex");
            encoder.start();
            appender.setContext(context);
            appender.setEncoder(encoder);
            appender.setOutputStream(output);
            appender.start();
            context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);

            for (String statement : new String[]{"insert", "updateById", "selectByLogin", "selectList"}) {
                Logger logger = context.getLogger(AUTH_MAPPER + "." + statement);
                logger.debug("Parameters: synthetic-password-hash-sentinel");
                logger.trace("Row: synthetic-password-hash-sentinel");
                logger.info("safe-info-" + statement);
                logger.warn("safe-warn-" + statement);
                logger.error("safe-error-" + statement);

                assertThat(logger.isDebugEnabled()).as(statement + " 参数日志").isFalse();
                assertThat(logger.isTraceEnabled()).as(statement + " 结果行日志").isFalse();
                assertThat(logger.isInfoEnabled()).isTrue();
                assertThat(logger.isWarnEnabled()).isTrue();
                assertThat(logger.isErrorEnabled()).isTrue();
            }
            Logger unrelated = context.getLogger(OTHER_MAPPER);
            assertThat(unrelated.isDebugEnabled()).as("其他业务 SQL 调试日志").isTrue();
            unrelated.debug("safe-unrelated-mapper-debug");

            String rendered = new String(output.toByteArray(), StandardCharsets.UTF_8);
            assertThat(rendered).doesNotContain("synthetic-password-hash-sentinel", "Parameters:", "Row:");
            for (String statement : new String[]{"insert", "updateById", "selectByLogin", "selectList"}) {
                assertThat(rendered).contains("safe-info-" + statement,
                        "safe-warn-" + statement, "safe-error-" + statement);
            }
            assertThat(rendered).contains("safe-unrelated-mapper-debug");
        } finally {
            appender.stop();
            encoder.stop();
            context.stop();
        }
    }

    private void loadLoggerLevelsFromApplicationResource(LoggerContext context) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        try (InputStream resource = getClass().getResourceAsStream("/logback-spring.xml")) {
            assertThat(resource).as("当前应用实际打包的日志配置").isNotNull();
            NodeList loggers = factory.newDocumentBuilder().parse(resource).getElementsByTagName("logger");
            boolean authGuardFound = false;
            for (int i = 0; i < loggers.getLength(); i++) {
                Element entry = (Element) loggers.item(i);
                String name = entry.getAttribute("name");
                String configuredLevel = entry.getAttribute("level");
                if (AUTH_MAPPER.equals(name)) {
                    assertThat(configuredLevel).as("认证 SQL 日志下限").isEqualTo("INFO");
                    authGuardFound = true;
                }
                // springProperty 在生产由 Spring 解析；这里只注入最详细的父级日志场景。
                String level = configuredLevel.replace("${currentProjectLevel}", "TRACE")
                        .replace("${currentRootLevel}", "TRACE");
                if (!level.isEmpty()) {
                    assertThat(level).doesNotContain("${");
                    context.getLogger(name).setLevel(Level.toLevel(level));
                    if (name.startsWith(AUTH_MAPPER + ".")) {
                        assertThat(context.getLogger(name).getLevel().isGreaterOrEqual(Level.INFO))
                                .as("认证子日志不能重新开放 DEBUG/TRACE").isTrue();
                    }
                }
                if (entry.hasAttribute("additivity")) {
                    context.getLogger(name).setAdditive(Boolean.parseBoolean(entry.getAttribute("additivity")));
                }
            }
            assertThat(authGuardFound).as("认证 Mapper 必须有独立日志级别，不能继承项目 DEBUG/TRACE").isTrue();
        }
    }
}
