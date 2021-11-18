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
package com.jeequan.jeepay.pay.ctrl.refund;

import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import com.jeequan.jeepay.pay.rqrs.refund.QueryRefundOrderRQ;
import com.jeequan.jeepay.pay.rqrs.refund.QueryRefundOrderRS;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.service.impl.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* 商户退款单查询controller
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/17 15:20
*/
@Slf4j
@RestController
public class QueryRefundOrderController extends ApiController {

    @Autowired private RefundOrderService refundOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;

    /**
     * 查单接口
     * **/
    @RequestMapping("/api/refund/query")
    public ApiRes queryRefundOrder(){

        //获取参数 & 验签
        QueryRefundOrderRQ rq = getRQByWithMchSign(QueryRefundOrderRQ.class);

        if(StringUtils.isAllEmpty(rq.getMchRefundNo(), rq.getRefundOrderId())){
            throw new BizException("mchRefundNo 和 refundOrderId不能同时为空");
        }

        RefundOrder refundOrder = refundOrderService.queryMchOrder(rq.getMchNo(), rq.getMchRefundNo(), rq.getRefundOrderId());
        if(refundOrder == null){
            throw new BizException("订单不存在");
        }

        QueryRefundOrderRS bizRes = QueryRefundOrderRS.buildByRefundOrder(refundOrder);
        return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());
    }
}
