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
package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.PayInterfaceConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 支付接口配置参数表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Service
public class PayInterfaceConfigService extends ServiceImpl<PayInterfaceConfigMapper, PayInterfaceConfig> {

    @Autowired
    private PayInterfaceDefineService payInterfaceDefineService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private MchAppService mchAppService;

    /**
     * @Author: ZhuXiao
     * @Description: 根据 账户类型、账户号、接口类型 获取支付参数配置
     * @Date: 17:20 2021/4/27
    */
    public PayInterfaceConfig getByInfoIdAndIfCode(Byte infoType, String infoId, String ifCode) {
        return getOne(PayInterfaceConfig.gw()
                .eq(PayInterfaceConfig::getInfoType, infoType)
                .eq(PayInterfaceConfig::getInfoId, infoId)
                .eq(PayInterfaceConfig::getIfCode, ifCode)
        );
    }

    /**
     * @Author: ZhuXiao
     * @Description: 根据 账户类型、账户号 获取支付参数配置列表
     * @Date: 14:19 2021/5/7
    */
    public List<PayInterfaceDefine> selectAllPayIfConfigListByIsvNo(Byte infoType, String infoId) {

        // 支付定义列表
        LambdaQueryWrapper<PayInterfaceDefine> queryWrapper = PayInterfaceDefine.gw();
        queryWrapper.eq(PayInterfaceDefine::getState, CS.YES);
        queryWrapper.eq(PayInterfaceDefine::getIsIsvMode, CS.YES); // 支持服务商模式

        List<PayInterfaceDefine> defineList = payInterfaceDefineService.list(queryWrapper);

        // 支付参数列表
        LambdaQueryWrapper<PayInterfaceConfig> wrapper = PayInterfaceConfig.gw();
        wrapper.eq(PayInterfaceConfig::getInfoId, infoId);
        wrapper.eq(PayInterfaceConfig::getInfoType, infoType);
        List<PayInterfaceConfig> configList = this.list(wrapper);

        for (PayInterfaceDefine define : defineList) {
            for (PayInterfaceConfig config : configList) {
                if (define.getIfCode().equals(config.getIfCode())) {
                    define.addExt("ifConfigState", config.getState()); // 配置状态
                }
            }
        }
        return defineList;
    }


    public List<PayInterfaceDefine> selectAllPayIfConfigListByAppId(String appId) {

        MchApp mchApp = mchAppService.getById(appId);
        if (mchApp == null|| mchApp.getState() != CS.YES) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        MchInfo mchInfo = mchInfoService.getById(mchApp.getMchNo());
        if (mchInfo == null || mchInfo.getState() != CS.YES) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        // 支付定义列表
        LambdaQueryWrapper<PayInterfaceDefine> queryWrapper = PayInterfaceDefine.gw();
        queryWrapper.eq(PayInterfaceDefine::getState, CS.YES);

        Map<String, PayInterfaceConfig> isvPayConfigMap = new HashMap<>(); // 服务商支付参数配置集合

        // 根据商户类型，添加接口是否支持该商户类型条件
        if (mchInfo.getType() == CS.MCH_TYPE_NORMAL) {
            queryWrapper.eq(PayInterfaceDefine::getIsMchMode, CS.YES); // 支持普通商户模式
        }
        if (mchInfo.getType() == CS.MCH_TYPE_ISVSUB) {
            queryWrapper.eq(PayInterfaceDefine::getIsIsvMode, CS.YES); // 支持服务商模式
            // 商户类型为特约商户，服务商应已经配置支付参数
            List<PayInterfaceConfig> isvConfigList = this.list(PayInterfaceConfig.gw()
                    .eq(PayInterfaceConfig::getInfoId, mchInfo.getIsvNo())
                    .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_ISV)
                    .eq(PayInterfaceConfig::getState, CS.YES)
                    .ne(PayInterfaceConfig::getIfParams, "")
                    .isNotNull(PayInterfaceConfig::getIfParams));

            for (PayInterfaceConfig config : isvConfigList) {
                config.addExt("mchType", mchInfo.getType());
                isvPayConfigMap.put(config.getIfCode(), config);
            }
        }

        List<PayInterfaceDefine> defineList = payInterfaceDefineService.list(queryWrapper);

        // 支付参数列表
        LambdaQueryWrapper<PayInterfaceConfig> wrapper = PayInterfaceConfig.gw();
        wrapper.eq(PayInterfaceConfig::getInfoId, appId);
        wrapper.eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP);
        List<PayInterfaceConfig> configList = this.list(wrapper);

        for (PayInterfaceDefine define : defineList) {
            define.addExt("mchType", mchInfo.getType()); // 所属商户类型

            for (PayInterfaceConfig config : configList) {
                if (define.getIfCode().equals(config.getIfCode())) {
                    define.addExt("ifConfigState", config.getState()); // 配置状态
                }
            }

            if (mchInfo.getType() == CS.MCH_TYPE_ISVSUB && isvPayConfigMap.get(define.getIfCode()) == null) {
                define.addExt("subMchIsvConfig", CS.NO); // 特约商户，服务商支付参数的配置状态，0表示未配置
            }
        }
        return defineList;
    }



    /** 查询商户app使用已正确配置了通道信息 */
    public boolean mchAppHasAvailableIfCode(String appId, String ifCode){

        return this.count(
                PayInterfaceConfig.gw()
                        .eq(PayInterfaceConfig::getIfCode, ifCode)
                        .eq(PayInterfaceConfig::getState, CS.PUB_USABLE)
                        .eq(PayInterfaceConfig::getInfoId, appId)
                        .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                ) > 0;

    }


}
