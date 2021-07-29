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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysRole;
import com.jeequan.jeepay.core.entity.SysUserRoleRela;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.mch.service.AuthService;
import com.jeequan.jeepay.service.impl.SysRoleEntRelaService;
import com.jeequan.jeepay.service.impl.SysRoleService;
import com.jeequan.jeepay.service.impl.SysUserRoleRelaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色管理类
 *
 * @author terrfly
 * @modify zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@RestController
@RequestMapping("api/sysRoles")
public class SysRoleController extends CommonCtrl {

	@Autowired SysRoleService sysRoleService;
	@Autowired SysUserRoleRelaService sysUserRoleRelaService;
	@Autowired private AuthService authService;
	@Autowired private SysRoleEntRelaService sysRoleEntRelaService;


	/** list */
	@PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_LIST', 'ENT_UR_USER_UPD_ROLE' )")
	@RequestMapping(value="", method = RequestMethod.GET)
	public ApiRes list() {

		SysRole queryObject = getObject(SysRole.class);

		LambdaQueryWrapper<SysRole> condition = SysRole.gw();
		condition.eq(SysRole::getSysType, CS.SYS_TYPE.MCH);
		condition.eq(SysRole::getBelongInfoId, getCurrentMchNo());

		if(StringUtils.isNotEmpty(queryObject.getRoleName())){
			condition.like(SysRole::getRoleName, queryObject.getRoleName());
		}

		if(StringUtils.isNotEmpty(queryObject.getRoleId())){
			condition.like(SysRole::getRoleId, queryObject.getRoleId());
		}

		condition.orderByDesc(SysRole::getUpdatedAt); //时间倒序

		IPage<SysRole> pages = sysRoleService.page(getIPage(true), condition);
		return ApiRes.page(pages);
	}


	/** detail */
	@PreAuthorize("hasAuthority( 'ENT_UR_ROLE_EDIT' )")
	@RequestMapping(value="/{recordId}", method = RequestMethod.GET)
	public ApiRes detail(@PathVariable("recordId") String recordId) {
		SysRole sysRole = sysRoleService.getOne(SysRole.gw().eq(SysRole::getRoleId, recordId).eq(SysRole::getBelongInfoId, getCurrentMchNo()));
		if (sysRole == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
		return ApiRes.ok(sysRole);
	}

	/** add */
	@PreAuthorize("hasAuthority( 'ENT_UR_ROLE_ADD' )")
	@RequestMapping(value="", method = RequestMethod.POST)
	public ApiRes add() {
		SysRole SysRole = getObject(SysRole.class);
		String roleId = "ROLE_" + StringKit.getUUID(6);
		SysRole.setRoleId(roleId);
		SysRole.setSysType(CS.SYS_TYPE.MCH); //后台系统
		SysRole.setBelongInfoId(getCurrentUser().getSysUser().getBelongInfoId());
		sysRoleService.save(SysRole);

		//权限信息集合
		String entIdListStr =  getValString("entIdListStr");

		//如果包含： 可分配权限的权限 && entIdListStr 不为空
		if(getCurrentUser().getAuthorities().contains(new SimpleGrantedAuthority("ENT_UR_ROLE_DIST"))
				&& StringUtils.isNotEmpty(entIdListStr)){
			List<String> entIdList = JSONArray.parseArray(entIdListStr, String.class);

			sysRoleEntRelaService.resetRela(roleId, entIdList);
		}

		return ApiRes.ok();
	}

	/** update */
	@PreAuthorize("hasAuthority( 'ENT_UR_ROLE_EDIT' )")
	@RequestMapping(value="/{recordId}", method = RequestMethod.PUT)
	public ApiRes update(@PathVariable("recordId") String recordId) {

		SysRole sysRole = getObject(SysRole.class);

		LambdaUpdateWrapper<SysRole> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(SysRole::getRoleId, recordId);
		updateWrapper.eq(SysRole::getBelongInfoId, getCurrentMchNo());
		sysRoleService.update(sysRole, updateWrapper);

		//权限信息集合
		String entIdListStr =  getValString("entIdListStr");

		//如果包含： 可分配权限的权限 && entIdListStr 不为空
		if(getCurrentUser().getAuthorities().contains(new SimpleGrantedAuthority("ENT_UR_ROLE_DIST"))
				&& StringUtils.isNotEmpty(entIdListStr)){
			List<String> entIdList = JSONArray.parseArray(entIdListStr, String.class);

			sysRoleEntRelaService.resetRela(recordId, entIdList);

			List<Long> sysUserIdList = new ArrayList<>();
			sysUserRoleRelaService.list(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, recordId)).stream().forEach(item -> sysUserIdList.add(item.getUserId()));

			//查询到该角色的人员， 将redis更新
			authService.refAuthentication(sysUserIdList);
		}

		return ApiRes.ok();
	}

	/** delete */
	@PreAuthorize("hasAuthority('ENT_UR_ROLE_DEL')")
	@RequestMapping(value="/{recordId}", method = RequestMethod.DELETE)
	public ApiRes del(@PathVariable("recordId") String recordId) {
		SysRole sysRole = sysRoleService.getOne(SysRole.gw().eq(SysRole::getRoleId, recordId).eq(SysRole::getBelongInfoId, getCurrentMchNo()));
		if (sysRole == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

		if(sysUserRoleRelaService.count(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, recordId)) > 0){
			throw new BizException("当前角色已分配到用户， 不可删除！");
		}
		sysRoleService.removeRole(recordId);
		return ApiRes.ok();
	}

}
