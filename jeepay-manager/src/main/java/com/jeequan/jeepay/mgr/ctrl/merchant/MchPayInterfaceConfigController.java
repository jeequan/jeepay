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
package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.entity.PayInterfaceDefine;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.mgr.mq.service.MqSendServiceImpl;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.PayInterfaceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商户支付接口管理类
 *
 * @author zhuxiao
 * @site https://www.jeepay.vip
 * @date 2021-04-27 15:50
 */
@RestController
@RequestMapping("/api/mch/payConfigs")
public class MchPayInterfaceConfigController extends CommonCtrl {

    @Autowired private PayInterfaceConfigService payInterfaceConfigService;
    @Autowired private MqSendServiceImpl mqSendServiceImpl;
    @Autowired private MchAppService mchAppService;

    /**
     * @Author: ZhuXiao
     * @Description: 查询应用支付接口配置列表
     * @Date: 15:50 2021/4/27
    */
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_LIST')")
    @GetMapping
    public ApiRes list() {

        List<PayInterfaceDefine> list = payInterfaceConfigService.selectAllPayIfConfigListByAppId(getValStringRequired("appId"));
        return ApiRes.ok(list);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 根据 appId、接口类型 获取应用参数配置
     * @Date: 17:03 2021/4/27
    */
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_VIEW')")
    @GetMapping("/{appId}/{ifCode}")
    public ApiRes getByAppId(@PathVariable(value = "appId") String appId, @PathVariable(value = "ifCode") String ifCode) {
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, appId, ifCode);
        if (payInterfaceConfig != null && payInterfaceConfig.getIfRate() != null) {
            payInterfaceConfig.setIfRate(payInterfaceConfig.getIfRate().multiply(new BigDecimal("100")));
        }
        return ApiRes.ok(payInterfaceConfig);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 应用支付接口配置
     * @Date: 16:13 2021/4/27
    */
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_ADD')")
    @PostMapping
    @MethodLog(remark = "更新应用支付参数")
    public ApiRes saveOrUpdate() {

        String infoId = getValStringRequired("infoId");
        String ifCode = getValStringRequired("ifCode");

        MchApp mchApp = mchAppService.getById(infoId);
        if (mchApp == null || mchApp.getState() != CS.YES) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

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
        }else {
            payInterfaceConfig.setCreatedUid(userId);
            payInterfaceConfig.setCreatedBy(realName);
        }

        boolean result = payInterfaceConfigService.saveOrUpdate(payInterfaceConfig);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR, "配置失败");
        }

        mqSendServiceImpl.sendModifyMchApp(mchApp.getMchNo(), infoId); // 推送mq到目前节点进行更新数据

        return ApiRes.ok();
    }

}
