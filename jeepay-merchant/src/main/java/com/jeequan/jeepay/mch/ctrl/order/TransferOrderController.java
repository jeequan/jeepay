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
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.TransferOrderService;
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


/**
* 转账订单api
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/13 10:52
*/
@Tag(name = "订单管理（转账类）")
@RestController
@RequestMapping("/api/transferOrders")
public class TransferOrderController extends CommonCtrl {

    @Autowired private TransferOrderService transferOrderService;

    /** list **/
    @Operation(summary = "转账订单信息列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "createdStart", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @Parameter(name = "createdEnd", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @Parameter(name = "unionOrderId", description = "转账/商户/渠道订单号"),
            @Parameter(name = "appId", description = "应用ID"),
            @Parameter(name = "state", description = "支付状态: 0-订单生成, 1-转账中, 2-转账成功, 3-转账失败, 4-订单关闭")
    })
    @PreAuthorize("hasAuthority('ENT_TRANSFER_ORDER_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<TransferOrder> list() {

        TransferOrder transferOrder = getObject(TransferOrder.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<TransferOrder> wrapper = TransferOrder.gw();
        wrapper.eq(TransferOrder::getMchNo, getCurrentMchNo());
        IPage<TransferOrder> pages = transferOrderService.pageList(getIPage(), wrapper, transferOrder, paramJSON);

        return ApiPageRes.pages(pages);
    }

    /** detail **/
    @Operation(summary = "转账订单信息详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "recordId", description = "转账订单号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_TRANSFER_ORDER_VIEW')")
    @RequestMapping(value="/{recordId}", method = RequestMethod.GET)
    public ApiRes<TransferOrder> detail(@PathVariable("recordId") String transferId) {
        TransferOrder refundOrder = transferOrderService.queryMchOrder(getCurrentMchNo(), null, transferId);
        if (refundOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(refundOrder);
    }
}
