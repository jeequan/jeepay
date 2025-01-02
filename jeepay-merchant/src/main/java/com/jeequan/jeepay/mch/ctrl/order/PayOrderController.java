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
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayWay;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.model.RefundOrderCreateReqModel;
import com.jeequan.jeepay.request.RefundOrderCreateRequest;
import com.jeequan.jeepay.response.RefundOrderCreateResponse;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayWayService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付订单管理类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Tag(name = "订单管理（支付类）")
@RestController
@RequestMapping("/api/payOrder")
public class PayOrderController extends CommonCtrl {

    @Autowired private PayOrderService payOrderService;
    @Autowired private PayWayService payWayService;
    @Autowired private MchAppService mchAppService;
    @Autowired private SysConfigService sysConfigService;

    /**
     * @Author: ZhuXiao
     * @Description: 订单信息列表
     * @Date: 10:43 2021/5/13
    */
    @Operation(summary = "支付订单信息列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "createdStart", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @Parameter(name = "createdEnd", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @Parameter(name = "unionOrderId", description = "支付/商户/渠道订单号"),
            @Parameter(name = "appId", description = "应用ID"),
            @Parameter(name = "wayCode", description = "支付方式代码"),
            @Parameter(name = "state", description = "支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭"),
            @Parameter(name = "notifyState", description = "向下游回调状态, 0-未发送,  1-已发送"),
            @Parameter(name = "divisionState", description = "0-未发生分账, 1-等待分账任务处理, 2-分账处理中, 3-分账任务已结束(不体现状态)")
    })
    @PreAuthorize("hasAuthority('ENT_ORDER_LIST')")
    @GetMapping
    public ApiPageRes<PayOrder> list() {

        PayOrder payOrder = getObject(PayOrder.class);
        JSONObject paramJSON = getReqParamJSON();

        LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
        wrapper.eq(PayOrder::getMchNo, getCurrentMchNo());

        IPage<PayOrder> pages = payOrderService.listByPage(getIPage(), payOrder, paramJSON, wrapper);

        // 得到所有支付方式
        Map<String, String> payWayNameMap = new HashMap<>();
        List<PayWay> payWayList = payWayService.list();
        if (!CollectionUtils.isEmpty(payWayList)) {
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
        }

        return ApiPageRes.pages(pages);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 支付订单信息
     * @Date: 10:43 2021/5/13
    */
    @Operation(summary = "支付订单信息详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "payOrderId", description = "支付订单号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_VIEW')")
    @GetMapping("/{payOrderId}")
    public ApiRes<PayOrder> detail(@PathVariable("payOrderId") String payOrderId) {
        PayOrder payOrder = payOrderService.getById(payOrderId);
        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        if (!payOrder.getMchNo().equals(getCurrentMchNo())) {
            return ApiRes.fail(ApiCodeEnum.SYS_PERMISSION_ERROR);
        }
        return ApiRes.ok(payOrder);
    }


    /**
     * 发起订单退款
     * @author terrfly
     * @site https://www.jeequan.com
     * @date 2021/6/17 16:38
     */
    @Operation(summary = "发起订单退款")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "payOrderId", description = "支付订单号", required = true),
            @Parameter(name = "refundAmount", description = "退款金额", required = true),
            @Parameter(name = "refundReason", description = "退款原因", required = true)
    })
    @MethodLog(remark = "发起订单退款")
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_REFUND')")
    @PostMapping("/refunds/{payOrderId}")
    public ApiRes refund(@PathVariable("payOrderId") String payOrderId) {

        Long refundAmount = getRequiredAmountL("refundAmount");
        String refundReason = getValStringRequired("refundReason");

        PayOrder payOrder = payOrderService.getById(payOrderId);
        if (payOrder == null || !payOrder.getMchNo().equals(getCurrentMchNo())) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        if(payOrder.getState() != PayOrder.STATE_SUCCESS){
            throw new BizException("订单状态不正确");
        }

        if(payOrder.getRefundAmount() + refundAmount > payOrder.getAmount()){
            throw new BizException("退款金额超过订单可退款金额！");
        }


        RefundOrderCreateRequest request = new RefundOrderCreateRequest();
        RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
        request.setBizModel(model);

        model.setMchNo(payOrder.getMchNo());     // 商户号
        model.setAppId(payOrder.getAppId());
        model.setPayOrderId(payOrderId);
        model.setMchRefundNo(SeqKit.genMhoOrderId());
        model.setRefundAmount(refundAmount);
        model.setRefundReason(refundReason);
        model.setCurrency("CNY");

        MchApp mchApp = mchAppService.getById(payOrder.getAppId());

        JeepayClient jeepayClient = new JeepayClient(sysConfigService.getDBApplicationConfig().getPaySiteUrl(), mchApp.getAppSecret());

        try {
            RefundOrderCreateResponse response = jeepayClient.execute(request);
            if(response.getCode() != 0){
                throw new BizException(response.getMsg());
            }
            return ApiRes.ok(response.get());
        } catch (JeepayException e) {
            throw new BizException(e.getMessage());
        }
    }

}
