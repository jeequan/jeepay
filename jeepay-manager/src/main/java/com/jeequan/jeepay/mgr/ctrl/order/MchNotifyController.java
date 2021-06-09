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
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchNotifyRecordService;
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
 * @site https://www.jeepay.vip
 * @date 2021-06-07 07:15
 */
@RestController
@RequestMapping("/api/mchNotify")
public class MchNotifyController extends CommonCtrl {

    @Autowired private MchNotifyRecordService mchNotifyService;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 商户通知列表
     */
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiRes list() {

        MchNotifyRecord mchNotify = getObject(MchNotifyRecord.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<MchNotifyRecord> wrapper = MchNotifyRecord.gw();
        if (StringUtils.isNotEmpty(mchNotify.getOrderId())) wrapper.eq(MchNotifyRecord::getOrderId, mchNotify.getOrderId());
        if (StringUtils.isNotEmpty(mchNotify.getMchNo())) wrapper.eq(MchNotifyRecord::getMchNo, mchNotify.getMchNo());
        if (StringUtils.isNotEmpty(mchNotify.getIsvNo())) wrapper.eq(MchNotifyRecord::getIsvNo, mchNotify.getIsvNo());
        if (StringUtils.isNotEmpty(mchNotify.getMchOrderNo())) wrapper.eq(MchNotifyRecord::getMchOrderNo, mchNotify.getMchOrderNo());
        if (mchNotify.getOrderType() != null) wrapper.eq(MchNotifyRecord::getOrderType, mchNotify.getOrderType());
        if (mchNotify.getState() != null) wrapper.eq(MchNotifyRecord::getState, mchNotify.getState());

        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) wrapper.ge(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdStart"));
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) wrapper.le(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdEnd"));
        }
        wrapper.orderByDesc(MchNotifyRecord::getCreatedAt);
        IPage<MchNotifyRecord> pages = mchNotifyService.page(getIPage(), wrapper);

        return ApiRes.page(pages);
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 商户通知信息
     */
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_VIEW')")
    @RequestMapping(value="/{notifyId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("notifyId") String notifyId) {
        MchNotifyRecord mchNotify = mchNotifyService.getById(notifyId);
        if (mchNotify == null) return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        return ApiRes.ok(mchNotify);
    }
}
