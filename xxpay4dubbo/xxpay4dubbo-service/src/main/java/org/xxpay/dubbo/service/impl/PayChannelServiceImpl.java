package org.xxpay.dubbo.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.common.domain.BaseParam;
import org.xxpay.common.enumm.RetEnum;
import org.xxpay.common.util.JsonUtil;
import org.xxpay.common.util.MyLog;
import org.xxpay.common.util.ObjectValidUtil;
import org.xxpay.common.util.RpcUtil;
import org.xxpay.dal.dao.mapper.PayChannelMapper;
import org.xxpay.dal.dao.model.PayChannel;
import org.xxpay.dubbo.api.service.IPayChannelService;
import org.xxpay.dubbo.service.BaseService;

import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/8
 * @description:
 */
@Service(version = "1.0.0")
public class PayChannelServiceImpl extends BaseService implements IPayChannelService {

    private static final MyLog _log = MyLog.getLog(PayChannelServiceImpl.class);

    @Override
    public Map selectPayChannel(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("查询支付渠道信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String channelId = baseParam.isNullValue("channelId") ? null : bizParamMap.get("channelId").toString();
        if (ObjectValidUtil.isInvalid(mchId, channelId)) {
            _log.warn("查询支付渠道信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        if(payChannel == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(payChannel);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }
}
