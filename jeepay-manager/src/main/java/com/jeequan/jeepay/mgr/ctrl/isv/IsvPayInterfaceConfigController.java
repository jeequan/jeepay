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
package com.jeequan.jeepay.mgr.ctrl.isv;

import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.entity.PayInterfaceDefine;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.params.IsvParams;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayInterfaceConfigService;
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
import java.util.List;

/**
 * 服务商支付接口管理类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */

@Tag(name = "服务商管理（支付接口）")
@RestController
@RequestMapping("/api/isv/payConfigs")
public class IsvPayInterfaceConfigController extends CommonCtrl {

    @Autowired private PayInterfaceConfigService payInterfaceConfigService;
    @Autowired private IMQSender mqSender;

   /**
    * @Author: ZhuXiao
    * @Description: 查询服务商支付接口配置列表
    * @Date: 16:45 2021/4/27
   */
   @Operation(summary = "查询服务商支付接口配置列表", description = "")
   @Parameters({
           @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
           @Parameter(name = "isvNo", description = "服务商号", required = true)
   })
   @PreAuthorize("hasAuthority('ENT_ISV_PAY_CONFIG_LIST')")
    @GetMapping
    public ApiRes<List<PayInterfaceDefine>> list() {

        List<PayInterfaceDefine> list = payInterfaceConfigService.selectAllPayIfConfigListByIsvNo(CS.INFO_TYPE_ISV, getValStringRequired("isvNo"));
        return ApiRes.ok(list);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 根据 服务商号、接口类型 获取商户参数配置
     * @Date: 17:03 2021/4/27
     */
    @Operation(summary = "根据[服务商号]、[接口类型]获取商户参数配置", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "isvNo", description = "服务商号", required = true),
            @Parameter(name = "ifCode", description = "接口类型代码", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_ISV_PAY_CONFIG_VIEW')")
    @GetMapping("/{isvNo}/{ifCode}")
    public ApiRes<PayInterfaceConfig> getByMchNo(@PathVariable(value = "isvNo") String isvNo, @PathVariable(value = "ifCode") String ifCode) {
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_ISV, isvNo, ifCode);
        if (payInterfaceConfig != null) {
            if (payInterfaceConfig.getIfRate() != null) {
                payInterfaceConfig.setIfRate(payInterfaceConfig.getIfRate().multiply(new BigDecimal("100")));
            }
            if (StringUtils.isNotBlank(payInterfaceConfig.getIfParams())) {
                IsvParams isvParams = IsvParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());
                if (isvParams != null) {
                    payInterfaceConfig.setIfParams(isvParams.deSenData());
                }
            }
        }
        return ApiRes.ok(payInterfaceConfig);
    }


    /**
     * @Author: ZhuXiao
     * @Description: 服务商支付接口参数配置
     * @Date: 16:45 2021/4/27
     */
    @Operation(summary = "服务商支付接口参数配置", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "infoId", description = "服务商号", required = true),
            @Parameter(name = "ifCode", description = "接口类型代码", required = true),
            @Parameter(name = "ifParams", description = "接口配置参数,json字符串"),
            @Parameter(name = "ifRate", description = "支付接口费率"),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用")
    })
    @PreAuthorize("hasAuthority('ENT_ISV_PAY_CONFIG_ADD')")
    @PostMapping
    @MethodLog(remark = "更新服务商支付参数")
    public ApiRes saveOrUpdate() {

        String infoId = getValStringRequired("infoId");
        String ifCode = getValStringRequired("ifCode");

        PayInterfaceConfig payInterfaceConfig = getObject(PayInterfaceConfig.class);
        payInterfaceConfig.setInfoType(CS.INFO_TYPE_ISV);

        // 存入真实费率
        if (payInterfaceConfig.getIfRate() != null) {
            payInterfaceConfig.setIfRate(payInterfaceConfig.getIfRate().divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP));
        }

        //添加更新者信息
        Long userId = getCurrentUser().getSysUser().getSysUserId();
        String realName = getCurrentUser().getSysUser().getRealname();
        payInterfaceConfig.setUpdatedUid(userId);
        payInterfaceConfig.setUpdatedBy(realName);

        //根据 服务商号、接口类型 获取商户参数配置
        PayInterfaceConfig dbRecoed = payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_ISV, infoId, ifCode);
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
            return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR, "配置失败");
        }

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_ISV_INFO, infoId, null, null));

        return ApiRes.ok();
    }
}
