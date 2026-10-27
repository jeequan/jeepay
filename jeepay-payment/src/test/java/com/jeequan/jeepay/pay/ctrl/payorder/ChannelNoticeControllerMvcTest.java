package com.jeequan.jeepay.pay.ctrl.payorder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IChannelNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.PayMchNotifyService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 通过 DispatcherServlet 验证真实重定向响应；不启动外部服务或调用支付渠道。 */
class ChannelNoticeControllerMvcTest {
    private static final String ORDER_ID = "synthetic-order-145";
    private static final String RETURN_URL = "https://merchant.example/return";
    // 使用编码后的特殊字符，检查 MVC 不会重写签名查询串。
    private static final String SIGNED_URL = RETURN_URL
            + "?existing=a%2Bb&payOrderId=" + ORDER_ID + "&sign=A%2BB%2FC%3D&label=%E4%B8%AD%E6%96%87";
    private final PayOrderService orders = mock(PayOrderService.class);
    private final ConfigContextQueryService configs = mock(ConfigContextQueryService.class);
    private final PayMchNotifyService notifications = mock(PayMchNotifyService.class);
    private final IChannelNoticeService channel = mock(IChannelNoticeService.class);
    private MockMvc mvc;
    private boolean committedBeforeRendering;
    private Exception completionException;

    @BeforeEach
    void setUp() {
        ChannelNoticeController controller = new ChannelNoticeController();
        ReflectionTestUtils.setField(controller, "payOrderService", orders);
        ReflectionTestUtils.setField(controller, "configContextQueryService", configs);
        ReflectionTestUtils.setField(controller, "payMchNotifyService", notifications);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
                .addInterceptors(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                        ReflectionTestUtils.setField(controller, "request", request);
                        ReflectionTestUtils.setField(controller, "response", response);
                        return true;
                    }
                    @Override
                    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                                           Object handler, ModelAndView modelAndView) {
                        committedBeforeRendering = response.isCommitted();
                    }
                    @Override
                    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                                Object handler, Exception exception) {
                        completionException = exception;
                    }
                }).build();
    }

    @Test
    void normalReturnRenders302WithoutCommittingBeforeViewRendering() throws Exception {
        PayOrder order = order(RETURN_URL);
        MchAppConfigContext config = config();
        when(orders.getById(ORDER_ID)).thenReturn(order);
        when(configs.queryMchInfoAndAppInfo("synthetic-mch", "synthetic-app")).thenReturn(config);
        when(channel.parseParams(any(), isNull(), eq(IChannelNoticeService.NoticeTypeEnum.DO_RETURN)))
                .thenReturn(MutablePair.of(ORDER_ID, Collections.emptyMap()));
        ChannelRetMsg result = ChannelRetMsg.confirmSuccess("synthetic-channel-order");
        result.setResponseEntity(ResponseEntity.ok("success"));
        when(channel.doNotice(any(), any(), eq(order), eq(config), eq(IChannelNoticeService.NoticeTypeEnum.DO_RETURN)))
                .thenReturn(result);
        when(notifications.createReturnUrl(order, "synthetic-secret")).thenReturn(SIGNED_URL);
        assertRedirect("/api/pay/return/alipay");
        assertEquals(PayOrder.STATE_SUCCESS, order.getState());
    }

    @Test
    void onlyJumpRenders302AndPreservesSignedUrlExactly() throws Exception {
        PayOrder order = order(RETURN_URL);
        when(orders.getById(ORDER_ID)).thenReturn(order);
        when(configs.queryMchInfoAndAppInfo("synthetic-mch", "synthetic-app")).thenReturn(config());
        when(notifications.createReturnUrl(order, "synthetic-secret")).thenReturn(SIGNED_URL);
        assertRedirect(onlyJumpPath());
        verifyNoInteractions(channel);
    }

    @Test
    void onlyJumpMissingOrderStopsBeforeConfigurationAndSigning() throws Exception {
        assertReturnPage();
        verifyNoInteractions(configs, notifications, channel);
    }

    @Test
    void onlyJumpWithoutReturnUrlStopsBeforeSigning() throws Exception {
        when(orders.getById(ORDER_ID)).thenReturn(order(null));
        when(configs.queryMchInfoAndAppInfo("synthetic-mch", "synthetic-app")).thenReturn(config());
        assertReturnPage();
        verifyNoInteractions(notifications, channel);
    }

    private void assertRedirect(String path) throws Exception {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> events = new ListAppender<>();
        events.start();
        rootLogger.addAppender(events);
        try (MockedStatic<SpringBeansUtil> beans = channelBean()) {
            MvcResult result = mvc.perform(get(path))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", SIGNED_URL))
                    .andExpect(redirectedUrl(SIGNED_URL))
                    .andExpect(view().name("redirect:" + SIGNED_URL)).andReturn();
            assertFalse(committedBeforeRendering, "控制器不应提前提交响应，重定向由 MVC 渲染");
            assertTrue(result.getResponse().isCommitted());
            assertNull(result.getResolvedException());
            assertNull(completionException);
            assertNull(result.getResponse().getForwardedUrl());
            assertTrue(events.list.stream().noneMatch(event -> event.getLevel().isGreaterOrEqual(Level.WARN)),
                    "正常跳转不应记录响应已提交或视图解析警告：" + events.list);
        } finally {
            rootLogger.detachAppender(events);
            events.stop();
        }
    }

    private void assertReturnPage() throws Exception {
        try (MockedStatic<SpringBeansUtil> beans = channelBean()) {
            MvcResult result = mvc.perform(get(onlyJumpPath()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("cashier/returnPage"))
                    .andExpect(forwardedUrl("/templates/cashier/returnPage.html"))
                    .andExpect(header().doesNotExist("Location")).andReturn();
            assertNull(result.getResolvedException());
            assertNull(completionException);
        }
    }

    private MockedStatic<SpringBeansUtil> channelBean() {
        MockedStatic<SpringBeansUtil> beans = mockStatic(SpringBeansUtil.class);
        beans.when(() -> SpringBeansUtil.getBean("alipayChannelNoticeService", IChannelNoticeService.class))
                .thenReturn(channel);
        return beans;
    }

    private String onlyJumpPath() {
        return "/api/pay/return/alipay/" + CS.PAY_RETURNURL_FIX_ONLY_JUMP_PREFIX + ORDER_ID;
    }

    private PayOrder order(String returnUrl) {
        PayOrder order = new PayOrder();
        order.setPayOrderId(ORDER_ID);
        order.setMchNo("synthetic-mch");
        order.setAppId("synthetic-app");
        order.setReturnUrl(returnUrl);
        return order;
    }

    private MchAppConfigContext config() {
        MchApp app = new MchApp();
        app.setAppSecret("synthetic-secret");
        MchAppConfigContext config = new MchAppConfigContext();
        config.setMchApp(app);
        return config;
    }
}
