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
package com.jeequan.jeepay.mgr.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayWay;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayWayService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付订单类
 *
 * @author pangxiaoyu
 * @site https://www.jeepay.vip
 * @date 2021-06-07 07:15
 */
@RestController
@RequestMapping("/api/payOrder")
public class PayOrderController extends CommonCtrl {

    @Autowired private PayOrderService payOrderService;
    @Autowired private PayWayService payWayService;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:15
     * @describe: 订单信息列表
     */
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiRes list() {

        PayOrder payOrder = getObject(PayOrder.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
        if (StringUtils.isNotEmpty(payOrder.getPayOrderId())) wrapper.eq(PayOrder::getPayOrderId, payOrder.getPayOrderId());
        if (StringUtils.isNotEmpty(payOrder.getMchNo())) wrapper.eq(PayOrder::getMchNo, payOrder.getMchNo());
        if (StringUtils.isNotEmpty(payOrder.getIsvNo())) wrapper.eq(PayOrder::getIsvNo, payOrder.getIsvNo());
        if (payOrder.getMchType() != null) wrapper.eq(PayOrder::getMchType, payOrder.getMchType());
        if (StringUtils.isNotEmpty(payOrder.getWayCode())) wrapper.eq(PayOrder::getWayCode, payOrder.getWayCode());
        if (StringUtils.isNotEmpty(payOrder.getMchOrderNo())) wrapper.eq(PayOrder::getMchOrderNo, payOrder.getMchOrderNo());
        if (payOrder.getState() != null) wrapper.eq(PayOrder::getState, payOrder.getState());
        if (payOrder.getNotifyState() != null) wrapper.eq(PayOrder::getNotifyState, payOrder.getNotifyState());
        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) wrapper.ge(PayOrder::getCreatedAt, paramJSON.getString("createdStart"));
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) wrapper.le(PayOrder::getCreatedAt, paramJSON.getString("createdEnd"));
        }
        wrapper.orderByDesc(PayOrder::getCreatedAt);
        IPage<PayOrder> pages = payOrderService.page(getIPage(), wrapper);
        // 得到所有支付方式
        Map<String, String> payWayNameMap = new HashMap<>();
        List<PayWay> payWayList = payWayService.list();
        for (PayWay payWay:payWayList) {
            payWayNameMap.put(payWay.getWayCode(), payWay.getWayName());
        }
        for (PayOrder order:pages.getRecords()) {
            // 存入支付方式名称
            if (StringUtils.isNotEmpty(payWayNameMap.get(order.getWayCode()))) {
                order.addExt("wayName", payWayNameMap.get(order.getWayCode()));
            }else {
                order.addExt("wayName", order.getWayCode());
            }
        }
        return ApiRes.page(pages);
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:15
     * @describe: 支付订单信息
     */
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_VIEW')")
    @RequestMapping(value="/{payOrderId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("payOrderId") String payOrderId) {
        PayOrder payOrder = payOrderService.getById(payOrderId);
        if (payOrder == null) return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        return ApiRes.ok(payOrder);
    }

}
