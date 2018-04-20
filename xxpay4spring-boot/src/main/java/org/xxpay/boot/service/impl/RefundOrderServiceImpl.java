package org.xxpay.boot.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.boot.service.BaseService;
import org.xxpay.boot.service.INotifyPayService;
import org.xxpay.boot.service.IPayChannel4AliService;
import org.xxpay.boot.service.IPayChannel4WxService;
import org.xxpay.boot.service.IRefundOrderService;
import org.xxpay.boot.service.mq.Mq4RefundNotify;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.domain.BaseParam;
import org.xxpay.common.enumm.RetEnum;
import org.xxpay.common.util.BeanConvertUtils;
import org.xxpay.common.util.JsonUtil;
import org.xxpay.common.util.MyLog;
import org.xxpay.common.util.ObjectValidUtil;
import org.xxpay.common.util.RpcUtil;
import org.xxpay.common.util.XXPayUtil;
import org.xxpay.dal.dao.model.RefundOrder;

import com.alibaba.fastjson.JSONObject;

/**
 * @author: dingzhiwei
 * @date: 17/10/30
 * @description:
 */
@Service
public class RefundOrderServiceImpl extends BaseService implements IRefundOrderService {

    private static final MyLog _log = MyLog.getLog(RefundOrderServiceImpl.class);

    @Autowired
    private Mq4RefundNotify mq4RefundNotify;
    
    @Autowired
    private INotifyPayService notifyPayService;
    
    @Autowired
    private IPayChannel4WxService payChannel4WxService;

    @Autowired
    private IPayChannel4AliService payChannel4AliService;

    @Override
    public int createRefundOrder(JSONObject refundOrder) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("refundOrder", refundOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = create(jsonParam);
        String s = RpcUtil.mkRet(result);
        if(s == null) return 0;
        return Integer.parseInt(s);
    }
    @Override
    public void sendRefundNotify(String refundOrderId, String channelName) {
        JSONObject object = new JSONObject();
        object.put("refundOrderId", refundOrderId);
        object.put("channelName", channelName);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("msg", object);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        sendRefundNotify(jsonParam);
    }

    public JSONObject query(String mchId, String refundOrderId, String mchRefundNo, String executeNotify) {
        Map<String,Object> paramMap = new HashMap<>();
        Map<String, Object> result;
        if(StringUtils.isNotBlank(refundOrderId)) {
            paramMap.put("mchId", mchId);
            paramMap.put("refundOrderId", refundOrderId);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = selectByMchIdAndRefundOrderId(jsonParam);
        }else {
            paramMap.put("mchId", mchId);
            paramMap.put("mchRefundNo", mchRefundNo);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = selectByMchIdAndMchRefundNo(jsonParam);
        }
        String s = RpcUtil.mkRet(result);
        if(s == null) return null;
        boolean isNotify = Boolean.parseBoolean(executeNotify);
        JSONObject payOrder = JSONObject.parseObject(s);
        if(isNotify) {
            paramMap = new HashMap<>();
            paramMap.put("refundOrderId", refundOrderId);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = notifyPayService.sendBizPayNotify(jsonParam);
            s = RpcUtil.mkRet(result);
            _log.info("业务查单完成,并再次发送业务支付通知.发送结果:{}", s);
        }
        return payOrder;
    }

   /* public String doWxRefundReq(String tradeType, JSONObject refundOrder, String resKey) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("tradeType", tradeType);
        paramMap.put("refundOrder", refundOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = payChannel4AliService.doAliRefundReq(jsonParam);
        String s = RpcUtil.mkRet(result);
        if(s == null) {
            return XXPayUtil.makeRetData(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, "0111", "调用微信支付失败"), resKey);
        }
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.putAll((Map) result.get("bizResult"));
        return XXPayUtil.makeRetData(map, resKey);
    }*/
    
    @Override
    public Map create(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("新增退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject refundOrderObj = baseParam.isNullValue("refundOrder") ? null : JSONObject.parseObject(bizParamMap.get("refundOrder").toString());
        if(refundOrderObj == null) {
            _log.warn("新增退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        RefundOrder refundOrder = BeanConvertUtils.map2Bean(refundOrderObj, RefundOrder.class);
        if(refundOrder == null) {
            _log.warn("新增退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = super.baseCreateRefundOrder(refundOrder);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map select(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据退款订单号查询退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
        if (ObjectValidUtil.isInvalid(refundOrderId)) {
            _log.warn("根据退款订单号查询退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        RefundOrder refundOrder = super.baseSelectRefundOrder(refundOrderId);
        if(refundOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(refundOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectByMchIdAndRefundOrderId(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和退款订单号查询退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
        if (ObjectValidUtil.isInvalid(mchId, refundOrderId)) {
            _log.warn("根据商户号和退款订单号查询退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        RefundOrder refundOrder = super.baseSelectByMchIdAndRefundOrderId(mchId, refundOrderId);
        if(refundOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(refundOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectByMchIdAndMchRefundNo(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String mchRefundNo = baseParam.isNullValue("mchRefundNo") ? null : bizParamMap.get("mchRefundNo").toString();
        if (ObjectValidUtil.isInvalid(mchId, mchRefundNo)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        RefundOrder refundOrder = super.baseSelectByMchIdAndMchRefundNo(mchId, mchRefundNo);
        if(refundOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(refundOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map updateStatus4Ing(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
        String channelOrderNo = baseParam.isNullValue("channelOrderNo") ? null : bizParamMap.get("channelOrderNo").toString();
        if (ObjectValidUtil.isInvalid(refundOrderId)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Ing(refundOrderId, channelOrderNo);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateStatus4Success(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
        if (ObjectValidUtil.isInvalid(refundOrderId)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4SuccessByRefund(refundOrderId);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateStatus4Complete(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
        if (ObjectValidUtil.isInvalid(refundOrderId)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Complete(refundOrderId);
        return RpcUtil.createBizResult(baseParam, result);
    }

    private Map sendRefundNotify(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("发送退款订单处理失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String msg = baseParam.isNullValue("msg") ? null : bizParamMap.get("msg").toString();
        if (ObjectValidUtil.isInvalid(msg)) {
            _log.warn("发送退款订单处理失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = 1;
        try {
        	mq4RefundNotify.send(msg);
        }catch (Exception e) {
            _log.error(e, "");
            result = 0;
        }
        return RpcUtil.createBizResult(baseParam, result);
    }

}
