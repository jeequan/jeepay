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
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.TransferOrderService;
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
@RestController
@RequestMapping("/api/transferOrders")
public class TransferOrderController extends CommonCtrl {

    @Autowired private TransferOrderService transferOrderService;

    /** list **/
    @PreAuthorize("hasAuthority('ENT_TRANSFER_ORDER_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiRes list() {

        TransferOrder transferOrder = getObject(TransferOrder.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<TransferOrder> wrapper = TransferOrder.gw();
        wrapper.eq(TransferOrder::getMchNo, getCurrentMchNo());
        IPage<TransferOrder> pages = transferOrderService.pageList(getIPage(), wrapper, transferOrder, paramJSON);

        return ApiRes.page(pages);
    }

    /** detail **/
    @PreAuthorize("hasAuthority('ENT_TRANSFER_ORDER_VIEW')")
    @RequestMapping(value="/{recordId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("recordId") String transferId) {
        TransferOrder refundOrder = transferOrderService.queryMchOrder(getCurrentMchNo(), null, transferId);
        if (refundOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(refundOrder);
    }
}
