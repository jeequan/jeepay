package com.jeequan.jeepay.mch.ctrl.transfer;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.entity.PayInterfaceDefine;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.MerchantControllerTestSupport;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.PayInterfaceConfigService;
import com.jeequan.jeepay.service.impl.PayInterfaceDefineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MchTransferControllerAuthorizationTest extends MerchantControllerTestSupport {

    @Mock
    private MchAppService mchAppService;
    @Mock
    private PayInterfaceConfigService payInterfaceConfigService;
    @Mock
    private PayInterfaceDefineService payInterfaceDefineService;
    @InjectMocks
    private MchTransferController controller;
    @Captor
    private ArgumentCaptor<LambdaQueryWrapper<PayInterfaceConfig>> configQuery;
    @Captor
    private ArgumentCaptor<LambdaQueryWrapper<PayInterfaceDefine>> defineQuery;

    @ParameterizedTest(name = "拒绝读取非本商户或不存在应用的转账接口：{0}")
    @ValueSource(strings = {FOREIGN_APP_ID, MISSING_APP_ID})
    void rejectsForeignOrMissingApplicationBeforeReadingConfigurationOrDefinitions(String appId) {
        if (FOREIGN_APP_ID.equals(appId)) {
            lenient().when(mchAppService.getById(appId)).thenReturn(application(appId, FOREIGN_MCH_NO));
        }

        assertThrows(BizException.class, () -> controller.ifCodeList(appId));

        verify(mchAppService).getById(appId);
        verifyNoInteractions(payInterfaceConfigService, payInterfaceDefineService);
    }

    @Test
    void ownApplicationReturnsExistingDefinitionsUsingOnlyEnabledApplicationConfigurations() {
        ownApplication();
        PayInterfaceConfig alipay = new PayInterfaceConfig().setIfCode("alipay");
        PayInterfaceConfig wxpay = new PayInterfaceConfig().setIfCode("wxpay");
        when(payInterfaceConfigService.list(ArgumentMatchers.<Wrapper<PayInterfaceConfig>>any())).thenReturn(List.of(alipay, wxpay));
        List<PayInterfaceDefine> definitions = List.of(
                new PayInterfaceDefine().setIfCode("alipay"),
                new PayInterfaceDefine().setIfCode("wxpay"));
        when(payInterfaceDefineService.list(ArgumentMatchers.<Wrapper<PayInterfaceDefine>>any())).thenReturn(definitions);

        ApiRes<List<PayInterfaceDefine>> response = controller.ifCodeList(OWN_APP_ID);

        assertEquals(ApiCodeEnum.SUCCESS.getCode(), response.getCode());
        assertSame(definitions, response.getData());
        var ordered = inOrder(mchAppService, payInterfaceConfigService, payInterfaceDefineService);
        ordered.verify(mchAppService).getById(OWN_APP_ID);
        ordered.verify(payInterfaceConfigService).list(configQuery.capture());
        ordered.verify(payInterfaceDefineService).list(defineQuery.capture());
        assertConfigurationScope(configQuery.getValue());
        LambdaQueryWrapper<PayInterfaceDefine> query = defineQuery.getValue();
        assertTrue(query.getSqlSegment().contains("if_code IN"));
        assertEquals(2, query.getParamNameValuePairs().size());
        assertTrue(query.getParamNameValuePairs().containsValue("alipay"));
        assertTrue(query.getParamNameValuePairs().containsValue("wxpay"));
        verifyNoMoreInteractions(payInterfaceConfigService, payInterfaceDefineService);
    }

    @Test
    void ownApplicationWithoutConfigurationReturnsEmptyListWithoutReadingDefinitions() {
        ownApplication();
        when(payInterfaceConfigService.list(ArgumentMatchers.<Wrapper<PayInterfaceConfig>>any())).thenReturn(Collections.emptyList());

        ApiRes<List<PayInterfaceDefine>> response = controller.ifCodeList(OWN_APP_ID);

        assertEquals(ApiCodeEnum.SUCCESS.getCode(), response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
        var ordered = inOrder(mchAppService, payInterfaceConfigService);
        ordered.verify(mchAppService).getById(OWN_APP_ID);
        ordered.verify(payInterfaceConfigService).list(configQuery.capture());
        assertConfigurationScope(configQuery.getValue());
        verifyNoInteractions(payInterfaceDefineService);
    }

    private void ownApplication() {
        lenient().when(mchAppService.getById(OWN_APP_ID)).thenReturn(application(OWN_APP_ID, CURRENT_MCH_NO));
    }

    private static void assertConfigurationScope(LambdaQueryWrapper<PayInterfaceConfig> query) {
        assertEquals("if_code", query.getSqlSelect());
        String sql = query.getSqlSegment();
        assertTrue(sql.contains("info_type ="));
        assertTrue(sql.contains("info_id ="));
        assertTrue(sql.contains("state ="));
        assertEquals(3, query.getParamNameValuePairs().size());
        assertTrue(query.getParamNameValuePairs().containsValue(CS.INFO_TYPE_MCH_APP));
        assertTrue(query.getParamNameValuePairs().containsValue(OWN_APP_ID));
        assertTrue(query.getParamNameValuePairs().containsValue(CS.PUB_USABLE));
    }
}
