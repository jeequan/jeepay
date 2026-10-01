package com.jeequan.jeepay.mch.ctrl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jeequan.jeepay.core.beans.RequestKitBean;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.entity.PayInterfaceDefine;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/** 仅初始化查询元数据和真实认证上下文，不启动 Spring、数据库或支付客户端。 */
@ExtendWith(MockitoExtension.class)
public abstract class MerchantControllerTestSupport {

    protected static final Long CURRENT_USER_ID = 1001L;
    protected static final String CURRENT_MCH_NO = "MCH_OWN";
    protected static final String FOREIGN_MCH_NO = "MCH_OTHER";
    protected static final String OWN_APP_ID = "APP_OWN";
    protected static final String FOREIGN_APP_ID = "APP_OTHER";
    protected static final String MISSING_APP_ID = "APP_MISSING";
    protected static final String CACHE_KEY = "test-current-merchant-token";

    @Mock
    protected RequestKitBean requestKitBean;

    @BeforeAll
    protected static void initializeQueryMetadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "merchant-authorization-tests");
        assistant.setCurrentNamespace("merchant.authorization.tests");
        TableInfoHelper.initTableInfo(assistant, MchApp.class);
        TableInfoHelper.initTableInfo(assistant, PayInterfaceConfig.class);
        TableInfoHelper.initTableInfo(assistant, PayInterfaceDefine.class);
    }

    @BeforeEach
    protected void authenticateCurrentMerchant() {
        SysUser sysUser = user(CURRENT_USER_ID, CURRENT_MCH_NO);
        JeeUserDetails principal = new JeeUserDetails(sysUser, "test-credential");
        principal.setCacheKey(CACHE_KEY);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    protected void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    protected static SysUser user(Long userId, String mchNo) {
        SysUser sysUser = new SysUser();
        sysUser.setSysUserId(userId);
        sysUser.setBelongInfoId(mchNo);
        sysUser.setSysType(CS.SYS_TYPE.MCH);
        return sysUser;
    }

    protected static MchApp application(String appId, String mchNo) {
        MchApp app = new MchApp();
        app.setAppId(appId);
        app.setMchNo(mchNo);
        app.setState(CS.YES);
        return app;
    }
}
