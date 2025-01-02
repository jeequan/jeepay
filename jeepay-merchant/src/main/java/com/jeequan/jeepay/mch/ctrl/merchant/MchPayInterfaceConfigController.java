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

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.entity.PayInterfaceDefine;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.DBApplicationConfig;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.PayInterfaceConfigService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 商户支付接口配置类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Tag(name = "商户支付接口管理")
@RestController
@RequestMapping("/api/mch/payConfigs")
public class MchPayInterfaceConfigController extends CommonCtrl {

    @Autowired private PayInterfaceConfigService payInterfaceConfigService;
    @Autowired private MchInfoService mchInfoService;
    @Autowired private MchAppService mchAppService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private IMQSender mqSender;

    /**
     * @Author: ZhuXiao
     * @Description: 查询商户支付接口配置列表
     * @Date: 10:51 2021/5/13
    */
    @Operation(summary = "查询应用支付接口配置列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_LIST')")
    @GetMapping
    public ApiRes<List<PayInterfaceDefine>> list() {
        MchInfo mchInfo = mchInfoService.getById(getCurrentUser().getSysUser().getBelongInfoId());
        List<PayInterfaceDefine> list = payInterfaceConfigService.selectAllPayIfConfigListByAppId(getValStringRequired("appId"));

        for (PayInterfaceDefine define : list) {
            define.addExt("mchParams", mchInfo.getType() == CS.MCH_TYPE_NORMAL ? define.getNormalMchParams() : define.getIsvsubMchParams());
            define.setNormalMchParams(null);
            define.setIsvsubMchParams(null);
        }
        return ApiRes.ok(list);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 根据 商户号、接口类型 获取商户参数配置
     * @Date: 10:54 2021/5/13
    */
    @Operation(summary = "根据应用ID、接口类型 获取应用参数配置")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID", required = true),
            @Parameter(name = "ifCode", description = "接口类型代码", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_VIEW')")
    @GetMapping("/{appId}/{ifCode}")
    public ApiRes<PayInterfaceConfig> getByMchNo(@PathVariable(value = "appId") String appId, @PathVariable(value = "ifCode") String ifCode) {
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, appId, ifCode);
        if (payInterfaceConfig != null) {
            // 费率转换为百分比数值
            if (payInterfaceConfig.getIfRate() != null) {
                payInterfaceConfig.setIfRate(payInterfaceConfig.getIfRate().multiply(new BigDecimal("100")));
            }

            // 敏感数据脱敏
            if (StringUtils.isNotBlank(payInterfaceConfig.getIfParams())) {
                MchInfo mchInfo = mchInfoService.getById(getCurrentMchNo());

                // 普通商户的支付参数执行数据脱敏
                if (mchInfo.getType() == CS.MCH_TYPE_NORMAL) {
                    NormalMchParams mchParams = NormalMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());
                    if (mchParams != null) {
                        payInterfaceConfig.setIfParams(mchParams.deSenData());
                    }
                }
            }
        }
        return ApiRes.ok(payInterfaceConfig);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 更新商户支付参数
     * @Date: 10:56 2021/5/13
    */
    @Operation(summary = "更新应用支付参数")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "infoId", description = "应用AppId", required = true),
            @Parameter(name = "ifCode", description = "接口类型代码", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_ADD')")
    @PostMapping
    @MethodLog(remark = "更新商户支付参数")
    public ApiRes saveOrUpdate() {

        String ifCode = getValStringRequired("ifCode");
        String infoId = getValStringRequired("infoId");

        PayInterfaceConfig payInterfaceConfig = getObject(PayInterfaceConfig.class);
        payInterfaceConfig.setInfoType(CS.INFO_TYPE_MCH_APP);
        payInterfaceConfig.setInfoId(infoId);

        // 存入真实费率
        if (payInterfaceConfig.getIfRate() != null) {
            payInterfaceConfig.setIfRate(payInterfaceConfig.getIfRate().divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP));
        }

        //添加更新者信息
        Long userId = getCurrentUser().getSysUser().getSysUserId();
        String realName = getCurrentUser().getSysUser().getRealname();
        payInterfaceConfig.setUpdatedUid(userId);
        payInterfaceConfig.setUpdatedBy(realName);

        //根据 商户号、接口类型 获取商户参数配置
        PayInterfaceConfig dbRecoed = payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, infoId, ifCode);
        //若配置存在，为saveOrUpdate添加ID，第一次配置添加创建者
        if (dbRecoed != null) {
            payInterfaceConfig.setId(dbRecoed.getId());

            // 合并支付参数
            payInterfaceConfig.setIfParams(StringKit.marge(dbRecoed.getIfParams(), payInterfaceConfig.getIfParams()));
        }else {
            payInterfaceConfig.setCreatedUid(userId);
            payInterfaceConfig.setCreatedBy(realName);
        }

        boolean result = payInterfaceConfigService.saveOrUpdate(payInterfaceConfig);
        if (!result) {
            throw new BizException("配置失败");
        }
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_APP, null, getCurrentMchNo(), infoId));

        return ApiRes.ok();
    }

    /** 查询支付宝商户授权URL **/
    @Operation(summary = "查询支付宝商户授权URL")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchAppId", description = "应用ID", required = true)
    })
    @GetMapping("/alipayIsvsubMchAuthUrls/{mchAppId}")
    public ApiRes queryAlipayIsvsubMchAuthUrl(@PathVariable String mchAppId) {

        MchApp mchApp = mchAppService.getById(mchAppId);

        if (mchApp == null || !mchApp.getMchNo().equals(getCurrentMchNo())) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        MchInfo mchInfo = mchInfoService.getById(mchApp.getMchNo());
        DBApplicationConfig dbApplicationConfig = sysConfigService.getDBApplicationConfig();
        String authUrl = dbApplicationConfig.genAlipayIsvsubMchAuthUrl(mchInfo.getIsvNo(), mchAppId);
        String authQrImgUrl = dbApplicationConfig.genScanImgUrl(authUrl);

        JSONObject result = new JSONObject();
        result.put("authUrl", authUrl);
        result.put("authQrImgUrl", authQrImgUrl);
        return ApiRes.ok(result);
    }


    /** 查询当前应用支持的支付接口 */
    @Operation(summary = "查询当前应用支持的支付接口")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID", required = true)
    })
    @PreAuthorize("hasAuthority( 'ENT_DIVISION_RECEIVER_ADD' )")
    @RequestMapping(value="ifCodes/{appId}", method = RequestMethod.GET)
    public ApiRes<Set<String>> getIfCodeByAppId(@PathVariable("appId") String appId) {

        if(mchAppService.count(MchApp.gw().eq(MchApp::getMchNo, getCurrentMchNo()).eq(MchApp::getAppId, appId)) <= 0){
            throw new BizException("商户应用不存在");
        }

        Set<String> result = new HashSet<>();

        payInterfaceConfigService.list(PayInterfaceConfig.gw().select(PayInterfaceConfig::getIfCode)
        .eq(PayInterfaceConfig::getState, CS.PUB_USABLE)
        .eq(PayInterfaceConfig::getInfoId, appId)
        .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
        ).stream().forEach(r -> result.add(r.getIfCode()));

        return ApiRes.ok(result);
    }


}
