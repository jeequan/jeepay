package com.jeequan.jeepay.mch.ctrl.merchant;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.MerchantControllerTestSupport;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MchPayPassageConfigControllerAuthorizationTest extends MerchantControllerTestSupport {

    private static final String WAY_CODE = "ALI_QR";

    @Mock
    private MchAppService mchAppService;
    @Mock
    private MchInfoService mchInfoService;
    @Mock
    private MchPayPassageService mchPayPassageService;
    @InjectMocks
    private MchPayPassageConfigController controller;

    @ParameterizedTest(name = "拒绝查询非本商户或不存在应用的支付接口：{0}")
    @ValueSource(strings = {FOREIGN_APP_ID, MISSING_APP_ID})
    void rejectsForeignOrMissingApplicationBeforeQueryingPaymentInterfaces(String appId) {
        if (FOREIGN_APP_ID.equals(appId)) {
            lenient().when(mchAppService.getById(appId)).thenReturn(application(appId, FOREIGN_MCH_NO));
        }
        // 当前商户有效，防止既有商户状态检查掩盖缺失的应用归属校验。
        lenient().when(mchInfoService.getById(CURRENT_MCH_NO)).thenReturn(merchant(CS.YES));

        ApiRes<?> response = controller.availablePayInterface(appId, WAY_CODE);

        assertEquals(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE.getCode(), response.getCode());
        assertNull(response.getData());
        verify(mchAppService).getById(appId);
        verifyNoInteractions(mchPayPassageService);
    }

    @Test
    void ownApplicationReturnsExistingInterfacePayloadAndQueryArguments() {
        ownApplication();
        when(mchInfoService.getById(CURRENT_MCH_NO)).thenReturn(merchant(CS.YES));
        JSONObject channel = new JSONObject();
        channel.put("ifCode", "alipay");
        channel.put("ifName", "支付宝");
        List<JSONObject> interfaces = List.of(channel);
        when(mchPayPassageService.selectAvailablePayInterfaceList(
                WAY_CODE, OWN_APP_ID, CS.INFO_TYPE_MCH_APP, CS.MCH_TYPE_NORMAL)).thenReturn(interfaces);

        ApiRes<?> response = controller.availablePayInterface(OWN_APP_ID, WAY_CODE);

        assertEquals(ApiCodeEnum.SUCCESS.getCode(), response.getCode());
        assertSame(interfaces, response.getData());
        var ordered = inOrder(mchAppService, mchPayPassageService);
        ordered.verify(mchAppService).getById(OWN_APP_ID);
        ordered.verify(mchPayPassageService).selectAvailablePayInterfaceList(
                WAY_CODE, OWN_APP_ID, CS.INFO_TYPE_MCH_APP, CS.MCH_TYPE_NORMAL);
        verifyNoMoreInteractions(mchPayPassageService);
    }

    @Test
    void ownApplicationStillRejectsDisabledMerchant() {
        ownApplication();
        when(mchInfoService.getById(CURRENT_MCH_NO)).thenReturn(merchant(CS.NO));

        ApiRes<?> response = controller.availablePayInterface(OWN_APP_ID, WAY_CODE);

        assertEquals(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE.getCode(), response.getCode());
        verifyNoInteractions(mchPayPassageService);
    }

    @Test
    void ownApplicationStillRejectsMissingMerchant() {
        ownApplication();

        ApiRes<?> response = controller.availablePayInterface(OWN_APP_ID, WAY_CODE);

        assertEquals(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE.getCode(), response.getCode());
        verifyNoInteractions(mchPayPassageService);
    }

    private void ownApplication() {
        lenient().when(mchAppService.getById(OWN_APP_ID)).thenReturn(application(OWN_APP_ID, CURRENT_MCH_NO));
    }

    private static MchInfo merchant(byte state) {
        MchInfo merchant = new MchInfo();
        merchant.setMchNo(CURRENT_MCH_NO);
        merchant.setState(state);
        merchant.setType(CS.MCH_TYPE_NORMAL);
        return merchant;
    }
}
