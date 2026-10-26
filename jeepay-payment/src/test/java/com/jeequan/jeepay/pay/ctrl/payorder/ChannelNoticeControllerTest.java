/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.pay.ctrl.payorder;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IChannelNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.PayMchNotifyService;
import com.jeequan.jeepay.pay.service.PayOrderProcessService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * 同步跳转（doReturn）返回重定向视图的回归测试。
 *
 * <p>该控制器返回 String 类型的视图名，若在 response.sendRedirect() 之后返回 null，
 * Spring MVC 会继续按请求路径推导视图名并解析静态资源，导致响应已提交后仍抛出
 * NoResourceFoundException（response committed already）。因此重定向必须交由 Spring MVC 处理。
 */
@ExtendWith(MockitoExtension.class)
class ChannelNoticeControllerTest {

    private static final String IF_CODE = "alipay";
    private static final String PAY_ORDER_ID = "P2026010100001";
    private static final String MCH_NO = "mchNo";
    private static final String APP_ID = "appId";
    private static final String APP_SECRET = "appSecret";
    private static final String RETURN_URL = "https://mch.example.com/pay/return";

    @Mock private PayOrderService payOrderService;
    @Mock private ConfigContextQueryService configContextQueryService;
    @Mock private PayMchNotifyService payMchNotifyService;
    @Mock private PayOrderProcessService payOrderProcessService;
    @Mock private IChannelNoticeService channelNoticeService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    private ChannelNoticeController controller;

    @BeforeEach
    void setUp() {
        controller = new ChannelNoticeController();
        ReflectionTestUtils.setField(controller, "payOrderService", payOrderService);
        ReflectionTestUtils.setField(controller, "configContextQueryService", configContextQueryService);
        ReflectionTestUtils.setField(controller, "payMchNotifyService", payMchNotifyService);
        ReflectionTestUtils.setField(controller, "payOrderProcessService", payOrderProcessService);
        ReflectionTestUtils.setField(controller, "request", request);
        ReflectionTestUtils.setField(controller, "response", response);
    }

    /** 订单携带 returnUrl 时，应返回 redirect 视图，而不是自己写响应后返回 null */
    @Test
    void doReturnWithReturnUrlShouldReturnRedirectView() {
        PayOrder payOrder = buildPayOrder(RETURN_URL);
        MchAppConfigContext mchAppConfigContext = buildMchAppConfigContext();
        ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmSuccess("CH2026010100001");
        channelRetMsg.setResponseEntity(ResponseEntity.ok("success"));

        MutablePair<String, Object> parsed = new MutablePair<>();
        parsed.setLeft(PAY_ORDER_ID);
        parsed.setRight(new HashMap<String, Object>());

        when(channelNoticeService.parseParams(eq(request), isNull(),
                eq(IChannelNoticeService.NoticeTypeEnum.DO_RETURN))).thenReturn(parsed);
        when(payOrderService.getById(PAY_ORDER_ID)).thenReturn(payOrder);
        when(configContextQueryService.queryMchInfoAndAppInfo(MCH_NO, APP_ID)).thenReturn(mchAppConfigContext);
        when(channelNoticeService.doNotice(eq(request), any(), eq(payOrder), eq(mchAppConfigContext),
                eq(IChannelNoticeService.NoticeTypeEnum.DO_RETURN))).thenReturn(channelRetMsg);
        when(payMchNotifyService.createReturnUrl(payOrder, APP_SECRET)).thenReturn(RETURN_URL + "?sign=abc");

        try (MockedStatic<SpringBeansUtil> springBeansUtil = Mockito.mockStatic(SpringBeansUtil.class)) {
            springBeansUtil.when(() -> SpringBeansUtil.getBean(IF_CODE + "ChannelNoticeService", IChannelNoticeService.class))
                    .thenReturn(channelNoticeService);

            String view = controller.doReturn(request, IF_CODE, null);

            assertEquals("redirect:" + RETURN_URL + "?sign=abc", view);
        }
    }

    /** 直接跳转（ONLYJUMP_）的订单号，同样应返回 redirect 视图 */
    @Test
    void doReturnWithOnlyJumpOrderIdShouldReturnRedirectView() {
        PayOrder payOrder = buildPayOrder(RETURN_URL);
        MchAppConfigContext mchAppConfigContext = buildMchAppConfigContext();

        when(payOrderService.getById(PAY_ORDER_ID)).thenReturn(payOrder);
        when(configContextQueryService.queryMchInfoAndAppInfo(MCH_NO, APP_ID)).thenReturn(mchAppConfigContext);
        when(payMchNotifyService.createReturnUrl(payOrder, APP_SECRET)).thenReturn(RETURN_URL + "?sign=abc");

        try (MockedStatic<SpringBeansUtil> springBeansUtil = Mockito.mockStatic(SpringBeansUtil.class)) {
            springBeansUtil.when(() -> SpringBeansUtil.getBean(IF_CODE + "ChannelNoticeService", IChannelNoticeService.class))
                    .thenReturn(channelNoticeService);

            String view = controller.doReturn(request, IF_CODE, CS.PAY_RETURNURL_FIX_ONLY_JUMP_PREFIX + PAY_ORDER_ID);

            assertEquals("redirect:" + RETURN_URL + "?sign=abc", view);
        }
    }

    /** 订单未携带 returnUrl 时，保持跳转到支付结果页的行为不变 */
    @Test
    void doReturnWithoutReturnUrlShouldReturnReturnPageView() {
        PayOrder payOrder = buildPayOrder(null);
        MchAppConfigContext mchAppConfigContext = buildMchAppConfigContext();
        ChannelRetMsg channelRetMsg = ChannelRetMsg.confirmSuccess("CH2026010100001");
        channelRetMsg.setResponseEntity(ResponseEntity.ok("success"));

        MutablePair<String, Object> parsed = new MutablePair<>();
        parsed.setLeft(PAY_ORDER_ID);
        parsed.setRight(new HashMap<String, Object>());

        when(channelNoticeService.parseParams(eq(request), isNull(),
                eq(IChannelNoticeService.NoticeTypeEnum.DO_RETURN))).thenReturn(parsed);
        when(payOrderService.getById(PAY_ORDER_ID)).thenReturn(payOrder);
        when(configContextQueryService.queryMchInfoAndAppInfo(MCH_NO, APP_ID)).thenReturn(mchAppConfigContext);
        when(channelNoticeService.doNotice(eq(request), any(), eq(payOrder), eq(mchAppConfigContext),
                eq(IChannelNoticeService.NoticeTypeEnum.DO_RETURN))).thenReturn(channelRetMsg);

        try (MockedStatic<SpringBeansUtil> springBeansUtil = Mockito.mockStatic(SpringBeansUtil.class)) {
            springBeansUtil.when(() -> SpringBeansUtil.getBean(IF_CODE + "ChannelNoticeService", IChannelNoticeService.class))
                    .thenReturn(channelNoticeService);

            String view = controller.doReturn(request, IF_CODE, null);

            assertEquals("cashier/returnPage", view);
        }
    }

    private PayOrder buildPayOrder(String returnUrl) {
        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderId(PAY_ORDER_ID);
        payOrder.setMchNo(MCH_NO);
        payOrder.setAppId(APP_ID);
        payOrder.setReturnUrl(returnUrl);
        return payOrder;
    }

    private MchAppConfigContext buildMchAppConfigContext() {
        MchApp mchApp = new MchApp();
        mchApp.setAppId(APP_ID);
        mchApp.setAppSecret(APP_SECRET);

        MchAppConfigContext mchAppConfigContext = new MchAppConfigContext();
        mchAppConfigContext.setMchNo(MCH_NO);
        mchAppConfigContext.setAppId(APP_ID);
        mchAppConfigContext.setMchApp(mchApp);
        return mchAppConfigContext;
    }
}
