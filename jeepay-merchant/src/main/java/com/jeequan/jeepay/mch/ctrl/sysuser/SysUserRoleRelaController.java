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
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.entity.SysUserRoleRela;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.mch.service.AuthService;
import com.jeequan.jeepay.service.impl.SysUserRoleRelaService;
import com.jeequan.jeepay.service.impl.SysUserService;
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

import java.util.Arrays;
import java.util.List;

/**
 * 用户角色管理类
 *
 * @author terrfly
 * @modify zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Tag(name = "系统管理（用户-角色-权限关联信息）")
@RestController
@RequestMapping("api/sysUserRoleRelas")
public class SysUserRoleRelaController extends CommonCtrl {

	@Autowired private SysUserRoleRelaService sysUserRoleRelaService;
	@Autowired private SysUserService sysUserService;
	@Autowired private AuthService authService;

	/** list */
	@Operation(summary = "关联关系--用户-角色关联信息列表")
	@Parameters({
			@Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
			@Parameter(name = "pageNumber", description = "分页页码"),
			@Parameter(name = "pageSize", description = "分页条数（-1时查全部数据）"),
			@Parameter(name = "userId", description = "用户ID")
	})
	@PreAuthorize("hasAuthority( 'ENT_UR_USER_UPD_ROLE' )")
	@RequestMapping(value="", method = RequestMethod.GET)
	public ApiPageRes<SysUserRoleRela> list() {

		SysUserRoleRela queryObject = getObject(SysUserRoleRela.class);

		LambdaQueryWrapper<SysUserRoleRela> condition = SysUserRoleRela.gw();

		if(queryObject.getUserId() != null){
			condition.eq(SysUserRoleRela::getUserId, queryObject.getUserId());
		}

		IPage<SysUserRoleRela> pages = sysUserRoleRelaService.page(getIPage(true), condition);

		return ApiPageRes.pages( pages);
	}

	/** 重置用户角色关联信息 */
	@Operation(summary = "更改用户角色信息")
	@Parameters({
			@Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
			@Parameter(name = "sysUserId", description = "用户ID", required = true),
			@Parameter(name = "roleIdListStr", description = "角色信息，eg：[str1,str2]，字符串列表转成json字符串", required = true)
	})
	@PreAuthorize("hasAuthority( 'ENT_UR_USER_UPD_ROLE' )")
	@RequestMapping(value="relas/{sysUserId}", method = RequestMethod.POST)
	public ApiRes relas(@PathVariable("sysUserId") Long sysUserId) {
		SysUser dbRecord = sysUserService.getOne(SysUser.gw().eq(SysUser::getSysUserId, sysUserId).eq(SysUser::getBelongInfoId, getCurrentMchNo()));
		if (dbRecord == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

		List<String> roleIdList = JSONArray.parseArray(getValStringRequired("roleIdListStr"), String.class);

		sysUserService.saveUserRole(sysUserId, roleIdList);

		authService.refAuthentication(Arrays.asList(sysUserId));

		return ApiRes.ok();
	}


}
