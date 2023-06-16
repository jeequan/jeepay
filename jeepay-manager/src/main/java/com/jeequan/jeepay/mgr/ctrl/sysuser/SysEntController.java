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
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.entity.SysEntitlement;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.TreeDataBuilder;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.SysEntitlementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
* 权限 菜单 管理
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:13
*/
@Api(tags = "系统管理（用户权限）")
@RestController
@RequestMapping("api/sysEnts")
public class SysEntController extends CommonCtrl {

	@Autowired SysEntitlementService sysEntitlementService;


	/** getOne */
	@ApiOperation("查询菜单权限详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
			@ApiImplicitParam(name = "entId", value = "权限ID[ENT_功能模块_子模块_操作], eg: ENT_ROLE_LIST_ADD", required = true),
			@ApiImplicitParam(name = "sysType", value = "所属系统： MGR-运营平台, MCH-商户中心", required = true)
	})
	@PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_ENT_LIST' )")
	@RequestMapping(value="/bySysType", method = RequestMethod.GET)
	public ApiRes bySystem() {

		return ApiRes.ok(sysEntitlementService.getOne(SysEntitlement.gw()
				.eq(SysEntitlement::getEntId, getValStringRequired("entId"))
				.eq(SysEntitlement::getSysType, getValStringRequired("sysType")))
		);
	}

	/** updateById */
	@ApiOperation("更新权限资源")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
			@ApiImplicitParam(name = "entId", value = "权限ID[ENT_功能模块_子模块_操作], eg: ENT_ROLE_LIST_ADD", required = true),
			@ApiImplicitParam(name = "entName", value = "权限名称", required = true),
			@ApiImplicitParam(name = "menuUri", value = "菜单uri/路由地址"),
			@ApiImplicitParam(name = "entSort", value = "排序字段, 规则：正序"),
			@ApiImplicitParam(name = "quickJump", value = "快速开始菜单 0-否, 1-是"),
			@ApiImplicitParam(name = "state", value = "状态 0-停用, 1-启用")
	})
	@PreAuthorize("hasAuthority( 'ENT_UR_ROLE_ENT_EDIT')")
	@MethodLog(remark = "更新资源权限")
	@RequestMapping(value="/{entId}", method = RequestMethod.PUT)
	public ApiRes updateById(@PathVariable("entId") String entId) {

		SysEntitlement queryObject = getObject(SysEntitlement.class);
		sysEntitlementService.update(queryObject, SysEntitlement.gw().eq(SysEntitlement::getEntId, entId).eq(SysEntitlement::getSysType, queryObject.getSysType()));
		return ApiRes.ok();
	}


	/** 查询权限集合 */
	@ApiOperation("查询权限集合")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
			@ApiImplicitParam(name = "sysType", value = "所属系统： MGR-运营平台, MCH-商户中心", required = true)
	})
	@PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_ENT_LIST', 'ENT_UR_ROLE_DIST' )")
	@RequestMapping(value="/showTree", method = RequestMethod.GET)
	public ApiRes<List<JSONObject>> showTree() {

		//查询全部数据
		List<SysEntitlement> list = sysEntitlementService.list(SysEntitlement.gw().eq(SysEntitlement::getSysType, getValStringRequired("sysType")));

		//转换为json树状结构
		JSONArray jsonArray = (JSONArray) JSONArray.toJSON(list);
		List<JSONObject> leftMenuTree = new TreeDataBuilder(jsonArray,
				"entId", "pid", "children", "entSort", true)
				.buildTreeObject();

		return ApiRes.ok(leftMenuTree);
	}
}
