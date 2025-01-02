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
package com.jeequan.jeepay.mch.ctrl.sysuser;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.SysRole;
import com.jeequan.jeepay.core.entity.SysRoleEntRela;
import com.jeequan.jeepay.core.entity.SysUserRoleRela;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.mch.service.AuthService;
import com.jeequan.jeepay.service.impl.SysRoleEntRelaService;
import com.jeequan.jeepay.service.impl.SysRoleService;
import com.jeequan.jeepay.service.impl.SysUserRoleRelaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色权限管理类
 *
 * @author terrfly
 * @modify zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Tag(name = "系统管理（用户-角色-权限关联信息）")
@RestController
@RequestMapping("api/sysRoleEntRelas")
public class SysRoleEntRelaController extends CommonCtrl {

	@Autowired private SysRoleEntRelaService sysRoleEntRelaService;
	@Autowired private SysUserRoleRelaService sysUserRoleRelaService;
	@Autowired private SysRoleService sysRoleService;
	@Autowired private AuthService authService;

	/** list */
	@Operation(summary = "关联关系--角色-权限关联信息列表")
	@Parameters({
			@Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
			@Parameter(name = "pageNumber", description = "分页页码"),
			@Parameter(name = "pageSize", description = "分页条数（-1时查全部数据）"),
			@Parameter(name = "roleId", description = "角色ID, ROLE_开头")
	})
	@PreAuthorize("hasAuthority( 'ENT_UR_ROLE_DIST' )")
	@RequestMapping(value="", method = RequestMethod.GET)
	public ApiPageRes<SysRoleEntRela> list() {

		SysRoleEntRela queryObject = getObject(SysRoleEntRela.class);

		LambdaQueryWrapper<SysRoleEntRela> condition = SysRoleEntRela.gw();

		if(queryObject.getRoleId() != null){
			condition.eq(SysRoleEntRela::getRoleId, queryObject.getRoleId());
		}

		IPage<SysRoleEntRela> pages = sysRoleEntRelaService.page(getIPage(true), condition);

		return ApiPageRes.pages(pages);
	}

	/** 重置角色权限关联信息 */
	@PreAuthorize("hasAuthority( 'ENT_UR_ROLE_DIST' )")
	@RequestMapping(value="relas/{roleId}", method = RequestMethod.POST)
	public ApiRes relas(@PathVariable("roleId") String roleId) {

		SysRole sysRole = sysRoleService.getOne(SysRole.gw().eq(SysRole::getRoleId, roleId).eq(SysRole::getBelongInfoId, getCurrentMchNo()));
		if (sysRole == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

		List<String> entIdList = JSONArray.parseArray(getValStringRequired("entIdListStr"), String.class);

		sysRoleEntRelaService.resetRela(roleId, entIdList);

		List<Long> sysUserIdList = new ArrayList<>();
		sysUserRoleRelaService.list(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, roleId)).stream().forEach(item -> sysUserIdList.add(item.getUserId()));

		//查询到该角色的人员， 将redis更新
		authService.refAuthentication(sysUserIdList);

		return ApiRes.ok();
	}

}
