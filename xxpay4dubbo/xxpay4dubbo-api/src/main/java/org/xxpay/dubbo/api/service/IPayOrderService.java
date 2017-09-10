package org.xxpay.dubbo.api.service;

import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/8
 * @description:
 */
public interface IPayOrderService {

    public Map createPayOrder(String jsonParam);

    public Map selectPayOrder(String jsonParam);

    public Map selectPayOrderByMchIdAndPayOrderId(String jsonParam);

    public Map selectPayOrderByMchIdAndMchOrderNo(String jsonParam);

    public Map updateStatus4Ing(String jsonParam);

    public Map updateStatus4Success(String jsonParam);

    public Map updateStatus4Complete(String jsonParam);

    public Map updateNotify(String jsonParam);

}
