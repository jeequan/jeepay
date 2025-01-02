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
import com.jeequan.jeepay.components.mq.model.PayOrderMchNotifyMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchNotifyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户通知类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@Tag(name = "订单管理（通知类）")
@RestController
@RequestMapping("/api/mchNotify")
public class MchNotifyController extends CommonCtrl {

    @Autowired private MchNotifyRecordService mchNotifyService;
    @Autowired private IMQSender mqSender;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 商户通知列表
     */
    @Operation(summary = "查询商户通知列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "createdStart", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @Parameter(name = "createdEnd", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @Parameter(name = "mchNo", description = "商户号"),
            @Parameter(name = "orderId", description = "订单ID"),
            @Parameter(name = "mchOrderNo", description = "商户订单号"),
            @Parameter(name = "isvNo", description = "服务商号"),
            @Parameter(name = "appId", description = "应用ID"),
            @Parameter(name = "state", description = "通知状态,1-通知中,2-通知成功,3-通知失败"),
            @Parameter(name = "orderType", description = "订单类型:1-支付,2-退款")
    })
    @PreAuthorize("hasAuthority('ENT_NOTIFY_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<MchNotifyRecord> list() {

        MchNotifyRecord mchNotify = getObject(MchNotifyRecord.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<MchNotifyRecord> wrapper = MchNotifyRecord.gw();
        if (StringUtils.isNotEmpty(mchNotify.getOrderId())) {
            wrapper.eq(MchNotifyRecord::getOrderId, mchNotify.getOrderId());
        }
        if (StringUtils.isNotEmpty(mchNotify.getMchNo())) {
            wrapper.eq(MchNotifyRecord::getMchNo, mchNotify.getMchNo());
        }
        if (StringUtils.isNotEmpty(mchNotify.getIsvNo())) {
            wrapper.eq(MchNotifyRecord::getIsvNo, mchNotify.getIsvNo());
        }
        if (StringUtils.isNotEmpty(mchNotify.getMchOrderNo())) {
            wrapper.eq(MchNotifyRecord::getMchOrderNo, mchNotify.getMchOrderNo());
        }
        if (mchNotify.getOrderType() != null) {
            wrapper.eq(MchNotifyRecord::getOrderType, mchNotify.getOrderType());
        }
        if (mchNotify.getState() != null) {
            wrapper.eq(MchNotifyRecord::getState, mchNotify.getState());
        }
        if (StringUtils.isNotEmpty(mchNotify.getAppId())) {
            wrapper.eq(MchNotifyRecord::getAppId, mchNotify.getAppId());
        }

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        wrapper.orderByDesc(MchNotifyRecord::getCreatedAt);
        IPage<MchNotifyRecord> pages = mchNotifyService.page(getIPage(), wrapper);

        return ApiPageRes.pages(pages);
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 商户通知信息
     */
    @Operation(summary = "通知信息详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "notifyId", description = "商户通知记录ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_VIEW')")
    @RequestMapping(value="/{notifyId}", method = RequestMethod.GET)
    public ApiRes<MchNotifyRecord> detail(@PathVariable("notifyId") String notifyId) {
        MchNotifyRecord mchNotify = mchNotifyService.getById(notifyId);
        if (mchNotify == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(mchNotify);
    }

   /*
    * 功能描述: 商户通知重发操作
    * @Author: terrfly
    * @Date: 2021/6/21 17:41
    */
    @Operation(summary = "商户通知重发")
    @Parameters({
           @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
           @Parameter(name = "notifyId", description = "商户通知记录ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_RESEND')")
    @RequestMapping(value="resend/{notifyId}", method = RequestMethod.POST)
    public ApiRes<MchNotifyRecord> resend(@PathVariable("notifyId") Long notifyId) {
        MchNotifyRecord mchNotify = mchNotifyService.getById(notifyId);
        if (mchNotify == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        if (mchNotify.getState() != MchNotifyRecord.STATE_FAIL) {
            throw new BizException("请选择失败的通知记录");
        }

        //更新通知中
        mchNotifyService.getBaseMapper().updateIngAndAddNotifyCountLimit(notifyId);

        //调起MQ重发
        mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        return ApiRes.ok(mchNotify);
    }

}
