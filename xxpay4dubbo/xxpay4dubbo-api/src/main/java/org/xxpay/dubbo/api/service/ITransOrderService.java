package org.xxpay.dubbo.api.service;

import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/10/26
 * @description: 转账业务
 */
public interface ITransOrderService {

    Map create(String jsonParam);

    Map select(String jsonParam);

    Map selectByMchIdAndTransOrderId(String jsonParam);

    Map selectByMchIdAndMchTransNo(String jsonParam);

    Map updateStatus4Ing(String jsonParam);

    Map updateStatus4Success(String jsonParam);

    Map updateStatus4Complete(String jsonParam);

    Map sendTransNotify(String jsonParam);

}
