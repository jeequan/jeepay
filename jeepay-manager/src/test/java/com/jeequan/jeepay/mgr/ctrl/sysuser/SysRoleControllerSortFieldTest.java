package com.jeequan.jeepay.mgr.ctrl.sysuser;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.beans.RequestKitBean;
import com.jeequan.jeepay.core.entity.SysRole;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.impl.SysRoleService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 角色列表排序参数回归测试。
 * <p>
 * 角色列表接口（GET /api/sysRoles）的排序字段来自前端请求参数，最终会被拼接进 ORDER BY 语句，
 * 因此必须限制在角色表字段白名单内，且不能携带 SQL 元字符。
 */
class SysRoleControllerSortFieldTest {

    private SysRoleController controller;
    private SysRoleService sysRoleService;

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        // 单元测试不启动应用，需要手工初始化实体与表字段的映射关系
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SysRole.class);
    }

    @BeforeEach
    void setUp() {
        sysRoleService = mock(SysRoleService.class);
        doReturn(new Page<>(1, 20)).when(sysRoleService).page(any(IPage.class), any(Wrapper.class));

        controller = new SysRoleController();
        ReflectionTestUtils.setField(controller, "sysRoleService", sysRoleService);
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void rejectsSortFieldCarryingSqlMetaCharacters() {
        bindRequestParams("sortField", "role_name;select 1", "sortOrder", "descend");

        BizException bizException = assertThrows(BizException.class, () -> controller.list());
        assertThat(bizException.getApiRes().getMsg()).contains("排序字段");
        verify(sysRoleService, never()).page(any(IPage.class), any(Wrapper.class));
    }

    @Test
    void sortsByAllowlistedColumnWithRequestedDirection() {
        bindRequestParams("sortField", "roleName", "sortOrder", "ascend");

        controller.list();

        assertThat(capturedOrderBy()).isEqualTo("ORDER BY role_name ASC");
    }

    @Test
    void fallsBackToDefaultOrderWhenSortFieldIsNotAllowlisted() {
        bindRequestParams("sortField", "sysType", "sortOrder", "ascend");

        controller.list();

        assertThat(capturedOrderBy()).isEqualTo("ORDER BY updated_at DESC");
    }

    @Test
    void usesDefaultOrderWhenNoSortFieldIsGiven() {
        bindRequestParams("pageNumber", "1", "pageSize", "20");

        controller.list();

        assertThat(capturedOrderBy()).isEqualTo("ORDER BY updated_at DESC");
    }

    /** 取出实际拼接到 SQL 中的排序片段 */
    private String capturedOrderBy() {
        ArgumentCaptor<Wrapper<SysRole>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(sysRoleService).page(any(IPage.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        int orderByIndex = sqlSegment.indexOf("ORDER BY");
        assertThat(orderByIndex).as("查询语句应包含排序片段: %s", sqlSegment).isGreaterThanOrEqualTo(0);
        return sqlSegment.substring(orderByIndex).trim();
    }

    private void bindRequestParams(String... nameValuePairs) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sysRoles");
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            request.setParameter(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestKitBean requestKit = new RequestKitBean();
        ReflectionTestUtils.setField(requestKit, "request", request);
        ReflectionTestUtils.setField(controller, "requestKitBean", requestKit);
    }
}
