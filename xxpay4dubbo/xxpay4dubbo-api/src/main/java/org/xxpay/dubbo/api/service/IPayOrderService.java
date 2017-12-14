package org.xxpay.dubbo.api.service;

import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/8
 * @description:
 */
public interface IPayOrderService {

    Map create(String jsonParam);

    Map select(String jsonParam);

    Map selectByMchIdAndPayOrderId(String jsonParam);

    Map selectByMchIdAndMchOrderNo(String jsonParam);

    Map updateStatus4Ing(String jsonParam);

    Map updateStatus4Success(String jsonParam);

    Map updateStatus4Complete(String jsonParam);

    Map updateNotify(String jsonParam);

}
