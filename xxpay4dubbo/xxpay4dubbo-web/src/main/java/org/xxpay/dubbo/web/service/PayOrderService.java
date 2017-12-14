package org.xxpay.dubbo.web.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.MyLog;
import org.xxpay.common.util.RpcUtil;
import org.xxpay.common.util.XXPayUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/9
 * @description:
 */
@Service
public class PayOrderService {

    private static final MyLog _log = MyLog.getLog(PayOrderService.class);

    @Autowired
    private RpcCommonService rpcCommonService;

    public int create(JSONObject payOrder) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("payOrder", payOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = rpcCommonService.rpcPayOrderService.create(jsonParam);
        String s = RpcUtil.mkRet(result);
        if(s == null) return 0;
        return Integer.parseInt(s);
    }

    public JSONObject query(String mchId, String payOrderId, String mchOrderNo, String executeNotify) {
        Map<String,Object> paramMap = new HashMap<>();
        Map<String, Object> result;
        if(StringUtils.isNotBlank(payOrderId)) {
            paramMap.put("mchId", mchId);
            paramMap.put("payOrderId", payOrderId);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = rpcCommonService.rpcPayOrderService.selectByMchIdAndPayOrderId(jsonParam);
        }else {
            paramMap.put("mchId", mchId);
            paramMap.put("mchOrderNo", mchOrderNo);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = rpcCommonService.rpcPayOrderService.selectByMchIdAndMchOrderNo(jsonParam);
        }
        String s = RpcUtil.mkRet(result);
        if(s == null) return null;
        boolean isNotify = Boolean.parseBoolean(executeNotify);
        JSONObject payOrder = JSONObject.parseObject(s);
        if(isNotify) {
            paramMap = new HashMap<>();
            paramMap.put("payOrderId", payOrderId);
            String jsonParam = RpcUtil.createBaseParam(paramMap);
            result = rpcCommonService.rpcNotifyPayService.sendBizPayNotify(jsonParam);
            s = RpcUtil.mkRet(result);
            _log.info("业务查单完成,并再次发送业务支付通知.发送结果:{}", s);
        }
        return payOrder;
    }

    public String doWxPayReq(String tradeType, JSONObject payOrder, String resKey) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("tradeType", tradeType);
        paramMap.put("payOrder", payOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = rpcCommonService.rpcPayChannel4WxService.doWxPayReq(jsonParam);
        String s = RpcUtil.mkRet(result);
        if(s == null) {
            return XXPayUtil.makeRetData(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, "0111", "调用微信支付失败"), resKey);
        }
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.putAll((Map) result.get("bizResult"));
        return XXPayUtil.makeRetData(map, resKey);
    }

    public String doAliPayReq(String channelId, JSONObject payOrder, String resKey) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("payOrder", payOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result;
        switch (channelId) {
            case PayConstant.PAY_CHANNEL_ALIPAY_MOBILE :
                result = rpcCommonService.rpcPayChannel4AliService.doAliPayMobileReq(jsonParam);
                break;
            case PayConstant.PAY_CHANNEL_ALIPAY_PC :
                result = rpcCommonService.rpcPayChannel4AliService.doAliPayPcReq(jsonParam);
                break;
            case PayConstant.PAY_CHANNEL_ALIPAY_WAP :
                result = rpcCommonService.rpcPayChannel4AliService.doAliPayWapReq(jsonParam);
                break;
            case PayConstant.PAY_CHANNEL_ALIPAY_QR :
                result = rpcCommonService.rpcPayChannel4AliService.doAliPayQrReq(jsonParam);
                break;
            default:
                result = null;
                break;
        }
        String s = RpcUtil.mkRet(result);
        if(s == null) {
            return XXPayUtil.makeRetData(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_FAIL, "0111", "调用支付宝支付失败"), resKey);
        }
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.putAll((Map) result.get("bizResult"));
        return XXPayUtil.makeRetData(map, resKey);
    }

}
