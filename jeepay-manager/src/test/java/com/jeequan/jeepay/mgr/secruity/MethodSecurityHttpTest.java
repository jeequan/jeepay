package com.jeequan.jeepay.mgr.secruity;

import com.jeequan.jeepay.mgr.config.SystemYmlConfig;
import com.jeequan.jeepay.core.exception.BizExceptionResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 加载生产安全配置、真实 HTTP 过滤器链及 MVC 异常处理器，不直接调用 Controller。
 * 仅注册无害的测试端点，业务依赖为 mock；不启动数据库、Redis、MQ、支付客户端或服务器。
 * 认证信息放入真实过滤器链使用的请求属性上下文仓库，因此这里只验证授权，
 * 不覆盖 JWT 解析、登录及令牌缓存集成。
 * 保留现有响应约定：方法权限拒绝为 HTTP 200 + 业务码 5004，未登录为 HTTP 401。
 * 测试配置不得自行启用方法安全，必须由生产 WebSecurityConfig 注册 Spring 代理及拦截器。
 */
@SpringJUnitConfig(MethodSecurityHttpTest.TestConfig.class)
@WebAppConfiguration
class MethodSecurityHttpTest {
    private static final String PATH = "/method-security-test/probe";
    private static final String AUTHORITY = "ENT_UR_USER_LIST";

    @Autowired private WebApplicationContext context;
    @Autowired private FilterChainProxy springSecurityFilterChain;
    @Autowired private ProbeBusiness business;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        reset(business);
        SecurityContextHolder.clearContext();
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain).build();
    }

    @Test
    void authenticatedWithoutPermissionIsDeniedBeforeBusinessRuns() throws Exception {
        mvc.perform(authenticatedRequest())
                .andExpect(status().isOk())
                .andDo(result -> verifyNoInteractions(business))
                .andExpect(jsonPath("$.code").value(5004));
        verifyNoInteractions(business);
    }

    @Test
    void unrelatedPermissionIsDeniedBeforeBusinessRuns() throws Exception {
        mvc.perform(authenticatedRequest("ENT_UNRELATED"))
                .andExpect(status().isOk())
                .andDo(result -> verifyNoInteractions(business))
                .andExpect(jsonPath("$.code").value(5004));
        verifyNoInteractions(business);
    }

    @Test
    void matchingPermissionAllowsBusinessExactlyOnce() throws Exception {
        mvc.perform(authenticatedRequest(AUTHORITY))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
        verify(business).read();
        verifyNoMoreInteractions(business);
    }

    @Test
    void anonymousRequestRemainsUnauthorizedBeforeBusinessRuns() throws Exception {
        mvc.perform(get(PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Unauthorized"));
        verifyNoInteractions(business);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ENT_PROBE_READ", "ENT_PROBE_EDIT"})
    void eitherMatchingPermissionAllowsBusiness(String authority) throws Exception {
        mvc.perform(authenticatedRequestAt(PATH + "/any", authority))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
        verify(business).read();
        verifyNoMoreInteractions(business);
    }

    @Test
    void noMatchingAlternativePermissionIsDeniedBeforeBusinessRuns() throws Exception {
        mvc.perform(authenticatedRequestAt(PATH + "/any", "ENT_UNRELATED"))
                .andExpect(status().isOk())
                .andDo(result -> verifyNoInteractions(business))
                .andExpect(jsonPath("$.code").value(5004));
        verifyNoInteractions(business);
    }

    private MockHttpServletRequestBuilder authenticatedRequest(String... authorities) {
        return authenticatedRequestAt(PATH, authorities);
    }

    private MockHttpServletRequestBuilder authenticatedRequestAt(String path, String... authorities) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                "isolated-test-user", null, AuthorityUtils.createAuthorityList(authorities)));
        return get(path).requestAttr(RequestAttributeSecurityContextRepository.DEFAULT_REQUEST_ATTR_NAME,
                securityContext);
    }

    interface ProbeBusiness {
        void read();
    }

    @RestController
    static class ProbeController {
        private final ProbeBusiness business;

        ProbeController(ProbeBusiness business) {
            this.business = business;
        }

        @GetMapping(PATH + "/any")
        @PreAuthorize("hasAnyAuthority('ENT_PROBE_READ', 'ENT_PROBE_EDIT')")
        public String probeAny() {
            business.read();
            return "ok";
        }

        @GetMapping(PATH)
        @PreAuthorize("hasAuthority('ENT_UR_USER_LIST')")
        public String probe() {
            business.read();
            return "ok";
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({WebSecurityConfig.class, JeeAuthenticationEntryPoint.class,
            BizExceptionResolver.class, ProbeController.class})
    static class TestConfig {
        @Bean
        ProbeBusiness business() {
            return mock(ProbeBusiness.class);
        }

        @Bean
        @Primary
        UserDetailsService isolatedUserDetailsService() {
            return mock(UserDetailsService.class);
        }

        @Bean
        SystemYmlConfig systemYmlConfig() {
            SystemYmlConfig config = new SystemYmlConfig();
            config.setAllowCors(false);
            return config;
        }
    }
}
