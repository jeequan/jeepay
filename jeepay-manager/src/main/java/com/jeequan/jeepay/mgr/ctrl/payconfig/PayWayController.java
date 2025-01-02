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
package com.jeequan.jeepay.mgr.ctrl.payconfig;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayWay;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayWayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 支付方式管理类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Tag(name = "支付方式配置")
@RestController
@RequestMapping("api/payWays")
public class PayWayController extends CommonCtrl {

	@Autowired PayWayService payWayService;
	@Autowired MchPayPassageService mchPayPassageService;
	@Autowired PayOrderService payOrderService;

	/**
	 * @Author: ZhuXiao
	 * @Description: list
	 * @Date: 15:52 2021/4/27
	*/
	@Operation(summary = "支付方式--列表")
	@Parameters({
			@Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
			@Parameter(name = "pageNumber", description = "分页页码"),
			@Parameter(name = "pageSize", description = "分页条数（-1时查全部数据）"),
			@Parameter(name = "wayCode", description = "支付方式代码"),
			@Parameter(name = "wayName", description = "支付方式名称")
	})
	@PreAuthorize("hasAnyAuthority('ENT_PC_WAY_LIST', 'ENT_PAY_ORDER_SEARCH_PAY_WAY')")
	@GetMapping
	public ApiPageRes<PayWay> list() {

		PayWay queryObject = getObject(PayWay.class);

		LambdaQueryWrapper<PayWay> condition = PayWay.gw();
		if(StringUtils.isNotEmpty(queryObject.getWayCode())){
			condition.like(PayWay::getWayCode, queryObject.getWayCode());
		}
		if(StringUtils.isNotEmpty(queryObject.getWayName())){
			condition.like(PayWay::getWayName, queryObject.getWayName());
		}
		condition.orderByAsc(PayWay::getWayCode);

		IPage<PayWay> pages = payWayService.page(getIPage(true), condition);

		return ApiPageRes.pages(pages);
	}


	/**
	 * @Author: ZhuXiao
	 * @Description: detail
	 * @Date: 15:52 2021/4/27
	*/
	@Operation(summary = "支付方式--详情")
	@Parameters({
			@Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
			@Parameter(name = "wayCode", description = "支付方式代码", required = true)
	})
	@PreAuthorize("hasAnyAuthority('ENT_PC_WAY_VIEW', 'ENT_PC_WAY_EDIT')")
	@GetMapping("/{wayCode}")
	public ApiRes<PayWay> detail(@PathVariable("wayCode") String wayCode) {
		return ApiRes.ok(payWayService.getById(wayCode));
	}

	/**
	 * @Author: ZhuXiao
	 * @Description: add
	 * @Date: 15:52 2021/4/27
	*/
	@Operation(summary = "支付方式--新增")
	@Parameters({
			@Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
			@Parameter(name = "wayCode", description = "支付方式代码", required = true),
			@Parameter(name = "wayName", description = "支付方式名称", required = true)
	})
	@PreAuthorize("hasAuthority('ENT_PC_WAY_ADD')")
	@PostMapping
	@MethodLog(remark = "新增支付方式")
	public ApiRes add() {
		PayWay payWay = getObject(PayWay.class);

		if (payWayService.count(PayWay.gw().eq(PayWay::getWayCode, payWay.getWayCode())) > 0) {
			throw new BizException("支付方式代码已存在");
		}
		payWay.setWayCode(payWay.getWayCode().toUpperCase());

		boolean result = payWayService.save(payWay);
		if (!result) {
			return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
		}
		return ApiRes.ok();
	}

	/**
	 * @Author: ZhuXiao
	 * @Description: update
	 * @Date: 15:52 2021/4/27
	*/
	@Operation(summary = "支付方式--更新")
	@Parameters({
			@Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
			@Parameter(name = "wayCode", description = "支付方式代码", required = true),
			@Parameter(name = "wayName", description = "支付方式名称", required = true)
	})
	@PreAuthorize("hasAuthority('ENT_PC_WAY_EDIT')")
	@PutMapping("/{wayCode}")
	@MethodLog(remark = "更新支付方式")
	public ApiRes update(@PathVariable("wayCode") String wayCode) {
		PayWay payWay = getObject(PayWay.class);
		payWay.setWayCode(wayCode);
		boolean result = payWayService.updateById(payWay);
		if (!result) {
			return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
		}
		return ApiRes.ok();
	}

	/**
	 * @Author: ZhuXiao
	 * @Description: delete
	 * @Date: 15:52 2021/4/27
	*/
	@Operation(summary = "支付方式--删除")
	@Parameters({
			@Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
			@Parameter(name = "wayCode", description = "支付方式代码", required = true)
	})
	@PreAuthorize("hasAuthority('ENT_PC_WAY_DEL')")
	@DeleteMapping("/{wayCode}")
	@MethodLog(remark = "删除支付方式")
	public ApiRes delete(@PathVariable("wayCode") String wayCode) {

		// 校验该支付方式是否有商户已配置通道或者已有订单
		if (mchPayPassageService.count(MchPayPassage.gw().eq(MchPayPassage::getWayCode, wayCode)) > 0
				|| payOrderService.count(PayOrder.gw().eq(PayOrder::getWayCode, wayCode)) > 0) {
			throw new BizException("该支付方式已有商户配置通道或已发生交易，无法删除！");
		}

		boolean result = payWayService.removeById(wayCode);
		if (!result) {
			return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
		}
		return ApiRes.ok();
	}


}
