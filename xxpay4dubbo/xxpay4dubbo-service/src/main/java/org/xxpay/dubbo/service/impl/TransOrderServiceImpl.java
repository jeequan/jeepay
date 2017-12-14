package org.xxpay.dubbo.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.common.domain.BaseParam;
import org.xxpay.common.enumm.RetEnum;
import org.xxpay.common.util.*;
import org.xxpay.dal.dao.model.TransOrder;
import org.xxpay.dubbo.api.service.ITransOrderService;
import org.xxpay.dubbo.service.BaseService4TransOrder;
import org.xxpay.dubbo.service.mq.Mq4TransNotify;

import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/10/30
 * @description:
 */
@Service(version = "1.0.0")
public class TransOrderServiceImpl extends BaseService4TransOrder implements ITransOrderService {

    private static final MyLog _log = MyLog.getLog(TransOrderServiceImpl.class);

    @Autowired
    private Mq4TransNotify mq4TransNotify;

    @Override
    public Map create(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("新增转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
        if(transOrderObj == null) {
            _log.warn("新增转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = BeanConvertUtils.map2Bean(transOrderObj, TransOrder.class);
        if(transOrder == null) {
            _log.warn("新增转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = super.baseCreateTransOrder(transOrder);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map select(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("根据转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = super.baseSelectTransOrder(transOrderId);
        if(transOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(transOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectByMchIdAndTransOrderId(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(mchId, transOrderId)) {
            _log.warn("根据商户号和转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = super.baseSelectByMchIdAndTransOrderId(mchId, transOrderId);
        if(transOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(transOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectByMchIdAndMchTransNo(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String mchTransNo = baseParam.isNullValue("mchTransNo") ? null : bizParamMap.get("mchTransNo").toString();
        if (ObjectValidUtil.isInvalid(mchId, mchTransNo)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = super.baseSelectByMchIdAndMchTransNo(mchId, mchTransNo);
        if(transOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(transOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map updateStatus4Ing(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        String channelOrderNo = baseParam.isNullValue("channelOrderNo") ? null : bizParamMap.get("channelOrderNo").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Ing(transOrderId, channelOrderNo);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateStatus4Success(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Success(transOrderId);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateStatus4Complete(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Complete(transOrderId);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map sendTransNotify(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("发送转账订单处理失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String msg = baseParam.isNullValue("msg") ? null : bizParamMap.get("msg").toString();
        if (ObjectValidUtil.isInvalid(msg)) {
            _log.warn("发送转账订单处理失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = 1;
        try {
            mq4TransNotify.send(msg);
        }catch (Exception e) {
            _log.error(e, "");
            result = 0;
        }
        return RpcUtil.createBizResult(baseParam, result);
    }
}
