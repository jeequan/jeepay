package org.xxpay.dubbo.api.service;

import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/10
 * @description:
 */
public interface IPayChannel4AliService {

    Map doAliPayWapReq(String jsonParam);

    Map doAliPayPcReq(String jsonParam);

    Map doAliPayMobileReq(String jsonParam);

    Map doAliPayQrReq(String jsonParam);

    Map doAliTransReq(String jsonParam);

    Map getAliTransReq(String jsonParam);

    Map doAliRefundReq(String jsonParam);

    Map getAliRefundReq(String jsonParam);

}
