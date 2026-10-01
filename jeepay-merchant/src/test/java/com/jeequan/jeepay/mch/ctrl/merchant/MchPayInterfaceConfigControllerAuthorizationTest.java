package com.jeequan.jeepay.mch.ctrl.merchant;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.MerchantControllerTestSupport;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.PayInterfaceConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MchPayInterfaceConfigControllerAuthorizationTest extends MerchantControllerTestSupport {

    private static final String IF_CODE = "alipay";

    @Mock
    private MchAppService mchAppService;
    @Mock
    private MchInfoService mchInfoService;
    @Mock
    private PayInterfaceConfigService payInterfaceConfigService;
    @InjectMocks
    private MchPayInterfaceConfigController controller;

    @ParameterizedTest(name = "拒绝读取非本商户或不存在应用：{0}")
    @ValueSource(strings = {FOREIGN_APP_ID, MISSING_APP_ID})
    void rejectsForeignOrMissingApplicationBeforeReadingConfiguration(String appId) {
        if (FOREIGN_APP_ID.equals(appId)) {
            lenient().when(mchAppService.getById(appId)).thenReturn(application(appId, FOREIGN_MCH_NO));
        }

        assertThrows(BizException.class, () -> controller.getByMchNo(appId, IF_CODE));

        verify(mchAppService).getById(appId);
        verifyNoInteractions(payInterfaceConfigService, mchInfoService);
    }

    @Test
    void ownApplicationStillScalesRateAndMasksNormalMerchantSecrets() {
        ownApplication();
        MchInfo merchant = merchant(CS.MCH_TYPE_NORMAL);
        when(mchInfoService.getById(CURRENT_MCH_NO)).thenReturn(merchant);
        PayInterfaceConfig config = config(new BigDecimal("0.0060"),
                "{\"appId\":\"provider-app\",\"privateKey\":\"1234567890abcdefghij\","
                        + "\"alipayPublicKey\":\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"signType\":\"RSA2\"}");
        when(payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, OWN_APP_ID, IF_CODE)).thenReturn(config);

        ApiRes<PayInterfaceConfig> response = controller.getByMchNo(OWN_APP_ID, IF_CODE);

        assertEquals(ApiCodeEnum.SUCCESS.getCode(), response.getCode());
        assertSame(config, response.getData());
        assertEquals(0, new BigDecimal("0.60").compareTo(response.getData().getIfRate()));
        JSONObject params = JSONObject.parseObject(response.getData().getIfParams());
        assertEquals("1234******ghij", params.getString("privateKey"));
        assertEquals("ABCDEF******UVWXYZ", params.getString("alipayPublicKey"));
        assertEquals("provider-app", params.getString("appId"));
        assertEquals("RSA2", params.getString("signType"));
        verifyConfigurationReadAfterOwnership();
    }

    @Test
    void ownApplicationWithNoConfigurationReturnsSuccessfulNull() {
        ownApplication();

        ApiRes<PayInterfaceConfig> response = controller.getByMchNo(OWN_APP_ID, IF_CODE);

        assertEquals(ApiCodeEnum.SUCCESS.getCode(), response.getCode());
        assertNull(response.getData());
        verifyConfigurationReadAfterOwnership();
        verifyNoInteractions(mchInfoService);
    }

    @Test
    void ownApplicationPreservesAbsentRateAndBlankParameters() {
        ownApplication();
        PayInterfaceConfig config = config(null, "");
        when(payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, OWN_APP_ID, IF_CODE)).thenReturn(config);

        ApiRes<PayInterfaceConfig> response = controller.getByMchNo(OWN_APP_ID, IF_CODE);

        assertEquals(ApiCodeEnum.SUCCESS.getCode(), response.getCode());
        assertNull(response.getData().getIfRate());
        assertEquals("", response.getData().getIfParams());
        verifyConfigurationReadAfterOwnership();
        verifyNoInteractions(mchInfoService);
    }

    @Test
    void ownIsvSubMerchantConfigurationPreservesExistingParameterBehavior() {
        ownApplication();
        when(mchInfoService.getById(CURRENT_MCH_NO)).thenReturn(merchant(CS.MCH_TYPE_ISVSUB));
        String params = "{\"subMchId\":\"test-sub-merchant\"}";
        PayInterfaceConfig config = config(new BigDecimal("0.0100"), params);
        when(payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, OWN_APP_ID, IF_CODE)).thenReturn(config);

        ApiRes<PayInterfaceConfig> response = controller.getByMchNo(OWN_APP_ID, IF_CODE);

        assertEquals(ApiCodeEnum.SUCCESS.getCode(), response.getCode());
        assertEquals(0, BigDecimal.ONE.compareTo(response.getData().getIfRate()));
        assertEquals(params, response.getData().getIfParams());
        verifyConfigurationReadAfterOwnership();
    }

    private void ownApplication() {
        lenient().when(mchAppService.getById(OWN_APP_ID)).thenReturn(application(OWN_APP_ID, CURRENT_MCH_NO));
    }

    private void verifyConfigurationReadAfterOwnership() {
        var ordered = inOrder(mchAppService, payInterfaceConfigService);
        ordered.verify(mchAppService).getById(OWN_APP_ID);
        ordered.verify(payInterfaceConfigService).getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, OWN_APP_ID, IF_CODE);
    }

    private static PayInterfaceConfig config(BigDecimal rate, String params) {
        PayInterfaceConfig config = new PayInterfaceConfig();
        config.setInfoType(CS.INFO_TYPE_MCH_APP);
        config.setInfoId(OWN_APP_ID);
        config.setIfCode(IF_CODE);
        config.setIfRate(rate);
        config.setIfParams(params);
        return config;
    }

    private static MchInfo merchant(byte type) {
        MchInfo merchant = new MchInfo();
        merchant.setMchNo(CURRENT_MCH_NO);
        merchant.setType(type);
        return merchant;
    }
}
