package com.jeequan.jeepay.mch.ctrl;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.cache.ITokenService;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentUserControllerAuthorizationTest extends MerchantControllerTestSupport {

    private static final String ORIGINAL_PASSWORD = "original-password";
    private static final String NEW_PASSWORD = "replacement-password";

    @Mock
    private SysUserAuthService sysUserAuthService;

    @InjectMocks
    private CurrentUserController controller;

    private MockedStatic<ITokenService> tokens;

    @BeforeEach
    void preventRealTokenAccess() {
        // 保留真实 logout() 路径，直接验证当前登录令牌被撤销。
        tokens = mockStatic(ITokenService.class);
    }

    @AfterEach
    void closeTokenMock() {
        tokens.close();
    }

    @ParameterizedTest(name = "拒绝修改其他用户密码：{1}")
    @CsvSource({"1002, MCH_OWN", "2001, MCH_OTHER"})
    void rejectsAnotherUserBeforeCheckingPasswords(Long targetId, String targetMerchant) {
        SysUser target = user(targetId, targetMerchant);
        requestPasswordChange(target.getSysUserId(), ORIGINAL_PASSWORD, NEW_PASSWORD);
        lenient().when(sysUserAuthService.validateCurrentUserPwd(ORIGINAL_PASSWORD)).thenReturn(true);

        BizException error = assertThrows(BizException.class, controller::modifyPwd);

        assertEquals(ApiCodeEnum.SYS_PERMISSION_ERROR.getCode(), error.getApiRes().getCode());
        verifyNoInteractions(sysUserAuthService);
        tokens.verifyNoInteractions();
    }

    @Test
    void requiresRecordIdBeforeCheckingPasswords() {
        requestPasswordChange(null, ORIGINAL_PASSWORD, NEW_PASSWORD);

        BizException error = assertThrows(BizException.class, controller::modifyPwd);

        assertEquals(ApiCodeEnum.PARAMS_ERROR.getCode(), error.getApiRes().getCode());
        assertTrue(error.getApiRes().getMsg().contains("recordId"));
        verifyNoInteractions(sysUserAuthService);
        tokens.verifyNoInteractions();
    }

    @Test
    void updatesOnlyCurrentUserAndLogsOutAfterSuccessfulReset() {
        requestPasswordChange(CURRENT_USER_ID, ORIGINAL_PASSWORD, NEW_PASSWORD);
        when(sysUserAuthService.validateCurrentUserPwd(ORIGINAL_PASSWORD)).thenReturn(true);
        doAnswer(invocation -> {
            // 密码重置成功之前不能撤销当前会话。
            tokens.verifyNoInteractions();
            return null;
        }).when(sysUserAuthService).resetAuthInfo(CURRENT_USER_ID, null, null, NEW_PASSWORD, CS.SYS_TYPE.MCH);

        ApiRes<?> response = controller.modifyPwd();

        assertEquals(ApiCodeEnum.SUCCESS.getCode(), response.getCode());
        var ordered = inOrder(sysUserAuthService);
        ordered.verify(sysUserAuthService).validateCurrentUserPwd(ORIGINAL_PASSWORD);
        ordered.verify(sysUserAuthService).resetAuthInfo(CURRENT_USER_ID, null, null, NEW_PASSWORD, CS.SYS_TYPE.MCH);
        verifyNoMoreInteractions(sysUserAuthService);
        tokens.verify(() -> ITokenService.removeIToken(CACHE_KEY, CURRENT_USER_ID));
        tokens.verifyNoMoreInteractions();
    }

    @Test
    void wrongOriginalPasswordDoesNotResetOrLogOut() {
        requestPasswordChange(CURRENT_USER_ID, ORIGINAL_PASSWORD, NEW_PASSWORD);
        when(sysUserAuthService.validateCurrentUserPwd(ORIGINAL_PASSWORD)).thenReturn(false);

        BizException error = assertThrows(BizException.class, controller::modifyPwd);

        assertEquals("原密码验证失败！", error.getMessage());
        verify(sysUserAuthService).validateCurrentUserPwd(ORIGINAL_PASSWORD);
        verifyNoMoreInteractions(sysUserAuthService);
        tokens.verifyNoInteractions();
    }

    @Test
    void unchangedPasswordDoesNotResetOrLogOut() {
        requestPasswordChange(CURRENT_USER_ID, ORIGINAL_PASSWORD, ORIGINAL_PASSWORD);
        when(sysUserAuthService.validateCurrentUserPwd(ORIGINAL_PASSWORD)).thenReturn(true);

        BizException error = assertThrows(BizException.class, controller::modifyPwd);

        assertEquals("新密码与原密码不能相同！", error.getMessage());
        verify(sysUserAuthService).validateCurrentUserPwd(ORIGINAL_PASSWORD);
        verifyNoMoreInteractions(sysUserAuthService);
        tokens.verifyNoInteractions();
    }

    private void requestPasswordChange(Long recordId, String original, String replacement) {
        JSONObject body = new JSONObject();
        if (recordId != null) {
            body.put("recordId", recordId);
        }
        body.put("originalPwd", Base64.encode(original));
        body.put("confirmPwd", Base64.encode(replacement));
        when(requestKitBean.getReqParamJSON()).thenReturn(body);
    }
}
