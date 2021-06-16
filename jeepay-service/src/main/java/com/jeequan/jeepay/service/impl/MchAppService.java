package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.MchAppMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 商户应用表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-06-15
 */
@Service
public class MchAppService extends ServiceImpl<MchAppMapper, MchApp> {

    @Autowired private PayOrderService payOrderService;
    @Autowired private MchPayPassageService mchPayPassageService;
    @Autowired private PayInterfaceConfigService payInterfaceConfigService;

    @Transactional(rollbackFor = Exception.class)
    public void removeByAppId(String appId) {

        // 1.查看当前应用是否存在交易数据
        int payCount = payOrderService.count(PayOrder.gw().eq(PayOrder::getAppId, appId));
        if (payCount > 0) throw new BizException("该应用已存在交易数据，不可删除");

        // 2.删除应用关联的支付通道
        boolean result = mchPayPassageService.remove(MchPayPassage.gw().eq(MchPayPassage::getAppId, appId));
        if (!result) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }

        // 3.删除应用配置的支付参数
        result = payInterfaceConfigService.remove(PayInterfaceConfig.gw()
                .eq(PayInterfaceConfig::getInfoId, appId)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
        );
        if (!result) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }

        // 4.删除当前应用
        result = removeById(appId);
        if (!result) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
    }
}
