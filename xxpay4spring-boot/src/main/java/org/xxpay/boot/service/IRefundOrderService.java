package org.xxpay.boot.service;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 * @author: dingzhiwei
 * @date: 17/10/26
 * @description: 退款业务
 */
public interface IRefundOrderService {

    Map create(String jsonParam);

    Map select(String jsonParam);

    Map selectByMchIdAndRefundOrderId(String jsonParam);

    Map selectByMchIdAndMchRefundNo(String jsonParam);

    Map updateStatus4Ing(String jsonParam);

    Map updateStatus4Success(String jsonParam);

    Map updateStatus4Complete(String jsonParam);

    // Map sendRefundNotify(String jsonParam);
    void sendRefundNotify(String refundOrderId, String channelName);
    
    int createRefundOrder(JSONObject refundOrder);

}
