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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.SysRoleEntRela;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.mgr.service.AuthService;
import com.jeequan.jeepay.service.impl.SysRoleEntRelaService;
import com.jeequan.jeepay.service.impl.SysUserRoleRelaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/*
* 角色 权限管理
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:13
*/
@RestController
@RequestMapping("api/sysRoleEntRelas")
public class SysRoleEntRelaController extends CommonCtrl {

	@Autowired private SysRoleEntRelaService sysRoleEntRelaService;

	/** list */
	@PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_ADD', 'ENT_UR_ROLE_DIST' )")
	@RequestMapping(value="", method = RequestMethod.GET)
	public ApiRes list() {

		SysRoleEntRela queryObject = getObject(SysRoleEntRela.class);

		LambdaQueryWrapper<SysRoleEntRela> condition = SysRoleEntRela.gw();

		if(queryObject.getRoleId() != null){
			condition.eq(SysRoleEntRela::getRoleId, queryObject.getRoleId());
		}

		IPage<SysRoleEntRela> pages = sysRoleEntRelaService.page(getIPage(true), condition);

		return ApiRes.page(pages);
	}

}
