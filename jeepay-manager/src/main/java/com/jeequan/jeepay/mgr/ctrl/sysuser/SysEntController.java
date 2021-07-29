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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.jeequan.jeepay.core.entity.SysEntitlement;
import com.jeequan.jeepay.service.impl.SysEntitlementService;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.TreeDataBuilder;

import java.util.List;

/*
* 权限 菜单 管理
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:13
*/
@RestController
@RequestMapping("api/sysEnts")
public class SysEntController extends CommonCtrl {

	@Autowired SysEntitlementService sysEntitlementService;


	/** getOne */
	@PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_ENT_LIST' )")
	@RequestMapping(value="/bySysType", method = RequestMethod.GET)
	public ApiRes bySystem() {

		return ApiRes.ok(sysEntitlementService.getOne(SysEntitlement.gw()
				.eq(SysEntitlement::getEntId, getValStringRequired("entId"))
				.eq(SysEntitlement::getSysType, getValStringRequired("sysType")))
		);
	}

	/** updateById */
	@PreAuthorize("hasAuthority( 'ENT_UR_ROLE_ENT_EDIT')")
	@MethodLog(remark = "更新资源权限")
	@RequestMapping(value="/{entId}", method = RequestMethod.PUT)
	public ApiRes updateById(@PathVariable("entId") String entId) {

		SysEntitlement queryObject = getObject(SysEntitlement.class);
		sysEntitlementService.update(queryObject, SysEntitlement.gw().eq(SysEntitlement::getEntId, entId).eq(SysEntitlement::getSysType, queryObject.getSysType()));
		return ApiRes.ok();
	}


	/** 查询权限集合 */
	@PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_ENT_LIST', 'ENT_UR_ROLE_DIST' )")
	@RequestMapping(value="/showTree", method = RequestMethod.GET)
	public ApiRes showTree() {

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
