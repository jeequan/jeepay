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
package com.jeequan.jeepay.mch.ctrl.merchant;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.entity.PayWay;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import com.jeequan.jeepay.service.impl.PayWayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * 商户支付通道配置类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Tag(name = "商户支付通道管理")
@RestController
@RequestMapping("/api/mch/payPassages")
public class MchPayPassageConfigController extends CommonCtrl {

    @Autowired private MchPayPassageService mchPayPassageService;
    @Autowired private PayWayService payWayService;
    @Autowired private MchInfoService mchInfoService;

    /**
     * @Author: ZhuXiao
     * @Description: 查询支付方式列表，并添加是否配置支付通道状态
     * @Date: 10:58 2021/5/13
    */
    @Operation(summary = "查询支付方式列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "appId", description = "应用ID", required = true),
            @Parameter(name = "wayCode", description = "支付方式代码"),
            @Parameter(name = "wayName", description = "支付方式名称")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_PASSAGE_LIST')")
    @GetMapping
    public ApiPageRes<PayWay> list() {

        String appId = getValStringRequired("appId");
        String wayCode = getValString("wayCode");
        String wayName = getValString("wayName");

        //支付方式集合
        LambdaQueryWrapper<PayWay> wrapper = PayWay.gw();
        if (StrUtil.isNotBlank(wayCode)) {
            wrapper.eq(PayWay::getWayCode, wayCode);
        }
        if (StrUtil.isNotBlank(wayName)) {
            wrapper.like(PayWay::getWayName, wayName);
        }
        IPage<PayWay> payWayPage = payWayService.page(getIPage(), wrapper);

        if (!CollectionUtils.isEmpty(payWayPage.getRecords())) {

            // 支付方式代码集合
            List<String> wayCodeList = new LinkedList<>();
            payWayPage.getRecords().stream().forEach(payWay -> wayCodeList.add(payWay.getWayCode()));

            // 商户支付通道集合
            List<MchPayPassage> mchPayPassageList = mchPayPassageService.list(MchPayPassage.gw()
                    .select(MchPayPassage::getWayCode, MchPayPassage::getState)
                    .eq(MchPayPassage::getAppId, appId)
                    .eq(MchPayPassage::getMchNo, getCurrentMchNo())
                    .in(MchPayPassage::getWayCode, wayCodeList));

            for (PayWay payWay : payWayPage.getRecords()) {
                payWay.addExt("passageState", CS.NO);
                for (MchPayPassage mchPayPassage : mchPayPassageList) {
                    // 某种支付方式多个通道的情况下，只要有一个通道状态为开启，则该支付方式对应为开启状态
                    if (payWay.getWayCode().equals(mchPayPassage.getWayCode()) && mchPayPassage.getState() == CS.YES) {
                        payWay.addExt("passageState", CS.YES);
                        break;
                    }
                }
            }
        }

        return ApiPageRes.pages(payWayPage);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 根据appId、支付方式查询可用的支付接口列表
     * @Date: 11:05 2021/5/13
     * @return
    */
    @Operation(summary = "根据[应用ID]、[支付方式代码]查询可用的支付接口列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID", required = true),
            @Parameter(name = "wayCode", description = "支付方式代码", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_PASSAGE_CONFIG')")
    @GetMapping("/availablePayInterface/{appId}/{wayCode}")
    public ApiRes availablePayInterface(@PathVariable("appId") String appId, @PathVariable("wayCode") String wayCode) {

        String mchNo = getCurrentUser().getSysUser().getBelongInfoId();
        MchInfo mchInfo = mchInfoService.getById(mchNo);
        if (mchInfo == null || mchInfo.getState() != CS.YES) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        // 根据支付方式查询可用支付接口列表
        List<JSONObject> list = mchPayPassageService.selectAvailablePayInterfaceList(wayCode, appId, CS.INFO_TYPE_MCH_APP, mchInfo.getType());

        return ApiRes.ok(list);
    }

    /**
     * @Author: ZhuXiao
     * @Description:
     * @Date: 11:05 2021/5/13
    */
    @Operation(summary = "商户支付通道详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "支付通道ID", required = true)
    })
    @GetMapping("/{id}")
    public ApiRes<MchPayPassage> detail(@PathVariable("id") Long id) {
        MchPayPassage payPassage = mchPayPassageService.getById(id);
        if (payPassage == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        if (!payPassage.getMchNo().equals(getCurrentUser().getSysUser().getBelongInfoId())) {
            return ApiRes.fail(ApiCodeEnum.SYS_PERMISSION_ERROR);
        }
        payPassage.setRate(payPassage.getRate().multiply(new BigDecimal("100")));
        return ApiRes.ok(payPassage);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 应用支付通道配置
     * @Date: 11:05 2021/5/13
    */
    @Operation(summary = "更新商户支付通道")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "reqParams", description = "商户支付通道配置信息", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_PASSAGE_ADD')")
    @PostMapping
    @MethodLog(remark = "更新应用支付通道")
    public ApiRes saveOrUpdate() {

        String reqParams = getValStringRequired("reqParams");

        try {
            List<MchPayPassage> mchPayPassageList = JSONArray.parseArray(reqParams, MchPayPassage.class);
            mchPayPassageService.saveOrUpdateBatchSelf(mchPayPassageList, getCurrentMchNo());
            return ApiRes.ok();
        }catch (Exception e) {
            return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR);
        }
    }

}
