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
package com.jeequan.jeepay.mgr.ctrl.sysuser;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysRole;
import com.jeequan.jeepay.core.entity.SysUserRoleRela;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.mgr.service.AuthService;
import com.jeequan.jeepay.service.impl.SysRoleEntRelaService;
import com.jeequan.jeepay.service.impl.SysRoleService;
import com.jeequan.jeepay.service.impl.SysUserRoleRelaService;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/*
* 角色管理
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:13
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

		QueryWrapper<SysRole> condition = new QueryWrapper<>();
		LambdaQueryWrapper<SysRole> lambdaCondition = condition.lambda();
		lambdaCondition.eq(SysRole::getSysType, CS.SYS_TYPE.MGR);
		lambdaCondition.eq(SysRole::getBelongInfoId, 0);

		if(StringUtils.isNotEmpty(queryObject.getRoleName())){
			lambdaCondition.like(SysRole::getRoleName, queryObject.getRoleName());
		}

		if(StringUtils.isNotEmpty(queryObject.getRoleId())){
			lambdaCondition.like(SysRole::getRoleId, queryObject.getRoleId());
		}

		//是否有排序字段
		MutablePair<Boolean, String> orderInfo = getSortInfo();
		if(orderInfo != null){
			condition.orderBy(true, orderInfo.getLeft(), orderInfo.getRight());
		}else{
			lambdaCondition.orderByDesc(SysRole::getUpdatedAt);
		}

		IPage<SysRole> pages = sysRoleService.page(getIPage(true), condition);
		return ApiRes.page(pages);
	}


	/** detail */
	@PreAuthorize("hasAuthority( 'ENT_UR_ROLE_EDIT' )")
	@RequestMapping(value="/{recordId}", method = RequestMethod.GET)
	public ApiRes detail(@PathVariable("recordId") String recordId) {
		return ApiRes.ok(sysRoleService.getById(recordId));
	}

	/** add */
	@PreAuthorize("hasAuthority( 'ENT_UR_ROLE_ADD' )")
	@MethodLog(remark = "添加角色信息")
	@RequestMapping(value="", method = RequestMethod.POST)
	public ApiRes add() {
		SysRole SysRole = getObject(SysRole.class);
		String roleId = "ROLE_" + StringKit.getUUID(6);
		SysRole.setRoleId(roleId);
		SysRole.setSysType(CS.SYS_TYPE.MGR); //后台系统
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
	@MethodLog(remark = "更新角色信息")
	public ApiRes update(@PathVariable("recordId") String recordId) {
		SysRole SysRole = getObject(SysRole.class);
		SysRole.setRoleId(recordId);
		sysRoleService.updateById(SysRole);

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
	@MethodLog(remark = "删除角色")
	@RequestMapping(value="/{recordId}", method = RequestMethod.DELETE)
	public ApiRes del(@PathVariable("recordId") String recordId) {

		if(sysUserRoleRelaService.count(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, recordId)) > 0){
			throw new BizException("当前角色已分配到用户, 不可删除！");
		}
		sysRoleService.removeRole(recordId);
		return ApiRes.ok();
	}

}
