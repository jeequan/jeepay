package org.xxpay.dubbo.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.RpcUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/10
 * @description:
 */
@Service
public class NotifyPayService {

    @Autowired
    private RpcCommonService rpcCommonService;

    public String doAliPayNotify(Map params) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("params", params);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map<String, Object> result = rpcCommonService.rpcNotifyPayService.doAliPayNotify(jsonParam);
        String s = RpcUtil.mkRet(result);
        if(s == null) {
            return PayConstant.RETURN_ALIPAY_VALUE_FAIL;
        }
        return s;
    }

    public String doWxPayNotify(String xmlResult) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("xmlResult", xmlResult);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        // 返回给微信的数据格式已经有service处理(包括正确与错误),肯定会返回result
        Map<String, Object> result = rpcCommonService.rpcNotifyPayService.doWxPayNotify(jsonParam);
        return RpcUtil.mkRet(result);
    }

}
