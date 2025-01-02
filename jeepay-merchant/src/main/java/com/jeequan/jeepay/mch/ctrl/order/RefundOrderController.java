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
package com.jeequan.jeepay.mch.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.RefundOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 退款订单管理类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Tag(name = "订单管理（退款类）")
@RestController
@RequestMapping("/api/refundOrder")
public class RefundOrderController extends CommonCtrl {

    @Autowired private RefundOrderService refundOrderService;

    /**
     * @Author: ZhuXiao
     * @Description: 退款订单信息列表
     * @Date: 10:44 2021/5/13
    */
    @Operation(summary = "退款订单信息列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "createdStart", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @Parameter(name = "createdEnd", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @Parameter(name = "unionOrderId", description = "支付/退款订单号"),
            @Parameter(name = "appId", description = "应用ID"),
            @Parameter(name = "state", description = "退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-退款任务关闭"),
            @Parameter(name = "mchType", description = "类型: 1-普通商户, 2-特约商户(服务商模式)")
    })
    @PreAuthorize("hasAuthority('ENT_REFUND_LIST')")
    @GetMapping
    public ApiPageRes<RefundOrder> list() {

        RefundOrder refundOrder = getObject(RefundOrder.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<RefundOrder> wrapper = RefundOrder.gw();
        wrapper.eq(RefundOrder::getMchNo, getCurrentMchNo());
        IPage<RefundOrder> pages = refundOrderService.pageList(getIPage(), wrapper, refundOrder, paramJSON);

        return ApiPageRes.pages(pages);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 退款订单信息
     * @Date: 10:44 2021/5/13
    */
    @Operation(summary = "退款订单信息详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "refundOrderId", description = "退款订单号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_REFUND_ORDER_VIEW')")
    @GetMapping("/{refundOrderId}")
    public ApiRes<RefundOrder> detail(@PathVariable("refundOrderId") String refundOrderId) {
        RefundOrder refundOrder = refundOrderService.getById(refundOrderId);
        if (refundOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        if (!refundOrder.getMchNo().equals(getCurrentUser().getSysUser().getBelongInfoId())) {
            return ApiRes.fail(ApiCodeEnum.SYS_PERMISSION_ERROR);
        }
        return ApiRes.ok(refundOrder);
    }
}
